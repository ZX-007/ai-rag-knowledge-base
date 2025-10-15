package com.lcx.mcp.server.csdn.domain.service;

import com.lcx.mcp.server.csdn.domain.adapter.ICSDNPort;
import com.lcx.mcp.server.csdn.domain.model.ArticleFunctionRequest;
import com.lcx.mcp.server.csdn.domain.model.ArticleFunctionResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * CSDN 文章服务
 * <p>
 * 提供 CSDN 文章发布功能的 MCP 工具服务
 * </p>
 * 
 * @author lcx
 * @version 1.0
 */
@Slf4j
@Service
public class CSDNArticleService {

    @Resource
    private ICSDNPort port;

    /**
     * 发布文章到 CSDN 博客平台
     * <p>
     * 该方法作为 MCP 工具暴露给 AI，用于自动发布技术文章到 CSDN。
     * 所有字段均为必填，请确保提供完整的参数。
     * </p>
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * {
     *   "title": "Spring Boot 配置文件详解",
     *   "markdowncontent": "# Spring Boot 配置文件\\n\\nSpring Boot 支持多环境配置...",
     *   "tags": "SpringBoot,配置,教程",
     *   "description": "详细介绍 Spring Boot 配置文件的使用方法"
     * }
     * </pre>
     * 
     * @param request 文章发布请求参数，包含标题、内容、标签和描述
     * @return 发布结果响应，包含文章ID、URL等信息
     */
    @Tool(description = """
            发布文章到 CSDN 博客平台的工具。
          
            功能说明：
            将包含标题、Markdown内容、标签和描述的文章发布到 CSDN 博客。
            
            使用示例：
            {
              "title": "Spring Boot 配置文件详解",
              "markdowncontent": "# Spring Boot 配置文件\\n\\nSpring Boot 支持多环境配置，包括 application.yml、application-dev.yml、application-prod.yml 等。\\n\\n## 配置优先级\\n\\n1. 命令行参数\\n2. 环境变量\\n3. 配置文件",
              "tags": "SpringBoot,配置,教程",
              "description": "详细介绍 Spring Boot 配置文件的使用方法"
            }
            """)
    public ArticleFunctionResponse saveArticle(ArticleFunctionRequest request) {
        try {
            log.info("CSDN发帖，标题:{} 内容:{} 标签:{}", 
                    request.getTitle(), 
                    request.getMarkdowncontent(), 
                    request.getTags());
            
            return port.writeArticle(request);
        } catch (IOException e) {
            log.error("发布文章到CSDN失败", e);
            return ArticleFunctionResponse.builder()
                    .code(-1)
                    .msg("发布失败：" + e.getMessage())
                    .articleData(null)
                    .build();
        }
    }

}
