package io.zhijian.tools.mcp.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MCP工具注解
 * 用于标记MCP工具类，简化工具注册过程
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface McpTool {
    
    /**
     * 工具名称
     */
    String name();
    
    /**
     * 工具描述
     */
    String description();
    
    /**
     * 工具版本
     */
    String version() default "1.0.0";
    
    /**
     * 是否启用
     */
    boolean enabled() default true;
} 