package com.lcx.trigger.service;

import com.lcx.api.IRagService;
import com.lcx.api.exception.ParameterException;
import com.lcx.api.exception.SystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * RAG服务实现类
 *
 * <p>该类实现了RAG（Retrieval-Augmented Generation）知识库的核心业务逻辑，包括：</p>
 * <ul>
 *   <li>知识库标签管理：查询和管理RAG标签列表</li>
 *   <li>文档上传处理：支持多种格式文档的上传和向量化存储</li>
 *   <li>文档预处理：使用Tika解析文档内容，使用TokenTextSplitter进行文本分割</li>
 *   <li>向量存储：将处理后的文档存储到PostgreSQL向量数据库中</li>
 * </ul>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>支持多种文档格式（PDF、Word、TXT等）的解析</li>
 *   <li>自动文本分割和向量化处理</li>
 *   <li>基于标签的知识库分类管理</li>
 *   <li>使用Redis缓存标签信息，提高查询效率</li>
 * </ul>
 *
 * @author lcx
 * @version 1.0
 * @since 2024
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements IRagService {

    private final PgVectorStore pgVectorStore;
    private final RedissonClient redissonClient;
    private final TokenTextSplitter tokenTextSplitter;

    @Override
    public List<String> queryRagTagList() {
        log.info("开始查询RAG标签列表");
        try {
            RList<String> elements = redissonClient.getList("ragTag");
            List<String> tagList = new ArrayList<>(elements);
            log.info("查询RAG标签列表成功，共{}个标签", tagList.size());
            return tagList;
        } catch (Exception e) {
            log.error("查询RAG标签列表失败", e);
            throw SystemException.redisError("查询RAG标签列表失败", e);
        }
    }

    @Override
    public String uploadFile(String ragTag, List<MultipartFile> files) {
        log.info("开始上传知识库文件，标签：{}，文件数量：{}", ragTag, files.size());

        // 处理每个上传的文件
        for (MultipartFile file : files) {
            log.info("处理文件：{}，大小：{} bytes", file.getOriginalFilename(), file.getSize());

            try {
                // 使用Tika解析文档内容
                TikaDocumentReader documentReader = new TikaDocumentReader(file.getResource());
                List<Document> documents = documentReader.get();

                // 使用TokenTextSplitter分割文档
                List<Document> documentSplitterList = tokenTextSplitter.apply(documents);

                // 为原始文档和分割后的文档添加知识库标签
                documents.forEach(doc -> doc.getMetadata().put("knowledge", ragTag));
                documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", ragTag));

                // 存储到PostgreSQL向量数据库
                pgVectorStore.accept(documentSplitterList);

                log.info("文件 {} 处理完成，原始文档数：{}，分割后文档数：{}",
                        file.getOriginalFilename(), documents.size(), documentSplitterList.size());
            } catch (Exception e) {
                log.error("处理文件 {} 失败", file.getOriginalFilename(), e);
                throw SystemException.fileUploadError("处理文件 " + file.getOriginalFilename() + " 失败", e);
            }
        }

        // 更新Redis中的标签列表
        updateRagTagList(ragTag);

        log.info("知识库文件上传完成，标签：{}", ragTag);
        return "文件上传成功";
    }

    /**
     * 更新RAG标签列表
     *
     * <p>将新的知识库标签添加到Redis缓存中，如果标签已存在则不重复添加。</p>
     *
     * @param ragTag 要添加的知识库标签
     */
    private void updateRagTagList(String ragTag) {
        try {
            RList<String> elements = redissonClient.getList("ragTag");
            if (!elements.contains(ragTag)) {
                elements.add(ragTag);
                log.info("添加新的知识库标签：{}", ragTag);
            } else {
                log.debug("知识库标签已存在：{}", ragTag);
            }
        } catch (Exception e) {
            log.error("更新RAG标签列表失败，标签：{}", ragTag, e);
            throw SystemException.redisError("更新RAG标签列表失败", e);
        }
    }
}
