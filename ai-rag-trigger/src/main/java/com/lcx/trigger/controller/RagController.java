package com.lcx.trigger.controller;

import com.lcx.api.IRagService;
import com.lcx.api.dto.FileUploadRequest;
import com.lcx.api.dto.GitRepositoryRequest;
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
 *   <li>分析Git仓库并导入知识库</li>
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
        return Response.success(ragService.queryRagTagList());
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
        return Response.success(ragService.uploadFile(request));
    }

    /**
     * 分析Git仓库并导入知识库
     *
     * <p>克隆指定的Git仓库，解析其中的文档文件，并将内容导入到知识库中。</p>
     * <p>支持的操作流程：</p>
     * <ol>
     *   <li>使用提供的凭据克隆Git仓库到本地临时目录</li>
     *   <li>遍历仓库中的所有文件，过滤出支持的文档类型</li>
     *   <li>使用Tika解析文档内容并进行文本分割</li>
     *   <li>将处理后的文档存储到PostgreSQL向量数据库</li>
     *   <li>更新Redis中的知识库标签列表</li>
     *   <li>清理本地临时文件</li>
     * </ol>
     *
     * <p>支持的仓库类型：</p>
     * <ul>
     *   <li>GitHub 公开和私有仓库</li>
     *   <li>GitLab 公开和私有仓库</li>
     *   <li>其他支持Git协议的代码托管平台</li>
     * </ul>
     *
     * <p>支持的文档格式：</p>
     * <ul>
     *   <li>代码文件：.java, .py, .js, .ts, .cpp, .c, .h, .go, .rs 等</li>
     *   <li>文档文件：.md, .txt, .doc, .docx, .pdf 等</li>
     *   <li>配置文件：.xml, .json, .yml, .yaml, .properties 等</li>
     * </ul>
     *
     * @param request Git仓库分析请求对象，包含仓库地址、用户名和访问令牌
     * @return 分析结果的响应信息
     */
    @RequestMapping(value = "analyze_git_repository", method = RequestMethod.POST)
    public Response<String> analyzeGitRepository(@Valid @RequestBody GitRepositoryRequest request) {
        return Response.success(ragService.analyzeGitRepository(request));
    }
}
