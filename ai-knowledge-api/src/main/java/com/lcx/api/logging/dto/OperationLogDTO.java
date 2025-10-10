package com.lcx.api.logging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 操作日志DTO
 * 
 * <p>记录用户操作行为的详细日志，用于审计、回溯和安全监控。</p>
 * 
 * @author lcx
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogDTO implements Serializable {

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

    /** 操作类型 */
    private String operationType;

    /** 操作模块 */
    private String module;

    /** 操作方法 */
    private String method;

    /** 方法描述 */
    private String methodDescription;

    /** 请求参数（脱敏后） */
    private String requestParams;

    /** 响应结果 */
    private String responseResult;

    /** 操作时间（毫秒时间戳） */
    private Long operationTime;

    /** 操作耗时（毫秒） */
    private Long duration;

    /** 是否成功 */
    private Boolean success;

    /** 错误信息（如果有） */
    private String errorMessage;

    /** 备注 */
    private String remark;
}

