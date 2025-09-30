/**
 * AI 触发器包
 * <p>
 * 该包实现了 AI 服务的触发和调用功能，包括控制器和服务层的实现。
 * 主要负责接收外部请求并调用相应的 AI 服务。
 * </p>
 * <p>
 * 主要组件：
 * <ul>
 *   <li>{@link com.lcx.trigger.controller.OllamaController} - Ollama AI 控制器，提供 REST API 接口</li>
 *   <li>{@link com.lcx.api.IOllamaService} - Ollama AI 服务接口</li>
 *   <li>{@link com.lcx.trigger.service.OllamaServiceImpl} - Ollama AI 服务实现类</li>
 * </ul>
 * </p>
 * <p>
 * 该包遵循 Spring Boot 的分层架构设计，实现了 Controller-Service 模式，
 * 确保代码的可维护性和可测试性。
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
package com.lcx.trigger;