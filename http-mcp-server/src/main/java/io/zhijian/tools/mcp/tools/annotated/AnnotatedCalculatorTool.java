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

/**
 * 注解版计算器工具
 * 使用@McpTool和@McpParameter注解简化开发
 */
@Component
@McpTool(
    name = "calculator",
    description = "Basic calculator for mathematical operations",
    version = "2.0.0"
)
public class AnnotatedCalculatorTool {

    @McpParameter(
        name = "operation",
        description = "计算操作类型",
        type = "string",
        required = true,
        enumValues = {"add", "subtract", "multiply", "divide"}
    )
    private String operation;

    @McpParameter(
        name = "a",
        description = "第一个操作数",
        type = "number",
        required = true
    )
    private Double a;

    @McpParameter(
        name = "b",
        description = "第二个操作数",
        type = "number",
        required = true
    )
    private Double b;

    /**
     * 创建工具规范
     */
    public McpServerFeatures.SyncToolSpecification createTool() throws JsonProcessingException {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool("calculator", "Basic calculator", generateSchema()),
                this::execute);
    }

    /**
     * 执行计算逻辑
     */
    private CallToolResult execute(Object exchange, java.util.Map<String, Object> context) {
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
        return new CallToolResult(result, true);
    }

    /**
     * 生成JSON Schema
     */
    private String generateSchema() throws JsonProcessingException {
        return SchemaGenerator.generateSchema(this.getClass(), "urn:jsonschema:CalculatorOperation");
    }
} 