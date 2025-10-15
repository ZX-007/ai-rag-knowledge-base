# MCP Server CSDN 配置说明



## 一、传输模式配置

本项目支持两种 MCP 传输模式：



### 1. STDIO 模式（标准输入输出）

**特点：**
- ✅ 不需要启动 Web 容器
- ✅ 通过标准输入输出进行通信
- ✅ 适用于命令行工具、IDE 集成
- ⚠️ 控制台日志被禁用（日志输出到文件）

**启动方式：**
```bash
java -jar mcp-server-csdn.jar --spring.profiles.active=stdio
```

**客户端配置示例：**
```json
{
  "mcpServers": {
    "csdn": {
      "command": "java",
      "args": [
        "-jar",
        "mcp-server-csdn.jar",
        "--spring.profiles.active=stdio"
      ]
    }
  }
}
```



### 2. SSE 模式（Server-Sent Events）

**特点：**
- ✅ 启动 Web 服务器（端口 8081）
- ✅ 通过 HTTP 连接进行通信
- ✅ 支持远程访问
- ✅ 可以正常输出控制台日志

**启动方式：**
```bash
java -jar mcp-server-csdn.jar --spring.profiles.active=sse
```

**客户端配置示例：**
```json
{
  "mcpServers": {
    "csdn": {
      "url": "http://localhost:8081/sse"
    }
  }
}
```



## 二、环境配置

支持多环境配置，可与传输模式组合使用：

### 本地环境（local）

```bash
# STDIO + 本地环境
java -jar mcp-server-csdn.jar --spring.profiles.active=local,stdio

# SSE + 本地环境
java -jar mcp-server-csdn.jar --spring.profiles.active=local,sse
```

### 开发环境（dev）

```bash
# STDIO + 开发环境
java -jar mcp-server-csdn.jar --spring.profiles.active=dev,stdio

# SSE + 开发环境
java -jar mcp-server-csdn.jar --spring.profiles.active=dev,sse
```

### 生产环境（prod）

```bash
# STDIO + 生产环境
java -jar mcp-server-csdn.jar --spring.profiles.active=prod,stdio

# SSE + 生产环境
java -jar mcp-server-csdn.jar --spring.profiles.active=prod,sse
```



## 三、配置文件说明

| 配置文件 | 用途 | 配置内容 |
|---------|------|---------|
| `application.yml` | 通用配置 | 应用名称、基础配置 |
| `application-stdio.yml` | STDIO 模式 | 传输方式、日志配置 |
| `application-sse.yml` | SSE 模式 | Web 服务器、传输方式 |
| `application-local.yml` | 本地环境 | CSDN Cookie、分类配置 |
| `application-dev.yml` | 开发环境 | 开发专用配置 |
| `application-prod.yml` | 生产环境 | 生产专用配置 |



## 四、日志配置

### STDIO 模式日志
- 控制台输出：**禁用**（保证 stdout 用于 JSON-RPC 通信）
- 文件输出：`data/log/mcp/mcp-server-csdn.log`

### SSE 模式日志
- 控制台输出：**启用**（可以查看实时日志）
- 文件输出：`data/log/mcp/mcp-server-csdn.log`



## 五、快速测试

### 测试 STDIO 模式
```bash
echo '{"jsonrpc":"2.0","id":1,"method":"tools/list"}' | java -jar mcp-server-csdn.jar --spring.profiles.active=stdio
```

### 测试 SSE 模式
```bash
# 启动服务
java -jar mcp-server-csdn.jar --spring.profiles.active=sse

# 访问 SSE 端点
curl http://localhost:8081/sse
```



## 六、常见问题

### Q: 如何选择传输模式？

**A:** 
- **STDIO 模式**：适用于本地开发、IDE 集成（如 Cursor、VS Code）
- **SSE 模式**：适用于远程访问、Web 应用集成、需要查看日志的场景

### Q: 可以同时启用两种模式吗？

**A:** 
不建议同时启用。选择一种模式启动服务即可。如果需要切换，重启应用并指定不同的 profile。

### Q: Profile 组合的优先级？

**A:** 
后面的 profile 会覆盖前面的配置。例如：
```bash
--spring.profiles.active=local,stdio
# local 的配置会被 stdio 覆盖（如果有冲突）
```



## 更多配置

需要配置 CSDN Cookie 和文章分类，请修改对应环境的配置文件：
```yaml
csdn:
  api:
    categories: Java场景面试宝典
    cookie: your-csdn-cookie-here
```

