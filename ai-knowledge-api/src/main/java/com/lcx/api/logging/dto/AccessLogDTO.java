package com.lcx.api.logging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 访问日志DTO
 * 
 * <p>记录HTTP请求的访问日志信息，用于访问统计、性能分析和安全审计。</p>
 * 
 * @author lcx
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 追踪ID */
    private String traceId;

    /** 服务ID */
    private String serviceId;

    /** 用户ID */
    private String userId;

    /** 用户名 */
    private String username;

    /** 客户端IP */
    private String clientIp;

    /** HTTP方法 */
    private String httpMethod;

    /** 请求URI */
    private String requestUri;

    /** 查询字符串 */
    private String queryString;

    /** User-Agent */
    private String userAgent;

    /** 请求开始时间（毫秒时间戳） */
    private Long startTime;

    /** 请求结束时间（毫秒时间戳） */
    private Long endTime;

    /** 请求耗时（毫秒） */
    private Long duration;

    /** HTTP状态码 */
    private Integer statusCode;

    /** 请求体大小（字节） */
    private Long requestSize;

    /** 响应体大小（字节） */
    private Long responseSize;

    /** 是否成功 */
    private Boolean success;

    /** 错误信息（如果有） */
    private String errorMessage;

    /** 备注 */
    private String remark;
}

