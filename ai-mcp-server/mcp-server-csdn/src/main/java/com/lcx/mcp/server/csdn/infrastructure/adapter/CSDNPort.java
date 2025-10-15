package com.lcx.mcp.server.csdn.infrastructure.adapter;

import com.alibaba.fastjson2.JSON;
import com.lcx.mcp.server.csdn.domain.adapter.ICSDNPort;
import com.lcx.mcp.server.csdn.domain.model.ArticleFunctionRequest;
import com.lcx.mcp.server.csdn.domain.model.ArticleFunctionResponse;
import com.lcx.mcp.server.csdn.infrastructure.gateway.ICSDNService;
import com.lcx.mcp.server.csdn.infrastructure.gateway.dto.ArticleRequestDTO;
import com.lcx.mcp.server.csdn.infrastructure.gateway.dto.ArticleResponseDTO;
import com.lcx.mcp.server.csdn.types.properties.CSDNApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CSDNPort implements ICSDNPort {

    private final ICSDNService csdnService;
    private final CSDNApiProperties csdnApiProperties;

    @Override
    public ArticleFunctionResponse writeArticle(ArticleFunctionRequest request) throws IOException {

        ArticleRequestDTO articleRequestDTO = new ArticleRequestDTO();
        articleRequestDTO.setTitle(request.getTitle());
        articleRequestDTO.setMarkdowncontent(request.getMarkdowncontent());
        articleRequestDTO.setContent(request.getContent());
        articleRequestDTO.setTags(request.getTags());
        articleRequestDTO.setDescription(request.getDescription());
        articleRequestDTO.setCategories(csdnApiProperties.getCategories());

        Call<ArticleResponseDTO> call = csdnService.saveArticle(articleRequestDTO, csdnApiProperties.getCookie());
        Response<ArticleResponseDTO> response = call.execute();

        log.info("请求CSDN发帖 \nreq:{} \nres:{}", JSON.toJSONString(articleRequestDTO), JSON.toJSONString(response));

        if (response.isSuccessful() && response.body() != null) {
            ArticleResponseDTO articleResponseDTO = response.body();
            ArticleResponseDTO.ArticleData articleData = articleResponseDTO.getData();

            if (articleData == null) {
                // 如果没有返回文章数据，说明请求失败
                return ArticleFunctionResponse.builder()
                        .code(articleResponseDTO.getCode() != null ? articleResponseDTO.getCode() : -1)
                        .msg(articleResponseDTO.getMsg() != null ? articleResponseDTO.getMsg() : "发布失败：未返回文章数据")
                        .articleData(null)
                        .build();
            }

            return ArticleFunctionResponse.builder()
                    .code(articleResponseDTO.getCode())
                    .msg(articleResponseDTO.getMsg())
                    .articleData(ArticleFunctionResponse.ArticleData.builder()
                            .url(articleData.getUrl())
                            .id(articleData.getId())
                            .qrcode(articleData.getQrcode())
                            .title(articleData.getTitle())
                            .description(articleData.getDescription())
                            .build())
                    .build();
        }

        // 请求失败时，尽量解析服务端 JSON，提取 code/msg，返回结构化响应
        Integer respCode = response.code();
        String respMsg = "发布失败";
        try {
            if (response.errorBody() != null) {
                String body = response.errorBody().string();
                if (!body.isEmpty()) {
                    ArticleResponseDTO err = com.alibaba.fastjson2.JSON.parseObject(body, ArticleResponseDTO.class);
                    if (err != null) {
                        if (err.getCode() != null) {
                            respCode = err.getCode();
                        }
                        if (err.getMsg() != null && !err.getMsg().isEmpty()) {
                            respMsg = err.getMsg();
                        }
                    }
                }
            }
            if ("发布失败".equals(respMsg)) {
                String reason = response.message();
                if (!reason.isEmpty()) respMsg = reason;
                else respMsg = "HTTP " + respCode;
            }
        } catch (Exception e) {
            log.warn("解析错误响应失败", e);
        }

        return ArticleFunctionResponse.builder()
                .code(respCode)
                .msg(respMsg)
                .articleData(null)
                .build();
    }

}
