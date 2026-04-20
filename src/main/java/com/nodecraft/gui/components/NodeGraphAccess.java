package com.nodecraft.gui.components;

import com.nodecraft.core.NodeCraft;
import com.nodecraft.nodesystem.graph.NodeGraph;
import java.util.function.Supplier;

final class NodeGraphAccess {
    private final Supplier<NodeGraph> graphSupplier;

    NodeGraphAccess(Supplier<NodeGraph> graphSupplier) {
        this.graphSupplier = graphSupplier;
    }

    NodeGraph getCurrentGraph() {
        if (graphSupplier == null) {
            NodeCraft.LOGGER.warn("NodeGraphProvider 未设置, 无法获取节点图。PropertyPanelComponent 可能无法正常工作。");
            return null;
        }

        try {
            return graphSupplier.get();
        } catch (Exception e) {
            NodeCraft.LOGGER.error("NodeGraphProvider 获取图时发生错误", e);
            return null;
        }
    }
}
