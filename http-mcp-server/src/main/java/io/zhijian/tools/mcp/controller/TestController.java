package io.zhijian.tools.mcp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器
 * 用于验证Web功能是否正常工作
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/health")
    public String health() {
        return "MCP Server is running! Web functionality is working.";
    }

    @GetMapping("/info")
    public String info() {
        return "MCP Server Info:\n" +
               "- Spring Boot Web: Enabled\n" +
               "- MCP Components: Auto-registered\n" +
               "- SSE Endpoint: /sse/*\n" +
               "- Test Endpoint: /test/*";
    }
} 