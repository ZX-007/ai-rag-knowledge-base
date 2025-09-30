package com.lcx.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI对话请求DTO
 * <p>
 * 用于接收AI对话请求的参数校验
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
@Data
public class ChatRequest {

    /**
     * AI模型名称
     */
    @NotBlank(message = "模型名称不能为空")
    @Size(min = 1, max = 100, message = "模型名称长度必须在1-100个字符之间")
    private String model;

    /**
     * 用户消息
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(min = 1, max = 4000, message = "消息内容长度必须在1-4000个字符之间")
    private String message;
}
