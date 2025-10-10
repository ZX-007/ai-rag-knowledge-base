import React, { useMemo } from 'react';
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
 * 使用 React.memo 优化性能，避免不必要的重渲染
 */
const ModelSelector: React.FC<ModelSelectorProps> = React.memo(({
  models,
  selectedModel,
  onModelChange,
  loading = false
}) => {
  // 使用 useMemo 缓存选项列表，避免每次都重新计算
  const options = useMemo(() => {
    return models.map(model => ({
      label: model,
      value: model
    }));
  }, [models]);

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
        options={options}
      />
    </Space>
  );
});

ModelSelector.displayName = 'ModelSelector';

export default ModelSelector;