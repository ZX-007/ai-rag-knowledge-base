package com.lcx.app.config;

import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * 参数校验配置类
 *
 * <p>该配置类负责设置Spring Boot应用程序的参数校验功能。</p>
 * <p>基于JSR-303/JSR-380 Bean Validation规范，提供统一的参数校验机制。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>校验器配置：创建和配置LocalValidatorFactoryBean</li>
 *   <li>手动校验支持：提供Validator Bean用于编程式校验</li>
 *   <li>注解校验：支持@Valid、@Validated等注解的自动校验</li>
 *   <li>自定义校验：支持扩展自定义校验器和校验规则</li>
 * </ul>
 *
 * <p>支持的校验注解：</p>
 * <ul>
 *   <li>@NotNull：非空校验</li>
 *   <li>@NotEmpty：非空且非空字符串校验</li>
 *   <li>@NotBlank：非空且包含非空白字符校验</li>
 *   <li>@Size：长度或大小校验</li>
 *   <li>@Min/@Max：数值范围校验</li>
 *   <li>@Pattern：正则表达式校验</li>
 *   <li>@Email：邮箱格式校验</li>
 *   <li>@Valid：级联校验</li>
 * </ul>
 *
 * <p>校验场景：</p>
 * <ul>
 *   <li>Controller参数校验：@RequestBody、@RequestParam等</li>
 *   <li>Service方法参数校验：@Validated注解的类</li>
 *   <li>实体类属性校验：JPA实体保存前校验</li>
 *   <li>配置属性校验：@ConfigurationProperties类校验</li>
 * </ul>
 *
 * <p>错误处理：</p>
 * <ul>
 *   <li>校验失败会抛出MethodArgumentNotValidException</li>
 *   <li>由GlobalExceptionHandler统一处理校验异常</li>
 *   <li>返回详细的校验错误信息给客户端</li>
 * </ul>
 *
 * <p>扩展能力：</p>
 * <ul>
 *   <li>自定义校验器：实现ConstraintValidator接口</li>
 *   <li>自定义校验注解：定义业务特定的校验规则</li>
 *   <li>国际化支持：校验错误消息的多语言支持</li>
 *   <li>分组校验：不同场景使用不同的校验规则</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // Controller中使用
 * @PostMapping("/users")
 * public Response<User> createUser(@Valid @RequestBody UserRequest request) {
 *     return userService.createUser(request);
 * }
 * 
 * // 实体类中使用
 * public class UserRequest {
 *     @NotBlank(message = "用户名不能为空")
 *     @Size(min = 2, max = 20, message = "用户名长度必须在2-20个字符之间")
 *     private String username;
 *     
 *     @Email(message = "邮箱格式不正确")
 *     private String email;
 * }
 * 
 * // 手动校验
 * @Autowired
 * private Validator validator;
 * 
 * Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);
 * }</pre>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class ValidationConfig {

    /**
     * 配置校验器工厂Bean
     *
     * <p>创建LocalValidatorFactoryBean实例，作为Spring Boot应用程序的校验器工厂。</p>
     * <p>LocalValidatorFactoryBean是Spring对JSR-303 Bean Validation的集成实现。</p>
     *
     * <p>主要功能：</p>
     * <ul>
     *   <li>校验器创建：负责创建和管理Validator实例</li>
     *   <li>消息解析：支持校验错误消息的国际化</li>
     *   <li>约束工厂：管理自定义约束验证器的创建</li>
     *   <li>Spring集成：与Spring容器深度集成，支持依赖注入</li>
     * </ul>
     *
     * <p>配置特性：</p>
     * <ul>
     *   <li>默认配置：使用Hibernate Validator作为默认实现</li>
     *   <li>消息源集成：自动集成Spring的MessageSource</li>
     *   <li>自定义扩展：支持添加自定义校验器和约束</li>
     *   <li>性能优化：校验器实例复用，提高校验性能</li>
     * </ul>
     *
     * <p>扩展示例：</p>
     * <pre>{@code
     * @Bean
     * public LocalValidatorFactoryBean validatorFactory() {
     *     LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
     *     // 设置自定义消息源
     *     factory.setValidationMessageSource(messageSource());
     *     // 添加自定义约束验证器
     *     factory.setConstraintValidatorFactory(customConstraintValidatorFactory());
     *     return factory;
     * }
     * }</pre>
     *
     * @return LocalValidatorFactoryBean 校验器工厂实例，用于创建和管理校验器
     */
    @Bean
    public LocalValidatorFactoryBean validatorFactory() {
        // 可以在这里添加自定义的校验器
        return new LocalValidatorFactoryBean();
    }

    /**
     * 配置校验器Bean
     *
     * <p>从校验器工厂中获取Validator实例，用于编程式参数校验。</p>
     * <p>该Validator可以在Service层或其他组件中注入使用，进行手动校验操作。</p>
     *
     * <p>主要用途：</p>
     * <ul>
     *   <li>编程式校验：在代码中手动触发对象校验</li>
     *   <li>条件校验：根据业务逻辑动态决定是否校验</li>
     *   <li>批量校验：对集合中的多个对象进行校验</li>
     *   <li>自定义校验逻辑：结合业务规则进行复杂校验</li>
     * </ul>
     *
     * <p>校验方法：</p>
     * <ul>
     *   <li>validate(object)：校验整个对象</li>
     *   <li>validateProperty(object, propertyName)：校验单个属性</li>
     *   <li>validateValue(beanType, propertyName, value)：校验属性值</li>
     * </ul>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * @Service
     * public class UserService {
     *     @Autowired
     *     private Validator validator;
     *     
     *     public void processUser(UserRequest request) {
     *         // 手动校验
     *         Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);
     *         if (!violations.isEmpty()) {
     *             // 处理校验错误
     *             String errorMessage = violations.stream()
     *                 .map(ConstraintViolation::getMessage)
     *                 .collect(Collectors.joining(", "));
     *             throw new BusinessException("参数校验失败: " + errorMessage);
     *         }
     *         // 业务逻辑处理
     *     }
     * }
     * }</pre>
     *
     * <p>与注解校验的区别：</p>
     * <ul>
     *   <li>注解校验：自动触发，适用于Controller层参数</li>
     *   <li>编程式校验：手动触发，适用于Service层业务逻辑</li>
     *   <li>灵活性：编程式校验可以根据条件动态校验</li>
     * </ul>
     *
     * @param validatorFactory 校验器工厂，用于创建Validator实例
     * @return Validator 校验器实例，用于手动触发参数校验
     */
    @Bean
    public Validator validator(LocalValidatorFactoryBean validatorFactory) {
        return validatorFactory.getValidator();
    }
}
