package io.zhijian.tools.mcp.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MCP资源注解
 * 用于标记MCP资源类，简化资源注册过程
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface McpResource {
    
    /**
     * 资源URI
     */
    String uri();
    
    /**
     * 资源名称
     */
    String name();
    
    /**
     * 资源描述
     */
    String description();
    
    /**
     * 内容类型
     */
    String contentType() default "application/json";
    
    /**
     * 是否启用
     */
    boolean enabled() default true;
} 