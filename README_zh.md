# AI 知识库系统

<div align="center">
[English](README.md) | [中文文档](README_zh.md)

**现代化的 RAG（检索增强生成）知识库系统**

[![许可证](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.10-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0--M8-green.svg)](https://spring.io/projects/spring-ai)
[![React](https://img.shields.io/badge/React-18.3-61dafb.svg)](https://reactjs.org/)

</div>

---

## 📖 项目简介

AI 知识库是一个基于 Spring Boot 3 和 Spring AI 构建的企业级 RAG（检索增强生成）系统。它提供了基于自定义知识库增强的智能对话 AI 能力，支持多种 AI 模型和灵活的部署选项。

### ✨ 核心特性

- 🤖 **多模型 AI 对话**：支持 OpenAI 兼容 API（OpenAI、Ollama、LM Studio 等）
- 📚 **RAG 增强**：基于向量数据库的知识检索，提供上下文感知的对话能力
- 📄 **文档处理**：通过 Apache Tika 支持 PDF、Word、Markdown、代码文件等多种格式
- 🔍 **Git 仓库分析**：克隆并索引整个 Git 仓库到知识库
- 💾 **向量存储**：基于 PostgreSQL 的 pgvector 扩展，提供高效的相似度搜索
- ⚡ **流式响应**：通过服务器发送事件（SSE）实现实时 AI 响应流
- 🔐 **生产就绪**：完善的日志记录、错误处理和安全特性
- 🐳 **Docker 支持**：通过 Docker Compose 一键部署
- 🎨 **现代化 UI**：基于 Ant Design 的响应式 React 前端

---

## 🏗️ 系统架构

### 模块结构

```
ai-knowledge-base/
├── ai-knowledge-api/          # API 层
│   ├── dto/                   # 数据传输对象
│   ├── exception/             # 自定义异常
│   ├── logging/               # 日志框架
│   ├── response/              # 统一响应模型
│   ├── validation/            # 自定义验证器
│   ├── IAiService.java        # AI 服务接口
│   └── IRagService.java       # RAG 服务接口
│
├── ai-knowledge-trigger/      # 控制器层
│   ├── controller/            # REST 控制器
│   │   ├── ChatController.java
│   │   └── RagController.java
│   └── service/               # 服务实现
│       ├── OpenAiServiceImpl.java
│       └── RagServiceImpl.java
│
├── ai-knowledge-app/          # 应用层
│   ├── config/                # Spring 配置
│   │   ├── ChatClientConfig.java
│   │   ├── RagEmbeddingConfig.java
│   │   ├── RedisClientConfig.java
│   │   ├── GlobalExceptionHandler.java
│   │   └── ...
│   └── Application.java       # 应用程序入口
│
└── frontend/                  # React 前端
    ├── src/
    │   ├── components/        # React 组件
    │   ├── services/          # API 服务
    │   └── types/             # TypeScript 类型
    └── ...
```

### 技术栈

#### 后端技术

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 编程语言 |
| Spring Boot | 3.4.10 | 应用框架 |
| Spring AI | 1.0.0-M8 | AI 集成框架 |
| PostgreSQL | Latest | 支持向量的数据库 |
| pgvector | 0.5.0+ | 向量相似度搜索 |
| Redis | 6.2+ | 缓存与会话存储 |
| Redisson | 3.52.0 | Redis 客户端 |
| Apache Tika | Latest | 文档解析 |
| JGit | 5.13.0 | Git 仓库操作 |
| Lombok | Latest | 代码生成 |

#### 前端技术

| 技术 | 版本 | 用途 |
|------|------|------|
| React | 18.3 | UI 框架 |
| TypeScript | 5.9 | 类型安全 |
| Vite | 7.1 | 构建工具 |
| Ant Design | 5.27 | UI 组件库 |
| React Markdown | 10.1 | Markdown 渲染 |

---

## 🚀 快速开始

### 环境要求

- **Java 17** 或更高版本
- **Maven 3.9+**
- **PostgreSQL** 并启用 pgvector 扩展
- **Redis 6.2+**
- **AI 模型服务**（选择其一）：
  - [Ollama](https://ollama.ai/)（推荐本地部署）
  - OpenAI API
  - OpenAI 兼容 API（LM Studio、LocalAI 等）

### 1. 数据库设置

#### PostgreSQL 与 pgvector

```bash
# 安装 PostgreSQL 和 pgvector 扩展
# 参考：https://github.com/pgvector/pgvector

# 创建数据库
createdb ai-rag-knowledge-base

# 启用 pgvector 扩展
psql -d ai-rag-knowledge-base -c "CREATE EXTENSION IF NOT EXISTS vector;"

# 初始化表（可选，Spring AI 会自动创建）
psql -d ai-rag-knowledge-base -f docs/dev-ops/pgvector/sql/init.sql
```

#### Redis

```bash
# 启动带密码的 Redis
redis-server --requirepass root

# 或使用 Docker
docker run -d --name redis -p 6379:6379 redis:6.2 --requirepass root
```

### 2. 配置 AI 模型

#### 方案 A：Ollama（推荐）

```bash
# 安装 Ollama
curl -fsSL https://ollama.ai/install.sh | sh

# 拉取所需模型
ollama pull deepseek-r1:1.5b          # 对话模型
ollama pull nomic-embed-text           # 嵌入模型

# 验证模型已安装
ollama list
```

#### 方案 B：OpenAI API

在 `application-local.yml` 中更新你的 OpenAI API 密钥：

```yaml
spring:
  ai:
    openai:
      base-url: https://api.openai.com
      api-key: sk-your-api-key-here
      chat:
        options:
          model: gpt-3.5-turbo
      embedding:
        options:
          model: text-embedding-ada-002
```

### 3. 构建和运行

```bash
# 克隆仓库
git clone https://github.com/yourusername/ai-knowledge-base.git
cd ai-knowledge-base

# 构建后端
mvn clean package -DskipTests

# 运行应用
java -jar ai-knowledge-app/target/ai-knowledge-base.jar

# 应用将在 http://localhost:8080 启动
```

### 4. 前端设置（可选）

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 访问 http://localhost:5173
```

---

## 🐳 Docker 部署

### 一键部署

```bash
# 启动所有服务（PostgreSQL、Redis、应用程序）
docker compose up -d

# 查看日志
docker compose logs -f ai-knowledge-base-app

# 停止服务
docker compose down
```

### 环境变量

在 `docker-compose.yml` 中配置以下环境变量：

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `SPRING_PROFILES_ACTIVE` | Spring 配置文件 | `pro` |
| `DATASOURCE_HOST` | PostgreSQL 主机 | `pgvector` |
| `DATASOURCE_PORT` | PostgreSQL 端口 | `5432` |
| `DATASOURCE_DATABASE_NAME` | 数据库名称 | `ai-rag-knowledge-base` |
| `DATASOURCE_USERNAME` | 数据库用户名 | `root` |
| `DATASOURCE_PASSWORD` | 数据库密码 | `root` |
| `OPENAI_BASE_URL` | AI 服务基础 URL | `http://localhost:11434` |
| `OPENAI_API_KEY` | AI 服务 API 密钥 | `empty` |
| `OPENAI_CHAT_MODEL` | 对话模型名称 | `deepseek-r1:1.5b` |
| `OPENAI_EMBEDDING_MODEL` | 嵌入模型名称 | `nomic-embed-text` |
| `REDIS_HOST` | Redis 主机 | `redis` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `REDIS_PASSWORD` | Redis 密码 | `root` |

---

## 📡 API 文档

### 基础 URL

```
http://localhost:8080/api/v1
```

### 认证

当前版本不需要认证。对于生产环境部署，建议通过 Spring Security 实现认证。

### 接口列表

#### 1. 查询可用模型

```http
GET /chat/models
```

**响应示例：**
```json
{
  "code": "0000",
  "info": "Success",
  "data": ["deepseek-r1:1.5b", "qwen2.5:3b", "llama3:8b"],
  "timestamp": "2024-01-01T12:00:00.000+00:00",
  "traceId": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### 2. 流式对话（推荐）

```http
POST /chat/generate_stream
Content-Type: application/json
```

**请求体：**
```json
{
  "model": "deepseek-r1:1.5b",
  "message": "解释一下 AI 中的 RAG 概念"
}
```

**响应：** 服务器发送事件（SSE）流

**cURL 示例：**
```bash
curl -N -H "Content-Type: application/json" \
  -X POST http://localhost:8080/api/v1/chat/generate_stream \
  -d '{"model":"deepseek-r1:1.5b","message":"你好，介绍一下你自己"}'
```

#### 3. RAG 增强的流式对话

```http
POST /chat/generate_stream_rag
Content-Type: application/json
```

**请求体：**
```json
{
  "model": "deepseek-r1:1.5b",
  "ragTag": "my-project-docs",
  "message": "如何配置数据库？"
}
```

**响应：** 带有知识库上下文的 SSE 流

#### 4. 查询 RAG 标签列表

```http
GET /rag/query_rag_tag_list
```

**响应示例：**
```json
{
  "code": "0000",
  "info": "Success",
  "data": ["my-project-docs", "technical-manuals", "api-documentation"],
  "timestamp": "2024-01-01T12:00:00.000+00:00",
  "traceId": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### 5. 上传文件到知识库

```http
POST /rag/file/upload
Content-Type: multipart/form-data
```

**表单数据：**
- `ragTag`: 知识库标签（字符串）
- `files`: 一个或多个文件（文件数组）

**支持的文件类型：**
- 文档：PDF、DOC、DOCX、TXT、MD
- 代码：JAVA、PY、JS、TS、GO、RS、CPP、C、H
- 配置：XML、JSON、YAML、YML、PROPERTIES

**cURL 示例：**
```bash
curl -X POST http://localhost:8080/api/v1/rag/file/upload \
  -F "ragTag=my-docs" \
  -F "files=@/path/to/document.pdf" \
  -F "files=@/path/to/readme.md"
```

**响应示例：**
```json
{
  "code": "0000",
  "info": "Success",
  "data": "文件上传成功！处理文件数：2，生成文档块数：156",
  "timestamp": "2024-01-01T12:00:00.000+00:00",
  "traceId": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### 6. 分析 Git 仓库

```http
POST /rag/analyze_git_repository
Content-Type: application/json
```

**请求体：**
```json
{
  "repoUrl": "https://github.com/username/repository.git",
  "userName": "your-username",
  "token": "your-github-token"
}
```

**注意：** 对于公开仓库，`userName` 和 `token` 可以为空字符串。

**响应示例：**
```json
{
  "code": "0000",
  "info": "Success",
  "data": "Git仓库分析完成！项目：repository，处理文件数：234，生成文档块数：1567，耗时：12345毫秒",
  "timestamp": "2024-01-01T12:00:00.000+00:00",
  "traceId": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

## 🔧 配置说明

### 应用配置文件

应用程序支持多个配置文件：

- `local`: 本地开发环境（默认）
- `pro`: 生产环境部署

通过以下方式激活配置文件：
```bash
# 命令行参数
java -jar app.jar --spring.profiles.active=pro

# 环境变量
export SPRING_PROFILES_ACTIVE=pro
```

### 主要配置文件

#### `application.yml`（基础配置）

```yaml
server:
  port: 8080

spring:
  application:
    name: ai-rag-knowledge-base
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/ai-rag-knowledge-base
    username: root
    password: root
  ai:
    openai:
      base-url: http://localhost:11434
      api-key: empty
      chat:
        options:
          model: deepseek-r1:1.5b
      embedding:
        options:
          model: nomic-embed-text
          dimensions: 768
    vectorstore:
      pgvector:
        schema-name: public
        table-name: vector_store
        index-type: HNSW
        distance-type: COSINE_DISTANCE
        dimensions: 768

redis:
  sdk:
    config:
      host: localhost
      port: 6379
      password: root
      pool-size: 10
```

#### `application-pro.yml`（生产环境配置）

使用环境变量以提高安全性：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DATASOURCE_HOST}:${DATASOURCE_PORT}/${DATASOURCE_DATABASE_NAME}
    username: ${DATASOURCE_USERNAME}
    password: ${DATASOURCE_PASSWORD}
  ai:
    openai:
      base-url: ${OPENAI_BASE_URL}
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: ${OPENAI_CHAT_MODEL}
      embedding:
        options:
          model: ${OPENAI_EMBEDDING_MODEL}
redis:
  sdk:
    config:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}
```

---

## 📊 日志系统

应用程序包含一个全面的日志框架，具有以下特性：

### 日志级别

- **DEBUG**：详细的诊断信息
- **INFO**：一般的信息性消息
- **WARN**：潜在有害情况的警告消息
- **ERROR**：可能仍允许应用程序继续运行的错误事件

### 结构化日志

所有日志都遵循结构化格式：

```
BIZ_BEGIN: op=generateStream, model=deepseek-r1:1.5b, msgLen=50
BIZ_INFO: op=ragSearch, model=deepseek-r1:1.5b, ragTag=my-docs, docs=5
BIZ_END: op=generateStream, model=deepseek-r1:1.5b, trace=550e8400-e29b-41d4-a716-446655440000
BIZ_ERROR: op=uploadFile, file=document.pdf, tag=my-docs
```

### 敏感数据脱敏

敏感信息（令牌、密码、用户数据）在日志中自动脱敏：

```java
// 原始数据: token = "ghp_1234567890abcdefghijklmnopqrstuvwxyz"
// 日志输出: token = "ghp_1***xyz"
```

### 日志文件

日志默认存储在 `./data/log/` 目录：

- `info.log`: 信息日志
- `warn.log`: 警告日志
- `error.log`: 错误日志

---

## 🛡️ 错误处理

### 统一响应格式

所有 API 响应都遵循统一格式：

```json
{
  "code": "0000",
  "info": "Success",
  "data": {},
  "timestamp": "2024-01-01T12:00:00.000+00:00",
  "traceId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 响应码

| 代码 | 消息 | 描述 |
|------|------|------|
| `0000` | 成功 | 请求成功 |
| `0001` | 未授权 | 认证失败 |
| `0002` | 未找到 | 资源不存在 |
| `0003` | 参数无效 | 请求参数无效 |
| `1001` | 系统错误 | 内部服务器错误 |
| `1002` | 服务不可用 | 外部服务不可用 |
| `2001` | 业务错误 | 业务逻辑错误 |

### 异常处理

应用程序包含自定义异常类型：

- **BusinessException**：业务逻辑错误
- **SystemException**：系统级错误
- **ValidationException**：验证错误

所有异常都由 `GlobalExceptionHandler` 处理，并返回适当的 HTTP 状态码和错误消息。

---

## 🔒 安全注意事项

### 生产环境部署检查清单

- [ ] 更改默认数据库凭据
- [ ] 更改默认 Redis 密码
- [ ] 启用 HTTPS/TLS
- [ ] 实现身份认证（Spring Security、OAuth2、JWT）
- [ ] 正确配置 CORS
- [ ] 设置速率限制
- [ ] 启用数据库连接加密
- [ ] 使用密钥管理服务（Vault、AWS Secrets Manager）
- [ ] 配置防火墙规则
- [ ] 启用审计日志
- [ ] 定期更新依赖项

### 文件上传安全

- 文件大小限制：每个文件 10MB（可配置）
- 通过自定义验证器进行文件类型验证
- 建议集成病毒扫描

---

## 🧪 测试

### 运行测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=OpenAiServiceImplTest

# 运行测试并生成覆盖率报告
mvn test jacoco:report
```

### 前端测试

```bash
cd frontend

# 运行测试
npm run test

# 监视模式运行测试
npm run test:watch
```

---

## 📈 性能优化

### 向量搜索优化

```sql
-- 创建 HNSW 索引以加快相似度搜索
CREATE INDEX ON vector_store USING hnsw (embedding vector_cosine_ops);
```

### Redis 缓存

- 模型列表缓存在 Redis 中
- RAG 标签缓存在 Redis 中
- 可配置的 TTL 和缓存失效策略

### 连接池配置

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

---

## 🤝 贡献指南

欢迎贡献！请遵循以下步骤：

1. Fork 仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m '添加某个很棒的特性'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 开启 Pull Request

### 代码规范

- 遵循 Google Java 代码风格指南
- 适当使用 Lombok 注解
- 编写详细的 JavaDoc 注释
- 为新功能添加单元测试

---

## 📝 许可证

本项目采用 Apache License 2.0 许可证 - 详见 [LICENSE](LICENSE) 文件。

---

## 🙏 致谢

- [Spring AI](https://spring.io/projects/spring-ai) - AI 集成框架
- [pgvector](https://github.com/pgvector/pgvector) - PostgreSQL 向量相似度搜索
- [Ollama](https://ollama.ai/) - 本地 LLM 部署
- [Apache Tika](https://tika.apache.org/) - 内容检测和分析
- [Ant Design](https://ant.design/) - UI 组件库

---

## 📞 支持

- 📧 邮箱：3338918781@qq.com
- 🐛 问题反馈：[GitHub Issues](https://github.com/yourusername/ai-knowledge-base/issues)
- 💬 讨论区：[GitHub Discussions](https://github.com/yourusername/ai-knowledge-base/discussions)

---

## 🗺️ 路线图

- [ ] 多用户支持与身份认证
- [ ] 知识库版本控制
- [ ] 高级分析仪表板
- [ ] 移动应用支持
- [ ] 多语言支持
- [ ] 语音输入/输出
- [ ] 集成更多 AI 模型（Claude、Gemini 等）
- [ ] 高级 RAG 技术（HyDE、RAG-Fusion）
- [ ] GraphRAG 支持
- [ ] 微调能力

---

<div align="center">

**用 ❤️ 基于 Spring Boot 和 Spring AI 构建**

</div>

