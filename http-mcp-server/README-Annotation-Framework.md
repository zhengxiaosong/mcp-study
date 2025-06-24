# MCP 注解框架使用指南

## 概述

这个注解框架旨在简化MCP（Model Context Protocol）组件的开发，通过使用注解来自动注册工具、资源和提示，大大减少样板代码。

## 架构设计

### 职责分工

1. **MyServer.java** - 应用启动类
   - 只负责Spring Boot应用的启动
   - 简洁明了，专注于应用入口

2. **McpServerConfig.java** - MCP服务器配置类
   - 负责MCP服务器的创建和配置
   - 管理HTTP SSE传输和Servlet注册
   - 初始化内存资源

3. **McpComponentRegistry.java** - 组件注册器
   - 自动发现和注册带有注解的组件
   - 在应用启动时自动执行

### 启动流程

```
MyServer.main() 
    ↓
SpringApplication.run() 
    ↓
McpServerConfig.mcpSyncServer() - 创建MCP服务器
    ↓
McpComponentRegistry.registerComponents() - 自动注册组件
    ↓
应用启动完成
```

## 核心注解

### 1. @McpTool - 工具注解

用于标记MCP工具类，自动注册到服务器。

```java
@Component
@McpTool(
    name = "calculator",
    description = "Basic calculator for mathematical operations",
    version = "2.0.0"
)
public class CalculatorTool {
    // 工具实现
}
```

### 2. @McpResource - 资源注解

用于标记MCP资源类，自动注册到服务器。

```java
@Component
@McpResource(
    uri = "memory://resource",
    name = "Memory Resource",
    description = "A resource stored in memory",
    contentType = "application/json"
)
public class MemoryResource {
    // 资源实现
}
```

### 3. @McpPrompt - 提示注解

用于标记MCP提示类，自动注册到服务器。

```java
@Component
@McpPrompt(
    name = "analyze_tool_result",
    description = "帮助LLM分析和解释工具执行结果",
    version = "2.0.0"
)
public class ToolAnalysisPrompt {
    // 提示实现
}
```

### 4. @McpParameter - 参数注解

用于定义工具或提示的参数，自动生成JSON Schema。

```java
@McpParameter(
    name = "operation",
    description = "计算操作类型",
    type = "string",
    required = true,
    enumValues = {"add", "subtract", "multiply", "divide"}
)
private String operation;

@McpParameter(
    name = "a",
    description = "第一个操作数",
    type = "number",
    required = true
)
private Double a;
```

## 使用步骤

### 1. 创建工具类

```java
@Component
@McpTool(
    name = "weather",
    description = "Get weather information for a location",
    version = "1.0.0"
)
public class WeatherTool {

    @McpParameter(
        name = "location",
        description = "城市或地区名称",
        type = "string",
        required = true
    )
    private String location;

    @McpParameter(
        name = "unit",
        description = "温度单位",
        type = "string",
        required = false,
        defaultValue = "celsius",
        enumValues = {"celsius", "fahrenheit"}
    )
    private String unit;

    public McpServerFeatures.SyncToolSpecification createTool() throws JsonProcessingException {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool("weather", "Get weather information", generateSchema()),
                this::execute);
    }

    private CallToolResult execute(Object exchange, java.util.Map<String, Object> context) {
        // 实现工具逻辑
        List<Content> result = new ArrayList<>();
        // ... 业务逻辑
        return new CallToolResult(result, true);
    }

    private String generateSchema() throws JsonProcessingException {
        return SchemaGenerator.generateSchema(this.getClass(), "urn:jsonschema:Weather");
    }
}
```

### 2. 创建资源类

```java
@Component
@McpResource(
    uri = "file://config",
    name = "Configuration Resource",
    description = "Application configuration",
    contentType = "application/json"
)
public class ConfigResource {

    public McpServerFeatures.SyncResourceSpecification createResource() {
        return new McpServerFeatures.SyncResourceSpecification(
                new Resource("file://config", "Configuration", "App config", "application/json", null),
                this::readResource);
    }

    private ReadResourceResult readResource(Object exchange, Object context) {
        // 实现资源读取逻辑
        return new ReadResourceResult(result);
    }
}
```

