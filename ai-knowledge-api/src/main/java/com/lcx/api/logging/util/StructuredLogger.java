package com.lcx.api.logging.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lcx.api.logging.LogConstants;
import com.lcx.api.logging.context.LogContext;
import com.lcx.api.logging.dto.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 结构化日志工具类
 * 
 * <p>提供结构化日志记录功能，支持JSON格式输出，便于日志收集、分析和监控。</p>
 * <p>结合MDC上下文，自动包含追踪ID、用户信息等关键信息。</p>
 * 
 * @author lcx
 * @version 1.0
 */
@Slf4j
public final class StructuredLogger {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private StructuredLogger() {
        throw new UnsupportedOperationException("StructuredLogger is a utility class");
    }

    /**
     * 记录访问日志
     */
    public static void logAccess(AccessLogDTO accessLog) {
        if (accessLog == null) {
            return;
        }
        
        enrichFromContext(accessLog);
        
        Map<String, Object> logData = new HashMap<>();
        logData.put("logType", LogConstants.LogType.ACCESS);
        logData.put("data", accessLog);
        
        String jsonLog = toJson(logData);
        if (accessLog.getSuccess() != null && !accessLog.getSuccess()) {
            log.warn("ACCESS_LOG: {}", jsonLog);
        } else {
            log.info("ACCESS_LOG: {}", jsonLog);
        }
    }

    /**
     * 记录业务日志
     */
    public static void logBusiness(BusinessLogDTO businessLog) {
        if (businessLog == null) {
            return;
        }
        
        enrichFromContext(businessLog);
        
        Map<String, Object> logData = new HashMap<>();
        logData.put("logType", LogConstants.LogType.BUSINESS);
        logData.put("data", businessLog);
        
        String jsonLog = toJson(logData);
        if (businessLog.getSuccess() != null && !businessLog.getSuccess()) {
            log.warn("BUSINESS_LOG: {}", jsonLog);
        } else {
            log.info("BUSINESS_LOG: {}", jsonLog);
        }
    }

    /**
     * 记录操作日志
     */
    public static void logOperation(OperationLogDTO operationLog) {
        if (operationLog == null) {
            return;
        }
        
        enrichFromContext(operationLog);
        
        Map<String, Object> logData = new HashMap<>();
        logData.put("logType", LogConstants.LogType.OPERATION);
        logData.put("data", operationLog);
        
        String jsonLog = toJson(logData);
        log.info("OPERATION_LOG: {}", jsonLog);
    }

    /**
     * 记录性能日志
     */
    public static void logPerformance(PerformanceLogDTO performanceLog) {
        if (performanceLog == null) {
            return;
        }
        
        enrichFromContext(performanceLog);
        
        Map<String, Object> logData = new HashMap<>();
        logData.put("logType", LogConstants.LogType.PERFORMANCE);
        logData.put("data", performanceLog);
        
        String jsonLog = toJson(logData);
        
        if (performanceLog.getTimeout() != null && performanceLog.getTimeout()) {
            log.warn("PERFORMANCE_LOG: {}", jsonLog);
        } else {
            log.info("PERFORMANCE_LOG: {}", jsonLog);
        }
    }

    /**
     * 记录简单的业务日志（便捷方法）
     */
    public static void logSimpleBusiness(String module, String operation, String description, boolean success) {
        BusinessLogDTO businessLog = BusinessLogDTO.builder()
                .module(module)
                .operation(operation)
                .description(description)
                .success(success)
                .build();
        logBusiness(businessLog);
    }

    /**
     * 记录简单的业务日志（成功场景）
     */
    public static void logSimpleSuccess(String module, String operation, String description) {
        logSimpleBusiness(module, operation, description, true);
    }

    /**
     * 记录简单的业务日志（失败场景）
     */
    public static void logSimpleFailure(String module, String operation, String description, String errorMessage) {
        BusinessLogDTO businessLog = BusinessLogDTO.builder()
                .module(module)
                .operation(operation)
                .description(description)
                .success(false)
                .errorMessage(errorMessage)
                .build();
        logBusiness(businessLog);
    }

    /**
     * 记录方法开始
     */
    public static void logMethodBegin(String module, String method, String params) {
        String maskedParams = SensitiveDataMasker.autoMask(params);
        log.info("METHOD_BEGIN: module={}, method={}, params={}", module, method, maskedParams);
    }

    /**
     * 记录方法结束
     */
    public static void logMethodEnd(String module, String method, long duration) {
        log.info("METHOD_END: module={}, method={}, duration={}ms", module, method, duration);
    }

    /**
     * 记录方法错误
     */
    public static void logMethodError(String module, String method, String errorMessage, Throwable throwable) {
        log.error("METHOD_ERROR: module={}, method={}, error={}", module, method, errorMessage, throwable);
    }

    private static void enrichFromContext(AccessLogDTO dto) {
        if (dto.getTraceId() == null) {
            dto.setTraceId(LogContext.getTraceId().orElse(null));
        }
        if (dto.getServiceId() == null) {
            dto.setServiceId(LogContext.getServiceId().orElse(LogConstants.ServiceName.APP));
        }
        if (dto.getUserId() == null) {
            dto.setUserId(LogContext.getUserId().orElse(null));
        }
        if (dto.getUsername() == null) {
            dto.setUsername(LogContext.getUsername().orElse(null));
        }
        if (dto.getClientIp() == null) {
            dto.setClientIp(LogContext.getClientIp().orElse(null));
        }
    }

    private static void enrichFromContext(BusinessLogDTO dto) {
        if (dto.getTraceId() == null) {
            dto.setTraceId(LogContext.getTraceId().orElse(null));
        }
        if (dto.getServiceId() == null) {
            dto.setServiceId(LogContext.getServiceId().orElse(LogConstants.ServiceName.APP));
        }
        if (dto.getUserId() == null) {
            dto.setUserId(LogContext.getUserId().orElse(null));
        }
        if (dto.getUsername() == null) {
            dto.setUsername(LogContext.getUsername().orElse(null));
        }
    }

    private static void enrichFromContext(OperationLogDTO dto) {
        if (dto.getTraceId() == null) {
            dto.setTraceId(LogContext.getTraceId().orElse(null));
        }
        if (dto.getServiceId() == null) {
            dto.setServiceId(LogContext.getServiceId().orElse(LogConstants.ServiceName.APP));
        }
        if (dto.getUserId() == null) {
            dto.setUserId(LogContext.getUserId().orElse(null));
        }
        if (dto.getUsername() == null) {
            dto.setUsername(LogContext.getUsername().orElse(null));
        }
        if (dto.getClientIp() == null) {
            dto.setClientIp(LogContext.getClientIp().orElse(null));
        }
    }

    private static void enrichFromContext(PerformanceLogDTO dto) {
        if (dto.getTraceId() == null) {
            dto.setTraceId(LogContext.getTraceId().orElse(null));
        }
        if (dto.getServiceId() == null) {
            dto.setServiceId(LogContext.getServiceId().orElse(LogConstants.ServiceName.APP));
        }
    }

    private static String toJson(Object obj) {
        if (obj == null) {
            return "{}";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert to JSON: {}", e.getMessage());
            return obj.toString();
        }
    }

    public static String formatTimestamp(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        return dateTime.format(FORMATTER);
    }
}

