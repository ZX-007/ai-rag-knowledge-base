import React, { useEffect, useRef, useState } from 'react';
import TimeUtil from '../utils/time';

interface ThinkingTimerProps {
  /** 思考开始时间戳（毫秒） */
  startAt?: number;
  /** 思考结束时间戳（毫秒） */
  endAt?: number;
  /** 是否处于思考流式运行中 */
  running?: boolean;
  /**
   * 标签前缀文本，默认显示“思考时间”；
   * 可传入“用时”以匹配已完成的样式文案
   */
  label?: string;
}

/**
 * 思考时长计时组件
 * 在流式过程中每隔500ms刷新显示；结束后展示最终耗时
 *
 * @param startAt - 思考开始时间戳（毫秒）
 * @param endAt - 思考结束时间戳（毫秒）
 * @param running - 是否处于思考运行中
 * @returns React元素，展示“思考时间 mm:ss / hh:mm:ss”
 */
const ThinkingTimer: React.FC<ThinkingTimerProps> = ({ startAt, endAt, running = false, label = '思考时间' }) => {
  const [now, setNow] = useState<number>(Date.now());
  const timerRef = useRef<number | null>(null);

  useEffect(() => {
    if (running && startAt) {
      // 使用 window.setInterval，确保在 React + Vite 环境下类型正确
      timerRef.current = window.setInterval(() => setNow(Date.now()), 500);
    }
    return () => {
      if (timerRef.current) {
        clearInterval(timerRef.current);
        timerRef.current = null;
      }
    };
  }, [running, startAt]);

  if (!startAt) {
    return <span>{label} —</span>;
  }

  const ms = endAt && !running ? Math.max(0, endAt - startAt) : Math.max(0, now - startAt);
  const formatted = TimeUtil.formatDuration(ms);
  return <span>{label} {formatted}</span>;
};

export default ThinkingTimer;