package io.zhijian.tools.mcp.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MCP提示注解
 * 用于标记MCP提示类，简化提示注册过程
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface McpPrompt {
    
    /**
     * 提示名称
     */
    String name();
    
    /**
     * 提示描述
     */
    String description();
    
    /**
     * 提示版本
     */
    String version() default "1.0.0";
    
    /**
     * 是否启用
     */
    boolean enabled() default true;
} 