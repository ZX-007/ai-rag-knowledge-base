package com.lcx.mcp.server.csdn.infrastructure.gateway.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
// 当 Jackson 反序列化 JSON 到该类时，忽略 JSON 中类里没有的未知字段，避免抛出异常。
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArticleResponseDTO {
    private Integer code;
    private String traceId;
    private ArticleData data;

    // 兼容服务端同时返回 msg / message 两种字段
    @JsonAlias({"msg", "message"})
    private String msg;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ArticleData {
        private String url;
        private Long id;
        private String qrcode;
        private String title;
        private String description;
    }
}