package com.lcx.api;

import com.lcx.api.dto.FileUploadRequest;
import com.lcx.api.dto.GitRepositoryRequest;
import com.lcx.api.exception.SystemException;

import java.util.List;

public interface IRagService {

    /**
     * 查询RAG标签列表
     *
     * <p>从Redis缓存中获取所有已创建的知识库标签列表，用于：</p>
     * <ul>
     *   <li>前端展示可用的知识库分类</li>
     *   <li>用户选择要查询的知识库</li>
     *   <li>管理已创建的知识库</li>
     * </ul>
     *
     * @return 包含所有RAG标签的响应结果
     */
    List<String> queryRagTagList();

    /**
     * 上传文件到知识库
     *
     * <p>处理用户上传的文档文件，包括以下步骤：</p>
     * <ol>
     *   <li>使用TikaDocumentReader解析文档内容</li>
     *   <li>使用TokenTextSplitter将文档分割成文本块</li>
     *   <li>为每个文档块添加知识库标签元数据</li>
     *   <li>将处理后的文档存储到PostgreSQL向量数据库</li>
     *   <li>更新Redis中的标签列表</li>
     * </ol>
     *
     * <p>支持的文档格式：</p>
     * <ul>
     *   <li>PDF文档</li>
     *   <li>Word文档（.doc, .docx）</li>
     *   <li>纯文本文件（.txt）</li>
     *   <li>其他Tika支持的格式</li>
     * </ul>
     *
     * @param request 文件上传请求，包含知识库标签和文件列表
     * @return 上传结果的响应信息
     * @throws IllegalArgumentException 当ragTag为空或files为空时抛出
     */
    String uploadFile(FileUploadRequest request);

    /**
     * 分析Git仓库并导入知识库
     *
     * <p>克隆指定的Git仓库，解析其中的文档文件，并将内容导入到知识库中，包括以下步骤：</p>
     * <ol>
     *   <li>使用提供的凭据克隆Git仓库到本地临时目录</li>
     *   <li>遍历仓库中的所有文件，过滤出支持的文档类型</li>
     *   <li>使用TikaDocumentReader解析文档内容</li>
     *   <li>使用TokenTextSplitter将文档分割成文本块</li>
     *   <li>为每个文档块添加知识库标签元数据</li>
     *   <li>将处理后的文档存储到PostgreSQL向量数据库</li>
     *   <li>更新Redis中的标签列表</li>
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
     * @param request Git仓库分析请求，包含仓库地址、用户名和访问令牌
     * @return 分析结果的详细信息，包含处理的文件数量、项目名称等
     * @throws SystemException 当系统级操作失败时抛出
     */
    String analyzeGitRepository(GitRepositoryRequest request);

}
