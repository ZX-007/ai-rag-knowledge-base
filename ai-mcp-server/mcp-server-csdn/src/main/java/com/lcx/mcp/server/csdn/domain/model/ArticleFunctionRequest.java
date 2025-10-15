package com.lcx.mcp.server.csdn.domain.model;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.lcx.mcp.server.csdn.types.utils.MarkdownConverter;
import lombok.Data;

/**
 * CSDN 文章发布请求参数
 * <p>
 * 用于定义发布到 CSDN 博客的文章信息，包括标题、内容、标签和描述等必填字段。
 * </p>
 * 
 * @author lcx
 * @version 0.0.1
 */
@Data
// 在将Java对象序列化为JSON时，只包含非空（non-null）的字段
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonClassDescription("CSDN 文章发布请求参数，包含文章的标题、Markdown内容、标签和描述信息")
public class ArticleFunctionRequest {

    /**
     * 文章标题
     * <p>必填字段，建议长度在 10-100 个字符之间</p>
     */
    @JsonProperty(required = true, value = "title")
    @JsonPropertyDescription("文章标题字符串。必填参数。应当简洁明了地概括文章核心主题，建议控制在10-100个字符范围内。好的标题能够吸引读者并准确传达文章内容，例如：'深入理解Java虚拟机内存模型'、'Spring Boot微服务最佳实践'。")
    private String title;

    /**
     * 文章内容（Markdown 格式）
     * <p>必填字段，使用标准 Markdown 语法编写，换行使用 \n</p>
     */
    @JsonProperty(required = true, value = "markdowncontent")
    @JsonPropertyDescription("文章正文内容字符串，使用Markdown语法格式编写。必填参数。支持标准Markdown语法的所有特性，包括：标题（#、##、###）、代码块（```language）、列表（-、1.）、表格、图片、链接等。换行必须使用转义字符\\n表示。内容应当结构清晰、层次分明，包含完整的技术说明或教程步骤。")
    private String markdowncontent;

    /**
     * 文章标签
     * <p>必填字段，多个标签用英文逗号分隔，建议 3-8 个标签</p>
     * <p>示例：Java,Spring,教程 或 Python,机器学习,深度学习</p>
     */
    @JsonProperty(required = true, value = "tags")
    @JsonPropertyDescription("文章标签字符串，用于分类和检索。必填参数。多个标签之间使用英文逗号（,）分隔，不要有空格。建议提供3-8个相关标签，标签应当准确反映文章的技术领域、主题和关键概念。示例格式：'Java,Spring,教程' 或 'Python,机器学习,深度学习,神经网络'。")
    private String tags;

    /**
     * 文章描述/摘要
     * <p>必填字段，建议长度在 50-200 个字符之间</p>
     * <p>用于文章列表展示和SEO优化</p>
     */
    @JsonProperty(required = true, value = "description")
    @JsonPropertyDescription("文章摘要或简介字符串。必填参数。应当用一段话简明扼要地概括文章的核心内容、主要观点或技术要点，帮助读者快速了解文章价值。建议长度控制在50-200个字符之间。这段描述将显示在文章列表中，同时也用于搜索引擎优化（SEO），因此应当包含关键词且具有吸引力。")
    private String description;

    /**
     * 获取 HTML 格式的文章内容
     * <p>将 Markdown 内容转换为 HTML 格式，用于 CSDN 文章发布</p>
     * 
     * @return HTML 格式的文章内容
     */
    public String getContent() {
        return MarkdownConverter.convertToHtml(markdowncontent);
    }

}
