package io.zhijian.tools.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.tool.Tool;
import io.modelcontextprotocol.spec.content.Content;
import io.modelcontextprotocol.spec.content.TextContent;
import io.modelcontextprotocol.spec.tool.CallToolResult;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 问候工具
 * 提供多语言、多风格的问候语生成功能
 */
public class GreetingTool {
    private static final String TOOL_NAME = "greeting";
    private static final String TOOL_DESCRIPTION = "Greeting Tool";

    public static McpServerFeatures.SyncToolSpecification createTool() throws JsonProcessingException {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool(TOOL_NAME, TOOL_DESCRIPTION, createSchema()),
                (exchange, context) -> {
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
                });
    }

    private static String createSchema() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("type", "object");
        rootNode.put("id", "urn:jsonschema:Greeting");

        ObjectNode propertiesNode = mapper.createObjectNode();

        // name 属性
        ObjectNode nameNode = mapper.createObjectNode();
        nameNode.put("type", "string");
        nameNode.put("description", "User name");

        // language 属性
        ObjectNode languageNode = mapper.createObjectNode();
        languageNode.put("type", "string");
        languageNode.put("description", "Preferred language (en/zh)");
        ArrayNode languageEnum = mapper.createArrayNode();
        languageEnum.add("en").add("zh");
        languageNode.set("enum", languageEnum);

        // style 属性
        ObjectNode styleNode = mapper.createObjectNode();
        styleNode.put("type", "string");
        styleNode.put("description", "Greeting style (formal/casual)");
        ArrayNode styleEnum = mapper.createArrayNode();
        styleEnum.add("formal").add("casual");
        styleNode.set("enum", styleEnum);

        propertiesNode.set("name", nameNode);
        propertiesNode.set("language", languageNode);
        propertiesNode.set("style", styleNode);

        rootNode.set("properties", propertiesNode);

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
    }
} 