import React, { Component, ReactNode } from 'react';
import { Result, Button } from 'antd';

interface ErrorBoundaryProps {
  children: ReactNode;
  /**
   * 自定义错误处理回调
   */
  onError?: (error: Error, errorInfo: React.ErrorInfo) => void;
  /**
   * 是否在错误时显示详情（仅开发环境建议开启）
   */
  showDetails?: boolean;
}

interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
  errorInfo: React.ErrorInfo | null;
}

/**
 * 错误边界组件
 * 
 * 捕获子组件树中的JavaScript错误，记录错误并显示备用UI
 * 防止整个应用崩溃，提升用户体验
 */
class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
    };
  }

  static getDerivedStateFromError(error: Error): Partial<ErrorBoundaryState> {
    // 更新 state 使下一次渲染能够显示降级后的 UI
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo): void {
    // 记录错误信息
    console.error('ErrorBoundary caught an error:', error, errorInfo);
    
    this.setState({
      error,
      errorInfo,
    });

    // 调用自定义错误处理回调
    if (this.props.onError) {
      this.props.onError(error, errorInfo);
    }
  }

  handleReset = (): void => {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null,
    });
  };

  handleReload = (): void => {
    window.location.reload();
  };

  render(): ReactNode {
    if (this.state.hasError) {
      const { showDetails = import.meta.env.DEV } = this.props;
      const { error, errorInfo } = this.state;

      return (
        <div style={{ 
          height: '100vh', 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'center',
          padding: '20px'
        }}>
          <Result
            status="error"
            title="应用出现错误"
            subTitle="抱歉，应用遇到了一个错误。您可以尝试重新加载页面或重置状态。"
            extra={[
              <Button type="primary" key="reload" onClick={this.handleReload}>
                重新加载页面
              </Button>,
              <Button key="reset" onClick={this.handleReset}>
                尝试恢复
              </Button>,
            ]}
          >
            {showDetails && error && (
              <div style={{ 
                textAlign: 'left', 
                maxWidth: '600px', 
                margin: '20px auto',
                padding: '16px',
                background: '#f5f5f5',
                borderRadius: '4px',
                fontSize: '12px',
                fontFamily: 'monospace',
                overflow: 'auto',
                maxHeight: '300px'
              }}>
                <div style={{ marginBottom: '12px', fontWeight: 'bold', color: '#cf1322' }}>
                  错误信息：
                </div>
                <div style={{ marginBottom: '12px', color: '#000' }}>
                  {error.toString()}
                </div>
                {errorInfo && (
                  <>
                    <div style={{ marginBottom: '8px', fontWeight: 'bold', color: '#cf1322' }}>
                      组件堆栈：
                    </div>
                    <div style={{ color: '#595959', whiteSpace: 'pre-wrap' }}>
                      {errorInfo.componentStack}
                    </div>
                  </>
                )}
              </div>
            )}
          </Result>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;

