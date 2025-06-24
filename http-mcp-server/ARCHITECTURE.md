# MCP 注解框架架构设计

## 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                    MCP 注解框架架构                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────┐    ┌─────────────────┐                │
│  │   MyServer      │    │ McpServerConfig │                │
│  │   (应用启动)     │    │  (服务器配置)    │                │
│  └─────────────────┘    └─────────────────┘                │
│           │                       │                        │
│           └───────────────────────┼────────────────────────┘
│                                   │                        │
│  ┌─────────────────────────────────┼────────────────────┐  │
│  │                                 │                    │  │
│  │  ┌─────────────────────────────┐ │ ┌────────────────┐ │  │
│  │  │   McpComponentRegistry      │ │ │  HttpServlet   │ │  │
│  │  │   (组件自动注册器)           │ │ │  SSE Transport │ │  │
│  │  └─────────────────────────────┘ │ └────────────────┘ │  │
│  │                                 │                    │  │
│  │  ┌─────────────────────────────┐ │ ┌────────────────┐ │  │
│  │  │   SchemaGenerator           │ │ │  McpSyncServer │ │  │
│  │  │   (Schema生成器)             │ │ │  (MCP服务器)   │ │  │
│  │  └─────────────────────────────┘ │ └────────────────┘ │  │
│  │                                 │                    │  │
│  └─────────────────────────────────┼────────────────────┘  │
│                                   │                        │
│  ┌─────────────────────────────────┼────────────────────┐  │
│  │                                 │                    │  │
│  │  ┌─────────────────────────────┐ │ ┌────────────────┐ │  │
│  │  │   @McpTool                  │ │ │  @McpResource  │ │  │
│  │  │   (工具注解)                 │ │ │  (资源注解)    │ │  │
│  │  └─────────────────────────────┘ │ └────────────────┘ │  │
│  │                                 │                    │  │
│  │  ┌─────────────────────────────┐ │ ┌────────────────┐ │  │
│  │  │   @McpPrompt                │ │ │  @McpParameter │ │  │
│  │  │   (提示注解)                 │ │ │  (参数注解)    │ │  │
│  │  └─────────────────────────────┘ │ └────────────────┘ │  │
│  │                                 │                    │  │
│  └─────────────────────────────────┼────────────────────┘  │
│                                   │                        │
│  ┌─────────────────────────────────┼────────────────────┐  │
│  │                                 │                    │  │
│  │  ┌─────────────────────────────┐ │ ┌────────────────┐ │  │
│  │  │   AnnotatedCalculatorTool   │ │ │ WeatherTool    │ │  │
│  │  │   (注解版计算器)             │ │ │ (天气工具)     │ │  │
│  │  └─────────────────────────────┘ │ └────────────────┘ │  │
│  │                                 │                    │  │
│  │  ┌─────────────────────────────┐ │ ┌────────────────┐ │  │
│  │  │   AnnotatedGreetingTool     │ │ │ MemoryResource │ │  │
│  │  │   (注解版问候工具)           │ │ │ (内存资源)     │ │  │
│  │  └─────────────────────────────┘ │ └────────────────┘ │  │
│  │                                 │                    │  │
│  └─────────────────────────────────┼────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## 职责分工

### 1. MyServer.java - 应用启动类
- **职责**: 只负责Spring Boot应用的启动
- **特点**: 简洁明了，专注于应用入口
- **代码量**: 最小化，只包含必要的启动逻辑

```java
@SpringBootApplication
public class MyServer {
    public static void main(String[] args) {
        System.out.println("Starting MCP Application...");
        SpringApplication.run(MyServer.class, args);
        System.out.println("MCP Application started successfully!");
    }
}
```

### 2. McpServerConfig.java - MCP服务器配置类
- **职责**: 
  - 创建和配置MCP服务器
  - 管理HTTP SSE传输
  - 注册Servlet
  - 初始化内存资源
- **特点**: 集中管理所有MCP相关的配置
- **优化**: 移除了`@EnableWebMvc`，使用Spring Boot的自动配置

