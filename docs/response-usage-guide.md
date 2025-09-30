# Response类使用指南

本项目完善了Response类，提供了统一的API响应格式和便利的创建方法。

## 主要特性

### 1. 基础Response类
- 支持泛型，可以封装任意类型的数据
- 包含状态码、消息、数据、时间戳和追踪ID
- 提供链式调用方法
- 包含成功/失败判断方法

### 2. ResponseUtils工具类
- 提供静态方法快速创建各种类型的响应
- 支持成功、失败、错误等不同场景
- 包含分页、列表、Map等特殊响应类型

### 3. ResponseCode枚举
- 统一管理所有状态码
- 包含业务相关的错误码
- 提供状态码判断方法

### 4. PageResult分页类
- 封装分页查询结果
- 包含分页信息和便利方法

## 使用示例

### 1. 基础用法

```java
// 创建成功响应
Response<String> successResponse = Response.success("操作成功");

// 创建成功响应（带数据）
Response<List<String>> dataResponse = Response.success("查询成功", tagList);

// 创建失败响应
Response<String> errorResponse = Response.failure("操作失败");

// 创建自定义响应
Response<String> customResponse = Response.of("4000", "参数错误");
```

### 2. 使用ResponseUtils

```java
// 成功响应
Response<String> ok = ResponseUtils.ok("操作成功");
Response<List<String>> data = ResponseUtils.ok("查询成功", tagList);

// 错误响应
Response<String> error = ResponseUtils.error("操作失败");
Response<String> badRequest = ResponseUtils.badRequest("参数错误");
Response<String> notFound = ResponseUtils.notFound("资源不存在");

// 业务错误响应
Response<String> aiError = ResponseUtils.aiServiceError("AI服务不可用");
Response<String> dbError = ResponseUtils.databaseError("数据库连接失败");
```

### 3. 分页响应

```java
// 创建分页响应
List<String> data = Arrays.asList("tag1", "tag2", "tag3");
Response<PageResult<String>> pageResponse = ResponseUtils.page(data, 100, 1, 10);

// 手动创建分页响应
PageResult<String> pageResult = PageResult.<String>builder()
    .data(data)
    .total(100)
    .page(1)
    .pageSize(10)
    .totalPages(10)
    .build();
Response<PageResult<String>> response = Response.success("查询成功", pageResult);
```

### 4. 在Controller中使用

```java
@RestController
public class RagController {
    
    @RequestMapping(value = "query_rag_tag_list", method = RequestMethod.GET)
    public Response<List<String>> queryRagTagList() {
        log.info("接收到查询RAG标签列表请求");
        List<String> tagList = ragService.queryRagTagList();
        return ResponseUtils.list("查询成功", tagList);
    }
    
    @RequestMapping(value = "file/upload", method = RequestMethod.POST)
    public Response<String> uploadFile(@RequestParam String ragTag, 
                                     @RequestParam("file") List<MultipartFile> files) {
        try {
            String result = ragService.uploadFile(ragTag, files);
            return ResponseUtils.operationSuccess("文件上传");
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return ResponseUtils.fileUploadError("文件上传失败: " + e.getMessage());
        }
    }
}
```

### 5. 链式调用

```java
// 链式设置属性
Response<String> response = Response.success("操作成功")
    .traceId("trace-123")
    .data("result data");
```

### 6. 响应判断

```java
Response<String> response = someService.doSomething();

// 判断是否成功
if (response.isSuccess()) {
    // 处理成功逻辑
    String data = response.getData();
} else {
    // 处理失败逻辑
    String errorMsg = response.getInfo();
}

// 判断是否失败
if (response.isFailure()) {
    // 处理失败逻辑
}
```

## 状态码说明

| 状态码 | 说明 | 使用场景 |
|--------|------|----------|
| 0000 | 成功 | 操作成功 |
| 4000 | 参数错误 | 请求参数不正确 |
| 4001 | 未授权 | 用户未登录或token无效 |
| 4003 | 禁止访问 | 用户无权限访问 |
| 4004 | 资源未找到 | 请求的资源不存在 |
| 5000 | 服务器内部错误 | 系统异常 |
| 5001 | 数据库连接错误 | 数据库操作失败 |
| 5002 | 文件上传错误 | 文件处理失败 |
| 5005 | AI服务错误 | AI服务调用失败 |
| 5006 | Redis连接错误 | Redis操作失败 |
| 5007 | 向量数据库错误 | 向量数据库操作失败 |

## 响应格式

```json
{
  "code": "0000",
  "info": "操作成功",
  "data": "响应数据",
  "timestamp": "2024-01-01T12:00:00",
  "traceId": "trace-123"
}
```

## 最佳实践

1. **统一使用Response类**：所有API接口都应该返回Response对象
2. **使用ResponseUtils**：优先使用工具类方法创建响应
3. **合理使用状态码**：根据业务场景选择合适的状态码
4. **添加追踪ID**：在分布式系统中添加traceId便于问题定位
5. **记录时间戳**：自动记录响应时间便于性能分析
6. **提供有意义的错误信息**：错误消息应该对用户友好且有助于问题定位
