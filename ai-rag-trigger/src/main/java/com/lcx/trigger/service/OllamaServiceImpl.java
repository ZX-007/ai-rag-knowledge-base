package com.lcx.trigger.service;


import com.lcx.api.IOllamaService;
import com.lcx.api.exception.SystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Ollama AI 服务实现类
 * <p>
 * 实现基于 Ollama 的 AI 对话生成和流式生成功能。
 * 该类负责与 Ollama 服务进行交互，处理 AI 模型的调用和响应。
 * </p>
 * <p>
 * 使用 Spring AI 框架的 OllamaChatClient 来与 Ollama 服务通信，
 * 支持同步和异步两种调用方式。
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 * @see com.lcx.api.IOllamaService Ollama 服务接口
 * @see org.springframework.ai.ollama.OllamaChatClient Ollama 聊天客户端
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OllamaServiceImpl implements IOllamaService {

    /**
     * Ollama 聊天客户端
     * <p>
     * 用于与 Ollama 服务进行通信，执行 AI 模型的调用。
     * 通过构造函数注入，确保依赖注入的正确性。
     * </p>
     */
    private final OllamaChatClient chatClient;

    /**
     * 生成 AI 回复（同步方式）
     * <p>
     * 使用指定的模型和消息生成 AI 回复，等待完整响应后返回。
     * 适用于需要完整响应的场景，如短文本生成。
     * </p>
     *
     * @param model   使用的 AI 模型名称，如 "llama2", "codellama" 等
     * @param message 用户输入的消息内容
     * @return AI 生成的完整回复，包含回复内容、元数据等信息
     * @throws IllegalArgumentException 当模型名称或消息为空时抛出
     * @throws SystemException 当 Ollama 服务调用失败时抛出
     */
    @Override
    public ChatResponse generate(String model, String message) {
        log.debug("开始生成 AI 回复，模型: {}, 消息长度: {}", model, message.length());

        try {
            ChatResponse response = chatClient.call(new Prompt(
                    message,
                    OllamaOptions.create().withModel(model)
            ));

            log.debug("AI 回复生成完成，模型: {}, 响应长度: {}",
                    model, response.getResult().getOutput().getContent().length());
            return response;
        } catch (Exception e) {
            log.error("AI 回复生成失败，模型: {}, 错误: {}", model, e.getMessage(), e);
            throw SystemException.aiServiceError("AI 回复生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 流式生成 AI 回复（异步方式）
     * <p>
     * 使用指定的模型和消息流式生成 AI 回复，返回响应流。
     * 适用于长文本生成场景，可以实时获取生成的内容片段。
     * </p>
     *
     * @param model   使用的 AI 模型名称，如 "llama2", "codellama" 等
     * @param message 用户输入的消息内容
     * @return AI 生成的回复流，可以实时订阅获取生成的内容片段
     * @throws IllegalArgumentException 当模型名称或消息为空时抛出
     * @throws SystemException 当 Ollama 服务调用失败时抛出
     */
    @Override
    public Flux<ChatResponse> generateStream(String model, String message) {
        log.debug("开始流式生成 AI 回复，模型: {}, 消息长度: {}", model, message.length());

        try {
            Flux<ChatResponse> responseStream = chatClient.stream(new Prompt(
                    message,
                    OllamaOptions.create().withModel(model)
            ));

            log.debug("AI 回复流创建成功，模型: {}", model);
            return responseStream
                    .doOnNext(response -> log.debug("收到 AI 回复片段，模型: {}", model))
                    .doOnError(error -> log.error(
                            "AI 回复流生成失败，模型: {}, 错误: {}",
                            model, error.getMessage(), error))
                    .doOnComplete(() -> log.debug("AI 回复流生成完成，模型: {}", model));
        } catch (Exception e) {
            log.error("AI 回复流创建失败，模型: {}, 错误: {}", model, e.getMessage(), e);
            throw SystemException.aiServiceError("AI 回复流创建失败: " + e.getMessage(), e);
        }
    }
}
