package com.lcx.api.logging.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lcx.api.logging.annotation.LogOperation;
import com.lcx.api.logging.annotation.LogPerformance;
import com.lcx.api.logging.context.LogContext;
import com.lcx.api.logging.dto.OperationLogDTO;
import com.lcx.api.logging.dto.PerformanceLogDTO;
import com.lcx.api.logging.util.SensitiveDataMasker;
import com.lcx.api.logging.util.StructuredLogger;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 日志切面
 * 
 * <p>基于AOP实现的日志切面，自动记录标记了日志注解的方法执行情况。</p>
 * <p>支持操作日志和性能日志的自动记录。</p>
 * 
 * @author lcx
 * @version 1.0
 */
@Slf4j
@Aspect
@Component
@Order(100)
public class LogAspect {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 操作日志切面
     */
    @Around("@annotation(logOperation)")
    public Object aroundLogOperation(ProceedingJoinPoint joinPoint, LogOperation logOperation) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = method.getName();
        
        Object[] args = joinPoint.getArgs();
        String params = null;
        if (logOperation.logParams() && args != null && args.length > 0) {
            params = formatParams(args);
        }
        
        OperationLogDTO operationLog = OperationLogDTO.builder()
                .operationType(logOperation.operation().getCode())
                .module(logOperation.module())
                .method(className + "." + methodName)
                .methodDescription(logOperation.description())
                .requestParams(params)
                .operationTime(startTime)
                .build();
        
        Object result = null;
        boolean success = true;
        String errorMessage = null;
        
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable throwable) {
            success = false;
            errorMessage = throwable.getMessage();
            if (logOperation.logException()) {
                log.error("Method execution failed: {}.{}", className, methodName, throwable);
            }
            throw throwable;
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            if (logOperation.logResult() && result != null) {
                String resultStr = formatResult(result);
                operationLog.setResponseResult(resultStr);
            }
            
            operationLog.setDuration(duration);
            operationLog.setSuccess(success);
            operationLog.setErrorMessage(errorMessage);
            
            StructuredLogger.logOperation(operationLog);
        }
    }

    /**
     * 性能日志切面
     */
    @Around("@annotation(logPerformance)")
    public Object aroundLogPerformance(ProceedingJoinPoint joinPoint, LogPerformance logPerformance) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = method.getName();
        String methodSignature = className + "." + methodName;
        
        String checkpointName = logPerformance.checkpointName();
        if (checkpointName == null || checkpointName.isEmpty()) {
            checkpointName = methodName;
        }
        
        String module = LogContext.getModule().orElse(null);
        
        Object result = null;
        boolean success = true;
        String errorMessage = null;
        
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable throwable) {
            success = false;
            errorMessage = throwable.getMessage();
            throw throwable;
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            boolean timeout = duration > logPerformance.timeoutThreshold();
            
            PerformanceLogDTO performanceLog = PerformanceLogDTO.builder()
                    .checkpointName(checkpointName)
                    .module(module)
                    .methodSignature(methodSignature)
                    .startTime(startTime)
                    .endTime(endTime)
                    .duration(duration)
                    .timeout(timeout)
                    .timeoutThreshold(logPerformance.timeoutThreshold())
                    .success(success)
                    .errorMessage(errorMessage)
                    .build();
            
            StructuredLogger.logPerformance(performanceLog);
        }
    }

    private String formatParams(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        
        try {
            String jsonStr = OBJECT_MAPPER.writeValueAsString(args);
            return SensitiveDataMasker.autoMask(jsonStr);
        } catch (JsonProcessingException e) {
            log.warn("Failed to format params: {}", e.getMessage());
            return "Failed to serialize params";
        }
    }

    private String formatResult(Object result) {
        if (result == null) {
            return null;
        }
        
        try {
            String jsonStr = OBJECT_MAPPER.writeValueAsString(result);
            if (jsonStr.length() > 1000) {
                return jsonStr.substring(0, 1000) + "...[truncated]";
            }
            return jsonStr;
        } catch (JsonProcessingException e) {
            log.warn("Failed to format result: {}", e.getMessage());
            return result.toString();
        }
    }
}

