package io.zhijian.tools.mcp.resources;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.resource.Resource;
import io.modelcontextprotocol.spec.resource.ResourceContents;
import io.modelcontextprotocol.spec.resource.TextResourceContents;
import io.modelcontextprotocol.spec.resource.ReadResourceResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 内存资源处理器
 * 用于管理MCP中的内存资源
 */
public class MemoryResource {
    private static final String RESOURCE_URI = "memory://resource";
    private static final String CONTENT_TYPE = "application/json";

    public static McpServerFeatures.SyncResourceSpecification createMemoryResource() {
        return new McpServerFeatures.SyncResourceSpecification(
                new Resource(
                        RESOURCE_URI,
                        "Memory Resource",
                        "A resource stored in memory",
                        CONTENT_TYPE,
                        null),
                (exchange, context) -> {
                    List<ResourceContents> result = new ArrayList<>();
                    
                    // 获取资源管理器实例
                    MemoryResourceManager manager = MemoryResourceManager.getInstance();
                    
                    // 获取资源内容
                    Object value = manager.getResource(RESOURCE_URI);
                    String content = value != null ? value.toString() : "Resource not found";
                    result.add(new TextResourceContents(RESOURCE_URI, CONTENT_TYPE, content));
                    
                    return new ReadResourceResult(result);
                });
    }
} 