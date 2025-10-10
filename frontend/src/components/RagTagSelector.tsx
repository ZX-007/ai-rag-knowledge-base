import React, { useMemo, useCallback } from 'react';
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
 * 使用 React.memo 优化性能，避免不必要的重渲染
 */
const RagTagSelector: React.FC<RagTagSelectorProps> = React.memo(({
  ragTags,
  selectedRagTag,
  onRagTagChange,
  loading = false
}) => {
  // 使用 useMemo 缓存选项列表
  const options = useMemo(() => {
    return ragTags.map(tag => ({
      label: tag,
      value: tag
    }));
  }, [ragTags]);

  // 使用 useCallback 缓存清空回调
  const handleClear = useCallback(() => {
    onRagTagChange(null);
  }, [onRagTagChange]);

  return (
    <Space align="center">
      <DatabaseOutlined style={{ color: 'var(--ant-colorSuccess, #52c41a)' }} />
      <Text strong>知识库:</Text>
      <Select
        value={selectedRagTag ?? undefined}
        onChange={onRagTagChange}
        onClear={handleClear}
        allowClear
        loading={loading}
        style={{ width: 200 }}
        placeholder="未启用（可选）"
        options={options}
      />
    </Space>
  );
});

RagTagSelector.displayName = 'RagTagSelector';

export default RagTagSelector;