package com.lcx.api.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一响应结果封装类
 * <p>
 * 用于封装 API 接口的响应结果，提供统一的响应格式。
 * 包含状态码、消息信息、具体数据内容和响应时间戳。
 * </p>
 *
 * @param <T> 响应数据的类型
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 成功状态码
     */
    public static final String SUCCESS_CODE = "2000";
    /**
     * 参数错误状态码
     */
    public static final String PARAM_ERROR_CODE = "4000";
    /**
     * 失败状态码
     */
    public static final String FAILURE_CODE = "5000";

    /**
     * 成功消息
     */
    public static final String SUCCESS_MESSAGE = "success";
    /**
     * 失败消息
     */
    public static final String FAILURE_MESSAGE = "failure";

    /**
     * 响应状态码
     * <p>
     * 用于表示请求的处理结果状态，如 "2000" 表示成功，"5000" 表示服务器错误等。
     * </p>
     */
    private String code;
    /**
     * 响应消息信息
     * <p>
     * 用于描述请求处理结果的详细信息，如成功消息或错误描述。
     * </p>
     */
    private String info;
    /**
     * 响应数据内容
     * <p>
     * 包含具体的业务数据，类型由泛型 T 指定。
     * 当请求失败时，此字段可能为 null。
     * </p>
     */
    private T data;
    /**
     * 响应时间戳
     * <p>
     * 记录响应生成的时间，便于调试和日志追踪。
     * </p>
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    /**
     * 请求追踪ID
     * <p>
     * 用于请求链路追踪，便于问题定位。
     * </p>
     */
    private String traceId;

    /**
     * 判断响应是否成功
     *
     * @return true 如果响应成功，false 否则
     */
    @JsonIgnore
    public boolean isSuccess() {
        return SUCCESS_CODE.equals(this.code);
    }

    /**
     * 判断响应是否失败
     *
     * @return true 如果响应失败，false 否则
     */
    @JsonIgnore
    public boolean isFailure() {
        return !isSuccess();
    }

    /**
     * 创建成功响应
     *
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> Response<T> success() {
        return Response.<T>builder()
                .code(SUCCESS_CODE)
                .info(SUCCESS_MESSAGE)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> Response<T> success(T data) {
        return Response.<T>builder()
                .code(SUCCESS_CODE)
                .info(SUCCESS_MESSAGE)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建成功响应（带消息和数据）
     *
     * @param message 响应消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return 成功响应
     */
    public static <T> Response<T> success(String message, T data) {
        return Response.<T>builder()
                .code(SUCCESS_CODE)
                .info(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败响应
     *
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> Response<T> failure() {
        return Response.<T>builder()
                .code(FAILURE_CODE)
                .info(FAILURE_MESSAGE)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败响应（带消息）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> Response<T> failure(String message) {
        return Response.<T>builder()
                .code(FAILURE_CODE)
                .info(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败响应（带错误码和消息）
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> Response<T> failure(String code, String message) {
        return Response.<T>builder()
                .code(code)
                .info(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建参数错误响应
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 参数错误响应
     */
    public static <T> Response<T> paramError(String message) {
        return Response.<T>builder()
                .code(PARAM_ERROR_CODE)
                .info(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建自定义响应
     *
     * @param code    状态码
     * @param message 消息
     * @param data    数据
     * @param <T>     数据类型
     * @return 自定义响应
     */
    public static <T> Response<T> of(String code, String message, T data) {
        return Response.<T>builder()
                .code(code)
                .info(message)
                .data(data)
                .build();
    }

    /**
     * 创建自定义响应（无数据）
     *
     * @param code    状态码
     * @param message 消息
     * @param <T>     数据类型
     * @return 自定义响应
     */
    public static <T> Response<T> of(String code, String message) {
        return Response.<T>builder()
                .code(code)
                .info(message)
                .build();
    }

    /**
     * 设置状态码
     *
     * @param code 状态码
     * @return 当前响应对象
     */
    public Response<T> code(String code) {
        this.code = code;
        return this;
    }

    /**
     * 设置响应消息
     *
     * @param info 响应消息
     * @return 当前响应对象
     */
    public Response<T> info(String info) {
        this.info = info;
        return this;
    }

    /**
     * 设置响应数据
     *
     * @param data 响应数据
     * @return 当前响应对象
     */
    public Response<T> data(T data) {
        this.data = data;
        return this;
    }

    /**
     * 设置追踪ID
     *
     * @param traceId 追踪ID
     * @return 当前响应对象
     */
    public Response<T> traceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    @Override
    public String toString() {
        return "Response{" +
                "code='" + code + '\'' +
                ", info='" + info + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                ", traceId='" + traceId + '\'' +
                '}';
    }
}
