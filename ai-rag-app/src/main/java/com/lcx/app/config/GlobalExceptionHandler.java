package com.lcx.app.config;

import com.lcx.api.exception.BaseException;
import com.lcx.api.exception.BusinessException;
import com.lcx.api.exception.ParameterException;
import com.lcx.api.exception.SystemException;
import com.lcx.api.response.Response;
import com.lcx.api.response.ResponseCode;
import jakarta.servlet.http.HttpServletRequest;
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
 * <p>
 * 统一处理系统中的各种异常，提供规范的错误响应格式。
 * 支持自定义异常、参数验证异常、系统异常等多种异常类型的处理。
 * </p>
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
     * @param request HTTP请求
     * @param e       异常
     * @return 错误响应
     */
    @ExceptionHandler(BaseException.class)
    public Response<Void> handleBaseException(HttpServletRequest request, BaseException e) {
        log.warn("处理自定义异常: {} - {}", e.getCode(), e.getMessage(), e);
        return Response.failure(e.getCode(), e.getMessage());
    }

    /**
     * 处理业务异常
     *
     * @param request HTTP请求
     * @param e       异常
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public Response<Void> handleBusinessException(HttpServletRequest request, BusinessException e) {
        log.warn("处理业务异常: {} - {}", e.getCode(), e.getMessage(), e);
        return Response.failure(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数异常
     *
     * @param request HTTP请求
     * @param e       异常
     * @return 错误响应
     */
    @ExceptionHandler(ParameterException.class)
    public Response<Void> handleParameterException(HttpServletRequest request, ParameterException e) {
        log.warn("处理参数异常: {} - {}", e.getCode(), e.getMessage(), e);
        return Response.failure(e.getCode(), e.getMessage());
    }

    /**
     * 处理系统异常
     *
     * @param request HTTP请求
     * @param e       异常
     * @return 错误响应
     */
    @ExceptionHandler(SystemException.class)
    public Response<Void> handleSystemException(HttpServletRequest request, SystemException e) {
        log.error("处理系统异常: {} - {}", e.getCode(), e.getMessage(), e);
        return Response.failure(e.getCode(), e.getMessage());
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
        log.warn("处理参数验证异常: {}", message, e);
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
        log.warn("处理绑定异常: {}", message, e);
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
        log.warn("处理缺少请求参数异常: {}", message, e);
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
        String message = "参数类型错误: " + e.getName() + " 应为 " + e.getRequiredType().getSimpleName();
        log.warn("处理参数类型不匹配异常: {}", message, e);
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
        log.warn("处理HTTP消息不可读异常: {}", message, e);
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
        log.warn("处理HTTP请求方法不支持异常: {}", message, e);
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
        log.warn("处理404异常: {}", message, e);
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
        log.warn("处理文件上传大小超限异常: {}", message, e);
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
        log.warn("处理非法参数异常: {}", e.getMessage(), e);
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
        log.warn("处理非法状态异常: {}", e.getMessage(), e);
        return Response.failure(ResponseCode.INTERNAL_ERROR.getCode(), e.getMessage());
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
        log.error("处理运行时异常: {}", e.getMessage(), e);
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
        log.error("处理未知异常: {}", e.getMessage(), e);
        return Response.failure(ResponseCode.INTERNAL_ERROR.getCode(), "系统内部错误");
    }
}
