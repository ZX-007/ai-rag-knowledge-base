import React, { useState, KeyboardEvent } from 'react';
import { Input, Button, Space } from 'antd';
import { SendOutlined } from '@ant-design/icons';

const { TextArea } = Input;

interface MessageInputProps {
  onSendMessage: (message: string) => void;
  disabled?: boolean;
  placeholder?: string;
}

/**
 * 消息输入组件
 * 提供消息输入和发送功能
 */
const MessageInput: React.FC<MessageInputProps> = ({ 
  onSendMessage, 
  disabled = false,
  placeholder = "请输入您的问题..."
}) => {
  const [message, setMessage] = useState('');

  const handleSend = () => {
    const trimmedMessage = message.trim();
    if (trimmedMessage && !disabled) {
      onSendMessage(trimmedMessage);
      setMessage('');
    }
  };

  const handleKeyPress = (e: KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="chat-input-area">
      <Space.Compact style={{ width: '100%' }}>
        <TextArea
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder={placeholder}
          disabled={disabled}
          autoSize={{ minRows: 1, maxRows: 4 }}
          style={{ resize: 'none' }}
        />
        <Button
          type="primary"
          icon={<SendOutlined />}
          onClick={handleSend}
          disabled={disabled || !message.trim()}
          style={{ height: 'auto', minHeight: '32px' }}
        >
          发送
        </Button>
      </Space.Compact>
      <div style={{ 
        fontSize: '12px', 
        color: 'var(--panel-text)', 
        marginTop: '8px',
        textAlign: 'center'
      }}>
        按 Enter 发送，Shift + Enter 换行
      </div>
    </div>
  );
};

export default MessageInput;