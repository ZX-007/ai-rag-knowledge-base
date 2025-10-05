import React from 'react';
import { Select, Space, Typography, Switch } from 'antd';
import { DatabaseOutlined } from '@ant-design/icons';

const { Text } = Typography;

interface RagTagSelectorProps {
  ragTags: string[];
  selectedRagTag: string | null;
  onRagTagChange: (ragTag: string | null) => void;
  loading?: boolean;
}

/**
 * RAG标签选择器组件
 * 用于选择知识库标签，启用RAG功能
 */
const RagTagSelector: React.FC<RagTagSelectorProps> = ({
  ragTags,
  selectedRagTag,
  onRagTagChange,
  loading = false
}) => {
  const isRagEnabled = selectedRagTag !== null;

  const handleRagToggle = (enabled: boolean) => {
    if (enabled) {
      // 启用RAG，选择第一个可用标签
      if (ragTags.length > 0) {
        onRagTagChange(ragTags[0]);
      }
    } else {
      // 禁用RAG
      onRagTagChange(null);
    }
  };

  return (
    <Space align="center">
      <DatabaseOutlined style={{ color: 'var(--ant-colorSuccess, #52c41a)' }} />
      <Text strong>知识库:</Text>
      <Switch
        checked={isRagEnabled}
        onChange={handleRagToggle}
        checkedChildren="启用"
        unCheckedChildren="禁用"
      />
      {isRagEnabled && (
        <Select
          value={selectedRagTag}
          onChange={onRagTagChange}
          loading={loading}
          style={{ minWidth: 150 }}
          placeholder="选择知识库标签"
          options={ragTags.map(tag => ({
            label: tag,
            value: tag
          }))}
        />
      )}
    </Space>
  );
};

export default RagTagSelector;