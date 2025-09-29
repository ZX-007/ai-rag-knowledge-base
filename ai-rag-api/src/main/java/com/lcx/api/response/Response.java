package com.lcx.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应结果封装类
 * <p>
 * 用于封装 API 接口的响应结果，提供统一的响应格式。
 * 包含状态码、消息信息和具体数据内容。
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
     * 响应状态码
     * <p>
     * 用于表示请求的处理结果状态，如 "200" 表示成功，"500" 表示服务器错误等。
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

}
