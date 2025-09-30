package com.lcx.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

/**
 * 文件类型校验器
 * <p>
 * 实现文件类型校验逻辑
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
public class FileTypeValidator implements ConstraintValidator<FileType, MultipartFile> {

    private String[] allowedTypes;

    @Override
    public void initialize(FileType constraintAnnotation) {
        this.allowedTypes = constraintAnnotation.allowedTypes();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return true; // 空文件由其他校验注解处理
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        return Arrays.asList(allowedTypes).contains(contentType);
    }
}
