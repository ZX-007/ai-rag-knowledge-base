@echo off
REM ============================================================================
REM 容器管理脚本 - MCP Server CSDN
REM 用法: run.cmd [start|stop|restart|logs|status|health|shell|help]
REM ============================================================================

chcp 65001 >nul
pushd "%~dp0.."

REM 解析操作
set "ACTION=%~1"
if "%ACTION%"=="" set "ACTION=start"

REM 显示标题
echo ==========================================
echo MCP Server CSDN - 容器管理
echo ==========================================
echo.

REM 帮助信息
if /i "%ACTION%"=="help" goto show_help
if /i "%ACTION%"=="--help" goto show_help
if /i "%ACTION%"=="-h" goto show_help

REM 检查 Docker
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker 未运行
    popd
    exit /b 1
)

REM 路由操作
if /i "%ACTION%"=="start" goto start
if /i "%ACTION%"=="stop" goto stop
if /i "%ACTION%"=="restart" goto restart
if /i "%ACTION%"=="logs" goto logs
if /i "%ACTION%"=="status" goto status
if /i "%ACTION%"=="health" goto health
if /i "%ACTION%"=="shell" goto shell

echo [ERROR] 未知操作: %ACTION%
echo 使用 'scripts\run.cmd help' 查看帮助
popd
exit /b 1

REM ============================================================================
REM 启动
REM ============================================================================
:start
echo [INFO] 启动容器...
echo.

docker ps -a --format "{{.Names}}" | findstr /C:"mcp-server-csdn-sse" >nul
if not errorlevel 1 (
    docker ps --format "{{.Names}}" | findstr /C:"mcp-server-csdn-sse" >nul
    if not errorlevel 1 (
        echo [INFO] 容器已在运行
        goto status
    )
    docker-compose start
    goto check_start
)

docker-compose up -d

:check_start
if errorlevel 1 (
    echo [ERROR] 容器启动失败
    popd
    exit /b 1
)

echo.
echo [SUCCESS] 容器启动成功
echo.
timeout /t 3 /nobreak >nul
goto status

REM ============================================================================
REM 停止
REM ============================================================================
:stop
echo [INFO] 停止容器...
docker-compose down

if errorlevel 1 (
    echo [ERROR] 停止失败
    popd
    exit /b 1
)

echo.
echo [SUCCESS] 容器已停止
echo.
goto end

REM ============================================================================
REM 重启
REM ============================================================================
:restart
echo [INFO] 重启容器...
docker-compose restart

if errorlevel 1 (
    echo [ERROR] 重启失败
    popd
    exit /b 1
)

echo.
echo [SUCCESS] 容器重启成功
echo.
timeout /t 3 /nobreak >nul
goto status

REM ============================================================================
REM 日志
REM ============================================================================
:logs
echo [INFO] 显示日志（Ctrl+C 退出）...
echo.
docker-compose logs -f
goto end

REM ============================================================================
REM 状态
REM ============================================================================
:status
echo ==========================================
echo 容器状态
echo ==========================================
docker ps --filter "name=mcp-server-csdn" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo.
echo 快捷命令:
echo   scripts\run.cmd logs     - 查看日志
echo   scripts\run.cmd health   - 健康检查
echo   scripts\run.cmd shell    - 进入容器
echo   scripts\stop.cmd         - 停止容器
echo.
goto end

REM ============================================================================
REM 健康检查
REM ============================================================================
:health
echo [INFO] 健康检查...
echo.

docker ps --format "{{.Names}}" | findstr /C:"mcp-server-csdn-sse" >nul
if errorlevel 1 (
    echo [ERROR] 容器未运行
    popd
    exit /b 1
)

docker exec mcp-server-csdn-sse wget -q -O - http://localhost:8081/actuator/health

if errorlevel 1 (
    echo [ERROR] 健康检查失败
    popd
    exit /b 1
)

echo.
echo.
echo [SUCCESS] 服务正常
echo.
echo 服务地址: http://localhost:8081
echo.
goto end

REM ============================================================================
REM Shell
REM ============================================================================
:shell
echo [INFO] 进入容器（输入 exit 退出）...
echo.

docker ps --format "{{.Names}}" | findstr /C:"mcp-server-csdn-sse" >nul
if errorlevel 1 (
    echo [ERROR] 容器未运行
    popd
    exit /b 1
)

docker exec -it mcp-server-csdn-sse /bin/sh
goto end

REM ============================================================================
REM 帮助
REM ============================================================================
:show_help
echo.
echo 用法: scripts\run.cmd [操作]
echo.
echo 操作:
echo   start      启动容器（默认）
echo   stop       停止容器
echo   restart    重启容器
echo   logs       查看日志
echo   status     查看状态
echo   health     健康检查
echo   shell      进入容器
echo   help       显示帮助
echo.
echo 示例:
echo   scripts\run.cmd          # 启动
echo   scripts\run.cmd logs     # 日志
echo   scripts\run.cmd health   # 检查
echo.
popd
exit /b 0

:end
popd
exit /b 0
