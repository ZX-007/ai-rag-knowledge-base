/**
 * 流式响应解析器工具
 * 
 * 提供统一的接口来解析不同 LLM 提供商的流式响应格式
 * 支持动态适配多种响应结构，确保在切换模型时仍能正常工作
 */

export interface StreamChunk {
  content?: string;
  thinking?: string;
  done?: boolean;
}

export interface ParsedStreamData {
  content: string;
  finished: boolean;
  metadata?: Record<string, any>;
}

/**
 * 通用流式数据解析器
 * 
 * 支持多种常见的流式响应格式：
 * - OpenAI 格式：{ choices: [{ delta: { content } }] }
 * - Anthropic 格式：{ delta: { text } }
 * - 自定义格式：{ result: { output: { content } } }
 * - 简单格式：{ content, text, message }
 */
export class StreamParser {
  private inThinking = false;

  /**
   * 解析流式JSON数据，提取内容
   */
  parseContent(parsed: any): string {
    // 优先级1: 标准化的 result.output.content（后端自定义格式）
    if (parsed.result?.output?.content) {
      return parsed.result.output.content;
    }

    // 优先级2: results 数组格式
    if (Array.isArray(parsed.results) && parsed.results.length > 0) {
      const firstResult = parsed.results[0];
      if (firstResult.output?.content) {
        return firstResult.output.content;
      }
    }

    // 优先级3: OpenAI 格式
    if (parsed.choices && Array.isArray(parsed.choices) && parsed.choices.length > 0) {
      const choice = parsed.choices[0];
      if (choice.delta?.content) {
        return choice.delta.content;
      }
      if (choice.message?.content) {
        return choice.message.content;
      }
    }

    // 优先级4: Anthropic Claude 格式
    if (parsed.delta?.text) {
      return parsed.delta.text;
    }
    if (parsed.content_block?.text) {
      return parsed.content_block.text;
    }

    // 优先级5: 通用字段
    if (typeof parsed.content === 'string') {
      return parsed.content;
    }
    if (typeof parsed.text === 'string') {
      return parsed.text;
    }
    if (typeof parsed.message === 'string') {
      return parsed.message;
    }

    // 优先级6: 嵌套 data 字段
    if (parsed.data?.content) {
      return parsed.data.content;
    }
    if (parsed.data?.text) {
      return parsed.data.text;
    }

    return '';
  }

  /**
   * 检查是否完成
   */
  isFinished(parsed: any): boolean {
    // 检查各种可能的完成标志
    if (parsed.result?.metadata?.finishReason) {
      return true;
    }
    if (parsed.results && parsed.results[0]?.metadata?.finishReason) {
      return true;
    }
    if (parsed.choices && parsed.choices[0]?.finish_reason) {
      return true;
    }
    if (parsed.done === true) {
      return true;
    }
    if (parsed.type === 'message_stop' || parsed.type === 'content_block_stop') {
      return true;
    }
    return false;
  }

  /**
   * 解析思考标签，返回内容类型和实际内容
   * 
   * 支持 <think>...</think> 标签来标识思考过程
   */
  *parseThinkingTags(content: string): Generator<StreamChunk, void, unknown> {
    // 处理 <think> 标签
    if (content.includes('<think>')) {
      this.inThinking = true;
      const parts = content.split('<think>');
      
      // <think> 之前的内容作为普通内容输出
      if (parts[0]) {
        yield { content: parts[0] };
      }
      
      // <think> 之后的内容
      const afterStart = parts[1] ?? '';
      if (afterStart) {
        // 检查是否在同一块中就有 </think>
        if (afterStart.includes('</think>')) {
          const [thinkContent, normalContent] = afterStart.split('</think>');
          if (thinkContent) {
            yield { thinking: thinkContent };
          }
          this.inThinking = false;
          if (normalContent) {
            yield { content: normalContent };
          }
        } else {
          yield { thinking: afterStart };
        }
      }
      return;
    }

    // 处理 </think> 标签
    if (content.includes('</think>')) {
      const [beforeEnd, afterEnd] = content.split('</think>');
      
      if (this.inThinking && beforeEnd) {
        yield { thinking: beforeEnd };
      }
      
      this.inThinking = false;
      
      if (afterEnd) {
        yield { content: afterEnd };
      }
      return;
    }

    // 没有标签，根据当前状态决定
    if (this.inThinking) {
      yield { thinking: content };
    } else {
      yield { content: content };
    }
  }

  /**
   * 重置解析器状态
   */
  reset(): void {
    this.inThinking = false;
  }

  /**
   * 获取当前思考状态
   */
  isInThinking(): boolean {
    return this.inThinking;
  }
}

/**
 * 创建流式解析器实例
 */
export function createStreamParser(): StreamParser {
  return new StreamParser();
}

/**
 * 解析 SSE（Server-Sent Events）数据行
 */
export function parseSSELine(line: string): { isData: boolean; content: string; isDone: boolean } {
  const trimmed = line.trim();
  
  if (trimmed === '') {
    return { isData: false, content: '', isDone: false };
  }

  if (trimmed.startsWith('data:')) {
    const data = trimmed.slice(5).trim();
    
    if (data === '[DONE]' || data === 'DONE') {
      return { isData: true, content: '', isDone: true };
    }
    
    return { isData: true, content: data, isDone: false };
  }

  // 其他类型的 SSE 行（id:, event:, retry: 等）
  return { isData: false, content: '', isDone: false };
}

