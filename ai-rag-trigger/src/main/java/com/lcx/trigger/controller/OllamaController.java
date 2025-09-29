package com.lcx.trigger.controller;

import com.lcx.trigger.service.OllamaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatResponse;
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
 * @see com.lcx.api.IAiService 通用 AI 服务接口
 * @see com.lcx.trigger.service.OllamaService Ollama 服务接口
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequiredArgsConstructor
@RequestMapping("/api/v1/ollama")
public class OllamaController{

    /**
     * Ollama AI 服务
     * <p>
     * 用于处理具体的 AI 业务逻辑，包括与 Ollama 服务的交互。
     * 通过构造函数注入，确保依赖注入的正确性。
     * </p>
     */
    private final OllamaService ollamaService;


    @RequestMapping(value = "/generate", method = RequestMethod.GET)
    public ChatResponse generate(@RequestParam String model, @RequestParam String message) {
        log.info("收到 AI 回复生成请求，模型: {}, 消息长度: {}", model, message.length());
        
        try {
            ChatResponse response = ollamaService.generate(model, message);
            log.info("AI 回复生成成功，模型: {}", model);
            return response;
        } catch (Exception e) {
            log.error("AI 回复生成失败，模型: {}, 错误: {}", model, e.getMessage(), e);
            throw e;
        }
    }


    @RequestMapping(value = "/generate_stream", method = RequestMethod.GET)
    public Flux<ChatResponse> generateStream(@RequestParam String model, @RequestParam String message) {
        log.info("收到 AI 流式回复生成请求，模型: {}, 消息长度: {}", model, message.length());
        
        try {
            Flux<ChatResponse> responseStream = ollamaService.generateStream(model, message);
            log.info("AI 流式回复生成成功，模型: {}", model);
            return responseStream;
        } catch (Exception e) {
            log.error("AI 流式回复生成失败，模型: {}, 错误: {}", model, e.getMessage(), e);
            throw e;
        }
    }

}    