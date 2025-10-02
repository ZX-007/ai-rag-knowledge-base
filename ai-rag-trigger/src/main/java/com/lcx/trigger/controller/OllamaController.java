package com.lcx.trigger.controller;

import com.lcx.api.IAiService;
import com.lcx.api.dto.ChatRequest;
import com.lcx.api.dto.RagChatRequest;
import com.lcx.api.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Ollama AI 控制器
 * <p>
 * 提供基于 Ollama 的 AI 对话服务 REST API 接口。
 * 该控制器实现了通用的 AI 服务接口，提供同步和异步两种调用方式。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>同步生成 AI 回复 - 适用于短文本生成场景</li>
 *   <li>流式生成 AI 回复 - 适用于长文本生成场景</li>
 * </ul>
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 * @see IAiService 通用 AI 服务接口
 */
@Slf4j
@Validated
@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
@RequestMapping("/api/v1/ollama")
public class OllamaController {

    private final IAiService aiService;

    /**
     * 查询可用的 AI 模型列表
     * <p>
     * 获取当前 AI 服务支持的所有可用模型列表。该方法会查询 AI 服务（如 Ollama）的可用模型，
     * 返回模型名称列表，供前端动态加载和选择。
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
     * @return 包含可用模型名称列表的响应结果
     * @throws com.lcx.api.exception.SystemException 当查询模型列表失败时抛出
     */
    @RequestMapping(value = "/models", method = RequestMethod.GET)
    public Response<List<String>> queryAvailableModels() {
        log.info("收到查询可用模型列表请求");

        List<String> models = aiService.queryAvailableModels();
        log.info("查询可用模型列表成功，共{}个模型", models.size());
        return Response.success(models);
    }

    /**
     * 生成 AI 回复
     * <p>
     * 同步生成 AI 回复，适用于短文本生成场景。该方法会等待 AI 服务完成处理后再返回结果。
     * 请求参数会经过验证，确保模型名称和消息内容符合要求。
     * </p>
     * <p>
     * 使用场景：
     * <ul>
     *   <li>简单的问答对话</li>
     *   <li>短文本生成任务</li>
     *   <li>需要完整响应的场景</li>
     * </ul>
     * </p>
     *
     * @param request 包含模型名称和用户消息的请求对象
     * @return AI 生成的完整回复，包含回复内容、元数据等信息
     * @throws jakarta.validation.ConstraintViolationException 当请求参数验证失败时抛出
     * @throws com.lcx.api.exception.SystemException 当 AI 服务调用失败时抛出
     * @see ChatRequest 请求参数对象
     * @see ChatResponse AI 回复对象
     */
    @RequestMapping(value = "/generate", method = RequestMethod.POST)
    public ChatResponse generate(@Valid @RequestBody ChatRequest request) {
        log.info("收到 AI 回复生成请求，模型: {}, 消息长度: {}", request.getModel(), request.getMessage().length());
        
        ChatResponse response = aiService.generate(request.getModel(), request.getMessage());
        log.info("AI 回复生成成功，模型: {}", request.getModel());
        return response;
    }

    /**
     * 流式生成 AI 回复
     * <p>
     * 异步流式生成 AI 回复，适用于长文本生成场景。该方法返回响应流，客户端可以实时接收生成的内容片段。
     * 响应格式为 Server-Sent Events (SSE)，支持实时数据传输。
     * </p>
     * <p>
     * 使用场景：
     * <ul>
     *   <li>长文本生成任务</li>
     *   <li>需要实时响应的对话场景</li>
     *   <li>大模型推理任务</li>
     * </ul>
     * </p>
     *
     * @param request 包含模型名称和用户消息的请求对象
     * @return AI 生成的回复流，可以实时订阅获取生成的内容片段
     * @throws jakarta.validation.ConstraintViolationException 当请求参数验证失败时抛出
     * @throws com.lcx.api.exception.SystemException 当 AI 服务调用失败时抛出
     * @see ChatRequest 请求参数对象
     * @see Flux 响应流对象
     * @see ChatResponse AI 回复对象
     */
    @RequestMapping(value = "/generate_stream", method = RequestMethod.POST, produces = "text/event-stream")
    public Flux<ChatResponse> generateStream(@Valid @RequestBody ChatRequest request) {
        log.info("收到 AI 流式回复生成请求，模型: {}, 消息长度: {}", request.getModel(), request.getMessage().length());
        
        Flux<ChatResponse> responseStream = aiService.generateStream(request.getModel(), request.getMessage());
        log.info("AI 流式回复生成成功，模型: {}", request.getModel());
        return responseStream;
    }

    /**
     * 流式生成 RAG AI 回复
     * <p>
     * 基于检索增强生成（RAG）技术的流式 AI 回复生成。该方法会先从向量数据库中检索相关文档，
     * 然后将检索到的文档作为上下文信息与用户消息一起发送给 AI 模型，生成更准确和相关的回复。
     * 响应格式为 Server-Sent Events (SSE)，支持实时数据传输。
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
     * <p>
     * 使用场景：
     * <ul>
     *   <li>基于知识库的问答系统</li>
     *   <li>文档检索和总结</li>
     *   <li>专业领域知识咨询</li>
     * </ul>
     * </p>
     *
     * @param request 包含模型名称、RAG 标签和用户消息的请求对象
     * @return AI 生成的回复流，可以实时订阅获取生成的内容片段
     * @throws jakarta.validation.ConstraintViolationException 当请求参数验证失败时抛出
     * @throws com.lcx.api.exception.SystemException 当 AI 服务调用失败时抛出
     * @see RagChatRequest RAG 请求参数对象
     * @see Flux 响应流对象
     * @see ChatResponse AI 回复对象
     */
    @RequestMapping(value = "generate_stream_rag", method = RequestMethod.POST, produces = "text/event-stream")
    public Flux<ChatResponse> generateStreamRag(@Valid @RequestBody RagChatRequest request) {
        log.info("收到 AI 流式 RAG 回复生成请求，模型: {}, RAG: {}, 消息长度: {}", request.getModel(), request.getRagTag(),request.getMessage().length());
        Flux<ChatResponse> responseStream = aiService.generateStreamRag(request.getModel(), request.getRagTag(), request.getMessage());
        log.info("AI 流式 RAG 回复生成成功，模型: {}", request.getModel());
        return responseStream;
    }

}    