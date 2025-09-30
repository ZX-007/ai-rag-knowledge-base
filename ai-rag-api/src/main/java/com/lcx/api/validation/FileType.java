package com.lcx.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 文件类型校验注解
 * <p>
 * 用于校验上传文件的类型是否在允许的范围内
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
@Documented
@Constraint(validatedBy = FileTypeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface FileType {

    /**
     * 允许的文件类型（MIME类型）
     */
    String[] allowedTypes() default {
            "text/plain",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/csv",
            "application/json",
            "text/html",
            "text/markdown"
    };

    /**
     * 错误消息
     */
    String message() default "不支持的文件类型，允许的类型: {allowedTypes}";

    /**
     * 校验组
     */
    Class<?>[] groups() default {};

    /**
     * 负载
     */
    Class<? extends Payload>[] payload() default {};
}
