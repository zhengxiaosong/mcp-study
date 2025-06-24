package io.zhijian.tools.mcp.console;

import java.util.List;

import io.modelcontextprotocol.spec.prompt.Prompt;
import io.modelcontextprotocol.spec.tool.Tool;

/**
 * 控制台用户界面管理器
 * 负责所有的输出格式化、日志记录和用户交互界面
 */
public class ConsoleUI {
    
    // ANSI 颜色代码
    private static final String RESET = "\033[0m";
    private static final String BLUE = "\033[34m";
    private static final String GREEN = "\033[32m";
    private static final String YELLOW = "\033[33m";
    private static final String RED = "\033[31m";
    private static final String CYAN = "\033[36m";
    private static final String BOLD = "\033[1m";
    private static final String DIM = "\033[2m";
    
    /**
     * 显示欢迎界面
     */
    public void showWelcome() {
        userOutput("\n╔═══════════════════════════════════════╗");
        userOutput("║        🤖 MCP Host 智能助手           ║");
        userOutput("║   作为 MCP Host，通过 Agent 为您服务    ║");
        userOutput("╚═══════════════════════════════════════╝");
        userOutput("");
        userOutput("📋 可用命令:");
        userOutput("  • exit/quit  - 退出程序");
        userOutput("  • tools     - 查看可用工具");
        userOutput("  • prompts   - 查看可用提示模板");
        userOutput("  • clear     - 清空对话历史");
        userOutput("");
    }
    
    /**
     * 显示用户输入提示符
     */
    public void showUserPrompt() {
        System.out.print(BOLD + "👤 您: " + RESET);
    }
    
    /**
     * 显示可用工具列表
     */
    public void showAvailableTools(List<Tool> tools) {
        userOutput("\n╔═══════════════════════════════════════╗");
        userOutput("║            🔧 可用工具列表             ║");
        userOutput("╚═══════════════════════════════════════╝");
        for (Tool tool : tools) {
            System.out.println("🔧 " + BLUE + tool.getName() + RESET + ": " + tool.getDescription());
        }
        userOutput("");
    }
    
    /**
     * 显示可用 Prompt 模板
     */
    public void showAvailablePrompts(List<Prompt> prompts) {
        userOutput("\n╔═══════════════════════════════════════╗");
        userOutput("║           📝 可用 Prompt 模板          ║");
        userOutput("╚═══════════════════════════════════════╝");
        for (Prompt prompt : prompts) {
            System.out.println("📝 " + YELLOW + prompt.getName() + RESET + ": " + prompt.getDescription());
        }
        userOutput("");
    }
    
    /**
     * 显示告别消息
     */
    public void showGoodbye() {
        userOutput("\n👋 感谢使用 MCP Host！再见！");
    }
    
    // ============ 日志和输出方法 ============
    
    /**
     * 用户界面输出 - 用于与用户直接交互的内容
     */
    public void userOutput(String message) {
        System.out.println(CYAN + BOLD + message + RESET);
    }
    
    /**
     * 系统信息日志 - 用于程序运行状态
     */
    public void logInfo(String message) {
        System.out.println(DIM + "[系统] " + message + RESET);
    }
    
    /**
     * 成功日志
     */
    public void logSuccess(String message) {
        System.out.println(GREEN + "[系统] " + message + RESET);
    }
    
    /**
     * 警告日志
     */
    public void logWarning(String message) {
        System.out.println(YELLOW + "[警告] " + message + RESET);
    }
    
    /**
     * 错误日志
     */
    public void logError(String message) {
        System.err.println(RED + "[错误] " + message + RESET);
    }
    
    /**
     * 工具执行日志
     */
    public void logTool(String message) {
        System.out.println(BLUE + "[工具] " + message + RESET);
    }
    
    /**
     * LLM客户端状态日志
     */
    public void logLLM(String message) {
        System.out.println(CYAN + "[LLM] " + message + RESET);
    }
    
    /**
     * 助手回答输出 - 用于显示AI助手的回答
     */
    public void assistantOutput(String message) {
        System.out.println(GREEN + "🤖 助手: " + RESET + message);
    }
    
    /**
     * 致命错误输出
     */
    public void fatalError(String message) {
        System.err.println(RED + "[致命错误] " + message + RESET);
    }
} 