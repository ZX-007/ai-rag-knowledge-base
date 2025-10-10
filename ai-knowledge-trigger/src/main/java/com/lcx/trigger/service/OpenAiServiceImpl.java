package com.lcx.trigger.service;

import com.lcx.api.IAiService;
import com.lcx.api.exception.SystemException;
import com.lcx.api.logging.annotation.LogOperation;
import com.lcx.api.logging.annotation.LogPerformance;
import com.lcx.api.logging.context.LogContext;
import com.lcx.api.logging.enums.OperationTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiServiceImpl implements IAiService {

    @Value("${spring.ai.openai.chat.options.model}")
    private String defaultModel;

    private final OpenAiChatModel chatModel;
    private final PgVectorStore pgVectorStore;
    private final RedissonClient redissonClient;

    @Override
    @LogOperation(
            module = "AI_CHAT",
            operation = OperationTypeEnum.QUERY,
            description = "查询可用AI模型列表"
    )
    public List<String> queryAvailableModels() {
        log.info("BIZ_BEGIN: op=queryModels");
        try {
            RSet<String> modelSet = redissonClient.getSet("ai:models", StringCodec.INSTANCE);
            List<String> models = new ArrayList<>(modelSet);
            log.info("BIZ_END: op=queryModels, count={}", models.size());
            return models;
        } catch (Exception e) {
            log.error("BIZ_ERROR: op=queryModels, key=ai:models", e);
            throw SystemException.redisError("查询模型列表", "ai:models", e);
        }
    }

    @Override
    @Deprecated(since = "1.1", forRemoval = true)
    @LogOperation(
            module = "AI_CHAT",
            operation = OperationTypeEnum.AI_GENERATE,
            description = "AI同步生成（已废弃）",
            logParams = false
    )
    @LogPerformance(checkpointName = "AI同步生成", timeoutThreshold = 10000)
    public ChatResponse generate(String model, String message) {
        int msgLen = message != null ? message.length() : 0;
        String selectedModel = (model != null && !model.isBlank()) ? model : this.defaultModel;
        log.info("BIZ_BEGIN: op=generate, model={}, msgLen={}", selectedModel, msgLen);
        try {
            ChatResponse response = chatModel.call(new Prompt(
                    message,
                    OpenAiChatOptions.builder().model(selectedModel).build()
            ));
            log.info("BIZ_END: op=generate, model={}", selectedModel);
            return response;
        } catch (Exception e) {
            log.error("BIZ_ERROR: op=generate, model={}, msgLen={}", selectedModel, msgLen, e);
            throw SystemException.aiServiceError("同步生成回复", selectedModel, e);
        }
    }

    @Override
    @LogOperation(
            module = "AI_CHAT",
            operation = OperationTypeEnum.AI_GENERATE,
            description = "AI流式生成",
            logParams = false
    )
    public Flux<ChatResponse> generateStream(String model, String message) {
        int msgLen = message != null ? message.length() : 0;
        String selectedModel = (model != null && !model.isBlank()) ? model : this.defaultModel;

        // 保存当前MDC上下文（用于异步流回调）
        final java.util.Map<String, String> mdcContext = LogContext.getContext();
        final String traceId = LogContext.getTraceId().orElse("N/A");

        log.info("BIZ_BEGIN: op=generateStream, model={}, msgLen={}", selectedModel, msgLen);
        try {
            Flux<ChatResponse> responseStream = chatModel.stream(new Prompt(
                    message,
                    OpenAiChatOptions.builder().model(selectedModel).build()
            ));

            return responseStream
                    .doOnError(error -> {
                        // 恢复MDC上下文
                        LogContext.setContext(mdcContext);
                        try {
                            log.error("BIZ_ERROR: op=generateStream, model={}, trace={}", selectedModel, traceId, error);
                        } finally {
                            LogContext.clear();
                        }
                        throw SystemException.aiServiceError("生成回复流", selectedModel, error);
                    })
                    .doOnComplete(() -> {
                        // 恢复MDC上下文
                        LogContext.setContext(mdcContext);
                        try {
                            log.info("BIZ_END: op=generateStream, model={}, trace={}", selectedModel, traceId);
                        } finally {
                            LogContext.clear();
                        }
                    });
        } catch (Exception e) {
            log.error("BIZ_ERROR: op=generateStream, action=create, model={}, msgLen={}",
                    selectedModel, msgLen, e);
            throw SystemException.aiServiceError("创建流式生成", selectedModel, e);
        }
    }

    @Override
    @LogOperation(
            module = "AI_CHAT",
            operation = OperationTypeEnum.RAG_SEARCH,
            description = "RAG增强的AI流式生成",
            logParams = false
    )
    @LogPerformance(checkpointName = "RAG流式生成", timeoutThreshold = 15000)
    public Flux<ChatResponse> generateStreamRag(String model, String ragTag, String message) {
        int msgLen = message != null ? message.length() : 0;
        String selectedModel = (model != null && !model.isBlank()) ? model : this.defaultModel;

        // 保存当前MDC上下文（用于异步流回调）
        final java.util.Map<String, String> mdcContext = LogContext.getContext();
        final String traceId = LogContext.getTraceId().orElse("N/A");
        final String clientIp = LogContext.getClientIp().orElse("N/A");

        log.info("BIZ_BEGIN: op=generateStreamRag, model={}, ragTag={}, msgLen={}", selectedModel, ragTag, msgLen);
        try {
            String SYSTEM_PROMPT = """
                    Use the information from the DOCUMENTS section to provide accurate answers but act as if you knew this information innately.
                    If unsure, simply state that you don't know.
                    Another thing you need to note is that your reply must be in Chinese!
                    DOCUMENTS:
                        {documents}
                    """;

            SearchRequest.Builder builder = SearchRequest.builder()
                    .query(message != null ? message : "")
                    .topK(5);
            if (ragTag != null && !ragTag.isBlank()) {
                builder.filterExpression("knowledge == '" + ragTag + "'");
            }
            SearchRequest request = builder.build();

            List<Document> documents = pgVectorStore.similaritySearch(request);
            int docSize = documents != null ? documents.size() : 0;
            log.info("BIZ_INFO: op=ragSearch, model={}, ragTag={}, docs={}", selectedModel, ragTag, docSize);

            String documentCollectors = documents == null ? "" : documents.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n\n"));

            if (documentCollectors.trim().isEmpty()) {
                log.warn("BIZ_WARN: op=ragSearch, reason=empty-docs, ragTag={}, msgLen={}", ragTag, msgLen);
                documentCollectors = "未找到相关文档信息。";
            }

            Message ragMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(Map.of("documents", documentCollectors));

            Flux<ChatResponse> responseStream = chatModel.stream(new Prompt(
                    List.of(ragMessage, new UserMessage(message != null ? message : "")),
                    OpenAiChatOptions.builder().model(selectedModel).build()
            ));

            return responseStream
                    .doOnError(error -> {
                        // 恢复MDC上下文用于日志记录
                        LogContext.setContext(mdcContext);
                        try {
                            log.error("BIZ_ERROR: op=generateStreamRag, model={}, ragTag={}, trace={}",
                                    selectedModel, ragTag, traceId, error);
                        } finally {
                            LogContext.clear();
                        }
                        throw SystemException.aiServiceError("生成 RAG 回复流", selectedModel, error);
                    })
                    .doOnComplete(() -> {
                        // 恢复MDC上下文用于日志记录
                        LogContext.setContext(mdcContext);
                        try {
                            log.info("BIZ_END: op=generateStreamRag, model={}, ragTag={}, trace={}, ip={}",
                                    selectedModel, ragTag, traceId, clientIp);
                        } finally {
                            LogContext.clear();
                        }
                    });
        } catch (Exception e) {
            log.error("BIZ_ERROR: op=generateStreamRag, action=create, model={}, ragTag={}, msgLen={}",
                    selectedModel, ragTag, msgLen, e);
            throw SystemException.aiServiceError("创建 RAG 流式生成", selectedModel, e);
        }
    }
}
