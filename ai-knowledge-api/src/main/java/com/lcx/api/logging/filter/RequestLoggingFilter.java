package com.lcx.api.logging.filter;

import com.lcx.api.logging.LogConstants;
import com.lcx.api.logging.context.LogContext;
import com.lcx.api.logging.dto.AccessLogDTO;
import com.lcx.api.logging.util.StructuredLogger;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 请求日志过滤器（企业级）
 *
 * <p>基于企业规范的请求日志过滤器，提供以下功能：</p>
 * <ul>
 *   <li>自动初始化日志上下文（MDC）</li>
 *   <li>生成或传递追踪ID</li>
 *   <li>记录结构化访问日志</li>
 *   <li>支持跨服务追踪</li>
 *   <li>自动清理上下文信息</li>
 * </ul>
 * 
 * <p>日志信息包括：</p>
 * <ul>
 *   <li>追踪ID：用于分布式追踪</li>
 *   <li>请求信息：方法、URI、IP等</li>
 *   <li>性能指标：请求耗时</li>
 *   <li>响应状态：HTTP状态码</li>
 * </ul>
 * 
 * @author lcx
 * @version 2.0
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, 
            @NonNull HttpServletResponse response, 
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();

        // 初始化日志上下文
        String traceId = request.getHeader(LogConstants.HttpHeader.TRACE_ID);
        LogContext.init(traceId);
        
        // 获取生成或传递的追踪ID
        traceId = LogContext.getTraceId().orElse(null);

        // 获取请求信息
        String httpMethod = request.getMethod();
        String requestUri = buildFullUri(request);
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader(LogConstants.HttpHeader.USER_AGENT);

        // 设置上下文信息
        LogContext.setHttpMethod(httpMethod);
        LogContext.setRequestUri(requestUri);
        LogContext.setClientIp(clientIp);

        // 在响应头回传追踪ID，便于前后端联动和分布式追踪
        response.setHeader(LogConstants.HttpHeader.TRACE_ID, traceId);

        // 记录请求开始日志（简洁版）
        log.info("REQUEST_BEGIN: method={}, uri={}, ip={}, ua={}", 
                httpMethod, requestUri, clientIp, truncateUserAgent(userAgent));

        int statusCode = 200;
        boolean success = true;
        String errorMessage = null;

        try {
            // 执行请求处理链
            filterChain.doFilter(request, response);
            statusCode = response.getStatus();
            success = statusCode < 400;
        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
            statusCode = 500;
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // 记录请求结束日志（简洁版）
            log.info("REQUEST_END: status={}, duration={}ms", statusCode, duration);

            // 记录结构化访问日志
            AccessLogDTO accessLog = AccessLogDTO.builder()
                    .traceId(traceId)
                    .httpMethod(httpMethod)
                    .requestUri(requestUri)
                    .clientIp(clientIp)
                    .userAgent(userAgent)
                    .startTime(startTime)
                    .endTime(endTime)
                    .duration(duration)
                    .statusCode(statusCode)
                    .success(success)
                    .errorMessage(errorMessage)
                    .build();
            
            StructuredLogger.logAccess(accessLog);

            // 清理MDC上下文，避免内存泄漏
            LogContext.clear();
        }
    }

    /**
     * 构建完整的请求URI（包含查询字符串）
     * 
     * @param request HTTP请求
     * @return 完整的URI
     */
    private String buildFullUri(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String query = request.getQueryString();
        return query == null ? requestUri : requestUri + "?" + query;
    }

    /**
     * 获取客户端真实IP地址
     * <p>支持代理和负载均衡环境</p>
     * <p>优先级：X-Forwarded-For > X-Real-IP > RemoteAddr</p>
     * 
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // 优先从X-Forwarded-For获取（支持多级代理）
        String xForwardedFor = request.getHeader(LogConstants.HttpHeader.X_FORWARDED_FOR);
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            // X-Forwarded-For可能包含多个IP，取第一个（客户端真实IP）
            return xForwardedFor.split(",")[0].trim();
        }

        // 从X-Real-IP获取（Nginx等代理设置）
        String xRealIp = request.getHeader(LogConstants.HttpHeader.X_REAL_IP);
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        // 兜底：直接获取连接IP
        return request.getRemoteAddr();
    }

    /**
     * 截断User-Agent字符串，避免日志过长
     * 
     * @param userAgent User-Agent字符串
     * @return 截断后的User-Agent
     */
    private String truncateUserAgent(String userAgent) {
        if (userAgent == null) {
            return null;
        }
        if (userAgent.length() <= 100) {
            return userAgent;
        }
        return userAgent.substring(0, 100) + "...";
    }
}


