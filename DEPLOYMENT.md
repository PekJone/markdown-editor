# Markdown Editor 部署说明文档

## 目录
- [环境要求](#环境要求)
- [快速部署](#快速部署)
- [配置说明](#配置说明)
- [常用命令](#常用命令)
- [访问地址](#访问地址)
- [故障排查](#故障排查)
- [备份与恢复](#备份与恢复)

---

## 环境要求

在部署前，请确保服务器满足以下要求：

- **操作系统**: Linux (推荐 Ubuntu 20.04+ / CentOS 7+)
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **内存**: 至少 2GB RAM
- **磁盘**: 至少 10GB 可用空间
- **网络**: 能访问外部数据库 (39.107.242.71:3346)

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

## 快速部署

### 1. 上传项目文件到服务器

将整个 `markdown-editor` 目录上传到服务器的部署目录（例如 `/opt/markdown-editor`）。

```bash
# 假设你在本地开发机器上
scp -r markdown-editor/ user@your-server:/opt/
```

### 2. 配置环境变量

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

### 3. 启动服务

在项目根目录下执行：

```bash
cd /opt/markdown-editor

# 构建并启动服务
docker compose up -d --build

# 或者直接启动（如果已经构建过）
docker compose up -d
```

### 4. 查看服务状态

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
