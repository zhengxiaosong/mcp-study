#!/bin/bash

echo "=========================================="
echo "启动 MCP 注解框架服务器"
echo "=========================================="

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请先安装Java"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven环境，请先安装Maven"
    exit 1
fi

echo "正在编译项目..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "编译失败，请检查代码"
    exit 1
fi

echo "正在启动服务器..."
echo "服务器将使用注解框架自动注册组件"
echo "访问地址: http://localhost:8080/sse"
echo "按 Ctrl+C 停止服务器"
echo ""

mvn spring-boot:run -Dspring-boot.run.main-class=io.zhijian.tools.mcp.MyServer 