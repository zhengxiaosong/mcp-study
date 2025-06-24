# HTTP MCP 服务器

这是一个基于 Spring Boot 的 MCP（Model Context Protocol）服务器实现，提供 HTTP SSE（Server-Sent Events）接口供客户端连接。

## 功能特性

- 🌐 基于 Spring Boot 的 Web 服务器
- 🔌 支持 HTTP SSE 长连接
- 🛠️ 提供多种工具实现：
  - 计算器工具（Calculator）
  - 问候工具（Greeting）
- 📝 支持客服 Prompt 模板
- 💾 内存资源管理
- 📊 日志记录功能

## 技术栈

- Java 17
- Spring Boot 3.2.5
- MCP SDK 0.10.0
- Jackson 2.17.1

## 快速启动

1. 编译项目：

    ```bash
    mvn clean package
    ```

2. 运行服务器：

    ```bash
    java -jar target/http-mcp-server-1.0-SNAPSHOT-jar-with-dependencies.jar
    ```

服务器将在 `http://localhost:8080` 启动。

## API 端点

- SSE 连接：`/sse`
- 健康检查：`/actuator/health`

## 配置说明

服务器默认配置：

- 端口：8080
- 编码：UTF-8
- 日志级别：INFO

## 开发说明

### 添加新工具

1. 在 `tools` 包下创建新的工具类
2. 实现工具接口
3. 在 `MyServer` 类中注册工具

### 添加新资源

1. 在 `resources` 包下创建新的资源类
2. 实现资源接口
3. 在 `MyServer` 类中注册资源

## 注意事项

- 确保 Java 17 或更高版本
- 确保 8080 端口未被占用
- 建议在生产环境中配置适当的日志级别
