package com.lcx.mcp.server.csdn.domain.model;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CSDN 文章发布响应结果
 * <p>
 * 该类封装了调用 CSDN 文章发布 API 后返回的响应信息，包括操作状态码、提示消息和文章详细数据。
 * </p>
 * 
 * @author lcx
 * @version 0.0.1
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonClassDescription("CSDN 文章发布响应结果，包含操作状态、提示消息和文章详细信息")
public class ArticleFunctionResponse {

    /**
     * 响应状态码
     * <p>
     * 用于标识文章发布操作的执行结果状态
     * </p>
     */
    @JsonProperty(required = true, value = "code")
    @JsonPropertyDescription("响应状态码：200-发布成功，400-请求错误或达到发布限制，401-未登录或登录过期，-1-系统内部错误")
    private Integer code;

    /**
     * 响应消息
     * <p>
     * 详细描述操作结果的文本信息，包括成功提示或错误原因
     * </p>
     */
    @JsonProperty(required = true, value = "msg")
    @JsonPropertyDescription("响应消息：操作结果的详细描述，成功时返回成功提示，失败时返回错误原因")
    private String msg;

    /**
     * 文章详细数据
     * <p>
     * 发布成功时返回的文章详细信息，包括文章ID、URL、二维码等
     * </p>
     */
    @JsonProperty(required = true, value = "articleData")
    @JsonPropertyDescription("文章详细数据：发布成功后返回的文章信息对象，包含文章ID、访问URL、分享二维码等，发布失败时为null")
    private ArticleData articleData;

    /**
     * 文章数据详情
     * <p>
     * 封装了已发布文章的详细信息，包括访问链接、唯一标识、分享二维码等
     * </p>
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("已发布文章的详细信息，包含文章URL、ID、二维码等数据")
    public static class ArticleData {
        
        /**
         * 文章访问URL
         * <p>
         * 文章发布成功后可以直接访问的完整链接地址
         * </p>
         */
        @JsonProperty(required = true, value = "url")
        @JsonPropertyDescription("文章访问URL：已发布文章的完整访问链接，可直接在浏览器中打开查看文章内容，格式如：https://blog.csdn.net/username/article/details/123456789")
        private String url;

        /**
         * 文章唯一标识ID
         * <p>
         * CSDN 平台为每篇文章分配的唯一数字标识符，用于文章的编辑、删除等操作
         * </p>
         */
        @JsonProperty(required = true, value = "id")
        @JsonPropertyDescription("文章唯一标识ID：CSDN平台分配的文章数字ID，可用于后续的文章编辑、删除等操作")
        private Long id;

        /**
         * 文章分享二维码
         * <p>
         * 用于移动端扫码分享的二维码图片URL或Base64编码
         * </p>
         */
        @JsonProperty(required = true, value = "qrcode")
        @JsonPropertyDescription("文章分享二维码：用于移动端扫码访问文章的二维码图片URL或Base64编码，方便在移动设备上分享和查看")
        private String qrcode;

        /**
         * 文章标题
         * <p>
         * 返回已发布文章的标题，用于确认发布内容
         * </p>
         */
        @JsonProperty(required = true, value = "title")
        @JsonPropertyDescription("文章标题：已发布文章的标题内容，用于确认发布的文章信息是否正确")
        private String title;

        /**
         * 文章描述/摘要
         * <p>
         * 返回已发布文章的描述信息，用于确认发布内容
         * </p>
         */
        @JsonProperty(required = true, value = "description")
        @JsonPropertyDescription("文章描述/摘要：已发布文章的描述或摘要内容，显示在文章列表中，用于确认发布的描述信息是否正确")
        private String description;
    }

}
