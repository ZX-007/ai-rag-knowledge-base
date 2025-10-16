# MCP Server CSDN Docker 使用指南

本文档介绍如何使用 Docker 构建和运行 MCP Server CSDN 服务。

## 目录

- [快速开始](#快速开始)
- [多阶段构建说明](#多阶段构建说明)
- [镜像构建](#镜像构建)
- [运行容器](#运行容器)
- [Docker Compose](#docker-compose)
- [环境变量](#环境变量)
- [健康检查](#健康检查)
- [最佳实践](#最佳实践)

---

## 快速开始

### 使用便捷脚本（Windows，最推荐）

**一键构建和启动：**

```bash
# 1. 配置环境变量（首次使用）
cp env.example .env
# 编辑 .env 文件，设置 CSDN_API_COOKIE 等

# 2. 构建镜像（会询问是否启动容器）
scripts\build.cmd

# 3. 启动容器
scripts\run.cmd

# 4. 查看状态
scripts\run.cmd status

# 5. 查看日志
scripts\run.cmd logs

# 6. 健康检查
scripts\run.cmd health

# 7. 停止容器
scripts\run.cmd stop
```

**脚本说明：**

- `scripts/build.cmd` - 构建 Docker 镜像并可选启动容器
- `scripts/run.cmd [操作]` - 容器管理脚本
  - `start` - 启动容器（默认）
  - `stop` - 停止并删除容器
  - `restart` - 重启容器
  - `logs` - 查看容器日志
  - `status` - 查看容器状态
  - `health` - 执行健康检查
  - `shell` - 进入容器 Shell
  - `help` - 显示帮助信息

### 使用 Docker Compose（推荐）

```bash
# 1. 配置环境变量
cp env.example .env
# 编辑 .env 文件，设置 CSDN_API_COOKIE 等

# 2. 启动服务
docker-compose up -d

# 3. 查看日志
docker-compose logs -f

# 4. 停止服务
docker-compose down
```

### 手动构建和运行

```bash
# 1. 构建镜像
docker build -t mcp-server-csdn:sse-latest -f Dockerfile .

# 2. 运行容器
docker run -d \
  --name mcp-server-csdn \
  -p 8081:8081 \
  -e CSDN_API_COOKIE="your-cookie" \
  -v $(pwd)/data/log:/app/data/log \
  mcp-server-csdn:sse-latest

# 3. 查看日志
docker logs -f mcp-server-csdn
```

---

## 多阶段构建说明

本项目使用多阶段 Docker 构建优化镜像大小和构建速度：

### 构建阶段

```dockerfile
# Stage 1: 依赖缓存
FROM maven:3.9.9-amazoncorretto-17 AS dependencies
# 仅下载依赖，利用 Docker 缓存层

# Stage 2: 应用构建
FROM maven:3.9.9-amazoncorretto-17 AS builder
# 编译应用，生成 JAR 包

# Stage 3: 运行镜像
FROM amazoncorretto:17-alpine3.20-jre
# 最小化运行环境，仅包含 JRE
```

### 优化特性

1. **依赖缓存** 
   - 将 `pom.xml` 单独复制，利用 Docker 层缓存
   - 依赖未变化时，跳过下载步骤，加快构建

2. **最小化镜像**
   - 使用 Alpine Linux 基础镜像
   - 仅包含 JRE（不含 JDK），减小镜像体积
   - 最终镜像约 200MB

3. **安全性**
   - 创建非 root 用户运行应用
   - 最小化系统依赖

---

## 镜像构建

### 使用构建脚本（推荐）

#### Windows 脚本

项目提供了 Windows 批处理脚本，自动化构建流程：

```bash
# 标准构建（使用缓存）
scripts\build.cmd

# 清理缓存构建（解决依赖问题时使用）
scripts\build.cmd --no-cache
```

**构建流程：**
1. 检查 JAR 文件是否存在，不存在则自动执行 Maven 构建
2. 使用 Docker 构建镜像（标签：`mcp-server-csdn:0.0.1` 和 `latest`）
3. 导出镜像到 `mcp-server-csdn.tar` 文件
4. 询问是否立即启动容器

#### Linux/macOS 脚本

```bash
# 赋予执行权限
chmod +x build.sh

# 构建 SSE 模式镜像
./build.sh sse

# 构建 STDIO 模式镜像
./build.sh stdio

# 同时构建两种模式
./build.sh both

# 无缓存构建
./build.sh sse --no-cache

# 构建并推送到镜像仓库
./build.sh sse --push
```

### 手动构建

#### SSE 模式（默认）

```bash
docker build \
  -t mcp-server-csdn:sse-latest \
  -f Dockerfile \
  .
```

#### STDIO 模式

```bash
docker build \
  -t mcp-server-csdn:stdio-latest \
  -f Dockerfile.stdio \
  .
```

### 构建参数

可以传递构建参数：

```bash
docker build \
  --build-arg VERSION=1.0.0 \
  --build-arg BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ') \
  -t mcp-server-csdn:sse-1.0.0 \
  .
```

---

## 运行容器

### SSE 模式

#### 基本运行

```bash
docker run -d \
  --name mcp-server-csdn-sse \
  -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=sse,prod \
  -e CSDN_API_COOKIE="your-cookie-here" \
  -e CSDN_API_CATEGORIES="Java场景面试宝典" \
  mcp-server-csdn:sse-latest
```

#### 完整配置

```bash
docker run -d \
  --name mcp-server-csdn-sse \
  --restart unless-stopped \
  -p 8081:8081 \
  \
  -e SPRING_PROFILES_ACTIVE=sse,prod \
  -e SERVER_PORT=8081 \
  -e CSDN_API_COOKIE="your-cookie" \
  -e CSDN_API_CATEGORIES="Java场景面试宝典" \
  \
  -e JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC" \
  -e TZ=Asia/Shanghai \
  \
  -v $(pwd)/data/log:/app/data/log \
  -v $(pwd)/config:/app/config:ro \
  \
  --cpus="1.0" \
  --memory="1g" \
  \
  --health-cmd="wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1" \
  --health-interval=30s \
  --health-timeout=3s \
  --health-start-period=40s \
  --health-retries=3 \
  \
  mcp-server-csdn:sse-latest
```

### STDIO 模式

```bash
# STDIO 模式通常用于进程间通信，需要与父进程交互
docker run -i \
  --name mcp-server-csdn-stdio \
  -e SPRING_PROFILES_ACTIVE=stdio,prod \
  -e CSDN_API_COOKIE="your-cookie" \
  -v $(pwd)/data/log:/app/data/log \
  mcp-server-csdn:stdio-latest
```

---

## Docker Compose

### 配置文件

项目提供了 `docker-compose.yml` 配置文件。

### 使用方法

```bash
# 启动服务（后台运行）
docker-compose up -d

# 启动服务（查看日志）
docker-compose up

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 重启服务
docker-compose restart

# 停止服务
docker-compose stop

# 停止并删除容器
docker-compose down

# 停止并删除容器、镜像、数据卷
docker-compose down --rmi all --volumes
```

### 环境变量文件

创建 `.env` 文件：

```bash
# CSDN API 配置
CSDN_API_CATEGORIES=Java场景面试宝典
CSDN_API_COOKIE=your-cookie-here

# JVM 配置
JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC
```

---

## 环境变量

### 必需变量

| 变量名 | 说明 | 示例 |
|--------|------|------|
| `CSDN_API_COOKIE` | CSDN 认证 Cookie | `uuid_tt_dd=xxx...` |
| `CSDN_API_CATEGORIES` | 文章分类 | `Java场景面试宝典` |

### 可选变量

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `SPRING_PROFILES_ACTIVE` | `prod,sse` | Spring 配置文件 |
| `SERVER_PORT` | `8081` | 服务端口（SSE 模式） |
| `JAVA_OPTS` | `-Xms256m -Xmx512m` | JVM 参数 |
| `TZ` | `Asia/Shanghai` | 时区 |
| `LANG` | `C.UTF-8` | 语言编码 |

### JVM 参数调优

```bash
# 小内存环境（512MB）
JAVA_OPTS="-Xms128m -Xmx256m -XX:+UseSerialGC"

# 标准环境（1GB）
JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 高性能环境（2GB+）
JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=100"
```

---

## 健康检查

### 内置健康检查

Dockerfile 中已配置健康检查：

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1
```

### 查看健康状态

```bash
# 查看容器健康状态
docker ps

# 查看详细健康信息
docker inspect --format='{{json .State.Health}}' mcp-server-csdn-sse | jq
```

### 手动健康检查

```bash
# 在容器内执行
docker exec mcp-server-csdn-sse wget -qO- http://localhost:8081/actuator/health

# 从宿主机执行
curl http://localhost:8081/actuator/health
```

---

## 最佳实践

### 1. 使用数据卷持久化日志

```bash
-v $(pwd)/data/log:/app/data/log
```

### 2. 资源限制

```bash
docker run \
  --cpus="1.0" \
  --memory="1g" \
  --memory-swap="1g" \
  ...
```

### 3. 日志管理

```bash
docker run \
  --log-driver json-file \
  --log-opt max-size=10m \
  --log-opt max-file=3 \
  ...
```

### 4. 安全实践

- ✅ 使用非 root 用户运行
- ✅ 只读挂载配置文件
- ✅ 使用环境变量传递敏感信息
- ✅ 定期更新基础镜像

### 5. 镜像标签管理

```bash
# 开发环境
mcp-server-csdn:dev-latest

# 测试环境
mcp-server-csdn:test-v1.0.0

# 生产环境
mcp-server-csdn:prod-v1.0.0
mcp-server-csdn:prod-latest
```

---

## 故障排查

### 查看日志

```bash
# 容器日志
docker logs -f mcp-server-csdn-sse

# 应用日志（挂载到宿主机）
tail -f data/log/mcp/mcp-server-csdn.log
```

### 进入容器

```bash
docker exec -it mcp-server-csdn-sse sh
```

### 检查配置

```bash
# 查看环境变量
docker exec mcp-server-csdn-sse env

# 查看配置文件
docker exec mcp-server-csdn-sse cat /app/config/application.yml
```

---

## 更多资源

- [Spring Boot Docker 指南](https://spring.io/guides/topicals/spring-boot-docker/)
- [Docker 最佳实践](https://docs.docker.com/develop/dev-best-practices/)
- [多阶段构建文档](https://docs.docker.com/build/building/multi-stage/)

