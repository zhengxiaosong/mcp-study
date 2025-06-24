@echo off

echo 启动 MCP + LLM 控制台程序...
echo 请确保 MCP 服务器已经在 http://localhost:8080 运行
echo.

REM 编译项目
echo 正在编译项目...
call mvn clean compile -q

if %ERRORLEVEL% neq 0 (
    echo 编译失败!
    pause
    exit /b 1
)

echo 编译成功!
echo.

REM 运行控制台程序
echo 启动控制台...
call mvn exec:java -Dexec.mainClass="io.zhijian.tools.mcp.console.McpLlmConsole" -q

pause 