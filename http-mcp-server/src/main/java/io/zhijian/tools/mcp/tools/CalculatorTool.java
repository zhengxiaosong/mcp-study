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

import java.util.ArrayList;
import java.util.List;

/**
 * 计算器工具
 * 提供基本的数学运算功能
 */
public class CalculatorTool {
    private static final String TOOL_NAME = "calculator";
    private static final String TOOL_DESCRIPTION = "Basic calculator";

    public static McpServerFeatures.SyncToolSpecification createTool() throws JsonProcessingException {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool(TOOL_NAME, TOOL_DESCRIPTION, createSchema()),
                (exchange, context) -> {
                    List<Content> result = new ArrayList<>();
                    try {
                        String operation = context.get("operation").toString();
                        double a = Double.valueOf(context.get("a").toString());
                        double b = Double.valueOf(context.get("b").toString());
                        double calculationResult;

                        switch (operation) {
                            case "add":
                                calculationResult = a + b;
                                break;
                            case "subtract":
                                calculationResult = a - b;
                                break;
                            case "multiply":
                                calculationResult = a * b;
                                break;
                            case "divide":
                                if (b == 0) {
                                    result.add(new TextContent("Error: Division by zero is not allowed."));
                                    return new CallToolResult(result, true);
                                }
                                calculationResult = a / b;
                                break;
                            default:
                                result.add(new TextContent("Error: Invalid operation. Only add, subtract, multiply, and divide are allowed."));
                                return new CallToolResult(result, true);
                        }

                        result.add(new TextContent("Result: " + calculationResult));
                    }
                    catch (Exception e) {
                        result.add(new TextContent("Error: " + e.getMessage()));
                        return new CallToolResult(result, true);
                    }
                    return new CallToolResult(result, false);
                });
    }

    private static String createSchema() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("type", "object");
        rootNode.put("id", "urn:jsonschema:Operation");

        ObjectNode propertiesNode = mapper.createObjectNode();

        ObjectNode operationNode = mapper.createObjectNode();
        operationNode.put("type", "string");
        operationNode.put("description", "计算操作类型，只能是add、subtract、multiply、divide四种");
        ArrayNode enumNode = mapper.createArrayNode();
        enumNode.add("add").add("subtract").add("multiply").add("divide");
        operationNode.set("enum", enumNode);

        ObjectNode aNode = mapper.createObjectNode();
        aNode.put("type", "number");

        ObjectNode bNode = mapper.createObjectNode();
        bNode.put("type", "number");

        propertiesNode.set("operation", operationNode);
        propertiesNode.set("a", aNode);
        propertiesNode.set("b", bNode);

        rootNode.set("properties", propertiesNode);

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
    }
} 