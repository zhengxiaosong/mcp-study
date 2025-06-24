package io.zhijian.tools.mcp.client;

import java.util.List;
import java.util.Map;

/**
 * LLM客户端接口
 */
public interface LLMClient {
    
    /**
     * 发送聊天请求
     * @param messages 消息列表
     * @return API响应
     */
    String chat(List<Map<String, String>> messages) throws Exception;
    
    /**
     * 发送单条消息
     * @param userMessage 用户消息
     * @return API响应
     */
    String chat(String userMessage) throws Exception;
    
    /**
     * 从API响应中提取助手回复内容
     * @param apiResponse API完整响应
     * @return 助手回复的文本内容
     */
    String extractAssistantMessage(String apiResponse) throws Exception;
    
    /**
     * 获取客户端名称
     * @return 客户端名称
     */
    String getClientName();
} 