package com.lcx.app.config;

import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ollama聊天模型配置类
 * 
 * <p>该配置类用于集成Ollama AI模型服务，提供本地化的大语言模型支持。</p>
 * <p>Ollama是一个轻量级的AI模型运行框架，支持在本地运行各种开源大语言模型。</p>
 * 
 * <p>主要功能包括：</p>
 * <ul>
 *   <li>配置Ollama API连接</li>
 *   <li>创建聊天客户端实例</li>
 *   <li>设置默认模型参数</li>
 *   <li>支持模型切换和配置</li>
 * </ul>
 * 
 * <p>支持的模型类型：</p>
 * <ul>
 *   <li>Llama系列模型（如llama2、llama3等）</li>
 *   <li>Mistral系列模型</li>
 *   <li>CodeLlama代码生成模型</li>
 *   <li>其他Ollama支持的开源模型</li>
 * </ul>
 * 
 * <p>配置要求：</p>
 * <ul>
 *   <li>需要本地安装并运行Ollama服务</li>
 *   <li>配置正确的服务地址和端口</li>
 *   <li>下载并安装所需的AI模型</li>
 * </ul>
 * 
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class OllamaChatModelConfig {

    /**
     * 配置Ollama API客户端
     * 
     * <p>创建与Ollama服务通信的API客户端实例。</p>
     * <p>该客户端负责与本地或远程的Ollama服务建立连接，处理模型调用请求。</p>
     * 
     * <p>配置说明：</p>
     * <ul>
     *   <li>从配置文件读取Ollama服务的基础URL</li>
     *   <li>支持HTTP和HTTPS协议</li>
     *   <li>自动处理连接超时和重试机制</li>
     * </ul>
     * 
     * @param ollamaBaseUrl Ollama服务的基础URL，从配置文件spring.ai.ollama.base-url读取
     * @return OllamaApi Ollama API客户端实例
     */
    @Bean
    public OllamaApi ollamaApi(@Value("${spring.ai.ollama.base-url}") String ollamaBaseUrl) {
        return new OllamaApi(ollamaBaseUrl);
    }

    /**
     * 配置Ollama聊天客户端
     * 
     * <p>创建用于聊天对话的客户端实例，支持与AI模型进行交互式对话。</p>
     * <p>该客户端封装了聊天相关的功能，提供简化的API接口。</p>
     * 
     * <p>功能特性：</p>
     * <ul>
     *   <li>支持流式和非流式响应</li>
     *   <li>自动处理上下文管理</li>
     *   <li>支持多轮对话</li>
     *   <li>可配置模型参数（温度、最大长度等）</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>智能问答系统</li>
     *   <li>代码生成和解释</li>
     *   <li>文本摘要和翻译</li>
     *   <li>创意写作辅助</li>
     * </ul>
     * 
     * @param ollamaApi Ollama API客户端实例
     * @param defaultModel 默认使用的AI模型名称，从配置文件spring.ai.ollama.chat.options.model读取
     * @return OllamaChatClient 聊天客户端实例，配置了默认模型参数
     */
    @Bean
    public OllamaChatClient ollamaChatClient(OllamaApi ollamaApi,
            @Value("${spring.ai.ollama.chat.options.model}") String defaultModel) {
        return new OllamaChatClient(ollamaApi)
                .withDefaultOptions(OllamaOptions.create().withModel(defaultModel));
    }

}
