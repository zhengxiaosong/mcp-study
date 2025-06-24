# STDIO MCP 服务器

这是一个基于标准输入输出的 MCP（Model Context Protocol）服务器实现，通过标准输入输出流与客户端通信。

## 功能特性

- 📡 基于标准输入输出的通信
- 🛠️ 提供多种工具实现：
  - 计算器工具（Calculator）
  - 问候工具（Greeting）
- 💾 内存资源管理
- 📊 日志记录功能

## 技术栈

- Java 17
- MCP SDK 0.10.0
- Jackson 2.17.1

## 快速启动

1. 编译项目：

    ```bash
    mvn clean package
    ```

2. 运行服务器：

    ```bash
    java -jar target/stdio-mcp-server-1.0-SNAPSHOT-jar-with-dependencies.jar
    ```

## 使用说明

服务器启动后，将通过标准输入输出与客户端通信。客户端需要：
1.通过标准输入发送请求
2.从标准输出读取响应

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
- 确保标准输入输出流未被重定向
- 建议在生产环境中配置适当的日志级别
