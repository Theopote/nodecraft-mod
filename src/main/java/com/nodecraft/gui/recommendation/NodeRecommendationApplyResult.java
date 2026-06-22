package com.nodecraft.gui.recommendation;

import java.util.List;
import java.util.UUID;

public record NodeRecommendationApplyResult(
        boolean success,
        UUID createdNodeId,
        List<UUID> createdNodeIds,
        String message
) {
    public static NodeRecommendationApplyResult failure(String message) {
        return new NodeRecommendationApplyResult(false, null, List.of(), message);
    }

    public static NodeRecommendationApplyResult success(UUID primaryId, List<UUID> allIds, String message) {
        return new NodeRecommendationApplyResult(true, primaryId, List.copyOf(allIds), message);
    }
}
