curl http://118.196.28.78:11434/api/generate \
  -H "Content-Type: application/json" \
  -d '{
        "model": "deepseek-r1:1.5b",
        "prompt": "1+1",
        "stream": false
      }'

# AI RAG Knowledge Base API - Curl Commands for Apifox
# 可以直接复制到 Apifox 中使用的 curl 命令

# =============================================================================
# Ollama AI 服务接口
# =============================================================================

# 1. 同步生成 AI 回复
curl -X POST \
  'http://localhost:8080/api/v1/ollama/generate' \
  -H 'Content-Type: application/json' \
  -d '{
    "model": "deepseek-r1:1.5b",
    "message": "你好，请介绍一下你自己"
  }'

# 2. 流式生成 AI 回复
curl -X POST \
  'http://localhost:8080/api/v1/ollama/generate_stream' \
  -H 'Content-Type: application/json' \
  -d '{
    "model": "deepseek-r1:1.5b",
    "message": "请写一篇关于人工智能的文章"
  }'

# =============================================================================
# RAG 知识库服务接口
# =============================================================================

# 3. 查询 RAG 标签列表
curl -X GET \
  'http://localhost:8080/api/v1/rag/query_rag_tag_list' \
  -H 'Accept: application/json'

# 4. 上传文件到知识库
curl -X POST \
  'http://localhost:8080/api/v1/rag/file/upload' \
  -H 'Content-Type: multipart/form-data' \
  -F 'ragTag=技术文档' \
  -F 'files=@README.md'

# =============================================================================
# 测试用例
# =============================================================================

# 测试用例 1: 正常 AI 对话
curl -X POST \
  'http://localhost:8080/api/v1/ollama/generate' \
  -H 'Content-Type: application/json' \
  -d '{
    "model": "deepseek-r1:1.5b",
    "message": "请帮我写一个Java的Hello World程序"
  }'

# 测试用例 2: 参数验证错误 - 模型名称为空
curl -X POST \
  'http://localhost:8080/api/v1/ollama/generate' \
  -H 'Content-Type: application/json' \
  -d '{
    "model": "",
    "message": "测试消息"
  }'

# 测试用例 3: 参数验证错误 - 消息内容为空
curl -X POST \
  'http://localhost:8080/api/v1/ollama/generate' \
  -H 'Content-Type: application/json' \
  -d '{
    "model": "deepseek-r1:1.5b",
    "message": ""
  }'

# 测试用例 4: 文件上传测试
curl -X POST \
  'http://localhost:8080/api/v1/rag/file/upload' \
  -H 'Content-Type: multipart/form-data' \
  -F 'ragTag=测试文档' \
  -F 'files=@README.md'

# 测试用例 5: 文件上传参数错误 - RAG标签为空
curl -X POST \
  'http://localhost:8080/api/v1/rag/file/upload' \
  -H 'Content-Type: multipart/form-data' \
  -F 'ragTag=' \
  -F 'files=@README.md'