package com.lcx.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * CORS跨域配置类
 * 
 * <p>该配置类用于解决前后端分离架构中的跨域资源共享（CORS）问题。</p>
 * <p>CORS是一种安全机制，允许Web应用程序从不同的域、协议或端口访问资源。</p>
 * 
 * <p>主要功能包括：</p>
 * <ul>
 *   <li>配置允许的请求源（Origin）</li>
 *   <li>设置允许的HTTP方法</li>
 *   <li>定义允许的请求头和响应头</li>
 *   <li>控制是否允许携带凭证（Cookie）</li>
 *   <li>设置预检请求的缓存时间</li>
 * </ul>
 * 
 * <p>适用场景：</p>
 * <ul>
 *   <li>前后端分离的Web应用</li>
 *   <li>需要支持多个前端域名的API服务</li>
 *   <li>移动端或第三方应用调用API</li>
 * </ul>
 * 
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class CorsConfig {

    /**
     * 配置CORS配置源
     * 
     * <p>创建并配置CORS规则，定义哪些跨域请求是被允许的。</p>
     * <p>该方法配置了完整的CORS策略，包括允许的源、方法、头部等。</p>
     * 
     * <p>配置详情：</p>
     * <ul>
     *   <li>允许本地开发环境的所有端口访问</li>
     *   <li>支持所有常用的HTTP方法</li>
     *   <li>允许常见的请求头和响应头</li>
     *   <li>启用凭证传递（支持Cookie）</li>
     *   <li>设置预检请求缓存1小时</li>
     * </ul>
     * 
     * @return CorsConfigurationSource CORS配置源，包含完整的跨域规则
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允许的源（前端地址）
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "https://localhost:*",
            "https://127.0.0.1:*"
        ));

        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
        ));

        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList(
            "Content-Type",
            "Authorization",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));

        // 允许的响应头
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Access-Control-Allow-Methods",
            "Access-Control-Allow-Headers",
            "Access-Control-Max-Age"
        ));

        // 是否允许发送Cookie
        configuration.setAllowCredentials(true);

        // 预检请求的缓存时间（秒）
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * 创建CORS过滤器Bean
     * 
     * <p>基于配置的CORS规则创建过滤器，用于在请求处理链中拦截和处理跨域请求。</p>
     * <p>该过滤器会自动应用CORS配置，对所有匹配的请求进行跨域处理。</p>
     * 
     * <p>工作原理：</p>
     * <ul>
     *   <li>拦截所有HTTP请求</li>
     *   <li>检查请求是否为跨域请求</li>
     *   <li>根据配置的规则验证请求的合法性</li>
     *   <li>在响应中添加相应的CORS头部</li>
     *   <li>处理预检请求（OPTIONS方法）</li>
     * </ul>
     * 
     * @return CorsFilter CORS过滤器实例，用于处理跨域请求
     */
    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }
}
