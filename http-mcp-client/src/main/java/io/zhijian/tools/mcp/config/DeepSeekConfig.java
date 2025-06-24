package io.zhijian.tools.mcp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * DeepSeek配置类
 */
public class DeepSeekConfig {
    
    private static final Properties config = loadConfig();
    
    // API配置
    public static final String API_URL = config.getProperty("deepseek.api.url", "https://api.deepseek.com/v1/chat/completions");
    public static final String CREDENTIAL = config.getProperty("deepseek.api.key", "your_deepseek_api_key_here");
    public static final String MODEL = config.getProperty("deepseek.model", "deepseek-chat");
    
    // 模型参数
    public static final double TEMPERATURE = Double.parseDouble(config.getProperty("deepseek.temperature", "0.1"));
    public static final int MAX_TOKENS = Integer.parseInt(config.getProperty("deepseek.max_tokens", "4000"));
    public static final double TOP_P = Double.parseDouble(config.getProperty("deepseek.top_p", "0.95"));
    public static final int TOP_K = Integer.parseInt(config.getProperty("deepseek.top_k", "50"));
    public static final double FREQUENCY_PENALTY = Double.parseDouble(config.getProperty("deepseek.frequency_penalty", "0.0"));
    
    private static Properties loadConfig() {
        Properties properties = new Properties();
        try (InputStream input = DeepSeekConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("无法加载DeepSeek配置文件，使用默认配置: " + e.getMessage());
        }
        return properties;
    }
    
    public static String getFullUrl() {
        return API_URL;
    }
} 