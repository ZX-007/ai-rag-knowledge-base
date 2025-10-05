import { ApiResponse, ChatRequest, RagChatRequest, GitRepositoryRequest } from '../types';

// 可通过环境变量配置 API 前缀与请求超时时间
const API_BASE_URL = (typeof import.meta !== 'undefined' && import.meta.env && import.meta.env.VITE_API_PREFIX) ? import.meta.env.VITE_API_PREFIX : '/api/v1';
const DEFAULT_TIMEOUT_MS = (typeof import.meta !== 'undefined' && import.meta.env && import.meta.env.VITE_API_TIMEOUT_MS) ? Number(import.meta.env.VITE_API_TIMEOUT_MS) : 15000;

function withTimeout(input: RequestInfo | URL, init?: RequestInit, timeoutMs: number = DEFAULT_TIMEOUT_MS): Promise<Response> {
  const controller = new AbortController();
  const id = setTimeout(() => controller.abort(), timeoutMs);
  const mergedInit: RequestInit = { ...init, signal: controller.signal };
  return fetch(input, mergedInit).finally(() => clearTimeout(id));
}

function ensureOk(response: Response) {
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }
}

// 适配后端可能返回的成功码（2000 或 20000）
const isSuccessCode = (code: string | undefined) => code === '2000' || code === '20000';

/**
 * API服务类
 */
export class ApiService {
  /**
   * 查询可用模型列表
   */
  static async getAvailableModels(): Promise<string[]> {
    const response = await withTimeout(`${API_BASE_URL}/ollama/models`);
    ensureOk(response);
    const result: ApiResponse<string[]> = await response.json();
    
    if (!isSuccessCode(result.code)) {
      throw new Error(result.info);
    }
    
    return result.data;
  }

  /**
   * 查询RAG标签列表
   */
  static async getRagTags(): Promise<string[]> {
    try {
      const response = await fetch(`${API_BASE_URL}/rag/query_rag_tag_list`);
      if (!response.ok) {
        console.debug('getRagTags: 后端不可达或返回非200，降级为空列表', response.status, response.statusText);
        return [];
      }

      const result: ApiResponse<string[]> = await response.json();

      if (!isSuccessCode(result.code)) {
        console.debug('getRagTags: 非成功业务码，降级为空列表', result.code, result.info);
        return [];
      }

      return Array.isArray(result.data) ? result.data : [];
    } catch (err) {
      // 网络错误（如后端未启动）或请求被中断时，降级为空列表，避免控制台抛出错误堆栈
      console.debug('getRagTags: 请求失败，降级为空列表', err);
      return [];
    }
  }

  /**
   * 分析 Git 仓库并导入知识库
   *
   * 向后端触发 Git 仓库分析流程，返回后端处理结果字符串。
   * @param request Git 仓库分析请求体
   * @returns 后端返回的结果描述字符串
   */
  static async analyzeGitRepository(request: GitRepositoryRequest): Promise<string> {
    const response = await withTimeout(`${API_BASE_URL}/rag/analyze_git_repository`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    });