```java
@Configuration
// 移除 @EnableWebMvc，使用 Spring Boot 的自动配置
public class McpServerConfig implements WebMvcConfigurer {
    
    @Bean
    public McpSyncServer mcpSyncServer(HttpServletSseServerTransportProvider transportProvider) {
        // 创建MCP服务器
        // 配置服务器能力
        // 初始化资源
        return syncServer;
    }
    
    @Bean
    public HttpServletSseServerTransportProvider servletSseServerTransportProvider() {
        // 配置HTTP SSE传输
    }
    
    @Bean
    public ServletRegistrationBean<HttpServletSseServerTransportProvider> sseServlet() {
        // 注册Servlet
    }
}
```

### 3. McpComponentRegistry.java - 组件注册器
- **职责**: 自动发现和注册带有注解的组件
- **特点**: 在应用启动时自动执行，无需手动干预

```java
@Component
public class McpComponentRegistry {
    
    @PostConstruct
    public void registerComponents() {
        // 扫描所有带有@McpTool注解的组件
        // 扫描所有带有@McpResource注解的组件
        // 扫描所有带有@McpPrompt注解的组件
        // 自动注册到MCP服务器
    }
}
```

## 启动流程

```
1. MyServer.main() 启动应用
   ↓
2. SpringApplication.run() 初始化Spring容器
   ↓
3. McpServerConfig.mcpSyncServer() 创建MCP服务器
   ↓
4. McpComponentRegistry.registerComponents() 自动注册组件
   ↓
5. 应用启动完成，可以接收请求
```

## 注解框架

### 核心注解

1. **@McpTool** - 标记工具类
2. **@McpResource** - 标记资源类
3. **@McpPrompt** - 标记提示类
4. **@McpParameter** - 定义参数

### 工具类示例

```java
@Component
@McpTool(name = "calculator", description = "Basic calculator")
public class CalculatorTool {
    
    @McpParameter(name = "operation", description = "计算操作", type = "string", required = true)
    private String operation;
    
    @McpParameter(name = "a", description = "第一个操作数", type = "number", required = true)
    private Double a;
    
    public McpServerFeatures.SyncToolSpecification createTool() {
        // 自动生成Schema并创建工具规范
    }
}
```

## 优势

### 1. 职责分离
- **MyServer**: 专注于应用启动
- **McpServerConfig**: 专注于MCP服务器配置
- **McpComponentRegistry**: 专注于组件自动注册

### 2. 代码简化
- 传统方式需要大量样板代码
- 注解方式只需添加注解即可

### 3. 自动注册
- 无需手动注册组件
- 自动发现和注册带有注解的类

### 4. 类型安全
- 参数定义与Java字段绑定
- 编译时检查，避免运行时错误

### 5. 可维护性
- 配置集中管理
- 组件自动发现
- 代码结构清晰

### 6. 配置优化
- **移除 @EnableWebMvc**: 使用Spring Boot的自动配置
- **减少配置复杂度**: 遵循约定优于配置原则
- **保持功能完整**: 所有Web功能正常工作

## 配置优化说明

### @EnableWebMvc 的移除

我们移除了 `@EnableWebMvc` 注解，因为：

1. **Spring Boot 自动配置**: `spring-boot-starter-web` 已经提供了完整的Web功能
2. **简化配置**: 不需要复杂的MVC配置
3. **功能验证**: 移除后所有功能正常工作

### 验证测试

添加了测试控制器来验证Web功能：

```java
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/health")
    public String health() {
        return "MCP Server is running! Web functionality is working.";
    }
}
```

访问 `http://localhost:8080/test/health` 可以验证Web功能。

## 扩展性

框架设计为可扩展的：

1. **新注解类型**: 可以轻松添加新的注解类型
2. **自定义Schema生成**: 可以自定义Schema生成逻辑
3. **验证规则**: 可以添加参数验证规则
4. **配置选项**: 可以扩展配置选项

这种架构设计使得MCP组件的开发变得更加简单、高效和可维护，同时通过配置优化进一步简化了代码。 