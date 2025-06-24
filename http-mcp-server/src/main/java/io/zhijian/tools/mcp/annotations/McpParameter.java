package io.zhijian.tools.mcp.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MCP参数注解
 * 用于定义工具或提示的参数
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface McpParameter {
    
    /**
     * 参数名称
     */
    String name();
    
    /**
     * 参数描述
     */
    String description();
    
    /**
     * 参数类型
     */
    String type() default "string";
    
    /**
     * 是否必需
     */
    boolean required() default false;
    
    /**
     * 默认值
     */
    String defaultValue() default "";
    
    /**
     * 枚举值（用于限制可选值）
     */
    String[] enumValues() default {};
} 