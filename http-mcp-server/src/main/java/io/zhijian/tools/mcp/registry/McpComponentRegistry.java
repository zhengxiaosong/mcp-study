package io.zhijian.tools.mcp.registry;

import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.zhijian.tools.mcp.annotations.McpTool;
import io.zhijian.tools.mcp.annotations.McpResource;
import io.zhijian.tools.mcp.annotations.McpPrompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * MCP组件注册器
 * 自动发现和注册带有注解的MCP组件
 */
@Component
public class McpComponentRegistry {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 初始化时自动注册所有组件
     */
    @PostConstruct
    public void registerComponents() {
        System.out.println("开始自动注册MCP组件...");
        
        // 获取McpSyncServer实例
        McpSyncServer syncServer = applicationContext.getBean(McpSyncServer.class);
        
        // 注册工具
        registerTools(syncServer);
        
        // 注册资源
        registerResources(syncServer);
        
        // 注册提示
        registerPrompts(syncServer);
        
        System.out.println("MCP组件自动注册完成");
    }

    /**
     * 注册所有带有@McpTool注解的工具
     */
    private void registerTools(McpSyncServer syncServer) {
        Map<String, Object> toolBeans = applicationContext.getBeansOfType(Object.class);
        
        for (Object bean : toolBeans.values()) {
            Class<?> clazz = bean.getClass();
            McpTool annotation = clazz.getAnnotation(McpTool.class);
            
            if (annotation != null && annotation.enabled()) {
                try {
                    System.out.println("注册工具: " + annotation.name());
                    
                    // 调用工具的createTool方法
                    java.lang.reflect.Method createMethod = clazz.getMethod("createTool");
                    McpServerFeatures.SyncToolSpecification toolSpec = 
                        (McpServerFeatures.SyncToolSpecification) createMethod.invoke(bean);
                    
                    syncServer.addTool(toolSpec);
                    
                } catch (Exception e) {
                    System.err.println("注册工具失败: " + annotation.name() + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 注册所有带有@McpResource注解的资源
     */
    private void registerResources(McpSyncServer syncServer) {
        Map<String, Object> resourceBeans = applicationContext.getBeansOfType(Object.class);
        
        for (Object bean : resourceBeans.values()) {
            Class<?> clazz = bean.getClass();
            McpResource annotation = clazz.getAnnotation(McpResource.class);
            
            if (annotation != null && annotation.enabled()) {
                try {
                    System.out.println("注册资源: " + annotation.name());
                    
                    // 调用资源的createResource方法
                    java.lang.reflect.Method createMethod = clazz.getMethod("createResource");
                    McpServerFeatures.SyncResourceSpecification resourceSpec = 
                        (McpServerFeatures.SyncResourceSpecification) createMethod.invoke(bean);
                    
                    syncServer.addResource(resourceSpec);
                    
                } catch (Exception e) {
                    System.err.println("注册资源失败: " + annotation.name() + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 注册所有带有@McpPrompt注解的提示
     */
    private void registerPrompts(McpSyncServer syncServer) {
        Map<String, Object> promptBeans = applicationContext.getBeansOfType(Object.class);
        
        for (Object bean : promptBeans.values()) {
            Class<?> clazz = bean.getClass();
            McpPrompt annotation = clazz.getAnnotation(McpPrompt.class);
            
            if (annotation != null && annotation.enabled()) {
                try {
                    System.out.println("注册提示: " + annotation.name());
                    
                    // 调用提示的createPrompt方法
                    java.lang.reflect.Method createMethod = clazz.getMethod("createPrompt");
                    McpServerFeatures.SyncPromptSpecification promptSpec = 
                        (McpServerFeatures.SyncPromptSpecification) createMethod.invoke(bean);
                    
                    syncServer.addPrompt(promptSpec);
                    
                } catch (Exception e) {
                    System.err.println("注册提示失败: " + annotation.name() + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
} 