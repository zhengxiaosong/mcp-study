package io.zhijian.tools.mcp.client;

import io.zhijian.tools.mcp.console.ConsoleUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * LLM客户端管理器
 * 负责管理多个LLM客户端，并在一个客户端失败时自动切换到另一个
 */
public class LLMClientManager implements LLMClient {
    
    private final List<LLMClient> clients;
    private int currentClientIndex = 0;
    private ConsoleUI ui;
    
    public LLMClientManager() {
        this.clients = new ArrayList<>();
        // 添加可用的LLM客户端
        this.clients.add(new DeepSeekHttpClient());
        this.clients.add(new AzureOpenAIClient());
    }
    
    public LLMClientManager(ConsoleUI ui) {
        this();
        this.ui = ui;
    }
    
    @Override
    public String getClientName() {
        return "LLM Client Manager";
    }
    
    /**
     * 发送聊天请求，如果当前客户端失败则自动切换到下一个
     */
    @Override
    public String chat(List<Map<String, String>> messages) throws Exception {
        Exception lastException = null;
        
        // 尝试所有可用的客户端
        for (int attempt = 0; attempt < clients.size(); attempt++) {
            LLMClient currentClient = clients.get(currentClientIndex);
            
            try {
                if (ui != null) {
                    ui.logLLM("尝试使用 " + currentClient.getClientName() + " 发送请求...");
                } else {
                    System.out.println("尝试使用 " + currentClient.getClientName() + " 发送请求...");
                }
                return currentClient.chat(messages);
            } catch (Exception e) {
                lastException = e;
                String errorMsg = currentClient.getClientName() + " 请求失败: " + e.getMessage();
                
                if (ui != null) {
                    ui.logError(errorMsg);
                    // 如果是配置错误，给出更详细的提示
                    if (e.getMessage().contains("Authentication") || e.getMessage().contains("api key")) {
                        ui.logWarning("请检查 " + currentClient.getClientName() + " 的API密钥配置");
                    } else if (e.getMessage().contains("max_tokens")) {
                        ui.logWarning("请检查 " + currentClient.getClientName() + " 的max_tokens参数配置");
                    }
                } else {
                    System.err.println(errorMsg);
                }
                
                // 切换到下一个客户端
                currentClientIndex = (currentClientIndex + 1) % clients.size();
                
                if (attempt < clients.size() - 1) {
                    if (ui != null) {
                        ui.logLLM("切换到 " + clients.get(currentClientIndex).getClientName());
                    } else {
                        System.out.println("切换到 " + clients.get(currentClientIndex).getClientName());
                    }
                }
            }
        }
        
        // 所有客户端都失败了
        String finalError = "所有LLM客户端都失败了。请检查：\n" +
                "1. API密钥是否正确配置\n" +
                "2. 网络连接是否正常\n" +
                "3. 模型参数是否合理";
        
        if (ui != null) {
            ui.logError(finalError);
        }
        
        throw new RuntimeException(finalError, lastException);
    }
    
    /**
     * 发送单条消息
     */
    @Override
    public String chat(String userMessage) throws Exception {
        return chat(List.of(Map.of("role", "user", "content", userMessage)));
    }
    
    /**
     * 从API响应中提取助手回复内容
     */
    @Override
    public String extractAssistantMessage(String apiResponse) throws Exception {
        // 使用当前活跃的客户端来解析响应
        LLMClient currentClient = clients.get(currentClientIndex);
        return currentClient.extractAssistantMessage(apiResponse);
    }
    
    /**
     * 获取当前活跃的客户端
     */
    public LLMClient getCurrentClient() {
        return clients.get(currentClientIndex);
    }
    
    /**
     * 获取所有可用的客户端
     */
    public List<LLMClient> getAllClients() {
        return new ArrayList<>(clients);
    }
    
    /**
     * 手动切换到指定的客户端
     */
    public void switchToClient(int index) {
        if (index >= 0 && index < clients.size()) {
            currentClientIndex = index;
            System.out.println("手动切换到 " + clients.get(currentClientIndex).getClientName());
        }
    }
} 