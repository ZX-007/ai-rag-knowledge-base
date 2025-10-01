package com.lcx.api;

import org.springframework.web.multipart.MultipartFile;

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
     * @param ragTag 知识库标签，用于分类和检索
     * @param files 要上传的文件列表
     * @return 上传结果的响应信息
     * @throws IllegalArgumentException 当ragTag为空或files为空时抛出
     */
    String uploadFile(String ragTag, List<MultipartFile> files);

}
