package com.lcx.api.dto;

import com.lcx.api.validation.FileSize;
import com.lcx.api.validation.FileType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传请求DTO
 * <p>
 * 用于接收文件上传请求的参数校验
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
@Data
public class FileUploadRequest {

    /**
     * RAG标签
     */
    @NotBlank(message = "RAG标签不能为空")
    @Size(min = 1, max = 32, message = "RAG标签长度必须在1-32个字符之间")
    private String ragTag;

    /**
     * 上传的文件列表
     */
    @NotNull(message = "文件列表不能为null")
    @NotEmpty(message = "文件列表不能为空")
    private List<MultipartFile> files;

    /**
     * 单个文件校验
     */
    @FileSize(max = 10 * 1024 * 1024, message = "文件大小不能超过10MB")
    @FileType(message = "不支持的文件类型")
    private MultipartFile file;
}
