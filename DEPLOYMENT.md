# Markdown Editor Deployment Documentation

## Table of Contents

- [Environment Requirements](#environment-requirements)
- [Deployment Methods](#deployment-methods)
  - [Method 1: Local File Deployment](#method-1-local-file-deployment)
  - [Method 2: Git Clone Deployment](#method-2-git-clone-deployment)
- [Configuration Instructions](#configuration-instructions)
- [Common Commands](#common-commands)
- [Access Addresses](#access-addresses)
- [Troubleshooting](#troubleshooting)
- [Backup and Recovery](#backup-and-recovery)

***

## Deployment Methods

This project supports two deployment methods. Please choose according to your actual situation:

### Method 1: Local File Deployment

Deploy by uploading locally built project files to the server. Suitable for cases without Git environment or when deploying a specific version.

**Features:**

- Fast deployment (no need to clone entire repository)
- Can deploy any version of code
- Requires manual file upload

**Steps:**

#### 1. Upload Project Files to Server

Upload the entire `markdown-editor` directory to the server's deployment directory (e.g., `/opt/markdown-editor`).

```bash
# Assuming you are on your local development machine
scp -r markdown-editor/ user@your-server:/opt/
```

#### 2. Configure Environment Variables

Edit the `.env` file on the server and modify the configuration according to your needs:

```bash
cd /opt/markdown-editor
nano .env
```

Main configuration items:

```
# Database Configuration
DB_HOST=39.107.242.71        # Database host address
DB_PORT=3346                 # Database port
DB_NAME=markdown_editor      # Database name
DB_USERNAME=root             # Database username
DB_PASSWORD=123456           # Database password

# JWT Configuration (recommended to modify in production)
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400000

# Service Port Configuration
BACKEND_PORT=8081            # Backend port
FRONTEND_PORT=8080           # Frontend port
```

#### 3. Start Services

```bash
cd /opt/markdown-editor

# Build and start services
docker compose up -d --build

# Or start directly (if already built)
docker compose up -d
```

#### 4. Check Service Status

```bash
# Check all service status
docker compose ps

# Check service logs
docker compose logs -f

# Check specific service logs
docker compose logs -f backend
docker compose logs -f frontend
```

***

### Method 2: Git Clone Deployment

Clone code directly from Git repository through Dockerfile and build inside the container. Suitable for continuous deployment and update scenarios.

**Features:**

- Simple deployment, only needs one `docker-compose.git.yml` file
- Supports one-click update: rebuild to get latest code
- Suitable for CI/CD automated deployment
- Requires server to access Git repository

**Steps:**

#### 1. Server Environment Preparation

Ensure Docker and Docker Compose are installed on the server:

```bash
# Check Docker version
docker --version

# Check Docker Compose version
docker compose version
```

If not installed, please refer to the [Environment Requirements](#environment-requirements) section of this document for installation.

#### 2. Create Deployment Directory

```bash
# Create deployment directory
sudo mkdir -p /opt/markdown-editor
cd /opt/markdown-editor
```

#### 3. Create docker-compose.git.yml Configuration File

Upload the `docker-compose.git.yml` file to the server, or create it directly:

```bash
nano docker-compose.git.yml
```

**Complete configuration content:**

```yaml
version: '3.8'

services:
  # Backend Service - Deploy from Git Clone
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile.git
    container_name: markdown-backend-git
    restart: unless-stopped
    ports:
      - "${BACKEND_PORT:-8081}:8081"
    environment:
      # Git Configuration
      GIT_REPO_URL: ${GIT_REPO_URL:-https://github.com/PekJone/markdown-editor.git}
      GIT_BRANCH: ${GIT_BRANCH:-main}
      # Database Configuration (using external database)
      SPRING_DATASOURCE_URL: jdbc:mysql://${DB_HOST:-39.107.242.71}:${DB_PORT:-3346}/${DB_NAME:-markdown_editor}?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: ${DB_USERNAME:-root}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD:-123456}
      # JWT Configuration
      JWT_SECRET: ${JWT_SECRET:-markdown-editor-secret-key-2024}
      JWT_EXPIRATION: ${JWT_EXPIRATION:-86400000}
      # File Upload Directory
      FILE_UPLOAD_DIR: ${FILE_UPLOAD_DIR:-/app/uploads}
      # Timezone Configuration
      TZ: ${TZ:-Asia/Shanghai}
    volumes:
      # Upload file persistence
      - uploads-git:/app/uploads
    networks:
      - markdown-network-git
    healthcheck:
      test: ["CMD", "wget", "-q", "--spider", "http://localhost:8081/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  # Frontend Service - Deploy from Git Clone
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
      # Git Configuration
      GIT_REPO_URL: ${GIT_REPO_URL:-https://github.com/PekJone/markdown-editor.git}
      GIT_BRANCH: ${GIT_BRANCH:-main}
      # API Proxy Configuration
      API_BASE_URL: http://backend:8081
      TZ: ${TZ:-Asia/Shanghai}
    healthcheck:
      test: ["CMD", "wget", "-q", "--spider", "http://localhost/"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

# Persistence Volumes
volumes:
  uploads-git:
    driver: local

# Network Configuration
networks:
  markdown-network-git:
    driver: bridge
```

#### 4. Create Backend Dockerfile.git

Upload the `backend/Dockerfile.git` file to the server's `/opt/markdown-editor/backend/` directory:

```bash
mkdir -p /opt/markdown-editor/backend
nano /opt/markdown-editor/backend/Dockerfile.git
```

**Complete configuration content:**

```dockerfile
FROM alpine/git:latest AS git-clone
LABEL maintainer="markdown-editor"
LABEL description="Markdown Editor Backend - Git Clone Stage"

ARG GIT_REPO_URL=https://github.com/PekJone/markdown-editor.git
ARG GIT_BRANCH=main

WORKDIR /app
RUN git clone --depth 1 --branch ${GIT_BRANCH} ${GIT_REPO_URL} /app/backend

FROM maven:3.8.6-jdk-8 AS builder
WORKDIR /build

COPY --from=git-clone /app/backend/pom.xml .
RUN mvn dependency:go-offline -B

COPY --from=git-clone /app/backend/src ./src
RUN mvn clean package -DskipTests -q

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

#### 5. Create Frontend Dockerfile.git

Upload the `frontend/Dockerfile.git` file to the server's `/opt/markdown-editor/frontend/` directory:

```bash
mkdir -p /opt/markdown-editor/frontend
nano /opt/markdown-editor/frontend/Dockerfile.git
```

**Complete configuration content:**

```dockerfile
FROM alpine/git:latest AS git-clone
LABEL maintainer="markdown-editor"
LABEL description="Markdown Editor Frontend - Git Clone Stage"

ARG GIT_REPO_URL=https://github.com/PekJone/markdown-editor.git
ARG GIT_BRANCH=main

WORKDIR /app
RUN git clone --depth 1 --branch ${GIT_BRANCH} ${GIT_REPO_URL} /app/frontend

FROM node:18-alpine AS builder
LABEL maintainer="markdown-editor"
LABEL description="Markdown Editor Frontend Build Stage"

WORKDIR /app

COPY --from=git-clone /app/frontend/package*.json ./

RUN npm config set registry https://registry.npmmirror.com

RUN npm ci --only=production --registry=https://registry.npmmirror.com

COPY --from=git-clone /app/frontend . .

RUN npm run build

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

#### 6. Create Frontend nginx.conf (Required)

The frontend deployment requires an nginx configuration file. Please create `/opt/markdown-editor/frontend/nginx.conf`:

```bash
nano /opt/markdown-editor/frontend/nginx.conf
```

**Complete configuration content:**

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

    # Gzip Compression
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

        # Static Resource Cache
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            expires 30d;
            add_header Cache-Control "public, immutable";
        }

        # API Proxy
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

        # Frontend Routing
        location / {
            try_files $uri $uri/ /index.html;
        }

        # Health Check
        location /health {
            return 200 'OK';
            add_header Content-Type text/plain;
        }
    }
}
```

#### 7. Create Environment Variable Configuration File (Optional)

Create `.env` file for custom configuration:

```bash
nano /opt/markdown-editor/.env
```

**Configuration content:**

```env
# Git Repository Configuration
GIT_REPO_URL=https://github.com/PekJone/markdown-editor.git
GIT_BRANCH=main

# Database Configuration
DB_HOST=39.107.242.71
DB_PORT=3346
DB_NAME=markdown_editor
DB_USERNAME=root
DB_PASSWORD=123456

# JWT Configuration
JWT_SECRET=markdown-editor-secret-key-2024
JWT_EXPIRATION=86400000

# Service Port Configuration
BACKEND_PORT=8081
FRONTEND_PORT=8080

# Timezone Configuration
TZ=Asia/Shanghai
```

#### 8. Start Services

```bash
cd /opt/markdown-editor

# Build and start services (first deployment)
docker compose -f docker-compose.git.yml up -d --build
```

#### 9. Check Service Status

```bash
# Check all service status
docker compose -f docker-compose.git.yml ps

# Check service logs
docker compose -f docker-compose.git.yml logs -f

# Check specific service logs
docker compose -f docker-compose.git.yml logs -f backend
docker compose -f docker-compose.git.yml logs -f frontend
```

#### 10. Update Deployment

When there are updates in the Git repository, simply rebuild:

```bash
cd /opt/markdown-editor

# Pull latest code and rebuild
docker compose -f docker-compose.git.yml up -d --build

# Or rebuild only a specific service
docker compose -f docker-compose.git.yml up -d --build backend
docker compose -f docker-compose.git.yml up -d --build frontend
```

#### 11. One-Click Deployment Script

Create a convenient script `/opt/markdown-editor/start-git.sh`:

```bash
#!/bin/bash
echo "===== Markdown Editor Git Deployment ====="
echo "Starting deployment..."
docker compose -f docker-compose.git.yml up -d --build
echo "Deployment completed!"
echo "Frontend URL: http://your-server-ip:8080"
echo "Backend URL: http://your-server-ip:8081"
echo "Swagger Documentation: http://your-server-ip:8081/swagger-ui/index.html"
```

Add execution permission and use:

```bash
chmod +x /opt/markdown-editor/start-git.sh
./start-git.sh
```

Create stop script `/opt/markdown-editor/stop-git.sh`:

```bash
#!/bin/bash
echo "===== Stopping Markdown Editor ====="
docker compose -f docker-compose.git.yml down
echo "Services stopped"
```

Add execution permission and use:

```bash
chmod +x /opt/markdown-editor/stop-git.sh
./stop-git.sh
```

***

## Environment Requirements

Before deployment, ensure the server meets the following requirements:

- **Operating System**: Linux (Recommended Ubuntu 20.04+ / CentOS 7+)
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **Memory**: At least 2GB RAM
- **Disk**: At least 10GB available space
- **Network**: Access to external database (39.107.242.71:3346) and GitHub

### Install Docker and Docker Compose

If Docker and Docker Compose are not installed on the server, follow these steps:

#### Ubuntu/Debian Systems:

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install dependencies
sudo apt install -y apt-transport-https ca-certificates curl software-properties-common

# Add Docker official GPG key
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# Add Docker software source
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install Docker
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# Start Docker and set to autostart
sudo systemctl start docker
sudo systemctl enable docker
```

#### CentOS/RHEL Systems:

```bash
# Install Docker
sudo yum install -y yum-utils
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo yum install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# Start Docker and set to autostart
sudo systemctl start docker
sudo systemctl enable docker
```

#### Verify Installation:

```bash
# Check Docker version
docker --version

# Check Docker Compose version
docker compose version
```

***

## Configuration Instructions

### Database Configuration

Current configuration uses external MySQL database at address: `39.107.242.71:3346`

**Note**:

- Ensure the server can access this database address
- Ensure the database user has sufficient permissions
- If you need to change the database, modify the configuration in the `.env` file

### Port Configuration

| Service | Host Port | Container Port | Description |
| -- | ----- | ---- | ------ |
| Frontend | 8080  | 80   | User access entry |
| Backend | 8081  | 8081 | API Service |

If the port is occupied, you can modify the port in the `.env` file.

### Persistence Configuration

- **Upload Files**: Stored in Docker volume `markdown-editor_uploads`
- Data is automatically persisted and won't be lost when the container restarts

***

## Common Commands

### Service Management

```bash
# Start all services
docker compose up -d

# Stop all services
docker compose stop

# Restart all services
docker compose restart

# Stop and delete all containers
docker compose down

# Stop and delete all containers, networks, volumes (use with caution)
docker compose down -v
```

### Log Viewing

```bash
# View all service logs
docker compose logs -f

# View backend logs
docker compose logs -f backend

# View frontend logs
docker compose logs -f frontend

# View last 100 lines of logs
docker compose logs --tail=100
```

### Update Deployment

```bash
# 1. Pull latest code
git pull

# 2. Rebuild and start
docker compose up -d --build

# 3. Clean up old images (optional)
docker image prune -a
```

### Enter Container

```bash
# Enter backend container
docker compose exec backend sh

# Enter frontend container
docker compose exec frontend sh
```

***

## Access Addresses

After successful deployment, you can access via the following addresses:

| Service             | Access Address                                               |
| -------------- | -------------------------------------------------- |
| **Frontend Application**       | `http://your-server-ip:8080`                       |
| **Backend API**     | `http://your-server-ip:8081`                       |
| **Swagger Documentation** | `http://your-server-ip:8081/swagger-ui/index.html` |

**Note**:

- If you need to access via domain name, configure DNS resolution or reverse proxy
- It is recommended to configure HTTPS in production environment

***

## Troubleshooting

### Services Cannot Start

1. Check if ports are occupied:

```bash
# Check port occupancy
sudo netstat -tlnp | grep -E ':(80|8081)'
```

2. Check Docker service status:

```bash
sudo systemctl status docker
```

3. View detailed error logs:

```bash
docker compose logs backend
docker compose logs frontend
```

### Database Connection Failed

1. Check network connection:

```bash
# Test database connection inside container
docker compose exec backend ping 39.107.242.71
```

2. Confirm database configuration is correct:

```bash
# Check environment variables
docker compose exec backend env | grep SPRING_DATASOURCE
```

3. Check database user permissions

### Upload File Issues

1. Check directory permissions:

```bash
docker compose exec backend ls -la /app/uploads
```

2. Check disk space:

```bash
docker system df
```

***

## Backup and Recovery

### Backup Upload Files

```bash
# Create backup directory
mkdir -p /opt/backups

# Backup upload files
docker run --rm -v markdown-editor_uploads:/data -v /opt/backups:/backup alpine tar czf /backup/uploads-$(date +%Y%m%d).tar.gz -C /data .
```

### Restore Upload Files

```bash
# Restore from backup file
docker run --rm -v markdown-editor_uploads:/data -v /opt/backups:/backup alpine tar xzf /backup/uploads-20240101.tar.gz -C /data
```

### View Backups

```bash
# List backup files
ls -lh /opt/backups/
```

***

## Security Recommendations

1. **Modify default passwords and keys**
   - Modify database password
   - Modify JWT_SECRET to a strong key
2. **Configure firewall**

```bash
# Only open necessary ports
sudo ufw allow 80/tcp
sudo ufw allow 22/tcp
sudo ufw enable
```

3. **Configure HTTPS** (required in production)
   - Use Let's Encrypt
   - Or configure Nginx SSL certificate
4. **Regular backups**
   - Set up scheduled backup tasks
   - Regularly test backup recovery
5. **Monitor service status**
   - Configure health check alerts
   - Monitor resource usage

***

## Performance Optimization

1. **Resource Limits**

You can add resource limits in `docker-compose.yml`:

```yaml
backend:
  deploy:
    resources:
      limits:
        memory: 1G
        cpus: '0.5'
```

2. **Log Management**

Configure log rotation to avoid occupying too much disk space.

3. **Cache Optimization**

Configure appropriate caching strategies to improve response speed.

***

## Technical Support

If you have any questions, please check:

- Service logs: `docker compose logs -f`
- Project documentation: README.md
- Swagger documentation: http://your-server-ip:8081/swagger-ui/index.html

***

## Update Log

### v1.0.0 (2024-01-01)

- Initial version
- Support Docker Compose deployment
- Use external MySQL database
