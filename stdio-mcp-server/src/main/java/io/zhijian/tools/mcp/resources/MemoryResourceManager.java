package io.zhijian.tools.mcp.resources;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存资源管理器
 * 使用单例模式确保资源在整个应用中共享
 */
public class MemoryResourceManager {
    private static final MemoryResourceManager instance = new MemoryResourceManager();
    private final Map<String, Object> resources = new ConcurrentHashMap<>();

    private MemoryResourceManager() {}

    public static MemoryResourceManager getInstance() {
        return instance;
    }

    public void setResource(String key, Object value) {
        resources.put(key, value);
    }

    public Object getResource(String key) {
        return resources.get(key);
    }

    public void removeResource(String key) {
        resources.remove(key);
    }

    public boolean hasResource(String key) {
        return resources.containsKey(key);
    }

    public void clear() {
        resources.clear();
    }
} 