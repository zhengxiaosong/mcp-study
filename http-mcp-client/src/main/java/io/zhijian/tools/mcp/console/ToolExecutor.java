package io.zhijian.tools.mcp.console;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.content.Content;
import io.modelcontextprotocol.spec.content.TextContent;
import io.modelcontextprotocol.spec.tool.CallToolRequest;
import io.modelcontextprotocol.spec.tool.CallToolResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具执行器
 * 负责解析工具调用请求、执行MCP工具、处理结果
 */
public class ToolExecutor {
    
    private final McpSyncClient mcpClient;
    private final ConsoleUI ui;
    
    public ToolExecutor(McpSyncClient mcpClient, ConsoleUI ui) {
        this.mcpClient = mcpClient;
        this.ui = ui;
        new ObjectMapper();
    }
    
    /**
     * 检查消息是否包含工具调用
     */
    public boolean containsToolCall(String message) {
        return message.contains("【工具调用】") && message.contains("【工具调用结束】");
    }
    
    /**
     * 处理工具调用并返回结果
     */
    public String processToolCalls(String message) throws Exception {
        Pattern pattern = Pattern.compile("【工具调用】\\s*工具名称:\\s*([^\\n]+)\\s*参数:\\s*([^【]+)【工具调用结束】");
        Matcher matcher = pattern.matcher(message);
        
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;
        int toolCallCount = 0;
        
        while (matcher.find()) {
            toolCallCount++;
            result.append(message, lastEnd, matcher.start());
            
            String toolName = matcher.group(1).trim();
            String paramsJson = matcher.group(2).trim();
            
            try {
                ui.logInfo("执行第 " + toolCallCount + " 个工具调用: " + toolName);
                String toolResult = executeSingleTool(toolName, paramsJson);
                result.append(toolResult);
            } catch (Exception e) {
                ui.logError("工具调用失败: " + e.getMessage());
                result.append("【工具调用失败】").append(e.getMessage()).append("【错误结束】");
            }
            
            lastEnd = matcher.end();
        }
        
        result.append(message.substring(lastEnd));
        
        if (toolCallCount > 0) {
            ui.logSuccess("完成 " + toolCallCount + " 个工具调用");
        }
        
        return result.toString();
    }
    
    private String executeSingleTool(String toolName, String paramsJson) throws Exception {
        ObjectNode params = (ObjectNode) new ObjectMapper().readTree(paramsJson);
        Map<String, Object> paramMap = new ObjectMapper().convertValue(params, 
            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        
        ui.logTool("正在调用工具: " + toolName + "，参数: " + paramsJson);
        CallToolResult toolResult = mcpClient.callTool(new CallToolRequest(toolName, paramMap));
        String toolOutput = extractToolResult(toolResult);
        
        return "【工具执行结果】" + toolOutput + "【结果结束】";
    }
    
    public String extractToolResult(CallToolResult result) {
        if (result.getContent() != null && !result.getContent().isEmpty()) {
            Content content = result.getContent().get(0);
            if (content instanceof TextContent) {
                return ((TextContent) content).getText();
            }
        }
        return "无结果";
    }
    
    public String extractToolNameFromMessage(String message) {
        Pattern pattern = Pattern.compile("【工具调用】\\s*工具名称:\\s*([^\\n]+)");
        Matcher matcher = pattern.matcher(message);
        return matcher.find() ? matcher.group(1).trim() : "未知工具";
    }
    
    public String extractToolResultFromMessage(String message) {
        Pattern pattern = Pattern.compile("【工具执行结果】([^【]+)【结果结束】");
        Matcher matcher = pattern.matcher(message);
        return matcher.find() ? matcher.group(1).trim() : "无结果";
    }
}