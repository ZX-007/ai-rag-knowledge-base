package com.lcx.app.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * 自定义健康检查指示器
 * <p>
 * 提供应用程序的健康状态检查，用于 Docker 容器健康检查和负载均衡器探测。
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
@Component
public class CustomHealthIndicator implements HealthIndicator {

    /**
     * 执行健康检查
     * <p>
     * 检查应用程序的关键组件是否正常运行。
     * 可以扩展为检查：
     * - 数据库连接状态
     * - Redis 连接状态
     * - 外部 API 可用性
     * - 磁盘空间
     * - 内存使用情况
     * </p>
     *
     * @return Health 健康状态
     */
    @Override
    public Health health() {
        try {
            // 这里可以添加自定义的健康检查逻辑
            // 例如：检查关键服务是否可用
            
            // 简单示例：总是返回健康状态
            return Health.up()
                    .withDetail("status", "AI Knowledge Base is running")
                    .withDetail("service", "ai-rag-knowledge-base")
                    .withDetail("version", "1.0.0")
                    .build();
            
        } catch (Exception e) {
            // 如果检查失败，返回不健康状态
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}

