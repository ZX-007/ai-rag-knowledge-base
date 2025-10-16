# MCP Server CSDN

基于 Spring Boot 的 MCP（Model Context Protocol）服务器，用于获取和管理 CSDN 文章内容。

## 快速开始

### 1. 环境准备

**必需软件：**
- Docker Desktop（Windows/macOS）或 Docker Engine（Linux）
- Java 17+（如需本地开发）
- Maven 3.9+（如需本地开发）

### 2. 配置环境变量

```bash
# 复制环境变量模板
cp env.example .env

# 编辑 .env 文件，设置必需的配置
notepad .env  # Windows
# 或
vim .env      # Linux/macOS
```

**必需配置：**
```properties
# CSDN API Cookie（从浏览器获取）
CSDN_API_COOKIE=your_cookie_here

# CSDN 文章分类
CSDN_API_CATEGORIES=Java场景面试宝典,Spring Boot
```

### 3. 构建和运行（Windows）

#### 方式一：一键构建和启动

```bash
# 构建镜像（会询问是否启动）
scripts\build.cmd

# 输入 Y 启动容器，或使用下面的命令单独启动
scripts\run.cmd
```

#### 方式二：分步操作

```bash
# 1. 仅构建镜像
scripts\build.cmd

# 2. 启动容器
scripts\run.cmd start

# 3. 查看状态
scripts\run.cmd status

# 4. 查看日志
scripts\run.cmd logs

# 5. 健康检查
scripts\run.cmd health

# 6. 重启容器
scripts\run.cmd restart

# 7. 停止容器
scripts\run.cmd stop
# 或使用专门的停止脚本
scripts\stop.cmd

# 8. 进入容器 Shell
scripts\run.cmd shell
```

### 4. 使用 Docker Compose

```bash
# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

## 脚本说明

### scripts/build.cmd - 构建脚本

**功能：**
- ✅ 自动检测并构建 JAR 包（如不存在）
- ✅ 构建 Docker 镜像
- ✅ 导出镜像到 tar 文件
- ✅ 可选择立即启动容器

**用法：**
```bash
# 标准构建
scripts\build.cmd

# 无缓存构建（解决依赖问题）
scripts\build.cmd --no-cache
```

### scripts/run.cmd - 容器管理脚本

**功能：**
- ✅ 启动/停止/重启容器
- ✅ 查看容器状态和日志
- ✅ 健康检查
- ✅ 进入容器 Shell

**用法：**
```bash
scripts\run.cmd [操作]

操作列表：
  start    - 启动容器（默认）
  stop     - 停止并删除容器
  restart  - 重启容器
  logs     - 查看容器日志（实时）
  status   - 查看容器状态
  health   - 执行健康检查
  shell    - 进入容器 Shell
  help     - 显示帮助信息
```

**示例：**
```bash
# 启动容器
scripts\run.cmd
# 或
scripts\run.cmd start

# 查看实时日志
scripts\run.cmd logs

# 健康检查
scripts\run.cmd health

# 停止容器
scripts\run.cmd stop
```

### scripts/stop.cmd - 停止容器脚本

**功能：**
- ✅ 快速停止容器
- ✅ 优雅关闭或强制停止
- ✅ 可选删除容器
- ✅ 智能状态检测

**用法：**
```bash
scripts\stop.cmd [选项]

选项：
  (无参数)   - 优雅停止容器
  --remove   - 停止并删除容器
  --kill     - 强制停止容器（不等待优雅关闭）
```

**示例：**
```bash
# 优雅停止容器
scripts\stop.cmd

# 停止并删除容器
scripts\stop.cmd --remove

# 强制停止（紧急情况使用）
scripts\stop.cmd --kill
```

## 服务端点

启动后可访问以下端点：

| 端点 | 地址 | 说明 |
|------|------|------|
| 健康检查 | http://localhost:8081/actuator/health | 服务健康状态 |
| MCP SSE | http://localhost:8081/sse | SSE 通信端点 |

## 目录结构

```
mcp-server-csdn/
├── scripts/              # 脚本目录
│   ├── build.cmd         # Windows 构建脚本
│   ├── run.cmd           # Windows 容器管理脚本
│   └── stop.cmd          # Windows 停止容器脚本
├── docker-compose.yml    # Docker Compose 配置
├── Dockerfile            # Docker 镜像定义
├── env.example           # 环境变量模板
├── pom.xml              # Maven 项目配置
├── docs/                # 文档目录
│   ├── CONFIG.md        # 配置指南
│   └── DOCKER.md        # Docker 详细文档
└── src/                 # 源代码目录
```

## 常见问题

### 1. 构建失败

**问题：** Maven 构建或 Docker 构建失败

**解决：**
```bash
# 清理 Maven 缓存
mvn clean

# 无缓存构建
scripts\build.cmd --no-cache
```

### 2. 容器启动失败

**问题：** 容器无法启动或立即退出

**解决：**
```bash
# 查看容器日志
scripts\run.cmd logs

# 或使用 docker 命令
docker logs mcp-server-csdn-sse

# 检查环境变量配置
scripts\run.cmd shell
env | grep CSDN
```

### 3. Cookie 失效

**问题：** CSDN API 返回认证错误

**解决：**
1. 登录 CSDN 网站
2. 打开浏览器开发者工具（F12）
3. 找到 Cookie 并复制
4. 更新 `.env` 文件中的 `CSDN_API_COOKIE`
5. 重启容器：`scripts\run.cmd restart`

### 4. 端口冲突

**问题：** 端口 8081 已被占用

**解决：**
```bash
# 修改 docker-compose.yml 中的端口映射
ports:
  - "8082:8081"  # 将宿主机端口改为 8082

# 或在 .env 文件中设置
SERVER_PORT=8082
```

## 日志管理

### 查看日志

```bash
# 容器日志（推荐）
scripts\run.cmd logs

# 应用日志（持久化在宿主机）
# Windows
type data\log\mcp\mcp-server-csdn.log

# Linux/macOS
tail -f data/log/mcp/mcp-server-csdn.log
```

### 日志位置

- 容器内：`/app/data/log/`
- 宿主机：`./data/log/`（通过数据卷挂载）

## 监控和维护

### 健康检查

```bash
# 使用脚本检查
scripts\run.cmd health

# 直接访问健康端点
curl http://localhost:8081/actuator/health

# 查看容器健康状态
docker ps
```

### 资源监控

```bash
# 查看容器资源使用
docker stats mcp-server-csdn-sse

# 查看容器详细信息
docker inspect mcp-server-csdn-sse
```

## 更多文档

- [配置指南](docs/CONFIG.md) - 详细的配置说明
- [Docker 指南](docs/DOCKER.md) - Docker 使用详解

## 技术栈

- **框架：** Spring Boot 3.x
- **Java：** Amazon Corretto 17
- **构建工具：** Maven 3.9
- **容器：** Docker / Docker Compose
- **通信协议：** SSE (Server-Sent Events) / STDIO

## 许可证

本项目采用 MIT 许可证。详见 [LICENSE](../../LICENSE) 文件。

## 支持

如有问题或建议，请提交 Issue 或 Pull Request。

