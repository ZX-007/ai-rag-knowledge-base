import { describe, it, expect } from 'vitest';
import TimeUtil from '../../utils/time';

/**
 * TimeUtil.formatDuration 的单元测试
 * 覆盖无效输入、分钟秒、小时级格式化等场景
 */
describe('TimeUtil.formatDuration', () => {
  it('should handle invalid or negative input', () => {
    expect(TimeUtil.formatDuration(NaN)).toBe('00:00');
    expect(TimeUtil.formatDuration(-100)).toBe('00:00');
  });

  it('should format under one hour as mm:ss', () => {
    expect(TimeUtil.formatDuration(0)).toBe('00:00');
    expect(TimeUtil.formatDuration(59_000)).toBe('00:59');
    expect(TimeUtil.formatDuration(65_000)).toBe('01:05');
  });

  it('should format one hour and above as hh:mm:ss', () => {
    expect(TimeUtil.formatDuration(3_600_000)).toBe('01:00:00');
    expect(TimeUtil.formatDuration(3_900_000)).toBe('01:05:00');
    expect(TimeUtil.formatDuration(3_906_500)).toBe('01:05:06');
  });
});