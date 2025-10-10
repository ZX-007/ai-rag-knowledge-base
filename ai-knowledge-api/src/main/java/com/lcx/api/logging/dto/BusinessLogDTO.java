package com.lcx.api.logging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 业务日志DTO
 * 
 * <p>记录业务操作的详细日志信息，用于业务跟踪、问题排查和数据分析。</p>
 * 
 * @author lcx
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 追踪ID */
    private String traceId;

    /** 服务ID */
    private String serviceId;

    /** 用户ID */
    private String userId;

    /** 用户名 */
    private String username;

    /** 业务模块 */
    private String module;

    /** 业务操作 */
    private String operation;

    /** 操作描述 */
    private String description;

    /** 操作开始时间（毫秒时间戳） */
    private Long startTime;

    /** 操作结束时间（毫秒时间戳） */
    private Long endTime;

    /** 操作耗时（毫秒） */
    private Long duration;

    /** 是否成功 */
    private Boolean success;

    /** 业务数据（JSON格式或其他） */
    private String businessData;

    /** 扩展属性 */
    private Map<String, Object> extendedData;

    /** 错误信息（如果有） */
    private String errorMessage;

    /** 备注 */
    private String remark;
}

