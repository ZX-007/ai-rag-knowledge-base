# ===== Build stage =====
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# 1. 复制所有 POM 文件，利用缓存层
# 复制根 POM 和所有子模块 POM 的目录结构
COPY pom.xml .
COPY ai-knowledge-api/pom.xml ai-knowledge-api/
COPY ai-knowledge-app/pom.xml ai-knowledge-app/
COPY ai-knowledge-trigger/pom.xml ai-knowledge-trigger/

# 2. 仅下载依赖 (如果 pom 文件未更改，这一层会被缓存)
RUN mvn dependency:go-offline -B -q

# 3. 复制所有源代码
# 将 src 目录复制到其对应的模块目录中，保持项目结构一致
COPY ai-knowledge-api/src ai-knowledge-api/src
COPY ai-knowledge-app/src ai-knowledge-app/src
COPY ai-knowledge-trigger/src ai-knowledge-trigger/src

# 4. 构建特定的应用模块及其依赖
# 使用 -DskipTests 跳过测试
RUN mvn -B -q package -DskipTests -pl ai-knowledge-app -am

# ===== Runtime stage =====
FROM eclipse-temurin:17-jre AS runtime

ENV TZ=Asia/Shanghai \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"

WORKDIR /app

# 复制最终生成的 Spring Boot 可执行 JAR
# 假设 finalName 为 ai-rag-knowledge-app，这里使用明确的名称
COPY --from=build /app/ai-knowledge-app/target/ai-knowledge-base.jar /app/app.jar

EXPOSE 8080

# 启动 Spring Boot 应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
