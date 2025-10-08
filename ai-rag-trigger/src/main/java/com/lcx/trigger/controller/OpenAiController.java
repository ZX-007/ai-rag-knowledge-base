package com.lcx.trigger.controller;

import com.lcx.api.dto.ChatRequest;
import com.lcx.api.dto.RagChatRequest;
import com.lcx.api.response.Response;
import com.lcx.trigger.service.OpenAiServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/openai")
public class OpenAiController {

    private final OpenAiServiceImpl aiService;

    @GetMapping(value = "/models")
    public Response<List<String>> queryAvailableModels() {
        return Response.success(aiService.queryAvailableModels());
    }

    @Deprecated(since = "1.1", forRemoval = true)
    @PostMapping(value = "/generate", produces = "application/json")
    public ChatResponse generate(@Valid @RequestBody ChatRequest request) {
        return aiService.generate(request.getModel(), request.getMessage());
    }

    @PostMapping(value = "/generate_stream", produces = "text/event-stream")
    public Flux<ChatResponse> generateStream(@Valid @RequestBody ChatRequest request) {
        return aiService.generateStream(request.getModel(), request.getMessage());
    }

    @PostMapping(value = "generate_stream_rag", produces = "text/event-stream")
    public Flux<ChatResponse> generateStreamRag(@Valid @RequestBody RagChatRequest request) {
        return aiService.generateStreamRag(request.getModel(), request.getRagTag(), request.getMessage());
    }

}    