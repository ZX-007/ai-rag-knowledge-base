package com.lcx.api.response;

import lombok.Getter;

/**
 * 响应状态码枚举
 * <p>
 * 定义系统中使用的各种响应状态码，便于统一管理和维护。
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
@Getter
public enum ResponseCode {

    /**
     * 成功
     */
    SUCCESS("2000", "操作成功"),

    /**
     * 参数错误
     */
    PARAM_ERROR("4000", "参数错误"),

    /**
     * 未授权
     */
    UNAUTHORIZED("4001", "未授权"),

    /**
     * 禁止访问
     */
    FORBIDDEN("4003", "禁止访问"),

    /**
     * 资源未找到
     */
    NOT_FOUND("4004", "资源未找到"),

    /**
     * 请求方法不支持
     */
    METHOD_NOT_ALLOWED("4005", "请求方法不支持"),

    /**
     * 请求超时
     */
    REQUEST_TIMEOUT("4008", "请求超时"),

    /**
     * 请求过于频繁
     */
    TOO_MANY_REQUESTS("4029", "请求过于频繁"),

    /**
     * 服务器内部错误
     */
    INTERNAL_ERROR("5000", "服务器内部错误"),

    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE("5003", "服务不可用"),

    /**
     * 网关超时
     */
    GATEWAY_TIMEOUT("5004", "网关超时"),

    /**
     * 数据库连接错误
     */
    DATABASE_ERROR("5001", "数据库连接错误"),

    /**
     * 文件上传错误
     */
    FILE_UPLOAD_ERROR("5002", "文件上传错误"),

    /**
     * AI服务错误
     */
    AI_SERVICE_ERROR("5005", "AI服务错误"),

    /**
     * Redis连接错误
     */
    REDIS_ERROR("5006", "Redis连接错误"),

    /**
     * 向量数据库错误
     */
    VECTOR_DB_ERROR("5007", "向量数据库错误");

    /**
     * 状态码
     * -- GETTER --
     *  获取状态码
     *
     * @return 状态码

     */
    private final String code;

    /**
     * 状态消息
     * -- GETTER --
     *  获取状态消息
     *
     * @return 状态消息

     */
    private final String message;

    /**
     * 构造函数
     *
     * @param code    状态码
     * @param message 状态消息
     */
    ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 对应的枚举，如果未找到返回null
     */
    public static ResponseCode fromCode(String code) {
        for (ResponseCode responseCode : values()) {
            if (responseCode.getCode().equals(code)) {
                return responseCode;
            }
        }
        return null;
    }

    /**
     * 判断是否为成功状态码
     *
     * @param code 状态码
     * @return true 如果是成功状态码，false 否则
     */
    public static boolean isSuccess(String code) {
        return SUCCESS.getCode().equals(code);
    }

    /**
     * 判断是否为失败状态码
     *
     * @param code 状态码
     * @return true 如果是失败状态码，false 否则
     */
    public static boolean isFailure(String code) {
        return !isSuccess(code);
    }

    @Override
    public String toString() {
        return "ResponseCode{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
