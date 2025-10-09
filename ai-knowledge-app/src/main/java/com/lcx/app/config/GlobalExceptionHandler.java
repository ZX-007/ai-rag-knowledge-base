package com.lcx.app.config;

import com.lcx.api.exception.BaseException;
import com.lcx.api.exception.BusinessException;
import com.lcx.api.exception.SystemException;
import com.lcx.api.response.Response;
import com.lcx.api.response.ResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * <p>该类是Spring Boot应用程序的全局异常处理器，负责统一处理系统中的各种异常。</p>
 * <p>通过@RestControllerAdvice注解，该处理器能够捕获所有Controller层抛出的异常，
 * 并将其转换为统一的响应格式，提供良好的用户体验和系统稳定性。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>异常统一处理：捕获并处理各种类型的异常</li>
 *   <li>响应格式标准化：将异常转换为统一的JSON响应格式</li>
 *   <li>日志记录：详细记录异常信息，便于问题排查</li>
 *   <li>用户友好提示：将技术异常转换为用户可理解的错误信息</li>
 * </ul>
 *
 * <p>支持的异常类型：</p>
 * <ul>
 *   <li>自定义异常：BaseException、BusinessException、SystemException</li>
 *   <li>参数验证异常：MethodArgumentNotValidException、BindException、ConstraintViolationException</li>
 *   <li>HTTP异常：HttpRequestMethodNotSupportedException、NoHandlerFoundException</li>
 *   <li>文件上传异常：MaxUploadSizeExceededException</li>
 *   <li>系统异常：RuntimeException、Exception、IllegalArgumentException等</li>
 * </ul>
 *
 * <p>异常处理策略：</p>
 * <ul>
 *   <li>业务异常：记录警告日志，返回业务错误码和消息</li>
 *   <li>系统异常：记录错误日志，返回用户友好的错误提示</li>
 *   <li>参数异常：记录警告日志，返回参数错误提示</li>
 *   <li>未知异常：记录错误日志，返回通用错误提示</li>
 * </ul>
 *
 * <p>日志记录信息：</p>
 * <ul>
 *   <li>异常类型和消息</li>
 *   <li>请求URI和HTTP方法</li>
 *   <li>客户端IP地址</li>
 *   <li>User-Agent信息（系统异常）</li>
 *   <li>完整的异常堆栈（错误级别）</li>
 * </ul>
 *
 * <p>安全特性：</p>
 * <ul>
 *   <li>敏感信息过滤：避免在响应中暴露系统内部信息</li>
 *   <li>IP地址获取：支持代理环境下的真实IP获取</li>
 *   <li>错误消息转换：将技术错误转换为用户友好消息</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 在Controller中抛出异常，会被自动捕获处理
 * @RestController
 * public class UserController {
 *     @PostMapping("/users")
 *     public Response<User> createUser(@Valid @RequestBody UserRequest request) {
 *         // 参数验证失败会抛出MethodArgumentNotValidException
 *         // 业务逻辑异常会抛出BusinessException
 *         return userService.createUser(request);
 *     }
 * }
 * }</pre>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义基础异常
     *
     * <p>处理继承自BaseException的所有自定义异常。</p>
     * <p>BaseException是系统中所有自定义异常的基类，包含错误码和错误消息。</p>
     *
     * <p>处理逻辑：</p>
     * <ul>
     *   <li>记录警告级别日志，包含异常详细信息和请求上下文</li>
     *   <li>提取异常中的错误码和错误消息</li>
     *   <li>返回标准化的错误响应格式</li>
     * </ul>
     *
     * <p>日志信息包括：</p>
     * <ul>
     *   <li>异常错误码和消息</li>
     *   <li>请求URI和HTTP方法</li>
     *   <li>客户端IP地址</li>
     *   <li>完整的异常堆栈信息</li>
     * </ul>
     *
     * @param request HTTP请求对象，用于获取请求上下文信息
     * @param e BaseException异常实例，包含错误码和错误消息
     * @return Response<Void> 标准化的错误响应，包含异常的错误码和消息
     */
    @ExceptionHandler(BaseException.class)
    public Response<Void> handleBaseException(HttpServletRequest request, BaseException e) {
        log.warn("处理自定义异常: code={}, message={}, request={}, method={}, ip={}",
                e.getCode(), e.getMessage(), request.getRequestURI(),
                request.getMethod(), getClientIpAddress(request), e);
        return Response.failure(e.getCode(), e.getMessage());
    }

    /**
     * 处理业务异常
     *
     * <p>专门处理BusinessException类型的业务逻辑异常。</p>
     * <p>BusinessException通常表示业务规则违反或业务流程中的预期错误。</p>
     *
     * <p>典型的业务异常场景：</p>
     * <ul>
     *   <li>用户权限不足</li>
     *   <li>数据状态不符合业务规则</li>
     *   <li>业务流程约束违反</li>
     *   <li>资源不存在或已被删除</li>
     * </ul>
     *
     * <p>处理特点：</p>
     * <ul>
     *   <li>记录警告级别日志，不记录完整堆栈</li>
     *   <li>直接返回业务异常中的错误码和消息</li>
     *   <li>适合向用户展示的友好错误信息</li>
     * </ul>
     *
     * @param request HTTP请求对象，用于获取请求上下文信息
     * @param e BusinessException业务异常实例
     * @return Response<Void> 包含业务错误码和消息的响应
     */
    @ExceptionHandler(BusinessException.class)
    public Response<Void> handleBusinessException(HttpServletRequest request, BusinessException e) {
        log.warn("处理业务异常: code={}, message={}, request={}, method={}, ip={}",
                e.getCode(), e.getMessage(), request.getRequestURI(),
                request.getMethod(), getClientIpAddress(request), e);
        return Response.failure(e.getCode(), e.getMessage());
    }

    /**
     * 处理系统异常
     *
     * <p>处理SystemException类型的系统级异常。</p>
     * <p>SystemException通常表示系统内部错误，如数据库连接失败、外部服务不可用等。</p>
     *
     * <p>典型的系统异常场景：</p>
     * <ul>
     *   <li>数据库连接异常</li>
     *   <li>外部API调用失败</li>
     *   <li>文件系统操作失败</li>
     *   <li>网络连接超时</li>
     *   <li>AI服务不可用</li>
     * </ul>
     *
     * <p>处理特点：</p>
     * <ul>
     *   <li>记录错误级别日志，包含完整堆栈信息</li>
     *   <li>记录User-Agent信息用于问题分析</li>
     *   <li>将技术错误转换为用户友好的错误消息</li>
     *   <li>避免向用户暴露系统内部技术细节</li>
     * </ul>
     *
     * <p>错误消息转换：</p>
     * <ul>
     *   <li>根据错误码映射为用户友好的提示信息</li>
     *   <li>提供重试建议或联系技术支持的指导</li>
     * </ul>
     *
     * @param request HTTP请求对象，用于获取请求上下文信息
     * @param e SystemException系统异常实例
     * @return Response<Void> 包含用户友好错误消息的响应
     */
    @ExceptionHandler(SystemException.class)
    public Response<Void> handleSystemException(HttpServletRequest request, SystemException e) {
        log.error("处理系统异常: code={}, message={}, request={}, method={}, ip={}, userAgent={}",
                e.getCode(), e.getMessage(), request.getRequestURI(),
                request.getMethod(), getClientIpAddress(request), 
                request.getHeader("User-Agent"), e);

        String userFriendlyMessage = getUserFriendlyMessage(e.getCode(), e.getMessage());
        return Response.failure(e.getCode(), userFriendlyMessage);
    }

    /**
     * 处理RuntimeException
     *
     * @param request HTTP请求
     * @param e       异常
     * @return 错误响应
     */
    @ExceptionHandler(RuntimeException.class)
    public Response<Void> handleRuntimeException(HttpServletRequest request, RuntimeException e) {
        log.error("处理运行时异常: message={}, request={}, method={}, ip={}",
                e.getMessage(), request.getRequestURI(), request.getMethod(), getClientIpAddress(request), e);
        return Response.failure(ResponseCode.INTERNAL_ERROR.getCode(), "系统内部错误");
    }

    /**
     * 处理其他所有异常
     *
     * @param request HTTP请求
     * @param e       异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public Response<Void> handleException(HttpServletRequest request, Exception e) {
        log.error("处理未知异常: message={}, request={}, method={}, ip={}",
                e.getMessage(), request.getRequestURI(), request.getMethod(),
                getClientIpAddress(request), e);
        return Response.failure(ResponseCode.INTERNAL_ERROR.getCode(), "系统内部错误");
    }

    /**
     * 处理参数验证异常（@Valid注解）
     *
     * @param request HTTP请求
     * @param e       异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<Void> handleMethodArgumentNotValidException(
            HttpServletRequest request, MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("处理参数验证异常: message={}, request={}, method={}, ip={}",
                message, request.getRequestURI(), request.getMethod(), getClientIpAddress(request), e);
        return Response.paramError(message);
    }

    /**
     * 处理绑定异常
     *
     * @param request HTTP请求
     * @param e       异常
     * @return 错误响应
     */
    @ExceptionHandler(BindException.class)
    public Response<Void> handleBindException(HttpServletRequest request, BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("处理绑定异常: message={}, request={}, method={}, ip={}",
                message, request.getRequestURI(), request.getMethod(), getClientIpAddress(request), e);
        return Response.paramError(message);
    }

    /**
     * 处理约束违反异常（@Valid注解在方法参数上的校验）
     *
     * @param request HTTP请求
     * @param e       异常
     * @return 错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Response<Void> handleConstraintViolationException(
            HttpServletRequest request, ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("处理约束违反异常: message={}, request={}, method={}, ip={}",
                message, request.getRequestURI(), request.getMethod(), getClientIpAddress(request), e);
        return Response.paramError(message);
    }

    /**
     * 处理缺少请求参数异常
     *
     * @param request HTTP请求
     * @param e       异常
     * @return 错误响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Response<Void> handleMissingServletRequestParameterException(
            HttpServletRequest request, MissingServletRequestParameterException e) {
        String message = "缺少必需的请求参数: " + e.getParameterName();
        log.warn("处理缺少请求参数异常: message={}, request={}, method={}, ip={}",
                message, request.getRequestURI(), request.getMethod(), getClientIpAddress(request), e);
        return Response.paramError(message);
    }

    /**
     * 处理方法参数类型不匹配异常
     *
     * @param request HTTP请求
     * @param e       异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Response<Void> handleMethodArgumentTypeMismatchException(
            HttpServletRequest request, MethodArgumentTypeMismatchException e) {
        Class<?> requiredTypeClass = e.getRequiredType();
        String requiredType = requiredTypeClass != null ? requiredTypeClass.getSimpleName() : "未知类型";
        String message = "参数类型错误: " + e.getName() + " 应为 " + requiredType;
        log.warn("处理参数类型不匹配异常: message={}, request={}, method={}, ip={}",
                message, request.getRequestURI(), request.getMethod(), getClientIpAddress(request), e);
        return Response.paramError(message);
    }

    /**
     * 处理HTTP消息不可读异常
     *
     * @param request HTTP请求
     * @param e       异常
     * @return 错误响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Response<Void> handleHttpMessageNotReadableException(
            HttpServletRequest request, HttpMessageNotReadableException e) {
        String message = "请求体格式错误或不可读";
        log.warn("处理HTTP消息不可读异常: message={}, request={}, method={}, ip={}",
                message, request.getRequestURI(), request.getMethod(), getClientIpAddress(request), e);
        return Response.paramError(message);
    }

    /**
     * 处理HTTP请求方法不支持异常
     *
     * @param request HTTP请求
     * @param e       异常
     * @return 错误响应
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Response<Void> handleHttpRequestMethodNotSupportedException(
            HttpServletRequest request, HttpRequestMethodNotSupportedException e) {
        String message = "不支持的HTTP请求方法: " + e.getMethod();
        log.warn("处理HTTP请求方法不支持异常: message={}, request={}, method={}, ip={}",
                message, request.getRequestURI(), request.getMethod(), getClientIpAddress(request), e);
        return Response.failure(ResponseCode.METHOD_NOT_ALLOWED.getCode(), message);
    }

    /**
     * 处理404异常
     *
     * @param request HTTP请求
     * @param e       异常
     * @return 错误响应
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Response<Void> handleNoHandlerFoundException(
            HttpServletRequest request, NoHandlerFoundException e) {
        String message = "请求的资源不存在: " + e.getRequestURL();
        log.warn("处理404异常: message={}, request={}, method={}, ip={}",
                message, request.getRequestURI(), request.getMethod(), getClientIpAddress(request), e);
        return Response.failure(ResponseCode.NOT_FOUND.getCode(), message);
    }

    /**
     * 处理文件上传大小超限异常
     *
     * @param request HTTP请求
     * @param e       异常
     * @return 错误响应
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Response<Void> handleMaxUploadSizeExceededException(
            HttpServletRequest request, MaxUploadSizeExceededException e) {
        String message = "上传文件大小超出限制";
        log.warn("处理文件上传大小超限异常: message={}, request={}, method={}, ip={}",
                message, request.getRequestURI(), request.getMethod(), getClientIpAddress(request), e);
        return Response.failure(ResponseCode.FILE_UPLOAD_ERROR.getCode(), message);
    }

    /**
     * 处理IllegalArgumentException
     *
     * @param request HTTP请求
     * @param e       异常
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Response<Void> handleIllegalArgumentException(
            HttpServletRequest request, IllegalArgumentException e) {
        log.warn("处理非法参数异常: message={}, request={}, method={}, ip={}",
                e.getMessage(), request.getRequestURI(), request.getMethod(), getClientIpAddress(request), e);
        return Response.paramError(e.getMessage());
    }

    /**
     * 处理IllegalStateException
     *
     * @param request HTTP请求
     * @param e       异常
     * @return 错误响应
     */
    @ExceptionHandler(IllegalStateException.class)
    public Response<Void> handleIllegalStateException(
            HttpServletRequest request, IllegalStateException e) {
        log.error("处理非法状态异常: message={}, request={}, method={}, ip={}",
                e.getMessage(), request.getRequestURI(), request.getMethod(), getClientIpAddress(request), e);
        return Response.failure(ResponseCode.INTERNAL_ERROR.getCode(), e.getMessage());
    }

    /**
     * 获取客户端真实IP地址
     *
     * <p>该方法用于获取客户端的真实IP地址，支持代理和负载均衡环境。</p>
     * <p>在使用反向代理（如Nginx）或负载均衡器的环境中，直接获取的IP可能是代理服务器的IP，
     * 需要通过特定的HTTP头来获取客户端的真实IP地址。</p>
     *
     * <p>IP获取优先级：</p>
     * <ol>
     *   <li>X-Forwarded-For：标准的代理转发头，可能包含多个IP（客户端IP在第一个）</li>
     *   <li>X-Real-IP：Nginx等代理服务器设置的真实IP头</li>
     *   <li>RemoteAddr：直接连接的IP地址（可能是代理IP）</li>
     * </ol>
     *
     * <p>处理逻辑：</p>
     * <ul>
     *   <li>检查X-Forwarded-For头，提取第一个有效IP</li>
     *   <li>检查X-Real-IP头，获取代理设置的真实IP</li>
     *   <li>最后使用request.getRemoteAddr()作为兜底方案</li>
     *   <li>过滤"unknown"等无效值</li>
     * </ul>
     *
     * <p>安全考虑：</p>
     * <ul>
     *   <li>X-Forwarded-For可能被伪造，在安全敏感场景需要额外验证</li>
     *   <li>建议在代理层面配置可信的IP头设置</li>
     * </ul>
     *
     * @param request HTTP请求对象，包含请求头信息
     * @return String 客户端真实IP地址，如果无法获取则返回连接IP
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 根据错误代码获取用户友好的错误消息
     *
     * <p>该方法将系统内部的技术错误代码转换为用户可理解的友好错误消息。</p>
     * <p>主要目的是避免向用户暴露系统内部的技术细节，同时提供有用的错误提示和解决建议。</p>
     *
     * <p>错误代码分类：</p>
     * <ul>
     *   <li>5001-5016：系统级错误，如数据库、网络、AI服务等</li>
     *   <li>每个错误代码对应特定的系统组件或服务</li>
     *   <li>提供针对性的用户提示和解决建议</li>
     * </ul>
     *
     * <p>消息转换策略：</p>
     * <ul>
     *   <li>技术术语转换：将技术错误转换为通俗易懂的描述</li>
     *   <li>解决建议：提供用户可以采取的解决方案</li>
     *   <li>联系支持：对于严重错误提供技术支持联系建议</li>
     *   <li>重试提示：对于临时性错误建议用户重试</li>
     * </ul>
     *
     * <p>特殊处理：</p>
     * <ul>
     *   <li>对于包含" - "分隔符的消息，提取业务相关部分</li>
     *   <li>未知错误代码使用通用的友好提示</li>
     *   <li>保持消息的一致性和专业性</li>
     * </ul>
     *
     * <p>支持的错误代码：</p>
     * <ul>
     *   <li>5001：数据库服务异常</li>
     *   <li>5002：文件上传异常</li>
     *   <li>5005：AI服务异常</li>
     *   <li>5006：缓存服务异常</li>
     *   <li>5007：向量数据库异常</li>
     *   <li>5008：网络连接异常</li>
     *   <li>其他：参见方法实现</li>
     * </ul>
     *
     * @param code 系统错误代码，用于识别错误类型
     * @param message 原始错误消息，可能包含技术细节
     * @return String 用户友好的错误消息，包含解决建议
     */
    private String getUserFriendlyMessage(String code, String message) {
        return switch (code) {
            case "5001" -> "数据库服务暂时不可用，请稍后重试。如问题持续存在，请联系技术支持。";
            case "5002" -> "文件上传失败，请检查文件格式和大小后重试。";
            case "5005" -> "AI服务暂时不可用，请稍后重试。";
            case "5006" -> "缓存服务暂时不可用，请稍后重试。";
            case "5007" -> "向量数据库服务暂时不可用，请稍后重试。";
            case "5008" -> "网络连接异常，请检查网络连接后重试。";
            case "5009" -> "文件解析失败，请检查文件格式是否正确。";
            case "5010" -> "AI模型加载失败，请稍后重试。";
            case "5011" -> "系统配置错误，请联系技术支持。";
            case "5012" -> "系统资源不足，请稍后重试。";
            case "5013" -> "外部服务调用失败，请稍后重试。";
            case "5014" -> "数据格式错误，请检查输入数据。";
            case "5015" -> "权限验证失败，请检查访问权限。";
            case "5016" -> "系统繁忙，请稍后重试。";
            case "5000" -> "系统内部错误，请稍后重试。如问题持续存在，请联系技术支持。";
            case "5003" -> "服务暂时不可用，请稍后重试。";
            case "5004" -> "请求超时，请稍后重试。";
            default -> {
                // 如果原始消息包含业务上下文，提取用户友好的部分
                if (message != null && message.contains(" - ")) {
                    String[] parts = message.split(" - ", 2);
                    if (parts.length > 1) {
                        yield "系统异常：" + parts[1] + "，请稍后重试。";
                    }
                }
                yield "系统异常，请稍后重试。如问题持续存在，请联系技术支持。";
            }
        };
    }

}
