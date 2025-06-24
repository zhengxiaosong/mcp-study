package io.zhijian.tools.mcp.console;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.tool.ListToolsResult;
import io.modelcontextprotocol.spec.tool.Tool;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.initialization.ClientCapabilities;
import io.modelcontextprotocol.spec.initialization.InitializeResult;
import io.zhijian.tools.mcp.client.ConfigValidator;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * MCP Host 控制台程序 - 重构后的简化版本
 * 主要负责程序启动、用户交互循环和资源管理
 */
public class McpLlmConsole {
    
    private static final String MCP_SERVER_URL = "http://localhost:8080/sse";
    
    private final McpSyncClient mcpClient;
    private final List<Tool> availableTools;
    private final ConsoleUI ui;
    private McpHostAgent agent;
    
    public McpLlmConsole() throws Exception {
        this.ui = new ConsoleUI();
        this.availableTools = new ArrayList<>();
        
        // 验证LLM配置
        ConfigValidator.validateConfig();
        
        // 初始化MCP客户端
        ui.logInfo("🔌 正在连接MCP服务器...");
        McpClientTransport transport = HttpClientSseClientTransport.builder(MCP_SERVER_URL)
                .build();
        
        this.mcpClient = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(30))
                .capabilities(ClientCapabilities.builder()
                        .roots(false)
                        .sampling()
                        .build())
                .build();
        
        // 初始化连接并获取可用工具
        InitializeResult initResult = mcpClient.initialize();
        ui.logSuccess("✅ MCP服务器连接成功: " + initResult.getServerInfo().getName() + 
                   " v" + initResult.getServerInfo().getVersion());
        
        // 获取可用工具列表
        ListToolsResult toolsResult = mcpClient.listTools();
        availableTools.addAll(toolsResult.getTools());
        ui.logInfo("🔧 发现可用工具: " + availableTools.size() + " 个");
        
        // 初始化Agent
        this.agent = new McpHostAgent(mcpClient, availableTools, ui);
    }
    
    /**
     * 启动控制台交互
     */
    public void start() {
        try (Scanner scanner = new Scanner(System.in)) {
            ui.showWelcome();
            
            while (true) {
                ui.showUserPrompt();
                String userInput = scanner.nextLine().trim();
                
                if (userInput.equalsIgnoreCase("exit") || userInput.equalsIgnoreCase("quit")) {
                    break;
                }
                
                if (userInput.equalsIgnoreCase("tools")) {
                    ui.showAvailableTools(availableTools);
                    continue;
                }
                
                if (userInput.equalsIgnoreCase("clear")) {
                    agent.clearHistory(availableTools);
                    continue;
                }
                
                if (userInput.equalsIgnoreCase("prompts")) {
                    agent.getPromptProcessor().showAvailablePrompts();
                    continue;
                }
                
                if (userInput.isEmpty()) {
                    continue;
                }
                
                try {
                    agent.handleUserInput(userInput);
                } catch (Exception e) {
                    ui.logError("处理请求时发生错误: " + e.getMessage());
                    break;
                }
            }
        }
        
        cleanup();
        ui.showGoodbye();
    }
    
    /**
     * 清理资源
     */
    private void cleanup() {
        try {
            ui.logInfo("正在关闭 MCP 连接...");
            mcpClient.closeGracefully();
            ui.logSuccess("MCP 连接已关闭");
        } catch (Exception e) {
            ui.logError("关闭MCP客户端时发生错误: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        try {
            McpLlmConsole console = new McpLlmConsole();
            console.start();
        } catch (Exception e) {
            ConsoleUI ui = new ConsoleUI();
            ui.fatalError("启动控制台失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}