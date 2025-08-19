package com.nodecraft.core.event;

/**
 * 节点相关的编辑器事件
 * 用于处理节点的添加、删除、连接等操作
 */
public class EditorNodeEvent extends EditorEvent {
    
    /**
     * 节点事件类型
     */
    public enum Type {
        NODE_ADDED,      // 节点添加
        NODE_REMOVED,    // 节点删除
        NODE_SELECTED,   // 节点选中
        NODE_DESELECTED, // 节点取消选中
        NODE_MOVED,      // 节点移动
        NODE_CONNECTED,  // 节点连接
        NODE_DISCONNECTED// 节点断开连接
    }
    
    private final Type nodeEventType;
    private final String nodeId;
    private final Object nodeData;
    
    /**
     * 创建一个节点事件
     * @param nodeEventType 节点事件类型
     * @param nodeId 节点ID
     * @param nodeData 节点相关数据
     */
    public EditorNodeEvent(Type nodeEventType, String nodeId, Object nodeData) {
        super(EditorNodeEvent.class.getSimpleName() + "." + nodeEventType);
        this.nodeEventType = nodeEventType;
        this.nodeId = nodeId;
        this.nodeData = nodeData;
    }
    
    /**
     * 创建一个节点事件（无附加数据）
     * @param nodeEventType 节点事件类型
     * @param nodeId 节点ID
     */
    public EditorNodeEvent(Type nodeEventType, String nodeId) {
        this(nodeEventType, nodeId, null);
    }
    
    /**
     * 获取节点事件类型
     * @return 节点事件类型
     */
    public Type getNodeEventType() {
        return nodeEventType;
    }
    
    /**
     * 获取节点ID
     * @return 节点ID
     */
    public String getNodeId() {
        return nodeId;
    }
    
    /**
     * 获取节点数据
     * @return 节点数据
     */
    public Object getNodeData() {
        return nodeData;
    }
    
    @Override
    public String toString() {
        return "EditorNodeEvent{" +
                "type='" + getEventType() + '\'' +
                ", nodeEventType=" + nodeEventType +
                ", nodeId='" + nodeId + '\'' +
                ", nodeData=" + nodeData +
                ", handled=" + isHandled() +
                '}';
    }
} 