package com.nodecraft.gui.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 节点分类类
 * 表示节点编辑器中的一个分类，包含该分类下的节点信息
 */
public class NodeCategory {
    
    // 分类ID
    private final String id;
    
    // 分类标题
    private final String title;
    
    // 分类描述
    private final String description;
    
    // 分类图标 (可选)
    private String icon;
    
    // 分类下的节点列表
    private final List<NodeInfo> nodes = new ArrayList<>();
    
    /**
     * 构造函数
     * @param id 分类ID
     * @param title 分类标题
     */
    public NodeCategory(String id, String title) {
        this(id, title, "");
    }
    
    /**
     * 构造函数
     * @param id 分类ID
     * @param title 分类标题
     * @param description 分类描述
     */
    public NodeCategory(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }
    
    /**
     * 获取分类ID
     * @return 分类ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * 获取分类标题
     * @return 分类标题
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * 获取分类描述
     * @return 分类描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 获取分类图标
     * @return 分类图标
     */
    public String getIcon() {
        return icon;
    }
    
    /**
     * 设置分类图标
     * @param icon 分类图标
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    /**
     * 获取该分类下的所有节点
     * @return 节点列表
     */
    public List<NodeInfo> getNodes() {
        return new ArrayList<>(nodes);
    }
    
    /**
     * 添加节点到分类
     * @param node 节点信息
     */
    public void addNode(NodeInfo node) {
        if (node != null && !nodes.contains(node)) {
            nodes.add(node);
        }
    }
    
    /**
     * 移除节点
     * @param nodeId 节点ID
     * @return 移除是否成功
     */
    public boolean removeNode(String nodeId) {
        return nodes.removeIf(node -> node.getId().equals(nodeId));
    }
    
    /**
     * 检查分类是否为空
     * @return 是否为空
     */
    public boolean isEmpty() {
        return nodes.isEmpty();
    }
    
    /**
     * 获取节点数量
     * @return 节点数量
     */
    public int getNodeCount() {
        return nodes.size();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeCategory that = (NodeCategory) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "NodeCategory{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", nodes=" + nodes.size() +
                '}';
    }
} 