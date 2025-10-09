package com.lcx.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * RAG 对话请求数据传输对象
 * <p>
 * 继承自 ChatRequest，增加了 RAG（检索增强生成）功能所需的标签字段。
 * 该 DTO 用于基于知识库的 AI 对话请求，通过 RAG 标签从向量数据库中检索相关文档，
 * 然后将检索到的文档作为上下文信息与用户消息一起发送给 AI 模型。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>继承基础对话功能 - 包含模型名称和用户消息</li>
 *   <li>RAG 增强功能 - 通过标签检索相关文档</li>
 *   <li>参数验证 - 确保所有字段符合要求</li>
 * </ul>
 * </p>
 * <p>
 * RAG 工作流程：
 * <ol>
 *   <li>根据 RAG 标签从向量数据库中检索相关文档</li>
 *   <li>将检索到的文档作为系统提示词</li>
 *   <li>结合用户消息发送给 AI 模型</li>
 *   <li>生成基于知识库的准确回复</li>
 * </ol>
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 * @see ChatRequest 基础对话请求对象
 * @see jakarta.validation.constraints.NotBlank 非空验证注解
 * @see jakarta.validation.constraints.Size 长度验证注解
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RagChatRequest extends ChatRequest {

    /**
     * RAG 标签
     * <p>
     * 用于标识知识库中的文档分类或主题。系统会根据此标签从向量数据库中检索相关文档，
     * 然后将这些文档作为上下文信息提供给 AI 模型，以生成更准确和相关的回复。
     * </p>
     * <p>
     * 验证规则：
     * <ul>
     *   <li>不能为空或空白字符串</li>
     *   <li>长度必须在 1-32 个字符之间</li>
     * </ul>
     * </p>
     * <p>
     * 使用建议：
     * <ul>
     *   <li>使用有意义的标签名称，如 "技术文档", "产品手册", "FAQ" 等</li>
     *   <li>保持标签的一致性和规范性</li>
     *   <li>避免使用特殊字符和空格</li>
     * </ul>
     * </p>
     */
    @NotBlank(message = "标签不能为空")
    @Size(min = 1, max = 32, message = "标签名称长度必须在1-32个字符之间")
    private String ragTag;

}
