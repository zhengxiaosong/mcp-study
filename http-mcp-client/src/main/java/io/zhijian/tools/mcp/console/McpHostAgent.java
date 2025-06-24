package io.zhijian.tools.mcp.console;

import io.zhijian.tools.mcp.client.LLMClientManager;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.tool.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MCP Host Agent
 * 负责核心的LLM交互和工具协调逻辑
 */
public class McpHostAgent implements PromptProcessor.LLMClient {
    
    private final LLMClientManager llmClientManager;
    private final ToolExecutor toolExecutor;
    private final PromptProcessor promptProcessor;
    private final ConsoleUI ui;
    private final List<Map<String, String>> conversationHistory;
    
    public McpHostAgent(McpSyncClient mcpClient, List<Tool> availableTools, ConsoleUI ui) {
        this.llmClientManager = new LLMClientManager(ui);
        this.ui = ui;
        this.conversationHistory = new ArrayList<>();
        this.toolExecutor = new ToolExecutor(mcpClient, ui);
        this.promptProcessor = new PromptProcessor(mcpClient, ui);
        
        // 初始化系统提示
        initializeSystemPrompt(availableTools);
    }
    
    /**
     * 处理用户输入的核心方法
     */
    public void handleUserInput(String userInput) throws Exception {
        conversationHistory.add(Map.of("role", "user", "content", userInput));
        
        // 如果是计算相关的请求，添加工具提醒
        if (userInput.contains("*") || userInput.contains("/") || userInput.contains("+") || userInput.contains("-") || userInput.contains("计算")) {
            ui.logInfo("检测到计算请求，提醒AI使用计算器工具");
            // 在对话历史中添加一个提醒
            conversationHistory.add(Map.of("role", "system", "content", "注意：用户要求进行计算，请使用calculator工具执行计算，不要只是描述计算步骤。"));
        }
        
        // 多轮工具调用循环
        int maxIterations = 10; // 最大迭代次数，防止无限循环
        int currentIteration = 0;
        String finalResult = null;
        
        while (currentIteration < maxIterations) {
            currentIteration++;
            ui.logInfo("第 " + currentIteration + " 轮处理...");
            
            String llmResponse = llmClientManager.chat(conversationHistory);
            String assistantMessage = llmClientManager.extractAssistantMessage(llmResponse);
            
            // 添加调试日志
            ui.logInfo("AI原始回答: " + assistantMessage);
            
            if (toolExecutor.containsToolCall(assistantMessage)) {
                // 还有工具调用，继续处理
                ui.logInfo("检测到工具调用，开始执行...");
                String processedMessage = processToolCallsOnly(assistantMessage);
                conversationHistory.add(Map.of("role", "assistant", "content", processedMessage));
                
                // 检查处理后的消息是否还有工具调用
                if (!toolExecutor.containsToolCall(processedMessage)) {
                    // 没有更多工具调用，让AI分析结果并决定是否需要继续
                    ui.logInfo("当前轮次工具调用完成，让AI分析结果...");
                    
                    // 添加一个提示，让AI分析当前结果并决定下一步
                    List<Map<String, String>> tempHistory = new ArrayList<>(conversationHistory);
                    tempHistory.add(Map.of("role", "user", "content", 
                        "请分析上述工具执行结果。如果计算还未完成（比如还有未执行的运算），请继续调用工具完成剩余计算。如果计算已完成，请给出最终答案和计算过程总结。"));
                    
                    String analysisResponse = llmClientManager.chat(tempHistory);
                    String analysisMessage = llmClientManager.extractAssistantMessage(analysisResponse);
                    
                    ui.logInfo("AI分析结果: " + analysisMessage);
                    
                    if (toolExecutor.containsToolCall(analysisMessage)) {
                        // AI决定继续调用工具
                        ui.logInfo("AI决定继续调用工具，进入下一轮");
                        conversationHistory.add(Map.of("role", "assistant", "content", analysisMessage));
                        continue;
                    } else {
                        // AI认为计算已完成
                        ui.logInfo("AI认为计算已完成，准备最终优化");
                        finalResult = analysisMessage;
                        break;
                    }
                } else {
                    ui.logInfo("还有更多工具调用，继续下一轮");
                }
                // 还有工具调用，继续下一轮
            } else {
                // 没有工具调用，保存结果用于最终优化
                ui.logInfo("没有工具调用，直接使用AI回答");
                finalResult = assistantMessage;
                conversationHistory.add(Map.of("role", "assistant", "content", assistantMessage));
                break;
            }
        }
        
        if (currentIteration >= maxIterations) {
            ui.logWarning("达到最大迭代次数，可能存在无限循环");
            finalResult = "处理超时，请重试";
        }
        
        // 使用MCP prompt对最终结果进行优化
        if (finalResult != null) {
            ui.logInfo("开始最终结果优化...");
            String optimizedResult = processFinalResultWithPrompt(finalResult);
            ui.assistantOutput(optimizedResult);
        }
    }
    
