# 前端自动部署配置指南

本文档介绍如何配置 GitHub Actions 自动部署前端到服务器。

## 📋 前置要求

1. ✅ 一台可以通过 SSH 访问的服务器
2. ✅ 服务器上已安装 Nginx 或其他 Web 服务器
3. ✅ 服务器有足够的磁盘空间
4. ✅ GitHub 仓库的 Actions 权限已启用

## 🔧 配置步骤

### 步骤 1: 生成 SSH 密钥对

在你的**本地电脑**上生成 SSH 密钥（或使用现有的）：

```bash
# 生成新的 SSH 密钥对（如果还没有）
ssh-keygen -t ed25519 -C "github-actions-deploy" -f ~/.ssh/github_actions

# 查看私钥（稍后需要添加到 GitHub Secrets）
cat ~/.ssh/github_actions

# 查看公钥（稍后需要添加到服务器）
cat ~/.ssh/github_actions.pub
```

### 步骤 2: 配置服务器

#### 2.1 添加公钥到服务器

```bash
# SSH 登录到你的服务器
ssh your-username@your-server-ip

# 添加公钥到授权文件
echo "你的公钥内容" >> ~/.ssh/authorized_keys

# 设置正确的权限
chmod 600 ~/.ssh/authorized_keys
chmod 700 ~/.ssh
```

#### 2.2 创建部署目录

```bash
# 创建部署目录
sudo mkdir -p /var/www/ai-knowledge-base

# 设置所有者（替换为你的用户名）
sudo chown -R your-username:your-username /var/www/ai-knowledge-base

# 设置权限
chmod -R 755 /var/www/ai-knowledge-base
```

#### 2.3 配置 Nginx（如果使用 Nginx）

创建或编辑 Nginx 配置：

```bash
sudo vim /etc/nginx/sites-available/ai-knowledge-base
```

添加以下内容：

```nginx
server {
    listen 80;
    server_name your-domain.com;  # 替换为你的域名或 IP
    
    root /var/www/ai-knowledge-base;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
    
    # Gzip 压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript 
               application/x-javascript application/xml+rss 
               application/json application/javascript;
}
```

启用站点并重启 Nginx：

```bash
# 创建符号链接
sudo ln -s /etc/nginx/sites-available/ai-knowledge-base /etc/nginx/sites-enabled/

# 测试配置
sudo nginx -t

# 重启 Nginx
sudo systemctl restart nginx
```

### 步骤 3: 配置 GitHub Environment Secrets

本项目使用 **环境级别的 Secrets**，更安全且支持部署保护规则。

1. **进入 GitHub 仓库**
2. **Settings** → **Environments**
3. **选择 "pro" 环境**（如果没有，点击 "New environment" 创建）
4. **在 "Environment secrets" 区域添加以下 Secrets**：

| Secret 名称 | 说明 | 示例值 |
|------------|------|--------|
| `FRONTEND_SERVER_HOST` | 服务器 IP 或域名 | `192.168.1.100` 或 `server.example.com` |
| `FRONTEND_SERVER_USERNAME` | SSH 用户名 | `ubuntu` 或 `root` |
| `FRONTEND_SERVER_SSH_KEY` | SSH 私钥内容 | 整个私钥文件内容（包括 BEGIN 和 END） |
| `FRONTEND_SERVER_PORT` | SSH 端口 | `22` |
| `FRONTEND_DEPLOY_DIR` | 部署目录 | `/var/www/ai-knowledge-base` |

#### 添加 SSH 私钥示例：

**SECRET_NAME**: `FRONTEND_SERVER_SSH_KEY`

**VALUE**: 
```
-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtz
c2gtZWQyNTUxOQAAACDxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
... (完整的私钥内容) ...
-----END OPENSSH PRIVATE KEY-----
```

> 💡 **提示**: 详细的环境配置说明请查看 [`docs/guide/github-environments-setup.md`](./github-environments-setup.md)

### 步骤 4: 测试部署

#### 方法 1: 推送代码触发

```bash
# 修改 frontend 目录下的任何文件
cd frontend
echo "// test deploy" >> src/App.tsx

# 提交并推送
git add .
git commit -m "test: 测试自动部署"
git push origin main
```

#### 方法 2: 手动触发

1. 进入 GitHub 仓库
2. 点击 **Actions** 标签
3. 选择 **部署前端到服务器** workflow
4. 点击 **Run workflow**
5. 选择环境（production/staging）
6. 点击 **Run workflow** 按钮

## 📊 工作流程说明

### 触发条件

```yaml
on:
  push:
    branches: [main]
    paths: ['frontend/**']  # 仅当 frontend 目录变化时触发
```

**说明**：
- ✅ 只有修改 `frontend/` 目录下的文件才会触发部署
- ✅ 修改后端代码不会触发前端部署
- ✅ 可以手动触发（workflow_dispatch）

### 部署流程

