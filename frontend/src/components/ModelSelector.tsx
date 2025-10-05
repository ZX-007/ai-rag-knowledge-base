import React from 'react';
import { Select, Space, Typography } from 'antd';
import { RobotOutlined } from '@ant-design/icons';

const { Text } = Typography;

interface ModelSelectorProps {
  models: string[];
  selectedModel: string;
  onModelChange: (model: string) => void;
  loading?: boolean;
}

/**
 * 模型选择器组件
 * 用于选择AI模型
 */
const ModelSelector: React.FC<ModelSelectorProps> = ({
  models,
  selectedModel,
  onModelChange,
  loading = false
}) => {
  return (
    <Space align="center">
      <RobotOutlined style={{ color: 'var(--ant-colorPrimary)' }} />
      <Text strong>AI模型:</Text>
      <Select
        value={selectedModel}
        onChange={onModelChange}
        loading={loading}
        style={{ minWidth: 200 }}
        placeholder="请选择AI模型"
        options={models.map(model => ({
          label: model,
          value: model
        }))}
      />
    </Space>
  );
};

export default ModelSelector;