package com.nodecraft.gui.recommendation;

import com.nodecraft.nodesystem.api.NodeDataType;

public record NodeRecommendation(
        String nodeId,
        String displayName,
        String categoryId,
        String connectPortId,
        NodeDataType connectPortType,
        String sourcePortId,
        NodeDataType sourcePortType,
        ConnectionPlan connectionPlan,
        int score,
        String reason
) {
    public enum ConnectionPlan {
        DIRECT,
        VIA_CONVERSION,
        MANUAL
    }
}
