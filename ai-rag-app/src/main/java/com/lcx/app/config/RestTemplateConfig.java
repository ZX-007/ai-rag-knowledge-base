package com.lcx.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置类
 * <p>
 * 配置 RestTemplate Bean，用于 HTTP 客户端调用。
 * 该配置类提供了 RestTemplate 的默认配置，支持基本的 HTTP 请求功能。
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 创建 RestTemplate Bean
     * <p>
     * 提供默认的 RestTemplate 配置，支持基本的 HTTP 请求功能。
     * 可以根据需要添加更多的配置，如超时设置、拦截器等。
     * </p>
     *
     * @return RestTemplate 实例
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
