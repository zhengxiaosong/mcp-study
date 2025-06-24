package io.zhijian.tools.mcp.prompts;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.common.Role;
import io.modelcontextprotocol.spec.content.TextContent;
import io.modelcontextprotocol.spec.prompt.GetPromptResult;
import io.modelcontextprotocol.spec.prompt.Prompt;
import io.modelcontextprotocol.spec.prompt.PromptArgument;
import io.modelcontextprotocol.spec.prompt.PromptMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 工具结果分析Prompt - 真正的Prompt用法
 * 为LLM提供分析工具结果的思维框架和指导原则
 * 这是给LLM的输入模板，而不是格式化输出
 */
public class ToolResultAnalysisPrompt {
    private static final String PROMPT_NAME = "analyze_tool_result";
    private static final String PROMPT_DESCRIPTION = "帮助LLM分析和解释工具执行结果的思维框架";

    public static McpServerFeatures.SyncPromptSpecification createPrompt() throws JsonProcessingException {
        return new McpServerFeatures.SyncPromptSpecification(
                new Prompt(PROMPT_NAME, PROMPT_DESCRIPTION, createArgumentSchema()),
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
     * 构建分析框架提示 - 这是给LLM的输入指导
     */
    private static String buildAnalysisPrompt(String toolName, String toolResult, String userContext) {
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
     * 创建参数架构
     */
    private static List<PromptArgument> createArgumentSchema() {
        List<PromptArgument> arguments = new ArrayList<>();
        
        arguments.add(new PromptArgument(
            "tool_name",
            "执行的工具名称",
            true
        ));
        
        arguments.add(new PromptArgument(
            "tool_result", 
            "工具执行的原始结果",
            true
        ));
        
        arguments.add(new PromptArgument(
            "user_context",
            "用户的原始请求上下文",
            false
        ));
        
        return arguments;
    }
} 