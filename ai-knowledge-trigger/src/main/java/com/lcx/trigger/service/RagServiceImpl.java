package com.lcx.trigger.service;

import com.lcx.api.IRagService;
import com.lcx.api.dto.FileUploadRequest;
import com.lcx.api.dto.GitRepositoryRequest;
import com.lcx.api.exception.BusinessException;
import com.lcx.api.exception.SystemException;
import com.lcx.api.logging.annotation.LogOperation;
import com.lcx.api.logging.enums.BusinessModuleEnum;
import com.lcx.api.logging.enums.OperationTypeEnum;
import com.lcx.api.logging.util.SensitiveDataMasker;
import com.lcx.api.logging.util.StructuredLogger;
import com.lcx.api.response.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
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
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements IRagService {

    private final PgVectorStore pgVectorStore;
    private final RedissonClient redissonClient;
    private final TokenTextSplitter tokenTextSplitter;

    @Override
    @LogOperation(
            module = "RAG",
            operation = OperationTypeEnum.QUERY,
            description = "查询RAG标签列表"
    )
    public List<String> queryRagTagList() {
        log.info("BIZ_BEGIN: op=queryRagTagList");
        try {
            RSet<String> elements = redissonClient.getSet("ai:rag:tags", StringCodec.INSTANCE);
            List<String> tagList = new ArrayList<>(elements);
            log.info("BIZ_END: op=queryRagTagList, size={}", tagList.size());
            return tagList;
        } catch (Exception e) {
            log.error("BIZ_ERROR: op=queryRagTagList, key=ai:rag:tags", e);
            throw SystemException.redisError("查询标签列表", "ai:rag:tags", e);
        }
    }

    @Override
    @LogOperation(
            module = "FILE",
            operation = OperationTypeEnum.UPLOAD,
            description = "上传文件到RAG知识库",
            logParams = true
    )
    public String uploadFile(FileUploadRequest request) {
        String ragTag = request.getRagTag();
        List<MultipartFile> files = request.getFiles();
        int fileCount = files != null ? files.size() : 0;
        log.info("BIZ_BEGIN: op=uploadFile, tag={}, fileCount={}", ragTag, fileCount);

        if (files == null || files.isEmpty()) {
            log.warn("BIZ_WARN: op=uploadFile, reason=no-files, tag={}", ragTag);
            return "未选择文件";
        }

        int processedCount = 0;
        int totalChunks = 0;
        
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                log.warn("BIZ_WARN: op=uploadFile, reason=empty-file, name={}", 
                        SensitiveDataMasker.mask(file.getOriginalFilename()));
                continue;
            }

            String fileName = file.getOriginalFilename();
            String maskedFileName = SensitiveDataMasker.mask(fileName);
            log.info("BIZ_PROCESS: op=uploadFile, file={}, size={}B", maskedFileName, file.getSize());
            
            try {
                TikaDocumentReader documentReader = new TikaDocumentReader(file.getResource());
                List<Document> documents = documentReader.get();

                List<Document> documentSplitterList = tokenTextSplitter.apply(documents);
                documents.forEach(doc -> doc.getMetadata().put("knowledge", ragTag));
                documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", ragTag));

                pgVectorStore.accept(documentSplitterList);
                
                processedCount++;
                totalChunks += documentSplitterList.size();
                
                log.info("BIZ_SUCCESS: op=uploadFile, file={}, chunks={}", 
                        maskedFileName, documentSplitterList.size());
            } catch (Exception e) {
                log.error("BIZ_ERROR: op=uploadFile, file={}, tag={}, size={}B", 
                        maskedFileName, ragTag, file.getSize(), e);
                throw SystemException.fileProcessError("上传并处理文件", maskedFileName, e);
            }
        }

        updateRagTagList(ragTag);
        
        log.info("BIZ_END: op=uploadFile, tag={}, processed={}/{}, totalChunks={}", 
                ragTag, processedCount, fileCount, totalChunks);
        
        return String.format("文件上传成功！处理文件数：%d，生成文档块数：%d", processedCount, totalChunks);
    }

    @Override
    @LogOperation(
            module = "GIT",
            operation = OperationTypeEnum.GIT_ANALYZE,
            description = "分析Git仓库并导入知识库",
            logParams = false  // 不记录参数，因为包含敏感令牌
    )
    public String analyzeGitRepository(GitRepositoryRequest request) {
        String repoUrl = request.getRepoUrl();
        String userName = request.getUserName();
        String token = request.getToken();
        
        // 脱敏日志
        String maskedUserName = SensitiveDataMasker.maskName(userName);
        String maskedToken = SensitiveDataMasker.maskToken(token);
        
        log.info("BIZ_BEGIN: op=analyzeGit, repo={}, user={}", repoUrl, maskedUserName);

        String repoProjectName = extractProjectName(repoUrl);
        String localPath = "git-cloned-repo/" + repoProjectName + "/";
        log.info("BIZ_INFO: op=analyzeGit, action=prepare-clone, project={}, path={}", 
                repoProjectName, new File(localPath).getAbsolutePath());

        int processedFileCount = 0;
        int totalDocumentCount = 0;
        long startTime = System.currentTimeMillis();
        Git git = null;

        try {
            FileUtils.deleteDirectory(new File(localPath));
            log.debug("BIZ_DEBUG: op=analyzeGit, action=cleanup, path={}", localPath);

            log.info("BIZ_INFO: op=analyzeGit, action=cloning, repo={}", repoUrl);
            git = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(new File(localPath))
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(
                            userName != null ? userName : "",
                            token != null ? token : ""))
                    .call();
            log.info("BIZ_SUCCESS: op=analyzeGit, action=clone-complete, repo={}", repoUrl);

        } catch (org.eclipse.jgit.api.errors.TransportException e) {
            String errorMsg = e.getMessage();
            log.error("BIZ_ERROR: op=analyzeGit, action=clone, type=transport, repo={}", repoUrl, e);
            if (errorMsg != null && errorMsg.contains("not authorized")) {
                throw new BusinessException(ResponseCode.UNAUTHORIZED.getCode(), "认证失败，请检查仓库或令牌", e);
            } else if (errorMsg != null && (errorMsg.contains("timeout") || errorMsg.contains("timed out"))) {
                throw SystemException.networkError("Git克隆超时", repoUrl, e);
            } else {
                throw SystemException.networkError("Git克隆传输异常", repoUrl, e);
            }
        } catch (org.eclipse.jgit.api.errors.InvalidRemoteException e) {
            log.error("BIZ_ERROR: op=analyzeGit, action=clone, type=invalid-remote, repo={}", repoUrl, e);
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "仓库不存在或地址错误", e);
        } catch (org.eclipse.jgit.api.errors.GitAPIException e) {
            log.error("BIZ_ERROR: op=analyzeGit, action=clone, type=git-api, repo={}", repoUrl, e);
            throw SystemException.externalServiceError("Git服务", "克隆仓库", e);
        } catch (Exception e) {
            log.error("BIZ_ERROR: op=analyzeGit, action=clone, type=unknown, repo={}", repoUrl, e);
            throw SystemException.externalServiceError("Git服务", "克隆仓库", e);
        }

        try {
            final int[] fileCounter = {0};
            final int[] documentCounter = {0};

            Files.walkFileTree(Paths.get(localPath), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().contains(".git")) {
                        return FileVisitResult.CONTINUE;
                    }
                    String fileName = file.getFileName().toString().toLowerCase();
                    if (!isDocumentFile(fileName)) {
                        return FileVisitResult.CONTINUE;
                    }
                    if (attrs.size() == 0) {
                        return FileVisitResult.CONTINUE;
                    }

                    log.info("BIZ_PROCESS: op=analyzeGit, action=process-file, project={}, file={}", 
                            repoProjectName, file.getFileName());
                    try {
                        TikaDocumentReader reader = new TikaDocumentReader(new PathResource(file));
                        List<Document> documents = reader.get();
                        if (documents == null || documents.isEmpty()) {
                            log.warn("BIZ_WARN: op=analyzeGit, reason=empty-docs, file={}", file.getFileName());
                            return FileVisitResult.CONTINUE;
                        }
                        List<Document> documentSplitterList = tokenTextSplitter.apply(documents);
                        documents.forEach(doc -> doc.getMetadata().put("knowledge", repoProjectName));
                        documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", repoProjectName));
                        pgVectorStore.accept(documentSplitterList);
                        fileCounter[0]++;
                        documentCounter[0] += documentSplitterList.size();
                        log.debug("BIZ_DEBUG: op=analyzeGit, file={}, chunks={}", 
                                file.getFileName(), documentSplitterList.size());
                    } catch (Exception e) {
                        log.warn("BIZ_WARN: op=analyzeGit, reason=process-error, file={}, project={}", 
                                file.getFileName(), repoProjectName, e);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (".git".equals(dir.getFileName().toString())) {
                        log.debug("analyzeGit skip-dir: path={}", dir);
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    log.warn("analyzeGit visit-failed: path={}, err={}", file, exc.getMessage());
                    return FileVisitResult.CONTINUE;
                }
            });

            updateRagTagList(repoProjectName);

            long endTime = System.currentTimeMillis();
            long costMs = endTime - startTime;
            processedFileCount = fileCounter[0];
            totalDocumentCount = documentCounter[0];

            log.info("BIZ_END: op=analyzeGit, project={}, files={}, chunks={}, costMs={}",
                    repoProjectName, processedFileCount, totalDocumentCount, costMs);
            
            // 结构化业务日志
            StructuredLogger.logSimpleSuccess(
                    BusinessModuleEnum.GIT.getCode(),
                    "GIT_ANALYZE",
                    String.format("分析Git仓库完成: project=%s, files=%d, chunks=%d, cost=%dms",
                            repoProjectName, processedFileCount, totalDocumentCount, costMs)
            );
            
            return String.format("Git仓库分析完成！项目：%s，处理文件数：%d，生成文档块数：%d，耗时：%d毫秒",
                    repoProjectName, processedFileCount, totalDocumentCount, costMs);

        } catch (Exception e) {
            log.error("BIZ_ERROR: op=analyzeGit, project={}, processedFiles={}", 
                    repoProjectName, processedFileCount, e);
            throw new SystemException(
                    ResponseCode.INTERNAL_ERROR.getCode(),
                    "Git仓库文件处理失败，请稍后重试", e
            );
        } finally {
            try {
                if (git != null) {
                    git.close();
                    log.debug("BIZ_DEBUG: op=analyzeGit, action=git-close");
                }
            } catch (Exception e) {
                log.warn("BIZ_WARN: op=analyzeGit, action=git-close-error", e);
            }
            try {
                FileUtils.deleteDirectory(new File(localPath));
                log.debug("BIZ_DEBUG: op=analyzeGit, action=cleanup, path={}", localPath);
            } catch (Exception e) {
                log.warn("BIZ_WARN: op=analyzeGit, action=cleanup-error, path={}", localPath, e);
            }
        }
    }

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

    private void updateRagTagList(String ragTag) {
        try {
            RSet<String> elements = redissonClient.getSet("ai:rag:tags", StringCodec.INSTANCE);
            if (!elements.contains(ragTag)) {
                elements.add(ragTag);
                log.info("BIZ_INFO: op=updateTag, action=add, tag={}", ragTag);
            } else {
                log.info("BIZ_INFO: op=updateTag, action=exists, tag={}", ragTag);
            }
        } catch (Exception e) {
            log.error("BIZ_ERROR: op=updateTag, tag={}, key=ai:rag:tags", ragTag, e);
            throw SystemException.redisError("添加标签到列表", "ai:rag:tags", e);
        }
    }
}
