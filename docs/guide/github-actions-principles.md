# GitHub Actions 工作原理详解

## 📚 目录
1. [整体架构](#整体架构)
2. [核心组件](#核心组件)
3. [工作流程](#工作流程)
4. [Runner 运行机制](#runner-运行机制)
5. [事件触发机制](#事件触发机制)
6. [安全机制](#安全机制)
7. [缓存机制](#缓存机制)

---

## 整体架构

GitHub Actions 采用**事件驱动架构**，主要由以下几个部分组成：

```
┌──────────────────────────────────────────────────────────┐
│                    GitHub 平台                           │
│  ┌─────────────┐    ┌──────────────┐    ┌────────────┐   │
│  │  仓库事件    │───▶│ Workflow 引擎 │──▶│ Job 队列    │  │
│  │  (Push/PR)  │    │  (调度器)     │    │            │   │
│  └─────────────┘    └──────────────┘    └────────────┘   │
│                                              │           │
└──────────────────────────────────────────────┼───────────┘
                                               │
                                               ▼
                        ┌────────────────────────────────┐
                        │         Runner 池              │
                        │  ┌──────────┐  ┌──────────┐    │
                        │  │ Runner 1 │  │ Runner 2 │    │
                        │  │ (Ubuntu) │  │ (Windows)│    │
                        │  └──────────┘  └──────────┘    │
                        │  ┌──────────┐  ┌──────────┐    │
                        │  │ Runner 3 │  │ Runner N │    │
                        │  │ (macOS)  │  │  (Self)  │    │
                        │  └──────────┘  └──────────┘    │
                        └────────────────────────────────┘
```

---

## 核心组件

### 1. **Workflow 引擎**
- **职责**: 解析 YAML 配置文件，管理工作流生命周期
- **功能**:
  - 监听仓库事件
  - 解析 `.github/workflows/*.yml` 文件
  - 根据触发条件决定是否执行
  - 创建 Job 并分配到 Runner

**YAML 解析示例**:
```yaml
on: push  # 引擎监听这个事件
jobs:
  build:  # 引擎创建这个 Job
    runs-on: ubuntu-latest  # 引擎分配 Runner 类型
    steps:  # 引擎按顺序执行步骤
      - run: echo "Hello"
```

### 2. **Runner（运行器）**
Runner 是实际执行任务的计算环境。

**类型**:
- **GitHub-hosted Runners**: GitHub 提供的云端虚拟机
  - Ubuntu (Linux)
  - Windows
  - macOS
- **Self-hosted Runners**: 用户自己部署的运行器
  - 可以在本地服务器、云主机上运行
  - 更多控制权，可以访问内网资源

**Runner 的本质**:
```
Runner = 虚拟机 + GitHub Actions Runner 软件
```

### 3. **Actions（可重用组件）**
Actions 是独立的可重用代码单元，本质上是：
- **JavaScript Actions**: Node.js 脚本
- **Docker Actions**: Docker 容器
- **Composite Actions**: 组合多个步骤

---

## 工作流程

### 完整执行流程：

```
1. 触发事件
   ↓
2. GitHub 检测到事件（如 git push）
   ↓
3. Workflow 引擎读取 .github/workflows/ 下的 YAML 文件
   ↓
4. 检查触发条件（on: push, branches 等）
   ↓
5. 如果匹配，创建 Workflow Run（工作流运行实例）
   ↓
6. 解析 Jobs，确定执行顺序（并行 or 串行）
   ↓
7. 从 Runner 池中分配可用的 Runner
   ↓
8. Runner 拉取仓库代码到虚拟机
   ↓
9. 按顺序执行每个 Step
   ↓
10. 收集日志和结果
   ↓
11. 清理环境，释放 Runner
   ↓
12. 显示执行结果（成功/失败）
```

### 详细步骤说明

#### Step 1-6: 事件检测
```javascript
// 伪代码：GitHub 内部事件监听
github.on('push', async (event) => {
  const workflows = await loadWorkflows(event.repository);
  for (const workflow of workflows) {
    if (shouldTrigger(workflow, event)) {
      await createWorkflowRun(workflow, event);
    }
  }
});
```

#### Step 7: Runner 分配
```
Job 队列: [Job1(ubuntu), Job2(windows), Job3(ubuntu)]
          ↓
Runner 池: 
  - ubuntu-runner-1 [空闲] ← 分配给 Job1
  - ubuntu-runner-2 [空闲] ← 分配给 Job3
  - windows-runner-1 [空闲] ← 分配给 Job2
```

#### Step 8-9: 代码执行
每个 Runner 在独立的虚拟机中：
```bash
# 1. 创建工作目录
mkdir /home/runner/work/repo-name

# 2. 克隆代码（如果使用 actions/checkout）
git clone https://github.com/user/repo.git

# 3. 执行每个 step
cd /home/runner/work/repo-name
npm install  # 示例命令
npm test     # 示例命令

# 4. 清理
rm -rf /home/runner/work/repo-name
```

---

## Runner 运行机制

### Runner 的生命周期

```
┌─────────────────────────────────────────────┐
│           Runner 虚拟机                      │
│                                             │
│  1. 启动阶段                                 │
│     • 创建全新的虚拟机                        │
│     • 安装预置软件（git, docker, etc）        │
│     • 启动 Runner Agent                      │
│                                              │
│  2. 准备阶段                                 │
│     • 设置环境变量                           │
│     • 创建工作目录                           │
│                                             │
│  3. 执行阶段                                 │
│     • 运行每个 step                          │
│     • 实时上传日志到 GitHub                  │
│     • 捕获输出和错误                         │
│                                             │
│  4. 清理阶段                                 │
│     • 删除工作目录                           │
│     • 销毁虚拟机（GitHub-hosted）             │
│     • 或准备接收下一个 Job（Self-hosted）     │
│                                             │
└─────────────────────────────────────────────┘
```

### Runner Agent 工作原理

Runner Agent 是一个长期运行的进程：

```javascript
// 简化的 Runner Agent 逻辑
class RunnerAgent {
  async start() {
    while (true) {
      // 1. 轮询 GitHub 获取新任务
      const job = await this.pollForJob();
      
      if (job) {
        // 2. 执行任务
        await this.executeJob(job);
        
        // 3. 上传结果
        await this.uploadResults(job);
      }
      
      // 4. 等待下一次轮询
      await sleep(1000);
    }
  }
  
  async executeJob(job) {
    for (const step of job.steps) {
      const result = await this.runStep(step);
      await this.streamLogs(result.logs);
    }
  }
}
```

### 隔离机制

每个 Job 都在**全新的环境**中运行：

```
Job 1 → Runner VM 1 (Ubuntu 22.04) → 执行完毕 → 销毁
Job 2 → Runner VM 2 (Ubuntu 22.04) → 执行完毕 → 销毁
Job 3 → Runner VM 3 (Ubuntu 22.04) → 执行完毕 → 销毁
```

**好处**:
- ✅ 完全隔离，避免任务间干扰
- ✅ 干净的环境，可重复的构建
- ✅ 安全，每次都是全新的系统

**代价**:
- ⏱️ 启动时间（通常 3-10 秒）
- 💰 资源消耗

---

## 事件触发机制

### 1. Webhook 触发

大多数事件通过 GitHub Webhook 触发：

```
开发者操作           GitHub 服务器                Actions 引擎
    │                     │                          │
    │  git push           │                          │
    ├──────────────────▶  │                          │
    │                     │  触发 push webhook        │
    │                     ├─────────────────────────▶│
    │                     │                          │ 解析 workflows
    │                     │                          │ 创建 Workflow Run
    │                     │                          │ 分配 Runner
    │                     │  ◀─────────────────────── │
    │                     │     执行完成通知          │
```

**支持的 Webhook 事件**:
- `push`: 推送代码
- `pull_request`: PR 相关操作
- `issues`: Issue 相关操作
- `release`: 发布版本
- `fork`: 仓库被 fork
- 等 30+ 种事件

### 2. 定时触发 (schedule)

使用 cron 表达式：

```yaml
on:
  schedule:
    - cron: '0 0 * * *'  # 每天 UTC 0:00
```

**实现原理**:
```
GitHub 内部有一个 Cron 调度器
    ↓
每分钟扫描所有 schedule 配置
    ↓
如果时间匹配，触发对应的 Workflow
```

### 3. 手动触发 (workflow_dispatch)

```yaml
on:
  workflow_dispatch:
    inputs:
      environment:
        description: '部署环境'
        required: true
        type: choice
        options:
          - dev
          - prod
```

**实现**: GitHub UI 提供"Run workflow"按钮，调用 API 触发

### 4. API 触发 (repository_dispatch)

通过 GitHub API 远程触发：

```bash
curl -X POST \
  -H "Accept: application/vnd.github+json" \
  -H "Authorization: token YOUR_TOKEN" \
  https://api.github.com/repos/owner/repo/dispatches \
  -d '{"event_type":"deploy"}'
```

---

## 安全机制

### 1. **Secrets 加密存储**

```
用户设置 Secret → 加密存储在 GitHub 数据库
                      │
                      ▼
    Workflow 执行时 → 解密 → 注入到环境变量 → Runner 使用
                      │
                      ▼
    日志中自动脱敏 → ****** （不会显示明文）
```

**加密方式**: 
- 使用 AES-256-GCM 加密
- 每个仓库有独立的加密密钥

### 2. **权限控制**

#### GITHUB_TOKEN
每个 Workflow 自动获得一个临时 token：

```yaml
steps:
  - name: 使用 token
    run: |
      curl -H "Authorization: token ${{ secrets.GITHUB_TOKEN }}" \
        https://api.github.com/repos/${{ github.repository }}
```

**特点**:
- ⏱️ 自动生成，Job 结束后失效
- 🔒 默认只有读权限
- 🎯 可以配置权限范围

```yaml
permissions:
  contents: read
  pull-requests: write
  issues: write
```

### 3. **Fork PR 限制**

来自 Fork 仓库的 PR 默认：
- ❌ 无法访问 Secrets
- ❌ 只能使用只读 token
- ✅ 需要维护者审批才能运行

### 4. **沙箱隔离**

每个 Job 在独立的虚拟机中运行：
- 🔒 文件系统隔离
- 🔒 网络隔离（可配置）
- 🔒 进程隔离

---

## 缓存机制

### 1. 依赖缓存原理

```yaml
- uses: actions/setup-java@v4
  with:
    cache: maven  # 自动缓存 ~/.m2/repository
```

**底层实现**:
```
第一次运行:
  1. 计算缓存 key (基于 pom.xml 哈希)
  2. 执行构建，下载依赖
  3. 上传 ~/.m2/repository 到 GitHub 缓存服务器
  4. 关联 key 和缓存文件

后续运行:
  1. 计算缓存 key
  2. 从缓存服务器下载（如果 key 匹配）
  3. 解压到 ~/.m2/repository
  4. 跳过依赖下载，直接使用
```

### 2. 缓存存储

```
GitHub 缓存服务
    │
    ├─ repo1/
    │   ├─ maven-linux-abc123 (缓存文件)
    │   ├─ npm-linux-def456
    │   └─ docker-layers-ghi789
    │
    └─ repo2/
        └─ ...
```

**限制**:
- 📦 单个缓存最大 10GB
- 💾 仓库总缓存最大 10GB
- ⏰ 缓存 7 天未使用会被删除

### 3. 手动缓存

```yaml
- name: 缓存依赖
  uses: actions/cache@v4
  with:
    path: |
      ~/.m2/repository
      ~/.npm
    key: ${{ runner.os }}-deps-${{ hashFiles('**/pom.xml') }}
    restore-keys: |
      ${{ runner.os }}-deps-
```

**工作流程**:
1. 计算 `key` 的值
2. 查找精确匹配的缓存
3. 如果没有，使用 `restore-keys` 前缀匹配
4. Job 结束时，如果缓存变化，上传新缓存

---

## Artifacts（构建产物）

### 存储机制

```yaml
- uses: actions/upload-artifact@v4
  with:
    name: my-app
    path: dist/
```

**流程**:
```
Runner 执行上传
    ↓
压缩 dist/ 目录
    ↓
上传到 GitHub Artifacts 存储（S3-like）
    ↓
关联到 Workflow Run
    ↓
其他 Job 可以下载
```

**特点**:
- 📁 默认保留 90 天（可配置 1-90 天）
- 💾 免费账户有存储限制
- 🔗 可以通过 GitHub UI 或 API 下载

---

## 计费原理

### GitHub-hosted Runners

**计费单位**: 分钟数

| Runner 类型 | 免费额度/月 | 超出价格        |
|-------------|-------------|----------------|
| Linux       | 2000 分钟   | $0.008/分钟    |
| Windows     | 2000 分钟   | $0.016/分钟    |
| macOS       | 2000 分钟   | $0.08/分钟     |

**计算示例**:
```
Job 运行 5 分钟 (Ubuntu) = 消耗 5 分钟额度
Job 运行 5 分钟 (macOS)  = 消耗 50 分钟额度（10x 倍率）
```

### Self-hosted Runners

- ✅ 完全免费（自己承担硬件成本）
- 🚀 可以更快（访问内网资源）
- 🔧 需要自己维护

---

## 性能优化原理

### 1. 并行执行

```yaml
jobs:
  build-backend:   # 并行执行
    runs-on: ubuntu-latest
    
  build-frontend:  # 并行执行
    runs-on: ubuntu-latest
```

**实现**:
- 每个 Job 分配到不同的 Runner
- 同时执行，缩短总时间

### 2. 矩阵策略

```yaml
strategy:
  matrix:
    os: [ubuntu, windows, macos]
    java: [11, 17, 21]
# 生成 3 × 3 = 9 个并行 Job
```

### 3. 条件跳过

```yaml
- name: 仅在 main 分支构建
  if: github.ref == 'refs/heads/main'
  run: npm run build
```

**原理**: 引擎在分配 Runner 前先评估条件，不满足直接跳过

---

## 总结

GitHub Actions 的核心原理：

1. **事件驱动**: Webhook/Schedule/API 触发
2. **分布式执行**: Workflow 引擎调度，Runner 池执行
3. **隔离安全**: 每个 Job 独立 VM，完全隔离
4. **可扩展**: Actions Marketplace 提供丰富的可重用组件
5. **缓存加速**: 智能缓存依赖和构建产物
6. **灵活计费**: 云端按分钟计费，自托管免费

理解这些原理可以帮助你：
- 🎯 更好地设计 Workflow
- ⚡ 优化执行速度
- 🔒 提升安全性
- 💰 降低成本

---

## 扩展阅读

- [GitHub Actions 架构文档](https://docs.github.com/en/actions/learn-github-actions/understanding-github-actions)
- [Self-hosted Runner 架构](https://docs.github.com/en/actions/hosting-your-own-runners/about-self-hosted-runners)
- [Actions Runner 源码](https://github.com/actions/runner)

