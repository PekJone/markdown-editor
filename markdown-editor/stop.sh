#!/bin/bash

# Markdown Editor 停止脚本

echo "========================================"
echo "   Markdown Editor 停止脚本"
echo "========================================"
echo ""

# 检查是否在正确的目录
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ 错误：找不到 docker-compose.yml"
    echo "请确保在项目根目录下运行此脚本"
    exit 1
fi

echo "正在停止服务..."
echo ""

# 停止服务
if docker compose version &> /dev/null; then
    docker compose down
else
    docker-compose down
fi

echo ""
echo "========================================"
if [ $? -eq 0 ]; then
    echo "   ✅ 服务已停止"
    echo ""
    echo "   提示：数据卷和上传文件已保留"
    echo "   如需完全删除，请运行：docker compose down -v"
else
    echo "   ⚠️  停止过程中有问题，请查看日志"
fi
echo "========================================"
