## AI RAG 前端（React + TypeScript + Vite + Ant Design）

一个现代化的对话式AI前端应用，支持模型选择、RAG知识库检索、文档上传与Git仓库分析，配合后端进行流式对话与知识检索。

### ✨ 功能特性

#### 核心功能
- **💬 智能对话**：支持服务端事件（SSE）流式返回，实时渲染对话内容
  - 支持流式响应，边接收边展示
  - 支持中断正在生成的回复
  - 支持 Markdown 渲染（表格、代码高亮、数学公式等）
  - 支持思考过程展示（`<think>` 标签）

- **🤖 模型管理**：从后端动态获取可用模型列表，支持快速切换
  - 自动选择第一个可用模型
  - 支持多种 AI 模型（OpenAI、Anthropic、本地模型等）

- **📚 知识库（RAG）**：
  - 选择知识库标签启用检索增强生成
  - 支持文档上传（PDF、DOC、DOCX、TXT、MD 等格式）
  - 支持 Git 仓库分析与导入
  - 自动关联上传的文档到知识库

- **🎨 用户体验**：
  - 深浅色主题一键切换，偏好保存在 localStorage
  - 响应式设计，适配各种屏幕尺寸
  - 优雅的动画效果
  - 错误边界保护，避免应用崩溃
  - 友好的错误提示

#### 技术亮点
- **⚡ 性能优化**：使用 React.memo、useMemo、useCallback 等优化手段
- **🔒 类型安全**：完整的 TypeScript 类型定义
- **🎯 代码分割**：自动分包优化，减小首屏加载体积
- **🛡️ 错误处理**：统一的错误处理机制，友好的用户提示
- **🧪 单元测试**：关键组件和工具函数均有测试覆盖

### 🚀 快速开始

#### 前置要求
- Node.js >= 16.0.0
- npm >= 7.0.0 或 yarn >= 1.22.0

#### 安装依赖
```bash
cd frontend
npm install
```

#### 开发模式
```bash
npm run dev
```
应用将在 `http://localhost:3000` 启动（默认端口可在 `vite.config.ts` 中修改）。

#### 生产构建
```bash
npm run build
```
构建产物将输出到 `dist/` 目录。

#### 本地预览构建产物
```bash
npm run preview
```

#### 运行测试
```bash
npm test           # 一次性运行所有测试
npm run test:watch # 监听模式，文件变化时自动重新运行
```

### ⚙️ 环境变量配置

可通过 Vite 环境变量调整后端地址与超时时间（需以 `VITE_` 前缀开头）。

#### 可用环境变量
| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `VITE_API_PREFIX` | API 基础路径前缀 | `/api/v1` |
| `VITE_API_TIMEOUT_MS` | API 请求超时时间（毫秒） | `15000` |
| `VITE_PROXY_TARGET` | 开发环境代理目标地址 | `http://localhost:8080` |

#### 配置方式
在 `frontend/` 目录下创建 `.env.local` 文件（已在 `.gitignore` 中，不会提交到版本控制）：

```bash
# API 配置
VITE_API_PREFIX=/api/v1
VITE_API_TIMEOUT_MS=15000

# 开发环境代理（如果后端运行在不同端口）
VITE_PROXY_TARGET=http://localhost:8080
```

参考 `.env.example` 文件了解所有可配置项。

### 🔌 API 接口说明

前端通过以下接口与后端通信（所有接口统一在 `src/services/api.ts` 中封装）：

| 功能 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取模型列表 | `GET` | `/chat/models` | 获取可用的AI模型列表 |
| 获取知识库标签 | `GET` | `/rag/query_rag_tag_list` | 获取所有知识库标签 |
| 普通对话 | `POST` | `/chat/generate_stream` | SSE流式对话 |
| RAG对话 | `POST` | `/chat/generate_stream_rag` | 基于知识库的SSE流式对话 |
| 上传文档 | `POST` | `/rag/file/upload` | 上传文档到知识库（FormData） |
| 分析Git仓库 | `POST` | `/rag/analyze_git_repository` | 分析并导入Git仓库 |

#### 响应格式
后端统一返回格式：
```typescript
{
  code: string;      // 成功: "2000" 或 "20000"
  info: string;      // 消息说明
  data: T;           // 实际数据
  timestamp: string; // 时间戳
  traceId?: string;  // 追踪ID（可选）
}
```

前端已内置对 `2000` 和 `20000` 两种成功码的兼容处理。

### 📁 项目结构

```
frontend/
├─ src/
│  ├─ components/              # UI 组件
│  │  ├─ ErrorBoundary.tsx     # 错误边界组件
│  │  ├─ FileUpload.tsx        # 文件上传组件
│  │  ├─ GitRepoAnalyze.tsx    # Git仓库分析组件
│  │  ├─ Loading.tsx           # 加载状态组件
│  │  ├─ MarkdownText.tsx      # Markdown渲染组件
│  │  ├─ MessageInput.tsx      # 消息输入组件
│  │  ├─ MessageList.tsx       # 消息列表组件
│  │  ├─ ModelSelector.tsx     # 模型选择器
│  │  ├─ RagTagSelector.tsx    # 知识库选择器
│  │  ├─ StreamingText.tsx     # 流式文本组件
│  │  ├─ ThemeToggle.tsx       # 主题切换组件
│  │  ├─ ThinkingTimer.tsx     # 思考计时器组件
│  │  └─ __tests__/            # 组件测试
│  ├─ services/                # 服务层
│  │  ├─ api.ts                # API服务封装
│  │  └─ __tests__/            # 服务测试
│  ├─ utils/                   # 工具函数
│  │  ├─ errorHandler.ts       # 错误处理工具
│  │  ├─ streamParser.ts       # 流式数据解析器
│  │  ├─ time.ts               # 时间格式化工具
│  │  └─ __tests__/            # 工具测试
│  ├─ types/                   # TypeScript 类型定义
│  │  └─ index.ts              # 全局类型
│  ├─ styles/                  # 样式文件
│  │  └─ markdown.css          # Markdown样式
│  ├─ App.tsx                  # 应用主组件
│  ├─ main.tsx                 # 应用入口
│  └─ index.css                # 全局样式
├─ public/                     # 静态资源
├─ dist/                       # 构建输出（自动生成）
├─ index.html                  # HTML 模板
├─ package.json                # 依赖配置
├─ tsconfig.json               # TypeScript 配置
├─ vite.config.ts              # Vite 配置
└─ README.md                   # 本文档
```

