package com.lcx.api.logging.annotation;

import com.lcx.api.logging.enums.OperationTypeEnum;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 
 * <p>用于标记需要记录操作日志的方法。</p>
 * <p>配合LogAspect切面使用，自动记录方法的执行情况。</p>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * @LogOperation(
 *     module = "AI_CHAT",
 *     operation = "GENERATE",
 *     description = "生成AI回复"
 * )
 * public ChatResponse generate(String message) {
 *     // 方法实现
 * }
 * }</pre>
 * 
 * @author lcx
 * @version 1.0
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogOperation {

    /**
     * 业务模块
     * <p>例如：AI_CHAT、RAG、FILE等</p>
     * 
     * @return 业务模块
     */
    String module() default "";

    /**
     * 操作类型
     * <p>例如：QUERY、CREATE、UPDATE、DELETE等</p>
     * 
     * @return 操作类型
     */
    OperationTypeEnum operation() default OperationTypeEnum.OTHER;

    /**
     * 操作描述
     * <p>详细描述操作内容</p>
     * 
     * @return 操作描述
     */
    String description() default "";

    /**
     * 是否记录请求参数
     * <p>注意：参数会自动进行敏感信息脱敏</p>
     * 
     * @return 是否记录请求参数
     */
    boolean logParams() default true;

    /**
     * 是否记录响应结果
     * <p>大型响应可能会影响日志性能</p>
     * 
     * @return 是否记录响应结果
     */
    boolean logResult() default false;

    /**
     * 是否记录异常信息
     * 
     * @return 是否记录异常信息
     */
    boolean logException() default true;
}

