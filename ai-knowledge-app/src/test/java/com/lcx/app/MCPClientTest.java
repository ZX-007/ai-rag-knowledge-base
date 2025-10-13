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

@SpringBootTest
public class MCPClientTest {

    @Resource
    private List<McpAsyncClient> mcpAsyncClientList;
    @Resource
    private ChatClient.Builder chatClientBuilder;
    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    @Test
    public void test() {
        mcpAsyncClientList.forEach(client -> {
            System.out.println(client.getClientInfo());
            System.out.println(client.getServerInfo());
            System.out.println();
        });

        ToolCallback[] toolCallbacks = toolCallbackProvider.getToolCallbacks();
        for (ToolCallback toolCallback : toolCallbacks) {
            System.out.println(toolCallback.getToolDefinition().name());
        }

    }

    @Test
    public void test_tool() {
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

    @Test
    public void test_mcp() {
        String userInput = "获取电脑配置";
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

}
