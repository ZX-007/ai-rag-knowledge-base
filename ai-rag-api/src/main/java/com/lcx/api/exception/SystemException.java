package com.lcx.api.exception;

import com.lcx.api.response.ResponseCode;

/**
 * 系统异常类
 * <p>
 * 用于处理系统级别的异常情况，如数据库连接失败、外部服务不可用等。
 * 继承自BaseException，提供系统异常的统一处理。
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
public class SystemException extends BaseException {

    /**
     * 构造函数
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public SystemException(String code, String message) {
        super(code, message);
    }

    /**
     * 构造函数
     *
     * @param code    错误码
     * @param message 错误消息
     * @param cause   原因异常
     */
    public SystemException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    /**
     * 构造函数
     *
     * @param code    错误码
     * @param message 错误消息
     * @param details 详细错误信息
     */
    public SystemException(String code, String message, String details) {
        super(code, message, details);
    }

    /**
     * 构造函数
     *
     * @param code    错误码
     * @param message 错误消息
     * @param details 详细错误信息
     * @param cause   原因异常
     */
    public SystemException(String code, String message, String details, Throwable cause) {
        super(code, message, details, cause);
    }

    /**
     * 使用ResponseCode创建系统异常
     *
     * @param responseCode 响应码枚举
     */
    public SystemException(ResponseCode responseCode) {
        super(responseCode);
    }

    /**
     * 使用ResponseCode创建系统异常
     *
     * @param responseCode 响应码枚举
     * @param cause        原因异常
     */
    public SystemException(ResponseCode responseCode, Throwable cause) {
        super(responseCode, cause);
    }

    /**
     * 使用ResponseCode创建系统异常
     *
     * @param responseCode 响应码枚举
     * @param details      详细错误信息
     */
    public SystemException(ResponseCode responseCode, String details) {
        super(responseCode, details);
    }

    /**
     * 使用ResponseCode创建系统异常
     *
     * @param responseCode 响应码枚举
     * @param details      详细错误信息
     * @param cause        原因异常
     */
    public SystemException(ResponseCode responseCode, String details, Throwable cause) {
        super(responseCode, details, cause);
    }

    /**
     * 创建数据库异常
     *
     * @param message 错误消息
     * @return 系统异常
     */
    public static SystemException databaseError(String message) {
        return new SystemException(ResponseCode.DATABASE_ERROR, message);
    }

    /**
     * 创建数据库异常
     *
     * @param message 错误消息
     * @param cause   原因异常
     * @return 系统异常
     */
    public static SystemException databaseError(String message, Throwable cause) {
        return new SystemException(ResponseCode.DATABASE_ERROR, message, cause);
    }

    /**
     * 创建Redis异常
     *
     * @param message 错误消息
     * @return 系统异常
     */
    public static SystemException redisError(String message) {
        return new SystemException(ResponseCode.REDIS_ERROR, message);
    }

    /**
     * 创建Redis异常
     *
     * @param message 错误消息
     * @param cause   原因异常
     * @return 系统异常
     */
    public static SystemException redisError(String message, Throwable cause) {
        return new SystemException(ResponseCode.REDIS_ERROR, message, cause);
    }

    /**
     * 创建AI服务异常
     *
     * @param message 错误消息
     * @return 系统异常
     */
    public static SystemException aiServiceError(String message) {
        return new SystemException(ResponseCode.AI_SERVICE_ERROR, message);
    }

    /**
     * 创建AI服务异常
     *
     * @param message 错误消息
     * @param cause   原因异常
     * @return 系统异常
     */
    public static SystemException aiServiceError(String message, Throwable cause) {
        return new SystemException(ResponseCode.AI_SERVICE_ERROR, message, cause);
    }

    /**
     * 创建向量数据库异常
     *
     * @param message 错误消息
     * @return 系统异常
     */
    public static SystemException vectorDbError(String message) {
        return new SystemException(ResponseCode.VECTOR_DB_ERROR, message);
    }

    /**
     * 创建向量数据库异常
     *
     * @param message 错误消息
     * @param cause   原因异常
     * @return 系统异常
     */
    public static SystemException vectorDbError(String message, Throwable cause) {
        return new SystemException(ResponseCode.VECTOR_DB_ERROR, message, cause);
    }

    /**
     * 创建文件上传异常
     *
     * @param message 错误消息
     * @return 系统异常
     */
    public static SystemException fileUploadError(String message) {
        return new SystemException(ResponseCode.FILE_UPLOAD_ERROR, message);
    }

    /**
     * 创建文件上传异常
     *
     * @param message 错误消息
     * @param cause   原因异常
     * @return 系统异常
     */
    public static SystemException fileUploadError(String message, Throwable cause) {
        return new SystemException(ResponseCode.FILE_UPLOAD_ERROR, message, cause);
    }
}
