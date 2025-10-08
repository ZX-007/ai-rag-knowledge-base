## AI RAG 前端（React + Vite + Ant Design）

一个简洁的对话式前端，支持模型选择、RAG 知识库标签选择、文档上传与 Git 仓库分析，配合后端进行流式对话与知识检索。

### 功能概览
- **聊天对话**：支持服务端事件（SSE）流式返回，实时渲染对话内容。
- **模型选择**：从后端获取可用模型，切换后立即生效。
- **RAG 标签**：选择知识库标签启用检索增强；可为空。
- **文件上传**：上传文档至指定知识库标签。
- **Git 仓库分析**：输入仓库地址（可含认证信息），触发后端分析并导入知识库。
- **深浅色主题**：一键切换，偏好保存在 localStorage。

### 快速开始
1) 安装依赖
```bash
cd frontend
npm install
```

2) 本地开发
```bash
npm run dev
```
默认开发端口为 `3000`（见 `vite.config.ts`）。

3) 生产构建与本地预览
```bash
npm run build
npm run preview
```

4) 单元测试
```bash
npm test           # 一次性跑完
npm run test:watch # 监听文件变化
```

### 环境变量
可通过 Vite 环境变量调整后端地址与超时时间（需以 `VITE_` 前缀开头）。
- `VITE_API_PREFIX`：API 前缀，默认 `/api/v1`
- `VITE_API_TIMEOUT_MS`：请求超时（毫秒），默认 `15000`

在根目录或 `frontend/` 下创建 `.env.local` 示例：
```bash
VITE_API_PREFIX=/api/v1
VITE_API_TIMEOUT_MS=15000
```

### 与后端的接口约定（摘要）
- 模型列表：`GET {VITE_API_PREFIX}/ollama/models`
- RAG 标签：`GET {VITE_API_PREFIX}/rag/query_rag_tag_list`
- 普通流式对话：`POST {VITE_API_PREFIX}/ollama/generate_stream`（SSE）
- RAG 流式对话：`POST {VITE_API_PREFIX}/ollama/generate_stream_rag`（SSE）
- 文档上传：`POST {VITE_API_PREFIX}/rag/file/upload`（FormData）
- Git 仓库分析：`POST {VITE_API_PREFIX}/rag/analyze_git_repository`

前端在 `src/services/api.ts` 中做了统一封装，并内置对后端返回码 `2000 / 20000` 的兼容处理。

### 目录结构
```
frontend/
├─ src/
│  ├─ components/        # UI 组件：消息列表、输入框、选择器、上传、Git 分析、主题切换等
│  ├─ services/          # API 封装（SSE 流式解析、上传、Git 分析等）
│  ├─ styles/            # Markdown 与全局样式
│  ├─ utils/             # 工具库（SSE 解析、时间格式化）
│  ├─ types/             # TypeScript 类型定义
│  ├─ App.tsx            # 应用入口组件
│  └─ main.tsx           # 渲染入口与 Antd Provider
├─ index.html
├─ package.json
└─ vite.config.ts
```

### 关键实现说明
- **流式解析**：`src/utils/streamParser.ts` 兼容多种常见提供商返回结构（OpenAI/Anthropic/自定义等），并支持解析 `<think>...</think>` 思考片段，前端以折叠卡展示。
- **消息模型**：`src/types/index.ts` 定义 `Message`、`ChatRequest`、`RagChatRequest` 等类型。
- **UI 体验**：
  - 顶部固定控制区：模型、知识库、上传、Git 分析、主题切换。
  - 中部消息区：AI 消息支持 Markdown（表格/代码高亮/公式）。
  - 底部输入区：`Enter` 发送，`Shift+Enter` 换行。

### 常见问题（FAQ）
- 看不到模型列表？
  - 请确认后端已启动，且 `VITE_API_PREFIX` 正确指向后端网关。
- RAG 标签为空？
  - 前端会降级为空列表并不报错；可尝试上传文档或执行 Git 仓库分析后再试。
- SSE 无法连接？
  - 确认后端 SSE 接口已开启 CORS/代理转发；本地开发时建议通过网关或 Vite 代理统一 `/api` 前缀。

### 许可证
遵循仓库根目录的 `LICENSE`。

