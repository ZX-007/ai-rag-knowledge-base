import { describe, it, expect } from 'vitest';
import { createStreamParser, parseSSELine } from '../streamParser';

describe('StreamParser', () => {
  describe('parseContent', () => {
    it('应该解析自定义格式 result.output.content', () => {
      const parser = createStreamParser();
      const data = {
        result: {
          output: {
            content: 'Hello World'
          }
        }
      };
      expect(parser.parseContent(data)).toBe('Hello World');
    });

    it('应该解析 results 数组格式', () => {
      const parser = createStreamParser();
      const data = {
        results: [
          {
            output: {
              content: 'Test Message'
            }
          }
        ]
      };
      expect(parser.parseContent(data)).toBe('Test Message');
    });

    it('应该解析 OpenAI 格式', () => {
      const parser = createStreamParser();
      const data = {
        choices: [
          {
            delta: {
              content: 'OpenAI response'
            }
          }
        ]
      };
      expect(parser.parseContent(data)).toBe('OpenAI response');
    });

    it('应该解析 Anthropic 格式', () => {
      const parser = createStreamParser();
      const data = {
        delta: {
          text: 'Anthropic response'
        }
      };
      expect(parser.parseContent(data)).toBe('Anthropic response');
    });

    it('应该解析简单格式', () => {
      const parser = createStreamParser();
      expect(parser.parseContent({ content: 'Simple' })).toBe('Simple');
      expect(parser.parseContent({ text: 'Text' })).toBe('Text');
      expect(parser.parseContent({ message: 'Message' })).toBe('Message');
    });

    it('无法解析时应返回空字符串', () => {
      const parser = createStreamParser();
      expect(parser.parseContent({})).toBe('');
      expect(parser.parseContent({ unknown: 'field' })).toBe('');
    });
  });

  describe('parseThinkingTags', () => {
    it('应该解析包含 <think> 标签的内容', () => {
      const parser = createStreamParser();
      const chunks = Array.from(parser.parseThinkingTags('<think>thinking</think>normal'));
      
      expect(chunks).toHaveLength(2);
      expect(chunks[0]).toEqual({ thinking: 'thinking' });
      expect(chunks[1]).toEqual({ content: 'normal' });
      expect(parser.isInThinking()).toBe(false);
    });

    it('应该处理跨多次调用的思考标签', () => {
      const parser = createStreamParser();
      
      // 第一次：开始思考
      const chunks1 = Array.from(parser.parseThinkingTags('<think>part1'));
      expect(chunks1[0]).toEqual({ thinking: 'part1' });
      expect(parser.isInThinking()).toBe(true);
      
      // 第二次：继续思考
      const chunks2 = Array.from(parser.parseThinkingTags(' part2'));
      expect(chunks2[0]).toEqual({ thinking: ' part2' });
      expect(parser.isInThinking()).toBe(true);
      
      // 第三次：结束思考
      const chunks3 = Array.from(parser.parseThinkingTags('</think>content'));
      expect(chunks3[0]).toEqual({ content: 'content' });
      expect(parser.isInThinking()).toBe(false);
    });

    it('应该处理没有标签的普通内容', () => {
      const parser = createStreamParser();
      const chunks = Array.from(parser.parseThinkingTags('normal content'));
      
      expect(chunks).toHaveLength(1);
      expect(chunks[0]).toEqual({ content: 'normal content' });
    });
  });

  describe('isFinished', () => {
    it('应该检测各种完成标志', () => {
      const parser = createStreamParser();
      
      expect(parser.isFinished({
        result: { metadata: { finishReason: 'stop' } }
      })).toBe(true);
      
      expect(parser.isFinished({
        choices: [{ finish_reason: 'stop' }]
      })).toBe(true);
      
      expect(parser.isFinished({ done: true })).toBe(true);
      expect(parser.isFinished({ type: 'message_stop' })).toBe(true);
      expect(parser.isFinished({})).toBe(false);
    });
  });
});

describe('parseSSELine', () => {
  it('应该解析 data: 行', () => {
    const result = parseSSELine('data: {"content":"test"}');
    expect(result.isData).toBe(true);
    expect(result.content).toBe('{"content":"test"}');
    expect(result.isDone).toBe(false);
  });

  it('应该识别 [DONE] 标记', () => {
    const result = parseSSELine('data: [DONE]');
    expect(result.isData).toBe(true);
    expect(result.isDone).toBe(true);
  });

  it('应该忽略空行', () => {
    const result = parseSSELine('');
    expect(result.isData).toBe(false);
  });

  it('应该忽略其他 SSE 类型', () => {
    expect(parseSSELine('event: message').isData).toBe(false);
    expect(parseSSELine('id: 123').isData).toBe(false);
    expect(parseSSELine('retry: 1000').isData).toBe(false);
  });
});

