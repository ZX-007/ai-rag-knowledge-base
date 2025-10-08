package com.lcx.trigger.service;

import com.lcx.api.IAiService;
import com.lcx.api.exception.SystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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
 * @since 1.1
 * @see com.lcx.api.IAiService AI 服务接口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OllamaServiceImpl implements IAiService {

    private final OllamaChatModel chatModel;
    private final PgVectorStore pgVectorStore;
    private final RestTemplate restTemplate;

    @Override
    public List<String> queryAvailableModels() {
        // todo 写死对话模型
        return List.of("deepseek-r1:1.5b");
//        log.debug("开始查询可用的 AI 模型列表");
//
//        try {
//            // 直接调用 Ollama 的 REST API 获取模型列表
//            String url = "http://localhost:11434/api/tags";
//            @SuppressWarnings("unchecked")
//            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
//
//            if (response != null && response.containsKey("models")) {
//                @SuppressWarnings("unchecked")
//                List<Map<String, Object>> models = (List<Map<String, Object>>) response.get("models");
//                List<String> modelNames = models.stream()
//                        .map(model -> (String) model.get("name"))
//                        .collect(Collectors.toList());
//
//                log.debug("查询到 {} 个可用模型: {}", modelNames.size(), modelNames);
//                return modelNames;
//            } else {
//                log.warn("Ollama API 响应格式异常，返回空模型列表");
//                return List.of();
//            }
//        } catch (Exception e) {
//            log.error("查询可用模型列表失败: {}", e.getMessage(), e);
//            throw SystemException.aiServiceError("查询可用模型列表失败: " + e.getMessage(), e);
//        }
    }

    @Override
    @Deprecated(since = "1.1", forRemoval = true)
    public ChatResponse generate(String model, String message) {
        log.debug("开始生成 AI 回复，模型: {}, 消息长度: {}", model, message.length());

        try {
            ChatResponse response = chatModel.call(new Prompt(
                    message,
                    OllamaOptions.builder().model(model).build()
            ));

            log.debug("AI 回复生成完成，模型: {}, 响应长度: {}",
                    model, response.getResult().getOutput().getText().length());
            return response;
        } catch (Exception e) {
            // 记录AI服务调用失败的业务上下文，包含更多调试信息
            log.error("AI 回复生成失败: 模型={}, 消息长度={}, 操作=同步生成, 错误详情={}",
                    model, message.length(), e.getMessage(), e);
            // 重新抛出时添加有意义的业务上下文
            throw SystemException.aiServiceError("同步生成回复", model, e);
        }
    }

    @Override
    public Flux<ChatResponse> generateStream(String model, String message) {
        log.debug("开始流式生成 AI 回复，模型: {}, 消息长度: {}", model, message.length());

        try {
            Flux<ChatResponse> responseStream = chatModel.stream(new Prompt(
                    message,
                    OllamaOptions.builder().model(model).build()
            ));

            log.debug("AI 回复流创建成功，模型: {}", model);
            return responseStream
                    .doOnNext(response -> log.debug("收到 AI 回复片段，模型: {}", model))
                    .doOnError(error -> {
                        log.error("AI 回复流生成失败，模型: {}, 错误: {}", model, error.getMessage(), error);
                        throw SystemException.aiServiceError("生成回复流", model, error);
                    })
                    .doOnComplete(() -> log.debug("AI 回复流生成完成，模型: {}", model));
        } catch (Exception e) {
            // 记录AI服务流式调用失败的业务上下文，包含更多调试信息
            log.error("AI 回复流创建失败: 模型={}, 消息长度={}, 操作=流式生成, 错误详情={}",
                    model, message.length(), e.getMessage(), e);
            // 重新抛出时添加有意义的业务上下文
            throw SystemException.aiServiceError("创建流式生成", model, e);
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
        SearchRequest request = SearchRequest.builder()
                .query(message)
                .topK(5)
                .filterExpression("knowledge == '" + ragTag + "'")
                .build();

        List<Document> documents = pgVectorStore.similaritySearch(request);
        String documentCollectors = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining());
        Message ragMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(Map.of("documents", documentCollectors));

        return chatModel.stream(new Prompt(
                List.of(new UserMessage(message), ragMessage),
                OllamaOptions.builder().model(model).build()
        ));
    }

}
