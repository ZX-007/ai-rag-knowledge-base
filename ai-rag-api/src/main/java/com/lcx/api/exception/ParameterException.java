package com.lcx.api.exception;

import com.lcx.api.response.ResponseCode;

/**
 * 参数异常类
 * <p>
 * 用于处理参数验证相关的异常情况。
 * 继承自BaseException，专门处理参数错误。
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
public class ParameterException extends BaseException {

    /**
     * 构造函数
     *
     * @param message 错误消息
     */
    public ParameterException(String message) {
        super(ResponseCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     * @param cause   原因异常
     */
    public ParameterException(String message, Throwable cause) {
        super(ResponseCode.PARAM_ERROR.getCode(), message, cause);
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     * @param details 详细错误信息
     */
    public ParameterException(String message, String details) {
        super(ResponseCode.PARAM_ERROR.getCode(), message, details);
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     * @param details 详细错误信息
     * @param cause   原因异常
     */
    public ParameterException(String message, String details, Throwable cause) {
        super(ResponseCode.PARAM_ERROR.getCode(), message, details, cause);
    }

    /**
     * 创建参数为空的异常
     *
     * @param paramName 参数名称
     * @return 参数异常
     */
    public static ParameterException paramNull(String paramName) {
        return new ParameterException("参数 " + paramName + " 不能为空");
    }

    /**
     * 创建参数格式错误的异常
     *
     * @param paramName 参数名称
     * @param format    期望格式
     * @return 参数异常
     */
    public static ParameterException paramFormatError(String paramName, String format) {
        return new ParameterException("参数 " + paramName + " 格式错误，期望格式：" + format);
    }

    /**
     * 创建参数值超出范围的异常
     *
     * @param paramName 参数名称
     * @param min       最小值
     * @param max       最大值
     * @return 参数异常
     */
    public static ParameterException paramOutOfRange(String paramName, Object min, Object max) {
        return new ParameterException("参数 " + paramName + " 超出范围，应在 " + min + " 到 " + max + " 之间");
    }
}