### 3. 创建提示类

```java
@Component
@McpPrompt(
    name = "data_analysis",
    description = "数据分析提示模板",
    version = "1.0.0"
)
public class DataAnalysisPrompt {

    @McpParameter(
        name = "data_type",
        description = "数据类型",
        type = "string",
        required = true,
        enumValues = {"sales", "user", "performance"}
    )
    private String dataType;

    public McpServerFeatures.SyncPromptSpecification createPrompt() throws JsonProcessingException {
        return new McpServerFeatures.SyncPromptSpecification(
                new Prompt("data_analysis", "数据分析提示", generateArguments()),
                (exchange, context) -> {
                    // 实现提示逻辑
                    return new GetPromptResult(null, messages);
                });
    }

    private List<PromptArgument> generateArguments() {
        return SchemaGenerator.generatePromptArguments(this.getClass());
    }
}
```

## 自动注册机制

框架会自动扫描所有带有相应注解的Spring组件，并在应用启动时自动注册到MCP服务器。

### 注册器工作流程

1. **组件扫描**: 扫描所有带有`@McpTool`、`@McpResource`、`@McpPrompt`注解的Spring Bean
2. **方法调用**: 调用每个组件的`createTool()`、`createResource()`或`createPrompt()`方法
3. **自动注册**: 将返回的规范对象注册到MCP服务器

## 优势

### 1. 代码简化
- **传统方式**: 需要手动创建JSON Schema，手动注册组件
- **注解方式**: 只需添加注解，自动生成Schema和注册

### 2. 类型安全
- 参数定义与Java字段绑定，减少错误
- 编译时检查，避免运行时错误

### 3. 维护性
- 参数定义集中在一个地方
- 修改参数时只需修改注解，无需手动更新Schema

### 4. 可读性
- 代码结构清晰，一目了然
- 参数含义直接在代码中体现

### 5. 职责分离
- **MyServer**: 专注于应用启动
- **McpServerConfig**: 专注于MCP服务器配置
- **McpComponentRegistry**: 专注于组件自动注册

## 示例对比

### 传统方式 vs 注解方式

**传统方式 (CalculatorTool.java)**:
```java
// 需要手动创建Schema
private static String createSchema() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode rootNode = mapper.createObjectNode();
    // ... 大量样板代码
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
}

// 需要手动注册
syncServer.addTool(CalculatorTool.createTool());
```

**注解方式 (AnnotatedCalculatorTool.java)**:
```java
@McpParameter(name = "operation", description = "计算操作类型", type = "string", required = true)
private String operation;

@McpParameter(name = "a", description = "第一个操作数", type = "number", required = true)
private Double a;

// 自动生成Schema
private String generateSchema() throws JsonProcessingException {
    return SchemaGenerator.generateSchema(this.getClass(), "urn:jsonschema:CalculatorOperation");
}

// 自动注册 - 无需手动调用
```

## 注意事项

1. **Spring组件**: 所有使用注解的类必须是Spring组件（使用`@Component`等注解）
2. **方法签名**: `createTool()`、`createResource()`、`createPrompt()`方法必须存在且返回正确的类型
3. **参数注解**: `@McpParameter`注解的字段必须是私有的
4. **Schema生成**: 使用`SchemaGenerator.generateSchema()`方法自动生成JSON Schema
5. **配置分离**: MCP服务器配置在`McpServerConfig`中，应用启动在`MyServer`中

## 扩展性

框架设计为可扩展的，可以轻松添加新的注解类型或修改现有注解的行为：

- 添加新的参数类型支持
- 扩展注解属性
- 自定义Schema生成逻辑
- 添加验证规则

这个注解框架大大简化了MCP组件的开发，让开发者可以专注于业务逻辑而不是样板代码，同时通过职责分离提高了代码的可维护性。 