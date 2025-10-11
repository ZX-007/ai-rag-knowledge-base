# 后端健康检查端点指南

## 📋 已配置的健康检查端点

### 1. 基础健康检查
```
GET http://localhost:8080/actuator/health
```

**响应示例**：
```json
{
  "status": "UP",
  "components": {
    "custom": {
      "status": "UP",
      "details": {
        "status": "AI Knowledge Base is running",
        "service": "ai-rag-knowledge-base",
        "version": "1.0.0"
      }
    },
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "6.2.x"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500GB,
        "free": 100GB,
        "threshold": 10MB
      }
    }
  }
}
```

### 2. Liveness 探针（存活探针）
```
GET http://localhost:8080/actuator/health/liveness
```

**用途**：检查应用是否存活（是否需要重启）

**响应示例**：
```json
{
  "status": "UP"
}
```

### 3. Readiness 探针（就绪探针）
```
GET http://localhost:8080/actuator/health/readiness
```

**用途**：检查应用是否准备好接收流量

**响应示例**：
```json
{
  "status": "UP"
}
```

### 4. 应用信息
```
GET http://localhost:8080/actuator/info
```

### 5. 指标信息
```
GET http://localhost:8080/actuator/metrics
```

---

## 🔧 配置说明

### 开发环境配置

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics  # 暴露的端点
      base-path: /actuator            # 基础路径
  endpoint:
    health:
      show-details: when-authorized   # 显示详细信息（需要授权）
      probes:
        enabled: true                 # 启用探针
  health:
    redis:
      enabled: true                   # Redis 健康检查
    db:
      enabled: true                   # 数据库健康检查
```

### 生产环境配置

```yaml
# application-pro.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: never  # 生产环境不暴露详细信息（安全考虑）
      probes:
        enabled: true
```

---

## 🚀 使用方法

### 本地测试

```bash
# 启动应用
cd ai-knowledge-app
mvn spring-boot:run

# 测试健康检查
curl http://localhost:8080/actuator/health

# 测试 liveness
curl http://localhost:8080/actuator/health/liveness

# 测试 readiness
curl http://localhost:8080/actuator/health/readiness
```

### Docker 容器测试

```bash
# 启动容器
docker-compose up -d

# 进入容器测试
docker exec -it ai-knowledge-base-app bash
curl http://localhost:8080/actuator/health

# 或从宿主机测试
curl http://localhost:8080/actuator/health
```

### 生产环境测试

```bash
# 通过公网访问
curl http://your-server-ip:8080/actuator/health

# 或在服务器上
ssh root@your-server
curl http://localhost:8080/actuator/health
```

---

## 📊 健康状态说明

### 状态码

| 状态 | HTTP 状态码 | 说明 |
|------|------------|------|
| UP | 200 | 健康 ✅ |
| DOWN | 503 | 不健康 ❌ |
| OUT_OF_SERVICE | 503 | 维护中 |
| UNKNOWN | 200 | 未知 |

### 组件健康检查

**自动检查的组件**：
- ✅ **db**：PostgreSQL 数据库连接
- ✅ **redis**：Redis 连接
- ✅ **diskSpace**：磁盘空间
- ✅ **custom**：自定义健康检查

---

## 🐳 Docker Compose 集成

### 添加健康检查到 docker-compose.yml

```yaml
services:
  ai-knowledge-base-app:
    image: white0618/ai-knowledge-base:v1.1.0
    # ... 其他配置
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s       # 每30秒检查一次
      timeout: 10s        # 超时时间
      retries: 3          # 重试次数
      start_period: 40s   # 启动宽限期
```

**效果**：
```bash
docker ps
# 会显示健康状态：
# STATUS: Up 5 minutes (healthy)
```

---

## 🔍 监控和告警

### Prometheus 集成（可选）

如果需要监控指标：

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

然后访问：
```
GET http://localhost:8080/actuator/prometheus
```

### 自定义告警脚本

```bash
#!/bin/bash
# health-check.sh

HEALTH_URL="http://localhost:8080/actuator/health"

while true; do
  STATUS=$(curl -s $HEALTH_URL | jq -r '.status')
  
  if [ "$STATUS" != "UP" ]; then
    echo "⚠️ 服务不健康！状态: $STATUS"
    # 发送告警通知
    # curl -X POST ... 钉钉/邮件/Slack
  fi
  
  sleep 60  # 每分钟检查一次
done
```

---

## 🎯 GitHub Actions 中的健康检查

### 当前配置

在 `.github/workflows/deploy-backend.yml` 中已配置：

```yaml
# 12. 健康检查
if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
  echo "✅ 健康检查接口响应正常"
else
  echo "⚠️ 健康检查接口未响应（可能还在启动中）"
fi
```

### 增强版健康检查（可选）

```bash
# 等待服务完全就绪
MAX_RETRIES=30
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
  if curl -f http://localhost:8080/actuator/health/readiness > /dev/null 2>&1; then
    echo "✅ 服务已就绪"
    break
  fi
  
  RETRY_COUNT=$((RETRY_COUNT + 1))
  echo "等待服务就绪... ($RETRY_COUNT/$MAX_RETRIES)"
  sleep 2
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
  echo "❌ 服务启动超时"
  exit 1
fi
```

---

## 📚 可用的端点

| 端点 | 路径 | 用途 |
|------|------|------|
| **健康检查** | `/actuator/health` | 总体健康状态 |
| **Liveness** | `/actuator/health/liveness` | 存活探针（K8s 用）|
| **Readiness** | `/actuator/health/readiness` | 就绪探针（K8s 用）|
| **信息** | `/actuator/info` | 应用信息 |
| **指标** | `/actuator/metrics` | 性能指标 |
| **指标详情** | `/actuator/metrics/{name}` | 特定指标 |

---

## 🔒 安全建议

### 1. 限制访问

```yaml
# application-pro.yml
management:
  endpoints:
    web:
      exposure:
        include: health  # 只暴露健康检查
```

### 2. 添加认证（可选）

```yaml
spring:
  security:
    user:
      name: admin
      password: ${ACTUATOR_PASSWORD}

management:
  endpoints:
    web:
      exposure:
        include: '*'
  endpoint:
    health:
      show-details: when-authorized
```

### 3. 配置防火墙

只允许特定 IP 访问 actuator 端点。

---

## ✅ 测试清单

部署后测试以下端点：

- [ ] `curl http://localhost:8080/actuator/health` 返回 200
- [ ] 响应包含 `"status": "UP"`
- [ ] 包含 `db` 组件状态
- [ ] 包含 `redis` 组件状态
- [ ] 包含 `custom` 组件状态
- [ ] Liveness 探针可用
- [ ] Readiness 探针可用

---

## 🎉 完成

现在你的后端应用已经有完整的健康检查功能：

- ✅ Spring Boot Actuator 依赖已添加
- ✅ 健康检查端点已配置
- ✅ 数据库健康检查已启用
- ✅ Redis 健康检查已启用
- ✅ 自定义健康检查已添加
- ✅ 生产环境安全配置
- ✅ GitHub Actions 部署流程已集成

部署后即可通过 `/actuator/health` 端点监控服务状态！🚀

