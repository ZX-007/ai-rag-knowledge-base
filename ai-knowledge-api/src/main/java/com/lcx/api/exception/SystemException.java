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
     * 创建带业务上下文的系统异常
     *
     * @param responseCode 响应码
     * @param businessContext 业务上下文描述
     * @return 系统异常
     */
    public static SystemException withContext(ResponseCode responseCode, String businessContext) {
        String message = String.format("%s - %s", responseCode.getMessage(), businessContext);
        return new SystemException(responseCode.getCode(), message);
    }

    /**
     * 创建带业务上下文的系统异常
     *
     * @param responseCode 响应码
     * @param businessContext 业务上下文描述
     * @param cause 原因异常
     * @return 系统异常
     */
    public static SystemException withContext(ResponseCode responseCode, String businessContext, Throwable cause) {
        String message = String.format("%s - %s", responseCode.getMessage(), businessContext);
        return new SystemException(responseCode.getCode(), message, cause);
    }

    /**
     * 创建带详细信息的数据库异常
     *
     * @param operation 操作描述
     * @param tableName 表名
     * @param cause 原因异常
     * @return 系统异常
     */
    public static SystemException databaseError(String operation, String tableName, Throwable cause) {
        String context = String.format("执行数据库操作失败: %s 表 %s", operation, tableName);
        return withContext(ResponseCode.DATABASE_ERROR, context, cause);
    }

    /**
     * 创建带详细信息的Redis异常
     *
     * @param operation 操作描述
     * @param key Redis键
     * @param cause 原因异常
     * @return 系统异常
     */
    public static SystemException redisError(String operation, String key, Throwable cause) {
        String context = String.format("执行Redis操作失败: %s 键 %s", operation, key);
        return withContext(ResponseCode.REDIS_ERROR, context, cause);
    }

    /**
     * 创建带详细信息的AI服务异常
     *
     * @param operation 操作描述
     * @param model 模型名称
     * @param cause 原因异常
     * @return 系统异常
     */
    public static SystemException aiServiceError(String operation, String model, Throwable cause) {
        String context = String.format("AI服务调用失败: %s 模型 %s", operation, model);
        return withContext(ResponseCode.AI_SERVICE_ERROR, context, cause);
    }

    /**
     * 创建带详细信息的文件处理异常
     *
     * @param operation 操作描述
     * @param fileName 文件名
     * @param cause 原因异常
     * @return 系统异常
     */
    public static SystemException fileProcessError(String operation, String fileName, Throwable cause) {
        String context = String.format("文件处理失败: %s 文件 %s", operation, fileName);
        return withContext(ResponseCode.INTERNAL_ERROR, context, cause);
    }

    /**
     * 创建网络错误异常
     *
     * @param operation 操作描述
     * @param url 请求URL
     * @param cause 原因异常
     * @return 系统异常
     */
    public static SystemException networkError(String operation, String url, Throwable cause) {
        String context = String.format("网络请求失败: %s URL %s", operation, url);
        return withContext(ResponseCode.NETWORK_ERROR, context, cause);
    }

    /**
     * 创建文件解析错误异常
     *
     * @param operation 操作描述
     * @param fileName 文件名
     * @param cause 原因异常
     * @return 系统异常
     */
    public static SystemException fileParseError(String operation, String fileName, Throwable cause) {
        String context = String.format("文件解析失败: %s 文件 %s", operation, fileName);
        return withContext(ResponseCode.FILE_PARSE_ERROR, context, cause);
    }

    /**
     * 创建模型加载错误异常
     *
     * @param modelName 模型名称
     * @param cause 原因异常
     * @return 系统异常
     */
    public static SystemException modelLoadError(String modelName, Throwable cause) {
        String context = String.format("模型加载失败: %s", modelName);
        return withContext(ResponseCode.MODEL_LOAD_ERROR, context, cause);
    }

    /**
     * 创建配置错误异常
     *
     * @param configKey 配置键
     * @param cause 原因异常
     * @return 系统异常
     */
    public static SystemException configError(String configKey, Throwable cause) {
        String context = String.format("配置错误: %s", configKey);
        return withContext(ResponseCode.CONFIG_ERROR, context, cause);
    }

    /**
     * 创建资源不足异常
     *
     * @param resourceType 资源类型
     * @param cause 原因异常
     * @return 系统异常
     */
    public static SystemException resourceExhausted(String resourceType, Throwable cause) {
        String context = String.format("系统资源不足: %s", resourceType);
        return withContext(ResponseCode.RESOURCE_EXHAUSTED, context, cause);
    }

    /**
     * 创建外部服务调用错误异常
     *
     * @param serviceName 服务名称
     * @param operation 操作描述
     * @param cause 原因异常
     * @return 系统异常
     */
    public static SystemException externalServiceError(String serviceName, String operation, Throwable cause) {
        String context = String.format("外部服务调用失败: %s 操作 %s", serviceName, operation);
        return withContext(ResponseCode.EXTERNAL_SERVICE_ERROR, context, cause);
    }
}
