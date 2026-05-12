@echo off
REM Markdown Editor 更新部署脚本 (Windows)
REM 用于在服务器上更新应用最新代码

echo ========================================
echo    Markdown Editor 更新部署脚本
echo ========================================
echo.

REM 检查是否在正确的目录
if not exist "docker-compose.git.yml" (
    echo ❌ 错误：找不到 docker-compose.git.yml
    echo 请确保在项目根目录下运行此脚本
    pause
    exit /b 1
)

REM 检查 Docker Compose 版本
docker compose version >nul 2>&1
if %errorlevel% equ 0 (
    set DOCKER_COMPOSE=docker compose
) else (
    docker-compose version >nul 2>&1
    if %errorlevel% equ 0 (
        set DOCKER_COMPOSE=docker-compose
    ) else (
        echo ❌ 错误：Docker Compose 未安装
        pause
        exit /b 1
    )
)

REM 设置 BUILD_TIMESTAMP 为当前时间戳
for /f "tokens=2 delims==" %%i in ('wmic os get localdatetime /value') do set datetime=%%i
set BUILD_TIMESTAMP=%datetime:~0,14%
echo ✅ BUILD_TIMESTAMP=%BUILD_TIMESTAMP%
echo.

echo 正在停止现有服务...
%DOCKER_COMPOSE% -f docker-compose.git.yml down

echo.
echo 正在清理旧的 Docker 镜像...
docker image prune -f

echo.
echo 正在构建并启动服务（拉取最新代码）...
set BUILD_TIMESTAMP=%BUILD_TIMESTAMP%
%DOCKER_COMPOSE% -f docker-compose.git.yml up -d --build

echo.
echo ========================================
if %errorlevel% equ 0 (
    echo    ✅ 部署更新成功！
    echo.
    echo    正在等待服务启动...
    timeout /t 5 /nobreak >nul
    echo.
    echo    查看服务状态：
    %DOCKER_COMPOSE% -f docker-compose.git.yml ps
    echo.
    echo    访问地址：
    echo    前端应用：http://localhost:8080
    echo    后端 API：http://localhost:8081
    echo    Swagger：http://localhost:8081/swagger-ui/index.html
    echo.
    echo    查看日志：
    echo    %DOCKER_COMPOSE% -f docker-compose.git.yml logs -f
) else (
    echo    ❌ 部署更新失败
    echo.
    echo    查看错误日志：
    echo    %DOCKER_COMPOSE% -f docker-compose.git.yml logs
)
echo ========================================
pause
