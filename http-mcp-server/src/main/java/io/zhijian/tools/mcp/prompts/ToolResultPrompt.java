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
 * 工具执行结果处理Prompt模板
 * 提供标准化的工具执行结果处理模板，确保结果解释的一致性和专业性
 */
public class ToolResultPrompt {
    private static final String PROMPT_NAME = "tool_result";
    private static final String PROMPT_DESCRIPTION = "工具执行结果处理模板，提供标准化的结果解释和后续处理建议";

    public static McpServerFeatures.SyncPromptSpecification createPrompt() throws JsonProcessingException {
        return new McpServerFeatures.SyncPromptSpecification(
                new Prompt(PROMPT_NAME, PROMPT_DESCRIPTION, createArgumentSchema()),
                (exchange, context) -> {
                    List<PromptMessage> messages = new ArrayList<>();
                    
                    try {
                        // 获取参数
                        String resultType = context.getArguments().getOrDefault("result_type", "success").toString();
                        String toolName = context.getArguments().getOrDefault("tool_name", "").toString();
                        String resultContent = context.getArguments().getOrDefault("result_content", "").toString();
                        String language = context.getArguments().getOrDefault("language", "zh").toString();
                        
                        // 构建系统消息
                        String systemMessage = buildSystemMessage(resultType, toolName, language);
                        messages.add(new PromptMessage(Role.ASSISTANT, new TextContent(systemMessage)));
                        
                        // 构建结果处理消息
                        String resultMessage = buildResultMessage(resultType, resultContent, language);
                        messages.add(new PromptMessage(Role.ASSISTANT, new TextContent(resultMessage)));
                        
                    } catch (Exception e) {
                        messages.add(new PromptMessage(
                                Role.ASSISTANT, 
                                new TextContent("工具执行结果处理失败，请重试。")
                        ));
                    }
                    
                    return new GetPromptResult(null, messages);
                });
    }

    /**
     * 构建系统提示消息
     */
    private static String buildSystemMessage(String resultType, String toolName, String language) {
        StringBuilder prompt = new StringBuilder();
        
        if ("zh".equals(language)) {
            prompt.append("# 工具执行结果处理指南\n\n");
            prompt.append("## 结果类型\n");
            prompt.append("- 工具名称: ").append(toolName).append("\n");
            prompt.append("- 执行状态: ").append(getResultTypeDesc(resultType, language)).append("\n\n");
            
            prompt.append("## 处理原则\n");
            prompt.append("1. 准确理解结果内容\n");
            prompt.append("2. 提供清晰的解释\n");
            prompt.append("3. 给出后续建议\n");
            prompt.append("4. 保持专业态度\n");
            prompt.append("5. 主动提供帮助\n\n");
            
        } else {
            prompt.append("# Tool Execution Result Processing Guide\n\n");
            prompt.append("## Result Type\n");
            prompt.append("- Tool Name: ").append(toolName).append("\n");
            prompt.append("- Execution Status: ").append(getResultTypeDesc(resultType, language)).append("\n\n");
            
            prompt.append("## Processing Principles\n");
            prompt.append("1. Accurately understand the result content\n");
            prompt.append("2. Provide clear explanations\n");
            prompt.append("3. Give follow-up suggestions\n");
            prompt.append("4. Maintain professionalism\n");
            prompt.append("5. Proactively offer help\n\n");
        }
        
        return prompt.toString();
    }

    /**
     * 构建结果处理消息
     */
    private static String buildResultMessage(String resultType, String resultContent, String language) {
        if ("zh".equals(language)) {
            switch (resultType) {
                case "success":
                    return "工具执行成功！\n\n" +
                           "执行结果：\n" + resultContent + "\n\n" +
                           "是否需要我为您进一步解释或提供其他帮助？";
                case "error":
                    return "工具执行遇到问题：\n\n" +
                           "错误信息：\n" + resultContent + "\n\n" +
                           "建议采取以下措施：\n" +
                           "1. 检查输入参数是否正确\n" +
                           "2. 确认系统状态是否正常\n" +
                           "3. 尝试使用替代方案\n\n" +
                           "需要我帮您尝试其他解决方案吗？";
                case "partial":
                    return "工具执行部分成功：\n\n" +
                           "当前结果：\n" + resultContent + "\n\n" +
                           "可能需要补充以下信息：\n" +
                           "1. 更详细的参数\n" +
                           "2. 额外的上下文信息\n" +
                           "3. 具体的需求说明\n\n" +
                           "请提供更多信息，我会继续协助您。";
                default:
                    return "工具执行完成，但结果状态未知。\n\n" +
                           "结果内容：\n" + resultContent + "\n\n" +
                           "建议您重新尝试或提供更多信息。";
            }
        } else {
            switch (resultType) {
                case "success":
                    return "Tool execution successful!\n\n" +
                           "Result:\n" + resultContent + "\n\n" +
                           "Would you like me to explain further or provide additional assistance?";
                case "error":
                    return "Tool execution encountered an issue:\n\n" +
                           "Error message:\n" + resultContent + "\n\n" +
                           "Suggested actions:\n" +
                           "1. Check if input parameters are correct\n" +
                           "2. Verify system status\n" +
                           "3. Try alternative solutions\n\n" +
                           "Would you like me to try other solutions?";
                case "partial":
                    return "Tool execution partially successful:\n\n" +
                           "Current result:\n" + resultContent + "\n\n" +
                           "Additional information may be needed:\n" +
                           "1. More detailed parameters\n" +
                           "2. Additional context\n" +
                           "3. Specific requirements\n\n" +
                           "Please provide more information, and I'll continue to assist you.";
                default:
                    return "Tool execution completed, but result status is unknown.\n\n" +
                           "Result content:\n" + resultContent + "\n\n" +
                           "Please try again or provide more information.";
            }
        }
    }

    /**
     * 获取结果类型描述
     */
    private static String getResultTypeDesc(String resultType, String language) {
        if ("zh".equals(language)) {
            switch (resultType) {
                case "success": return "执行成功";
                case "error": return "执行错误";
                case "partial": return "部分成功";
                default: return "未知状态";
            }
        } else {
            switch (resultType) {
                case "success": return "Execution Successful";
                case "error": return "Execution Error";
                case "partial": return "Partially Successful";
                default: return "Unknown Status";
            }
        }
    }

    /**
     * 创建参数架构
     */
    private static List<PromptArgument> createArgumentSchema() {
        List<PromptArgument> arguments = new ArrayList<>();
        
        // result_type 参数（必填）
        arguments.add(new PromptArgument(
            "result_type",
            "结果类型",
            true
        ));
        // tool_name 参数（必填）
        arguments.add(new PromptArgument(
            "tool_name",
            "工具名称",
            true
        ));
        // result_content 参数（必填）
        arguments.add(new PromptArgument(
            "result_content",
            "结果内容",
            true
        ));
        // language 参数（必填）
        arguments.add(new PromptArgument(
            "language",
            "语言偏好",
            true
        ));
        return arguments;
    }
}