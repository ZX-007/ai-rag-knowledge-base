import React, { useEffect, useRef } from 'react';
import { Avatar, Collapse } from 'antd';
import { UserOutlined, RobotOutlined, ThunderboltOutlined } from '@ant-design/icons';
import { Message } from '../types';
import MarkdownText from './MarkdownText';
import ThinkingTimer from './ThinkingTimer';

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
          <div style={{ display: 'flex', alignItems: 'flex-start', gap: '12px', 
                      flexDirection: message.type === 'user' ? 'row-reverse' : 'row' }}>
            <Avatar 
              icon={message.type === 'user' ? <UserOutlined /> : <RobotOutlined />}
              style={{ 
                backgroundColor: message.type === 'user' ? 'var(--chat-avatar-user)' : 'var(--chat-avatar-assistant)',
                flexShrink: 0
              }}
            />
            <div 
              className={`message-bubble ${message.type}`}
              style={{
                textAlign: 'left',
                maxWidth: '70%'
              }}
            >
              {message.type === 'assistant' ? (
                <div>
                  {/* 思考过程展示 */}
                  {message.thinkingContent && (
                    <Collapse 
                      size="small" 
                      style={{ marginBottom: '12px' }}
                      expandIconPosition="start"
                      ghost
                      items={[
                        {
                          key: '1',
                          label: (
                            <span style={{ color: 'var(--panel-text)', fontSize: '13px', display: 'inline-flex', alignItems: 'center' }}>
                              <ThunderboltOutlined style={{ marginRight: 6 }} />
                              {message.id === currentStreamingMessageId && isStreaming ? '深度思考中' : '已深度思考'}（
                              <ThinkingTimer 
                                startAt={message.thinkingStartAt}
                                endAt={message.thinkingEndAt}
                                running={message.id === currentStreamingMessageId && isStreaming}
                                label={message.id === currentStreamingMessageId && isStreaming ? '思考时间' : '用时'}
                              />
                              ）
                            </span>
                          ),
                          children: (
                            <div style={{ 
                              background: 'var(--panel-bg)', 
                              padding: '12px', 
                              borderRadius: '6px',
                              fontSize: '13px',
                              color: 'var(--panel-text)',
                              lineHeight: '1.5',
                              whiteSpace: 'pre-wrap'
                            }}>
                              <MarkdownText 
                                content={message.thinkingContent}
                                isStreaming={message.id === currentStreamingMessageId && isStreaming}
                              />
                            </div>
                          )
                        }
                      ]}
                    />
                  )}
                  
                  {/* 正文内容 */}
                  <div>
                    <MarkdownText 
                      content={message.content}
                      isStreaming={message.id === currentStreamingMessageId && isStreaming}
                    />
                  </div>
                </div>
              ) : (
                // 用户消息
                <MarkdownText content={message.content} />
              )}
            </div>
          </div>
          <div style={{ 
            fontSize: '12px', 
            color: 'var(--panel-text)', 
            marginTop: '4px',
            textAlign: message.type === 'user' ? 'right' : 'left'
          }}>
            {new Date(message.timestamp).toLocaleTimeString()}
          </div>
        </div>
      ))}
      <div ref={messagesEndRef} />
    </div>
  );
};

export default MessageList;