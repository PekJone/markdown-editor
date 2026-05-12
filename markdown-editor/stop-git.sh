#!/bin/bash
echo "===== 停止 Markdown Editor ====="
docker compose -f docker-compose.git.yml down
echo "服务已停止"
