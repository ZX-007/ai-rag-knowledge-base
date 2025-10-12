# 后端自动部署配置指南

## 📋 部署流程概述

本系统采用**零停机部署 + 自动回滚**策略，确保服务高可用性。

### 核心特性

- ⚡ **零停机部署**：服务中断时间 < 30秒
- 🔄 **自动回滚**：健康检查失败自动恢复到旧版本
- 💾 **完整备份**：配置文件、环境变量、镜像全部备份
- 🏥 **健康检查**：Spring Boot Actuator 多维度验证
- 🔒 **数据安全**：数据卷持久化，回滚不丢失数据

---

## 🚀 部署流程详解

### GitHub Actions 阶段（步骤 1-12）

```
1. 检出代码
2. 设置 Java 17 环境
3. 获取版本号（从 tag 或手动输入）
4. 设置 Docker Buildx
5. 登录 Docker Hub
6. 构建并推送镜像
   ├── 构建：load: true（加载到本地）
   ├── 推送：手动推送到 Docker Hub
   └── 标签：version + latest
7. 备份服务器当前状态 ✨
   ├── 记录当前版本号 → .current_version
   ├── 备份配置文件 → docker-compose.yml.backup
   ├── 备份 .env 文件 → .env.backup
   └── 备份旧镜像 → images/xxx_v1.0.0.tar.gz
8. 上传新的 docker-compose.yml
9. 服务器部署（详见下文）
10. 验证部署
11. 成功通知
12. 失败通知
```

### 服务器部署阶段（步骤 9 内部，零停机）

```
1. 从 Docker Hub 拉取新镜像
   ↓ (3-5分钟，旧服务仍在运行 ✅)
2. 停止旧服务（使用备份的配置）
   ↓ (停机开始，约10-30秒)
3. 更新 docker-compose.yml 镜像版本
4. 更新 .env 文件（从 GitHub Secret）
5. 启动新服务
   ↓ (停机结束)
6. 等待服务启动 (10秒)
7. 检查服务状态
8. 查看服务日志
9. 健康检查（最多2分钟）
   ├── 成功 → 清理旧镜像，完成部署 ✅
   └── 失败 → 自动回滚到旧版本 🔄
10. 清理未使用的镜像
11. 清理临时备份文件
```

### 自动回滚机制 🔄

**触发条件**：
- 容器启动失败
- 健康检查超时（2分钟）
- 健康状态为 DOWN

**回滚流程**：
```
1. 停止失败的新版本服务
2. 恢复 docker-compose.yml.backup
3. 恢复 .env.backup
4. 启动旧版本服务
5. 验证恢复成功
6. 报告回滚结果

回滚时间: < 30秒
```

---

## 🔧 配置步骤

### 步骤 1: 准备 Docker Hub

#### 1.1 注册账号

