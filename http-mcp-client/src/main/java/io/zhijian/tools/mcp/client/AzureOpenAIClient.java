package io.zhijian.tools.mcp.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Azure OpenAI HTTP客户端
 */
public class AzureOpenAIClient implements LLMClient {
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    // Azure OpenAI配置
    private final String endpoint;
    private final String credential;
    private final String engine;
    private final String apiVersion;
    private final Double temperature;
    private final Integer maxTokens;
    private final Double topP;
    
    public AzureOpenAIClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(60))
                .build();
        this.objectMapper = new ObjectMapper();
        
        // 从配置文件读取配置
        Properties config = loadConfig();
        this.endpoint = config.getProperty("azure.openai.endpoint", "https://techsun-daiguide-openai-eu2.openai.azure.com");
        this.credential = config.getProperty("azure.openai.credential");
        this.engine = config.getProperty("azure.openai.engine", "gpt-4o");
        this.apiVersion = config.getProperty("azure.openai.api_version", "2024-02-15-preview");
        this.temperature = Double.parseDouble(config.getProperty("azure.openai.temperature", "0.01"));
        this.maxTokens = Integer.parseInt(config.getProperty("azure.openai.max_tokens", "64000"));
        this.topP = Double.parseDouble(config.getProperty("azure.openai.top_p", "0.95"));
    }
    
    private Properties loadConfig() {
        Properties config = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                config.load(input);
            }
        } catch (IOException e) {
            System.err.println("无法加载配置文件，使用默认配置: " + e.getMessage());
        }
        return config;
    }
    
    @Override
    public String getClientName() {
        return "Azure OpenAI";
    }
    
    /**
     * 发送聊天请求到Azure OpenAI API
     */
    @Override
    public String chat(List<Map<String, String>> messages) throws IOException, InterruptedException {
        // 构建请求体
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("top_p", topP);
        
        // 添加消息
        ArrayNode messagesArray = objectMapper.createArrayNode();
        for (Map<String, String> message : messages) {
            ObjectNode messageNode = objectMapper.createObjectNode();
            messageNode.put("role", message.get("role"));
            messageNode.put("content", message.get("content"));
            messagesArray.add(messageNode);
        }
        requestBody.set("messages", messagesArray);
        
        // 构建URL
        String url = endpoint + "/openai/deployments/" + engine + "/chat/completions?api-version=" + apiVersion;
        
        // 创建HTTP请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("api-key", credential)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofMinutes(1))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();
        
        // 发送请求
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                String errorDetail = "Azure OpenAI请求失败，状态码: " + response.statusCode() + "，响应: " + response.body();
                
                // 根据状态码提供更具体的错误信息
                if (response.statusCode() == 401) {
                    errorDetail += "\n可能原因：API密钥无效或已过期";
                } else if (response.statusCode() == 400) {
                    errorDetail += "\n可能原因：请求参数错误，请检查模型名称、API版本等配置";
                } else if (response.statusCode() == 429) {
                    errorDetail += "\n可能原因：请求频率过高，请稍后重试";
                } else if (response.statusCode() == 503) {
                    errorDetail += "\n可能原因：服务暂时不可用，请稍后重试";
                }
                
                throw new RuntimeException(errorDetail);
            }
            
            return response.body();
        } catch (java.net.http.HttpTimeoutException e) {
            throw new RuntimeException("Azure OpenAI请求超时: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Azure OpenAI网络请求异常: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Azure OpenAI请求被中断: " + e.getMessage(), e);
        }
    }
    
    /**
     * 简单的聊天方法，接收单条用户消息
     */
    @Override
    public String chat(String userMessage) throws IOException, InterruptedException {
        return chat(List.of(Map.of("role", "user", "content", userMessage)));
    }
    
    /**
     * 从API响应中提取助手回复内容
     */
    @Override
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