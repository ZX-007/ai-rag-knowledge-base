# 企业级日志系统使用文档

> 提供统一、规范、高性能的日志记录能力  
> 版本：v1.0 | 更新时间：2025年10月10日 | 状态：✅ 生产就绪

---

## 📖 目录

- [快速开始](#-快速开始) - 30秒上手
- [系统概述](#-系统概述) - 了解核心特性和架构
- [核心组件](#-核心组件) - 详细的组件说明
- [使用指南](#-使用指南) - 从基础到高级的完整指南
- [配置说明](#-配置说明) - 环境配置和路径设置
- [故障排查](#-故障排查) - 常见问题诊断和解决
- [性能优化](#-性能优化) - 提升日志系统性能
- [日志分析](#-日志分析) - 实用技巧和工具集成
- [常见问题](#-常见问题) - FAQ
- [附录](#-附录) - 快速参考和资源链接

---

## 🚀 快速开始

### 30秒上手示例

```java
import com.lcx.api.logging.util.SensitiveDataMasker;
import com.lcx.api.logging.annotation.LogOperation;
import com.lcx.api.logging.enums.OperationTypeEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MyService {
    
    @LogOperation(
        module = "MY_MODULE",
        operation = OperationTypeEnum.QUERY,
        description = "查询数据"
    )
    public List<Data> queryData(String keyword) {
        // 敏感信息自动脱敏
        log.info("BIZ_BEGIN: op=queryData, keyword={}", 
                SensitiveDataMasker.mask(keyword));
        
        // 业务逻辑
        List<Data> result = repository.findByKeyword(keyword);
        
        log.info("BIZ_END: op=queryData, count={}", result.size());
        return result;
    }
}
```

**效果**：日志自动包含追踪ID、IP、执行时间等信息！

### 查看日志文件

```bash
# 日志位置
D:\workspace\Java\ai-knowledge-base\data\log\info.log

# 实时查看（Windows）
Get-Content data\log\info.log -Tail 50 -Wait

# 实时查看（Linux/Mac）
tail -f data/log/info.log
```

---

## 📋 系统概述

### 核心特性

| 特性 | 说明 | 收益 |
|------|------|------|
| **MDC上下文管理** | 自动传递追踪ID、用户信息 | 分布式追踪、问题定位 |
| **敏感信息脱敏** | 自动识别手机号、邮箱等 | 数据安全、合规 |
| **结构化日志** | JSON格式输出 | 便于分析、监控 |
| **AOP日志切面** | 注解驱动自动记录 | 代码简洁、统一规范 |
| **异步日志** | 异步队列输出 | 高性能、不阻塞业务 |
| **分级存储** | INFO/WARN/ERROR分文件 | 问题快速定位 |
| **自动归档** | 按日期归档压缩 | 节省存储、便于管理 |

### 模块架构

```
ai-knowledge-api (核心组件 - 共享)
├── logging/
│   ├── annotation/          # 日志注解
│   ├── context/             # MDC上下文管理
│   ├── dto/                 # 日志DTO
│   ├── enums/               # 枚举定义
│   └── util/                # 工具类

ai-knowledge-app (切面实现)
├── logging/aspect/          # AOP切面
└── resources/
    └── logback-spring.xml   # 日志配置
```

### 日志文件结构

```
data/log/
├── info.log                          # 当前INFO日志
├── warn.log                          # 当前WARN日志
├── error.log                         # 当前ERROR日志
└── archive/                          # 归档目录
    ├── info-2025-10-10.0.log.gz     # 自动压缩归档
    ├── warn-2025-10-10.0.log.gz
    └── error-2025-10-10.0.log.gz
```

---

## 🔧 核心组件

### 1. LogConstants - 日志常量

集中管理所有日志相关常量。

```java
// MDC上下文键
LogConstants.MdcKey.TRACE_ID        // 追踪ID
LogConstants.MdcKey.USER_ID         // 用户ID
LogConstants.MdcKey.CLIENT_IP       // 客户端IP
LogConstants.MdcKey.MODULE          // 业务模块
LogConstants.MdcKey.OPERATION       // 业务操作

// HTTP请求头
LogConstants.HttpHeader.TRACE_ID    // X-Trace-Id
LogConstants.HttpHeader.X_FORWARDED_FOR

// 日志类型
LogConstants.LogType.ACCESS         // 访问日志
LogConstants.LogType.BUSINESS       // 业务日志
LogConstants.LogType.OPERATION      // 操作日志
LogConstants.LogType.PERFORMANCE    // 性能日志

// 服务名称
LogConstants.ServiceName.APP        // ai-knowledge-app
```

### 2. LogContext - MDC上下文管理器

基于SLF4J的MDC实现，线程安全的上下文管理。

```java
// 初始化（Filter中自动调用）
LogContext.init();
LogContext.init(traceId);  // 使用指定的追踪ID

// 设置用户信息
LogContext.setUserId("user123");
LogContext.setUsername("张三");

// 设置请求信息
LogContext.setClientIp("192.168.1.100");
LogContext.setHttpMethod("POST");
LogContext.setRequestUri("/api/chat");

// 设置业务信息
LogContext.setModule("AI_CHAT");
LogContext.setOperation("GENERATE");

// 获取信息
Optional<String> traceId = LogContext.getTraceId();
Optional<String> userId = LogContext.getUserId();

// 清理上下文（重要！）
LogContext.clear();                 // 清理所有
LogContext.clearExceptCore();       // 保留traceId和serviceId
LogContext.remove("customKey");     // 移除指定键
```

### 3. SensitiveDataMasker - 敏感信息脱敏

提供多种敏感信息的脱敏方法，确保日志安全。

```java
// 手机号脱敏
SensitiveDataMasker.maskMobile("13812345678")       // → 138****5678

// 邮箱脱敏
SensitiveDataMasker.maskEmail("user@example.com")  // → use***@example.com

// 身份证脱敏
SensitiveDataMasker.maskIdCard("110101199001011234") // → 110101********1234

// 银行卡脱敏
SensitiveDataMasker.maskBankCard("6222021234567890") // → 6222********7890

// 密码脱敏
SensitiveDataMasker.maskPassword("password123")     // → ******

// 令牌脱敏
SensitiveDataMasker.maskToken("abcdef1234567890")   // → abcdef12...

// 姓名脱敏
SensitiveDataMasker.maskName("张三")                // → 张*

// 通用脱敏
SensitiveDataMasker.mask("sensitive_data")          // → sen***ata

// 自动检测并脱敏（推荐）
SensitiveDataMasker.autoMask(text)  // 自动识别手机号、邮箱、身份证
```

### 4. StructuredLogger - 结构化日志工具

提供结构化日志记录，支持JSON格式输出。

```java
// 访问日志
StructuredLogger.logAccess(AccessLogDTO.builder()
    .httpMethod("POST")
    .requestUri("/api/chat")
    .duration(120L)
    .statusCode(200)
    .success(true)
    .build());

// 业务日志 - 详细方式
StructuredLogger.logBusiness(BusinessLogDTO.builder()
    .module("AI_CHAT")
    .operation("GENERATE")
    .description("生成AI回复")
    .duration(1500L)
    .success(true)
    .build());

// 业务日志 - 简便方式
StructuredLogger.logSimpleSuccess("AI_CHAT", "GENERATE", "操作成功");
StructuredLogger.logSimpleFailure("AI_CHAT", "GENERATE", "操作失败", errorMsg);

// 方法日志
StructuredLogger.logMethodBegin("AI_CHAT", "generate", params);
StructuredLogger.logMethodEnd("AI_CHAT", "generate", duration);
StructuredLogger.logMethodError("AI_CHAT", "generate", error, exception);

// 性能日志
StructuredLogger.logPerformance(PerformanceLogDTO.builder()
    .checkpointName("数据库查询")
    .duration(500L)
    .timeout(false)
    .build());
```

### 5. 日志注解

#### @LogOperation - 操作日志注解

自动记录方法的执行情况、参数、返回值和异常。

```java
@LogOperation(
    module = "AI_CHAT",                          // 业务模块
    operation = OperationTypeEnum.AI_GENERATE,   // 操作类型
    description = "生成AI回复",                   // 操作描述
    logParams = true,                            // 记录参数（自动脱敏）
    logResult = false,                           // 记录返回值
    logException = true                          // 记录异常
)
public ChatResponse generate(String message) {
    // 方法实现
}
```

#### @LogPerformance - 性能日志注解

自动记录方法执行时间，超时告警。

```java
@LogPerformance(
    checkpointName = "AI生成",       // 监控点名称
    timeoutThreshold = 5000,         // 超时阈值（毫秒）
    logParams = false                // 是否记录参数
)
public String generateResponse(String prompt) {
    // 超过5秒会记录WARN日志
}
```

---

## 📖 使用指南

### 基础日志记录

#### 1. 标准日志格式

使用统一的日志格式，便于解析和分析：

```java
@Slf4j
@Service
public class UserService {
    
    public User createUser(UserRequest request) {
        // 操作开始
        log.info("BIZ_BEGIN: op=createUser, userId={}", request.getUserId());
        
        try {
            // 业务处理
            User user = userRepository.save(request);
            
            // 操作成功
            log.info("BIZ_SUCCESS: op=createUser, userId={}, duration={}ms", 
                    user.getId(), duration);
            return user;
            
        } catch (Exception e) {
            // 操作失败（包含完整堆栈）
            log.error("BIZ_ERROR: op=createUser, userId={}, reason={}", 
                    request.getUserId(), e.getMessage(), e);
            throw e;
        }
    }
}
```

#### 2. 日志级别规范

| 级别 | 使用场景 | 示例 | 生产环境 |
|------|---------|------|---------|
| **ERROR** | 系统错误、需要立即处理 | 数据库连接失败、外部服务不可用 | ✅ 记录 |
| **WARN** | 警告信息、潜在问题 | 参数验证失败、超时告警 | ✅ 记录 |
| **INFO** | 重要业务流程 | 用户登录、订单创建 | ✅ 记录 |
| **DEBUG** | 调试信息、详细过程 | 方法参数、中间结果 | ❌ 关闭 |
| **TRACE** | 最详细的追踪信息 | 循环内的详细步骤 | ❌ 关闭 |

**推荐配置**：
- 开发环境：DEBUG级别
- 生产环境：INFO级别

#### 3. 使用占位符（性能优化）

```java
// ❌ 错误：字符串拼接（总是执行）
log.info("User " + userId + " created order " + orderId);

// ✅ 正确：使用占位符（按需执行）
log.info("User {} created order {}", userId, orderId);

// ✅ 最佳：条件日志（避免不必要的计算）
if (log.isDebugEnabled()) {
    log.debug("Complex result: {}", expensiveCalculation());
}
```

### 敏感信息处理

#### 自动脱敏（推荐）

```java
String userInfo = "姓名：张三，手机：13812345678，邮箱：user@example.com";

// 自动检测并脱敏
String masked = SensitiveDataMasker.autoMask(userInfo);
log.info("User info: {}", masked);
// 输出: 姓名：张*，手机：138****5678，邮箱：use***@example.com
```

#### 手动脱敏

```java
// 根据数据类型选择合适的脱敏方法
log.info("登录: user={}, mobile={}, email={}", 
    SensitiveDataMasker.maskName(name),
    SensitiveDataMasker.maskMobile(mobile),
    SensitiveDataMasker.maskEmail(email));
```

### 使用注解简化日志

注解会自动记录方法的完整生命周期：

```java
@Service
@Slf4j
public class OrderService {
    
    @LogOperation(
        module = "ORDER",
        operation = OperationTypeEnum.CREATE,
        description = "创建订单",
        logParams = true,      // 记录参数（自动脱敏）
        logResult = false      // 不记录返回值（可能很大）
    )
    @LogPerformance(
        checkpointName = "创建订单",
        timeoutThreshold = 3000  // 超过3秒告警
    )
    public Order createOrder(OrderRequest request) {
        // 注解自动记录：
        // 1. 方法开始和参数
        // 2. 执行时间
        // 3. 是否超时
        // 4. 异常信息
        // 5. 方法结束
        
        return orderRepository.save(order);
    }
}
```

### MDC上下文使用

#### 在HTTP请求中（自动）

Filter自动设置，无需手动操作：
- ✅ traceId - 追踪ID
- ✅ serviceId - 服务ID  
- ✅ clientIp - 客户端IP
- ✅ httpMethod - HTTP方法
- ✅ requestUri - 请求URI

#### 手动设置业务信息

```java
public void processOrder(String orderId) {
    // 设置业务上下文
    LogContext.setModule("ORDER");
    LogContext.setOperation("PROCESS");
    LogContext.put("orderId", orderId);  // 自定义字段
    
    try {
        // 业务处理（日志自动包含上下文信息）
        log.info("开始处理订单");
        processOrderLogic(orderId);
        
    } finally {
        // 清理业务上下文（保留HTTP上下文）
        LogContext.remove("module");
        LogContext.remove("operation");
        LogContext.remove("orderId");
    }
}
```

### Reactive异步流中的日志⭐

**关键问题**：Reactive流是异步的，MDC不会自动传递到回调中！

#### 问题示例

```java
// ❌ 错误：MDC会丢失
public Flux<ChatResponse> generateStream(String message) {
    return chatModel.stream(prompt)
            .doOnComplete(() -> {
                // 此时MDC已被清除，trace=N/A ❌
                log.info("BIZ_END: op=generateStream");
            });
}
```

#### 正确实现：保存-恢复-清理模式

```java
// ✅ 正确：手动传递MDC
public Flux<ChatResponse> generateStream(String message) {
    // 步骤1️⃣：保存MDC上下文（在请求线程中）
    final Map<String, String> mdcContext = LogContext.getContext();
    final String traceId = LogContext.getTraceId().orElse("N/A");
    final String clientIp = LogContext.getClientIp().orElse("N/A");
    
    log.info("BIZ_BEGIN: op=generateStream, msgLen={}", message.length());
    
    return chatModel.stream(prompt)
            .doOnNext(chunk -> {
                // 步骤2️⃣：恢复MDC（在异步回调中）
                LogContext.setContext(mdcContext);
                try {
                    log.debug("Stream chunk received");
                } finally {
                    // 步骤3️⃣：清理MDC（防止线程池污染）
                    LogContext.clear();
                }
            })
            .doOnComplete(() -> {
                LogContext.setContext(mdcContext);
                try {
                    log.info("BIZ_END: op=generateStream, trace={}, ip={}", 
                            traceId, clientIp);
                } finally {
                    LogContext.clear();
                }
            })
            .doOnError(error -> {
                LogContext.setContext(mdcContext);
                try {
                    log.error("BIZ_ERROR: op=generateStream, trace={}", 
                            traceId, error);
                } finally {
                    LogContext.clear();
                }
            });
}
```

**要点说明**：
1. **保存**：在创建Flux前保存MDC和关键变量
2. **恢复**：在每个回调中恢复MDC
3. **清理**：在finally中清理，避免污染线程池
4. **变量**：保存关键变量供回调使用

### 跨线程异步调用

```java
public void asyncOperation() {
    // 保存当前线程的MDC
    final Map<String, String> mdcContext = LogContext.getContext();
    
    // 在线程池中执行
    executor.submit(() -> {
        // 恢复MDC
        LogContext.setContext(mdcContext);
        try {
            log.info("异步任务执行中");
            // 业务逻辑
        } finally {
            LogContext.clear();  // 必须清理
        }
    });
}
```

---

## 🎯 最佳实践

### 1. 异常日志记录

```java
// ❌ 错误：丢失堆栈信息
catch (Exception e) {
    log.error(e.getMessage());
}

// ❌ 错误：信息不足
catch (Exception e) {
    log.error("处理失败", e);
}

// ✅ 正确：包含上下文和完整堆栈
catch (Exception e) {
    log.error("BIZ_ERROR: op=processOrder, orderId={}, userId={}, reason={}", 
              orderId, userId, e.getMessage(), e);
}
```

### 2. 批量操作日志

```java
// ❌ 错误：循环内大量日志
for (int i = 0; i < 100000; i++) {
    log.info("Processing item {}", i);  // 产生10万条日志！
}

// ✅ 正确：适度记录进度
log.info("BIZ_BEGIN: op=processBatch, totalItems={}", items.size());
int processed = 0;

for (Item item : items) {
    processItem(item);
    processed++;
    
    // 每10%记录一次进度
    if (processed % (items.size() / 10) == 0) {
        log.info("BIZ_PROGRESS: processed={}/{} ({}%)", 
                processed, items.size(), (processed * 100 / items.size()));
    }
}

log.info("BIZ_END: op=processBatch, total={}, success={}", 
        items.size(), processed);
```

### 3. 外部服务调用日志

```java
public void callExternalService(String apiUrl) {
    log.info("EXTERNAL_CALL_BEGIN: service=GitHub, action=clone, url={}", apiUrl);
    
    long startTime = System.currentTimeMillis();
    try {
        // 调用外部服务
        Response response = httpClient.get(apiUrl);
        long duration = System.currentTimeMillis() - startTime;
        
        log.info("EXTERNAL_CALL_SUCCESS: service=GitHub, status={}, duration={}ms", 
                response.getStatus(), duration);
                
    } catch (Exception e) {
        long duration = System.currentTimeMillis() - startTime;
        log.error("EXTERNAL_CALL_ERROR: service=GitHub, url={}, duration={}ms, reason={}", 
                apiUrl, duration, e.getMessage(), e);
        throw e;
    }
}
```

### 4. 条件日志优化性能

```java
// ❌ 避免：总是执行复杂计算
log.debug("Result: " + complexCalculation());  // 即使DEBUG关闭也会执行

// ✅ 推荐：条件判断
if (log.isDebugEnabled()) {
    log.debug("Result: {}", complexCalculation());  // 只在DEBUG开启时执行
}

// ✅ 更好：使用占位符
log.debug("Result: {}", () -> complexCalculation());  // Lambda延迟执行
```

### 5. 避免大对象日志

```java
// ❌ 避免：记录整个对象（可能很大）
log.info("User object: {}", user);

// ✅ 推荐：只记录关键字段
log.info("User: id={}, name={}, status={}", 
        user.getId(), user.getName(), user.getStatus());

// ✅ 结构化日志：需要完整信息时
StructuredLogger.logBusiness(BusinessLogDTO.builder()
    .businessData(objectMapper.writeValueAsString(user))
    .build());
```

### 6. 定时任务日志

```java
@Scheduled(cron = "0 0 1 * * ?")
public void scheduledTask() {
    // 定时任务没有HTTP请求，需要手动初始化MDC
    LogContext.init();
    LogContext.setModule("SCHEDULED_TASK");
    
    try {
        log.info("TASK_BEGIN: name=cleanExpiredData");
        
        // 执行任务
        int count = cleanExpiredData();
        
        log.info("TASK_END: name=cleanExpiredData, cleanedCount={}", count);
        
    } catch (Exception e) {
        log.error("TASK_ERROR: name=cleanExpiredData", e);
    } finally {
        LogContext.clear();  // 必须清理
    }
}
```

### 7. 分页查询日志

```java
public Page<Order> queryOrders(PageRequest pageRequest) {
    log.info("BIZ_BEGIN: op=queryOrders, page={}, size={}", 
            pageRequest.getPage(), pageRequest.getSize());
    
    Page<Order> result = orderRepository.findAll(pageRequest);
    
    log.info("BIZ_END: op=queryOrders, page={}, size={}, total={}, totalPages={}", 
            result.getNumber(), result.getSize(), 
            result.getTotalElements(), result.getTotalPages());
    
    return result;
}
```

---

## ⚙️ 配置说明

### Logback配置文件

**位置**：`ai-knowledge-app/src/main/resources/logback-spring.xml`

#### 日志文件配置

| 文件 | 级别 | 单文件大小 | 保留时间 | 总容量 |
|------|------|-----------|---------|--------|
| `info.log` | INFO+ | 200MB | 30天 | 20GB |
| `warn.log` | WARN | 100MB | 30天 | 10GB |
| `error.log` | ERROR | 100MB | 90天 | 20GB |

#### 异步日志配置

```xml
<appender name="ASYNC_INFO" class="ch.qos.logback.classic.AsyncAppender">
    <discardingThreshold>0</discardingThreshold>     <!-- 不丢弃日志 -->
    <queueSize>10240</queueSize>                     <!-- 队列大小 -->
    <neverBlock>false</neverBlock>                   <!-- 确保不丢失 -->
    <includeCallerData>false</includeCallerData>     <!-- 性能优化 -->
    <appender-ref ref="INFO_FILE"/>
</appender>
```

### 环境配置

#### 开发环境（local）

```yaml
# application-local.yml
spring:
  profiles:
    active: local

logging:
  file:
    path: D:/workspace/Java/ai-knowledge-base/data/log  # 绝对路径
  level:
    com.lcx: DEBUG           # 详细日志
    org.springframework: WARN
```

#### 生产环境（pro）

```yaml
# application-pro.yml
spring:
  profiles:
    active: pro

logging:
  file:
    path: /var/log/ai-knowledge-app  # Linux标准路径
  level:
    com.lcx: INFO            # 只记录重要信息
    org.springframework: WARN
```

### 日志路径控制

日志文件位置由 `logging.file.path` 决定：

| 配置类型 | 配置值 | 生成位置 | 适用场景 |
|---------|--------|---------|---------|
| 相对路径 | `./data/log` | 依赖启动目录 | Docker部署 |
| 绝对路径 | `D:/logs/app` | 固定位置 | 开发环境⭐ |
| 环境变量 | `${LOG_PATH}` | 可配置 | 生产环境 |

**当前配置**：开发环境使用绝对路径 `D:/workspace/Java/ai-knowledge-base/data/log`

---

## 🚨 故障排查

### 问题诊断流程图

```
日志问题
    ├── 文件未生成？ → 检查路径权限、磁盘空间
    ├── 级别不生效？ → 检查配置优先级
    ├── MDC为N/A？ → 检查是否异步、是否手动传递
    ├── 文件过大？ → 检查循环日志、DEBUG级别
    └── 性能下降？ → 检查日志级别、大对象、异步配置
```

### 问题1：日志文件没有生成

**症状**：应用启动后，日志目录为空

**诊断步骤**：
```bash
# 1. 检查目录权限
ls -la ./data/log

# 2. 检查磁盘空间
df -h

# 3. 查看控制台是否有日志
# 如果控制台有日志，说明logback配置正常，只是文件输出有问题

# 4. 检查配置
grep "LOG_PATH" ai-knowledge-app/src/main/resources/logback-spring.xml
```

**常见原因**：
- 路径没有写权限
- logging.file.path配置错误
- 磁盘空间不足
- 文件被其他进程占用

**解决方法**：
```yaml
# 使用绝对路径避免路径错误
logging:
  file:
    path: D:/logs/ai-knowledge-app
```

### 问题2：日志级别不生效

**症状**：设置了DEBUG级别，但看不到DEBUG日志

**配置优先级**（从高到低）：
1. 启动参数：`--logging.level.com.lcx=DEBUG`
2. 环境变量：`LOGGING_LEVEL_COM_LCX=DEBUG`
3. application-{profile}.yml
4. application.yml
5. logback-spring.xml

**诊断方法**：
```bash
# 通过Actuator查看实际级别
curl http://localhost:8080/actuator/loggers/com.lcx

# 响应示例
{
  "configuredLevel": "DEBUG",
  "effectiveLevel": "DEBUG"
}
```

**解决方法**：
```yaml
# 确保所有配置文件一致
logging:
  level:
    root: INFO
    com.lcx: DEBUG  # 包名正确
```

### 问题3：MDC信息为N/A

**症状**：日志中显示 `[trace=N/A user=N/A ip=N/A]`

**常见场景**：

| 场景 | 原因 | 解决方法 |
|------|------|---------|
| Reactive流回调 | 异步执行，MDC已清除 | 手动传递MDC（见上文） |
| 新线程 | ThreadLocal不跨线程 | 手动传递MDC |
| 定时任务 | 没有HTTP上下文 | 手动初始化MDC |
| @Async方法 | 异步执行 | 使用TaskDecorator传递 |

**Reactive流解决方案**：
```java
// 保存-恢复-清理模式（见"使用指南"章节）
final Map<String, String> mdc = LogContext.getContext();
// 在回调中恢复...
```

**定时任务解决方案**：
```java
@Scheduled(...)
public void task() {
    LogContext.init();  // 手动初始化
    try {
        // 业务逻辑
    } finally {
        LogContext.clear();
    }
}
```

### 问题4：日志文件过大

**症状**：日志文件迅速增长到GB级别

**排查清单**：
- [ ] 是否有循环内的日志？
- [ ] 是否开启了DEBUG级别？
- [ ] 是否记录了大对象？
- [ ] 是否有异常堆栈过多？

**诊断命令**：
```bash
# 查找日志最多的类
grep -o 'INFO.*\[.*\]' info.log | awk '{print $3}' | sort | uniq -c | sort -rn | head -20

# 统计日志增长速度（每秒多少条）
wc -l info.log && sleep 10 && wc -l info.log
```

**解决方案**：
```java
// 1. 关闭不必要的DEBUG日志
logging.level.com.lcx: INFO

// 2. 优化循环日志
for (int i = 0; i < total; i++) {
    // ❌ 每条都记录
    // log.info("Processing {}", i);
    
    // ✅ 按比例记录
    if (i % 1000 == 0 || i == total - 1) {
        log.info("Progress: {}/{}", i, total);
    }
}

// 3. 限制对象大小
log.info("User: {}", user.getId());  // 只记录ID，不记录整个对象
```

### 问题5：性能下降

**症状**：启用日志后，应用性能明显下降

**性能检查**：
```java
// 检查点1：日志级别
// 生产环境应该是INFO，不是DEBUG

// 检查点2：是否启用异步
// logback-spring.xml应该使用ASYNC_INFO等异步appender

// 检查点3：是否记录大对象
// 避免序列化大对象

// 检查点4：是否使用了字符串拼接
log.info("User " + user);  // ❌ 总是执行
log.info("User {}", user); // ✅ 按需执行
```

**优化方案**：参见"性能优化"章节

---

## 🔧 性能优化

### 1. 日志级别调优

```yaml
# 开发环境：详细日志
logging:
  level:
    com.lcx: DEBUG
    
# 生产环境：精简日志
logging:
  level:
    root: WARN                  # 根级别WARN
    com.lcx: INFO               # 业务代码INFO
    org.springframework: WARN   # Spring框架WARN
    org.hibernate: WARN         # Hibernate WARN
```

### 2. 异步日志优化

```xml
<!-- 根据业务调整队列大小 -->
<appender name="ASYNC_INFO" class="ch.qos.logback.classic.AsyncAppender">
    <queueSize>10240</queueSize>           <!-- 高并发：增大队列 -->
    <neverBlock>false</neverBlock>         <!-- false：不丢失日志 -->
    <includeCallerData>false</includeCallerData>  <!-- false：提升性能 -->
</appender>
```

### 3. 减少不必要的日志

```java
// 优化前：过多日志
public List<User> getUsers() {
    log.debug("开始查询用户");
    log.debug("构建查询条件");
    log.debug("执行数据库查询");
    List<User> users = repository.findAll();
    log.debug("查询完成，结果数：{}", users.size());
    return users;
}

// 优化后：精简日志
public List<User> getUsers() {
    List<User> users = repository.findAll();
    log.info("BIZ: op=getUsers, count={}", users.size());
    return users;
}
```

### 4. 使用日志采样

```java
// 高频操作，采样记录
private final AtomicLong counter = new AtomicLong(0);

public void highFrequencyOperation() {
    long count = counter.incrementAndGet();
    
    // 每100次记录一次
    if (count % 100 == 0) {
        log.info("HIGH_FREQ_OP: count={}", count);
    }
}
```

### 5. 限制日志大小

```java
// 长字符串截断
private String truncate(String str, int maxLength) {
    if (str == null || str.length() <= maxLength) {
        return str;
    }
    return str.substring(0, maxLength) + "...[truncated]";
}

log.info("Response: {}", truncate(response, 1000));
```

---

## 📊 日志分析

### 实用查询技巧

#### 1. 按追踪ID查找完整请求链路

```bash
# 方法1：grep
grep "trace=d4f584faa9584bb" info.log | sort

# 方法2：PowerShell
Select-String -Path data\log\info.log -Pattern "trace=d4f584faa9584bb" | 
    Sort-Object LineNumber

# 方法3：按时间过滤
grep "trace=d4f584faa9584bb" info.log | 
    grep "2025-10-10 12:"
```

#### 2. 分析性能问题

```bash
# 查找耗时超过3秒的操作
grep "duration=" info.log | 
    awk -F'duration=' '{print $2}' | 
    awk -F'ms' '{if($1>3000) print $0}'

# 查找超时的操作
grep "PERFORMANCE_LOG.*timeout.*true" info.log

# 统计平均响应时间
grep "REQUEST_END" info.log | 
    awk -F'duration=' '{sum+=$2; count++} END {print sum/count "ms"}'
```

#### 3. 错误分析

```bash
# 统计错误类型
grep "BIZ_ERROR" error.log | 
    awk -F'op=' '{print $2}' | 
    awk '{print $1}' | 
    sort | uniq -c | sort -rn

# 查找今天的错误
grep "$(date +%Y-%m-%d)" error.log

# 按IP统计错误
grep "ERROR" error.log | 
    grep -o "ip=[0-9.]*" | 
    sort | uniq -c | sort -rn
```

#### 4. 用户行为分析

```bash
# 查找特定用户的所有操作
grep "user=user123" info.log | sort

# 统计用户操作频率
grep "user=user123" info.log | 
    grep "BIZ_BEGIN" | 
    awk -F'op=' '{print $2}' | 
    awk '{print $1}' | 
    sort | uniq -c
```

### 日志分析工具集成

#### ELK Stack（Elasticsearch + Logstash + Kibana）

```yaml
# filebeat.yml
filebeat.inputs:
  - type: log
    enabled: true
    paths:
      - /var/log/ai-knowledge-app/*.log
    fields:
      app: ai-knowledge-app
      env: production
    multiline:
      pattern: '^\d{4}-\d{2}-\d{2}'
      negate: true
      match: after
    
output.elasticsearch:
  hosts: ["localhost:9200"]
  index: "ai-knowledge-app-%{+yyyy.MM.dd}"
```

#### Grafana Loki

```yaml
# promtail-config.yml
scrape_configs:
  - job_name: ai-knowledge-app
    static_configs:
      - targets:
          - localhost
        labels:
          app: ai-knowledge-app
          env: production
          __path__: /var/log/ai-knowledge-app/*.log
```

#### 使用LogQL查询（Loki）

```logql
# 查找错误日志
{app="ai-knowledge-app"} |= "ERROR"

# 按追踪ID查询
{app="ai-knowledge-app"} |= "trace=d4f584faa9584bb"

# 查询耗时超过3秒的请求
{app="ai-knowledge-app"} |= "REQUEST_END" | 
    json | duration > 3000
```

---

## ❓ 常见问题

### Q1: 如何在日志中查看追踪ID？

**A**: 追踪ID自动包含在日志的MDC部分：

```
2025-10-10 12:00:45.123 [http-nio-8080-exec-1] INFO  c.l.t.s.RagServiceImpl 
[trace=d4f584faa9584bb|user=user123|ip=192.168.1.100] | BIZ_BEGIN: op=uploadFile
         ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
        这就是追踪ID
```

**用途**：
- 前后端请求关联
- 分布式服务追踪
- 问题定位和重现

### Q2: 如何关联前后端请求？

**A**: 通过HTTP请求头传递追踪ID

**前端**：
```javascript
// 生成或获取追踪ID
const traceId = generateTraceId();

fetch('/api/endpoint', {
    headers: {
        'X-Trace-Id': traceId
    }
});
```

**后端**：
- 自动从请求头读取 `X-Trace-Id`
- 如果没有则自动生成
- 在响应头中返回 `X-Trace-Id`

### Q3: 如何调整日志级别？

**A**: 三种方式

**方式1：配置文件（永久）**
```yaml
# application-local.yml
logging:
  level:
    com.lcx.trigger.service: DEBUG  # 特定包
    com.lcx: INFO                   # 整个模块
```

**方式2：Actuator（运行时）**
```bash
# 查看当前级别
curl http://localhost:8080/actuator/loggers/com.lcx

# 修改级别
curl -X POST http://localhost:8080/actuator/loggers/com.lcx \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'
```

**方式3：启动参数（临时）**
```bash
java -jar app.jar --logging.level.com.lcx=DEBUG
```

### Q4: 流式响应中MDC为什么是N/A？⭐

**A**: Reactive流是异步的，MDC不会自动传递

**问题示例**：
```java
// ❌ 错误
return chatModel.stream(prompt)
    .doOnComplete(() -> log.info("完成"));  // MDC已清除
```

**解决方案**：使用**保存-恢复-清理模式**
```java
// ✅ 正确
final Map<String, String> mdc = LogContext.getContext();
return chatModel.stream(prompt)
    .doOnComplete(() -> {
        LogContext.setContext(mdc);
        try {
            log.info("完成");
        } finally {
            LogContext.clear();
        }
    });
```

详见"使用指南 → Reactive异步流中的日志"章节。

### Q5: 如何查看日志归档？

**A**: 归档文件自动压缩存储

**位置**：
```
./data/log/archive/
├── info-2025-10-10.0.log.gz
├── warn-2025-10-10.0.log.gz
└── error-2025-10-10.0.log.gz
```

**查看方法**：
```bash
# Windows - 解压后查看
Expand-Archive archive\info-2025-10-10.0.log.gz

# Linux - 直接查看（不解压）
zcat archive/info-2025-10-10.0.log.gz | less
zgrep "keyword" archive/info-2025-10-10.0.log.gz
```

### Q6: 如何定位生产环境问题？

**A**: 使用追踪ID进行全链路追踪

**步骤1**：获取错误追踪ID
```bash
# 从错误日志查找
grep "ERROR" error.log | head -1
# 提取trace ID（例如：trace=d4f584faa9584bb）
```

**步骤2**：搜索完整调用链
```bash
grep "trace=d4f584faa9584bb" info.log | sort
```

**步骤3**：分析时间线
```
12:00:45.123 | REQUEST_BEGIN
12:00:45.150 | BIZ_BEGIN: op=generateStreamRag
12:00:45.200 | BIZ_INFO: op=ragSearch, docs=5
12:00:47.500 | BIZ_END: op=generateStreamRag
12:00:47.520 | REQUEST_END: duration=2397ms
```

**步骤4**：查找异常详情
```bash
grep "trace=d4f584faa9584bb" error.log
```

### Q7: 异步日志会丢失吗？

**A**: 正常情况下不会丢失

**配置保证**：
- `neverBlock=false` - 队列满时会阻塞，确保不丢失
- `queueSize=10240` - 足够大的队列
- `discardingThreshold=0` - 不丢弃任何级别的日志

**极端情况**：
- 应用突然crash - 队列中未写入的日志可能丢失
- 建议：关键操作使用同步日志

### Q8: 如何监控日志系统健康度？

**A**: 监控指标

```bash
# 1. 日志文件大小
ls -lh data/log/*.log

# 2. 归档文件数量
ls -1 data/log/archive/ | wc -l

# 3. 错误日志增长
tail -f error.log | grep "ERROR" | wc -l

# 4. 磁盘使用率
du -sh data/log/
```

**告警阈值建议**：
- 单文件 > 1GB：检查日志级别
- 错误率 > 5%：检查系统稳定性
- 磁盘使用 > 80%：清理归档或增加磁盘

### Q9: 如何在Docker中使用？

**A**: 挂载卷映射日志目录

```yaml
# docker-compose.yml
services:
  ai-knowledge-app:
    image: ai-knowledge-app:latest
    volumes:
      - ./logs:/var/log/ai-knowledge-app  # 映射日志目录
    environment:
      - LOGGING_FILE_PATH=/var/log/ai-knowledge-app
```

```dockerfile
# Dockerfile
ENV LOGGING_FILE_PATH=/var/log/ai-knowledge-app
VOLUME /var/log/ai-knowledge-app
```

---

## 📚 附录

### A. 快速参考

#### 常用导入

```java
// 上下文管理
import com.lcx.api.logging.context.LogContext;

// 工具类
import com.lcx.api.logging.util.SensitiveDataMasker;
import com.lcx.api.logging.util.StructuredLogger;

// 注解
import com.lcx.api.logging.annotation.LogOperation;
import com.lcx.api.logging.annotation.LogPerformance;

// 枚举
import com.lcx.api.logging.enums.OperationTypeEnum;
import com.lcx.api.logging.enums.BusinessModuleEnum;

// DTO
import com.lcx.api.logging.dto.*;
```

#### 日志格式模板

```java
// 业务操作
log.info("BIZ_BEGIN: op={}, param={}", operation, param);
log.info("BIZ_SUCCESS: op={}, result={}, duration={}ms", op, result, duration);
log.info("BIZ_ERROR: op={}, reason={}", operation, error);

// 外部调用
log.info("EXTERNAL_CALL: service={}, action={}, url={}", service, action, url);

// 性能监控
log.warn("PERFORMANCE: op={}, duration={}ms, threshold={}ms", op, dur, threshold);

// 请求日志
log.info("REQUEST_BEGIN: method={}, uri={}, ip={}", method, uri, ip);
log.info("REQUEST_END: status={}, duration={}ms", status, duration);
```

#### MDC字段完整列表

| MDC键 | 类型 | 说明 | 设置位置 | 示例值 |
|-------|------|------|---------|--------|
| `traceId` | 核心 | 追踪ID | RequestLoggingFilter | d4f584faa9584bb |
| `serviceId` | 核心 | 服务ID | LogContext.init() | ai-knowledge-app |
| `userId` | 业务 | 用户ID | 业务代码 | user123 |
| `username` | 业务 | 用户名 | 业务代码 | 张三 |
| `clientIp` | 请求 | 客户端IP | RequestLoggingFilter | 192.168.1.100 |
| `httpMethod` | 请求 | HTTP方法 | RequestLoggingFilter | POST |
| `requestUri` | 请求 | 请求URI | RequestLoggingFilter | /api/chat |
| `sessionId` | 会话 | 会话ID | 业务代码 | SESSION_123 |
| `tenantId` | 租户 | 租户ID | 业务代码 | TENANT_001 |
| `module` | 业务 | 业务模块 | 业务代码 | AI_CHAT |
| `operation` | 业务 | 业务操作 | 业务代码 | GENERATE |

### B. 操作类型枚举

| 枚举值 | 代码 | 说明 | 使用场景 |
|--------|------|------|---------|
| QUERY | `OperationTypeEnum.QUERY` | 查询操作 | 查询列表、详情 |
| CREATE | `OperationTypeEnum.CREATE` | 创建操作 | 新增数据 |
| UPDATE | `OperationTypeEnum.UPDATE` | 更新操作 | 修改数据 |
| DELETE | `OperationTypeEnum.DELETE` | 删除操作 | 删除数据 |
| UPLOAD | `OperationTypeEnum.UPLOAD` | 上传操作 | 文件上传 |
| DOWNLOAD | `OperationTypeEnum.DOWNLOAD` | 下载操作 | 文件下载 |
| AI_GENERATE | `OperationTypeEnum.AI_GENERATE` | AI生成 | AI对话 |
| RAG_SEARCH | `OperationTypeEnum.RAG_SEARCH` | RAG检索 | 知识库检索 |
| GIT_ANALYZE | `OperationTypeEnum.GIT_ANALYZE` | Git分析 | 仓库分析 |

### C. 业务模块枚举

| 枚举值 | 代码 | 说明 |
|--------|------|------|
| AI_CHAT | `BusinessModuleEnum.AI_CHAT` | AI对话模块 |
| RAG | `BusinessModuleEnum.RAG` | RAG检索模块 |
| FILE | `BusinessModuleEnum.FILE` | 文件管理模块 |
| GIT | `BusinessModuleEnum.GIT` | Git仓库模块 |
| SYSTEM | `BusinessModuleEnum.SYSTEM` | 系统管理模块 |
| USER | `BusinessModuleEnum.USER` | 用户管理模块 |

### D. 日志输出示例

#### 控制台输出（彩色）

```
2025-10-10 12:00:45.123 [http-nio-8080-1] INFO  c.l.t.s.OpenAiServiceImpl 
[trace=d4f584faa9584bb|user=N/A|ip=127.0.0.1] | BIZ_BEGIN: op=generateStream
```

**颜色说明**：
- 🔴 **ERROR** - 红色粗体（醒目）
- 🟡 **WARN** - 黄色（警示）
- 🟢 **INFO** - 绿色（正常）
- 🔵 **DEBUG** - 青色（调试）
- ⚪ **TRACE** - 默认色（追踪）

#### 文件输出（结构化）

**标准格式**：
```
2025-10-10 12:00:45.123 [http-nio-8080-exec-1] INFO  com.lcx.trigger.service.OpenAiServiceImpl 
[trace=d4f584faa9584bb|user=user123|ip=192.168.1.100|module=AI_CHAT|op=GENERATE] 
- BIZ_BEGIN: op=generateStream, model=qwen3-max, msgLen=50
```

**JSON格式**（结构化日志）：
```json
{
  "timestamp": "2025-10-10 12:00:45.123",
  "level": "INFO",
  "thread": "http-nio-8080-exec-1",
  "logger": "com.lcx.trigger.service.OpenAiServiceImpl",
  "mdc": {
    "traceId": "d4f584faa9584bb",
    "serviceId": "ai-knowledge-app",
    "userId": "user123",
    "clientIp": "192.168.1.100",
    "module": "AI_CHAT",
    "operation": "GENERATE"
  },
  "message": "BIZ_BEGIN: op=generateStream, model=qwen3-max, msgLen=50"
}
```

### E. 学习路径

#### 入门（必读）
1. ✅ 快速开始 → 30秒上手
2. ✅ 系统概述 → 了解核心特性
3. ✅ 核心组件 → LogContext、SensitiveDataMasker
4. ✅ 使用指南 → 基础日志记录、敏感信息处理

#### 进阶（推荐）
1. ✅ 使用指南 → 注解使用、MDC上下文
2. ✅ 使用指南 → Reactive异步流日志⭐
3. ✅ 配置说明 → 环境配置、路径控制
4. ✅ 故障排查 → 常见问题解决

#### 专家（深入）
1. ✅ 性能优化 → 全部章节
2. ✅ 日志分析 → 查询技巧、工具集成
3. ✅ 常见问题 → 所有FAQ
4. ✅ 源码学习 → 查看实现细节

### F. 检查清单

在生产环境部署前，请确认：

**配置检查**
- [ ] 日志级别设置为INFO（不是DEBUG）
- [ ] 日志路径使用绝对路径或环境变量
- [ ] 配置了合理的日志保留天数
- [ ] 启用了异步日志
- [ ] 设置了合理的单文件大小限制

**代码检查**
- [ ] 敏感信息都已脱敏
- [ ] 没有循环内的日志
- [ ] 异常日志包含完整堆栈
- [ ] Reactive流中正确传递MDC
- [ ] 使用了占位符而非字符串拼接

**运维检查**
- [ ] 日志目录有足够磁盘空间
- [ ] 应用有日志目录写权限
- [ ] 配置了日志监控告警
- [ ] 规划了日志归档备份策略

### G. 相关资源

**代码位置**：
- 核心组件：`ai-knowledge-api/src/main/java/com/lcx/api/logging/`
- 切面实现：`ai-knowledge-app/src/main/java/com/lcx/app/logging/aspect/`
- 配置文件：`ai-knowledge-app/src/main/resources/logback-spring.xml`

**示例代码**：
- `RagServiceImpl.java` - 文件上传、Git仓库分析
- `OpenAiServiceImpl.java` - AI生成、Reactive流式响应
- `RequestLoggingFilter.java` - HTTP请求日志
- `GlobalExceptionHandler.java` - 异常日志

**官方文档**：
- [Logback官方文档](http://logback.qos.ch/)
- [SLF4J用户手册](http://www.slf4j.org/manual.html)
- [Spring Boot日志](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging)
- [Reactor上下文](https://projectreactor.io/docs/core/release/reference/#context)

### H. 版本历史

#### v1.0  (2025-10-10) - 企业级版本
- ✅ MDC上下文管理（LogContext）
- ✅ 结构化日志支持（StructuredLogger）
- ✅ 敏感信息脱敏（SensitiveDataMasker）
- ✅ AOP日志切面（@LogOperation、@LogPerformance）
- ✅ 分级日志存储（info/warn/error）
- ✅ 自动归档压缩
- ✅ 迁移到api模块实现共享
- ✅ 修复Reactive流MDC传递问题
- ✅ 优化日志格式和颜色显示
- ✅ 完善文档和示例

---

## 🤝 贡献和支持

### 反馈渠道
- **Issue追踪**：项目Issue系统
- **技术讨论**：开发团队群组
- **文档改进**：提交Pull Request

### 技术支持
- **开发团队**：ZX-007个人
- **维护负责人**：ZX-007
- **更新频率**：持续更新

---

## 🎓 最后的话

### 核心原则

1. **简洁明了**：日志应该清晰、有意义
2. **安全第一**：敏感信息必须脱敏
3. **性能优先**：避免过度日志影响性能
4. **便于追踪**：使用追踪ID关联请求
5. **结构化**：便于后续分析和监控

### 黄金法则

- ✅ 使用占位符，不用字符串拼接
- ✅ 敏感信息必须脱敏
- ✅ 异常必须包含堆栈
- ✅ Reactive流手动传递MDC
- ✅ 适度记录，避免日志泛滥

### 推荐实践

```java
// 这是一个完美的日志示例 ⭐
@Slf4j
@Service
public class OrderService {
    
    @LogOperation(
        module = "ORDER",
        operation = OperationTypeEnum.CREATE,
        description = "创建订单"
    )
    @LogPerformance(timeoutThreshold = 3000)
    public Order createOrder(OrderRequest request) {
        String maskedUserId = SensitiveDataMasker.mask(request.getUserId());
        log.info("BIZ_BEGIN: op=createOrder, user={}, amount={}", 
                maskedUserId, request.getAmount());
        
        try {
            Order order = orderRepository.save(request);
            log.info("BIZ_SUCCESS: op=createOrder, orderId={}", order.getId());
            return order;
        } catch (Exception e) {
            log.error("BIZ_ERROR: op=createOrder, user={}, reason={}", 
                    maskedUserId, e.getMessage(), e);
            throw e;
        }
    }
}
```

---

**Happy Logging!** 📝✨

*让日志成为你的最佳助手，而不是负担。*
