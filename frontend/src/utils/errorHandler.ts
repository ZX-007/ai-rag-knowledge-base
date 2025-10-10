/**
 * 错误处理工具
 * 提供统一的错误处理和用户友好的错误消息
 */

/**
 * 错误类型枚举
 */
export enum ErrorType {
  NETWORK = 'network',
  API = 'api',
  VALIDATION = 'validation',
  UNKNOWN = 'unknown',
}

/**
 * 自定义错误类
 */
export class AppError extends Error {
  type: ErrorType;
  originalError?: Error;

  constructor(message: string, type: ErrorType = ErrorType.UNKNOWN, originalError?: Error) {
    super(message);
    this.name = 'AppError';
    this.type = type;
    this.originalError = originalError;
  }
}

/**
 * 解析错误并返回用户友好的消息
 */
export function parseError(error: unknown): { message: string; type: ErrorType } {
  // 已经是 AppError
  if (error instanceof AppError) {
    return { message: error.message, type: error.type };
  }

  // 标准 Error 对象
  if (error instanceof Error) {
    // 网络错误
    if (error.name === 'TypeError' && error.message.includes('fetch')) {
      return {
        message: '网络连接失败，请检查网络设置或后端服务是否正常运行',
        type: ErrorType.NETWORK,
      };
    }

    // AbortError（用户中断）
    if (error.name === 'AbortError') {
      return {
        message: '操作已被取消',
        type: ErrorType.UNKNOWN,
      };
    }

    // HTTP 错误
    if (error.message.includes('HTTP error')) {
      const status = error.message.match(/status: (\d+)/)?.[1];
      if (status === '404') {
        return {
          message: '请求的资源不存在，请检查API配置',
          type: ErrorType.API,
        };
      }
      if (status === '500') {
        return {
          message: '服务器内部错误，请稍后重试',
          type: ErrorType.API,
        };
      }
      if (status === '401' || status === '403') {
        return {
          message: '无权访问，请检查认证信息',
          type: ErrorType.API,
        };
      }
      return {
        message: `服务器返回错误: ${status}`,
        type: ErrorType.API,
      };
    }

    // 超时错误
    if (error.message.includes('timeout') || error.message.includes('timed out')) {
      return {
        message: '请求超时，请检查网络连接或稍后重试',
        type: ErrorType.NETWORK,
      };
    }

    // 其他错误
    return {
      message: error.message || '发生未知错误',
      type: ErrorType.UNKNOWN,
    };
  }

  // 字符串错误
  if (typeof error === 'string') {
    return {
      message: error,
      type: ErrorType.UNKNOWN,
    };
  }

  // 未知错误
  return {
    message: '发生未知错误，请稍后重试',
    type: ErrorType.UNKNOWN,
  };
}

/**
 * 创建网络错误
 */
export function createNetworkError(message?: string, originalError?: Error): AppError {
  return new AppError(
    message || '网络连接失败',
    ErrorType.NETWORK,
    originalError
  );
}

/**
 * 创建API错误
 */
export function createApiError(message: string, originalError?: Error): AppError {
  return new AppError(message, ErrorType.API, originalError);
}

/**
 * 创建验证错误
 */
export function createValidationError(message: string): AppError {
  return new AppError(message, ErrorType.VALIDATION);
}

/**
 * 日志记录（开发环境）
 */
export function logError(error: unknown, context?: string): void {
  if (import.meta.env.DEV) {
    console.group(`❌ Error ${context ? `in ${context}` : ''}`);
    console.error(error);
    if (error instanceof AppError && error.originalError) {
      console.error('Original Error:', error.originalError);
    }
    console.groupEnd();
  }
}

