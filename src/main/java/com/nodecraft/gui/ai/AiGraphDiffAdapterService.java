package com.nodecraft.gui.ai;

import com.nodecraft.nodesystem.graph.NodeGraph;

import java.util.ArrayList;
import java.util.List;

public final class AiGraphDiffAdapterService {

    private AiGraphDiffAdapterService() {
    }

    public record PlanNode(String ref, String typeId, Object nodeState) {
    }

    public record PlanConnection(String sourceRef, String sourcePortId, String targetRef, String targetPortId) {
    }

    public static AiGraphDiffService.GraphDiffSummary buildGraphDiffSummary(
            List<PlanNode> nodes,
            List<PlanConnection> connections,
            NodeGraph graph
    ) {
        return AiGraphDiffService.buildGraphDiffSummary(toDiffGraphPlan(nodes, connections), graph);
    }

    public static AiGraphDiffService.MappedDiffSummary buildMappedDiffSummary(
            List<PlanNode> nodes,
            List<PlanConnection> connections,
            NodeGraph graph
    ) {
        return AiGraphDiffService.buildMappedDiffSummary(toDiffGraphPlan(nodes, connections), graph);
    }

    private static AiGraphDiffService.GraphPlan toDiffGraphPlan(
            List<PlanNode> nodes,
            List<PlanConnection> connections
    ) {
        List<AiGraphDiffService.PlanNode> diffNodes = new ArrayList<>();
        if (nodes != null) {
            diffNodes = new ArrayList<>(nodes.size());
            for (PlanNode node : nodes) {
                diffNodes.add(new AiGraphDiffService.PlanNode(node.ref(), node.typeId(), node.nodeState()));
            }
        }

        List<AiGraphDiffService.PlanConnection> diffConnections = new ArrayList<>();
        if (connections != null) {
            diffConnections = new ArrayList<>(connections.size());
            for (PlanConnection connection : connections) {
                diffConnections.add(new AiGraphDiffService.PlanConnection(
                        connection.sourceRef(),
                        connection.sourcePortId(),
                        connection.targetRef(),
                        connection.targetPortId()
                ));
            }
        }

        return new AiGraphDiffService.GraphPlan(diffNodes, diffConnections);
    }
}
