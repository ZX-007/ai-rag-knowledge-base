package com.lcx.app.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis客户端配置类
 * 
 * <p>该配置类使用Redisson框架来配置Redis客户端连接。</p>
 * <p>Redisson是一个功能强大的Redis Java客户端，提供了丰富的分布式对象和服务。</p>
 * 
 * <p>主要功能包括：</p>
 * <ul>
 *   <li>配置Redis连接参数</li>
 *   <li>设置连接池和超时参数</li>
 *   <li>配置序列化编解码器</li>
 *   <li>支持分布式锁、缓存等功能</li>
 * </ul>
 * 
 * <p>Redisson特性：</p>
 * <ul>
 *   <li>支持Redis集群、哨兵、单机模式</li>
 *   <li>提供分布式锁、信号量、队列等</li>
 *   <li>自动重连和故障转移</li>
 *   <li>高性能的异步和同步API</li>
 * </ul>
 * 
 * <p>适用场景：</p>
 * <ul>
 *   <li>分布式缓存</li>
 *   <li>分布式锁</li>
 *   <li>会话存储</li>
 *   <li>消息队列</li>
 * </ul>
 * 
 * @author lcx
 * @version 1.0
 * @since 1.0
 * @see <a href="https://github.com/redisson/redisson">Redisson官方文档</a>
 */
@Configuration
@EnableConfigurationProperties(RedisClientConfigProperties.class)
public class RedisClientConfig {

    /**
     * 创建Redisson客户端Bean
     * 
     * <p>基于配置属性创建Redisson客户端实例，用于与Redis服务器通信。</p>
     * <p>该方法配置了完整的Redis连接参数，包括连接池、超时、重试等设置。</p>
     * 
     * <p>配置详情：</p>
     * <ul>
     *   <li>使用JSON Jackson编解码器进行数据序列化</li>
     *   <li>配置单机模式连接（可扩展为集群模式）</li>
     *   <li>设置连接池大小和最小空闲连接数</li>
     *   <li>配置各种超时参数和重试机制</li>
     *   <li>启用连接保活和定期检查</li>
     * </ul>
     * 
     * <p>性能优化：</p>
     * <ul>
     *   <li>连接池复用减少连接开销</li>
     *   <li>合理的超时设置避免长时间等待</li>
     *   <li>自动重试机制提高可靠性</li>
     *   <li>定期ping检查连接健康状态</li>
     * </ul>
     * 
     * @param properties Redis连接配置属性，包含主机、端口、密码等信息
     * @return RedissonClient Redisson客户端实例，用于Redis操作
     */
    @Bean
    public RedissonClient redissonClient(RedisClientConfigProperties properties) {
        Config config = new Config();
        // 根据需要可以设定编解码器；https://github.com/redisson/redisson/wiki/4.-%E6%95%B0%E6%8D%AE%E5%BA%8F%E5%88%97%E5%8C%96
        config.setCodec(JsonJacksonCodec.INSTANCE);

        config.useSingleServer()
                .setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                .setPassword(properties.getPassword())
                .setConnectionPoolSize(properties.getPoolSize())
                .setConnectionMinimumIdleSize(properties.getMinIdleSize())
                .setIdleConnectionTimeout(properties.getIdleTimeout())
                .setConnectTimeout(properties.getConnectTimeout())
                .setRetryAttempts(properties.getRetryAttempts())
                .setRetryInterval(properties.getRetryInterval())
                .setPingConnectionInterval(properties.getPingInterval())
                .setKeepAlive(properties.isKeepAlive());

        return Redisson.create(config);
    }

}
