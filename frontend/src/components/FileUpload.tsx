import React, { useState } from 'react';
import { Upload, Button, Modal, Input, Space, Typography, App as AntdApp } from 'antd';
import { UploadOutlined, InboxOutlined } from '@ant-design/icons';
import type { UploadProps, UploadFile } from 'antd';
import { ApiService } from '../services/api';

const { Dragger } = Upload;
const { Text } = Typography;

interface FileUploadProps {
  onUploadSuccess?: (ragTag: string) => void;
}

/**
 * 文件上传组件
 * 支持拖拽上传文件到知识库
 */
const FileUpload: React.FC<FileUploadProps> = ({ onUploadSuccess }) => {
  const { message: messageApi } = AntdApp.useApp();
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [ragTag, setRagTag] = useState('');
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const [uploading, setUploading] = useState(false);

  const showModal = () => {
    setIsModalVisible(true);
  };

  const handleCancel = () => {
    setIsModalVisible(false);
    setRagTag('');
    setFileList([]);
  };

  const handleUpload = async () => {
    if (!ragTag.trim()) {
      messageApi.error('请输入知识库标签');
      return;
    }

    if (fileList.length === 0) {
      messageApi.error('请选择要上传的文件');
      return;
    }

    setUploading(true);
    try {
      const files = fileList.map(file => file.originFileObj as File).filter(Boolean);
      await ApiService.uploadFiles(ragTag.trim(), files);
      // 成功提示与后续动作交由父组件处理，避免重复弹窗
      handleCancel();
      onUploadSuccess?.(ragTag.trim());
    } catch (error) {
      messageApi.open({ type: 'error', content: `文件上传失败: ${error instanceof Error ? error.message : '未知错误'}` });
    } finally {
      setUploading(false);
    }
  };

  const uploadProps: UploadProps = {
    name: 'files',
    multiple: true,
    beforeUpload: (file) => {
      const uploadFile: UploadFile = {
        uid: `${file.name}-${file.size}-${Date.now()}`,
        name: file.name,
        status: 'done',
        originFileObj: file,
      };
      setFileList(prev => [...prev, uploadFile]);
      return false; // 阻止自动上传
    },
    onRemove: (file) => {
      setFileList(prev => prev.filter(f => f.uid !== file.uid));
    },
    fileList: fileList
  };

  return (
    <>
      <Button 
        type="primary" 
        icon={<UploadOutlined />} 
        onClick={showModal}
        style={{ backgroundColor: 'var(--ant-colorSuccess, #52c41a)', borderColor: 'var(--ant-colorSuccess, #52c41a)', color: 'var(--ant-colorText, #000000)' }}
      >
        上传文档
      </Button>

      <Modal
        title="上传文档到知识库"
        open={isModalVisible}
        onCancel={handleCancel}
        footer={[
          <Button key="cancel" onClick={handleCancel}>
            取消
          </Button>,
          <Button 
            key="upload" 
            type="primary" 
            loading={uploading}
            onClick={handleUpload}
          >
            上传
          </Button>
        ]}
        width={600}
      >
        <Space direction="vertical" style={{ width: '100%' }} size="large">
          <div>
            <Text strong>知识库标签:</Text>
            <Input
              value={ragTag}
              onChange={(e) => setRagTag(e.target.value)}
              placeholder="请输入知识库标签，如：技术文档、产品手册等"
              style={{ marginTop: 8 }}
            />
          </div>

          <div>
            <Text strong>选择文件:</Text>
            <Dragger {...uploadProps} style={{ marginTop: 8 }}>
              <p className="ant-upload-drag-icon">
                <InboxOutlined />
              </p>
              <p className="ant-upload-text">点击或拖拽文件到此区域上传</p>
              <p className="ant-upload-hint">
                支持单个或批量上传。支持 PDF, DOC, DOCX, TXT, MD 等格式
              </p>
            </Dragger>
          </div>
        </Space>
      </Modal>
    </>
  );
};

export default FileUpload;