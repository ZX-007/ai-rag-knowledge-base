## AI RAG 知识库

一个基于 Spring Boot 3 与 Spring AI 的后端服务，提供检索增强生成（RAG）与对话能力，集成 PgVector 向量库、Redis 缓存与本地 Ollama/OpenAI 兼容推理服务。

### 模块结构
- `ai-rag-api`: 公共 API 与模型
  - DTO：`ChatRequest`、`RagChatRequest`、`FileUploadRequest`、`GitRepositoryRequest`
  - 接口：`IAiService`（对话）、`IRagService`（知识库）
  - 响应：`Response<T>`、`ResponseCode`
  - 异常：`BaseException`、`BusinessException`、`SystemException`
  - 校验：文件大小/类型自定义注解与校验器
- `ai-rag-trigger`: 业务控制器与服务实现
  - 控制器：
    - `/api/v1/ollama/*`（`OllamaController`）
    - `/api/v1/openai/*`（`OpenAiController`）
    - `/api/v1/rag/*`（`RagController`）
  - 服务实现：`OllamaServiceImpl`、`OpenAiServiceImpl`、`RagServiceImpl`
  - 集成：Spring AI（Ollama/OpenAI、Tika、PgVector）、JGit、Redisson
- `ai-rag-app`: 应用启动与基础配置
  - 启动类：`com.lcx.app.Application`
  - 配置：Ollama 客户端、Embedding、PgVector、Redisson、全局异常、CORS、Validation、`RestTemplate`

### 运行环境
- JDK 17、Maven 3.9+
- PostgreSQL（启用 `pgvector` 扩展）
- Redis（用于缓存 RAG 标签等）
- 推理服务（二选一或同时）：
  - 本地 `Ollama`（推荐，示例模型：`deepseek-r1:1.5b`、`nomic-embed-text`）
  - OpenAI 兼容接口（如 LM Studio/自建网关）

### 快速开始
1) 构建
```bash
mvn -q -DskipTests package
```

2) 启动（本地）
```bash
java -jar ai-rag-app/target/ai-rag-knowledge-app.jar
```
默认端口 `8080`，默认激活 `local` 配置。

3) 生产配置（`pro`）
- 在 `application-pro.yml` 中通过环境变量注入：
  - 数据库：`DATASOURCE_HOST|PORT|DATABASE_NAME|USERNAME|PASSWORD`
  - Ollama：`OLLAMA_BASE_URL|OLLAMA_CHAT_MODEL|OLLAMA_EMBEDDING_MODEL`
  - OpenAI：`OPENAI_BASE_URL|OPENAI_API_KEY|OPENAI_CHAT_MODEL|OPENAI_EMBEDDING_MODEL`
  - Redis：`REDIS_HOST|REDIS_PORT|REDIS_PASSWORD`
- 切换：`SPRING_PROFILES_ACTIVE=pro`

### 关键配置项（摘）
- `spring.datasource.*`：PostgreSQL 连接（PgVectorStore 使用）
- `spring.ai.ollama.*`：Ollama 地址与默认模型
- `spring.ai.openai.*`：OpenAI 兼容端点与模型
- `redis.sdk.config.*`：Redisson 连接配置

### 接口概览
- 模型查询
  - GET `/api/v1/ollama/models`
  - GET `/api/v1/openai/models`

- 对话（SSE 流式，推荐）
  - POST `/api/v1/ollama/generate_stream`（`ChatRequest{model,message}`）
  - POST `/api/v1/openai/generate_stream`（`ChatRequest{model,message}`）

- RAG 对话（SSE 流式）
  - POST `/api/v1/ollama/generate_stream_rag`（`RagChatRequest{model,ragTag,message}`）
  - POST `/api/v1/openai/generate_stream_rag`（`RagChatRequest{model,ragTag,message}`）

- 知识库管理
  - GET  `/api/v1/rag/query_rag_tag_list` → `Response<List<String>>`
  - POST `/api/v1/rag/file/upload`（`multipart/form-data`：`ragTag`、`files[]`）
  - POST `/api/v1/rag/analyze_git_repository`（`GitRepositoryRequest{repoUrl,userName,token}`）

说明：历史的 `/generate`（非流式）接口已标记废弃，建议使用流式接口。

### 调用示例
- 流式对话（Ollama）
```bash
curl -N -H "Content-Type: application/json" \
  -X POST http://localhost:8080/api/v1/ollama/generate_stream \
  -d '{"model":"deepseek-r1:1.5b","message":"你好，介绍一下项目"}'
```

- RAG 流式对话（OpenAI 兼容）
```bash
curl -N -H "Content-Type: application/json" \
  -X POST http://localhost:8080/api/v1/openai/generate_stream_rag \
  -d '{"model":"deepseek-r1-distill-qwen-1.5b","ragTag":"my-knowledge","message":"给我一个使用说明"}'
```

- 查询 RAG 标签
```bash
curl http://localhost:8080/api/v1/rag/query_rag_tag_list
```

- 文件入库（multipart）
```bash
curl -X POST http://localhost:8080/api/v1/rag/file/upload \
  -F ragTag=my-knowledge \
  -F "files=@/path/to/file.pdf"
```

- Git 仓库入库
```bash
curl -H "Content-Type: application/json" \
  -X POST http://localhost:8080/api/v1/rag/analyze_git_repository \
  -d '{"repoUrl":"https://github.com/username/repo.git","userName":"your_name","token":"your_token"}'
```

### 错误与响应
- 统一响应 `Response<T>`：`code`、`info`、`data`、`timestamp`、`traceId`
- 全局异常：`GlobalExceptionHandler` 按类型映射到 `ResponseCode` 与友好消息

### Docker 一键部署
项目根目录提供 `docker-compose.yml`，包含：`ai-rag-app`、`ollama`、`redis`、`pgvector`。
```bash
docker compose up -d
```
默认将拉起并预热 `deepseek-r1:1.5b`，并通过环境变量注入数据库、Redis 与模型配置。

### 重要提示
- 请先在 Ollama 中拉取并可用：`deepseek-r1:1.5b`、`nomic-embed-text`
- PgVector 数据库需已启用扩展，并初始化向量表（参考 `docs/dev-ops/pgvector/sql/init.sql`）
- `queryAvailableModels` 当前返回示例模型列表，生产可改为直连推理服务查询

### 许可
Apache License 2.0，详见 `LICENSE`。

