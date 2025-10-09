package com.lcx.app;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.PathResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

@Slf4j
@SpringBootTest
public class JGitTest {

    @Resource
    private TokenTextSplitter tokenTextSplitter;
    @Resource
    private SimpleVectorStore simpleVectorStore;

    @Test
    public void cloneTest() throws Exception {
        String repoURL = "https://github.com/ZX-007/rate-limiter.git";
        String username = "";
        String password = "";

        String localPath = "git-cloned-repo/NoF-RPC";
        log.info("克隆路径：" + new File(localPath).getAbsolutePath());

        FileUtils.deleteDirectory(new File(localPath));

        Git git = Git.cloneRepository()
                .setURI(repoURL)
                .setDirectory(new File(localPath))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
                .call();

        git.close();
    }

    /**
     * 测试文件遍历和文档处理
     * 遍历指定目录下的所有文件，使用 Tika 读取文档内容并存储到向量数据库
     */
    @Test
    public void fileTest() throws IOException {
        Files.walkFileTree(Paths.get("D:\\workspace\\Java\\ai-rag-knowledge-base\\git-cloned-repo\\rate-limiter"), new SimpleFileVisitor<>() {
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

                log.info("处理文件路径: {}", file.toString());

                try {
                    PathResource resource = new PathResource(file);
                    TikaDocumentReader reader = new TikaDocumentReader(resource);

                    List<Document> documents = reader.get();
                    
                    // 检查文档是否为空
                    if (documents == null || documents.isEmpty()) {
                        log.warn("文档读取结果为空: {}", file.toString());
                        return FileVisitResult.CONTINUE;
                    }
                    
                    List<Document> documentSplitterList = tokenTextSplitter.apply(documents);

                    documents.forEach(doc -> doc.getMetadata().put("knowledge", "NoF-RPC"));
                    documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", "NoF-RPC"));

                    simpleVectorStore.accept(documentSplitterList);
                    
                } catch (Exception e) {
                    log.error("处理文件时发生错误: {}, 错误信息: {}", file.toString(), e.getMessage());
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
        });
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
}
