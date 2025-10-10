# AI Knowledge Base

<div align="center">

[English](README.md) | [中文文档](README_zh.md)

**A Modern RAG (Retrieval-Augmented Generation) Knowledge Base System**

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.10-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0--M8-green.svg)](https://spring.io/projects/spring-ai)
[![React](https://img.shields.io/badge/React-18.3-61dafb.svg)](https://reactjs.org/)

</div>

---

## 📖 Overview

AI Knowledge Base is an enterprise-grade RAG (Retrieval-Augmented Generation) system built with Spring Boot 3 and Spring AI. It provides intelligent conversational AI capabilities enhanced with custom knowledge bases, supporting multiple AI models and flexible deployment options.

### ✨ Key Features

- 🤖 **Multi-Model AI Chat**: Support for OpenAI-compatible APIs (OpenAI, Ollama, LM Studio, etc.)
- 📚 **RAG Enhancement**: Vector-based knowledge retrieval for context-aware conversations
- 📄 **Document Processing**: Support for PDF, Word, Markdown, code files, and more via Apache Tika
- 🔍 **Git Repository Analysis**: Clone and index entire Git repositories into the knowledge base
- 💾 **Vector Storage**: PostgreSQL with pgvector extension for efficient similarity search
- ⚡ **Streaming Responses**: Server-Sent Events (SSE) for real-time AI response streaming
- 🔐 **Production Ready**: Comprehensive logging, error handling, and security features
- 🐳 **Docker Support**: One-command deployment with Docker Compose
- 🎨 **Modern UI**: Responsive React frontend with Ant Design components

---

## 🏗️ Architecture

### Module Structure

```
ai-knowledge-base/
├── ai-knowledge-api/          # API Layer
│   ├── dto/                   # Data Transfer Objects
│   ├── exception/             # Custom Exceptions
│   ├── logging/               # Logging Framework
│   ├── response/              # Unified Response Models
│   ├── validation/            # Custom Validators
│   ├── IAiService.java        # AI Service Interface
│   └── IRagService.java       # RAG Service Interface
│
├── ai-knowledge-trigger/      # Controller Layer
│   ├── controller/            # REST Controllers
│   │   ├── ChatController.java
│   │   └── RagController.java
│   └── service/               # Service Implementations
│       ├── OpenAiServiceImpl.java
│       └── RagServiceImpl.java
│
├── ai-knowledge-app/          # Application Layer
│   ├── config/                # Spring Configurations
│   │   ├── ChatClientConfig.java
│   │   ├── RagEmbeddingConfig.java
│   │   ├── RedisClientConfig.java
│   │   ├── GlobalExceptionHandler.java
│   │   └── ...
│   └── Application.java       # Application Entry Point
│
└── frontend/                  # React Frontend
    ├── src/
    │   ├── components/        # React Components
    │   ├── services/          # API Services
    │   └── types/             # TypeScript Types
    └── ...
```

### Technology Stack

#### Backend

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Programming Language |
| Spring Boot | 3.4.10 | Application Framework |
| Spring AI | 1.0.0-M8 | AI Integration Framework |
| PostgreSQL | Latest | Database with Vector Support |
| pgvector | 0.5.0+ | Vector Similarity Search |
| Redis | 6.2+ | Caching & Session Storage |
| Redisson | 3.52.0 | Redis Client |
| Apache Tika | Latest | Document Parsing |
| JGit | 5.13.0 | Git Repository Operations |
| Lombok | Latest | Code Generation |

#### Frontend

| Technology | Version | Purpose |
|------------|---------|---------|
| React | 18.3 | UI Framework |
| TypeScript | 5.9 | Type Safety |
| Vite | 7.1 | Build Tool |
| Ant Design | 5.27 | UI Components |
| React Markdown | 10.1 | Markdown Rendering |

---

## 🚀 Quick Start

### Prerequisites

- **Java 17** or higher
- **Maven 3.9+**
- **PostgreSQL** with pgvector extension
- **Redis 6.2+**
- **AI Model Service** (choose one):
  - [Ollama](https://ollama.ai/) (recommended for local deployment)
  - OpenAI API
  - OpenAI-compatible API (LM Studio, LocalAI, etc.)

### 1. Database Setup

#### PostgreSQL with pgvector

```bash
# Install PostgreSQL and pgvector extension
# Refer to: https://github.com/pgvector/pgvector

# Create database
createdb ai-rag-knowledge-base

# Enable pgvector extension
psql -d ai-rag-knowledge-base -c "CREATE EXTENSION IF NOT EXISTS vector;"

# Initialize tables (optional, auto-created by Spring AI)
psql -d ai-rag-knowledge-base -f docs/dev-ops/pgvector/sql/init.sql
```

#### Redis

```bash
# Start Redis with password
redis-server --requirepass root

# Or use Docker
docker run -d --name redis -p 6379:6379 redis:6.2 --requirepass root
```

### 2. Configure AI Models

#### Option A: Ollama (Recommended)

```bash
# Install Ollama
curl -fsSL https://ollama.ai/install.sh | sh

# Pull required models
ollama pull deepseek-r1:1.5b          # Chat model
ollama pull nomic-embed-text           # Embedding model

# Verify models are running
ollama list
```

#### Option B: OpenAI API

Update `application-local.yml` with your OpenAI API key:

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

### 3. Build and Run

```bash
# Clone repository
git clone https://github.com/yourusername/ai-knowledge-base.git
cd ai-knowledge-base

# Build backend
mvn clean package -DskipTests

# Run application
java -jar ai-knowledge-app/target/ai-knowledge-base.jar

# Application will start at http://localhost:8080
```

### 4. Frontend Setup (Optional)

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev

# Access at http://localhost:5173
```

---

## 🐳 Docker Deployment

### One-Command Deployment

```bash
# Start all services (PostgreSQL, Redis, Application)
docker compose up -d

# View logs
docker compose logs -f ai-knowledge-base-app

# Stop services
docker compose down
```

### Environment Variables

Configure the following environment variables in `docker-compose.yml`:

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Spring profile | `pro` |
| `DATASOURCE_HOST` | PostgreSQL host | `pgvector` |
| `DATASOURCE_PORT` | PostgreSQL port | `5432` |
| `DATASOURCE_DATABASE_NAME` | Database name | `ai-rag-knowledge-base` |
| `DATASOURCE_USERNAME` | Database username | `root` |
| `DATASOURCE_PASSWORD` | Database password | `root` |
| `OPENAI_BASE_URL` | AI service base URL | `http://localhost:11434` |
| `OPENAI_API_KEY` | AI service API key | `empty` |
| `OPENAI_CHAT_MODEL` | Chat model name | `deepseek-r1:1.5b` |
| `OPENAI_EMBEDDING_MODEL` | Embedding model name | `nomic-embed-text` |
| `REDIS_HOST` | Redis host | `redis` |
| `REDIS_PORT` | Redis port | `6379` |
| `REDIS_PASSWORD` | Redis password | `root` |

---

## 📡 API Documentation

### Base URL

```
http://localhost:8080/api/v1
```

### Authentication

Currently, no authentication is required. For production deployment, implement authentication via Spring Security.

### Endpoints

#### 1. Query Available Models

```http
GET /chat/models
```

**Response:**
```json
{
  "code": "0000",
  "info": "Success",
  "data": ["deepseek-r1:1.5b", "qwen2.5:3b", "llama3:8b"],
  "timestamp": "2024-01-01T12:00:00.000+00:00",
  "traceId": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### 2. Stream Chat (Recommended)

```http
POST /chat/generate_stream
Content-Type: application/json
```

**Request Body:**
```json
{
  "model": "deepseek-r1:1.5b",
  "message": "Explain the concept of RAG in AI"
}
```

**Response:** Server-Sent Events (SSE) stream

**cURL Example:**
```bash
curl -N -H "Content-Type: application/json" \
  -X POST http://localhost:8080/api/v1/chat/generate_stream \
  -d '{"model":"deepseek-r1:1.5b","message":"Hello, introduce yourself"}'
```

#### 3. RAG-Enhanced Stream Chat

```http
POST /chat/generate_stream_rag
Content-Type: application/json
```

**Request Body:**
```json
{
  "model": "deepseek-r1:1.5b",
  "ragTag": "my-project-docs",
  "message": "How do I configure the database?"
}
```

**Response:** SSE stream with context from knowledge base

#### 4. Query RAG Tags

```http
GET /rag/query_rag_tag_list
```

**Response:**
```json
{
  "code": "0000",
  "info": "Success",
  "data": ["my-project-docs", "technical-manuals", "api-documentation"],
  "timestamp": "2024-01-01T12:00:00.000+00:00",
  "traceId": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### 5. Upload Files to Knowledge Base

```http
POST /rag/file/upload
Content-Type: multipart/form-data
```

**Form Data:**
- `ragTag`: Knowledge base tag (string)
- `files`: One or more files (file[])

**Supported File Types:**
- Documents: PDF, DOC, DOCX, TXT, MD
- Code: JAVA, PY, JS, TS, GO, RS, CPP, C, H
- Config: XML, JSON, YAML, YML, PROPERTIES

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/rag/file/upload \
  -F "ragTag=my-docs" \
  -F "files=@/path/to/document.pdf" \
  -F "files=@/path/to/readme.md"
```

**Response:**
```json
{
  "code": "0000",
  "info": "Success",
  "data": "文件上传成功！处理文件数：2，生成文档块数：156",
  "timestamp": "2024-01-01T12:00:00.000+00:00",
  "traceId": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### 6. Analyze Git Repository

```http
POST /rag/analyze_git_repository
Content-Type: application/json
```

**Request Body:**
```json
{
  "repoUrl": "https://github.com/username/repository.git",
  "userName": "your-username",
  "token": "your-github-token"
}
```

**Note:** For public repositories, `userName` and `token` can be empty strings.

**Response:**
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

## 🔧 Configuration

### Application Profiles

The application supports multiple profiles:

- `local`: Local development (default)
- `pro`: Production deployment

Activate profile via:
```bash
# Command line
java -jar app.jar --spring.profiles.active=pro

# Environment variable
export SPRING_PROFILES_ACTIVE=pro
```

### Key Configuration Files

#### `application.yml` (Base Configuration)

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

#### `application-pro.yml` (Production Configuration)

Uses environment variables for security:

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

## 📊 Logging

The application includes a comprehensive logging framework with the following features:

### Logging Levels

- **DEBUG**: Detailed diagnostic information
- **INFO**: General informational messages
- **WARN**: Warning messages for potentially harmful situations
- **ERROR**: Error events that might still allow the application to continue

### Structured Logging

All logs follow a structured format:

```
BIZ_BEGIN: op=generateStream, model=deepseek-r1:1.5b, msgLen=50
BIZ_INFO: op=ragSearch, model=deepseek-r1:1.5b, ragTag=my-docs, docs=5
BIZ_END: op=generateStream, model=deepseek-r1:1.5b, trace=550e8400-e29b-41d4-a716-446655440000
BIZ_ERROR: op=uploadFile, file=document.pdf, tag=my-docs
```

### Sensitive Data Masking

Sensitive information (tokens, passwords, user data) is automatically masked in logs:

```java
// Original: token = "ghp_1234567890abcdefghijklmnopqrstuvwxyz"
// Logged:   token = "ghp_1***xyz"
```

### Log Files

Logs are stored in `./data/log/` by default:

- `info.log`: Informational logs
- `warn.log`: Warning logs
- `error.log`: Error logs

---

## 🛡️ Error Handling

### Unified Response Format

All API responses follow a consistent format:

```json
{
  "code": "0000",
  "info": "Success",
  "data": {},
  "timestamp": "2024-01-01T12:00:00.000+00:00",
  "traceId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Response Codes

| Code | Message | Description |
|------|---------|-------------|
| `0000` | Success | Request successful |
| `0001` | Unauthorized | Authentication failed |
| `0002` | Not Found | Resource not found |
| `0003` | Invalid Parameter | Invalid request parameters |
| `1001` | System Error | Internal server error |
| `1002` | Service Unavailable | External service unavailable |
| `2001` | Business Error | Business logic error |

### Exception Handling

The application includes custom exception types:

- **BusinessException**: For business logic errors
- **SystemException**: For system-level errors
- **ValidationException**: For validation errors

All exceptions are handled by `GlobalExceptionHandler` with appropriate HTTP status codes and error messages.

---

## 🔒 Security Considerations

### Production Deployment Checklist

- [ ] Change default database credentials
- [ ] Change default Redis password
- [ ] Enable HTTPS/TLS
- [ ] Implement authentication (Spring Security, OAuth2, JWT)
- [ ] Configure CORS properly
- [ ] Set up rate limiting
- [ ] Enable database connection encryption
- [ ] Use secrets management (Vault, AWS Secrets Manager)
- [ ] Configure firewall rules
- [ ] Enable audit logging
- [ ] Regular dependency updates

### File Upload Security

- File size limits: 10MB per file (configurable)
- File type validation via custom validators
- Virus scanning (recommended to integrate)

---

## 🧪 Testing

### Run Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=OpenAiServiceImplTest

# Run tests with coverage
mvn test jacoco:report
```

### Frontend Tests

```bash
cd frontend

# Run tests
npm run test

# Run tests in watch mode
npm run test:watch
```

---

## 📈 Performance Optimization

### Vector Search Optimization

```sql
-- Create HNSW index for faster similarity search
CREATE INDEX ON vector_store USING hnsw (embedding vector_cosine_ops);
```

### Redis Caching

- Model lists cached in Redis
- RAG tags cached in Redis
- Configurable TTL and cache invalidation

### Connection Pooling

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style

- Follow Google Java Style Guide
- Use Lombok annotations where appropriate
- Write comprehensive JavaDoc comments
- Add unit tests for new features

---

## 📝 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- [Spring AI](https://spring.io/projects/spring-ai) - AI integration framework
- [pgvector](https://github.com/pgvector/pgvector) - Vector similarity search for PostgreSQL
- [Ollama](https://ollama.ai/) - Local LLM deployment
- [Apache Tika](https://tika.apache.org/) - Content detection and analysis
- [Ant Design](https://ant.design/) - UI component library

---

## 📞 Support

- 📧 Email: 3338918781@qq.com
- 🐛 Issues: [GitHub Issues](https://github.com/yourusername/ai-knowledge-base/issues)
- 💬 Discussions: [GitHub Discussions](https://github.com/yourusername/ai-knowledge-base/discussions)

---

## 🗺️ Roadmap

- [ ] Multi-user support with authentication
- [ ] Knowledge base versioning
- [ ] Advanced analytics dashboard
- [ ] Mobile app support
- [ ] Multi-language support
- [ ] Voice input/output
- [ ] Integration with more AI models (Claude, Gemini, etc.)
- [ ] Advanced RAG techniques (HyDE, RAG-Fusion)
- [ ] GraphRAG support
- [ ] Fine-tuning capabilities

---

<div align="center">

**Built with ❤️ using Spring Boot and Spring AI**

</div>
