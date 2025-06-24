package io.zhijian.tools.mcp.resources.annotated;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.resource.Resource;
import io.modelcontextprotocol.spec.resource.ResourceContents;
import io.modelcontextprotocol.spec.resource.TextResourceContents;
import io.modelcontextprotocol.spec.resource.ReadResourceResult;
import io.zhijian.tools.mcp.annotations.McpResource;
import io.zhijian.tools.mcp.resources.MemoryResourceManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 注解版内存资源
 * 使用@McpResource注解简化开发
 */
@Component
@McpResource(
    uri = "memory://resource",
    name = "Memory Resource",
    description = "A resource stored in memory",
    contentType = "application/json"
)
public class AnnotatedMemoryResource {

    /**
     * 创建资源规范
     */
    public McpServerFeatures.SyncResourceSpecification createResource() {
        return new McpServerFeatures.SyncResourceSpecification(
                new Resource(
                        "memory://resource",
                        "Memory Resource",
                        "A resource stored in memory",
                        "application/json",
                        null),
                this::readResource);
    }

    /**
     * 读取资源内容
     */
    private ReadResourceResult readResource(Object exchange, Object context) {
        List<ResourceContents> result = new ArrayList<>();
        
        // 获取资源管理器实例
        MemoryResourceManager manager = MemoryResourceManager.getInstance();
        
        // 获取资源内容
        Object value = manager.getResource("memory://resource");
        String content = value != null ? value.toString() : "Resource not found";
        result.add(new TextResourceContents("memory://resource", "application/json", content));
        
        return new ReadResourceResult(result);
    }
} 