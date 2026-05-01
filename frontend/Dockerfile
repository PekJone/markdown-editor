# 阶段 1: 构建应用
FROM node:18-alpine AS builder
LABEL maintainer="markdown-editor"
LABEL description="Markdown Editor Frontend Build Stage"

WORKDIR /app

# 复制 package 文件，利用 Docker 缓存
COPY package*.json ./

# 设置 npm 镜像源，加速构建
RUN npm config set registry https://registry.npmmirror.com

# 安装依赖
RUN npm ci --only=production --registry=https://registry.npmmirror.com

# 复制源代码
COPY . .

# 构建应用
RUN npm run build

# 阶段 2: 运行时镜像
FROM nginx:alpine
LABEL maintainer="markdown-editor"
LABEL description="Markdown Editor Frontend"

# 设置时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' > /etc/timezone

# 复制构建好的文件
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/nginx.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost/ || exit 1

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
