package com.lcx.api.logging.context;

import com.lcx.api.logging.LogConstants;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 日志上下文管理器
 * 
 * <p>基于MDC（Mapped Diagnostic Context）实现的线程安全日志上下文管理器。</p>
 * <p>提供统一的上下文信息管理，支持追踪ID、用户信息、业务信息等的设置和获取。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>追踪ID管理：自动生成或使用传入的追踪ID</li>
 *   <li>用户信息管理：用户ID、用户名等</li>
 *   <li>请求信息管理：IP、请求URI、HTTP方法等</li>
 *   <li>业务信息管理：模块、操作等</li>
 *   <li>上下文清理：请求结束后自动清理</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * // 初始化上下文
 * LogContext.init();
 * 
 * // 设置用户信息
 * LogContext.setUserId("user123");
 * LogContext.setUsername("张三");
 * 
 * // 设置业务信息
 * LogContext.setModule("AI_CHAT");
 * LogContext.setOperation("GENERATE");
 * 
 * // 记录日志（MDC信息会自动包含在日志中）
 * log.info("处理用户请求");
 * 
 * // 清理上下文（在Filter或拦截器的finally块中）
 * LogContext.clear();
 * }</pre>
 * 
 * @author lcx
 * @version 1.0
 */
public final class LogContext {

    private LogContext() {
        throw new UnsupportedOperationException("LogContext is a utility class");
    }

    /**
     * 初始化日志上下文
     * <p>生成新的追踪ID并设置服务ID</p>
     */
    public static void init() {
        String traceId = generateTraceId();
        MDC.put(LogConstants.MdcKey.TRACE_ID, traceId);
        MDC.put(LogConstants.MdcKey.SERVICE_ID, LogConstants.ServiceName.APP);
    }

    /**
     * 使用指定的追踪ID初始化日志上下文
     * 
     * @param traceId 追踪ID
     */
    public static void init(String traceId) {
        MDC.put(LogConstants.MdcKey.TRACE_ID, traceId != null && !traceId.isEmpty() ? traceId : generateTraceId());
        MDC.put(LogConstants.MdcKey.SERVICE_ID, LogConstants.ServiceName.APP);
    }

    /**
     * 生成追踪ID
     * <p>使用UUID生成32位追踪ID（去除中划线）</p>
     * 
     * @return 追踪ID
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取当前追踪ID
     * 
     * @return 追踪ID，如果不存在则返回Optional.empty()
     */
    public static Optional<String> getTraceId() {
        return Optional.ofNullable(MDC.get(LogConstants.MdcKey.TRACE_ID));
    }

    /**
     * 设置用户ID
     * 
     * @param userId 用户ID
     */
    public static void setUserId(String userId) {
        if (userId != null && !userId.isEmpty()) {
            MDC.put(LogConstants.MdcKey.USER_ID, userId);
        }
    }

    /**
     * 获取用户ID
     * 
     * @return 用户ID
     */
    public static Optional<String> getUserId() {
        return Optional.ofNullable(MDC.get(LogConstants.MdcKey.USER_ID));
    }

    /**
     * 设置用户名
     * 
     * @param username 用户名
     */
    public static void setUsername(String username) {
        if (username != null && !username.isEmpty()) {
            MDC.put(LogConstants.MdcKey.USERNAME, username);
        }
    }

    /**
     * 获取用户名
     * 
     * @return 用户名
     */
    public static Optional<String> getUsername() {
        return Optional.ofNullable(MDC.get(LogConstants.MdcKey.USERNAME));
    }

    /**
     * 设置客户端IP
     * 
     * @param clientIp 客户端IP
     */
    public static void setClientIp(String clientIp) {
        if (clientIp != null && !clientIp.isEmpty()) {
            MDC.put(LogConstants.MdcKey.CLIENT_IP, clientIp);
        }
    }

    /**
     * 获取客户端IP
     * 
     * @return 客户端IP
     */
    public static Optional<String> getClientIp() {
        return Optional.ofNullable(MDC.get(LogConstants.MdcKey.CLIENT_IP));
    }

    /**
     * 设置HTTP请求方法
     * 
     * @param method HTTP方法（GET、POST等）
     */
    public static void setHttpMethod(String method) {
        if (method != null && !method.isEmpty()) {
            MDC.put(LogConstants.MdcKey.HTTP_METHOD, method);
        }
    }

    /**
     * 获取HTTP请求方法
     * 
     * @return HTTP方法
     */
    public static Optional<String> getHttpMethod() {
        return Optional.ofNullable(MDC.get(LogConstants.MdcKey.HTTP_METHOD));
    }

    /**
     * 设置请求URI
     * 
     * @param uri 请求URI
     */
    public static void setRequestUri(String uri) {
        if (uri != null && !uri.isEmpty()) {
            MDC.put(LogConstants.MdcKey.REQUEST_URI, uri);
        }
    }

