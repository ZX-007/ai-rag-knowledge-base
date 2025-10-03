package com.lcx.app.config;

import org.springframework.ai.ollama.OllamaEmbeddingClient;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * RAG (Retrieval-Augmented Generation) 嵌入配置类
 *
 * <p>该类负责配置RAG系统中用于文档嵌入和向量存储的相关组件，包括：</p>
 * <ul>
 *   <li>文本分割器：将长文档分割成适合处理的块</li>
 *   <li>嵌入客户端：使用Ollama模型将文本转换为向量</li>
 *   <li>向量存储：支持内存存储和PostgreSQL向量存储两种方式</li>
 * </ul>
 *
 * <p>配置的组件支持以下功能：</p>
 * <ul>
 *   <li>文档预处理和分块</li>
 *   <li>文本向量化</li>
 *   <li>向量相似性搜索</li>
 *   <li>RAG检索增强生成</li>
 * </ul>
 *
 * @author lcx
 * @version 1.0
 * @since 2024
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

    @Bean
    public OllamaEmbeddingClient ollamaEmbeddingClient(OllamaApi ollamaApi,
            @Value("${spring.ai.ollama.embedding.model}") String embeddingModel) {
        OllamaEmbeddingClient ollamaEmbeddingClient = new OllamaEmbeddingClient(ollamaApi);
        ollamaEmbeddingClient.withDefaultOptions(OllamaOptions.create().withModel(embeddingModel));
        return ollamaEmbeddingClient;
    }

    /**
     * 配置简单向量存储Bean（内存存储）
     *
     * <p>SimpleVectorStore提供基于内存的向量存储功能，适用于：</p>
     * <ul>
     *   <li>开发测试环境</li>
     *   <li>小规模数据存储</li>
     *   <li>临时向量存储需求</li>
     * </ul>
     *
     * <p>特点：</p>
     * <ul>
     *   <li>数据存储在内存中，访问速度快</li>
     *   <li>应用重启后数据丢失</li>
     *   <li>适合原型开发和测试</li>
     * </ul>
     *

     * @return SimpleVectorStore实例，提供内存向量存储功能
     */
    @Bean
    public SimpleVectorStore vectorStore(OllamaEmbeddingClient embeddingClient) {
        return new SimpleVectorStore(embeddingClient);
    }

    /**
     * 配置PostgreSQL向量存储Bean（持久化存储）
     *
     * <p>PgVectorStore提供基于PostgreSQL的向量存储功能，适用于：</p>
     * <ul>
     *   <li>生产环境</li>
     *   <li>大规模数据存储</li>
     *   <li>需要数据持久化的场景</li>
     * </ul>
     *
     * <p>特点：</p>
     * <ul>
     *   <li>数据持久化存储，应用重启后数据不丢失</li>
     *   <li>支持复杂的向量相似性查询</li>
     *   <li>支持事务和并发访问</li>
     *   <li>可扩展性强，适合大规模部署</li>
     * </ul>
     *
     * <p>依赖组件：</p>
     * <ul>
     *   <li>PostgreSQL数据库（需安装pgvector扩展）</li>
     *   <li>JdbcTemplate用于数据库操作</li>
     *   <li>Ollama嵌入客户端用于向量化</li>
     * </ul>
     *
     * @param jdbcTemplate Spring JDBC模板，用于数据库操作
     * @return PgVectorStore实例，提供PostgreSQL向量存储功能
     */
    @Bean
    public PgVectorStore pgVectorStore(OllamaEmbeddingClient embeddingClient, JdbcTemplate jdbcTemplate) {
        return new PgVectorStore(jdbcTemplate, embeddingClient);
    }

}
