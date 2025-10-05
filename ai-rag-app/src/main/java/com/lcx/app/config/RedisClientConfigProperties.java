package com.lcx.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis连接配置属性类
 * 
 * <p>该类用于封装Redis连接相关的配置参数，支持从配置文件中读取Redis连接设置。</p>
 * <p>配合Redisson客户端使用，提供完整的Redis连接配置管理。</p>
 * 
 * <p>配置前缀：redis.sdk.config</p>
 * <p>支持的配置项包括基本连接参数、连接池设置、超时配置、重试机制等。</p>
 * 
 * <p>主要配置分类：</p>
 * <ul>
 *   <li>基础连接：主机地址、端口、密码</li>
 *   <li>连接池：池大小、最小空闲连接数</li>
 *   <li>超时设置：连接超时、空闲超时</li>
 *   <li>重试机制：重试次数、重试间隔</li>
 *   <li>健康检查：ping间隔、保活设置</li>
 * </ul>
 * 
 * <p>使用示例（application.yml）：</p>
 * <pre>
 * redis:
 *   sdk:
 *     config:
 *       host: localhost
 *       port: 6379
 *       password: your_password
 *       pool-size: 64
 *       min-idle-size: 10
 * </pre>
 * 
 * @author lcx
 * @version 1.0
 * @since 1.0
 * @see <a href="https://github.com/redisson/redisson/tree/master/redisson-spring-boot-starter">redisson-spring-boot-starter</a>
 */
@Data
@ConfigurationProperties(prefix = "redis.sdk.config", ignoreInvalidFields = true)
public class RedisClientConfigProperties {

    /** 
     * Redis服务器主机地址
     * <p>支持IP地址或域名，默认为localhost</p>
     */
    private String host;
    
    /** 
     * Redis服务器端口号
     * <p>默认Redis端口为6379</p>
     */
    private int port;
    
    /** 
     * Redis服务器访问密码
     * <p>如果Redis服务器设置了密码认证，需要配置此项</p>
     */
    private String password;
    
    /** 
     * 连接池大小
     * <p>设置连接池的最大连接数，默认为64</p>
     * <p>根据应用并发量调整，过大会占用过多资源，过小可能导致连接不足</p>
     */
    private int poolSize = 64;
    
    /** 
     * 连接池最小空闲连接数
     * <p>设置连接池保持的最小空闲连接数，默认为10</p>
     * <p>保持一定数量的空闲连接可以提高响应速度</p>
     */
    private int minIdleSize = 10;
    
    /** 
     * 连接最大空闲时间（毫秒）
     * <p>超过该时间的空闲连接将被关闭，默认为10000毫秒（10秒）</p>
     * <p>合理设置可以释放长时间未使用的连接资源</p>
     */
    private int idleTimeout = 10000;
    
    /** 
     * 连接超时时间（毫秒）
     * <p>建立连接的最大等待时间，默认为10000毫秒（10秒）</p>
     * <p>网络环境较差时可以适当增加此值</p>
     */
    private int connectTimeout = 10000;
    
    /** 
     * 连接重试次数
     * <p>连接失败时的重试次数，默认为3次</p>
     * <p>增加重试次数可以提高连接成功率，但会增加响应时间</p>
     */
    private int retryAttempts = 3;
    
    /** 
     * 连接重试间隔时间（毫秒）
     * <p>每次重试之间的等待时间，默认为1000毫秒（1秒）</p>
     * <p>适当的重试间隔可以避免频繁重试对服务器造成压力</p>
     */
    private int retryInterval = 1000;
    
    /** 
     * 定期检查连接可用性的时间间隔（毫秒）
     * <p>定期向Redis服务器发送ping命令检查连接状态，默认为0（不检查）</p>
     * <p>启用此功能可以及时发现连接问题，建议设置为30000毫秒（30秒）</p>
     */
    private int pingInterval = 0;
    
    /** 
     * 是否保持长连接
     * <p>设置是否启用TCP keepalive机制，默认为true</p>
     * <p>启用长连接可以减少连接建立的开销，提高性能</p>
     */
    private boolean keepAlive = true;

}