访问 [https://hub.docker.com](https://hub.docker.com) 注册账号并创建仓库。

#### 1.2 生成 Access Token

1. 登录 Docker Hub
2. Account Settings → Security
3. New Access Token
4. 描述：`GitHub Actions Deploy`
5. 权限：`Read, Write, Delete`
6. **复制 token**（只显示一次！）

---

### 步骤 2: 配置服务器

#### 2.1 安装 Docker

```bash
# SSH 登录服务器
ssh your-username@your-server-ip

# 一键安装 Docker
curl -fsSL https://get.docker.com | sh

# 启动并设置开机自启
sudo systemctl start docker
sudo systemctl enable docker

# 添加用户到 docker 组
sudo usermod -aG docker $USER

# 重新登录使生效
exit && ssh your-username@your-server-ip

# 验证
docker --version
docker compose version
```

#### 2.2 创建部署目录

```bash
# 创建目录结构
mkdir -p /opt/ai-knowledge-base/{logs/app,redis/data,redis,pgvector/data,pgvector/sql,images}
cd /opt/ai-knowledge-base

# 设置权限
chmod -R 755 .
```

#### 2.3 准备配置文件

**Redis 配置**：
```bash
cat > redis/redis.conf << 'EOF'
requirepass root@321
bind 0.0.0.0
port 6379
save 900 1
save 300 10
save 60 10000
dir /data
EOF
```

**PostgreSQL 初始化脚本**：
```bash
cat > pgvector/sql/init.sql << 'EOF'
CREATE EXTENSION IF NOT EXISTS vector;
EOF
```

#### 2.4 生成 SSH 密钥（无密码）

```bash
# 生成专用密钥
ssh-keygen -t ed25519 -C "github-actions-backend" -f ~/.ssh/github_actions_backend -N ""

# 添加公钥到 authorized_keys
cat ~/.ssh/github_actions_backend.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys

# 显示私钥（复制用于 GitHub Secret）
cat ~/.ssh/github_actions_backend
```

---

### 步骤 3: 配置 GitHub Environment Secrets

1. **进入 GitHub 仓库**
2. **Settings** → **Environments** → **pro**（或创建新环境）
3. **添加 Environment Secrets**：

#### 必需的 8 个 Secrets

| Secret 名称 | 值 | 说明 |
|------------|-----|------|
| `DOCKER_USERNAME` | `white0618` | Docker Hub 用户名 |
| `DOCKER_PASSWORD` | `dckr_pat_xxx...` | Docker Hub Access Token |
| `BACKEND_SERVER_HOST` | `192.168.1.100` | 服务器 IP 或域名 |
| `BACKEND_SERVER_USERNAME` | `root` | SSH 登录用户名 |
| `BACKEND_SERVER_SSH_KEY` | `-----BEGIN OPENSSH...` | SSH 私钥（完整内容，无密码）|
| `BACKEND_SERVER_PORT` | `22` | SSH 端口 |
| `BACKEND_DEPLOY_DIR` | `/opt/ai-knowledge-base` | 部署目录路径 |
| `DASHSCOPE_API_KEY` | `sk-xxx...` | 阿里云灵积 API Key ✨ |

**重要提示**：
- SSH 私钥必须是**完整内容**（包括 BEGIN 和 END 行）
- SSH 密钥必须**无密码**保护
- `DASHSCOPE_API_KEY` 用于自动创建 .env 文件

---

### 步骤 4: 更新镜像名称

#### 4.1 修改 docker-compose.yml

```yaml
services:
  ai-knowledge-base-app:
    image: your-dockerhub-username/ai-knowledge-base:v*.*.*  # 修改用户名
```

#### 4.2 修改 workflow 文件

`.github/workflows/deploy-backend.yml`:

```yaml
env:
  DOCKER_IMAGE: your-dockerhub-username/ai-knowledge-base  # 修改用户名
```

---

## 🎯 使用方法

### 方式 1: 推送 Tag 自动触发（推荐）

```bash
# 1. 开发完成后提交代码
git add .
git commit -m "feat: 新功能开发完成"
git push origin main

# 2. 创建并推送 tag
git tag v1.0.0
git push origin v1.0.0

# 3. GitHub Actions 自动执行部署
```

**Tag 命名规范**：
- 格式：`v{major}.{minor}.{patch}`
- 示例：`v1.0.0`, `v1.1.0`, `v2.0.0`
- 遵循[语义化版本](https://semver.org/lang/zh-CN/)

### 方式 2: 手动触发部署

1. GitHub 仓库 → **Actions** 标签
2. 选择 **"部署后端到服务器"**
3. 点击 **"Run workflow"**
4. 填写参数：
   - `tag_version`: 输入版本号（如 `v1.1.0`）
   - `environment`: 选择环境（production/staging）
5. 点击绿色的 **"Run workflow"**

---

## 📊 详细部署流程

### 阶段 1: 构建和推送（GitHub Actions）

```
检出代码 → 设置 Java → 获取版本号
  ↓
Docker Buildx → 登录 Docker Hub
  ↓
构建镜像（多阶段构建）
  ├── Build stage: Maven 编译 JAR
  └── Runtime stage: JRE + JAR
  ↓
推送到 Docker Hub
  ├── your-username/ai-knowledge-base:v1.1.0
  └── your-username/ai-knowledge-base:latest
```

**时间**：约 5-8 分钟

### 阶段 2: 备份当前状态（关键）✨

```bash
SSH 到服务器
  ↓
读取 docker-compose.yml 获取当前版本 (v1.0.0)
  ↓
保存版本号: echo "v1.0.0" > .current_version
  ↓
备份配置: cp docker-compose.yml docker-compose.yml.backup
  ↓
备份 .env: cp .env .env.backup
  ↓
备份旧镜像: docker save xxx:v1.0.0 | gzip > images/ai-knowledge-base_v1.0.0.tar.gz
```

**重要性**：这是回滚机制的基础！

### 阶段 3: 上传新配置

```bash
上传 docker-compose.yml
  ↓
覆盖服务器上的文件（旧配置已在步骤2备份）
```

### 阶段 4: 零停机部署

```
拉取新镜像（旧服务运行中）
  docker pull xxx:v1.1.0
  ↓ (3-5分钟，用户请求正常响应 ✅)
拉取完成
  ↓
停止旧服务（使用备份配置）
  docker compose -f docker-compose.yml.backup down
  ↓ (停机开始)
更新镜像版本
  sed -i "s|:v1.0.0|:v1.1.0|g" docker-compose.yml
  ↓
创建/更新 .env 文件 ✨
  echo "DASHSCOPE_API_KEY=sk-xxx" > .env
  ↓
启动新服务
  docker compose up -d
  ↓ (停机结束，约 10-30秒)
```

**停机时间**：10-30 秒（而不是 3-5 分钟）

### 阶段 5: 健康检查和验证

```
等待服务启动 (10秒)
  ↓
检查容器状态
  docker compose ps
  ↓
查看服务日志
  docker compose logs --tail=12
  ↓
健康检查循环（最多 2 分钟）
  每 2 秒检查一次
  ├── curl /actuator/health
  ├── 检查 status: UP
  ├── 验证 Readiness 探针
  └── 验证 Liveness 探针
    ↓
  成功？
  ├── YES → 清理旧镜像，部署完成 ✅
  └── NO  → 触发自动回滚 🔄
```

### 阶段 6: 自动回滚（失败时）

```
健康检查失败
  ↓
读取 .current_version (v1.0.0)
  ↓
停止失败的服务
  docker compose down
  ↓
恢复配置文件
  mv docker-compose.yml.backup docker-compose.yml
  ↓
恢复 .env 文件
  mv .env.backup .env
  ↓
启动旧版本服务
  docker compose up -d
  ↓
验证恢复
  curl /actuator/health
  ↓
报告回滚结果
  ✅ 成功 or ❌ 失败（需人工介入）
```

---

## 🔧 配置步骤

### 步骤 1: 配置 Docker Hub

1. 注册 [Docker Hub](https://hub.docker.com) 账号
2. 生成 Access Token：
   - Account Settings → Security → New Access Token
   - Description: `GitHub Actions`
   - Permissions: `Read, Write, Delete`
   - **复制 token**（只显示一次）

---

### 步骤 2: 配置服务器环境

#### 2.1 安装 Docker

```bash
# 一键安装
curl -fsSL https://get.docker.com | sh
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER

# 验证
docker --version
docker compose version
```

#### 2.2 创建目录结构

```bash
# 创建所有必要的目录
mkdir -p /opt/ai-knowledge-base/{logs/app,redis/data,pgvector/data,pgvector/sql,images}
cd /opt/ai-knowledge-base
```

#### 2.3 准备配置文件

**Redis 配置** (`redis/redis.conf`):
```bash
cat > redis/redis.conf << 'EOF'
requirepass root@321
bind 0.0.0.0
port 6379
save 900 1
save 300 10
save 60 10000
dir /data
EOF
```

**数据库初始化** (`pgvector/sql/init.sql`):
```bash
cat > pgvector/sql/init.sql << 'EOF'
CREATE EXTENSION IF NOT EXISTS vector;
EOF
```

#### 2.4 配置 SSH 密钥

```bash
# 生成无密码密钥
ssh-keygen -t ed25519 -C "github-actions" -f ~/.ssh/github_actions_backend -N ""

# 添加公钥
cat ~/.ssh/github_actions_backend.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys

# 显示私钥（用于 GitHub Secret）
cat ~/.ssh/github_actions_backend
```

---

### 步骤 3: 配置 GitHub Secrets

**进入**: Settings → Environments → pro → Add secret

| Secret 名称 | 示例值 | 说明 |
|------------|--------|------|
| `DOCKER_USERNAME` | `white0618` | Docker Hub 用户名 |
| `DOCKER_PASSWORD` | `dckr_pat_xxx` | Docker Hub Access Token |
| `BACKEND_SERVER_HOST` | `192.168.1.100` | 服务器 IP |
| `BACKEND_SERVER_USERNAME` | `root` | SSH 用户名 |
| `BACKEND_SERVER_SSH_KEY` | `-----BEGIN...` | SSH 私钥（完整，无密码）|
| `BACKEND_SERVER_PORT` | `22` | SSH 端口 |
| `BACKEND_DEPLOY_DIR` | `/opt/ai-knowledge-base` | 部署目录 |
| `DASHSCOPE_API_KEY` | `sk-xxx...` | 阿里云 API Key ✨ |

---

### 步骤 4: 更新镜像名称

#### docker-compose.yml (第3行)
```yaml
image: your-dockerhub-username/ai-knowledge-base:v*.*.*
```

#### .github/workflows/deploy-backend.yml (第27行)
```yaml
env:
  DOCKER_IMAGE: your-dockerhub-username/ai-knowledge-base
```

---

## 🚀 开始部署

### 首次部署

```bash
# 1. 提交所有配置
git add .
git commit -m "chore: 配置后端自动部署"
git push origin main

# 2. 创建第一个版本 tag
git tag v1.0.0
git push origin v1.0.0

# 3. 查看 GitHub Actions 运行状态
# 4. 等待 8-15 分钟
# 5. 验证部署
curl http://your-server-ip:8080/actuator/health
```

### 日常更新

```bash
# 开发新功能 → 测试通过 → 创建 tag
git tag v1.1.0
git push origin v1.1.0

# 自动部署，停机时间 < 30秒
```

---

## 🔍 监控和验证

### 查看部署状态

```bash
# GitHub Actions 页面
Actions → 选择运行记录 → 查看详细日志
```

### 服务器验证

```bash
# SSH 到服务器
ssh root@your-server
cd /opt/ai-knowledge-base

# 查看容器状态
docker compose ps
# 应显示: (healthy)

# 测试健康检查
curl http://localhost:8080/actuator/health
# 应返回: {"status":"UP",...}

# 查看日志
docker compose logs -f ai-knowledge-base-app

# 查看镜像备份
ls -lh images/
```

---

## 📁 服务器目录结构

```
/opt/ai-knowledge-base/
├── docker-compose.yml              ← 当前配置
├── .env                            ← 环境变量（自动创建）
├── .current_version                ← 临时：旧版本号（部署时）
├── docker-compose.yml.backup       ← 临时：配置备份（部署时）
├── .env.backup                     ← 临时：环境变量备份（部署时）
├── images/                         ← 镜像备份目录
│   ├── ai-knowledge-base_v1.0.0.tar.gz
│   ├── ai-knowledge-base_v1.1.0.tar.gz
│   └── ai-knowledge-base_v1.2.0.tar.gz
├── logs/app/                       ← 应用日志（持久化）
│   ├── info.log
│   ├── error.log
│   └── warn.log
├── redis/                          ← Redis 数据（持久化）
│   ├── data/
│   └── redis.conf
└── pgvector/                       ← 数据库数据（持久化）
    ├── data/
    └── sql/init.sql
```

---

## 🔄 回滚操作

### 自动回滚（无需操作）

系统会自动：
1. 检测健康检查失败
2. 停止新版本
3. 恢复配置和环境变量
4. 启动旧版本
5. 验证恢复成功

### 手动回滚

```bash
cd /opt/ai-knowledge-base

# 1. 查看可用版本
ls images/

# 2. 加载旧版本镜像（可选，如果没有则从 Docker Hub 拉取）
docker load < images/ai-knowledge-base_v1.0.0.tar.gz

# 3. 修改配置版本号
sed -i 's|:v1.1.0|:v1.0.0|g' docker-compose.yml

# 4. 重启服务
docker compose down
docker compose up -d

# 5. 验证
curl http://localhost:8080/actuator/health
docker compose ps
```

---

## ❓ 常见问题

### Q1: 健康检查失败？

**检查步骤**：
```bash
# 1. 查看容器状态
docker compose ps

# 2. 查看应用日志
docker compose logs --tail=100 ai-knowledge-base-app

# 3. 检查健康端点
curl http://localhost:8080/actuator/health

# 4. 检查依赖服务
docker compose ps redis pgvector
```

### Q2: DASHSCOPE_API_KEY 未设置？

**解决方案**：

确保 GitHub Secret 已配置，系统会自动创建 .env 文件。

**手动创建**（如果需要）：
```bash
cd /opt/ai-knowledge-base
echo "DASHSCOPE_API_KEY=sk-your-key" > .env
chmod 600 .env
docker compose restart
```

### Q3: 部署超时？

**可能原因**：
- 网络速度慢（拉取镜像时间长）
- 服务启动慢（首次需要初始化数据库）

**解决方案**：
- 等待更长时间或手动触发
- 检查服务器网络连接
- 预热镜像：提前 `docker pull`

### Q4: 如何查看部署历史？

```bash
# 在服务器上
cd /opt/ai-knowledge-base/images
ls -lt  # 按时间排序查看所有版本

# 在 GitHub 上
Actions 标签 → 查看所有运行记录
```

### Q5: 数据会丢失吗？

**不会！** 以下数据持久化保存：
- ✅ 数据库数据（pgvector/data）
- ✅ Redis 数据（redis/data）
- ✅ 应用日志（logs/app）
- ✅ 镜像备份（images/）

**回滚只替换**：
- 应用代码（Docker 镜像）
- 配置文件（docker-compose.yml）
- 环境变量（.env）

---

## 📊 性能指标

### 部署时间明细

| 阶段 | 时间 | 用户影响 |
|------|------|---------|
| 构建镜像 | 3-5 分钟 | 无影响 |
| 推送 Docker Hub | 1-2 分钟 | 无影响 |
| 备份当前状态 | 10-30 秒 | 无影响 |
| 上传配置 | < 5 秒 | 无影响 |
| 拉取新镜像 | 2-4 分钟 | **无影响（旧服务运行）**✅ |
| 服务切换 | 10-30 秒 | **停机时间** ⚠️ |
| 健康检查 | 10-60 秒 | 新服务已启动 |
| **总部署时间** | **8-15 分钟** | **停机 < 30秒** ✅ |

### 回滚时间

| 回滚类型 | 时间 | 数据丢失 |
|---------|------|---------|
| 自动回滚 | < 30 秒 | ❌ 否 |
| 手动回滚 | 1-2 分钟 | ❌ 否 |

---

## 🛡️ 高可用特性

### 1. 零停机部署

**原理**：先拉取镜像再切换服务

```
传统部署:
  停止服务 → 拉取镜像(5分钟) → 启动
  停机时间: 5分钟 ❌

零停机部署:
  拉取镜像(5分钟，服务运行中) → 停止 → 启动
  停机时间: 30秒 ✅
```

### 2. 自动回滚

**保护层级**：
```
1. 部署前：完整备份（版本+配置+环境）
2. 部署中：健康检查（2分钟超时）
3. 失败时：自动回滚（< 30秒）
4. 回滚后：再次验证
5. 最终：服务始终可用 ✅
```

### 3. 数据安全

**持久化数据**：
- PostgreSQL: `/opt/ai-knowledge-base/pgvector/data`
- Redis: `/opt/ai-knowledge-base/redis/data`
- 日志: `/opt/ai-knowledge-base/logs/app`

**回滚不影响**：
- ✅ 用户数据
- ✅ 缓存数据
- ✅ 历史日志

### 4. 版本管理

**镜像备份**：
```bash
images/ai-knowledge-base_v1.0.0.tar.gz  # 使用版本号命名
images/ai-knowledge-base_v1.1.0.tar.gz  # 清晰可追溯
```

**去重策略**：
- 只备份不存在的版本
- 新镜像在 Docker Hub 上，不备份到本地

---

## 💡 高级技巧

### 1. 预热镜像（加速部署）

```bash
# 在非高峰时段提前拉取
docker pull white0618/ai-knowledge-base:v1.1.0

# 部署时直接使用，无需等待拉取
```

### 2. 监控部署进度

```bash
# 实时监控
watch -n 2 'docker compose ps; echo ""; curl -s http://localhost:8080/actuator/health 2>/dev/null || echo "服务启动中..."'
```

### 3. 清理旧备份

```bash
cd /opt/ai-knowledge-base/images

# 保留最近 5 个版本
ls -t ai-knowledge-base_*.tar.gz | tail -n +6 | xargs rm -f

# 查看空间占用
du -sh .
```

### 4. 对比版本差异

```bash
# 查看两个版本的区别
docker run --rm white0618/ai-knowledge-base:v1.0.0 java -version
docker run --rm white0618/ai-knowledge-base:v1.1.0 java -version
```

---

## 🔒 安全最佳实践

1. **SSH 密钥管理**
   - ✅ 使用专用的无密码密钥
   - ✅ 定期轮换密钥
   - ✅ 限制密钥权限

2. **Secrets 管理**
   - ✅ 使用 GitHub Environment Secrets
   - ✅ 不在代码中硬编码
   - ✅ 定期更新敏感信息

3. **容器安全**
   - ✅ 使用最新基础镜像
   - ✅ 定期更新依赖
   - ✅ 限制容器权限

4. **网络安全**
   - ✅ 配置防火墙
   - ✅ 使用非标准端口（Redis: 16379, PG: 15432）
   - ✅ 健康检查端点考虑限制访问

---

## ✅ 部署检查清单

### 首次部署前

- [ ] Docker Hub 账号和 Token 已准备
- [ ] 服务器已安装 Docker
- [ ] 部署目录已创建
- [ ] Redis 和 PostgreSQL 配置文件已准备
- [ ] SSH 密钥已生成（无密码）
- [ ] GitHub 中 8 个 Secrets 已配置
- [ ] docker-compose.yml 镜像名已更新
- [ ] workflow 文件镜像名已更新

### 部署后验证

- [ ] GitHub Actions 显示成功
- [ ] `docker compose ps` 显示容器运行
- [ ] 容器状态为 `(healthy)`
- [ ] `curl /actuator/health` 返回 `"status":"UP"`
- [ ] Readiness 和 Liveness 探针正常
- [ ] 应用日志无严重错误
- [ ] images/ 目录有镜像备份
- [ ] .env 文件已创建且包含 API Key

---

## 🎯 故障排查

### 部署失败处理流程

```
1. 查看 GitHub Actions 日志
   ↓
2. 确定失败阶段
   ├── 构建失败 → 检查代码和 Dockerfile
   ├── 推送失败 → 检查 Docker Hub Token
   ├── SSH 失败 → 检查密钥配置
   └── 健康检查失败 → 查看应用日志
   ↓
3. 查看服务器状态
   ├── docker compose ps
   ├── docker compose logs
   └── curl /actuator/health
   ↓
4. 根据错误信息修复
   ↓
5. 如果已自动回滚，服务应该正常运行
```

### 常见错误和解决方案

| 错误信息 | 原因 | 解决方案 |
|---------|------|---------|
| `DASHSCOPE_API_KEY is not set` | Secret 未配置 | 添加到 GitHub Secrets |
| `ssh: handshake failed` | SSH 密钥有密码 | 使用无密码密钥 |
| `unauthorized: authentication required` | Docker Hub 登录失败 | 检查 Token 是否有效 |
| `health check timeout` | 服务启动慢/配置错误 | 查看应用日志，检查配置 |
| `port already in use` | 端口被占用 | 检查并停止占用端口的进程 |

---

## 📚 相关文档

- [健康检查指南](./health-check-guide.md)
- [前端部署配置](./deploy-frontend-setup.md)
- [环境变量配置](./environment-variables.md)
- [Docker 官方文档](https://docs.docker.com/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/reference/actuator/)

---

## 🎉 总结

你的后端部署系统现在是一个**企业级的高可用系统**：

### 核心优势

| 特性 | 实现 | 效果 |
|------|------|------|
| **自动化** | GitHub Actions | 推送 tag 即部署 |
| **零停机** | 先拉取后切换 | 停机 < 30秒 |
| **自动回滚** | 健康检查 + 备份 | 失败自动恢复 |
| **数据安全** | Volume 持久化 | 永不丢失 |
| **可追溯** | 版本号命名 | 完整历史 |
| **可观测** | Actuator 端点 | 实时监控 |

### 部署效率

- 🚀 总部署时间：8-15 分钟
- ⚡ 服务停机时间：< 30 秒
- 🔄 自动回滚时间：< 30 秒
- 💾 完整备份：所有版本可恢复

配置完成后，推送一个 tag 开始你的自动化部署之旅吧！🎉
