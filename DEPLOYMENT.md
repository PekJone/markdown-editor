# Markdown Editor 部署说明文档

## 目录
- [环境要求](#环境要求)
- [部署方式](#部署方式)
  - [方式一：本地文件部署](#方式一本地文件部署)
  - [方式二：Git克隆部署](#方式二git克隆部署)
- [配置说明](#配置说明)
- [常用命令](#常用命令)
- [访问地址](#访问地址)
- [故障排查](#故障排查)
- [备份与恢复](#备份与恢复)

---

## 部署方式

本项目支持两种部署方式，请根据实际情况选择：

### 方式一：本地文件部署

通过将本地构建好的项目文件上传到服务器进行部署。适用于没有Git环境或需要部署特定版本的情况。

**特点：**
- 部署速度快（无需克隆整个仓库）
- 可以部署任意版本的代码
- 需要手动上传文件

**操作步骤：**

#### 1. 上传项目文件到服务器

将整个 `markdown-editor` 目录上传到服务器的部署目录（例如 `/opt/markdown-editor`）。

```bash
# 假设你在本地开发机器上
scp -r markdown-editor/ user@your-server:/opt/
```

#### 2. 配置环境变量

在服务器上编辑 `.env` 文件，根据实际需求修改配置：

```bash
cd /opt/markdown-editor
nano .env
```

主要配置项说明：
```
# 数据库配置
DB_HOST=39.107.242.71        # 数据库主机地址
DB_PORT=3346                 # 数据库端口
DB_NAME=markdown_editor      # 数据库名称
DB_USERNAME=root             # 数据库用户名
DB_PASSWORD=123456           # 数据库密码

# JWT 配置（生产环境建议修改）
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400000

# 服务端口配置
BACKEND_PORT=8081            # 后端端口
FRONTEND_PORT=80             # 前端端口
```

#### 3. 启动服务

```bash
cd /opt/markdown-editor

# 构建并启动服务
docker compose up -d --build

# 或者直接启动（如果已经构建过）
docker compose up -d
```

#### 4. 查看服务状态

```bash
# 查看所有服务状态
docker compose ps

# 查看服务日志
docker compose logs -f

# 查看特定服务日志
docker compose logs -f backend
docker compose logs -f frontend
```

---

### 方式二：Git克隆部署

通过Dockerfile直接从Git仓库克隆代码并在容器内构建。适用于持续部署和更新场景。

**特点：**
- 部署简单，只需一个 `docker-compose.git.yml` 文件
- 支持一键更新：重新构建即可获取最新代码
- 适合CI/CD自动化部署
- 需要服务器能访问Git仓库

**操作步骤：**

#### 1. 服务器环境准备

确保服务器已安装 Docker 和 Docker Compose：

```bash
# 检查 Docker 版本
docker --version

# 检查 Docker Compose 版本
docker compose version
```

如果未安装，请参考本文档 [环境要求](#环境要求) 部分进行安装。

#### 2. 创建部署目录

```bash
# 创建部署目录
sudo mkdir -p /opt/markdown-editor
cd /opt/markdown-editor
```

#### 3. 创建 docker-compose.git.yml 配置文件

将 `docker-compose.git.yml` 文件上传到服务器，或者直接创建：

```bash
nano docker-compose.git.yml
```

**完整配置内容：**

```yaml
version: '3.8'

services:
  # 后端服务 - 从Git克隆部署
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile.git
    container_name: markdown-backend-git
    restart: unless-stopped
    ports:
      - "${BACKEND_PORT:-8081}:8081"
    environment:
      # Git 配置
      GIT_REPO_URL: ${GIT_REPO_URL:-https://github.com/PekJone/markdown-editor.git}
      GIT_BRANCH: ${GIT_BRANCH:-main}
      # 数据库配置（使用外部数据库）
      SPRING_DATASOURCE_URL: jdbc:mysql://${DB_HOST:-39.107.242.71}:${DB_PORT:-3346}/${DB_NAME:-markdown_editor}?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: ${DB_USERNAME:-root}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD:-123456}
      # JWT 配置
      JWT_SECRET: ${JWT_SECRET:-markdown-editor-secret-key-2024}
      JWT_EXPIRATION: ${JWT_EXPIRATION:-86400000}
      # 文件上传目录
      FILE_UPLOAD_DIR: ${FILE_UPLOAD_DIR:-/app/uploads}
      # 时区配置
      TZ: ${TZ:-Asia/Shanghai}
    volumes:
      # 上传文件持久化
      - uploads-git:/app/uploads
    networks:
      - markdown-network-git
    healthcheck:
      test: ["CMD", "wget", "-q", "--spider", "http://localhost:8081/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  # 前端服务 - 从Git克隆部署
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile.git
    container_name: markdown-frontend-git
    restart: unless-stopped
    ports:
      - "${FRONTEND_PORT:-8080}:80"
    depends_on:
      - backend
    networks:
      - markdown-network-git
    environment:
      # Git 配置
      GIT_REPO_URL: ${GIT_REPO_URL:-https://github.com/PekJone/markdown-editor.git}
      GIT_BRANCH: ${GIT_BRANCH:-main}
      # API代理配置
      API_BASE_URL: http://backend:8081
      TZ=${TZ:-Asia/Shanghai}
    healthcheck:
      test: ["CMD", "wget", "-q", "--spider", "http://localhost/"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

# 持久化数据卷
volumes:
  uploads-git:
    driver: local

# 网络配置
networks:
  markdown-network-git:
    driver: bridge
```

#### 4. 创建后端 Dockerfile.git

将 `backend/Dockerfile.git` 文件上传到服务器的 `/opt/markdown-editor/backend/` 目录：

```bash
mkdir -p /opt/markdown-editor/backend
nano /opt/markdown-editor/backend/Dockerfile.git
```

**完整配置内容：**

```dockerfile
# 阶段 1: 克隆代码
FROM alpine/git:latest AS git-clone
LABEL maintainer="markdown-editor"
LABEL description="Markdown Editor Backend - Git Clone Stage"

ARG GIT_REPO_URL=https://github.com/PekJone/markdown-editor.git
ARG GIT_BRANCH=main

WORKDIR /app
RUN git clone --depth 1 --branch ${GIT_BRANCH} ${GIT_REPO_URL} /app/backend

# 阶段 2: 构建应用
FROM maven:3.8.6-jdk-8 AS builder
WORKDIR /build

COPY --from=git-clone /app/backend/pom.xml .
RUN mvn dependency:go-offline -B

COPY --from=git-clone /app/backend/src ./src
RUN mvn clean package -DskipTests -q

# 阶段 3: 运行时镜像
FROM openjdk:8-jre-alpine
LABEL maintainer="markdown-editor"
LABEL description="Markdown Editor Backend"

WORKDIR /app

COPY --from=builder /build/target/editor-backend-1.0.0.jar app.jar

RUN mkdir -p /app/uploads

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' > /etc/timezone

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]
```

#### 5. 创建前端 Dockerfile.git

将 `frontend/Dockerfile.git` 文件上传到服务器的 `/opt/markdown-editor/frontend/` 目录：

```bash
mkdir -p /opt/markdown-editor/frontend
nano /opt/markdown-editor/frontend/Dockerfile.git
```

**完整配置内容：**

```dockerfile
# 阶段 1: 克隆代码
FROM alpine/git:latest AS git-clone
LABEL maintainer="markdown-editor"
LABEL description="Markdown Editor Frontend - Git Clone Stage"

ARG GIT_REPO_URL=https://github.com/PekJone/markdown-editor.git
ARG GIT_BRANCH=main

WORKDIR /app
RUN git clone --depth 1 --branch ${GIT_BRANCH} ${GIT_REPO_URL} /app/frontend

# 阶段 2: 构建应用
FROM node:18-alpine AS builder
LABEL maintainer="markdown-editor"
LABEL description="Markdown Editor Frontend Build Stage"

WORKDIR /app

COPY --from=git-clone /app/frontend/package*.json ./

RUN npm config set registry https://registry.npmmirror.com

RUN npm ci --only=production --registry=https://registry.npmmirror.com

COPY --from=git-clone /app/frontend . .

RUN npm run build

# 阶段 3: 运行时镜像
FROM nginx:alpine
LABEL maintainer="markdown-editor"
LABEL description="Markdown Editor Frontend"

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' > /etc/timezone

COPY --from=builder /app/dist /usr/share/nginx/html

COPY --from=git-clone /app/frontend/nginx.conf /etc/nginx/nginx.conf
COPY --from=git-clone /app/frontend/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
```

#### 6. 创建前端 nginx.conf（必需）

前端部署需要nginx配置文件，请创建 `/opt/markdown-editor/frontend/nginx.conf`：

```bash
nano /opt/markdown-editor/frontend/nginx.conf
```

**完整配置内容：**

```nginx
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;

    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;

    # Gzip 压缩
    gzip on;
    gzip_vary on;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types text/plain text/css text/xml text/javascript
               application/json application/javascript application/xml+rss
               application/rss+xml font/truetype font/opentype
               application/vnd.ms-fontobject image/svg+xml;

    server {
        listen 80;
        server_name localhost;
        root /usr/share/nginx/html;
        index index.html;

        # 静态资源缓存
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            expires 30d;
            add_header Cache-Control "public, immutable";
        }

        # API 代理
        location /api/ {
            proxy_pass http://backend:8081;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_connect_timeout 60s;
            proxy_send_timeout 60s;
            proxy_read_timeout 60s;
        }

        # 前端路由
        location / {
            try_files $uri $uri/ /index.html;
        }

        # 健康检查
        location /health {
            return 200 'OK';
            add_header Content-Type text/plain;
        }
    }
}
```

#### 7. 创建环境变量配置文件（可选）

创建 `.env` 文件自定义配置：

```bash
nano /opt/markdown-editor/.env
```

**配置内容：**

```env
# Git 仓库配置
GIT_REPO_URL=https://github.com/PekJone/markdown-editor.git
GIT_BRANCH=main

# 数据库配置
DB_HOST=39.107.242.71
DB_PORT=3346
DB_NAME=markdown_editor
DB_USERNAME=root
DB_PASSWORD=123456

# JWT 配置
JWT_SECRET=markdown-editor-secret-key-2024
JWT_EXPIRATION=86400000

# 服务端口配置
BACKEND_PORT=8081
FRONTEND_PORT=8080

# 时区配置
TZ=Asia/Shanghai
```

#### 8. 启动服务

```bash
cd /opt/markdown-editor

# 构建并启动服务（首次部署）
docker compose -f docker-compose.git.yml up -d --build
```

#### 9. 查看服务状态

```bash
# 查看所有服务状态
docker compose -f docker-compose.git.yml ps

# 查看服务日志
docker compose -f docker-compose.git.yml logs -f

# 查看特定服务日志
docker compose -f docker-compose.git.yml logs -f backend
docker compose -f docker-compose.git.yml logs -f frontend
```

#### 10. 更新部署

当Git仓库有更新时，只需重新构建即可：

```bash
cd /opt/markdown-editor

# 拉取最新代码并重新构建
docker compose -f docker-compose.git.yml up -d --build

# 或者只重建某个服务
docker compose -f docker-compose.git.yml up -d --build backend
docker compose -f docker-compose.git.yml up -d --build frontend
```

#### 11. 一键部署脚本

创建便捷脚本 `/opt/markdown-editor/start-git.sh`：

```bash
#!/bin/bash
echo "===== Markdown Editor Git 部署 ====="
echo "开始部署..."
docker compose -f docker-compose.git.yml up -d --build
echo "部署完成！"
echo "前端地址: http://你的服务器IP:8080"
echo "后端地址: http://你的服务器IP:8081"
echo "Swagger文档: http://你的服务器IP:8081/swagger-ui/index.html"
```

添加执行权限并使用：

```bash
chmod +x /opt/markdown-editor/start-git.sh
./start-git.sh
```

创建停止脚本 `/opt/markdown-editor/stop-git.sh`：

```bash
#!/bin/bash
echo "===== 停止 Markdown Editor ====="
docker compose -f docker-compose.git.yml down
echo "服务已停止"
```

添加执行权限并使用：

```bash
chmod +x /opt/markdown-editor/stop-git.sh
./stop-git.sh
```

---

## 环境要求

在部署前，请确保服务器满足以下要求：

- **操作系统**: Linux (推荐 Ubuntu 20.04+ / CentOS 7+)
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **内存**: 至少 2GB RAM
- **磁盘**: 至少 10GB 可用空间
- **网络**: 能访问外部数据库 (39.107.242.71:3346) 和 GitHub

### 安装 Docker 和 Docker Compose

如果服务器还没有安装 Docker 和 Docker Compose，请按照以下步骤安装：

#### Ubuntu/Debian 系统:
```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装依赖
sudo apt install -y apt-transport-https ca-certificates curl software-properties-common

# 添加 Docker 官方 GPG 密钥
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# 添加 Docker 软件源
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 安装 Docker
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# 启动 Docker 并设置开机自启
sudo systemctl start docker
sudo systemctl enable docker
```

#### CentOS/RHEL 系统:
```bash
# 安装 Docker
sudo yum install -y yum-utils
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo yum install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# 启动 Docker 并设置开机自启
sudo systemctl start docker
sudo systemctl enable docker
```

#### 验证安装:
```bash
# 检查 Docker 版本
docker --version

# 检查 Docker Compose 版本
docker compose version
```

---

## 配置说明

### 数据库配置

当前配置使用外部 MySQL 数据库，地址为：`39.107.242.71:3346`

**注意**：
- 确保服务器能访问这个数据库地址
- 确保数据库用户有足够的权限
- 如果需要更换数据库，请修改 `.env` 文件中的配置

### 端口配置

| 服务 | 宿主机端口 | 容器端口 | 说明 |
|------|----------|---------|------|
| 前端 | 80 | 80 | 用户访问入口 |
| 后端 | 8081 | 8081 | API 服务 |

如果端口被占用，可以在 `.env` 文件中修改端口。

### 持久化配置

- **上传文件**: 存储在 Docker volume `markdown-editor_uploads` 中
- 数据会自动持久化，容器重启不会丢失

---

## 常用命令

### 服务管理

```bash
# 启动所有服务
docker compose up -d

# 停止所有服务
docker compose stop

# 重启所有服务
docker compose restart

# 停止并删除所有容器
docker compose down

# 停止并删除所有容器、网络、卷（慎用）
docker compose down -v
```

### 日志查看

```bash
# 查看所有服务日志
docker compose logs -f

# 查看后端日志
docker compose logs -f backend

# 查看前端日志
docker compose logs -f frontend

# 查看最近100行日志
docker compose logs --tail=100
```

### 更新部署

```bash
# 1. 拉取最新代码
git pull

# 2. 重新构建并启动
docker compose up -d --build

# 3. 清理旧镜像（可选）
docker image prune -a
```

### 进入容器

```bash
# 进入后端容器
docker compose exec backend sh

# 进入前端容器
docker compose exec frontend sh
```

---

## 访问地址

部署成功后，可以通过以下地址访问：

| 服务 | 访问地址 |
|------|---------|
| **前端应用** | `http://your-server-ip` |
| **后端 API** | `http://your-server-ip:8081` |
| **Swagger 文档** | `http://your-server-ip:8081/swagger-ui/index.html` |

**注意**：
- 如果需要通过域名访问，请配置 DNS 解析或反向代理
- 生产环境建议配置 HTTPS

---

## 故障排查

### 服务无法启动

1. 检查端口是否被占用：
```bash
# 检查端口占用
sudo netstat -tlnp | grep -E ':(80|8081)'
```

2. 检查 Docker 服务状态：
```bash
sudo systemctl status docker
```

3. 查看详细错误日志：
```bash
docker compose logs backend
docker compose logs frontend
```

### 数据库连接失败

1. 检查网络连接：
```bash
# 在容器内测试数据库连接
docker compose exec backend ping 39.107.242.71
```

2. 确认数据库配置正确：
```bash
# 检查环境变量
docker compose exec backend env | grep SPRING_DATASOURCE
```

3. 检查数据库用户权限

### 上传文件问题

1. 检查目录权限：
```bash
docker compose exec backend ls -la /app/uploads
```

2. 检查磁盘空间：
```bash
docker system df
```

---

## 备份与恢复

### 备份上传文件

```bash
# 创建备份目录
mkdir -p /opt/backups

# 备份上传文件
docker run --rm -v markdown-editor_uploads:/data -v /opt/backups:/backup alpine tar czf /backup/uploads-$(date +%Y%m%d).tar.gz -C /data .
```

### 恢复上传文件

```bash
# 从备份文件恢复
docker run --rm -v markdown-editor_uploads:/data -v /opt/backups:/backup alpine tar xzf /backup/uploads-20240101.tar.gz -C /data
```

### 查看备份

```bash
# 列出备份文件
ls -lh /opt/backups/
```

---

## 安全建议

1. **修改默认密码和密钥**
   - 修改数据库密码
   - 修改 JWT_SECRET 为强密钥

2. **配置防火墙**
```bash
# 只开放必要端口
sudo ufw allow 80/tcp
sudo ufw allow 22/tcp
sudo ufw enable
```

3. **配置 HTTPS**（生产环境必需）
   - 使用 Let's Encrypt
   - 或配置 Nginx SSL 证书

4. **定期备份**
   - 设置定时备份任务
   - 定期测试备份恢复

5. **监控服务状态**
   - 配置健康检查告警
   - 监控资源使用情况

---

## 性能优化

1. **资源限制**

可以在 `docker-compose.yml` 中添加资源限制：

```yaml
backend:
  deploy:
    resources:
      limits:
        memory: 1G
        cpus: '0.5'
```

2. **日志管理**

配置日志轮询，避免占用过多磁盘空间。

3. **缓存优化**

配置适当的缓存策略，提高响应速度。

---

## 技术支持

如有问题，请查看：
- 服务日志：`docker compose logs -f`
- 项目文档：README.md
- Swagger 文档：http://your-server-ip:8081/swagger-ui/index.html

---

## 更新日志

### v1.0.0 (2024-01-01)
- 初始版本
- 支持 Docker Compose 部署
- 使用外部 MySQL 数据库