    /**
     * 获取请求URI
     * 
     * @return 请求URI
     */
    public static Optional<String> getRequestUri() {
        return Optional.ofNullable(MDC.get(LogConstants.MdcKey.REQUEST_URI));
    }

    /**
     * 设置会话ID
     * 
     * @param sessionId 会话ID
     */
    public static void setSessionId(String sessionId) {
        if (sessionId != null && !sessionId.isEmpty()) {
            MDC.put(LogConstants.MdcKey.SESSION_ID, sessionId);
        }
    }

    /**
     * 获取会话ID
     * 
     * @return 会话ID
     */
    public static Optional<String> getSessionId() {
        return Optional.ofNullable(MDC.get(LogConstants.MdcKey.SESSION_ID));
    }

    /**
     * 设置租户ID（多租户场景）
     * 
     * @param tenantId 租户ID
     */
    public static void setTenantId(String tenantId) {
        if (tenantId != null && !tenantId.isEmpty()) {
            MDC.put(LogConstants.MdcKey.TENANT_ID, tenantId);
        }
    }

    /**
     * 获取租户ID
     * 
     * @return 租户ID
     */
    public static Optional<String> getTenantId() {
        return Optional.ofNullable(MDC.get(LogConstants.MdcKey.TENANT_ID));
    }

    /**
     * 设置业务模块
     * 
     * @param module 业务模块
     */
    public static void setModule(String module) {
        if (module != null && !module.isEmpty()) {
            MDC.put(LogConstants.MdcKey.MODULE, module);
        }
    }

    /**
     * 获取业务模块
     * 
     * @return 业务模块
     */
    public static Optional<String> getModule() {
        return Optional.ofNullable(MDC.get(LogConstants.MdcKey.MODULE));
    }

    /**
     * 设置业务操作
     * 
     * @param operation 业务操作
     */
    public static void setOperation(String operation) {
        if (operation != null && !operation.isEmpty()) {
            MDC.put(LogConstants.MdcKey.OPERATION, operation);
        }
    }

    /**
     * 获取业务操作
     * 
     * @return 业务操作
     */
    public static Optional<String> getOperation() {
        return Optional.ofNullable(MDC.get(LogConstants.MdcKey.OPERATION));
    }

    /**
     * 设置服务ID
     * 
     * @param serviceId 服务ID
     */
    public static void setServiceId(String serviceId) {
        if (serviceId != null && !serviceId.isEmpty()) {
            MDC.put(LogConstants.MdcKey.SERVICE_ID, serviceId);
        }
    }

    /**
     * 获取服务ID
     * 
     * @return 服务ID
     */
    public static Optional<String> getServiceId() {
        return Optional.ofNullable(MDC.get(LogConstants.MdcKey.SERVICE_ID));
    }

    /**
     * 设置自定义属性
     * 
     * @param key 属性键
     * @param value 属性值
     */
    public static void put(String key, String value) {
        if (key != null && !key.isEmpty() && value != null) {
            MDC.put(key, value);
        }
    }

    /**
     * 获取自定义属性
     * 
     * @param key 属性键
     * @return 属性值
     */
    public static Optional<String> get(String key) {
        return Optional.ofNullable(MDC.get(key));
    }

    /**
     * 移除指定属性
     * 
     * @param key 属性键
     */
    public static void remove(String key) {
        if (key != null && !key.isEmpty()) {
            MDC.remove(key);
        }
    }

    /**
     * 获取所有MDC上下文信息
     * 
     * @return MDC上下文信息的副本
     */
    public static Map<String, String> getContext() {
        Map<String, String> context = MDC.getCopyOfContextMap();
        return context != null ? context : new HashMap<>();
    }

    /**
     * 设置MDC上下文信息
     * <p>用于跨线程传递上下文信息</p>
     * 
     * @param context 上下文信息
     */
    public static void setContext(Map<String, String> context) {
        if (context != null && !context.isEmpty()) {
            MDC.setContextMap(context);
        }
    }

    /**
     * 清理所有MDC上下文信息
     * <p>通常在请求结束时调用，避免内存泄漏</p>
     */
    public static void clear() {
        MDC.clear();
    }

    /**
     * 清理特定的上下文信息（保留追踪ID和服务ID）
     * <p>用于在长时间运行的线程中部分清理上下文</p>
     */
    public static void clearExceptCore() {
        String traceId = MDC.get(LogConstants.MdcKey.TRACE_ID);
        String serviceId = MDC.get(LogConstants.MdcKey.SERVICE_ID);
        
        MDC.clear();
        
        if (traceId != null) {
            MDC.put(LogConstants.MdcKey.TRACE_ID, traceId);
        }
        if (serviceId != null) {
            MDC.put(LogConstants.MdcKey.SERVICE_ID, serviceId);
        }
    }
}

