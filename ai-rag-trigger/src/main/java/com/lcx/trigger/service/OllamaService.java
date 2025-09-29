package com.lcx.trigger.service;

import com.lcx.api.IAiService;

/**
 * Ollama AI 服务接口
 * <p>
 * 继承通用 AI 服务接口，专门用于 Ollama 本地 AI 模型服务。
 * 该接口可以扩展 Ollama 特有的功能，如模型管理、配置管理等。
 * </p>
 * <p>
 * Ollama 是一个开源的大语言模型运行工具，支持在本地运行各种开源模型，
 * 如 Llama、CodeLlama、Mistral 等。
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 * @see com.lcx.api.IAiService 通用 AI 服务接口
 * @see <a href="https://ollama.ai/">Ollama 官方网站</a>
 */
public interface OllamaService extends IAiService {
    
    // 可以在这里添加 Ollama 特有的方法
    // 例如：
    // - 模型管理相关方法
    // - 配置管理相关方法
    // - 模型列表查询方法
    // - 模型状态检查方法等
    
}
