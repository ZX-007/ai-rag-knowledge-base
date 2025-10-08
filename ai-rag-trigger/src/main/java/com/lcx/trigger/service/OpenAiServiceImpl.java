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
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiServiceImpl implements IAiService {

    private final OpenAiChatModel chatModel;
    private final PgVectorStore pgVectorStore;

    @Override
    public List<String> queryAvailableModels() {
        // todo 写死对话模型
        return List.of("deepseek-r1-distill-qwen-1.5b");
    }

    @Override
    @Deprecated(since = "1.1", forRemoval = true)
    public ChatResponse generate(String model, String message) {
        log.debug("开始生成 AI 回复，模型: {}, 消息长度: {}", model, message.length());

        try {
            ChatResponse response = chatModel.call(new Prompt(
                    message,
                    OpenAiChatOptions.builder().model(model).build()
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
                    OpenAiChatOptions.builder().model(model).build()
            ));

            log.info("AI 回复流创建成功，模型: {}", model);
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
        log.debug("开始 RAG 流式生成 AI 回复，模型: {}, RAG标签: {}, 消息长度: {}", model, ragTag, message.length());

        try {
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
            log.debug("RAG 文档检索完成，找到 {} 个相关文档", documents.size());

            String documentCollectors = documents.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n\n"));

            // 检查是否找到相关文档
            if (documentCollectors.trim().isEmpty()) {
                log.warn("RAG 检索未找到相关文档，RAG标签: {}, 查询: {}", ragTag, message);
                documentCollectors = "未找到相关文档信息。";
            }

            Message ragMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(Map.of("documents", documentCollectors));

            Flux<ChatResponse> responseStream = chatModel.stream(new Prompt(
                    List.of(ragMessage, new UserMessage(message)),
                    OpenAiChatOptions.builder().model(model).build()
            ));

            log.debug("RAG AI 回复流创建成功，模型: {}", model);
            return responseStream
                    .doOnNext(response -> log.debug("收到 RAG AI 回复片段，模型: {}", model))
                    .doOnError(error -> {
                        log.error("RAG AI 回复流生成失败，模型: {}, RAG标签: {}, 错误: {}", model, ragTag, error.getMessage(), error);
                        throw SystemException.aiServiceError("生成 RAG 回复流", model, error);
                    })
                    .doOnComplete(() -> log.debug("RAG AI 回复流生成完成，模型: {}", model));
        } catch (Exception e) {
            // 记录RAG服务流式调用失败的业务上下文，包含更多调试信息
            log.error("RAG AI 回复流创建失败: 模型={}, RAG标签={}, 消息长度={}, 操作=RAG流式生成, 错误详情={}",
                    model, ragTag, message.length(), e.getMessage(), e);
            // 重新抛出时添加有意义的业务上下文
            throw SystemException.aiServiceError("创建 RAG 流式生成", model, e);
        }
    }

}
