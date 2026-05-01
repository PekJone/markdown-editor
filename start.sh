#!/bin/bash

# Markdown Editor 一键启动脚本

echo "========================================"
echo "   Markdown Editor 部署脚本"
echo "========================================"
echo ""

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    echo "❌ 错误：Docker 未安装"
    echo "请先安装 Docker 和 Docker Compose"
    echo "参考文档：DEPLOYMENT.md"
    exit 1
fi

# 检查 Docker Compose 是否安装
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "❌ 错误：Docker Compose 未安装"
    exit 1
fi

# 检查是否在正确的目录
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ 错误：找不到 docker-compose.yml"
    echo "请确保在项目根目录下运行此脚本"
    exit 1
fi

# 检查 .env 文件是否存在
if [ ! -f ".env" ]; then
    echo "⚠️  警告：.env 文件不存在"
    echo "正在创建默认配置文件..."
    cp .env.example .env 2>/dev/null || true
    echo "✅ 已创建默认配置文件"
    echo ""
    echo "请编辑 .env 文件配置数据库连接信息，然后重新运行此脚本"
    exit 0
fi

echo "✅ 环境检查通过"
echo ""
echo "正在构建并启动服务..."
echo ""

# 使用 Docker Compose 启动服务
if docker compose version &> /dev/null; then
    docker compose up -d --build
else
    docker-compose up -d --build
fi

echo ""
echo "========================================"
if [ $? -eq 0 ]; then
    echo "   ✅ 服务启动成功！"
    echo ""
    echo "   访问地址："
    echo "   前端应用：http://localhost"
    echo "   后端 API：http://localhost:8081"
    echo "   Swagger：http://localhost:8081/swagger-ui/index.html"
    echo ""
    echo "   常用命令："
    echo "   查看日志：docker compose logs -f"
    echo "   查看状态：docker compose ps"
    echo "   停止服务：docker compose stop"
else
    echo "   ❌ 服务启动失败"
    echo ""
    echo "   请查看日志：docker compose logs"
fi
echo "========================================"
