import React, { useState } from 'react';
import { Button, Modal, Input, Space, Typography, App as AntdApp } from 'antd';
import { GithubOutlined } from '@ant-design/icons';
import { ApiService } from '../services/api';
import type { GitRepositoryRequest } from '../types';

const { Text } = Typography;

interface GitRepoAnalyzeProps {
  /** 分析成功后的回调（传递推断出的 ragTag） */
  onAnalyzeSuccess?: (ragTag: string) => void;
}

/**
 * Git 仓库分析组件
 *
 * 以弹窗表单的形式收集 Git 仓库地址与可选认证信息，
 * 调用后端接口执行克隆与文档解析，并在成功后刷新知识库标签。
 */
const GitRepoAnalyze: React.FC<GitRepoAnalyzeProps> = ({ onAnalyzeSuccess }) => {
  const { message: messageApi } = AntdApp.useApp();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [repoUrl, setRepoUrl] = useState('');
  const [userName, setUserName] = useState('');
  const [token, setToken] = useState('');

  const show = () => setOpen(true);
  const close = () => {
    setOpen(false);
    setSubmitting(false);
    setRepoUrl('');
    setUserName('');
    setToken('');
  };

  /**
   * 根据仓库地址推断项目名，用于选择RAG标签
   */
  const inferProjectName = (url: string): string => {
    try {
      const parts = url.split('/');
      const last = parts[parts.length - 1] || '';
      return last.replace('.git', '');
    } catch {
      return '';
    }
  };

  /**
   * 提交分析请求
   */
  const handleAnalyze = async () => {
    if (!repoUrl.trim()) {
      messageApi.open({ type: 'error', content: '请填写仓库地址' });
      return;
    }

    const req: GitRepositoryRequest = {
      repoUrl: repoUrl.trim(),
      userName: userName.trim() || undefined,
      token: token.trim() || undefined,
    };

    setSubmitting(true);
    try {
      const result = await ApiService.analyzeGitRepository(req);
      messageApi.open({ type: 'success', content: result || 'Git 仓库分析完成' });
      const ragTag = inferProjectName(req.repoUrl);
      close();
      if (ragTag) {
        onAnalyzeSuccess?.(ragTag);
      } else {
        onAnalyzeSuccess?.('');
      }
    } catch (e) {
      messageApi.open({ type: 'error', content: `分析失败：${e instanceof Error ? e.message : '未知错误'}` });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <Button 
        icon={<GithubOutlined />} 
        onClick={show}
        style={{ backgroundColor: '#1677ff', color: '#fff' }}
      >
        分析 Git 仓库
      </Button>

      <Modal
        title="分析 Git 仓库并导入知识库"
        open={open}
        onCancel={close}
        footer={[
          <Button key="cancel" onClick={close}>取消</Button>,
          <Button key="submit" type="primary" loading={submitting} onClick={handleAnalyze}>开始分析</Button>
        ]}
        width={600}
      >
        <Space direction="vertical" style={{ width: '100%' }} size="large">
          <div>
            <Text strong>仓库地址：</Text>
            <Input 
              placeholder="例如：https://github.com/org/repo.git"
              value={repoUrl}
              onChange={(e) => setRepoUrl(e.target.value)}
              style={{ marginTop: 8 }}
            />
          </div>

          <div>
            <Text strong>用户名（可选）：</Text>
            <Input 
              placeholder="私有仓库时填写"
              value={userName}
              onChange={(e) => setUserName(e.target.value)}
              style={{ marginTop: 8 }}
            />
          </div>

          <div>
            <Text strong>令牌/密码（可选）：</Text>
            <Input.Password 
              placeholder="私有仓库时填写访问令牌或密码"
              value={token}
              onChange={(e) => setToken(e.target.value)}
              style={{ marginTop: 8 }}
            />
          </div>
        </Space>
      </Modal>
    </>
  );
};

export default GitRepoAnalyze;