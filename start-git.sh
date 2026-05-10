#!/bin/bash
echo "===== Markdown Editor Git 部署 ====="
echo "开始部署..."
docker compose -f docker-compose.git.yml up -d --build
echo "部署完成！"
echo "前端地址: http://你的服务器IP:8080"
echo "后端地址: http://你的服务器IP:8081"
echo "Swagger文档: http://你的服务器IP:8081/swagger-ui/index.html"
