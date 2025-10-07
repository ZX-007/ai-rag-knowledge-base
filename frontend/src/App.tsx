import React, { useState, useEffect, useCallback } from 'react';
import { Space, Divider, ConfigProvider, theme as antdTheme, App as AntdApp } from 'antd';
import { v4 as uuidv4 } from 'uuid';
import { Message, AppState } from './types';
import { ApiService } from './services/api';
import MessageList from './components/MessageList';
import MessageInput from './components/MessageInput';
import ModelSelector from './components/ModelSelector';
import RagTagSelector from './components/RagTagSelector';
import FileUpload from './components/FileUpload';
import GitRepoAnalyze from './components/GitRepoAnalyze';
import ThemeToggle from './components/ThemeToggle';
import type { ThemeMode } from './components/ThemeToggle';

/**
 * 主应用组件
 * 整合聊天界面、模型选择、RAG功能等
 */
const App: React.FC = () => {
  const [state, setState] = useState<AppState>({
    models: [],
    ragTags: [],
    selectedModel: '',
    selectedRagTag: null,
    messages: [],
    isStreaming: false,
    currentStreamingMessageId: null
  });

  const [loading, setLoading] = useState({
    models: false,
    ragTags: false
  });

  /** 当前主题模式（默认从 localStorage 读取） */
  const [theme, setTheme] = useState<ThemeMode>(() => {
    const saved = localStorage.getItem('theme');
    return (saved === 'dark' || saved === 'light') ? (saved as ThemeMode) : 'light';
  });

  /** 应用主题到 body 的 data-theme 属性 */
  useEffect(() => {
    document.body.dataset.theme = theme;
    localStorage.setItem('theme', theme);
  }, [theme]);

  // 使用 App 上下文中的 message 实例（由 main.tsx 顶层提供）
  const { message: messageApi } = AntdApp.useApp();

  // 加载可用模型
  const loadModels = useCallback(async () => {
    setLoading(prev => ({ ...prev, models: true }));
    try {
      const models = await ApiService.getAvailableModels();
      setState(prev => ({
        ...prev,
        models,
        selectedModel: models.length > 0 ? models[0] : ''
      }));
    } catch (error) {
      messageApi.open({ type: 'error', content: `加载模型列表失败: ${error instanceof Error ? error.message : '未知错误'}` });
    } finally {
      setLoading(prev => ({ ...prev, models: false }));
    }
  }, []);

  // 加载RAG标签
  const loadRagTags = useCallback(async () => {
    setLoading(prev => ({ ...prev, ragTags: true }));
    try {
      const ragTags = await ApiService.getRagTags();
      setState(prev => ({ ...prev, ragTags }));
    } catch (error) {
      messageApi.open({ type: 'error', content: `加载RAG标签失败: ${error instanceof Error ? error.message : '未知错误'}` });
    } finally {
      setLoading(prev => ({ ...prev, ragTags: false }));
    }
  }, []);

  // 初始化加载数据
  useEffect(() => {
    loadModels();
    loadRagTags();
  }, [loadModels, loadRagTags]);

  // 发送消息
  const handleSendMessage = useCallback(async (messageContent: string) => {
    if (!state.selectedModel) {
      messageApi.open({ type: 'error', content: '请先选择AI模型' });
      return;
    }

    // 添加用户消息
    const userMessage: Message = {
      id: uuidv4(),
      type: 'user',
      content: messageContent,
      timestamp: Date.now()
    };

    // 创建AI回复消息
    const assistantMessage: Message = {
      id: uuidv4(),
      type: 'assistant',
      content: '',
      thinkingContent: '',
      timestamp: Date.now(),
      streaming: true
    };

    setState(prev => ({
      ...prev,
      messages: [...prev.messages, userMessage, assistantMessage],
      isStreaming: true,
      currentStreamingMessageId: assistantMessage.id
    }));

    try {
      // 选择流式聊天方法
      const streamGenerator = state.selectedRagTag
        ? ApiService.streamRagChat({
            model: state.selectedModel,
            message: messageContent,
            ragTag: state.selectedRagTag
          })
        : ApiService.streamChat({
            model: state.selectedModel,
            message: messageContent
          });

      let fullContent = '';
      let fullThinkingContent = '';
      
      for await (const chunk of streamGenerator) {
        if (chunk.thinking) {
          // 更新思考内容
          fullThinkingContent += chunk.thinking;
          setState(prev => ({
            ...prev,
            messages: prev.messages.map(msg =>
              msg.id === assistantMessage.id
                ? { 
                    ...msg, 
                    thinkingContent: fullThinkingContent,
                    // 记录思考开始时间，仅在首次出现时设置
                    thinkingStartAt: msg.thinkingStartAt ?? Date.now()
                  }
                : msg
            )
          }));
        } else if (chunk.content) {
          // 更新正文内容
          fullContent += chunk.content;
          setState(prev => ({
            ...prev,
            messages: prev.messages.map(msg =>
              msg.id === assistantMessage.id
                ? { ...msg, content: fullContent }
                : msg
            )
          }));
        }
      }

      // 流式完成
      setState(prev => ({
        ...prev,
        isStreaming: false,
        currentStreamingMessageId: null,
        messages: prev.messages.map(msg =>
          msg.id === assistantMessage.id
            ? { 
                ...msg, 
                streaming: false,
                // 记录思考结束时间（若有思考内容）
                thinkingEndAt: msg.thinkingContent ? Date.now() : msg.thinkingEndAt
              }
            : msg
        )
      }));

    } catch (error) {
      messageApi.open({ type: 'error', content: `发送消息失败: ${error instanceof Error ? error.message : '未知错误'}` });
      
      // 移除失败的AI消息
      setState(prev => ({
        ...prev,
        messages: prev.messages.filter(msg => msg.id !== assistantMessage.id),
        isStreaming: false,
        currentStreamingMessageId: null
      }));
    }
  }, [state.selectedModel, state.selectedRagTag, messageApi]);

  // 模型选择变化
  const handleModelChange = useCallback((model: string) => {
    setState(prev => ({ ...prev, selectedModel: model }));
  }, []);

  // RAG标签选择变化
  const handleRagTagChange = useCallback((ragTag: string | null) => {
    setState(prev => ({ ...prev, selectedRagTag: ragTag }));
  }, []);

  // 文件上传成功回调
  const handleUploadSuccess = useCallback((ragTag: string) => {
    // 重新加载RAG标签列表
    loadRagTags();
    // 自动选择新上传的标签
    setState(prev => ({ ...prev, selectedRagTag: ragTag }));
    messageApi.open({ type: 'success', content: '文件上传成功，已自动选择该知识库标签' });
  }, [loadRagTags, messageApi]);

  // Git 仓库分析成功后回调
  const handleAnalyzeSuccess = useCallback((ragTag: string) => {
    // 刷新标签列表并自动选择推断出的标签
    loadRagTags();
    if (ragTag) {
      setState(prev => ({ ...prev, selectedRagTag: ragTag }));
      messageApi.open({ type: 'success', content: `Git 仓库分析完成，已自动选择知识库：${ragTag}` });
    } else {
      messageApi.open({ type: 'success', content: 'Git 仓库分析完成' });
    }
  }, [loadRagTags, messageApi]);

  return (
    <ConfigProvider theme={{ cssVar: true, hashed: false, algorithm: theme === 'dark' ? antdTheme.darkAlgorithm : antdTheme.defaultAlgorithm }}>
      <div className="app-container">
        {/* 固定顶部栏（移除页面标题，仅保留控制区）*/}
        <div className="fixed-header">
          <div className="chat-controls">
            <Space split={<Divider type="vertical" />} wrap>
              <ModelSelector
                models={state.models}
                selectedModel={state.selectedModel}
                onModelChange={handleModelChange}
                loading={loading.models}
              />
              <RagTagSelector
                ragTags={state.ragTags}
                selectedRagTag={state.selectedRagTag}
                onRagTagChange={handleRagTagChange}
                loading={loading.ragTags}
              />
              <FileUpload onUploadSuccess={handleUploadSuccess} />
              <GitRepoAnalyze onAnalyzeSuccess={handleAnalyzeSuccess} />
              <ThemeToggle mode={theme} onToggle={setTheme} />
            </Space>
          </div>
        </div>

        {/* 对话内容区域 */}
        <div className="chat-content-wrapper">
          <div className="chat-content-container">
            {state.messages.length === 0 ? (
              <div className="empty-state">
                <div className="empty-state-content">
                  <div className="empty-state-icon">💬</div>
                  <div className="empty-state-title">欢迎使用AI RAG知识库对话系统</div>
                  <div className="empty-state-subtitle">
                    {state.selectedRagTag ? 
                      `当前使用知识库: ${state.selectedRagTag}` : 
                      '请输入您的问题开始对话'
                    }
                  </div>
                </div>
              </div>
            ) : (
              <MessageList
                messages={state.messages}
                isStreaming={state.isStreaming}
                currentStreamingMessageId={state.currentStreamingMessageId}
              />
            )}
          </div>

          {/* 固定底部输入框 */}
          <div className="fixed-input">
            <div className="input-container">
              <MessageInput
                onSendMessage={handleSendMessage}
                disabled={state.isStreaming || !state.selectedModel}
                placeholder={
                  !state.selectedModel 
                    ? "请先选择AI模型..." 
                    : state.selectedRagTag 
                      ? `向知识库 "${state.selectedRagTag}" 提问...`
                      : "请输入您的问题..."
                }
              />
            </div>
          </div>
        </div>
      </div>
    </ConfigProvider>
  );
};

export default App;