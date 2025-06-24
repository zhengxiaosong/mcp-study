# MCP 学习项目

这是一个用于学习和实践 Model Context Protocol (MCP) 的项目，包含服务器端和客户端实现。

MCP 采用的是 `https://github.com/zhengxiaosong/mcp-java-sdk` 这个版本

## 项目结构

项目包含三个主要模块：

### 1. HTTP MCP 服务器 (http-mcp-server)

- 基于 Spring Boot 的 Web 服务器
- 提供 HTTP SSE 接口
- 支持多种工具和资源
- [详细文档](./http-mcp-server/README.md)

### 2. STDIO MCP 服务器 (stdio-mcp-server)

- 基于标准输入输出的服务器
- 轻量级实现
- 支持工具和资源
- [详细文档](./stdio-mcp-server/README.md)

### 3. HTTP MCP 客户端 (http-mcp-client)

- HTTP SSE 客户端
- LLM 集成
- 交互式控制台
- [详细文档](./http-mcp-client/README.md)

## 快速开始

1. 启动服务器（选择一种方式）：

    ```bash
    # HTTP 服务器
    cd http-mcp-server
    mvn clean package
    java -jar target/http-mcp-server-1.0-SNAPSHOT-jar-with-dependencies.jar

    # 或 STDIO 服务器
    cd stdio-mcp-server
    mvn clean package
    java -jar target/stdio-mcp-server-1.0-SNAPSHOT-jar-with-dependencies.jar
    ```

2. 启动客户端：

    ```bash
    cd http-mcp-client
    ./run-console.sh  # Linux/macOS
    # 或
    run-console.bat   # Windows
    ```

## 技术栈

- Java 17
- Spring Boot 3.2.5
- MCP SDK 0.10.0
- Jackson 2.17.1
- DeepSeek API

## 开发环境要求

- JDK 17 或更高版本
- Maven 3.6 或更高版本
- 网络连接（用于 Maven 依赖下载）

## 注意事项

- 确保服务器和客户端使用兼容的 MCP 版本
- 在生产环境中注意配置安全设置
- 建议使用版本控制管理代码

## 贡献指南

1. Fork 项目
2. 创建特性分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request
