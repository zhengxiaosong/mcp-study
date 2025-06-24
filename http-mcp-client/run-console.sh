#!/bin/bash

echo "启动 MCP + LLM 控制台程序..."
echo "请确保 MCP 服务器已经在 http://localhost:8080 运行"
echo ""

# 编译项目
echo "正在编译项目..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "编译失败!"
    exit 1
fi

echo "编译成功!"
echo ""

# 运行控制台程序
echo "启动控制台..."
mvn exec:java -Dexec.mainClass="io.zhijian.tools.mcp.console.McpLlmConsole" -q -X