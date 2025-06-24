# LLM客户端管理系统

## 概述

本项目实现了多LLM客户端管理系统，支持自动故障转移和负载均衡。

## 支持的LLM客户端

### 1. DeepSeek

- 配置项：`deepseek.*`
- 默认API：`https://api.deepseek.com/v1/chat/completions`

### 2. Azure OpenAI

- 配置项：`azure.openai.*`
- 支持GPT-4o等模型

## 配置说明

在 `src/main/resources/application.properties` 中配置：

```properties
# Azure OpenAI Configuration
azure.openai.endpoint=https://your-endpoint.openai.azure.com
azure.openai.credential=your-api-key
azure.openai.engine=gpt-4o
azure.openai.api_version=2024-02-15-preview
azure.openai.temperature=0.01
azure.openai.max_tokens=64000
azure.openai.top_p=0.95

# DeepSeek Configuration
deepseek.api.url=https://api.deepseek.com/v1/chat/completions
deepseek.api.key=your-deepseek-api-key
deepseek.model=deepseek-chat
deepseek.temperature=0.1
deepseek.max_tokens=4000
deepseek.top_p=0.95
deepseek.top_k=50
deepseek.frequency_penalty=0.0
```

## 自动故障转移

系统会自动在以下情况切换LLM客户端：

1. **请求超时**：当LLM响应超过1分钟
2. **网络错误**：连接失败或网络异常
3. **API错误**：返回非200状态码
4. **解析错误**：响应格式异常

## 工作流程

1. **初始化**：创建LLMClientManager，加载所有可用客户端
2. **请求发送**：尝试使用当前活跃客户端发送请求
3. **故障检测**：如果当前客户端失败，自动切换到下一个
4. **循环尝试**：直到找到可用的客户端或所有客户端都失败
5. **结果返回**：使用成功客户端的响应格式解析结果

## 使用示例

```java
// 创建LLM客户端管理器
LLMClientManager manager = new LLMClientManager(consoleUI);

// 发送请求（自动故障转移）
String response = manager.chat(messages);

// 获取当前活跃客户端
LLMClient currentClient = manager.getCurrentClient();
```

## 扩展新的LLM客户端

1. 实现 `LLMClient` 接口
2. 在 `LLMClientManager` 构造函数中添加新客户端
3. 在配置文件中添加相应配置项

## 注意事项

- 确保所有API密钥配置正确
- 网络连接稳定
- 监控客户端切换日志
- 定期检查API配额使用情况
