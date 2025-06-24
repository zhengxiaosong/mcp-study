package io.zhijian.tools.mcp.prompts.annotated;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.common.Role;
import io.modelcontextprotocol.spec.content.TextContent;
import io.modelcontextprotocol.spec.prompt.GetPromptResult;
import io.modelcontextprotocol.spec.prompt.Prompt;
import io.modelcontextprotocol.spec.prompt.PromptMessage;
import io.zhijian.tools.mcp.annotations.McpPrompt;
import io.zhijian.tools.mcp.annotations.McpParameter;
import io.zhijian.tools.mcp.utils.SchemaGenerator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 注解版工具结果分析Prompt
 * 使用@McpPrompt和@McpParameter注解简化开发
 */
@Component
@McpPrompt(
    name = "analyze_tool_result",
    description = "帮助LLM分析和解释工具执行结果的思维框架",
    version = "2.0.0"
)
public class AnnotatedToolResultAnalysisPrompt {

    @McpParameter(
        name = "tool_name",
        description = "执行的工具名称",
        type = "string",
        required = true
    )
    private String toolName;

    @McpParameter(
        name = "tool_result",
        description = "工具执行的原始结果",
        type = "string",
        required = true
    )
    private String toolResult;

    @McpParameter(
        name = "user_context",
        description = "用户的原始请求上下文",
        type = "string",
        required = false
    )
    private String userContext;

    /**
     * 创建提示规范
     */
    public McpServerFeatures.SyncPromptSpecification createPrompt() throws JsonProcessingException {
        return new McpServerFeatures.SyncPromptSpecification(
                new Prompt("analyze_tool_result", "帮助LLM分析和解释工具执行结果的思维框架", generateArguments()),
                (exchange, context) -> {
                    List<PromptMessage> messages = new ArrayList<>();
                    
                    try {
                        // 获取参数
                        String toolName = context.getArguments().getOrDefault("tool_name", "").toString();
                        String toolResult = context.getArguments().getOrDefault("tool_result", "").toString();
                        String userContext = context.getArguments().getOrDefault("user_context", "").toString();
                        
                        // 构建分析框架提示
                        String analysisPrompt = buildAnalysisPrompt(toolName, toolResult, userContext);
                        
                        // 返回给LLM的提示消息
                        messages.add(new PromptMessage(
                                Role.USER, 
                                new TextContent(analysisPrompt)
                        ));
                        
                    } catch (Exception e) {
                        messages.add(new PromptMessage(
                                Role.USER, 
                                new TextContent("请分析工具执行结果并向用户解释。")
                        ));
                    }
                    
                    return new GetPromptResult(null, messages);
                });
    }

    /**
     * 构建分析框架提示
     */
    private String buildAnalysisPrompt(String toolName, String toolResult, String userContext) {
        return String.format(
            "请作为智能助手，分析以下工具执行结果并向用户提供解释：\n" +
            "\n" +
            "## 上下文信息\n" +
            "- 用户请求：%s\n" +
            "- 使用工具：%s\n" +
            "- 执行结果：%s\n" +
            "\n" +
            "## 分析框架\n" +
            "请按照以下思维框架分析和回答：\n" +
            "\n" +
            "1. **结果理解**：首先理解工具返回的原始结果含义\n" +
            "2. **准确性验证**：检查结果是否合理（特别是数值计算）\n" +
            "3. **用户价值**：将技术结果转化为对用户有意义的信息\n" +
            "4. **完整性评估**：判断是否需要额外信息或进一步操作\n" +
            "5. **后续建议**：主动提供相关的帮助或建议\n" +
            "\n" +
            "## 回答原则\n" +
            "- 使用自然、友好的语言\n" +
            "- 避免直接复述技术输出\n" +
            "- 确保用户能够理解结果的实际意义\n" +
            "- 在适当时候询问是否需要进一步帮助\n" +
            "\n" +
            "请基于以上框架，为用户解释这个工具执行结果：\n",
            userContext, toolName, toolResult);
    }

    /**
     * 生成参数列表
     */
    private List<io.modelcontextprotocol.spec.prompt.PromptArgument> generateArguments() {
        return SchemaGenerator.generatePromptArguments(this.getClass());
    }
} 