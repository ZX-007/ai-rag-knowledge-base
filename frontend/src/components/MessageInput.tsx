import React, { useState, KeyboardEvent } from 'react';
import { Input } from 'antd';
import { UpOutlined, StopOutlined, PlusOutlined } from '@ant-design/icons';

const { TextArea } = Input;

interface MessageInputProps {
  onSendMessage: (message: string) => void;
  onStopStreaming?: () => void;
  disabled?: boolean;
  isStreaming?: boolean;
  placeholder?: string;
}

/**
 * 消息输入组件
 * 提供消息输入和发送功能
 */
const MessageInput: React.FC<MessageInputProps> = ({ 
  onSendMessage, 
  onStopStreaming,
  disabled = false,
  isStreaming = false,
  placeholder = "在此处提问"
}) => {
  const [message, setMessage] = useState('');

  const handleSend = () => {
    const trimmedMessage = message.trim();
    if (trimmedMessage && !disabled && !isStreaming) {
      onSendMessage(trimmedMessage);
      setMessage('');
    }
  };

  const handleStop = () => {
    if (onStopStreaming) {
      onStopStreaming();
    }
  };

  const handleKeyPress = (e: KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      if (!isStreaming) {
        handleSend();
      }
    }
  };

  return (
    <div className="chat-input-area">
      <div className="unified-input-container">
        {/* 左侧加号区域 */}
        <div className="left-circle-button" onClick={() => {}}>
          <PlusOutlined style={{ fontSize: '20px' }} />
        </div>
        
        {/* 中间输入框 */}
        <TextArea
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder={placeholder}
          disabled={disabled || isStreaming}
          autoSize={{ minRows: 1, maxRows: 4 }}
          className="unified-textarea"
          bordered={false}
        />
        
        {/* 右侧发送/终止按钮区域 */}
        <div 
          className={`right-circle-button ${isStreaming ? 'streaming' : ''} ${(!message.trim() || disabled) && !isStreaming ? 'disabled' : ''}`}
          onClick={isStreaming ? handleStop : handleSend}
        >
          {isStreaming ? (
            <StopOutlined style={{ fontSize: '18px' }} />
          ) : (
            <UpOutlined style={{ fontSize: '18px' }} />
          )}
        </div>
      </div>
      
      <div style={{ 
        fontSize: '11px', 
        color: 'var(--ant-colorTextTertiary)', 
        marginTop: '8px',
        textAlign: 'center',
        opacity: 0.85
      }}>
        {isStreaming ? (
          <span>模型正在生成回复...</span>
        ) : (
          <span>AI 可能会生成不准确的信息，请注意核实</span>
        )}
      </div>
    </div>
  );
};

export default MessageInput;