package com.lcx.trigger.controller;

import com.lcx.api.IRagService;
import com.lcx.api.dto.FileUploadRequest;
import com.lcx.api.response.Response;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RAG控制器
 *
 * <p>该控制器负责处理RAG（Retrieval-Augmented Generation）知识库相关的HTTP请求，包括：</p>
 * <ul>
 *   <li>查询知识库标签列表</li>
 *   <li>上传文档到知识库</li>
 * </ul>
 *
 * <p>控制器职责：</p>
 * <ul>
 *   <li>接收HTTP请求并验证参数</li>
 *   <li>调用业务服务层处理具体业务逻辑</li>
 *   <li>返回统一的响应格式</li>
 *   <li>处理跨域请求</li>
 * </ul>
 *
 * @author lcx
 * @version 1.0
 * @since 2024
 */
@Slf4j
@Validated
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/rag/")
public class RagController {

    @Resource
    private IRagService ragService;

    /**
     * 查询RAG标签列表
     *
     * <p>获取所有已创建的知识库标签列表，用于前端展示和管理。</p>
     *
     * @return 包含所有RAG标签的响应结果
     */
    @RequestMapping(value = "query_rag_tag_list", method = RequestMethod.GET)
    public Response<List<String>> queryRagTagList() {
        log.info("接收到查询RAG标签列表请求");
        
        List<String> tagList = ragService.queryRagTagList();
        log.info("查询RAG标签列表成功，共{}个标签", tagList.size());
        return Response.success(tagList);
    }

    /**
     * 上传文件到知识库
     *
     * <p>处理用户上传的文档文件，支持多种格式的文档解析和向量化存储。</p>
     *
     * @param request 文件上传请求对象
     * @return 上传结果的响应信息
     */
    @RequestMapping(value = "file/upload", method = RequestMethod.POST, headers = "content-type=multipart/form-data")
    public Response<String> uploadFileWithDto(@Valid FileUploadRequest request) {
        log.info("接收到文件上传请求，标签：{}，文件数量：{}",
                request.getRagTag(), request.getFiles() != null ? request.getFiles().size() : 0);

        String result = ragService.uploadFile(request.getRagTag(), request.getFiles());
        log.info("文件上传成功，标签：{}", request.getRagTag());
        return Response.success(result);
    }

}
