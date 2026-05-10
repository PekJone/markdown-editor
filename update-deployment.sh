#!/bin/bash

# Markdown Editor 更新部署脚本
# 用于在服务器上更新应用最新代码

echo "========================================"
echo "   Markdown Editor 更新部署脚本"
echo "========================================"
echo ""

# 检查是否在正确的目录
if [ ! -f "docker-compose.git.yml" ]; then
    echo "❌ 错误：找不到 docker-compose.git.yml"
    echo "请确保在项目根目录下运行此脚本"
    exit 1
fi

# 检查 Docker Compose 版本
if docker compose version &> /dev/null; then
    DOCKER_COMPOSE="docker compose"
elif docker-compose version &> /dev/null; then
    DOCKER_COMPOSE="docker-compose"
else
    echo "❌ 错误：Docker Compose 未安装"
    exit 1
fi

# 设置 BUILD_TIMESTAMP 为当前时间戳，强制 Docker 重新构建
export BUILD_TIMESTAMP=$(date +%s)

echo "✅ BUILD_TIMESTAMP=${BUILD_TIMESTAMP}"
echo ""

echo "正在停止现有服务..."
$DOCKER_COMPOSE -f docker-compose.git.yml down

echo ""
echo "正在清理旧的 Docker 镜像..."
docker image prune -f

echo ""
echo "正在构建并启动服务（拉取最新代码）..."
$DOCKER_COMPOSE -f docker-compose.git.yml up -d --build

echo ""
echo "========================================"
if [ $? -eq 0 ]; then
    echo "   ✅ 部署更新成功！"
    echo ""
    echo "   正在等待服务启动..."
    sleep 5
    echo ""
    echo "   查看服务状态："
    $DOCKER_COMPOSE -f docker-compose.git.yml ps
    echo ""
    echo "   访问地址："
    echo "   前端应用：http://localhost:8080"
    echo "   后端 API：http://localhost:8081"
    echo "   Swagger：http://localhost:8081/swagger-ui/index.html"
    echo ""
    echo "   查看日志："
    echo "   $DOCKER_COMPOSE -f docker-compose.git.yml logs -f"
else
    echo "   ❌ 部署更新失败"
    echo ""
    echo "   查看错误日志："
    echo "   $DOCKER_COMPOSE -f docker-compose.git.yml logs"
fi
echo "========================================"
