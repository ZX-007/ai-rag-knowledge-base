@echo off
REM ============================================================================
REM 停止容器脚本 - MCP Server CSDN
REM 用法: stop.cmd [--remove] [--kill] [--help]
REM ============================================================================

chcp 65001 >nul
pushd "%~dp0.."

REM 解析参数
set "REMOVE="
set "KILL="

:parse_args
if "%~1"=="" goto end_parse
if /i "%~1"=="--remove" set "REMOVE=1"
if /i "%~1"=="-r" set "REMOVE=1"
if /i "%~1"=="--kill" set "KILL=1"
if /i "%~1"=="-k" set "KILL=1"
if /i "%~1"=="--help" goto show_help
if /i "%~1"=="-h" goto show_help
shift
goto parse_args
:end_parse

REM 显示标题
echo ==========================================
echo MCP Server CSDN - 停止容器
echo ==========================================
echo.

REM 检查 Docker
docker info >nul 2>&1
if errorlevel 1 (
    echo [WARNING] Docker 未运行
    popd
    exit /b 0
)

REM 检查容器
docker ps -a --format "{{.Names}}" | findstr /C:"mcp-server-csdn-sse" >nul
if errorlevel 1 (
    echo [INFO] 容器不存在
    popd
    exit /b 0
)

docker ps --format "{{.Names}}" | findstr /C:"mcp-server-csdn-sse" >nul
if errorlevel 1 (
    echo [INFO] 容器已停止
    
    if defined REMOVE (
        echo [INFO] 删除容器...
        docker-compose down
        echo.
        echo [SUCCESS] 容器已删除
        echo.
    ) else (
        echo 使用 stop.cmd --remove 删除容器
        echo.
    )
    popd
    exit /b 0
)

REM 停止容器
echo [INFO] 正在停止容器...
echo.

if defined KILL (
    echo [WARNING] 强制停止...
    docker kill mcp-server-csdn-sse
    
    if errorlevel 1 (
        echo [ERROR] 停止失败
        popd
        exit /b 1
    )
    
    echo.
    echo [SUCCESS] 已强制停止
    
    if defined REMOVE (
        docker rm mcp-server-csdn-sse
        echo [SUCCESS] 容器已删除
    )
    echo.
    
) else if defined REMOVE (
    docker-compose down
    
    if errorlevel 1 (
        echo [ERROR] 停止失败
        popd
        exit /b 1
    )
    
    echo.
    echo [SUCCESS] 容器已停止并删除
    echo.
    
) else (
    docker-compose stop
    
    if errorlevel 1 (
        echo [ERROR] 停止失败
        popd
        exit /b 1
    )
    
    echo.
    echo [SUCCESS] 容器已停止
    echo.
    echo 提示:
    echo   scripts\run.cmd start      - 重新启动
    echo   scripts\stop.cmd --remove  - 删除容器
    echo.
)

REM 显示状态
docker ps -a --filter "name=mcp-server-csdn" --format "table {{.Names}}\t{{.Status}}"
echo.

goto end

:show_help
echo.
echo 用法: scripts\stop.cmd [选项]
echo.
echo 选项:
echo   --remove, -r    停止并删除容器
echo   --kill, -k      强制停止
echo   --help, -h      显示帮助
echo.
echo 示例:
echo   scripts\stop.cmd            # 优雅停止
echo   scripts\stop.cmd --remove   # 停止并删除
echo   scripts\stop.cmd --kill     # 强制停止
echo.
popd
exit /b 0

:end
popd
exit /b 0
