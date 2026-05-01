# 阶段 1: 构建应用
FROM maven:3.8.6-jdk-8 AS builder
WORKDIR /build

# 只复制 pom.xml，利用 Docker 缓存层
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制整个项目并构建
COPY src ./src
RUN mvn clean package -DskipTests -q

# 阶段 2: 运行时镜像
FROM openjdk:8-jre-alpine
LABEL maintainer="markdown-editor"
LABEL description="Markdown Editor Backend"

WORKDIR /app

# 复制构建好的 jar 包
COPY --from=builder /build/target/editor-backend-1.0.0.jar app.jar

# 创建上传文件目录 
RUN mkdir -p /app/uploads

# 设置时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' > /etc/timezone

# 暴露端口
EXPOSE 8081

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# 运行应用
ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]
