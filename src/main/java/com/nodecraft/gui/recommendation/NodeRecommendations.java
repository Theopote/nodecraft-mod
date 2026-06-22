package com.nodecraft.gui.recommendation;

public final class NodeRecommendations {

    private static final NodeRecommendationService INSTANCE = new DefaultNodeRecommendationService();

    private NodeRecommendations() {
    }

    public static NodeRecommendationService get() {
        return INSTANCE;
    }
}
