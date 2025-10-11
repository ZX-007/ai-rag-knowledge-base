# 后端自动部署配置指南

## 📋 部署流程说明

后端部署工作流会自动完成以下步骤：

```
1. 推送 tag (如 v1.1.0)
   ↓
2. 构建 Docker 镜像
   ↓
3. 推送到 Docker Hub
   ↓
4. 保存镜像为 tar.gz 文件
   ↓
5. 上传镜像和 docker-compose.yml 到服务器
   ↓
6. 在服务器上加载镜像
   ↓
7. 更新 docker-compose.yml 版本号
   ↓
8. 停止旧服务 → 启动新版本
   ↓
9. 健康检查
   ↓
✅ 部署完成！
```

---

## 🔧 配置步骤

### 步骤 1: 配置 Docker Hub

#### 1.1 注册 Docker Hub 账号

如果还没有账号：
1. 访问 [https://hub.docker.com](https://hub.docker.com)
2. 注册账号
3. 创建仓库（如：`ai-knowledge-base`）

#### 1.2 生成 Access Token

1. 登录 Docker Hub
2. 点击右上角头像 → **Account Settings**
3. 左侧菜单选择 **Security**
4. 点击 **New Access Token**
5. 设置：
   - Description: `GitHub Actions Deploy`
   - Access permissions: `Read, Write, Delete`
6. 点击 **Generate**
7. **复制生成的 token**（只显示一次！）

---

### 步骤 2: 配置服务器

#### 2.1 安装 Docker 和 Docker Compose

```bash
# SSH 登录到服务器
ssh your-username@your-server-ip

# 安装 Docker
curl -fsSL https://get.docker.com | sh

# 启动 Docker 服务
sudo systemctl start docker
sudo systemctl enable docker

# 将当前用户添加到 docker 组（避免每次都用 sudo）
sudo usermod -aG docker $USER

# 重新登录使权限生效
exit
ssh your-username@your-server-ip

# 验证安装
docker --version
docker-compose --version
```

#### 2.2 创建部署目录

```bash
# 创建部署目录
mkdir -p /home/your-username/ai-knowledge-base
cd /home/your-username/ai-knowledge-base

# 创建必要的子目录
mkdir -p logs/app
mkdir -p redis/data
mkdir -p pgvector/data
mkdir -p pgvector/sql

# 设置权限
chmod -R 755 .
```

#### 2.3 准备配置文件

**创建 Redis 配置**：
```bash
cat > redis/redis.conf << 'EOF'
# Redis 配置
requirepass root
bind 0.0.0.0
protected-mode yes
port 6379
tcp-backlog 511
timeout 0
tcp-keepalive 300
daemonize no
supervised no
pidfile /var/run/redis_6379.pid
loglevel notice
logfile ""
databases 16
always-show-logo yes
save 900 1
save 300 10
save 60 10000
stop-writes-on-bgsave-error yes
rdbcompression yes
rdbchecksum yes
dbfilename dump.rdb
dir /data
EOF
```

**创建 PostgreSQL 初始化脚本**：
```bash
cat > pgvector/sql/init.sql << 'EOF'
-- 安装 pgvector 扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 创建表等初始化操作
-- （根据你的实际需求添加）
EOF
```

#### 2.4 配置 SSH 密钥

```bash
# 添加 GitHub Actions 的公钥
mkdir -p ~/.ssh
chmod 700 ~/.ssh

# 编辑 authorized_keys（添加你的公钥）
nano ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
```

---

### 步骤 3: 配置 GitHub Environment Secrets

#### 3.1 创建环境（如果还没有）

1. **进入 GitHub 仓库**
2. **Settings** → **Environments**
3. **点击 "New environment"**
4. **环境名称**: `pro`
5. **点击 "Configure environment"**

#### 3.2 添加以下 Environment Secrets

在 `pro` 环境中添加以下 secrets：

| Secret 名称 | 值 | 说明 |
|------------|-----|------|
| `DOCKER_USERNAME` | `your-dockerhub-username` | Docker Hub 用户名 |
| `DOCKER_PASSWORD` | `your-access-token` | Docker Hub Access Token（步骤1.2生成的）|
| `BACKEND_SERVER_HOST` | `192.168.1.100` | 服务器 IP 或域名 |
| `BACKEND_SERVER_USERNAME` | `ubuntu` | SSH 登录用户名 |
| `BACKEND_SERVER_SSH_KEY` | `-----BEGIN OPENSSH...` | SSH 私钥（无密码） |
| `BACKEND_SERVER_PORT` | `22` | SSH 端口 |
| `BACKEND_DEPLOY_DIR` | `/home/ubuntu/ai-knowledge-base` | 部署目录路径 |

#### 3.3 更新 docker-compose.yml 镜像名称

确保 `docker-compose.yml` 中的镜像名称与你的 Docker Hub 仓库一致：

```yaml
services:
  ai-knowledge-base-app:
    image: your-dockerhub-username/ai-knowledge-base:v1.1.0  # 修改这里
    # ... 其他配置
```

同时更新 workflow 文件中的镜像名称（`.github/workflows/deploy-backend.yml`）：

```yaml
env:
  DOCKER_IMAGE: your-dockerhub-username/ai-knowledge-base  # 修改这里
```

---

## 🚀 使用方法

### 方法 1: 推送 Tag 自动部署（推荐）

```bash
# 1. 确保代码已提交
git add .
git commit -m "feat: 新功能开发完成"

# 2. 创建并推送 tag
git tag v1.1.0
git push origin v1.1.0

# 3. GitHub Actions 会自动触发部署
```

**Tag 命名规范**：
- 格式：`v{major}.{minor}.{patch}`
- 示例：`v1.0.0`, `v1.1.0`, `v2.0.0`
- 遵循 [语义化版本](https://semver.org/lang/zh-CN/)

### 方法 2: 手动触发部署

1. **进入 GitHub 仓库**
2. **Actions** 标签
3. **选择 "部署后端到服务器"**
4. **点击 "Run workflow"**
5. **填写参数**：
   - `tag_version`: 输入版本号（如 `v1.1.0`）
   - `environment`: 选择环境（production/staging）
6. **点击绿色的 "Run workflow"**

---

## 📊 部署流程详解

### 1. 构建 Docker 镜像

使用多阶段构建，优化镜像大小：

```dockerfile
# Build stage: 使用 Maven 构建 JAR
FROM maven:3.9.6-eclipse-temurin-17 AS build
# ... 构建过程

# Runtime stage: 使用 JRE 运行
FROM eclipse-temurin:17-jre AS runtime
# ... 运行配置
```

**优势**：
- ✅ 最终镜像只包含 JRE 和 JAR，体积小
- ✅ 利用 Docker 层缓存，加快构建速度

### 2. 推送到 Docker Hub

```bash
# 推送两个标签
docker push your-username/ai-knowledge-base:v1.1.0  # 版本标签
docker push your-username/ai-knowledge-base:latest  # 最新标签
```

### 3. 保存和传输镜像

```bash
# 保存为压缩文件
docker save image:tag | gzip > image.tar.gz

# 上传到服务器
scp image.tar.gz server:/path/to/deploy/

# 在服务器上加载
docker load < image.tar.gz
```

### 4. Docker Compose 部署

```bash
# 更新 docker-compose.yml 中的版本号
sed -i "s|image: xxx:.*|image: xxx:v1.1.0|g" docker-compose.yml

# 停止旧服务
docker-compose down

# 启动新服务
docker-compose up -d
```

**保留的数据**：
- ✅ 数据库数据（pgvector/data）
- ✅ Redis 数据（redis/data）
- ✅ 应用日志（logs/app）

---

## 🔍 监控和调试

### 查看服务状态

```bash
# SSH 到服务器
ssh your-username@your-server-ip
cd /home/ubuntu/ai-knowledge-base

# 查看运行中的容器
docker-compose ps

# 查看实时日志
docker-compose logs -f ai-knowledge-base-app

# 查看最后 100 行日志
docker-compose logs --tail=100 ai-knowledge-base-app
```

### 健康检查

```bash
# 检查容器状态
docker ps | grep ai-knowledge-base

# 测试应用接口
curl http://localhost:8080/actuator/health

# 进入容器查看
docker exec -it ai-knowledge-base-app bash
```

### 回滚到之前的版本

```bash
# 1. 查看可用的镜像版本
docker images | grep ai-knowledge-base

# 2. 或者加载之前保存的镜像文件
docker load < ai-knowledge-base_20241010_220500.tar.gz

# 3. 修改 docker-compose.yml 版本号
nano docker-compose.yml
# 修改 image: xxx:v1.1.0 为 image: xxx:v1.0.0

# 4. 重新部署
docker-compose down
docker-compose up -d
```

---

## ❓ 常见问题

### Q1: Docker Hub 推送失败？

**A**: 检查 Docker Hub secrets 配置

```bash
# 本地测试登录
docker login -u your-username -p your-token

# 测试推送
docker push your-username/ai-knowledge-base:test
```

### Q2: 镜像加载失败？

**A**: 可能的原因

1. **磁盘空间不足**：
```bash
# 检查磁盘空间
df -h

# 清理未使用的镜像
docker system prune -a
```

2. **镜像文件损坏**：
```bash
# 重新下载镜像
docker pull your-username/ai-knowledge-base:v1.1.0
```

### Q3: 服务启动失败？

**A**: 排查步骤

```bash
# 1. 查看详细日志
docker-compose logs ai-knowledge-base-app

# 2. 检查依赖服务
docker-compose ps

# 3. 检查端口占用
netstat -tulpn | grep 8080

# 4. 检查环境变量
docker inspect ai-knowledge-base-app | grep -A 20 Env
```

### Q4: 数据库连接失败？

**A**: 检查网络和配置

```bash
# 1. 测试数据库连接
docker-compose exec ai-knowledge-base-app ping pgvector

# 2. 检查数据库是否启动
docker-compose ps pgvector

# 3. 查看数据库日志
docker-compose logs pgvector
```

### Q5: 如何配置环境变量？

**A**: 有两种方式

**方式 1**: 在 `docker-compose.yml` 中配置（已采用）
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=pro
  - DATASOURCE_HOST=pgvector
```

**方式 2**: 使用 `.env` 文件
```bash
# 创建 .env 文件
cat > .env << 'EOF'
SPRING_PROFILES_ACTIVE=pro
DATASOURCE_HOST=pgvector
DATASOURCE_PASSWORD=root
EOF
```

---

## 📈 性能优化建议

### 1. 使用 Docker 镜像缓存

Workflow 已配置 GitHub Actions 缓存：
```yaml
cache-from: type=gha
cache-to: type=gha,mode=max
```

### 2. 优化镜像大小

- ✅ 使用多阶段构建
- ✅ 仅包含运行时依赖（JRE 而非 JDK）
- ✅ 使用 `.dockerignore` 排除不必要文件

### 3. 并行构建（多服务项目）

如果有多个服务，可以使用 matrix 策略：

```yaml
strategy:
  matrix:
    service: [api, worker, scheduler]
steps:
  - name: 构建 ${{ matrix.service }}
    run: docker build -t ${{ matrix.service }}:${{ steps.version.outputs.VERSION }} .
```

---

## 🔒 安全最佳实践

1. **使用 Docker Hub Access Token**：不要使用密码
2. **限制 SSH 密钥权限**：专用密钥，仅用于部署
3. **使用环境变量**：敏感信息不要硬编码
4. **定期更新基础镜像**：修复安全漏洞
5. **限制容器权限**：不要使用 `privileged: true`

---

## 📚 相关文档

- [Docker 官方文档](https://docs.docker.com/)
- [Docker Compose 文档](https://docs.docker.com/compose/)
- [GitHub Actions 文档](https://docs.github.com/en/actions)
- [语义化版本规范](https://semver.org/lang/zh-CN/)

---

## ✅ 配置检查清单

部署前请确认：

- [ ] Docker Hub 账号已创建，Access Token 已生成
- [ ] GitHub Environment Secrets 已全部配置（7个）
- [ ] 服务器已安装 Docker 和 Docker Compose
- [ ] 部署目录已创建，权限正确
- [ ] SSH 密钥已配置（无密码）
- [ ] docker-compose.yml 镜像名称已更新
- [ ] workflow 文件镜像名称已更新
- [ ] 本地测试 SSH 连接成功
- [ ] 本地测试 Docker 登录成功

配置完成后，推送一个 tag 测试部署！🚀

