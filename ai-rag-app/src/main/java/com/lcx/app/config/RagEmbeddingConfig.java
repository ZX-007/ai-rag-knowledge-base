package com.lcx.app.config;

import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG (Retrieval-Augmented Generation) 嵌入配置类
 *
 * @author lcx
 */
@Configuration
public class RagEmbeddingConfig {

    /**
     * 配置文本分割器Bean
     *
     * <p>TokenTextSplitter用于将长文档分割成较小的文本块，以便：</p>
     * <ul>
     *   <li>控制每个文本块的大小，避免超出模型处理限制</li>
     *   <li>提高向量化处理的效率</li>
     *   <li>优化检索精度</li>
     * </ul>
     *
     * @return TokenTextSplitter实例，用于文档分块处理
     */
    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter();
    }

}
