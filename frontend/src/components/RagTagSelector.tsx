import React from 'react';
import { Select, Space, Typography } from 'antd';
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
  return (
    <Space align="center">
      <DatabaseOutlined style={{ color: 'var(--ant-colorSuccess, #52c41a)' }} />
      <Text strong>知识库:</Text>
      <Select
        value={selectedRagTag ?? undefined}
        onChange={(value) => onRagTagChange(value)}
        onClear={() => onRagTagChange(null)}
        allowClear
        loading={loading}
        style={{ width: 200 }}
        placeholder="未启用（可选）"
        options={ragTags.map(tag => ({
          label: tag,
          value: tag
        }))}
      />
    </Space>
  );
};

export default RagTagSelector;