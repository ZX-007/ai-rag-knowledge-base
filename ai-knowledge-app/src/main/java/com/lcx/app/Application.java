package com.lcx.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI RAG 知识库应用启动类
 * <p>
 * 该启动类负责启动整个 AI RAG 知识库系统，包括所有相关模块的组件扫描和配置。
 * </p>
 * <p>
 * 通过 @ComponentScan 注解确保扫描到所有相关包中的 Spring 组件。
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication(scanBasePackages = {
        "com.lcx.api",     // API 模块
        "com.lcx.trigger", // 触发器模块
        "com.lcx.app"      // 应用模块
})
public class Application {

    /**
     * 应用程序入口点
     * <p>
     * 启动 Spring Boot 应用程序，初始化所有配置的组件和服务。
     * </p>
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}

