package com.lcx.api.exception;

import com.lcx.api.response.ResponseCode;

/**
 * 业务异常类
 * <p>
 * 用于处理业务逻辑相关的异常情况。
 * 继承自BaseException，提供业务异常的统一处理。
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
public class BusinessException extends BaseException {

    /**
     * 构造函数
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BusinessException(String code, String message) {
        super(code, message);
    }

    /**
     * 构造函数
     *
     * @param code    错误码
     * @param message 错误消息
     * @param cause   原因异常
     */
    public BusinessException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    /**
     * 使用ResponseCode创建业务异常
     *
     * @param responseCode 响应码枚举
     */
    public BusinessException(ResponseCode responseCode) {
        super(responseCode);
    }

    /**
     * 使用ResponseCode创建业务异常
     *
     * @param responseCode 响应码枚举
     * @param cause        原因异常
     */
    public BusinessException(ResponseCode responseCode, Throwable cause) {
        super(responseCode, cause);
    }

}
