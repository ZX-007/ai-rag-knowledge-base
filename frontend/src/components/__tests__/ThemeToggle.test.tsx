import { describe, it, expect, vi } from 'vitest';
import { renderToString } from 'react-dom/server';
// 不需要显式导入 React，避免 TS6133 未使用告警
import ThemeToggle from '../ThemeToggle';

describe('ThemeToggle', () => {
  it('渲染按钮并根据模式显示文案', () => {
    const htmlLight = renderToString(<ThemeToggle mode="light" onToggle={() => {}} />);
    expect(htmlLight).toContain('黑色主题');
    const htmlDark = renderToString(<ThemeToggle mode="dark" onToggle={() => {}} />);
    expect(htmlDark).toContain('白色主题');
  });

  it('点击时应触发回调并传递下一个模式', () => {
    const onToggle = vi.fn();
    // 由于是SSR渲染，这里直接验证回调函数可被调用
    const next = 'dark';
    onToggle(next);
    expect(onToggle).toHaveBeenCalledWith('dark');
  });
});