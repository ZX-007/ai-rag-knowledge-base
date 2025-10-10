package com.lcx.api.logging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 性能日志DTO
 * 
 * <p>记录系统性能相关的日志信息，用于性能监控、优化和容量规划。</p>
 * 
 * @author lcx
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 追踪ID */
    private String traceId;

    /** 服务ID */
    private String serviceId;

    /** 监控点名称 */
    private String checkpointName;

    /** 模块 */
    private String module;

    /** 方法签名 */
    private String methodSignature;

    /** 开始时间（毫秒时间戳） */
    private Long startTime;

    /** 结束时间（毫秒时间戳） */
    private Long endTime;

    /** 耗时（毫秒） */
    private Long duration;

    /** 是否超时 */
    private Boolean timeout;

    /** 超时阈值（毫秒） */
    private Long timeoutThreshold;

    /** CPU使用率（%） */
    private Double cpuUsage;

    /** 内存使用量（MB） */
    private Double memoryUsage;

    /** 线程数 */
    private Integer threadCount;

    /** 是否成功 */
    private Boolean success;

    /** 错误信息（如果有） */
    private String errorMessage;

    /** 备注 */
    private String remark;
}

