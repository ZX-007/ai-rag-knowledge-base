# MCP Server CSDN - 脚本工具

本目录包含用于构建和管理 MCP Server CSDN Docker 容器的便捷脚本。

## 📁 文件列表

| 文件 | 说明 | 平台 |
|------|------|------|
| `build.cmd` | 构建 Docker 镜像 | Windows |
| `run.cmd` | 容器管理脚本 | Windows |
| `stop.cmd` | 停止容器脚本 | Windows |

## 🚀 快速使用

### 1. 构建镜像

```bash
# 从项目根目录执行
scripts\build.cmd

# 或进入 scripts 目录
cd scripts
build.cmd
```

### 2. 启动容器

```bash
# 从项目根目录执行
scripts\run.cmd

# 或进入 scripts 目录
cd scripts
run.cmd
```

### 3. 停止容器

```bash
# 从项目根目录执行
scripts\stop.cmd

# 或进入 scripts 目录
cd scripts
stop.cmd
```

## 📝 脚本详情

### build.cmd - 构建脚本

**功能：**
- ✅ Maven 构建（自动检测）
- ✅ Docker 镜像构建
- ✅ 导出镜像到 tar
- ✅ 可选启动容器

**用法：**
```bash
scripts\build.cmd [选项]

选项：
  --no-cache      清理缓存构建
  --auto-start    自动启动容器
  --help, -h      显示帮助
```

**示例：**
```bash
scripts\build.cmd                    # 标准构建
scripts\build.cmd --no-cache         # 清理缓存
scripts\build.cmd --auto-start       # 构建并启动
```

**工作流程：**
```
检查 JAR 文件
    ↓
不存在 → Maven 构建
    ↓
Docker 构建镜像
    ↓
导出镜像到 tar
    ↓
询问是否启动容器
```

---

### run.cmd - 容器管理脚本

**功能：**
- ✅ 启动/停止/重启容器
- ✅ 查看容器状态和日志
- ✅ 健康检查
- ✅ 进入容器 Shell

**用法：**
```bash
scripts\run.cmd [操作]
```

**可用操作：**

| 操作 | 说明 | 示例 |
|------|------|------|
| `start` | 启动容器（默认） | `scripts\run.cmd start` |
| `stop` | 停止并删除容器 | `scripts\run.cmd stop` |
| `restart` | 重启容器 | `scripts\run.cmd restart` |
| `logs` | 查看容器日志（实时） | `scripts\run.cmd logs` |
| `status` | 查看容器状态 | `scripts\run.cmd status` |
| `health` | 执行健康检查 | `scripts\run.cmd health` |
| `shell` | 进入容器 Shell | `scripts\run.cmd shell` |
| `help` | 显示帮助信息 | `scripts\run.cmd help` |

**示例：**

```bash
# 启动容器
scripts\run.cmd
scripts\run.cmd start

# 查看状态
scripts\run.cmd status

# 查看日志（Ctrl+C 退出）
scripts\run.cmd logs

# 健康检查
scripts\run.cmd health

# 重启容器
scripts\run.cmd restart

# 进入容器 Shell
scripts\run.cmd shell

# 停止容器
scripts\run.cmd stop
```

---

### stop.cmd - 停止容器脚本

**功能：**
- ✅ 优雅停止容器
- ✅ 强制停止
- ✅ 删除容器

**用法：**
```bash
scripts\stop.cmd [选项]

选项：
  --remove, -r    停止并删除
  --kill, -k      强制停止
  --help, -h      显示帮助
```

**示例：**
```bash
scripts\stop.cmd              # 优雅停止
scripts\stop.cmd --remove     # 停止并删除
scripts\stop.cmd --kill       # 强制停止
```

**停止模式：**

| 模式 | 命令 | 删除容器 |
|------|------|---------|
| 优雅停止 | `stop.cmd` | 否 |
| 强制停止 | `stop.cmd --kill` | 否 |
| 停止删除 | `stop.cmd --remove` | 是 |

## 📚 常见场景

### 场景 1：首次构建和部署

```bash
# 1. 构建镜像
scripts\build.cmd

# 2. 选择 Y 启动容器
# 或手动启动
scripts\run.cmd start

# 3. 查看日志确认启动成功
scripts\run.cmd logs
```

### 场景 2：代码更新后重新部署

```bash
# 1. 重新构建镜像
scripts\build.cmd

# 2. 重启容器
scripts\run.cmd restart

# 3. 健康检查
scripts\run.cmd health
```

### 场景 3：排查问题

```bash
# 1. 查看日志
scripts\run.cmd logs

# 2. 进入容器检查
scripts\run.cmd shell

# 3. 查看环境变量
env | grep CSDN
```

### 场景 4：清理和重建

```bash
# 1. 停止并删除容器
scripts\stop.cmd --remove

# 2. 清理缓存重新构建
scripts\build.cmd --no-cache

# 3. 启动新容器
scripts\run.cmd start
```

### 场景 5：快速停止和重启

```bash
# 1. 快速停止容器
scripts\stop.cmd

# 2. 等待一段时间或修改配置

# 3. 重新启动
scripts\run.cmd start
```

### 场景 6：强制停止无响应容器

```bash
# 1. 尝试优雅停止
scripts\stop.cmd

# 2. 如果容器无响应，强制停止
scripts\stop.cmd --kill

# 3. 清理并重新部署
scripts\stop.cmd --remove
scripts\build.cmd
```

## ⚠️ 注意事项

1. **需要 Docker 运行**
   - 确保 Docker Desktop 已启动
   - 脚本会自动检测 Docker 状态

2. **环境变量配置**
   - 首次使用前需配置 `.env` 文件
   - 参考 `env.example` 文件

3. **端口冲突**
   - 默认使用端口 8081
   - 如有冲突，修改 `docker-compose.yml`

4. **日志查看**
   - 使用 `Ctrl+C` 退出日志查看
   - 不会停止容器

## 🐛 故障排查

### 问题：脚本提示"命令不存在"

**原因：** 脚本不在 PATH 中

**解决：** 使用完整路径或先 cd 到目录

```bash
# 方式 1：使用完整路径
scripts\build.cmd

# 方式 2：切换目录
cd scripts
build.cmd
```

### 问题：Docker 构建失败

**原因：** 缓存问题或依赖下载失败

**解决：**
```bash
# 清理缓存重新构建
scripts\build.cmd --no-cache
```

### 问题：容器启动失败

**原因：** 环境变量未配置或配置错误

**解决：**
```bash
# 检查环境变量
scripts\run.cmd shell
env | grep CSDN

# 查看详细日志
scripts\run.cmd logs
```

## 📖 更多文档

- [项目 README](../README.md) - 项目总览
- [Docker 指南](../docs/DOCKER.md) - Docker 详细文档
- [配置指南](../docs/CONFIG.md) - 配置说明

## 💡 提示

- 使用 Tab 键可以自动补全文件名
- 脚本输出使用颜色标识状态（INFO/SUCCESS/ERROR）
- 所有操作都有错误检查和友好提示

