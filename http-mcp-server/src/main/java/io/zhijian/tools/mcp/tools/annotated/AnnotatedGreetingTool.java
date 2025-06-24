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

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 注解版问候工具
 * 使用@McpTool和@McpParameter注解简化开发
 */
@Component
@McpTool(
    name = "greeting",
    description = "Multi-language greeting generator",
    version = "2.0.0"
)
public class AnnotatedGreetingTool {

    @McpParameter(
        name = "name",
        description = "用户姓名",
        type = "string",
        required = true
    )
    private String name;

    @McpParameter(
        name = "language",
        description = "首选语言",
        type = "string",
        required = false,
        defaultValue = "en",
        enumValues = {"en", "zh"}
    )
    private String language;

    @McpParameter(
        name = "style",
        description = "问候风格",
        type = "string",
        required = false,
        defaultValue = "casual",
        enumValues = {"formal", "casual"}
    )
    private String style;

    /**
     * 创建工具规范
     */
    public McpServerFeatures.SyncToolSpecification createTool() throws JsonProcessingException {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool("greeting", "Greeting Tool", generateSchema()),
                this::execute);
    }

    /**
     * 执行问候逻辑
     */
    private CallToolResult execute(Object exchange, java.util.Map<String, Object> context) {
        List<Content> result = new ArrayList<>();
        try {
            String name = context.get("name").toString();
            String language = context.getOrDefault("language", "en").toString();
            String style = context.getOrDefault("style", "casual").toString();
            
            // 获取当前时间
            int hour = LocalTime.now().getHour();
            String timeOfDay;
            if (hour >= 5 && hour < 12) {
                timeOfDay = language.equals("zh") ? "早上" : "morning";
            } else if (hour >= 12 && hour < 18) {
                timeOfDay = language.equals("zh") ? "下午" : "afternoon";
            } else {
                timeOfDay = language.equals("zh") ? "晚上" : "evening";
            }

            // 生成问候语
            String greeting;
            if (language.equals("zh")) {
                greeting = style.equals("formal") ?
                        String.format("尊敬的 %s，%s好。希望您今天心情愉快！", name, timeOfDay) :
                        String.format("嘿 %s！%s好啊！今天过得怎么样？", name, timeOfDay);
            } else {
                greeting = style.equals("formal") ?
                        String.format("Dear %s, good %s. I hope you're having a wonderful day!", name, timeOfDay) :
                        String.format("Hey %s! How's your %s going?", name, timeOfDay);
            }

            result.add(new TextContent(greeting));
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
        return SchemaGenerator.generateSchema(this.getClass(), "urn:jsonschema:Greeting");
    }
} 