# 服务器部署指南

## 问题分析

新提交的代码在服务器上没有生效，原因是 Docker 构建缓存导致没有重新拉取 Git 最新代码。

## 已修复

我们已经修复了这个问题，通过以下方式：

1. **修改了 Dockerfile.git**：添加了 BUILD_TIMESTAMP 参数，强制 Docker 每次重新执行 Git 克隆阶段
2. **修改了 docker-compose.git.yml**：传入 BUILD_TIMESTAMP 参数
3. **添加了更新脚本**：update-deployment.sh (Linux) 和 update-deployment.bat (Windows)

## 服务器更新步骤

### 方式一：使用更新脚本（推荐）

#### Linux 服务器：

```bash
# 1. 进入部署目录
cd /opt/markdown-editor

# 2. 拉取最新的部署脚本
git pull origin main

# 3. 执行更新脚本
chmod +x update-deployment.sh
./update-deployment.sh
```

#### Windows 服务器：

```cmd
# 1. 进入部署目录
cd C:\markdown-editor

# 2. 拉取最新的部署脚本
git pull origin main

# 3. 执行更新脚本
update-deployment.bat
```

### 方式二：手动更新

如果脚本执行有问题，也可以手动执行：

#### Linux：

```bash
cd /opt/markdown-editor

# 拉取最新代码
git pull origin main

# 停止现有服务
docker compose -f docker-compose.git.yml down

# 清理旧镜像
docker image prune -f

# 设置时间戳并重新构建
export BUILD_TIMESTAMP=$(date +%s)
docker compose -f docker-compose.git.yml up -d --build

# 查看服务状态
docker compose -f docker-compose.git.yml ps

# 查看日志
docker compose -f docker-compose.git.yml logs -f
```

#### Windows：

```cmd
cd C:\markdown-editor

# 拉取最新代码
git pull origin main

# 停止现有服务
docker compose -f docker-compose.git.yml down

# 清理旧镜像
docker image prune -f

# 设置时间戳并重新构建
set BUILD_TIMESTAMP=%date:~0,4%%date:~5,2%%date:~8,2%%time:~0,2%%time:~3,2%%time:~6,2%
docker compose -f docker-compose.git.yml up -d --build

# 查看服务状态
docker compose -f docker-compose.git.yml ps

# 查看日志
docker compose -f docker-compose.git.yml logs -f
```

## 验证更新

更新完成后，验证以下内容：

1. **前端**：访问 http://服务器IP:8080 验证消息中心功能
2. **后端**：访问 http://服务器IP:8081/swagger-ui/index.html
3. **查看日志**：确认没有错误信息

## 后续更新

每次有新代码提交后，只需要在服务器上执行更新脚本即可：

```bash
# Linux
./update-deployment.sh

# Windows
update-deployment.bat
```

脚本会自动：
1. 拉取最新代码
2. 停止旧服务
3. 清理旧镜像
4. 重新构建（强制拉取最新 Git 代码）
5. 启动新服务
6. 显示服务状态
