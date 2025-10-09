package com.lcx.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 文件大小校验注解
 * <p>
 * 用于校验上传文件的大小是否在指定范围内
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileSizeValidator.class)
public @interface FileSize {

    /**
     * 最大文件大小（字节）
     */
    long max() default 10 * 1024 * 1024; // 默认10MB

    /**
     * 最小文件大小（字节）
     */
    long min() default 0;

    /**
     * 错误消息
     */
    String message() default "文件大小必须在 {min} 到 {max} 字节之间";

    /**
     * 校验组
     */
    Class<?>[] groups() default {};

    /**
     * 负载
     */
    Class<? extends Payload>[] payload() default {};
}
