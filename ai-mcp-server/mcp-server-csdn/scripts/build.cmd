@echo off
REM ============================================================================
REM 构建脚本 - MCP Server CSDN
REM 用法: build.cmd [--no-cache] [--auto-start] [--help]
REM ============================================================================

chcp 65001 >nul
pushd "%~dp0.."

REM 解析参数
set "NO_CACHE="
set "AUTO_START="

:parse_args
if "%~1"=="" goto end_parse
if /i "%~1"=="--no-cache" set "NO_CACHE=1"
if /i "%~1"=="--auto-start" set "AUTO_START=1"
if /i "%~1"=="--help" goto show_help
if /i "%~1"=="-h" goto show_help
shift
goto parse_args
:end_parse

REM 显示标题
echo ==========================================
echo MCP Server CSDN - 构建脚本
echo ==========================================
echo.

REM 检查 Docker
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker 未运行，请先启动 Docker Desktop
    popd
    exit /b 1
)

REM Maven 构建
if not exist "target\mcp-server-csdn.jar" (
    echo [INFO] 开始 Maven 构建...
    echo.
    call mvn clean package -DskipTests
    if errorlevel 1 (
        echo.
        echo [ERROR] Maven 构建失败
        popd
        exit /b 1
    )
    echo.
    echo [SUCCESS] Maven 构建成功
    echo.
) else (
    echo [INFO] JAR 文件已存在，跳过 Maven 构建
    echo.
)

REM Docker 构建
echo [INFO] 开始构建 Docker 镜像...
echo.

if defined NO_CACHE (
    echo [WARNING] 使用 --no-cache 模式
    docker build --no-cache -t mcp-server-csdn:0.0.1 -t mcp-server-csdn:latest .
) else (
    docker build -t mcp-server-csdn:0.0.1 -t mcp-server-csdn:latest .
)

if errorlevel 1 (
    echo.
    echo [ERROR] Docker 镜像构建失败
    popd
    exit /b 1
)

echo.
echo [SUCCESS] 镜像构建成功
echo.
docker images mcp-server-csdn
echo.

REM 导出镜像
echo [INFO] 导出镜像到 tar 文件...
docker save -o mcp-server-csdn.tar mcp-server-csdn:0.0.1

if errorlevel 1 (
    echo [ERROR] 镜像导出失败
    popd
    exit /b 1
)

echo.
echo [SUCCESS] 镜像已导出到 mcp-server-csdn.tar
echo.

REM 启动容器
if defined AUTO_START goto start_container

echo ==========================================
echo 是否立即启动容器？
echo ==========================================
echo.
set /p "CONFIRM=输入 Y 启动容器 (Y/N): "
if /i not "%CONFIRM%"=="Y" goto skip_start

:start_container
echo.
echo [INFO] 正在启动容器...
echo.

docker ps -a --format "{{.Names}}" | findstr /C:"mcp-server-csdn-sse" >nul
if not errorlevel 1 (
    echo [INFO] 删除已存在的容器...
    docker-compose down >nul 2>&1
)

docker-compose up -d

if errorlevel 1 (
    echo [ERROR] 容器启动失败
    popd
    exit /b 1
)

echo.
echo [SUCCESS] 容器启动成功
echo.
docker ps --filter "name=mcp-server-csdn"
echo.
goto end

:skip_start
echo.
echo [INFO] 跳过容器启动
echo.
echo 提示: scripts\run.cmd start
echo.
goto end

:show_help
echo.
echo 用法: scripts\build.cmd [选项]
echo.
echo 选项:
echo   --no-cache      清理缓存构建
echo   --auto-start    自动启动容器
echo   --help, -h      显示帮助
echo.
echo 示例:
echo   scripts\build.cmd                # 标准构建
echo   scripts\build.cmd --no-cache     # 清理缓存
echo   scripts\build.cmd --auto-start   # 构建并启动
echo.
popd
exit /b 0

:end
popd
exit /b 0
