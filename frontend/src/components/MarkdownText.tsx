import React from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import remarkMath from 'remark-math';
import rehypeKatex from 'rehype-katex';
import rehypeHighlight from 'rehype-highlight';
import 'highlight.js/styles/github.css';
import 'github-markdown-css/github-markdown.css';
import 'katex/dist/katex.min.css';
import '../styles/markdown.css';

interface MarkdownTextProps {
  /**
   * 要渲染的Markdown文本内容
   */
  content: string;
  /**
   * 是否处于流式渲染中，用于显示光标指示
   */
  isStreaming?: boolean;
  /**
   * 额外容器类名（可选），用于局部覆盖样式
   */
  className?: string;
}

/**
 * MarkdownText 组件
 *
 * 用途：在对话消息中渲染支持 GFM（GitHub Flavored Markdown）的 Markdown 文本，
 * 包括表格、任务列表、删除线、代码块等常见语法。
 *
 * 参数说明：
 * - content: 待渲染的 Markdown 字符串。
 * - isStreaming: 是否处于流式状态，若为 true 将在末尾显示一个闪烁光标以提示正在更新。
 *
 * 返回值：渲染后的 React 节点。
 */
/**
 * MarkdownText 组件
 *
 * 增强版 Markdown 渲染器，支持：
 * - GFM（GitHub Flavored Markdown）：表格、任务列表、删除线等
 * - 数学公式（LaTeX）：行内公式和块级公式
 * - 代码高亮：自动检测语言并高亮显示
 * - 自定义组件：优化链接、图片等元素的渲染
 *
 * @param content Markdown 文本内容
 * @param isStreaming 是否处于流式状态（末尾显示闪烁光标）
 * @param className 可选的容器类名，用于覆盖默认样式
 * @returns 渲染后的 React 节点
 */
const MarkdownText: React.FC<MarkdownTextProps> = ({ content, isStreaming = false, className }) => {
  return (
    <div className={`markdown-body ${className ?? ''}`}>
      <ReactMarkdown 
        remarkPlugins={[remarkGfm, remarkMath]} 
        rehypePlugins={[rehypeKatex, rehypeHighlight]}
        components={{
          // 优化代码块渲染
          code({ node, inline, className, children, ...props }: any) {
            return !inline ? (
              <pre className={className}>
                <code className={className} {...props}>
                  {children}
                </code>
              </pre>
            ) : (
              <code className={className} {...props}>
                {children}
              </code>
            );
          },
          // 优化链接渲染（在新标签页打开外部链接）
          a({ node, href, children, ...props }: any) {
            const isExternal = href && (href.startsWith('http://') || href.startsWith('https://'));
            return (
              <a 
                href={href} 
                target={isExternal ? '_blank' : undefined}
                rel={isExternal ? 'noopener noreferrer' : undefined}
                {...props}
              >
                {children}
              </a>
            );
          }
        }}
      >
        {content}
      </ReactMarkdown>
      {isStreaming && <span className="streaming-cursor" />}
    </div>
  );
};

export default MarkdownText;