package com.nodecraft.gui.recommendation;

import com.nodecraft.nodesystem.api.NodeDataType;
import java.util.UUID;

public record NodeRecommendationContext(
        RecommendationTrigger trigger,
        RecommendationDirection direction,
        UUID sourceNodeId,
        String sourcePortId,
        NodeDataType sourceDataType,
        float placementX,
        float placementY,
        Integer limit
) {
    public static NodeRecommendationContext forSelectedNode(UUID nodeId, float x, float y, int limit) {
        return new NodeRecommendationContext(
                RecommendationTrigger.SELECTION_PANEL,
                RecommendationDirection.DOWNSTREAM,
                nodeId,
                null,
                null,
                x,
                y,
                limit);
    }

    public static NodeRecommendationContext forPortDrag(
            UUID nodeId,
            String portId,
            NodeDataType dataType,
            boolean fromOutput,
            float x,
            float y) {
        return new NodeRecommendationContext(
                RecommendationTrigger.PORT_DRAG,
                fromOutput ? RecommendationDirection.DOWNSTREAM : RecommendationDirection.UPSTREAM,
                nodeId,
                portId,
                dataType,
                x,
                y,
                null);
    }
}
