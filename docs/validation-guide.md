# 参数校验使用指南

## 概述

本项目已集成完整的参数校验功能，基于 Jakarta Bean Validation 3.0 和 Hibernate Validator 8.0，提供了标准校验注解和自定义校验注解。



## 依赖配置

### 父POM依赖管理
```xml
<properties>
    <validation-api.version>3.0.2</validation-api.version>
    <hibernate-validator.version>8.0.1.Final</hibernate-validator.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>jakarta.validation</groupId>
            <artifactId>jakarta.validation-api</artifactId>
            <version>${validation-api.version}</version>
        </dependency>
        <dependency>
            <groupId>org.hibernate.validator</groupId>
            <artifactId>hibernate-validator</artifactId>
            <version>${hibernate-validator.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 模块依赖
- **ai-rag-api**: 添加 `jakarta.validation-api` 和 `hibernate-validator`
- **ai-rag-app**: 添加 `spring-boot-starter-validation`



## 自定义校验注解

### 1. @FileSize - 文件大小校验
```java
@FileSize(max = 10 * 1024 * 1024, message = "文件大小不能超过10MB")
private MultipartFile file;
```

### 2. @FileType - 文件类型校验
```java
@FileType(allowedTypes = {
    "text/plain",
    "application/pdf",
    "application/msword"
}, message = "不支持的文件类型")
private MultipartFile file;
```

### 3. @Size - 字符串长度校验（标准注解）
```java
@Size(min = 1, max = 50, message = "字符串长度必须在1-50个字符之间")
private String ragTag;
```



## 标准校验注解

- `@NotNull` - 不能为null
- `@NotBlank` - 不能为null、空字符串或只包含空白字符
- `@NotEmpty` - 不能为null或空集合/数组
- `@Size` - 集合/数组/字符串大小
- `@Min/@Max` - 数值范围
- `@Pattern` - 正则表达式匹配
- `@Email` - 邮箱格式
- `@Valid` - 级联校验



## 控制器使用示例

### 1. 方法参数校验
```java
@RequestMapping(value = "/generate", method = RequestMethod.GET)
public ChatResponse generate(
    @RequestParam @NotBlank(message = "模型名称不能为空") 
    @Size(min = 1, max = 100, message = "模型名称长度必须在1-100个字符之间") 
    String model, 
    @RequestParam @NotBlank(message = "消息内容不能为空") 
    @Size(min = 1, max = 4000, message = "消息内容长度必须在1-4000个字符之间") 
    String message) {
    // 业务逻辑
}
```

### 2. DTO对象校验
```java
@RequestMapping(value = "/generate_dto", method = RequestMethod.POST)
public ChatResponse generateWithDto(@Valid @RequestBody ChatRequest request) {
    // 业务逻辑
}
```

### 3. 文件上传校验
```java
@RequestMapping(value = "file/upload_dto", method = RequestMethod.POST)
public Response<String> uploadFileWithDto(@Valid FileUploadRequest request) {
    // 业务逻辑
}
```



## DTO类定义示例

### ChatRequest
```java
@Data
public class ChatRequest {
    @NotBlank(message = "模型名称不能为空")
    @Size(min = 1, max = 100, message = "模型名称长度必须在1-100个字符之间")
    private String model;

    @NotBlank(message = "消息内容不能为空")
    @Size(min = 1, max = 4000, message = "消息内容长度必须在1-4000个字符之间")
    private String message;
}
```

### FileUploadRequest
```java
@Data
public class FileUploadRequest {
    @NotBlank(message = "RAG标签不能为空")
    @Size(min = 1, max = 50, message = "RAG标签长度必须在1-50个字符之间")
    private String ragTag;

    @NotNull(message = "文件列表不能为null")
    @NotEmpty(message = "文件列表不能为空")
    private List<MultipartFile> files;

    @FileSize(max = 10 * 1024 * 1024, message = "文件大小不能超过10MB")
    @FileType(allowedTypes = {
        "text/plain", "application/pdf", "application/msword"
    }, message = "不支持的文件类型")
    private MultipartFile file;
}
```



## 异常处理

全局异常处理器已配置处理以下校验异常：

### 1. MethodArgumentNotValidException
处理 `@Valid` 注解在方法参数上的校验失败

### 2. BindException
处理表单数据绑定校验失败

### 3. ConstraintViolationException
处理方法参数上的校验注解校验失败

### 4. MissingServletRequestParameterException
处理缺少必需请求参数

### 5. MethodArgumentTypeMismatchException
处理参数类型不匹配



## 错误响应格式

所有校验失败都会返回统一的错误响应格式：

```json
{
    "code": "PARAM_ERROR",
    "message": "具体的校验错误信息",
    "data": null,
    "success": false,
    "timestamp": "2024-01-01T12:00:00"
}
```



## 测试

项目包含完整的参数校验测试用例，位于 `ai-rag-app/src/test/java/com/lcx/app/test/ValidationTest.java`，涵盖：

- ChatRequest参数校验
- FileUploadRequest参数校验
- 自定义校验注解测试



## 最佳实践

1. **使用DTO对象**：对于复杂的请求参数，建议使用DTO对象而不是直接在方法参数上添加校验注解
2. **自定义错误消息**：为每个校验注解提供清晰的中文错误消息
3. **分组校验**：对于不同场景需要不同校验规则时，使用校验分组
4. **级联校验**：使用 `@Valid` 注解进行对象属性的级联校验
5. **性能考虑**：避免在循环中进行大量校验操作



## 扩展自定义校验注解

如需添加新的自定义校验注解，请参考现有注解的实现：

1. 创建注解接口（如 `@CustomValidation`）
2. 创建校验器实现类（如 `CustomValidationValidator`）
3. 在需要的地方使用注解
4. 添加相应的测试用例



## 注意事项

1. Spring Boot 3.x 使用 Jakarta EE 规范，注意包名从 `javax.validation` 改为 `jakarta.validation`
2. 文件上传校验需要考虑文件大小和类型的限制
3. 字符串长度校验默认忽略空白字符，可通过 `ignoreWhitespace` 参数控制
4. 校验失败时会抛出异常，由全局异常处理器统一处理
