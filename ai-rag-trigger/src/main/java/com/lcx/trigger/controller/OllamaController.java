package com.lcx.trigger.controller;

import com.lcx.api.IAiService;
import com.lcx.api.IOllamaService;
import com.lcx.api.dto.ChatRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

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
 * @see IOllamaService Ollama 服务接口
 */
@Slf4j
@Validated
@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
@RequestMapping("/api/v1/ollama")
public class OllamaController {

    /**
     * Ollama AI 服务
     * <p>
     * 用于处理具体的 AI 业务逻辑，包括与 Ollama 服务的交互。
     * 通过构造函数注入，确保依赖注入的正确性。
     * </p>
     */
    private final IOllamaService IOllamaService;

    @RequestMapping(value = "/generate", method = RequestMethod.POST)
    public ChatResponse generate(@Valid @RequestBody ChatRequest request) {
        log.info("收到 AI 回复生成请求，模型: {}, 消息长度: {}", request.getModel(), request.getMessage().length());
        
        ChatResponse response = IOllamaService.generate(request.getModel(), request.getMessage());
        log.info("AI 回复生成成功，模型: {}", request.getModel());
        return response;
    }

    @RequestMapping(value = "/generate_stream", method = RequestMethod.POST)
    public Flux<ChatResponse> generateStream(@Valid @RequestBody ChatRequest request) {
        log.info("收到 AI 流式回复生成请求，模型: {}, 消息长度: {}", request.getModel(), request.getMessage().length());
        
        Flux<ChatResponse> responseStream = IOllamaService.generateStream(request.getModel(), request.getMessage());
        log.info("AI 流式回复生成成功，模型: {}", request.getModel());
        return responseStream;
    }

}    