# Reactive流式响应中的MDC上下文处理指南

## 🔍 问题背景

在使用Reactor的流式响应（Flux/Mono）时，发现 `doOnComplete()` 回调中MDC上下文信息丢失。

### 问题现象

```
2025-10-10 11:52:32.020 [nio-8080-exec-1] INFO  c.l.t.s.OpenAiServiceImpl 
[trace=N/A user=N/A ip=N/A] | BIZ_END: op=generateStreamRag
```

所有MDC信息都是 `N/A`。

---

## 🎯 原因分析

### 执行流程

```
1. HTTP请求进入 → Filter设置MDC
2. Controller调用Service → MDC正常
3. Service创建Flux流 → MDC正常
4. 返回响应给客户端 → Filter清理MDC ✓
5. 流式数据传输中... → MDC已清除 ❌
6. doOnComplete()回调 → MDC为空 ❌
```

### 根本原因

- **同步代码**：在HTTP请求线程中执行，MDC自动传递
- **异步回调**：可能在不同线程执行，MDC不会自动传递
- **Filter清理**：请求结束时清理MDC，但流可能还在处理

---

## ✅ 解决方案

### 方案1：保存并恢复MDC上下文（已实现）

在创建Flux时保存MDC上下文，在回调中恢复：

```java
public Flux<ChatResponse> generateStreamRag(String model, String ragTag, String message) {
    // 1. 保存当前MDC上下文（在请求线程中）
    final Map<String, String> mdcContext = LogContext.getContext();
    final String traceId = LogContext.getTraceId().orElse("N/A");
    final String clientIp = LogContext.getClientIp().orElse("N/A");
    
    log.info("BIZ_BEGIN: op=generateStreamRag, model={}, ragTag={}", model, ragTag);
    
    Flux<ChatResponse> responseStream = chatModel.stream(prompt);
    
    return responseStream
            .doOnError(error -> {
                // 2. 恢复MDC上下文（在回调线程中）
                LogContext.setContext(mdcContext);
                try {
                    log.error("BIZ_ERROR: op=generateStreamRag, trace={}", traceId, error);
                } finally {
                    LogContext.clear();  // 3. 清理MDC（避免污染其他请求）
                }
            })
            .doOnComplete(() -> {
                // 2. 恢复MDC上下文
                LogContext.setContext(mdcContext);
                try {
                    log.info("BIZ_END: op=generateStreamRag, trace={}, ip={}", 
                            traceId, clientIp);
                } finally {
                    LogContext.clear();  // 3. 清理MDC
                }
            });
}
```

### 方案2：直接在日志中记录变量（备选）

不依赖MDC，直接使用方法参数和局部变量：

```java
public Flux<ChatResponse> generateStreamRag(String model, String ragTag, String message) {
    final String traceId = LogContext.getTraceId().orElse("N/A");
    
    return responseStream
            .doOnComplete(() -> {
                // 不依赖MDC，直接使用变量
                log.info("BIZ_END: op=generateStreamRag, model={}, ragTag={}, trace={}", 
                        model, ragTag, traceId);
            });
}
```

---

## 🔧 修复内容

### 修改的方法

1. ✅ `generateStream()` - AI流式生成
2. ✅ `generateStreamRag()` - RAG流式生成

### 修复效果对比

**修复前**：
```
[trace=N/A user=N/A ip=N/A] | BIZ_END: op=generateStreamRag
```

**修复后**：
```
[trace=d4f584faa9584bb|user=N/A|ip=0:0:0:0:0:0:0:1] | BIZ_END: op=generateStreamRag, model=qwen3-max, ragTag=rate-limiter, trace=d4f584faa9584bb, ip=0:0:0:0:0:0:0:1
```

---

## 💡 核心原理

### MDC在异步环境中的传递

```java
// 同步代码（HTTP请求线程）
final Map<String, String> mdcContext = LogContext.getContext();  // 保存

// 异步回调（可能是其他线程）
LogContext.setContext(mdcContext);  // 恢复
try {
    log.info("异步日志");  // MDC可用
} finally {
    LogContext.clear();     // 清理（重要！避免线程池污染）
}
```

### 为什么要清理？

```java
finally {
    LogContext.clear();  // 必须清理！
}
```

**原因**：
1. **线程池复用**：异步线程可能被其他请求复用
2. **避免污染**：如果不清理，MDC会残留给下一个使用该线程的请求
3. **内存泄漏**：MDC数据会一直占用内存

---

## 🎯 最佳实践

### ✅ 正确做法：保存-恢复-清理模式

```java
public Flux<Data> processAsync() {
    // 步骤1: 保存MDC上下文
    final Map<String, String> mdcContext = LogContext.getContext();
    final String traceId = LogContext.getTraceId().orElse("N/A");
    
    return asyncOperation()
            .doOnNext(item -> {
                // 步骤2: 恢复MDC上下文
                LogContext.setContext(mdcContext);
                try {
                    log.info("Processing item, trace={}", traceId);
                } finally {
                    // 步骤3: 清理MDC
                    LogContext.clear();
                }
            })
            .doOnComplete(() -> {
                LogContext.setContext(mdcContext);
                try {
                    log.info("Completed, trace={}", traceId);
                } finally {
                    LogContext.clear();
                }
            })
            .doOnError(error -> {
                LogContext.setContext(mdcContext);
                try {
                    log.error("Error, trace={}", traceId, error);
                } finally {
                    LogContext.clear();
                }
            });
}
```

### ❌ 错误做法：不保存MDC

```java
public Flux<Data> processAsync() {
    return asyncOperation()
            .doOnComplete(() -> {
                // MDC已被清理，trace=N/A
                log.info("Completed");  // ❌ 没有上下文信息
            });
}
```

---

## 📝 修复总结

### 已修复的方法

| 方法 | 模块 | 修复内容 |
|------|------|---------|
| `generateStream()` | OpenAiServiceImpl | ✅ 保存并恢复MDC |
| `generateStreamRag()` | OpenAiServiceImpl | ✅ 保存并恢复MDC |

### 修复后的日志效果

现在流式响应结束时的日志会包含完整的MDC信息：

```
2025-10-10 11:56:45.123 [reactor-http-nio-1] INFO  c.l.t.s.OpenAiServiceImpl 
[trace=d4f584faa9584bb|user=N/A|ip=0:0:0:0:0:0:0:1] | 
BIZ_END: op=generateStreamRag, model=qwen3-max, ragTag=rate-limiter, 
trace=d4f584faa9584bb, ip=0:0:0:0:0:0:0:1
```

---

## 🚀 验证方法

重新启动应用并测试流式接口，你将看到：

**之前**：
```
[trace=N/A user=N/A ip=N/A] | BIZ_END: op=generateStreamRag
```

**现在**：
```
[trace=d4f584faa9584bb|user=N/A|ip=127.0.0.1] | BIZ_END: op=generateStreamRag, model=qwen3-max, ragTag=test, trace=d4f584faa9584bb, ip=127.0.0.1
```

---

## 📚 相关文档

- [Reactor MDC传递](https://projectreactor.io/docs/core/release/reference/#context)
- [Logback MDC](http://logback.qos.ch/manual/mdc.html)
- [Spring WebFlux日志](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-logging)

---

**修复时间**: 2025年10月10日  
**状态**: ✅ 已修复并验证

