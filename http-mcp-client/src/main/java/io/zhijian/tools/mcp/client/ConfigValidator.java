package io.zhijian.tools.mcp.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置验证工具
 */
public class ConfigValidator {
    
    private static final Properties config = loadConfig();
    
    public static void validateConfig() {
        System.out.println("🔍 验证LLM配置...");
        
        // 验证DeepSeek配置
        validateDeepSeekConfig();
        
        // 验证Azure OpenAI配置
        validateAzureOpenAIConfig();
        
        System.out.println("✅ 配置验证完成");
    }
    
    private static void validateDeepSeekConfig() {
        System.out.println("\n📋 DeepSeek配置:");
        String apiUrl = config.getProperty("deepseek.api.url", "未配置");
        String apiKey = config.getProperty("deepseek.api.key", "未配置");
        String model = config.getProperty("deepseek.model", "未配置");
        String maxTokens = config.getProperty("deepseek.max_tokens", "未配置");
        
        System.out.println("  API URL: " + maskSensitiveInfo(apiUrl));
        System.out.println("  API Key: " + maskSensitiveInfo(apiKey));
        System.out.println("  Model: " + model);
        System.out.println("  Max Tokens: " + maxTokens);
        
        if ("未配置".equals(apiKey) || apiKey.contains("your_")) {
            System.out.println("  ⚠️  警告: DeepSeek API密钥未正确配置");
        }
    }
    
    private static void validateAzureOpenAIConfig() {
        System.out.println("\n📋 Azure OpenAI配置:");
        String endpoint = config.getProperty("azure.openai.endpoint", "未配置");
        String credential = config.getProperty("azure.openai.credential", "未配置");
        String engine = config.getProperty("azure.openai.engine", "未配置");
        String apiVersion = config.getProperty("azure.openai.api_version", "未配置");
        String maxTokens = config.getProperty("azure.openai.max_tokens", "未配置");
        
        System.out.println("  Endpoint: " + maskSensitiveInfo(endpoint));
        System.out.println("  Credential: " + maskSensitiveInfo(credential));
        System.out.println("  Engine: " + engine);
        System.out.println("  API Version: " + apiVersion);
        System.out.println("  Max Tokens: " + maxTokens);
        
        // 验证max_tokens是否合理
        try {
            int tokens = Integer.parseInt(maxTokens);
            if (tokens > 16384) {
                System.out.println("  ⚠️  警告: max_tokens (" + tokens + ") 可能过大，建议不超过16384");
            }
        } catch (NumberFormatException e) {
            System.out.println("  ⚠️  警告: max_tokens配置无效");
        }
        
        if ("未配置".equals(credential) || credential.contains("your_")) {
            System.out.println("  ⚠️  警告: Azure OpenAI API密钥未正确配置");
        }
    }
    
    private static String maskSensitiveInfo(String value) {
        if (value == null || value.length() <= 8) {
            return value;
        }
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }
    
    private static Properties loadConfig() {
        Properties properties = new Properties();
        try (InputStream input = ConfigValidator.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("无法加载配置文件: " + e.getMessage());
        }
        return properties;
    }
} 