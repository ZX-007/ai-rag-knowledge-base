package com.lcx.trigger.service;

import com.lcx.api.IRagService;
import com.lcx.api.dto.FileUploadRequest;
import com.lcx.api.dto.GitRepositoryRequest;
import com.lcx.api.exception.BusinessException;
import com.lcx.api.exception.SystemException;
import com.lcx.api.response.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.core.io.PathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
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
    private final SimpleVectorStore simpleVectorStore;
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
            // 记录Redis操作失败的业务上下文，包含更多调试信息
            log.error("查询RAG标签列表失败: Redis键=ragTag, 操作=查询标签列表, 错误详情={}",
                    e.getMessage(), e);
            // 重新抛出时添加有意义的业务上下文
            throw SystemException.redisError("查询标签列表", "ragTag", e);
        }
    }

    @Override
    public String uploadFile(FileUploadRequest request) {
        String ragTag = request.getRagTag();
        List<MultipartFile> files = request.getFiles();

        log.info("开始上传知识库文件，标签：{}，文件数量：{}", ragTag, files.size());

        // 处理每个上传的文件
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                log.warn("跳过空文件：{}", file.getOriginalFilename());
                continue;
            }

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
                // 记录文件处理失败的业务上下文，包含更多调试信息
                log.error("处理文件失败: 文件名={}, 标签={}, 文件大小={}, 错误详情={}",
                        file.getOriginalFilename(), ragTag, file.getSize(), e.getMessage(), e);
                // 重新抛出时添加有意义的业务上下文
                throw SystemException.fileProcessError("上传并处理文件", file.getOriginalFilename(), e);
            }
        }

        // 更新Redis中的标签列表
        updateRagTagList(ragTag);

        log.info("知识库文件上传完成，标签：{}", ragTag);
        return "文件上传成功";
    }

    @Override
    public String analyzeGitRepository(GitRepositoryRequest request) {
        String repoUrl = request.getRepoUrl();
        String userName = request.getUserName();
        String token = request.getToken();

        log.info("开始分析Git仓库，地址：{}，用户名：{}", repoUrl, userName);

        String repoProjectName = extractProjectName(repoUrl);
        String localPath = "git-cloned-repo/" + repoProjectName + "/";
        log.info("开始分析Git仓库，项目名称：{}，克隆路径：{}", repoProjectName, new File(localPath).getAbsolutePath());

        // 统计信息
        int processedFileCount = 0;
        int totalDocumentCount = 0;
        long startTime = System.nanoTime();
        Git git = null;

        try {
            // 清理可能存在的旧目录
            FileUtils.deleteDirectory(new File(localPath));

            // Git 克隆操作，添加详细的异常处理
            log.info("正在克隆Git仓库: {}", repoUrl);
            git = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(new File(localPath))
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, token))
                    .call();
            log.info("Git仓库克隆成功: {}", repoUrl);

        } catch (org.eclipse.jgit.api.errors.TransportException e) {
            String errorMsg = e.getMessage();
            log.error("Git仓库克隆失败 - 传输异常: 仓库={}, 错误详情={}",
                    repoUrl, errorMsg);

            // 根据具体错误信息判断异常类型
            if (errorMsg.contains("not authorized")) {
                throw new BusinessException(ResponseCode.UNAUTHORIZED.getCode(), "认证失败，请检查仓库或令牌", e);
            } else if (errorMsg.contains("timeout") || errorMsg.contains("timed out")) {
                throw SystemException.networkError("Git克隆超时", repoUrl, e);
            } else {
                throw SystemException.networkError("Git克隆传输异常", repoUrl, e);
            }
        } catch (org.eclipse.jgit.api.errors.InvalidRemoteException e) {
            log.error("Git仓库克隆失败 - 无效的远程仓库: 仓库={}, 错误详情={}", repoUrl, e.getMessage());
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "仓库不存在或地址错误", e);
        } catch (org.eclipse.jgit.api.errors.GitAPIException e) {
            log.error("Git仓库克隆失败 - Git API异常: 仓库={}, 错误详情={}", repoUrl, e.getMessage());
            throw SystemException.externalServiceError("Git服务", "克隆仓库", e);
        } catch (Exception e) {
            log.error("Git仓库克隆失败 - 未知异常: 仓库={}, 错误详情={}", repoUrl, e.getMessage());
            throw SystemException.externalServiceError("Git服务", "克隆仓库", e);
        }

        try {
            // 使用数组来在匿名内部类中修改值
            final int[] fileCounter = {0};
            final int[] documentCounter = {0};

            Files.walkFileTree(Paths.get(localPath), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                    // 跳过 .git 目录下的所有文件
                    if (file.toString().contains(".git")) {
                        log.debug("跳过 Git 文件: {}", file.toString());
                        return FileVisitResult.CONTINUE;
                    }

                    // 只处理常见的文档文件类型
                    String fileName = file.getFileName().toString().toLowerCase();
                    if (!isDocumentFile(fileName)) {
                        log.debug("跳过非文档文件: {}", file.toString());
                        return FileVisitResult.CONTINUE;
                    }

                    // 跳过空文件
                    if (attrs.size() == 0) {
                        log.debug("跳过空文件: {}", file.toString());
                        return FileVisitResult.CONTINUE;
                    }

                    log.info("{} 处理文件: {}", repoProjectName, file.getFileName());

                    try {
                        TikaDocumentReader reader = new TikaDocumentReader(new PathResource(file));
                        List<Document> documents = reader.get();

                        // 检查文档是否为空
                        if (documents == null || documents.isEmpty()) {
                            log.warn("文档读取结果为空: {}", file.toString());
                            return FileVisitResult.CONTINUE;
                        }

                        List<Document> documentSplitterList = tokenTextSplitter.apply(documents);

                        documents.forEach(doc -> doc.getMetadata().put("knowledge", repoProjectName));
                        documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", repoProjectName));

                        // todo 使用ps-vector
                        simpleVectorStore.accept(documentSplitterList);

                        fileCounter[0]++;
                        documentCounter[0] += documentSplitterList.size();

                        log.debug("文件 {} 处理完成，生成文档块数：{}", file.getFileName(), documentSplitterList.size());
                    } catch (Exception e) {
                        // 记录文件处理失败的业务上下文，但不中断整个流程
                        log.warn("处理文件失败: 文件={}, 项目={}, 错误详情={}",
                                file.getFileName(), repoProjectName, e.getMessage(), e);
                        // 继续处理其他文件，不中断整个流程
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    // 跳过 .git 目录
                    if (dir.getFileName().toString().equals(".git")) {
                        log.debug("跳过 .git 目录: {}", dir.toString());
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    log.warn("访问文件失败: {} - {}", file.toString(), exc.getMessage());
                    return FileVisitResult.CONTINUE;
                }
            });

            // 更新Redis中的标签列表
            updateRagTagList(repoProjectName);

            long endTime = System.nanoTime();
            long processingTimeNanos = endTime - startTime;
            long processingTimeMillis = processingTimeNanos / 1_000_000; // 转换为毫秒

            processedFileCount = fileCounter[0];
            totalDocumentCount = documentCounter[0];

            String result = String.format("Git仓库分析完成！项目：%s，处理文件数：%d，生成文档块数：%d，耗时：%d毫秒",
                    repoProjectName, processedFileCount, totalDocumentCount, processingTimeMillis);

            log.info("Git仓库分析完成 - {}", result);
            return result;

        } catch (Exception e) {
            // 记录文件处理过程中发生异常的业务上下文
            log.error("文件处理过程中发生异常: 项目={}, 处理文件数={}, 错误详情={}",
                    repoProjectName, processedFileCount, e.getMessage(), e);
            throw new SystemException(
                    ResponseCode.INTERNAL_ERROR.getCode(),
                    "Git仓库文件处理失败，请稍后重试", e
            );
        } finally {
            // 确保资源清理
            try {
                if (git != null) {
                    git.close();
                    log.debug("Git资源已关闭");
                }
            } catch (Exception e) {
                log.warn("关闭Git资源时发生异常: {}", e.getMessage());
            }

            try {
                // 清理本地临时文件
                FileUtils.deleteDirectory(new File(localPath));
                log.debug("临时目录已清理: {}", localPath);
            } catch (Exception e) {
                log.warn("清理临时目录时发生异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 判断是否为文档文件
     * @param fileName 文件名
     * @return 是否为支持的文档文件类型
     */
    private boolean isDocumentFile(String fileName) {
        String[] supportedExtensions = {
                ".txt", ".md", ".java", ".xml", ".json", ".yml", ".yaml",
                ".properties", ".sql", ".js", ".ts", ".html", ".css",
                ".py", ".cpp", ".c", ".h", ".go", ".rs", ".kt", ".scala",
                ".doc", ".docx", ".pdf", ".rtf"
        };

        for (String ext : supportedExtensions) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private String extractProjectName(String repoUrl) {
        String[] parts = repoUrl.split("/");
        String projectNameWithGit = parts[parts.length - 1];
        return projectNameWithGit.replace(".git", "");
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
            // 记录Redis操作失败的业务上下文，包含更多调试信息
            log.error("更新RAG标签列表失败: 标签={}, Redis键=ragTag, 操作=添加标签, 错误详情={}",
                    ragTag, e.getMessage(), e);
            // 重新抛出时添加有意义的业务上下文
            throw SystemException.redisError("添加标签到列表", "ragTag", e);
        }
    }
}
