/**
 * 时间与时长相关工具方法
 * 提供将毫秒时长格式化为 mm:ss 或 hh:mm:ss 字符串的能力
 */
export class TimeUtil {
  /**
   * 将毫秒数格式化为 mm:ss 或 hh:mm:ss
   * 
   * @param ms - 时长的毫秒数
   * @returns 格式化后的字符串；当小时为0时输出 mm:ss，否则输出 hh:mm:ss
   */
  static formatDuration(ms: number): string {
    if (!Number.isFinite(ms) || ms < 0) return '00:00';
    const totalSeconds = Math.floor(ms / 1000);
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    const mm = String(minutes).padStart(2, '0');
    const ss = String(seconds).padStart(2, '0');
    if (hours > 0) {
      const hh = String(hours).padStart(2, '0');
      return `${hh}:${mm}:${ss}`;
    }
    return `${mm}:${ss}`;
  }
}

export default TimeUtil;