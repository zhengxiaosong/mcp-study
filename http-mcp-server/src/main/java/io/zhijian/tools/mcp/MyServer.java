package io.zhijian.tools.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.spec.logging.LoggingMessageNotification;
import io.modelcontextprotocol.spec.logging.LoggingLevel;
import io.modelcontextprotocol.spec.initialization.ServerCapabilities;
import io.zhijian.tools.mcp.prompts.ToolResultAnalysisPrompt;
import io.zhijian.tools.mcp.resources.MemoryResource;
import io.zhijian.tools.mcp.resources.MemoryResourceManager;
import io.zhijian.tools.mcp.tools.CalculatorTool;
import io.zhijian.tools.mcp.tools.GreetingTool;
import javax.annotation.PostConstruct;

/**
 * MCP服务器主类
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
public class MyServer {

    @Autowired
    private HttpServletSseServerTransportProvider transportProvider;
    
    private McpSyncServer syncServer;

    /**
     * 启动MCP服务器
     */
    @SuppressWarnings("deprecation")
    @PostConstruct
    public void startServer() {
        System.setProperty("file.encoding", "UTF-8");
        System.out.println("Starting MCP Server...");
        
        try {
            // 初始化服务器
            syncServer = McpServer.sync(transportProvider)
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
            
            // 添加工具和资源
            System.out.println("Adding calculator tool...");
            syncServer.addTool(CalculatorTool.createTool());
            System.out.println("Adding memory resource...");
            syncServer.addResource(MemoryResource.createMemoryResource());
            System.out.println("Adding greeting tool...");
            syncServer.addTool(GreetingTool.createTool());
            System.out.println("Adding tool result prompt...");
            syncServer.addPrompt(ToolResultAnalysisPrompt.createPrompt());

            System.out.println("Server initialization completed.");
            syncServer.loggingNotification(LoggingMessageNotification.builder()
                            .level(LoggingLevel.INFO)
                            .logger("custom-logger")
                            .data("Server initialized successfully.")
                    .build());

        }
        catch (JsonProcessingException e) {
            System.err.println("Error creating tool schema: " + e.getMessage());
            e.printStackTrace();
        }
        catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 启动Web应用
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(MyServer.class, args);
    }
}