    ensureOk(response);
    const result: ApiResponse<string> = await response.json();
    if (!isSuccessCode(result.code)) {
      throw new Error(result.info);
    }
    return result.data;
  }

  /**
   * 流式聊天
   */
  static async *streamChat(request: ChatRequest): AsyncGenerator<{content: string, thinking?: string}, void, unknown> {
    const response = await fetch(`${API_BASE_URL}/ollama/generate_stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream',
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const reader = response.body?.getReader();
    if (!reader) {
      throw new Error('无法获取响应流');
    }

    const decoder = new TextDecoder();
    let buffer = '';

    // 流式解析状态机：跟踪是否处于<think>思考内容段
    let inThinking = false;

    try {
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';

        for (const line of lines) {
          if (line.trim() === '') continue;
          
          if (line.startsWith('data:')) {
            const data = line.slice(5).trim();
            if (data === '[DONE]') {
              return;
            }
            
            try {
              const parsed = JSON.parse(data);
              console.log('解析的流式数据:', parsed);
              
              // 处理后端实际返回的数据结构
              let content = '';
              
              // 优先从 result.output.content 获取内容
              if (parsed.result?.output?.content) {
                content = parsed.result.output.content;
              }
              // 如果没有，尝试从 results 数组中获取
              else if (parsed.results && parsed.results.length > 0) {
                const firstResult = parsed.results[0];
                if (firstResult.output?.content) {
                  content = firstResult.output.content;
                }
              }
              
              if (content) {
                // 可能出现："<think>"、思考片段、"</think>"，或普通正文片段；需要按状态机处理
                // 1) 包含起始标签：进入思考状态，并处理其后的内容
                if (content.includes('<think>')) {
                  inThinking = true;
                  const afterStart = content.split('<think>')[1] ?? '';
                  if (afterStart) {
                    yield { content: '', thinking: afterStart };
                  }
                  continue; // 当前行剩余已处理
                }

                // 2) 包含结束标签：输出结束标签之前的思考内容，并退出思考状态；结束标签之后若有正文则输出
                if (content.includes('</think>')) {
                  const [beforeEnd, afterEnd] = content.split('</think>');
                  if (inThinking && beforeEnd) {
                    yield { content: '', thinking: beforeEnd };
                  }
                  inThinking = false;
                  if (afterEnd) {
                    yield { content: afterEnd };
                  }
                  continue;
                }

                // 3) 不含任何标签：按当前状态决定归属
                if (inThinking) {
                  yield { content: '', thinking: content };
                } else {
                  yield { content: content };
                }
              }
              
              // 检查是否完成
              if (parsed.result?.metadata?.finishReason || 
                  (parsed.results && parsed.results[0]?.metadata?.finishReason)) {
                return;
              }
            } catch (e) {
              console.warn('解析流式数据失败:', e, data);
            }
          }
        }
      }
    } finally {
      reader.releaseLock();
    }
  }

  /**
   * RAG流式聊天
   */
  static async *streamRagChat(request: RagChatRequest): AsyncGenerator<{content: string, thinking?: string}, void, unknown> {
    const response = await fetch(`${API_BASE_URL}/ollama/generate_stream_rag`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream',
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const reader = response.body?.getReader();
    if (!reader) {
      throw new Error('无法获取响应流');
    }

    const decoder = new TextDecoder();
    let buffer = '';

    // 流式解析状态机：跟踪是否处于<think>思考内容段
    let inThinking = false;

    try {
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';

        for (const line of lines) {
          if (line.trim() === '') continue;
          
          if (line.startsWith('data:')) {
            const data = line.slice(5).trim();
            if (data === '[DONE]') {
              return;
            }
            
            try {
              const parsed = JSON.parse(data);
              console.log('解析的RAG流式数据:', parsed);
              
              // 处理后端实际返回的数据结构
              let content = '';
              
              // 优先从 result.output.content 获取内容
              if (parsed.result?.output?.content) {
                content = parsed.result.output.content;
              }
              // 如果没有，尝试从 results 数组中获取
              else if (parsed.results && parsed.results.length > 0) {
                const firstResult = parsed.results[0];
                if (firstResult.output?.content) {
                  content = firstResult.output.content;
                }
              }
              
              if (content) {
                // 可能出现："<think>"、思考片段、"</think>"，或普通正文片段；需要按状态机处理
                if (content.includes('<think>')) {
                  inThinking = true;
                  const afterStart = content.split('<think>')[1] ?? '';
                  if (afterStart) {
                    yield { content: '', thinking: afterStart };
                  }
                  continue;
                }

                if (content.includes('</think>')) {
                  const [beforeEnd, afterEnd] = content.split('</think>');
                  if (inThinking && beforeEnd) {
                    yield { content: '', thinking: beforeEnd };
                  }
                  inThinking = false;
                  if (afterEnd) {
                    yield { content: afterEnd };
                  }
                  continue;
                }

                if (inThinking) {
                  yield { content: '', thinking: content };
                } else {
                  yield { content: content };
                }
              }
              
              // 检查是否完成
              if (parsed.result?.metadata?.finishReason || 
                  (parsed.results && parsed.results[0]?.metadata?.finishReason)) {
                return;
              }
            } catch (e) {
              console.warn('解析RAG流式数据失败:', e, data);
            }
          }
        }
      }
    } finally {
      reader.releaseLock();
    }
  }

  /**
   * 上传文件到知识库
   */
  static async uploadFiles(ragTag: string, files: File[]): Promise<string> {
    const formData = new FormData();
    formData.append('ragTag', ragTag);
    
    files.forEach(file => {
      formData.append('files', file);
    });

    const response = await fetch(`${API_BASE_URL}/rag/file/upload`, {
      method: 'POST',
      body: formData,
    });

    const result: ApiResponse<string> = await response.json();
    
    if (!isSuccessCode(result.code)) {
      throw new Error(result.info);
    }
    
    return result.data;
  }
}