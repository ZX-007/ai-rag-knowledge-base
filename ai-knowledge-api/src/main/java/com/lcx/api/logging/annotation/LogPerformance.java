package com.lcx.api.logging.annotation;

import java.lang.annotation.*;

/**
 * 性能日志注解
 * 
 * <p>用于标记需要记录性能日志的方法。</p>
 * <p>配合LogAspect切面使用，自动记录方法的执行时间和性能指标。</p>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * @LogPerformance(
 *     checkpointName = "AI生成",
 *     timeoutThreshold = 5000
 * )
 * public String generateResponse(String prompt) {
 *     // 方法实现
 * }
 * }</pre>
 * 
 * @author lcx
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogPerformance {

    /**
     * 监控点名称
     * <p>用于标识性能监控点</p>
     * 
     * @return 监控点名称
     */
    String checkpointName() default "";

    /**
     * 超时阈值（毫秒）
     * <p>超过此阈值将记录警告日志</p>
     * 
     * @return 超时阈值（毫秒）
     */
    long timeoutThreshold() default 3000L;

    /**
     * 是否记录方法参数
     * 
     * @return 是否记录方法参数
     */
    boolean logParams() default false;
}

