import React, { useEffect, useState, useRef } from 'react';

interface StreamingTextProps {
  content: string;
  isStreaming: boolean;
  onStreamComplete?: () => void;
}

/**
 * 流式文本显示组件
 * 实现实时流式文本显示，无打字机效果以提高响应速度
 */
const StreamingText: React.FC<StreamingTextProps> = ({ 
  content, 
  isStreaming, 
  onStreamComplete 
}) => {
  const [displayedContent, setDisplayedContent] = useState('');
  const contentRef = useRef('');

  // 实时更新显示内容，无延迟
  useEffect(() => {
    if (content !== contentRef.current) {
      contentRef.current = content;
      setDisplayedContent(content);
    }
  }, [content]);

  // 检查流式完成状态
  useEffect(() => {
    if (!isStreaming && content && onStreamComplete) {
      onStreamComplete();
    }
  }, [isStreaming, content, onStreamComplete]);

  // 重置内容时清空显示
  useEffect(() => {
    if (content === '') {
      setDisplayedContent('');
      contentRef.current = '';
    }
  }, [content]);

  return (
    <span>
      {displayedContent}
      {isStreaming && (
        <span className="streaming-cursor" />
      )}
    </span>
  );
};

export default StreamingText;