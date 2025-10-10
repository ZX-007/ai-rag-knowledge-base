import React, { useEffect, useRef, useState } from 'react';
import { Collapse, Space, Typography, Button, message as antMessage, Tooltip } from 'antd';
import { 
  ThunderboltOutlined, 
  CopyOutlined, 
  LikeOutlined,
  LikeFilled,
  DislikeOutlined,
  DislikeFilled,
  ShareAltOutlined,
  ReloadOutlined,
  MoreOutlined 
} from '@ant-design/icons';
import { Message } from '../types';
import MarkdownText from './MarkdownText';
import ThinkingTimer from './ThinkingTimer';

const { Text } = Typography;

interface MessageListProps {
  messages: Message[];
  isStreaming: boolean;
  currentStreamingMessageId: string | null;
}

/**
 * 消息列表组件
 * 显示用户和AI的对话消息，支持展示思考过程
 */
const MessageList: React.FC<MessageListProps> = ({ 
  messages, 
  isStreaming, 
  currentStreamingMessageId 
}) => {
  const messagesEndRef = useRef<HTMLDivElement>(null);
  // 跟踪每个消息的反馈状态：'like' | 'dislike' | null
  const [messageFeedback, setMessageFeedback] = useState<Record<string, 'like' | 'dislike' | null>>({});

  // 自动滚动到底部
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // 复制消息内容
  const handleCopy = async (content: string) => {
    try {
      await navigator.clipboard.writeText(content);
      antMessage.success('已复制到剪贴板');
    } catch (err) {
      antMessage.error('复制失败');
    }
  };

  // 处理点赞
  const handleLike = (messageId: string) => {
    setMessageFeedback(prev => ({
      ...prev,
      [messageId]: prev[messageId] === 'like' ? null : 'like'
    }));
  };

  // 处理点踩
  const handleDislike = (messageId: string) => {
    setMessageFeedback(prev => ({
      ...prev,
      [messageId]: prev[messageId] === 'dislike' ? null : 'dislike'
    }));
  };

  return (
    <div className="chat-messages">
      {messages.map((message) => (
        <div 
          key={message.id} 
          className={`message-item ${message.type === 'user' ? 'message-user' : 'message-assistant'}`}
        >
          {message.type === 'assistant' ? (
            // AI 消息 - 纯文本，无背景
            <div className="ai-message-content">
              {/* 思考过程展示 */}
              {message.thinkingContent && (
                <div className="thinking-section">
                  {/* 时间戳在思考顶部 */}
                  <div className="thinking-timestamp">
                    {new Date(message.timestamp).toLocaleTimeString('zh-CN', { 
                      hour: '2-digit', 
                      minute: '2-digit',
                      second: '2-digit'
                    })}
                  </div>
                  
                  <Collapse 
                    size="small" 
                    expandIconPosition="start"
                    ghost
                    bordered={false}
                    className="thinking-collapse"
                    items={[
                      {
                        key: '1',
                        label: (
                          <Space size={6} className="thinking-label">
                            <ThunderboltOutlined />
                            <Text>
                              已深度思考（用时
                              <ThinkingTimer 
                                startAt={message.thinkingStartAt}
                                endAt={message.thinkingEndAt}
                                running={message.id === currentStreamingMessageId && isStreaming}
                                label=""
                              />
                              秒）
                            </Text>
                          </Space>
                        ),
                        children: (
                          <div className="thinking-content">
                            <MarkdownText 
                              content={message.thinkingContent}
                              isStreaming={message.id === currentStreamingMessageId && isStreaming}
                            />
                          </div>
                        )
                      }
                    ]}
                  />
                </div>
              )}
              
              {/* 正文内容 */}
              <div className="ai-text-content">
                <MarkdownText 
                  content={message.content || (message.id === currentStreamingMessageId && isStreaming ? '正在思考...' : '')}
                  isStreaming={message.id === currentStreamingMessageId && isStreaming && !message.thinkingContent}
                />
              </div>
              
              {/* AI 消息操作按钮 - 只在非流式状态显示 */}
              {!message.streaming && message.content && (
                <div className="ai-message-actions">
                  <Tooltip title="复制" placement="bottom">
                    <Button
                      type="text"
                      size="small"
                      icon={<CopyOutlined />}
                      onClick={() => handleCopy(message.content)}
                      className="action-button"
                    />
                  </Tooltip>
                  <Tooltip title="赞同" placement="bottom">
                    <Button
                      type="text"
                      size="small"
                      icon={messageFeedback[message.id] === 'like' ? <LikeFilled /> : <LikeOutlined />}
                      onClick={() => handleLike(message.id)}
                      className={`action-button ${messageFeedback[message.id] === 'like' ? 'active-like' : ''}`}
                    />
                  </Tooltip>
                  <Tooltip title="反对" placement="bottom">
                    <Button
                      type="text"
                      size="small"
                      icon={messageFeedback[message.id] === 'dislike' ? <DislikeFilled /> : <DislikeOutlined />}
                      onClick={() => handleDislike(message.id)}
                      className={`action-button ${messageFeedback[message.id] === 'dislike' ? 'active-dislike' : ''}`}
                    />
                  </Tooltip>
                  <Tooltip title="分享" placement="bottom">
                    <Button
                      type="text"
                      size="small"
                      icon={<ShareAltOutlined />}
                      className="action-button"
                    />
                  </Tooltip>
                  <Tooltip title="重新生成" placement="bottom">
                    <Button
                      type="text"
                      size="small"
                      icon={<ReloadOutlined />}
                      className="action-button"
                    />
                  </Tooltip>
                  <Tooltip title="更多" placement="bottom">
                    <Button
                      type="text"
                      size="small"
                      icon={<MoreOutlined />}
                      className="action-button"
                    />
                  </Tooltip>
                </div>
              )}
            </div>
          ) : (
            // 用户消息 - 灰色框，靠右，纯文本显示
            <div className="user-message-wrapper">
              <div className="user-message-container">
                <div className="user-message-box">
                  {message.content}
                </div>
                <div className="message-actions">
                  <Button
                    size="small"
                    icon={<CopyOutlined />}
                    onClick={() => handleCopy(message.content)}
                    className="copy-button"
                  >
                    复制
                  </Button>
                </div>
              </div>
            </div>
          )}
        </div>
      ))}
      <div ref={messagesEndRef} />
    </div>
  );
};

export default MessageList;