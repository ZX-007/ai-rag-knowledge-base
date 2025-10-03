package com.lcx.app.config;

import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaChatModelConfig {

    @Bean
    public OllamaApi ollamaApi(@Value("${spring.ai.ollama.base-url}") String ollamaBaseUrl) {
        return new OllamaApi(ollamaBaseUrl);
    }

    @Bean
    public OllamaChatClient ollamaChatClient(OllamaApi ollamaApi,
            @Value("${spring.ai.ollama.chat.options.model}") String defaultModel) {
        return new OllamaChatClient(ollamaApi)
                .withDefaultOptions(OllamaOptions.create().withModel(defaultModel));
    }

}
