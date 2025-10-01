package com.lcx.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 对话请求数据传输对象
 * <p>
 * 用于接收和验证 AI 对话请求的参数。该 DTO 包含了进行 AI 对话所需的基本信息，
 * 包括模型名称和用户消息内容。所有字段都经过严格的参数验证，确保数据的完整性和有效性。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>参数验证 - 确保模型名称和消息内容符合要求</li>
 *   <li>数据传输 - 在客户端和服务端之间传递 AI 对话请求</li>
 *   <li>类型安全 - 提供强类型的数据结构</li>
 * </ul>
 * </p>
 * <p>
 * 验证规则：
 * <ul>
 *   <li>模型名称：不能为空，长度 1-64 个字符</li>
 *   <li>消息内容：不能为空，长度 1-3200 个字符</li>
 * </ul>
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 * @see jakarta.validation.constraints.NotBlank 非空验证注解
 * @see jakarta.validation.constraints.Size 长度验证注解
 */
@Data
public class ChatRequest {

    /**
     * AI 模型名称
     * <p>
     * 指定要使用的 AI 模型，如 "llama2", "gpt-3.5-turbo", "claude-3" 等。
     * 不同的模型具有不同的能力和特点，选择合适的模型可以获得更好的效果。
     * </p>
     * <p>
     * 验证规则：
     * <ul>
     *   <li>不能为空或空白字符串</li>
     *   <li>长度必须在 1-64 个字符之间</li>
     * </ul>
     * </p>
     */
    @NotBlank(message = "模型名称不能为空")
    @Size(min = 1, max = 64, message = "模型名称长度必须在1-64个字符之间")
    private String model;

    /**
     * 用户消息内容
     * <p>
     * 用户输入的问题、指令或对话内容。这是发送给 AI 模型的主要输入，
     * AI 模型将基于此消息生成相应的回复。
     * </p>
     * <p>
     * 验证规则：
     * <ul>
     *   <li>不能为空或空白字符串</li>
     *   <li>长度必须在 1-3200 个字符之间</li>
     * </ul>
     * </p>
     * <p>
     * 使用建议：
     * <ul>
     *   <li>提供清晰、具体的问题描述</li>
     *   <li>包含必要的上下文信息</li>
     *   <li>避免过于冗长的描述</li>
     * </ul>
     * </p>
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(min = 1, max = 3200, message = "消息内容长度必须在1-3200个字符之间")
    private String message;
}
