import React from 'react';
import { Spin } from 'antd';
import { LoadingOutlined } from '@ant-design/icons';

interface LoadingProps {
  /**
   * 加载提示文字
   */
  tip?: string;
  /**
   * 尺寸大小
   */
  size?: 'small' | 'default' | 'large';
  /**
   * 是否全屏展示
   */
  fullScreen?: boolean;
  /**
   * 自定义样式
   */
  style?: React.CSSProperties;
}

/**
 * 加载状态组件
 * 
 * 用于展示加载中的状态，支持全屏和局部加载
 */
const Loading: React.FC<LoadingProps> = ({ 
  tip = '加载中...', 
  size = 'default',
  fullScreen = false,
  style 
}) => {
  const antIcon = <LoadingOutlined style={{ fontSize: size === 'large' ? 48 : size === 'small' ? 16 : 24 }} spin />;

  if (fullScreen) {
    return (
      <div
        style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: 'var(--ant-colorBgLayout, rgba(255, 255, 255, 0.9))',
          zIndex: 9999,
          ...style,
        }}
      >
        <Spin indicator={antIcon} size={size} tip={tip} />
      </div>
    );
  }

  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '40px 20px',
        ...style,
      }}
    >
      <Spin indicator={antIcon} size={size} tip={tip} />
    </div>
  );
};

export default Loading;

