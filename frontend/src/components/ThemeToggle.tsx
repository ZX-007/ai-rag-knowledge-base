import React from 'react';
import { Button } from 'antd';
import { BulbOutlined, MoonOutlined } from '@ant-design/icons';

export type ThemeMode = 'light' | 'dark';

interface ThemeToggleProps {
  /** 当前主题模式 */
  mode: ThemeMode;
  /** 点击切换回调 */
  onToggle: (next: ThemeMode) => void;
}

/**
 * 主题切换按钮组件
 *
 * 提供一个按钮用于在浅色(light)与深色(dark)主题之间切换。
 * - 使用 Ant Design 按钮与图标展示当前模式
 * - 点击后通过 onToggle 回调通知上层更新主题
 *
 * @param mode 当前主题模式（light/dark）
 * @param onToggle 切换主题时的回调函数，参数为切换后的主题
 * @returns 渲染后的 React 节点
 */
const ThemeToggle: React.FC<ThemeToggleProps> = ({ mode, onToggle }) => {
  const isDark = mode === 'dark';

  const handleClick = () => {
    const next = isDark ? 'light' : 'dark';
    onToggle(next);
  };

  return (
    <Button
      type={isDark ? 'default' : 'primary'}
      icon={isDark ? <BulbOutlined /> : <MoonOutlined />}
      onClick={handleClick}
    >
      {isDark ? '白色主题' : '黑色主题'}
    </Button>
  );
};

export default ThemeToggle;