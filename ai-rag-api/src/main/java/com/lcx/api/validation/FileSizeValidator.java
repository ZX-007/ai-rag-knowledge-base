package com.lcx.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件大小校验器
 * <p>
 * 实现文件大小校验逻辑
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
public class FileSizeValidator implements ConstraintValidator<FileSize, MultipartFile> {

    private long maxSize;
    private long minSize;

    @Override
    public void initialize(FileSize constraintAnnotation) {
        this.maxSize = constraintAnnotation.max();
        this.minSize = constraintAnnotation.min();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return true; // 空文件由其他校验注解处理
        }

        long fileSize = file.getSize();
        return fileSize >= minSize && fileSize <= maxSize;
    }
}
