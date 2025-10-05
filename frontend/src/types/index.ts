/**
 * 消息类型
 */
export interface Message {
  id: string;
  type: 'user' | 'assistant';
  content: string;
  thinkingContent?: string; // 新增：思考内容
  /**
   * 思考开始时间戳（毫秒）
   * 当收到第一段 <think> 内容时记录，用于计时展示
   */
  thinkingStartAt?: number;
  /**
   * 思考结束时间戳（毫秒）
   * 在一次流式会话完成时记录，用于展示最终耗时
   */
  thinkingEndAt?: number;
  timestamp: number;
  streaming?: boolean;
}

/**
 * 聊天请求
 */
export interface ChatRequest {
  model: string;
  message: string;
}

/**
 * RAG聊天请求
 */
export interface RagChatRequest extends ChatRequest {
  ragTag: string;
}

/**
 * 流式响应数据结构
 */
export interface StreamResponse {
  result?: {
    metadata?: {
      contentFilterMetadata?: any;
      finishReason?: string | null;
    };
    output?: {
      messageType?: string;
      properties?: Record<string, any>;
      content?: string;
      media?: any[];
    };
  };
  metadata?: {
    usage?: {
      generationTokens?: number;
      promptTokens?: number;
      totalTokens?: number;
    };
    rateLimit?: {
      requestsReset?: string;
      requestsLimit?: number;
      tokensLimit?: number;
      tokensReset?: string;
      tokensRemaining?: number;
      requestsRemaining?: number;
    };
    promptMetadata?: any[];
  };
  results?: Array<{
    metadata?: {
      contentFilterMetadata?: any;
      finishReason?: string | null;
    };
    output?: {
      messageType?: string;
      properties?: Record<string, any>;
      content?: string;
      media?: any[];
    };
  }>;
}

/**
 * API响应 - 匹配后端实际返回格式
 */
export interface ApiResponse<T = any> {
  code: string;
  info: string;
  data: T;
  timestamp: string;
  traceId?: string | null;
}

/**
 * 文件上传请求
 */
export interface FileUploadRequest {
  ragTag: string;
  files: File[];
}

/**
 * Git 仓库分析请求
 *
 * 用于触发后端对 Git 仓库的分析与导入知识库。
 * - repoUrl: 仓库地址（支持 https/ssh）
 * - userName: 私有仓库用户名（可选）
 * - token: 私有仓库访问令牌或密码（可选）
 */
export interface GitRepositoryRequest {
  /** 仓库地址，如：https://github.com/org/repo.git */
  repoUrl: string;
  /** 私有仓库用户名（可选） */
  userName?: string;
  /** 私有仓库访问令牌或密码（可选） */
  token?: string;
}

/**
 * 应用状态
 */
export interface AppState {
  models: string[];
  ragTags: string[];
  selectedModel: string;
  selectedRagTag: string | null;
  messages: Message[];
  isStreaming: boolean;
  currentStreamingMessageId: string | null;
}