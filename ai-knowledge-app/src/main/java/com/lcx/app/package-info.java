/**
 * AI RAG 知识库应用模块包
 * <p>
 * 该包是 AI RAG 知识库系统的主应用模块，负责启动整个应用程序
 * 并协调各个子模块的工作。
 * </p>
 * <p>
 * 主要组件：
 * <ul>
 *   <li>{@link com.lcx.app.Application} - 应用程序启动类，负责 Spring Boot 应用的启动</li>
 * </ul>
 * </p>
 * <p>
 * 该包作为整个系统的入口点，通过依赖其他模块来实现完整的 AI RAG 功能。
 * 应用启动时会自动扫描并加载所有相关模块的组件。
 * </p>
 * <p>
 * 系统架构：
 * <ul>
 *   <li>API 层：定义通用接口和响应模型</li>
 *   <li>触发器层：实现具体的 AI 服务调用</li>
 *   <li>应用层：整合所有模块，提供完整的应用服务</li>
 * </ul>
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 * @see com.lcx.api API 模块
 * @see com.lcx.trigger 触发器模块
 */
package com.lcx.app;