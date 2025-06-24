package io.zhijian.tools.mcp.console;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.content.TextContent;
import io.modelcontextprotocol.spec.prompt.GetPromptRequest;
import io.modelcontextprotocol.spec.prompt.GetPromptResult;
import io.modelcontextprotocol.spec.prompt.ListPromptsResult;
import io.modelcontextprotocol.spec.prompt.PromptMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Prompt 处理器
 * 负责MCP Prompt的调用和处理逻辑
 */
public class PromptProcessor {
    
    private final McpSyncClient mcpClient;
    private final ConsoleUI ui;
    
    public PromptProcessor(McpSyncClient mcpClient, ConsoleUI ui) {
        this.mcpClient = mcpClient;
        this.ui = ui;
    }
    
    /**
     * 使用MCP Prompt指导LLM分析工具结果
     */
    public String processWithMCPPrompt(String messageWithToolResults, ToolExecutor toolExecutor, 
                                      String lastUserInput, LLMClient llmClient) throws Exception {
        String toolName = toolExecutor.extractToolNameFromMessage(messageWithToolResults);
        String toolResult = toolExecutor.extractToolResultFromMessage(messageWithToolResults);
        
        Map<String, Object> promptArgs = new HashMap<>();
        promptArgs.put("tool_name", toolName);
        promptArgs.put("tool_result", toolResult);
        promptArgs.put("user_context", lastUserInput);
        
        ui.logInfo("正在使用MCP Prompt指导LLM分析结果...");
        GetPromptRequest request = new GetPromptRequest("analyze_tool_result", promptArgs);
        GetPromptResult promptResult = mcpClient.getPrompt(request);
        
        if (promptResult.getMessages() != null && !promptResult.getMessages().isEmpty()) {
            PromptMessage promptMessage = promptResult.getMessages().get(0);
            if (promptMessage.getContent() instanceof TextContent) {
                String analysisPrompt = ((TextContent) promptMessage.getContent()).getText();
                
                List<Map<String, String>> analysisHistory = new ArrayList<>();
                analysisHistory.add(Map.of("role", "user", "content", analysisPrompt));
                
                String llmResponse = llmClient.chat(analysisHistory);
                return llmClient.extractAssistantMessage(llmResponse);
            }
        }
        
        throw new Exception("MCP Prompt返回格式异常");
    }
    
    /**
     * 显示可用的 Prompt 模板
     */
    public void showAvailablePrompts() {
        try {
            ListPromptsResult promptsResult = mcpClient.listPrompts();
            ui.showAvailablePrompts(promptsResult.getPrompts());
        } catch (Exception e) {
            ui.logError("获取 Prompt 列表失败: " + e.getMessage());
        }
    }
    
    // 用于LLM客户端的接口
    public interface LLMClient {
        String chat(List<Map<String, String>> history) throws Exception;
        String extractAssistantMessage(String response);
    }
}