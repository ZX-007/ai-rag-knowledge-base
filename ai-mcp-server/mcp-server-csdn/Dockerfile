# ============================================
# Dockerfile for MCP Server CSDN
# 从本地拷贝 JAR 包
# ============================================

# ==============================
# 运行阶段（生产镜像）
# ==============================
FROM eclipse-temurin:17-jre-jammy

# 设置维护者信息
LABEL maintainer="lcx"
LABEL version="0.0.1"
LABEL description="MCP Server CSDN - AI-powered CSDN article publishing service"

# 安装必要工具（健康检查需要）
RUN apt-get update && \
    apt-get install -y --no-install-recommends wget curl && \
    rm -rf /var/lib/apt/lists/*

# 创建非特权用户运行应用（安全最佳实践）
# Debian/Ubuntu 使用 groupadd 和 useradd 命令
RUN groupadd -g 1000 mcpserver && \
    useradd -r -u 1000 -g mcpserver -s /bin/bash -d /app mcpserver

# 设置工作目录
WORKDIR /app

# 创建数据和日志目录
RUN mkdir -p /app/data/log/mcp

# 从本地拷贝 JAR 文件
# 需要先在本地执行 mvn clean package 构建 JAR
COPY target/mcp-server-csdn.jar /app/app.jar

# 设置目录权限
RUN chown -R mcpserver:mcpserver /app

# 切换到非特权用户
USER mcpserver

# 设置环境变量
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport" \
    SERVER_PORT=8081 \
    SPRING_PROFILES_ACTIVE=prod,sse \
    TZ=Asia/Shanghai \
    LANG=C.UTF-8 \
    LC_ALL=C.UTF-8

# 暴露端口（SSE 模式使用）
EXPOSE ${SERVER_PORT}

# 健康检查（SSE 模式适用）
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${SERVER_PORT}/actuator/health || exit 1

# 启动应用
# 使用 exec 形式确保应用接收信号（如 SIGTERM）
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -jar /app/app.jar"]