    /**
     * 只处理工具调用，不使用MCP prompt
     */
    private String processToolCallsOnly(String message) throws Exception {
        return toolExecutor.processToolCalls(message);
    }
    
    /**
     * 使用MCP prompt处理最终结果
     */
    private String processFinalResultWithPrompt(String finalResult) throws Exception {
        try {
            return promptProcessor.processWithMCPPrompt(finalResult, toolExecutor, 
                getLastUserInput(), this);
        } catch (Exception e) {
            ui.logWarning("MCP Prompt调用失败，使用原始结果: " + e.getMessage());
            return finalResult;
        }
    }
    
    private String getLastUserInput() {
        for (int i = conversationHistory.size() - 1; i >= 0; i--) {
            Map<String, String> msg = conversationHistory.get(i);
            if ("user".equals(msg.get("role"))) {
                return msg.get("content");
            }
        }
        return "用户请求";
    }
    
    public void clearHistory(List<Tool> availableTools) {
        conversationHistory.clear();
        initializeSystemPrompt(availableTools);
        ui.logSuccess("对话历史已清空");
        System.out.println();
    }
    
    private void initializeSystemPrompt(List<Tool> availableTools) {
        StringBuilder systemPrompt = new StringBuilder();
        systemPrompt.append("# 智能助手系统提示\n\n");
        systemPrompt.append("## 角色定义\n");
        systemPrompt.append("你是一个智能助手，可以帮助用户解决各种问题。你能够使用工具来获取信息或执行操作。\n\n");
        
        systemPrompt.append("## 重要原则\n");
        systemPrompt.append("**当用户要求进行计算时，你必须使用计算器工具，而不是自己计算！**\n");
        systemPrompt.append("**不要只是描述计算步骤，而是要实际调用工具执行计算！**\n\n");
        
        systemPrompt.append("## 工具使用指南\n");
        systemPrompt.append("你可以使用以下工具来帮助用户：\n\n");
        
        for (Tool tool : availableTools) {
            systemPrompt.append("### ").append(tool.getName()).append("\n");
            systemPrompt.append("- 描述: ").append(tool.getDescription()).append("\n");
            systemPrompt.append("- 参数: ").append(tool.getInputSchema()).append("\n\n");
        }
        
        // 添加调试日志
        ui.logInfo("初始化系统提示，可用工具数量: " + availableTools.size());
        for (Tool tool : availableTools) {
            ui.logInfo("工具: " + tool.getName() + " - " + tool.getDescription());
        }
        
        systemPrompt.append("## 工具调用格式\n");
        systemPrompt.append("当你需要使用工具时，请使用以下格式：\n");
        systemPrompt.append("【工具调用】\n");
        systemPrompt.append("工具名称: <工具名>\n");
        systemPrompt.append("参数: <JSON格式的参数>\n");
        systemPrompt.append("【工具调用结束】\n\n");
        
        systemPrompt.append("## 计算任务处理规则\n");
        systemPrompt.append("当用户要求计算数学表达式时：\n");
        systemPrompt.append("1. **必须使用计算器工具**：不要自己计算，必须调用calculator工具\n");
        systemPrompt.append("2. **分解复杂表达式**：将复杂表达式分解为简单运算\n");
        systemPrompt.append("3. **按优先级执行**：先乘除，后加减\n");
        systemPrompt.append("4. **逐步调用工具**：每个运算都要调用一次calculator工具\n\n");
        
        systemPrompt.append("## 示例：用户说\"计算 30 * 39 + 3 * 4 - 1\"\n");
        systemPrompt.append("你应该这样回应：\n");
        systemPrompt.append("我来帮您计算这个表达式。按照运算优先级，我需要先计算乘法，再进行加减法。\n\n");
        systemPrompt.append("第一步，计算 30 * 39：\n");
        systemPrompt.append("【工具调用】\n");
        systemPrompt.append("工具名称: calculator\n");
        systemPrompt.append("参数: {\"operation\": \"multiply\", \"a\": 30, \"b\": 39}\n");
        systemPrompt.append("【工具调用结束】\n\n");
        systemPrompt.append("第二步，计算 3 * 4：\n");
        systemPrompt.append("【工具调用】\n");
        systemPrompt.append("工具名称: calculator\n");
        systemPrompt.append("参数: {\"operation\": \"multiply\", \"a\": 3, \"b\": 4}\n");
        systemPrompt.append("【工具调用结束】\n\n");
        systemPrompt.append("注意：第三步和第四步需要在获得前两步的实际结果后才能执行。\n");
        systemPrompt.append("请等待工具执行结果，然后根据实际返回的数值继续计算。\n\n");
        
        systemPrompt.append("## 多轮工具调用策略\n");
        systemPrompt.append("1. 首先调用所有可以立即执行的工具（使用已知数值）\n");
        systemPrompt.append("2. 等待工具执行结果\n");
        systemPrompt.append("3. 根据工具返回的实际结果，继续调用后续工具\n");
        systemPrompt.append("4. 重复步骤2-3，直到完成所有计算\n\n");
        systemPrompt.append("## 重要提醒\n");
        systemPrompt.append("- 不要使用占位符如 <第一步结果>，必须等待实际数值\n");
        systemPrompt.append("- 工具执行后，使用返回的具体数值进行后续计算\n");
        systemPrompt.append("- 如果计算复杂，可以分多轮进行，每轮等待工具结果\n");
        systemPrompt.append("- 最终给出完整的计算过程和结果\n\n");
        
        systemPrompt.append("### 工作流程\n");
        systemPrompt.append("1. **分析任务**：理解用户需求，制定执行计划\n");
        systemPrompt.append("2. **逐步执行**：通过多轮工具调用完成复杂任务\n");
        systemPrompt.append("3. **收集结果**：每轮工具调用后，将结果添加到对话中\n");
        systemPrompt.append("4. **继续执行**：根据结果决定是否需要继续调用工具\n");
        systemPrompt.append("5. **完成总结**：所有工具调用完成后，系统会自动优化最终结果\n\n");
        
        systemPrompt.append("### 多轮调用示例\n");
        systemPrompt.append("对于表达式 `29 * 48 + 39 / 3`：\n");
        systemPrompt.append("1. 第一步：计算 `29 * 48`\n");
        systemPrompt.append("2. 第二步：计算 `39 / 3`\n");
        systemPrompt.append("3. 第三步：将前两步结果相加\n");
        systemPrompt.append("每步完成后，等待结果再继续下一步。\n\n");
        
        systemPrompt.append("### 多步计算策略\n");
        systemPrompt.append("**方法一：一次性发出所有工具调用**\n");
        systemPrompt.append("在第一次回答中发出所有必要的工具调用，系统会依次执行。\n\n");
        systemPrompt.append("**方法二：分步执行**\n");
        systemPrompt.append("1. 发出第一组工具调用\n");
        systemPrompt.append("2. 等待执行结果\n");
        systemPrompt.append("3. 分析结果，决定是否需要继续调用工具\n");
        systemPrompt.append("4. 如果需要，发出下一组工具调用\n");
        systemPrompt.append("5. 重复直到计算完成\n\n");
        
        systemPrompt.append("### 计算完成判断\n");
        systemPrompt.append("当收到工具执行结果后，请判断：\n");
        systemPrompt.append("1. **计算是否完成**：所有运算是否都已执行\n");
        systemPrompt.append("2. **是否需要继续**：是否还有未完成的运算\n");
        systemPrompt.append("3. **给出最终答案**：如果计算完成，提供最终结果\n\n");
        
        systemPrompt.append("### 数学表达式处理规则\n");
        systemPrompt.append("1. **运算优先级**：先乘除，后加减\n");
        systemPrompt.append("2. **分步计算**：将复杂表达式分解为简单运算\n");
        systemPrompt.append("3. **结果引用**：使用前一步的结果作为下一步的输入\n");
        systemPrompt.append("4. **验证结果**：检查最终计算结果的合理性\n\n");
        
        systemPrompt.append("### 工具调用格式示例\n");
        systemPrompt.append("对于表达式 `20 * 4 + 40 / 2 + 5`：\n");
        systemPrompt.append("**重要：你需要一次性发出所有必要的工具调用，不要分多次发送！**\n\n");
        systemPrompt.append("第一步：\n");
        systemPrompt.append("【工具调用】\n");
        systemPrompt.append("工具名称: calculator\n");
        systemPrompt.append("参数: {\"operation\": \"multiply\", \"a\": 20, \"b\": 4}\n");
        systemPrompt.append("【工具调用结束】\n\n");
        systemPrompt.append("第二步：\n");
        systemPrompt.append("【工具调用】\n");
        systemPrompt.append("工具名称: calculator\n");
        systemPrompt.append("参数: {\"operation\": \"divide\", \"a\": 40, \"b\": 2}\n");
        systemPrompt.append("【工具调用结束】\n\n");
        systemPrompt.append("第三步：\n");
        systemPrompt.append("【工具调用】\n");
        systemPrompt.append("工具名称: calculator\n");
        systemPrompt.append("参数: {\"operation\": \"add\", \"a\": 80, \"b\": 20}\n");
        systemPrompt.append("【工具调用结束】\n\n");
        systemPrompt.append("第四步：\n");
        systemPrompt.append("【工具调用】\n");
        systemPrompt.append("工具名称: calculator\n");
        systemPrompt.append("参数: {\"operation\": \"add\", \"a\": 100, \"b\": 5}\n");
        systemPrompt.append("【工具调用结束】\n\n");
        
        systemPrompt.append("### 重要提醒\n");
        systemPrompt.append("1. **一次性发出所有工具调用**：不要分多次发送，一次回答中包含所有必要的工具调用\n");
        systemPrompt.append("2. **按运算优先级排序**：先乘除，后加减\n");
        systemPrompt.append("3. **使用前一步的结果**：在后续工具调用中使用前面步骤的计算结果\n");
        systemPrompt.append("4. **确保完整性**：确保所有运算都被包含在工具调用中\n\n");
        
        systemPrompt.append("## 工具结果处理指南\n");
        systemPrompt.append("当你收到工具执行结果时，请按照以下模板分析和回答：\n\n");
        
        systemPrompt.append("### 多步计算处理模板：\n");
        systemPrompt.append("1. **收集所有步骤结果**：记录每一步的计算结果\n");
        systemPrompt.append("2. **验证中间结果**：检查每个步骤的计算是否正确\n");
        systemPrompt.append("3. **组合最终结果**：将所有步骤的结果正确组合\n");
        systemPrompt.append("4. **最终验证**：手动验证最终结果的合理性\n\n");
        
        systemPrompt.append("### 工具结果处理策略：\n");
        systemPrompt.append("1. **等待工具结果**：不要假设工具会返回什么，等待实际结果\n");
        systemPrompt.append("2. **分析结果内容**：仔细分析工具返回的具体数值\n");
        systemPrompt.append("3. **决定下一步**：根据当前结果决定是否需要继续计算\n");
        systemPrompt.append("4. **使用实际结果**：在后续工具调用中使用工具返回的实际数值\n\n");
        
        systemPrompt.append("### 成功结果处理模板：\n");
        systemPrompt.append("\"根据计算器的执行结果：{结果内容}，我可以告诉您...\"\n");
        systemPrompt.append("- 用自然语言解释计算过程\n");
        systemPrompt.append("- 确认结果的准确性\n");
        systemPrompt.append("- 提供最终答案\n\n");
        
        systemPrompt.append("### 错误结果处理模板：\n");
        systemPrompt.append("\"很抱歉，计算器执行时遇到了问题：{错误信息}。这可能是因为...\"\n");
        systemPrompt.append("- 解释可能的错误原因\n");
        systemPrompt.append("- 提供解决建议或替代方案\n");
        systemPrompt.append("- 询问是否需要尝试其他方法\n\n");
        
        systemPrompt.append("### 多步计算示例：\n");
        systemPrompt.append("对于表达式 `20 * 4 + 40 / 2 + 5`：\n");
        systemPrompt.append("1. 第一步结果：20 * 4 = 80\n");
        systemPrompt.append("2. 第二步结果：40 / 2 = 20\n");
        systemPrompt.append("3. 第三步结果：80 + 20 = 100\n");
        systemPrompt.append("4. 第四步结果：100 + 5 = 105\n");
        systemPrompt.append("最终答案：105\n\n");
        
        systemPrompt.append("### 工具结果处理示例：\n");
        systemPrompt.append("假设第一步工具返回：Result: 80\n");
        systemPrompt.append("第二步工具返回：Result: 20\n");
        systemPrompt.append("第三步应该使用实际结果：{\"operation\": \"add\", \"a\": 80, \"b\": 20}\n");
        systemPrompt.append("而不是假设的结果！\n\n");
        
        systemPrompt.append("## 对话原则\n");
        systemPrompt.append("1. 始终以用户需求为中心\n");
        systemPrompt.append("2. 使用清晰、准确的语言\n");
        systemPrompt.append("3. 主动提供帮助和建议\n");
        systemPrompt.append("4. 保持对话的连贯性和上下文理解\n");
        systemPrompt.append("5. 在适当时机使用工具来增强回答的准确性和实用性\n");
        
        conversationHistory.add(Map.of("role", "system", "content", systemPrompt.toString()));
    }
    
    @Override
    public String chat(List<Map<String, String>> history) throws Exception {
        return llmClientManager.chat(history);
    }
    
    @Override
    public String extractAssistantMessage(String response) {
        try {
            return llmClientManager.extractAssistantMessage(response);
        } catch (Exception e) {
            // 你可以选择记录日志，或者抛出运行时异常
            throw new RuntimeException("extractAssistantMessage 发生异常", e);
        }
    }
    
    public PromptProcessor getPromptProcessor() {
        return promptProcessor;
    }
}