### 💡 核心技术实现

#### 流式响应处理
- **位置**：`src/utils/streamParser.ts`
- **功能**：
  - 兼容多种AI提供商的响应格式（OpenAI、Anthropic、自定义等）
  - 支持解析 `<think>...</think>` 标签，展示AI思考过程
  - 自动检测响应完成标志
  - 统一的流式数据解析接口

#### 错误处理机制
- **位置**：`src/utils/errorHandler.ts`
- **功能**：
  - 统一的错误分类（网络错误、API错误、验证错误等）
  - 自动解析错误并转换为用户友好的提示
  - 开发环境下的详细日志记录
  - 自定义 AppError 类型

#### 性能优化
- **React.memo**：对所有展示型组件进行记忆化，避免不必要的重渲染
- **useMemo**：缓存计算结果（如选项列表的转换）
- **useCallback**：缓存回调函数，避免子组件重渲染
- **代码分割**：自动将大型依赖（React、Ant Design、Markdown库）分包

#### UI/UX 设计
- **顶部控制栏**：固定定位，包含模型选择、知识库选择、文件上传、Git分析、主题切换
- **消息展示区**：
  - 支持完整的 Markdown 语法（GFM）
  - 代码块自动高亮（highlight.js）
  - 数学公式渲染（KaTeX）
  - 流式打字机效果
- **输入区**：
  - 胶囊式设计，美观现代
  - 左侧功能菜单（清空对话、附加文件等）
  - 右侧发送/停止按钮，根据状态自动切换
  - `Enter` 发送，`Shift+Enter` 换行

### ❓ 常见问题（FAQ）

#### 1. 看不到模型列表？
**原因**：无法连接到后端服务

**解决方案**：
- 确认后端服务已启动
- 检查 `VITE_API_PREFIX` 环境变量是否正确配置
- 检查浏览器控制台的网络请求，确认请求地址正确
- 如果使用代理，确认 `vite.config.ts` 中的代理配置正确

#### 2. RAG 标签列表为空？
**原因**：知识库中还没有任何文档

**解决方案**：
- 使用"上传文档"功能上传文件到知识库
- 使用"分析 Git 仓库"功能导入代码仓库
- 后端可能还未初始化知识库

#### 3. SSE 流式响应无法连接？
**原因**：跨域或代理配置问题

**解决方案**：
- 确认后端 SSE 接口已开启 CORS
- 本地开发时使用 Vite 代理（`vite.config.ts` 中已配置）
- 生产环境使用 Nginx 等反向代理统一处理跨域

#### 4. 文件上传失败？
**原因**：文件格式不支持或文件过大

**解决方案**：
- 确认文件格式在支持的范围内（PDF、DOC、DOCX、TXT、MD等）
- 检查后端对文件大小的限制
- 查看浏览器控制台的具体错误信息

#### 5. Git 仓库分析失败？
**原因**：仓库地址错误或认证信息不正确

**解决方案**：
- 确认仓库地址格式正确（如 `https://github.com/org/repo.git`）
- 私有仓库需要提供正确的用户名和访问令牌
- 检查后端服务器能否访问该仓库（网络、防火墙等）

#### 6. 深色主题下某些元素显示不正常？
**原因**：自定义组件或第三方组件未适配深色主题

**解决方案**：
- 检查 `index.css` 中的深色主题变量定义
- 确保使用了 CSS 变量而非硬编码颜色值
- 如发现问题请提交 Issue

### 🔧 开发指南

#### 添加新组件
1. 在 `src/components/` 目录下创建新组件文件
2. 使用 TypeScript 定义 Props 接口
3. 使用 React.memo 包装展示型组件
4. 为复杂组件添加单元测试（`__tests__/` 目录）

#### 添加新的 API 接口
1. 在 `src/types/index.ts` 中定义请求和响应类型
2. 在 `src/services/api.ts` 中添加方法
3. 使用统一的错误处理机制
4. 添加适当的日志记录

#### 样式开发规范
- 优先使用 Ant Design 的组件和主题系统
- 自定义样式使用 CSS 变量，支持主题切换
- 避免硬编码颜色值
- 遵循响应式设计原则

### 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

#### 提交代码前请确保：
1. 代码通过 TypeScript 类型检查
2. 新功能有相应的测试覆盖
3. 遵循现有的代码风格
4. 更新相关文档

### 📄 许可证

遵循仓库根目录的 `LICENSE` 文件。

### 📞 联系方式

如有问题或建议，请通过以下方式联系：
- 提交 GitHub Issue
- 查看项目文档

---

**Happy Coding! 🎉**

