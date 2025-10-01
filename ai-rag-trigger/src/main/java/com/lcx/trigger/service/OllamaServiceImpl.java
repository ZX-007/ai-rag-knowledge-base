package com.lcx.trigger.service;


import com.lcx.api.IAiService;
import com.lcx.api.exception.SystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
 * @see com.lcx.api.IAiService AI 服务接口
 * @see org.springframework.ai.ollama.OllamaChatClient Ollama 聊天客户端
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OllamaServiceImpl implements IAiService {

    private final OllamaChatClient chatClient;
    private final PgVectorStore pgVectorStore;

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

    @Override
    public Flux<ChatResponse> generateStreamRag(String model, String ragTag, String message) {
        String SYSTEM_PROMPT = """
                Use the information from the DOCUMENTS section to provide accurate answers but act as if you knew this information innately.
                If unsure, simply state that you don't know.
                Another thing you need to note is that your reply must be in Chinese!
                DOCUMENTS:
                    {documents}
                """;

        // 创建搜索请求：指定文档
        SearchRequest request = SearchRequest.query(message)
                .withTopK(5)
                .withFilterExpression("knowledge == '" + ragTag + "'");

        List<Document> documents = pgVectorStore.similaritySearch(request);
        String documentCollectors = documents.stream()
                .map(Document::getContent)
                .collect(Collectors.joining());
        Message ragMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(Map.of("documents", documentCollectors));

        return chatClient.stream(new Prompt(
                List.of(new UserMessage(message),ragMessage),
                OllamaOptions.create().withModel(model)
        ));
    }
}
