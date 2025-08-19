package com.nodecraft.gui.editor.impl;

import java.util.Map;
import java.util.UUID;
import com.nodecraft.nodesystem.graph.NodeGraph;
import com.nodecraft.nodesystem.api.INode;
import imgui.ImVec2;
import org.jetbrains.annotations.Nullable;

/**
 * 定义与画布编辑器交互所需的接口
 */
public interface ICanvasEditor {
    float getCanvasZoom();
    float getCanvasOffsetX();
    float getCanvasOffsetY();
    NodeGraph getCurrentGraph();
    UUID getSelectedNodeId();
    void setSelectedNodeId(UUID nodeId);
    boolean connectPorts(UUID sourceNodeId, String sourcePortId, UUID targetNodeId, String targetPortId);
    boolean disconnectPorts(UUID sourceNodeId, String sourcePortId, UUID targetNodeId, String targetPortId);
    
    // 添加 ImGuiNodeIO 需要的方法
    Map<UUID, NodePosition> getNodePositions();
    NodePosition getNodePosition(UUID nodeId);
    void setCurrentGraph(NodeGraph graph);
    void setNodePositions(Map<UUID, NodePosition> positions);
    
    // 添加 ImGuiNodeMenus 可能需要的方法
    boolean isShowGrid();
    void setShowGrid(boolean showGrid);
    void setCanvasZoom(float zoom);
    void setCanvasOffset(float x, float y);
    void clearNodePositions();
    void clearSelectedNodes();
    void removeSelectedNode(UUID nodeId);
    void removeNodePosition(UUID nodeId);
    UUID getNodeIdUnderMouse(float mouseX, float mouseY);
    void close();
    INode addNode(String nodeTypeId, float x, float y);
    
    /**
     * 根据节点类型ID、指定UUID和初始状态创建并添加一个节点到图中。
     * 此方法主要用于历史记录的撤销/重做功能，以恢复特定ID和状态的节点。
     *
     * @param nodeTypeId 节点的类型ID。
     * @param oldNodeId 节点的旧UUID（仅用于历史记录内部映射，新创建的节点会有新UUID），如果为null，将自动生成新的UUID。
     * @param x 节点的初始世界X坐标。
     * @param y 节点的初始世界Y坐标。
     * @param nodeState 节点的初始状态数据，将通过 setNodeState 应用。如果为null，则使用节点默认状态。
     * @return 新创建或恢复的节点实例，如果失败则返回null。
     */
    INode addNodeWithState(String nodeTypeId, @Nullable UUID oldNodeId, float x, float y, @Nullable Object nodeState);
    
    java.util.Set<UUID> getSelectedNodeIds();
    void setCanvasView(float zoom, float offsetX, float offsetY);
    void pasteNodesAtPosition(float x, float y);
    
    /**
     * 获取节点交互组件
     * @return 节点交互组件实例
     */
    ImGuiNodeInteraction getInteraction();
    
    /**
     * 获取端口屏幕位置映射
     * @return 端口屏幕位置映射
     */
    Map<UUID, Map<String, ImVec2>> getPortScreenPositions();
    
    /**
     * 获取节点IO组件
     * @return 节点IO组件实例
     */
    ImGuiNodeIO getNodeIO();
    
    /**
     * 获取历史记录组件
     * @return 历史记录组件实例
     */
    ImGuiNodeHistory getHistory();
    
    /**
     * 获取剪贴板组件
     * @return 剪贴板组件实例
     */
    ImGuiNodeClipboard getClipboard();
    
    /**
     * 撤销操作
     * @return 是否成功撤销
     */
    boolean undo();
    
    /**
     * 重做操作
     * @return 是否成功重做
     */
    boolean redo();
    
    /**
     * 复制选中的节点
     * @return 是否成功复制
     */
    boolean copySelectedNodes();
    
    /**
     * 剪切选中的节点
     * @return 是否成功剪切
     */
    boolean cutSelectedNodes();
    
    /**
     * 在指定位置粘贴节点
     * @param x 粘贴位置的X坐标
     * @param y 粘贴位置的Y坐标
     * @return 是否成功粘贴
     */
    boolean pasteNodesAt(float x, float y);
    
    /**
     * 删除选中的节点
     * @return 是否成功删除
     */
    boolean deleteSelectedNodes();
    
    /**
     * 检查是否有未保存的更改
     * @return 是否有未保存的更改
     */
    boolean hasUnsavedChanges();
    
    /**
     * 复制选中的节点
     * @return 是否成功复制
     */
    boolean duplicateSelectedNode();

    // === 节点颜色管理方法 ===

    /**
     * 设置节点的自定义颜色
     * @param nodeId 节点ID
     * @param color 颜色值（ImGui格式的整数颜色）
     */
    void setNodeCustomColor(UUID nodeId, int color);

    /**
     * 获取节点的自定义颜色
     * @param nodeId 节点ID
     * @return 自定义颜色，如果没有设置则返回null
     */
    Integer getNodeCustomColor(UUID nodeId);

    /**
     * 移除节点的自定义颜色
     * @param nodeId 节点ID
     */
    void removeNodeCustomColor(UUID nodeId);

    /**
     * 检查节点是否有自定义颜色
     * @param nodeId 节点ID
     * @return 是否有自定义颜色
     */
    boolean hasNodeCustomColor(UUID nodeId);

    // === 节点状态管理方法 ===

    /**
     * 切换节点的禁用状态
     * @param nodeId 节点ID
     * @return 切换后的状态（true=禁用，false=启用）
     */
    boolean toggleNodeDisabled(UUID nodeId);

    /**
     * 设置节点的禁用状态
     * @param nodeId 节点ID
     * @param disabled 是否禁用
     */
    void setNodeDisabled(UUID nodeId, boolean disabled);

    /**
     * 检查节点是否被禁用
     * @param nodeId 节点ID
     * @return 是否被禁用
     */
    boolean isNodeDisabled(UUID nodeId);

    /**
     * 切换节点的可见性状态
     * @param nodeId 节点ID
     * @return 切换后的状态（true=可见，false=隐藏）
     */
    boolean toggleNodeVisible(UUID nodeId);

    /**
     * 设置节点的可见性状态
     * @param nodeId 节点ID
     * @param visible 是否可见
     */
    void setNodeVisible(UUID nodeId, boolean visible);

    /**
     * 检查节点是否可见
     * @param nodeId 节点ID
     * @return 是否可见
     */
    boolean isNodeVisible(UUID nodeId);
} 