package io.zhijian.tools.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.logging.LoggingMessageNotification;
import io.modelcontextprotocol.spec.logging.LoggingLevel;
import io.modelcontextprotocol.spec.initialization.ServerCapabilities;
import io.zhijian.tools.mcp.resources.MemoryResource;
import io.zhijian.tools.mcp.resources.MemoryResourceManager;
import io.zhijian.tools.mcp.tools.CalculatorTool;
import io.zhijian.tools.mcp.tools.GreetingTool;

/**
 * MCP服务器主类
 */
public class MyServer {
    /**
     * 启动MCP服务器
     * @param args 命令行参数
     */
    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        System.out.println("Starting MCP Server...");
        
        // 初始化服务器
        StdioServerTransportProvider stdioServerTransportProvider =
                new StdioServerTransportProvider(new ObjectMapper());
        McpSyncServer syncServer = McpServer.sync(stdioServerTransportProvider)
                .serverInfo("my-server", "1.0.0")
                .capabilities(ServerCapabilities.builder()
                        .resources(true, true)
                        .tools(true)
                        .prompts(true)
                        .logging()
                        .build())
                .build();

        try {
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
}
