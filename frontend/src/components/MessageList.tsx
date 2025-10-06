import React, { useEffect, useRef } from 'react';
import { Collapse, Space, Typography } from 'antd';
import { ThunderboltOutlined } from '@ant-design/icons';
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

  // 自动滚动到底部
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

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
            </div>
          ) : (
            // 用户消息 - 灰色框，靠右，纯文本显示
            <div className="user-message-wrapper">
              <div className="user-message-box">
                {message.content}
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