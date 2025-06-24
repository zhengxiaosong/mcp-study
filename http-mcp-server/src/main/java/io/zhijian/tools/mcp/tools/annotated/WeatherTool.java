package io.zhijian.tools.mcp.tools.annotated;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.tool.Tool;
import io.modelcontextprotocol.spec.content.Content;
import io.modelcontextprotocol.spec.content.TextContent;
import io.modelcontextprotocol.spec.tool.CallToolResult;
import io.zhijian.tools.mcp.annotations.McpTool;
import io.zhijian.tools.mcp.annotations.McpParameter;
import io.zhijian.tools.mcp.utils.SchemaGenerator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 天气工具示例
 * 展示如何使用注解框架创建简单的工具
 */
@Component
@McpTool(
    name = "weather",
    description = "Get weather information for a location",
    version = "1.0.0"
)
public class WeatherTool {

    @McpParameter(
        name = "location",
        description = "城市或地区名称",
        type = "string",
        required = true
    )
    private String location;

    @McpParameter(
        name = "unit",
        description = "温度单位",
        type = "string",
        required = false,
        defaultValue = "celsius",
        enumValues = {"celsius", "fahrenheit"}
    )
    private String unit;

    private final Random random = new Random();

    /**
     * 创建工具规范
     */
    public McpServerFeatures.SyncToolSpecification createTool() throws JsonProcessingException {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool("weather", "Get weather information", generateSchema()),
                this::execute);
    }

    /**
     * 执行天气查询逻辑
     */
    private CallToolResult execute(Object exchange, java.util.Map<String, Object> context) {
        List<Content> result = new ArrayList<>();
        try {
            String location = context.get("location").toString();
            String unit = context.getOrDefault("unit", "celsius").toString();
            
            // 模拟天气数据
            int temperature = random.nextInt(30) + 5; // 5-35度
            String[] conditions = {"晴天", "多云", "小雨", "阴天", "雾霾"};
            String condition = conditions[random.nextInt(conditions.length)];
            
            // 转换温度单位
            if ("fahrenheit".equals(unit)) {
                temperature = (int) (temperature * 9.0 / 5.0 + 32);
            }
            
            String weatherInfo = String.format(
                "📍 %s 的天气信息：\n" +
                "🌡️ 温度：%d°%s\n" +
                "☁️ 天气：%s\n" +
                "💨 湿度：%d%%\n" +
                "🌬️ 风速：%d km/h",
                location, 
                temperature, 
                "fahrenheit".equals(unit) ? "F" : "C",
                condition,
                random.nextInt(40) + 40, // 40-80%
                random.nextInt(20) + 5   // 5-25 km/h
            );
            
            result.add(new TextContent(weatherInfo));
        }
        catch (Exception e) {
            result.add(new TextContent("Error: " + e.getMessage()));
            return new CallToolResult(result, true);
        }
        return new CallToolResult(result, true);
    }

    /**
     * 生成JSON Schema
     */
    private String generateSchema() throws JsonProcessingException {
        return SchemaGenerator.generateSchema(this.getClass(), "urn:jsonschema:Weather");
    }
} 