package io.zhijian.tools.mcp.client;

import io.zhijian.tools.mcp.config.DeepSeekConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 简单的DeepSeek HTTP客户端，不依赖Spring Boot
 */
public class DeepSeekHttpClient implements LLMClient {
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public DeepSeekHttpClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(60 * 2))
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public String getClientName() {
        return "DeepSeek";
    }
    
    /**
     * 发送聊天请求到DeepSeek API
     * @param messages 消息列表，格式：[{"role": "user", "content": "你好"}]
     * @return API响应
     */
    public String chat(List<Map<String, String>> messages) throws IOException, InterruptedException {
        // 构建请求体
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", DeepSeekConfig.MODEL);
        requestBody.put("temperature", DeepSeekConfig.TEMPERATURE);
        requestBody.put("max_tokens", DeepSeekConfig.MAX_TOKENS);
        requestBody.put("top_p", DeepSeekConfig.TOP_P);
        requestBody.put("top_k", DeepSeekConfig.TOP_K);
        requestBody.put("frequency_penalty", DeepSeekConfig.FREQUENCY_PENALTY);
        
        // 添加消息
        ArrayNode messagesArray = objectMapper.createArrayNode();
        for (Map<String, String> message : messages) {
            ObjectNode messageNode = objectMapper.createObjectNode();
            messageNode.put("role", message.get("role"));
            messageNode.put("content", message.get("content"));
            messagesArray.add(messageNode);
        }
        requestBody.set("messages", messagesArray);
        
        // 创建HTTP请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DeepSeekConfig.getFullUrl()))
                .header("Authorization", "Bearer " + DeepSeekConfig.CREDENTIAL)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofMinutes(1))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();
        
        // 发送请求
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new RuntimeException("HTTP请求失败，状态码: " + response.statusCode() + "，响应: " + response.body());
            }
            
            return response.body();
        } catch (java.net.http.HttpTimeoutException e) {
            throw new RuntimeException("DeepSeek请求超时: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("DeepSeek请求超时网络请求异常: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("DeepSeek请求超时请求被中断: " + e.getMessage(), e);
        }
    }
    
    /**
     * 简单的聊天方法，接收单条用户消息
     * @param userMessage 用户消息
     * @return API响应
     */
    public String chat(String userMessage) throws IOException, InterruptedException {
        return chat(List.of(Map.of("role", "user", "content", userMessage)));
    }
    
    /**
     * 从API响应中提取助手回复内容
     * @param apiResponse API完整响应
     * @return 助手回复的文本内容
     */
    public String extractAssistantMessage(String apiResponse) throws IOException {
        ObjectNode responseNode = (ObjectNode) objectMapper.readTree(apiResponse);
        ArrayNode choices = (ArrayNode) responseNode.get("choices");
        if (choices != null && choices.size() > 0) {
            ObjectNode firstChoice = (ObjectNode) choices.get(0);
            ObjectNode message = (ObjectNode) firstChoice.get("message");
            if (message != null) {
                return message.get("content").asText();
            }
        }
        return "无法解析响应";
    }
} 