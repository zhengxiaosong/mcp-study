package io.zhijian.tools.mcp.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.zhijian.tools.mcp.annotations.McpParameter;

import java.lang.reflect.Field;

/**
 * JSON Schema生成器
 * 根据@McpParameter注解自动生成JSON Schema
 */
public class SchemaGenerator {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 根据类生成JSON Schema
     * @param clazz 目标类
     * @param schemaId Schema ID
     * @return JSON Schema字符串
     */
    public static String generateSchema(Class<?> clazz, String schemaId) throws JsonProcessingException {
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("type", "object");
        rootNode.put("id", schemaId);

        ObjectNode propertiesNode = mapper.createObjectNode();
        ArrayNode requiredNode = mapper.createArrayNode();

        // 遍历所有字段
        for (Field field : clazz.getDeclaredFields()) {
            McpParameter parameter = field.getAnnotation(McpParameter.class);
            if (parameter != null) {
                ObjectNode propertyNode = mapper.createObjectNode();
                propertyNode.put("type", parameter.type());
                propertyNode.put("description", parameter.description());

                // 处理枚举值
                if (parameter.enumValues().length > 0) {
                    ArrayNode enumNode = mapper.createArrayNode();
                    for (String enumValue : parameter.enumValues()) {
                        enumNode.add(enumValue);
                    }
                    propertyNode.set("enum", enumNode);
                }

                // 处理默认值
                if (!parameter.defaultValue().isEmpty()) {
                    if ("number".equals(parameter.type())) {
                        try {
                            propertyNode.put("default", Double.parseDouble(parameter.defaultValue()));
                        } catch (NumberFormatException e) {
                            // 忽略无效的默认值
                        }
                    } else {
                        propertyNode.put("default", parameter.defaultValue());
                    }
                }

                propertiesNode.set(parameter.name(), propertyNode);

                // 添加到必需字段列表
                if (parameter.required()) {
                    requiredNode.add(parameter.name());
                }
            }
        }

        rootNode.set("properties", propertiesNode);
        
        // 如果有必需字段，添加到Schema中
        if (requiredNode.size() > 0) {
            rootNode.set("required", requiredNode);
        }

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
    }

    /**
     * 根据参数类生成参数列表
     * @param clazz 参数类
     * @return 参数列表
     */
    public static java.util.List<io.modelcontextprotocol.spec.prompt.PromptArgument> generatePromptArguments(Class<?> clazz) {
        java.util.List<io.modelcontextprotocol.spec.prompt.PromptArgument> arguments = new java.util.ArrayList<>();
        
        for (Field field : clazz.getDeclaredFields()) {
            McpParameter parameter = field.getAnnotation(McpParameter.class);
            if (parameter != null) {
                arguments.add(new io.modelcontextprotocol.spec.prompt.PromptArgument(
                    parameter.name(),
                    parameter.description(),
                    parameter.required()
                ));
            }
        }
        
        return arguments;
    }
} 