package com.lcx.app.config;

import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * 参数校验配置
 * <p>
 * 配置参数校验相关的Bean和属性
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class ValidationConfig {

    /**
     * 配置校验器工厂Bean
     * <p>
     * 用于创建Validator实例，支持自定义校验行为
     * </p>
     *
     * @return LocalValidatorFactoryBean
     */
    @Bean
    public LocalValidatorFactoryBean validatorFactory() {
        // 可以在这里添加自定义的校验器
        return new LocalValidatorFactoryBean();
    }

    /**
     * 配置校验器
     * <p>
     * 用于手动触发参数校验
     * </p>
     *
     * @param validatorFactory 校验器工厂
     * @return Validator
     */
    @Bean
    public Validator validator(LocalValidatorFactoryBean validatorFactory) {
        return validatorFactory.getValidator();
    }
}
