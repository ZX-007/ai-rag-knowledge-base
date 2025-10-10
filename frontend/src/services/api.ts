import { ApiResponse, ChatRequest, RagChatRequest, GitRepositoryRequest } from '../types';
import { createStreamParser, parseSSELine } from '../utils/streamParser';
import { createNetworkError, createApiError, logError } from '../utils/errorHandler';

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
    try {
      const response = await withTimeout(`${API_BASE_URL}/chat/models`);
      ensureOk(response);
      const result: ApiResponse<string[]> = await response.json();
      
      if (!isSuccessCode(result.code)) {
        throw createApiError(result.info);
      }
      
      return result.data;
    } catch (error) {
      logError(error, 'getAvailableModels');
      if (error instanceof Error && error.message.includes('fetch')) {
        throw createNetworkError('无法连接到服务器，请检查后端服务是否正常运行', error);
      }
      throw error;
    }
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
    try {
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
        throw createApiError(result.info);
      }
      return result.data;
    } catch (error) {
      logError(error, 'analyzeGitRepository');
      if (error instanceof Error && error.message.includes('fetch')) {
        throw createNetworkError('无法连接到服务器进行Git仓库分析', error);
      }
      throw error;
    }
  }

  /**
   * 流式聊天（使用统一解析器）
   */
  static async *streamChat(request: ChatRequest, abortSignal?: AbortSignal): AsyncGenerator<{content: string, thinking?: string}, void, unknown> {
    const response = await fetch(`${API_BASE_URL}/chat/generate_stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream',
      },
      body: JSON.stringify(request),
      signal: abortSignal,
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const reader = response.body?.getReader();
    if (!reader) {
      throw new Error('无法获取响应流');
    }

    const decoder = new TextDecoder();
    const parser = createStreamParser();
    let buffer = '';

    try {
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';

        for (const line of lines) {
          const { isData, content: sseContent, isDone } = parseSSELine(line);
          
          if (isDone) {
            return;
          }
          
          if (!isData || !sseContent) {
            continue;
          }
          
          try {
            const parsed = JSON.parse(sseContent);
            
            // 使用统一解析器提取内容
            const content = parser.parseContent(parsed);
            
            if (content) {
              // 解析思考标签并输出
              for (const chunk of parser.parseThinkingTags(content)) {
                if (chunk.content) {
                  yield { content: chunk.content };
                }
                if (chunk.thinking) {
                  yield { content: '', thinking: chunk.thinking };
                }
              }
            }
            
            // 检查是否完成
            if (parser.isFinished(parsed)) {
              return;
            }
          } catch (e) {
            console.warn('解析流式数据失败:', e, sseContent);
          }
        }
      }
    } finally {
      reader.releaseLock();
    }
  }

  /**
   * RAG流式聊天（使用统一解析器）
   */
  static async *streamRagChat(request: RagChatRequest, abortSignal?: AbortSignal): AsyncGenerator<{content: string, thinking?: string}, void, unknown> {
    const response = await fetch(`${API_BASE_URL}/chat/generate_stream_rag`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream',
      },
      body: JSON.stringify(request),
      signal: abortSignal,
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const reader = response.body?.getReader();
    if (!reader) {
      throw new Error('无法获取响应流');
    }

    const decoder = new TextDecoder();
    const parser = createStreamParser();
    let buffer = '';

    try {
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';

        for (const line of lines) {
          const { isData, content: sseContent, isDone } = parseSSELine(line);
          
          if (isDone) {
            return;
          }
          
          if (!isData || !sseContent) {
            continue;
          }
          
          try {
            const parsed = JSON.parse(sseContent);
            
            // 使用统一解析器提取内容
            const content = parser.parseContent(parsed);
            
            if (content) {
              // 解析思考标签并输出
              for (const chunk of parser.parseThinkingTags(content)) {
                if (chunk.content) {
                  yield { content: chunk.content };
                }
                if (chunk.thinking) {
                  yield { content: '', thinking: chunk.thinking };
                }
              }
            }
            
            // 检查是否完成
            if (parser.isFinished(parsed)) {
              return;
            }
          } catch (e) {
            console.warn('解析RAG流式数据失败:', e, sseContent);
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
    try {
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
        throw createApiError(result.info);
      }
      
      return result.data;
    } catch (error) {
      logError(error, 'uploadFiles');
      if (error instanceof Error && error.message.includes('fetch')) {
        throw createNetworkError('文件上传失败，请检查网络连接', error);
      }
      throw error;
    }
  }
}