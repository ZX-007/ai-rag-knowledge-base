# 异常处理机制使用指南



## 概述

本项目集成了规范的异常处理机制，提供了统一的异常分类、处理和响应格式。通过全局异常处理器，所有异常都会被统一捕获并转换为标准的响应格式。



## 异常处理架构

### 1. 异常类层次结构

```
BaseException (基础异常类)
├── BusinessException (业务异常)
├── ParameterException (参数异常)
└── SystemException (系统异常)
```

### 2. 全局异常处理器

`GlobalExceptionHandler` 负责统一处理所有异常，包括：

- 自定义业务异常
- 参数验证异常
- 系统异常
- Spring框架异常
- 其他未知异常



## 异常类型说明

### BaseException - 基础异常类

所有自定义异常的基类，包含：
- `code`: 错误码
- `message`: 错误消息
- `details`: 详细错误信息（可选）

**使用示例：**
```java
// 使用错误码和消息创建异常
throw new BaseException("5001", "数据库连接失败");

// 使用ResponseCode枚举创建异常
throw new BaseException(ResponseCode.DATABASE_ERROR);

// 带详细信息的异常
throw new BaseException(ResponseCode.DATABASE_ERROR, "连接超时", cause);
```

### BusinessException - 业务异常

用于处理业务逻辑相关的异常情况。

**使用示例：**
```java
// 业务规则验证失败
throw new BusinessException(ResponseCode.PARAM_ERROR, "用户不存在");

// 业务操作失败
throw new BusinessException("4001", "操作失败", "具体失败原因");
```

### ParameterException - 参数异常

专门用于处理参数验证相关的异常。

**使用示例：**
```java
// 参数为空
throw ParameterException.paramNull("userId");

// 参数格式错误
throw ParameterException.paramFormatError("email", "邮箱格式");

// 参数值超出范围
throw ParameterException.paramOutOfRange("age", 0, 120);

// 自定义参数异常
throw new ParameterException("参数验证失败");
```

### SystemException - 系统异常

用于处理系统级别的异常，如数据库连接失败、外部服务不可用等。

**使用示例：**
```java
// 数据库异常
throw SystemException.databaseError("数据库连接失败", cause);

// Redis异常
throw SystemException.redisError("Redis连接失败", cause);

// AI服务异常
throw SystemException.aiServiceError("AI服务不可用", cause);

// 向量数据库异常
throw SystemException.vectorDbError("向量数据库操作失败", cause);

// 文件上传异常
throw SystemException.fileUploadError("文件上传失败", cause);
```



## 控制器中的异常处理

### 1. 参数验证

在控制器方法开始处进行参数验证：

```java
@RequestMapping(value = "/upload", method = RequestMethod.POST)
public Response<String> uploadFile(@RequestParam String ragTag, 
                                 @RequestParam("file") List<MultipartFile> files) {
    // 参数验证
    if (ragTag == null || ragTag.trim().isEmpty()) {
        throw ParameterException.paramNull("ragTag");
    }
    if (files == null || files.isEmpty()) {
        throw ParameterException.paramNull("files");
    }
    
    // 业务逻辑处理
    try {
        String result = ragService.uploadFile(ragTag, files);
        return Response.success(result);
    } catch (Exception e) {
        log.error("文件上传失败", e);
        throw SystemException.fileUploadError("文件上传失败", e);
    }
}
```

### 2. 业务逻辑异常处理

在服务层抛出具体的业务异常：

```java
@Override
public String uploadFile(String ragTag, List<MultipartFile> files) {
    // 参数验证
    if (ragTag == null || ragTag.trim().isEmpty()) {
        throw ParameterException.paramNull("ragTag");
    }
    
    try {
        // 业务逻辑处理
        // ...
        return "文件上传成功";
    } catch (Exception e) {
        log.error("处理文件失败", e);
        throw SystemException.fileUploadError("处理文件失败", e);
    }
}
```



## 响应码说明

项目使用统一的响应码体系：

