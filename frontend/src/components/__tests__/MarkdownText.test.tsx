import { describe, it, expect } from 'vitest';
import { renderToString } from 'react-dom/server';
import MarkdownText from '../MarkdownText';

describe('MarkdownText', () => {
  it('应渲染基本Markdown并带有样式类', () => {
    const html = renderToString(<MarkdownText content={'# 标题\n\n普通段落文本'} />);
    expect(html).toContain('markdown-body');
    expect(html).toContain('<h1>');
    expect(html).toContain('<p>');
  });

  it('在流式模式下应显示光标指示', () => {
    const html = renderToString(<MarkdownText content={'内容'} isStreaming />);
    expect(html).toContain('streaming-cursor');
  });

  it('代码块应添加高亮类名', () => {
    const md = '```js\nconsole.log(123)\n```';
    const html = renderToString(<MarkdownText content={md} />);
    // rehype-highlight 会为 <code> 添加 hljs 类名
    expect(html).toContain('<code class="hljs');
  });
});