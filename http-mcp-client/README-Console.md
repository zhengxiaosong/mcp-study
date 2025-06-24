# MCP + LLM 智能助手控制台

这是一个集成了MCP客户端和LLM的控制台程序，允许用户通过命令行与LLM交流，LLM可以智能地调用MCP工具来帮助解决问题。

## 功能特性

- 🤖 与DeepSeek LLM进行自然语言对话
- 🔧 LLM可以自动调用MCP工具（计算器、问候等）
- 💬 支持多轮对话，保持上下文
- 📝 实时显示工具调用过程
- 🎯 智能工具选择和参数生成

## 前置条件

1. **启动MCP服务器**

   ```bash
   # 确保MCP服务器在 http://localhost:8080 运行
   # 可以使用 http-mcp-server 或 stdio-mcp-server
   ```

2. **配置LLM API密钥**
   - 检查 `DeepSeekConfig.java` 中的API配置
   - 确保API密钥有效

## 快速启动

### Linux/macOS

```bash
cd http-mcp-client
./run-console.sh
```

### Windows

```cmd
cd http-mcp-client
run-console.bat
```

### 手动启动

```bash
cd http-mcp-client
mvn clean compile
mvn exec:java -Dexec.mainClass="io.zhijian.tools.mcp.console.McpLlmConsole"
```

## 使用说明

### 基本命令

- 输入任何问题或请求，LLM会智能回复
- `tools` - 查看可用的MCP工具
- `clear` - 清空对话历史
- `prompt-demo` - 体验客服Prompt（将客服对话模板注入上下文，后续对话带有客服风格）
- `exit` 或 `quit` - 退出程序

### 示例对话

```plain-text
用户: 帮我计算 15 + 27
助手: 我来帮你计算 15 + 27。

【工具调用】
工具名称: calculator
参数: {"operation": "add", "a": 15, "b": 27}
【工具调用结束】

【工具执行结果】42【结果结束】

计算结果是 42。

用户: 用中文正式地问候张三
助手: 我来为您生成一个中文正式问候。

【工具调用】
工具名称: greeting
参数: {"name": "张三", "language": "zh", "style": "formal"}
【工具调用结束】

【工具执行结果】尊敬的张三先生/女士，您好！【结果结束】

为您生成的正式中文问候是：尊敬的张三先生/女士，您好！
```

### 示例：体验客服Prompt

```plain-text
用户: prompt-demo
客服Prompt已注入对话历史，后续对话将带有客服风格！
用户: 我想咨询一下产品售后
助手: ...（此时助手回复将带有客服风格和流程）
```

## LLM与MCP Prompt结合原理

1. 用户输入 `prompt-demo` 后，程序会调用 MCP 服务器的 `customer_service` prompt，将其生成的多轮消息（如系统提示、欢迎语、示例对话等）注入到 LLM 的对话历史。
2. 之后用户的每一次输入，LLM 都会在这些客服Prompt上下文的基础上生成回复，实现"带有客服风格和流程"的智能对话。
3. 你可以随时输入 `clear` 清空历史，或再次输入 `prompt-demo` 重新体验。

## 工作原理

1. **初始化阶段**
   - 连接到MCP服务器获取可用工具列表
   - 构建系统提示，告知LLM可用的工具和使用格式
   - 建立与LLM的连接

2. **对话处理**
   - 用户输入被添加到对话历史
   - LLM分析用户需求，决定是否需要使用工具
   - 如果需要工具，LLM生成标准格式的工具调用请求

3. **工具调用**
   - 解析LLM生成的工具调用请求
   - 通过MCP客户端调用相应工具
   - 将工具结果集成到对话中

4. **响应生成**
   - LLM基于工具结果生成最终回复
   - 显示完整的对话过程

## 支持的工具

程序会自动发现MCP服务器提供的所有工具，常见的包括：

- **calculator**: 数学计算（加减乘除）
- **greeting**: 多语言问候生成
- **其他工具**: 根据MCP服务器配置

## 故障排除

### 常见问题

1. **无法连接MCP服务器**

   ```plain-text
   错误: MCP服务器连接失败
   解决: 确保MCP服务器在 localhost:8080 运行
   ```

2. **LLM API调用失败**

   ```plain-text
   错误: HTTP请求失败，状态码: 401
   解决: 检查 DeepSeekConfig.java 中的API密钥
   ```

3. **工具调用失败**

   ```plain-text
   错误: 工具调用失败
   解决: 检查工具参数格式是否正确
   ```

### 调试模式

如需详细日志，可以设置JVM参数：

```bash
mvn exec:java -Dexec.mainClass="io.zhijian.tools.mcp.console.McpLlmConsole" -Dlogback.configurationFile=logback-debug.xml
```

## 技术架构

```plain-text
用户输入 → LLM分析 → 工具调用决策 → MCP客户端 → 工具执行 → 结果整合 → 用户展示
```

- **LLM客户端**: DeepSeekHttpClient（纯HTTP，无Spring依赖）
- **MCP客户端**: 官方MCP SDK
- **传输协议**: HTTP SSE
- **数据格式**: JSON

## 扩展开发

### 添加新工具支持

1. 在MCP服务器端添加新工具
2. 重启控制台程序，会自动发现新工具
3. LLM会自动学习使用新工具

### 自定义LLM提示

修改 `initializeSystemPrompt()` 方法来自定义LLM的行为。

### 添加新命令

在 `start()` 方法中添加新的命令处理逻辑
