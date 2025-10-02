package com.lcx.api;

import org.springframework.ai.chat.ChatResponse;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI 服务通用接口
 * <p>
 * 定义了 AI 服务的标准接口，包括同步和异步生成功能。
 * 该接口可以被不同的 AI 服务实现（如 Ollama、OpenAI 等）。
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
public interface IAiService {

    /**
     * 查询可用的 AI 模型列表
     * <p>
     * 获取当前 AI 服务支持的所有可用模型列表。
     * 该方法会查询 AI 服务的可用模型，返回模型名称列表。
     * </p>
     * <p>
     * 使用场景：
     * <ul>
     *   <li>前端模型选择器动态加载可用模型</li>
     *   <li>模型管理界面显示所有可用模型</li>
     *   <li>模型状态检查和验证</li>
     * </ul>
     * </p>
     *
     * @return 可用的 AI 模型名称列表
     * @throws RuntimeException 当查询模型列表失败时抛出
     */
    List<String> queryAvailableModels();

    /**
     * 生成 AI 回复
     * <p>
     * 使用指定的模型和消息生成 AI 回复，返回完整的响应结果。
     * 这是一个同步方法，会等待 AI 服务完成处理后再返回结果。
     * </p>
     *
     * @param model   使用的 AI 模型名称，如 "llama2", "gpt-3.5-turbo" 等
     * @param message 用户输入的消息内容
     * @return AI 生成的完整回复，包含回复内容、元数据等信息
     * @throws IllegalArgumentException 当模型名称或消息为空时抛出
     * @throws RuntimeException 当 AI 服务调用失败时抛出
     */
    ChatResponse generate(String model, String message);

    /**
     * 流式生成 AI 回复
     * <p>
     * 使用指定的模型和消息流式生成 AI 回复，返回响应流。
     * 这是一个异步方法，可以实时获取 AI 生成的内容，适用于长文本生成场景。
     * </p>
     *
     * @param model   使用的 AI 模型名称，如 "llama2", "gpt-3.5-turbo" 等
     * @param message 用户输入的消息内容
     * @return AI 生成的回复流，可以实时订阅获取生成的内容片段
     * @throws IllegalArgumentException 当模型名称或消息为空时抛出
     * @throws RuntimeException 当 AI 服务调用失败时抛出
     */
    Flux<ChatResponse> generateStream(String model, String message);

    /**
     * 流式生成 RAG AI 回复
     * <p>
     * 基于检索增强生成（RAG）技术的流式 AI 回复生成。该方法会先从向量数据库中检索相关文档，
     * 然后将检索到的文档作为上下文信息与用户消息一起发送给 AI 模型，生成更准确和相关的回复。
     * 这是一个异步方法，可以实时获取 AI 生成的内容，适用于长文本生成场景。
     * </p>
     * <p>
     * RAG 工作流程：
     * <ol>
     *   <li>根据 RAG 标签从向量数据库中检索相关文档</li>
     *   <li>将检索到的文档作为系统提示词</li>
     *   <li>结合用户消息发送给 AI 模型</li>
     *   <li>流式返回 AI 生成的回复</li>
     * </ol>
     * </p>
     *
     * @param model   使用的 AI 模型名称，如 "llama2", "gpt-3.5-turbo" 等
     * @param ragTag  RAG 标签，用于从向量数据库中检索相关文档
     * @param message 用户输入的消息内容
     * @return AI 生成的回复流，可以实时订阅获取生成的内容片段
     * @throws IllegalArgumentException 当模型名称、RAG 标签或消息为空时抛出
     * @throws RuntimeException 当 AI 服务调用失败时抛出
     */
    Flux<ChatResponse> generateStreamRag(String model, String ragTag, String message);

}