| 响应码 | 说明 | 使用场景 |
|--------|------|----------|
| 2000 | 操作成功 | 正常业务处理成功 |
| 4000 | 参数错误 | 请求参数验证失败 |
| 4001 | 未授权 | 用户未登录或token无效 |
| 4003 | 禁止访问 | 用户权限不足 |
| 4004 | 资源未找到 | 请求的资源不存在 |
| 4005 | 请求方法不支持 | HTTP方法不支持 |
| 4008 | 请求超时 | 请求处理超时 |
| 4029 | 请求过于频繁 | 限流触发 |
| 5000 | 服务器内部错误 | 系统内部错误 |
| 5001 | 数据库连接错误 | 数据库操作失败 |
| 5002 | 文件上传错误 | 文件处理失败 |
| 5003 | 服务不可用 | 外部服务不可用 |
| 5004 | 网关超时 | 网关处理超时 |
| 5005 | AI服务错误 | AI服务调用失败 |
| 5006 | Redis连接错误 | Redis操作失败 |
| 5007 | 向量数据库错误 | 向量数据库操作失败 |



## 异常处理最佳实践

### 1. 异常抛出原则

- **早抛出，晚捕获**：在发现问题时立即抛出异常
- **具体明确**：异常消息要具体明确，便于问题定位
- **分类处理**：根据异常类型选择合适的异常类

### 2. 日志记录

- **参数异常**：使用 `warn` 级别记录
- **业务异常**：使用 `warn` 级别记录
- **系统异常**：使用 `error` 级别记录
- **包含上下文**：记录关键参数和操作上下文

### 3. 异常处理流程

```java
public Response<String> businessMethod(String param) {
    try {
        // 1. 参数验证
        if (param == null || param.trim().isEmpty()) {
            throw ParameterException.paramNull("param");
        }
        
        // 2. 业务逻辑处理
        String result = doBusinessLogic(param);
        
        // 3. 返回成功结果
        return Response.success(result);
        
    } catch (ParameterException e) {
        // 参数异常直接抛出，由全局异常处理器处理
        throw e;
    } catch (BusinessException e) {
        // 业务异常直接抛出，由全局异常处理器处理
        throw e;
    } catch (Exception e) {
        // 其他异常包装为系统异常
        log.error("业务处理失败", e);
        throw SystemException.databaseError("业务处理失败", e);
    }
}
```

### 4. 异常信息设计

- **用户友好**：异常消息要对用户友好，避免技术细节
- **开发友好**：日志中要包含足够的技术细节用于调试
- **可追踪**：包含请求ID等追踪信息



## 测试异常处理

### 1. 单元测试

```java
@Test
public void testParameterValidation() {
    // 测试参数为空的情况
    assertThrows(ParameterException.class, () -> {
        ragService.uploadFile(null, files);
    });
    
    // 测试参数格式错误的情况
    assertThrows(ParameterException.class, () -> {
        ParameterException.paramFormatError("email", "邮箱格式");
    });
}
```

### 2. 集成测试

```java
@Test
public void testGlobalExceptionHandler() {
    // 发送请求触发异常
    ResponseEntity<Response<Void>> response = restTemplate.postForEntity(
        "/api/v1/rag/file/upload", request, Response.class);
    
    // 验证响应格式
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getCode()).isEqualTo("4000");
    assertThat(response.getBody().getInfo()).contains("参数");
}
```



## 监控和告警

### 1. 异常监控

- 监控异常频率和类型
- 设置异常阈值告警
- 跟踪异常趋势

### 2. 性能影响

- 异常处理不应影响正常业务流程
- 避免在异常处理中进行耗时操作
- 合理使用异常，避免过度使用



## 总结

规范的异常处理机制提供了：

1. **统一的异常分类**：业务异常、参数异常、系统异常
2. **全局异常处理**：自动捕获和转换异常为标准响应
3. **丰富的响应码**：覆盖各种业务场景
4. **完善的日志记录**：便于问题定位和监控
5. **友好的错误信息**：提升用户体验

通过这套异常处理机制，可以确保系统的稳定性和可维护性，同时提供良好的用户体验。
