package com.lcx.app;

import io.modelcontextprotocol.client.McpAsyncClient;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * MCP 客户端集成测试类
 * <p>
 * 本测试类用于测试 Model Context Protocol (MCP) 的各种功能，包括：
 * </p>
 * <ul>
 *     <li>MCP 客户端连接和工具注册</li>
 *     <li>AI 与 MCP 工具的交互</li>
 *     <li>单个 MCP 工具调用</li>
 *     <li>多个 MCP 工具协同工作</li>
 *     <li>CSDN 文章自动发布</li>
 *     <li>AI 内容生成 + 自动发布工作流</li>
 * </ul>
 *
 * <h3>涉及的 MCP 服务器：</h3>
 * <ul>
 *     <li><b>mcp-server-systemInfo</b> - 提供系统信息采集功能</li>
 *     <li><b>mcp-server-csdn</b> - 提供 CSDN 文章发布功能</li>
 * </ul>
 *
 * <h3>测试分类：</h3>
 * <ul>
 *     <li><b>基础测试</b> - 验证 MCP 连接和工具注册</li>
 *     <li><b>功能测试</b> - 测试单个工具的调用</li>
 *     <li><b>工作流测试</b> - 测试多工具协同完成复杂任务</li>
 *     <li><b>集成测试</b> - 测试 AI + MCP 的端到端流程</li>
 * </ul>
 *
 * @author lcx
 * @version 1.2.0
 * @since 1.2.0
 */
@SpringBootTest
public class MCPClientTest {

    @Resource
    private List<McpAsyncClient> mcpAsyncClientList;
    @Resource
    private ChatClient.Builder chatClientBuilder;
    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    /**
     * 测试 MCP 客户端和工具注册
     * 功能：
     * 1. 检查所有 MCP 客户端的连接状态
     * 2. 打印客户端和服务器信息
     * 3. 列出所有可用的工具回调
     * 
     * 预期结果：能够看到所有注册的 MCP 服务器和可用工具
     */
    @Test
    public void test_mcp_client_and_tools_info() {
        System.out.println("\n=== MCP 客户端信息 ===");
        mcpAsyncClientList.forEach(client -> {
            System.out.println("客户端信息: " + client.getClientInfo());
            System.out.println("服务器信息: " + client.getServerInfo());
            System.out.println();
        });

        System.out.println("\n=== 可用工具列表 ===");
        ToolCallback[] toolCallbacks = toolCallbackProvider.getToolCallbacks();
        for (ToolCallback toolCallback : toolCallbacks) {
            System.out.println("工具名称: " + toolCallback.getToolDefinition().name());
        }
    }

    /**
     * 测试 AI 工具列表查询
     * 功能：让 AI 自我介绍可用的工具能力
     * 
     * 测试场景：询问 AI 有哪些工具可以使用
     * 预期结果：AI 会列出所有注册的 MCP 工具及其功能说明
     */
    @Test
    public void test_ai_list_available_tools() {
        String userInput = "你有哪些工具可以使用";
        
        var chatClient = chatClientBuilder
                .defaultToolCallbacks(toolCallbackProvider)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("qwen3-max")
                        .build())
                .build();

