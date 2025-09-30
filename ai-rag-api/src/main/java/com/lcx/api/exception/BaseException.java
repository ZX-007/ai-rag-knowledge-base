package com.lcx.api.exception;

import com.lcx.api.response.ResponseCode;
import lombok.Getter;

/**
 * 基础异常类
 * <p>
 * 所有业务异常的基类，提供统一的异常处理机制。
 * 包含错误码、错误消息和可选的详细错误信息。
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
@Getter
public class BaseException extends RuntimeException {

    /**
     * 错误码
     */
    private final String code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 详细错误信息（可选）
     */
    private final String details;

    /**
     * 构造函数
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BaseException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
        this.details = null;
    }

    /**
     * 构造函数
     *
     * @param code    错误码
     * @param message 错误消息
     * @param cause   原因异常
     */
    public BaseException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.details = null;
    }

    /**
     * 构造函数
     *
     * @param code    错误码
     * @param message 错误消息
     * @param details 详细错误信息
     */
    public BaseException(String code, String message, String details) {
        super(message);
        this.code = code;
        this.message = message;
        this.details = details;
    }

    /**
     * 构造函数
     *
     * @param code    错误码
     * @param message 错误消息
     * @param details 详细错误信息
     * @param cause   原因异常
     */
    public BaseException(String code, String message, String details, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.details = details;
    }

    /**
     * 使用ResponseCode创建异常
     *
     * @param responseCode 响应码枚举
     */
    public BaseException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.code = responseCode.getCode();
        this.message = responseCode.getMessage();
        this.details = null;
    }

    /**
     * 使用ResponseCode创建异常
     *
     * @param responseCode 响应码枚举
     * @param cause        原因异常
     */
    public BaseException(ResponseCode responseCode, Throwable cause) {
        super(responseCode.getMessage(), cause);
        this.code = responseCode.getCode();
        this.message = responseCode.getMessage();
        this.details = null;
    }

    /**
     * 使用ResponseCode创建异常
     *
     * @param responseCode 响应码枚举
     * @param details      详细错误信息
     */
    public BaseException(ResponseCode responseCode, String details) {
        super(responseCode.getMessage());
        this.code = responseCode.getCode();
        this.message = responseCode.getMessage();
        this.details = details;
    }

    /**
     * 使用ResponseCode创建异常
     *
     * @param responseCode 响应码枚举
     * @param details      详细错误信息
     * @param cause        原因异常
     */
    public BaseException(ResponseCode responseCode, String details, Throwable cause) {
        super(responseCode.getMessage(), cause);
        this.code = responseCode.getCode();
        this.message = responseCode.getMessage();
        this.details = details;
    }

    @Override
    public String toString() {
        return "BaseException{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}