```
1. 检出代码
   ↓
2. 设置 Node.js 环境
   ↓
3. 安装依赖（npm ci）
   ↓
4. 运行测试
   ↓
5. 构建生产版本（npm run build）
   ↓
6. 压缩构建产物（dist.tar.gz）
   ↓
7. 上传到服务器 /tmp 目录
   ↓
8. 备份当前版本
   ↓
9. 解压并部署到目标目录
   ↓
10. 设置文件权限
   ↓
11. 清理临时文件和旧备份
   ↓
12. 验证部署
```

### 安全特性

- 🔐 **自动备份**: 每次部署前自动备份当前版本
- 🔙 **回滚机制**: 保留最近 3 个备份版本
- 🔒 **权限控制**: 自动设置正确的文件权限
- 🧪 **测试验证**: 部署前先运行测试

## 🎯 常见问题

### Q1: SSH 连接失败？

**检查清单**：
```bash
# 1. 测试 SSH 连接
ssh -i ~/.ssh/github_actions your-username@your-server-ip

# 2. 检查服务器 SSH 配置
sudo nano /etc/ssh/sshd_config
# 确保包含：
# PubkeyAuthentication yes
# PasswordAuthentication no

# 3. 重启 SSH 服务
sudo systemctl restart sshd
```

### Q2: 权限错误？

```bash
# 在服务器上设置正确的所有者
sudo chown -R your-username:your-username /var/www/ai-knowledge-base

# 或者给予更宽松的权限
sudo chmod -R 777 /var/www/ai-knowledge-base  # 不推荐生产环境
```

### Q3: 如何查看部署日志？

1. GitHub 仓库 → **Actions** 标签
2. 选择具体的运行记录
3. 点击 **deploy-frontend** job
4. 展开每个步骤查看详细日志

### Q4: 如何回滚到之前的版本？

```bash
# SSH 登录服务器
ssh your-username@your-server-ip

# 查看备份
ls -la /var/www/ai-knowledge-base_backup_*

# 回滚到某个备份
sudo rm -rf /var/www/ai-knowledge-base
sudo cp -r /var/www/ai-knowledge-base_backup_20241010_143022 /var/www/ai-knowledge-base

# 重启 Nginx
sudo systemctl restart nginx
```

### Q5: 如何部署到多个环境？

修改 workflow，添加环境配置：

```yaml
env:
  STAGING_DIR: /var/www/staging
  PRODUCTION_DIR: /var/www/production

jobs:
  deploy:
    steps:
      - name: 设置部署目录
        run: |
          if [ "${{ github.event.inputs.environment }}" == "production" ]; then
            echo "DEPLOY_DIR=/var/www/production" >> $GITHUB_ENV
          else
            echo "DEPLOY_DIR=/var/www/staging" >> $GITHUB_ENV
          fi
```

## 📈 性能优化建议

### 1. 启用 CDN（可选）

在 Nginx 配置中添加：
```nginx
# 添加缓存头
add_header Cache-Control "max-age=31536000, public";
```

### 2. 压缩传输

Workflow 已经使用 tar.gz 压缩，可以进一步优化：
```bash
# 使用更高压缩率
tar -czf --best dist.tar.gz dist/
```

### 3. 增量部署

只上传变化的文件：
```bash
rsync -avz --delete dist/ user@server:/var/www/ai-knowledge-base/
```

## 🔔 添加部署通知

### Slack 通知

```yaml
- name: Slack 通知
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    text: '前端部署完成！'
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
  if: always()
```

### 钉钉通知

```yaml
- name: 钉钉通知
  uses: zcong1993/actions-ding@v3
  with:
    dingToken: ${{ secrets.DING_TOKEN }}
    body: |
      {
        "msgtype": "text",
        "text": {
          "content": "前端部署成功！\n提交: ${{ github.sha }}"
        }
      }
```

## 🚀 高级配置

### 多服务器部署

```yaml
strategy:
  matrix:
    server:
      - { host: '192.168.1.100', dir: '/var/www/server1' }
      - { host: '192.168.1.101', dir: '/var/www/server2' }
steps:
  - name: 部署到 ${{ matrix.server.host }}
    uses: appleboy/ssh-action@v1.0.3
    with:
      host: ${{ matrix.server.host }}
      # ... 其他配置
```

### 蓝绿部署

```bash
# 部署到新目录
DEPLOY_DIR="/var/www/ai-knowledge-base-new"
# 切换符号链接
ln -sfn /var/www/ai-knowledge-base-new /var/www/current
# 重启服务
nginx -s reload
```

## 📚 相关资源

- [GitHub Actions 文档](https://docs.github.com/en/actions)
- [SSH Action 文档](https://github.com/appleboy/ssh-action)
- [SCP Action 文档](https://github.com/appleboy/scp-action)
- [Nginx 配置指南](https://nginx.org/en/docs/)

---

## ✅ 验证清单

部署前请确认：

- [ ] SSH 密钥已正确配置
- [ ] GitHub Secrets 已全部添加
- [ ] 服务器部署目录已创建并有正确权限
- [ ] Nginx 配置已正确设置
- [ ] 测试环境已验证通过
- [ ] 备份机制已测试

祝部署顺利！🎉