        System.out.println("\n>>> QUESTION: " + userInput);
        System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());
    }

    /**
     * 测试 MCP 工具调用 - 文件创建
     * 功能：测试 MCP 工具的基本调用能力
     * 
     * 测试场景：
     * - 场景1：获取电脑配置信息
     * - 场景2：使用 MCP 工具创建文件
     * 
     * 预期结果：AI 能够正确调用 MCP 工具完成任务
     */
    @Test
    public void test_mcp_tool_create_file() {
        // 场景1：获取电脑配置
        String userInput = "获取电脑配置";
        
        // 场景2：创建文件
        userInput = "请使用MCP工具在 D:\\workspace\\Java\\ai-knowledge-base\\data 文件夹下，创建 电脑.txt";

        var chatClient = chatClientBuilder
                .defaultToolCallbacks(toolCallbackProvider)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("qwen3-max")
                        .build())
                .build();

        System.out.println("\n>>> QUESTION: " + userInput);
        System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());
    }

    /**
     * 测试 MCP 工具链工作流 - 多步骤任务
     * 功能：测试 AI 调用多个 MCP 工具完成复杂任务
     * 
     * 测试场景：
     * 1. 获取电脑配置信息（调用 systemInfo MCP）
     * 2. 在指定文件夹创建文件
     * 3. 将配置信息写入文件
     * 
     * 预期结果：AI 能够按顺序调用多个工具，完成完整的工作流
     */
    @Test
    public void test_mcp_workflow_multi_steps() {
        // 简单场景
        String userInput = "获取电脑配置";
        
        // 复杂工作流：获取信息 -> 创建文件 -> 写入内容
        userInput = "获取电脑配置 在 D:\\workspace\\Java\\ai-knowledge-base\\data 文件夹下，创建 电脑.txt 把电脑配置写入 电脑.txt";
        
        var chatClient = chatClientBuilder
                .defaultToolCallbacks(toolCallbackProvider)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("qwen3-max")
                        .build())
                .build();
        
        System.out.println("\n>>> QUESTION: " + userInput);
        System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());
    }

    /**
     * 测试 CSDN 发帖功能 - 基础测试
     * 功能：测试 CSDN MCP 的基本文章发布能力
     * 
     * 测试内容：
     * - 发布一篇完整的技术文章
     * - 包含标题、内容、标签、描述等基本信息
     * 
     * 预期结果：文章成功发布到 CSDN
     */
    @Test
    public void test_csdn_post_article() {
        String userInput = """
                帮我在 CSDN 上发布一篇关于 Spring Boot 配置文件的技术文章。
                
                文章标题定为"Spring Boot 配置文件详解"，内容主要介绍 Spring Boot 的多环境配置功能，
                包括 application.yml、application-dev.yml、application-prod.yml 等配置文件的使用。
                
                文章需要包含一个"配置优先级"的章节，讲解三个方面：
                1. 命令行参数
                2. 环境变量  
                3. 配置文件
                
                给文章打上这几个标签：SpringBoot、配置、教程，文章描述写：详细介绍 Spring Boot 配置文件的使用方法。
                """;

        var chatClient = chatClientBuilder
                .defaultToolCallbacks(toolCallbackProvider)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("qwen3-max")
                        .build())
                .build();

        System.out.println("\n>>> QUESTION: " + userInput);
        System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());
    }

    /**
     * 测试 CSDN 发帖功能 - 草稿发布
     * 功能：测试文章状态控制（草稿 vs 发布）
     *
     * 测试内容：
     * - 发布草稿状态的文章
     * - 验证文章不会立即公开
     *
     * 使用场景：
     * - 测试环境避免发布过多测试文章
     * - 文章需要后续编辑再发布
     *
     * 预期结果：文章保存为草稿，不在博客中公开显示
     */
    @Test
    public void test_csdn_post_draft() {
        String userInput = """
                帮我发布一篇 CSDN 草稿，标题是'测试文章'，
                内容是'这是一篇测试文章'，状态设置为草稿
                """;

        var chatClient = chatClientBuilder
                .defaultToolCallbacks(toolCallbackProvider)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("qwen3-max")
                        .build())
                .build();

        System.out.println("\n>>> QUESTION: " + userInput);
        System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());
    }

    /**
     * 测试 CSDN 发帖功能 - 结构化技术面试题文章
     * 功能：测试发布包含多级标题和代码的结构化文章
     *
     * 测试内容：
     * - 发布 Java 面试题文章
     * - 包含多级 Markdown 标题
     * - 包含技术要点说明
     *
     * 文章特点：
     * - 结构清晰（使用 H1、H2 标题）
     * - 内容专业（面试常见问题）
     * - 易于阅读（格式规范）
     *
     * 预期结果：文章排版美观，结构清晰
     */
    @Test
    public void test_csdn_post_interview_article() {
        String userInput = """
                帮我发布一篇 Java 面试题文章到 CSDN，内容如下：
                
                # Java 面试高频题 TOP 10
                
                ## 1. Java 中的 volatile 关键字
                volatile 保证可见性和有序性，但不保证原子性。
                
                ## 2. synchronized 和 Lock 的区别
                synchronized 是 JVM 层面的锁，Lock 是 API 层面的锁。
                
                标签：'Java,面试,并发编程'，描述：'整理了 Java 面试中的高频题目'
                """;

        var chatClient = chatClientBuilder
                .defaultToolCallbacks(toolCallbackProvider)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("qwen3-max")
                        .build())
                .build();

        System.out.println("\n>>> QUESTION: " + userInput);
        System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());
    }

    /**
     * 测试 CSDN 发帖功能 - AI 生成内容并发布
     * 功能：测试 AI 内容生成 + CSDN 自动发布的完整流程
     *
     * 测试内容：
     * - 让 AI 根据主题生成技术文章
     * - 包含源码分析和面试题
     * - 自动发布到 CSDN
     *
     * 预期结果：AI 生成高质量文章并成功发布
     */
    @Test
    public void test_csdn_post_with_ai_content() {
        String userInput = """
                请帮我写一篇关于 Java HashMap 底层原理的技术文章，
                包含源码分析和面试题，然后发布到 CSDN，标签使用 'Java,HashMap,面试'
                """;

        var chatClient = chatClientBuilder
                .defaultToolCallbacks(toolCallbackProvider)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("qwen3-max")
                        .build())
                .build();

        System.out.println("\n>>> QUESTION: " + userInput);
        System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());
    }

    /**
     * 测试 CSDN 发帖功能 - 完整工作流（内容生成 + 发布）
     * 功能：测试从内容创作到发布的端到端流程
     *
     * 测试内容：
     * 1. AI 根据详细要求生成技术文章
     *    - 包含多个指定章节
     *    - 符合技术写作规范
     * 2. 自动设置文章元数据（标签、类型等）
     * 3. 发布到 CSDN
     *
     * 测试场景：Spring AOP 原理讲解
     * 预期结果：生成结构完整、内容专业的原创技术文章
     */
    @Test
    public void test_csdn_workflow_generate_and_post() {
        String userInput = """
                请帮我完成以下任务：
                1. 写一篇关于 Spring AOP 原理的技术文章，要求包含：
                   - AOP 的基本概念
                   - 动态代理的实现原理（JDK 和 CGLIB）
                   - 实际应用场景示例
                2. 然后将这篇文章发布到 CSDN
                3. 标签使用：'Spring,AOP,动态代理'
                4. 文章类型设置为原创
                """;

        var chatClient = chatClientBuilder
                .defaultToolCallbacks(toolCallbackProvider)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("qwen3-max")
                        .build())
                .build();

        System.out.println("\n>>> QUESTION: " + userInput);
        System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());
    }

}
