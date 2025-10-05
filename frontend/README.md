# AI RAG Knowledge Base Frontend

这是一个基于 React + Vite 的 AI RAG 知识库前端项目，提供简洁清爽的用户界面来与 AI 模型进行对话和管理知识库。



## 功能特性

- 🤖 **AI 对话**: 支持与多种 AI 模型进行实时对话
- 📡 **流式响应**: 实时接收 AI 生成的回复，提供流畅的用户体验
- 🔍 **RAG 增强**: 基于知识库的检索增强生成，提供更准确的回答
- 📁 **文件上传**: 支持多种格式的文档上传到知识库
- 🎨 **响应式设计**: 适配桌面和移动设备
- ⚡ **快速启动**: 基于 Vite 构建，开发体验优秀



## 技术栈

- **React 18**: 现代化的 React 框架
- **Vite**: 快速的构建工具
- **Lucide React**: 美观的图标库
- **原生 CSS**: 简洁的样式实现



## 快速开始

### 环境要求

- Node.js >= 16.0.0
- npm >= 8.0.0

### 安装依赖

```bash
cd frontend
npm install
```

### 启动开发服务器

```bash
npm run dev
```

项目将在 `http://localhost:3000` 启动

### 构建生产版本

```bash
npm run build
```

### 预览生产版本

```bash
npm run preview
```



## 项目结构

```
frontend/
├── public/                 # 静态资源
├── src/
│   ├── components/         # React 组件
│   │   ├── ChatInterface.jsx    # 聊天界面组件
│   │   ├── FileUpload.jsx       # 文件上传组件
│   │   ├── ModelSelector.jsx    # 模型选择器
│   │   └── RagTagSelector.jsx   # RAG 标签选择器
│   ├── App.jsx            # 主应用组件
│   ├── App.css            # 主应用样式
│   ├── main.jsx           # 应用入口
│   └── index.css          # 全局样式
├── index.html             # HTML 模板
├── package.json           # 项目配置
└── vite.config.js         # Vite 配置
```



## API 接口

项目与后端 API 进行交互，主要接口包括：

### Ollama 接口 (`/api/v1/ollama`)

- `GET /models` - 获取可用模型列表
- `POST /generate_stream` - 流式生成 AI 回复
- `POST /generate_stream_rag` - 流式生成 RAG AI 回复

### RAG 接口 (`/api/v1/rag`)

- `GET /query_rag_tag_list` - 获取 RAG 标签列表
- `POST /file/upload` - 上传文件到知识库

## 流式响应处理

项目支持 Server-Sent Events (SSE) 流式响应，实时显示 AI 生成的内容。响应格式如下：

```json
{
  "result": {
    "metadata": {
      "finishReason": null,
      "contentFilterMetadata": null
    },
    "output": {
      "messageType": "ASSISTANT",
      "properties": {},
      "content": "生成的内容片段",
      "media": []
    }
  },
  "metadata": {
    "usage": {
      "generationTokens": 0,
      "promptTokens": 0,
      "totalTokens": 0
    }
  }
}
```

## 配置说明

### 环境变量（推荐）

为区分本地与生产环境，前端通过环境变量控制后端地址：

- 本地开发：`frontend/.env.development`
  - `VITE_API_PREFIX=http://localhost:8080/api/v1`
  - `VITE_API_TIMEOUT_MS=15000`
- 生产部署：`frontend/.env.production`
  - `VITE_API_PREFIX=/api/v1`（通过 Nginx 反向代理转发）
  - `VITE_API_TIMEOUT_MS=15000`

代码中会优先读取 `VITE_API_PREFIX`，否则回退到默认 `'/api/v1'`（见 `src/services/api.ts`）。

### 代理配置

在 `vite.config.js` 中配置了 API 代理，将 `/api` 请求代理到后端服务器：

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      secure: false
    }
  }
}
```

说明：
- 如果设置了 `VITE_API_PREFIX` 为完整地址（如 `http://localhost:8080/api/v1`），则请求会直接走该地址，开发代理不会生效。
- 若希望通过代理避免 CORS，可在本地不设置 `VITE_API_PREFIX`，使用默认 `'/api/v1'` 路径让代理接管。

### 后端服务

确保后端服务运行在 `http://localhost:8080`，或者修改代理配置指向正确的后端地址。

## 使用说明

1. **选择模型**: 在对话页面选择要使用的 AI 模型
2. **选择 RAG 标签**: 可选择使用 RAG 增强功能，基于特定知识库进行回答
3. **开始对话**: 输入问题并发送，AI 将实时生成回复
4. **上传文档**: 在上传页面选择 RAG 标签并上传文档到知识库
5. **管理对话**: 可以清空对话历史或停止正在进行的生成

## 浏览器支持

- Chrome >= 87
- Firefox >= 78
- Safari >= 14
- Edge >= 88

## 开发说明

### 添加新功能

1. 在 `src/components/` 目录下创建新组件
2. 在 `App.jsx` 中引入并使用
3. 添加相应的样式文件

### 样式规范

- 使用 CSS 模块化，每个组件有独立的样式文件
- 遵循 BEM 命名规范
- 使用 CSS 变量定义主题色彩
- 支持响应式设计

## 故障排除

### 常见问题

1. **API 请求失败**: 检查后端服务是否正常运行
2. **流式响应中断**: 检查网络连接和服务器状态
3. **文件上传失败**: 检查文件格式和大小限制
4. **样式显示异常**: 清除浏览器缓存并重新加载

### 调试模式

在浏览器开发者工具中可以看到详细的网络请求和错误信息。

## 许可证

MIT License