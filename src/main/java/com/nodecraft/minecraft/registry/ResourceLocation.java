package com.nodecraft.minecraft.registry;

import java.util.Objects;

/**
 * 表示Minecraft资源位置的类，格式为 "namespace:path"
 */
public class ResourceLocation {
    private static final String DEFAULT_NAMESPACE = "minecraft";
    
    private final String namespace;
    private final String path;
    
    /**
     * 创建资源位置
     * @param namespace 命名空间
     * @param path 路径
     */
    public ResourceLocation(String namespace, String path) {
        this.namespace = namespace != null && !namespace.isEmpty() ? namespace : DEFAULT_NAMESPACE;
        this.path = Objects.requireNonNull(path, "Path cannot be null");
        
        validateNamespace(this.namespace);
        validatePath(this.path);
    }
    
    /**
     * 使用默认命名空间创建资源位置
     * @param path 路径
     */
    public ResourceLocation(String path) {
        this(DEFAULT_NAMESPACE, path);
    }
    
    /**
     * 从字符串解析资源位置
     * @param location 格式为 "namespace:path" 的字符串，如果没有冒号，则使用默认命名空间
     * @return 资源位置
     */
    public static ResourceLocation fromString(String location) {
        if (location == null || location.isEmpty()) {
            throw new IllegalArgumentException("Location cannot be null or empty");
        }
        
        int colonIndex = location.indexOf(':');
        if (colonIndex >= 0) {
            String namespace = location.substring(0, colonIndex);
            String path = location.substring(colonIndex + 1);
            return new ResourceLocation(namespace, path);
        } else {
            return new ResourceLocation(DEFAULT_NAMESPACE, location);
        }
    }
    
    /**
     * 获取命名空间
     * @return 命名空间
     */
    public String getNamespace() {
        return namespace;
    }
    
    /**
     * 获取路径
     * @return 路径
     */
    public String getPath() {
        return path;
    }
    
    /**
     * 验证命名空间
     * @param namespace 要验证的命名空间
     */
    private static void validateNamespace(String namespace) {
        for (int i = 0; i < namespace.length(); i++) {
            char c = namespace.charAt(i);
            if (c != '_' && c != '-' && c != '.' && (c < 'a' || c > 'z') && (c < '0' || c > '9')) {
                throw new IllegalArgumentException("Namespace can only contain [a-z0-9_.-]: " + namespace);
            }
        }
    }
    
    /**
     * 验证路径
     * @param path 要验证的路径
     */
    private static void validatePath(String path) {
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c != '_' && c != '-' && c != '.' && c != '/' && (c < 'a' || c > 'z') && (c < '0' || c > '9')) {
                throw new IllegalArgumentException("Path can only contain [a-z0-9_.-/]: " + path);
            }
        }
    }
    
    @Override
    public String toString() {
        return namespace + ":" + path;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceLocation that = (ResourceLocation) o;
        return namespace.equals(that.namespace) && path.equals(that.path);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(namespace, path);
    }
} 