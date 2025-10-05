import React from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeHighlight from 'rehype-highlight';
import 'highlight.js/styles/github.css';
import 'github-markdown-css/github-markdown.css';
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
 * 使用 react-markdown + remark-gfm + rehype-highlight 渲染 Markdown 文本，
 * 并应用统一样式类 .markdown-body 提升美观性与缩进表现。
 *
 * @param content Markdown 文本内容
 * @param isStreaming 是否处于流式状态（末尾显示闪烁光标）
 * @param className 可选的容器类名，用于覆盖默认样式
 * @returns 渲染后的 React 节点
 */
const MarkdownText: React.FC<MarkdownTextProps> = ({ content, isStreaming = false, className }) => {
  return (
    <div className={`markdown-body ${className ?? ''}`}>
      <ReactMarkdown remarkPlugins={[remarkGfm]} rehypePlugins={[rehypeHighlight]}>
        {content}
      </ReactMarkdown>
      {isStreaming && <span className="streaming-cursor" />}
    </div>
  );
};

export default MarkdownText;