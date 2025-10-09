package com.lcx.api.response;

import java.util.List;
import java.util.Map;

/**
 * Response工具类
 * <p>
 * 提供Response对象的便利创建方法，简化API响应构建。
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
public class ResponseUtils {

    /**
     * 私有构造函数，防止实例化
     */
    private ResponseUtils() {
        throw new UnsupportedOperationException("工具类不能被实例化");
    }

    /**
     * 创建成功响应
     *
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> Response<T> ok() {
        return Response.success();
    }

    /**
     * 创建成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> Response<T> ok(T data) {
        return Response.success(data);
    }

    /**
     * 创建成功响应（带消息和数据）
     *
     * @param message 响应消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return 成功响应
     */
    public static <T> Response<T> ok(String message, T data) {
        return Response.success(message, data);
    }

    /**
     * 创建失败响应
     *
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> Response<T> error() {
        return Response.failure();
    }

    /**
     * 创建失败响应（带消息）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> Response<T> error(String message) {
        return Response.failure(message);
    }

    /**
     * 创建失败响应（带错误码和消息）
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> Response<T> error(String code, String message) {
        return Response.failure(code, message);
    }

    /**
     * 创建参数错误响应
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 参数错误响应
     */
    public static <T> Response<T> badRequest(String message) {
        return Response.paramError(message);
    }

    /**
     * 创建未授权响应
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 未授权响应
     */
    public static <T> Response<T> unauthorized(String message) {
        return Response.of(ResponseCode.UNAUTHORIZED.getCode(), message);
    }

    /**
     * 创建禁止访问响应
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 禁止访问响应
     */
    public static <T> Response<T> forbidden(String message) {
        return Response.of(ResponseCode.FORBIDDEN.getCode(), message);
    }

    /**
     * 创建资源未找到响应
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 资源未找到响应
     */
    public static <T> Response<T> notFound(String message) {
        return Response.of(ResponseCode.NOT_FOUND.getCode(), message);
    }

    /**
     * 创建服务器内部错误响应
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 服务器内部错误响应
     */
    public static <T> Response<T> internalError(String message) {
        return Response.of(ResponseCode.INTERNAL_ERROR.getCode(), message);
    }

    /**
     * 创建服务不可用响应
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 服务不可用响应
     */
    public static <T> Response<T> serviceUnavailable(String message) {
        return Response.of(ResponseCode.SERVICE_UNAVAILABLE.getCode(), message);
    }

    /**
     * 创建数据库错误响应
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 数据库错误响应
     */
    public static <T> Response<T> databaseError(String message) {
        return Response.of(ResponseCode.DATABASE_ERROR.getCode(), message);
    }

    /**
     * 创建文件上传错误响应
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 文件上传错误响应
     */
    public static <T> Response<T> fileUploadError(String message) {
        return Response.of(ResponseCode.FILE_UPLOAD_ERROR.getCode(), message);
    }

    /**
     * 创建AI服务错误响应
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return AI服务错误响应
     */
    public static <T> Response<T> aiServiceError(String message) {
        return Response.of(ResponseCode.AI_SERVICE_ERROR.getCode(), message);
    }

    /**
     * 创建Redis错误响应
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return Redis错误响应
     */
    public static <T> Response<T> redisError(String message) {
        return Response.of(ResponseCode.REDIS_ERROR.getCode(), message);
    }

    /**
     * 创建向量数据库错误响应
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 向量数据库错误响应
     */
    public static <T> Response<T> vectorDbError(String message) {
        return Response.of(ResponseCode.VECTOR_DB_ERROR.getCode(), message);
    }

    /**
     * 创建分页响应
     *
     * @param data     数据列表
     * @param total    总数量
     * @param page     当前页码
     * @param pageSize 每页大小
     * @param <T>      数据类型
     * @return 分页响应
     */
    public static <T> Response<PageResult<T>> page(List<T> data, long total, int page, int pageSize) {
        PageResult<T> pageResult = PageResult.<T>builder()
                .data(data)
                .total(total)
                .page(page)
                .pageSize(pageSize)
                .totalPages((int) Math.ceil((double) total / pageSize))
                .build();
        return Response.success("查询成功", pageResult);
    }

    /**
     * 创建分页响应（带消息）
     *
     * @param message  响应消息
     * @param data     数据列表
     * @param total    总数量
     * @param page     当前页码
     * @param pageSize 每页大小
     * @param <T>      数据类型
     * @return 分页响应
     */
    public static <T> Response<PageResult<T>> page(String message, List<T> data, long total, int page, int pageSize) {
        PageResult<T> pageResult = PageResult.<T>builder()
                .data(data)
                .total(total)
                .page(page)
                .pageSize(pageSize)
                .totalPages((int) Math.ceil((double) total / pageSize))
                .build();
        return Response.success(message, pageResult);
    }

    /**
     * 创建列表响应
     *
     * @param data 数据列表
     * @param <T>  数据类型
     * @return 列表响应
     */
    public static <T> Response<List<T>> list(List<T> data) {
        return Response.success("查询成功", data);
    }

    /**
     * 创建列表响应（带消息）
     *
     * @param message 响应消息
     * @param data    数据列表
     * @param <T>     数据类型
     * @return 列表响应
     */
    public static <T> Response<List<T>> list(String message, List<T> data) {
        return Response.success(message, data);
    }

    /**
     * 创建Map响应
     *
     * @param data 数据Map
     * @param <K>  Key类型
     * @param <V>  Value类型
     * @return Map响应
     */
    public static <K, V> Response<Map<K, V>> map(Map<K, V> data) {
        return Response.success("查询成功", data);
    }

    /**
     * 创建Map响应（带消息）
     *
     * @param message 响应消息
     * @param data    数据Map
     * @param <K>      Key类型
     * @param <V>      Value类型
     * @return Map响应
     */
    public static <K, V> Response<Map<K, V>> map(String message, Map<K, V> data) {
        return Response.success(message, data);
    }

    /**
     * 创建字符串响应
     *
     * @param message 响应消息
     * @return 字符串响应
     */
    public static Response<String> message(String message) {
        return Response.success(message);
    }

    /**
     * 创建操作成功响应
     *
     * @param operation 操作名称
     * @return 操作成功响应
     */
    public static Response<String> operationSuccess(String operation) {
        return Response.success(operation + "成功");
    }

    /**
     * 创建操作失败响应
     *
     * @param operation 操作名称
     * @return 操作失败响应
     */
    public static Response<String> operationFailure(String operation) {
        return Response.failure(operation + "失败");
    }

    /**
     * 创建操作失败响应（带错误信息）
     *
     * @param operation 操作名称
     * @param errorMsg  错误信息
     * @return 操作失败响应
     */
    public static Response<String> operationFailure(String operation, String errorMsg) {
        return Response.failure(operation + "失败: " + errorMsg);
    }
}
