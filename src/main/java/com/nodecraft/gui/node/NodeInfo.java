package com.nodecraft.gui.node;

import com.nodecraft.nodesystem.api.INode;
import java.util.Objects;

/**
 * 节点信息类
 * 存储节点的元数据，如ID、标题、描述等，以及对应的实现类
 */
public class NodeInfo {
    
    // 节点ID
    private final String id;
    
    // 节点标题
    private final String displayName;
    
    // 节点描述
    private final String description;
    
    // 所属分类ID
    private final String categoryId;

    // 节点实现类
    private final Class<? extends INode> nodeClass;
    
    // 图标路径 (可选)
    private String icon;
    
    /**
     * 构造函数
     * @param id 节点ID
     * @param displayName 节点标题
     * @param description 节点描述
     * @param categoryId 所属分类ID
     * @param nodeClass 节点实现类
     */
    public NodeInfo(String id, String displayName, String description, String categoryId, Class<? extends INode> nodeClass) {
        this.id = Objects.requireNonNull(id, "Node ID cannot be null");
        this.displayName = Objects.requireNonNull(displayName, "Node displayName cannot be null");
        this.description = description != null ? description : "";
        this.categoryId = Objects.requireNonNull(categoryId, "Node categoryId cannot be null");
        this.nodeClass = Objects.requireNonNull(nodeClass, "Node class cannot be null for ID: " + id);
    }
    
    /**
     * 简化的构造函数，用于向后兼容或某些场景
     * 注意：使用此构造函数创建的NodeInfo将无法通过NodeRegistry实例化
     * @deprecated Consider using the constructor with nodeClass parameter.
     */
    @Deprecated
    public NodeInfo(String id, String displayName, String description, String categoryId) {
        this(id, displayName, description, categoryId, null);
    }
    
    /**
     * 获取节点ID
     * @return 节点ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * 获取节点标题
     * @return 节点标题
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 获取节点描述
     * @return 节点描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 获取所属分类ID
     * @return 分类ID
     */
    public String getCategoryId() {
        return categoryId;
    }
    
    /**
     * 获取节点实现类
     * @return 节点实现类
     */
    public Class<? extends INode> getNodeClass() {
        return nodeClass;
    }
    
    /**
     * 获取图标路径
     * @return 图标路径
     */
    public String getIcon() {
        return icon;
    }
    
    /**
     * 设置图标路径
     * @param icon 图标路径
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeInfo nodeInfo = (NodeInfo) o;
        return Objects.equals(id, nodeInfo.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "NodeInfo{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", class=" + (nodeClass != null ? nodeClass.getSimpleName() : "null") +
                '}';
    }
} 