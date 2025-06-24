package io.zhijian.tools.mcp.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.spec.logging.LoggingMessageNotification;
import io.modelcontextprotocol.spec.logging.LoggingLevel;
import io.modelcontextprotocol.spec.initialization.ServerCapabilities;
import io.zhijian.tools.mcp.resources.MemoryResourceManager;

/**
 * MCP 服务器配置类
 * 负责配置 HTTP SSE 传输、Servlet 注册和 MCP 服务器创建
 * 
 * 注意：移除了@EnableWebMvc，因为Spring Boot的自动配置已经提供了Web功能
 */
@Configuration
public class McpServerConfig implements WebMvcConfigurer {

    @Bean
    public HttpServletSseServerTransportProvider servletSseServerTransportProvider() {
        return new HttpServletSseServerTransportProvider(new ObjectMapper(), "/sse");
    }

    @Bean
    public ServletRegistrationBean<HttpServletSseServerTransportProvider> sseServlet(
            HttpServletSseServerTransportProvider transportProvider) {
        ServletRegistrationBean<HttpServletSseServerTransportProvider> bean = 
            new ServletRegistrationBean<>(transportProvider, "/sse/*");
        bean.setLoadOnStartup(1);
        return bean;
    }

    /**
     * 创建McpSyncServer Bean
     * 负责MCP服务器的初始化和配置
     */
    @SuppressWarnings("deprecation")
    @Bean
    public McpSyncServer mcpSyncServer(HttpServletSseServerTransportProvider transportProvider) {
        System.setProperty("file.encoding", "UTF-8");
        System.out.println("Creating MCP Server...");
        
        try {
            // 初始化服务器
            McpSyncServer syncServer = McpServer.sync(transportProvider)
                    .serverInfo("my-server", "1.0.0")
                    .capabilities(ServerCapabilities.builder()
                            .resources(true, true)
                            .tools(true)
                            .prompts(true)
                            .logging()
                            .build())
                    .build();

            // 初始化内存资源
            System.out.println("Initializing memory resource...");
            MemoryResourceManager.getInstance().setResource("memory://resource", "Initial resource value");
            
            System.out.println("MCP Server created successfully.");
            syncServer.loggingNotification(LoggingMessageNotification.builder()
                            .level(LoggingLevel.INFO)
                            .logger("custom-logger")
                            .data("MCP Server created with annotation-based components.")
                    .build());

            return syncServer;

        }
        catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create MCP Server", e);
        }
    }
} 