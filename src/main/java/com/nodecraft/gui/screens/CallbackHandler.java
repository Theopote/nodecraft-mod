package com.nodecraft.gui.screens;

import com.nodecraft.core.NodeCraft;
import com.nodecraft.gui.components.panel.CanvasComponent;
import com.nodecraft.gui.components.panel.NodeLibraryComponent;
import com.nodecraft.gui.editor.NodeEditorFactory;
import com.nodecraft.gui.editor.impl.ImGuiNodeEditor;
import com.nodecraft.nodesystem.api.INode;

/**
 * 处理来自 UI 组件的回调。
 */
public class CallbackHandler implements 
    NodeLibraryComponent.NodeSelectCallback,
    CanvasComponent.NodeDropCallback {

    public CallbackHandler() {
    }

    public void setStatusMessage(String message, int type) {
        NodeCraft.LOGGER.debug("Status message: [{}] {}", type, message);
    }
    
    @Override
    public void onNodeSelected(String nodeId, String nodeTitle) {
        setStatusMessage("已选择节点: " + nodeTitle, 0);
    }
    
    @Override
    public void onNodeDropped(String nodeId, float x, float y) {
        NodeCraft.LOGGER.debug("节点 {} 被放置在画布上 ({}, {}) (由 CallbackHandler 处理)", nodeId, x, y);
        
        // 获取当前编辑器实例
        ImGuiNodeEditor editor = ImGuiNodeEditor.getInstance();
        if (editor != null) {
            // 调用编辑器的addNode方法创建节点
            INode node = editor.addNode(nodeId, x, y);
            if (node != null) {
                NodeCraft.LOGGER.info("成功创建并添加节点到画布: {}", nodeId);
                setStatusMessage("节点已创建并放置: " + nodeId, 0);
            } else {
                NodeCraft.LOGGER.error("无法创建节点: {}", nodeId);
                setStatusMessage("无法创建节点: " + nodeId, 2); // 使用类型2表示错误
            }
        } else {
            NodeCraft.LOGGER.error("无法获取ImGuiNodeEditor实例");
            setStatusMessage("无法添加节点，编辑器实例不可用", 2);
        }
    }
} 