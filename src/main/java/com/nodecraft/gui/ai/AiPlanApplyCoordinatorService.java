package com.nodecraft.gui.ai;

import com.nodecraft.core.NodeCraft;
import com.nodecraft.gui.editor.base.GraphApplyTarget;
import com.nodecraft.nodesystem.api.INode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AiPlanApplyCoordinatorService {

    private AiPlanApplyCoordinatorService() {
    }

    public record PlanNode(String ref, String typeId, float offsetX, float offsetY, Object nodeState) {
    }

    public record PlanConnection(String sourceRef, String sourcePortId, String targetRef, String targetPortId) {
    }

    public record ApplyResult(boolean success, int undoSteps, String statusMessage, int createdNodes, int connectedEdges) {
    }

    public static ApplyResult applyExact(
            GraphApplyTarget applyTarget,
            List<PlanNode> nodesToApply,
            List<PlanConnection> connections,
            float[] anchor
    ) {
        if (applyTarget == null) {
            return new ApplyResult(false, 0, "Failed to apply plan: editor unavailable.", 0, 0);
        }
        if (nodesToApply == null) {
            nodesToApply = List.of();
        }
        if (connections == null) {
            connections = List.of();
        }
        if (anchor == null || anchor.length < 2) {
            anchor = new float[]{0.0f, 0.0f};
        }

        Map<String, UUID> createdNodeIds = new HashMap<>();
        int undoSteps = 0;
        int successfulConnections = 0;

        try {
            for (PlanNode node : nodesToApply) {
                float x = anchor[0] + node.offsetX();
                float y = anchor[1] + node.offsetY();
                INode created = node.nodeState() == null
                        ? applyTarget.addNode(node.typeId(), x, y)
                        : applyTarget.addNodeWithState(node.typeId(), null, x, y, node.nodeState());

                if (created == null) {
                    rollback(applyTarget, undoSteps);
                    return new ApplyResult(false, 0,
                            "Failed to create node: " + node.ref() + " (" + node.typeId() + "). Auto-rolled back.",
                            0,
                            0);
                }
                createdNodeIds.put(node.ref(), created.getId());
                undoSteps++;
            }

            for (PlanConnection connection : connections) {
                UUID sourceNodeId = createdNodeIds.get(connection.sourceRef());
                UUID targetNodeId = createdNodeIds.get(connection.targetRef());
                if (sourceNodeId == null || targetNodeId == null) {
                    rollback(applyTarget, undoSteps);
                    return new ApplyResult(false, 0,
                            "Connection failed due to missing node ref: "
                                    + connection.sourceRef() + " -> " + connection.targetRef() + ". Auto-rolled back.",
                            0,
                            0);
                }

                boolean connected = applyTarget.connectPorts(
                        sourceNodeId,
                        connection.sourcePortId(),
                        targetNodeId,
                        connection.targetPortId()
                );
                if (!connected) {
                    NodeCraft.LOGGER.warn("AI plan connection failed and will rollback: {}.{} -> {}.{}",
                            connection.sourceRef(), connection.sourcePortId(),
                            connection.targetRef(), connection.targetPortId());
                    rollback(applyTarget, undoSteps);
                    return new ApplyResult(false, 0,
                            "Failed to connect: "
                                    + connection.sourceRef() + "." + connection.sourcePortId()
                                    + " -> " + connection.targetRef() + "." + connection.targetPortId()
                                    + ". Auto-rolled back.",
                            0,
                            0);
                }

                successfulConnections++;
                undoSteps++;
            }

            return new ApplyResult(
                    true,
                    undoSteps,
                    "Applied AI plan: created " + createdNodeIds.size()
                            + " nodes, connected " + successfulConnections
                            + ". Undo steps available: " + undoSteps + ".",
                    createdNodeIds.size(),
                    successfulConnections
            );
        } catch (Exception e) {
            rollback(applyTarget, undoSteps);
            NodeCraft.LOGGER.error("Failed to apply AI plan", e);
            return new ApplyResult(false, 0, "Failed to apply plan: " + e.getMessage() + ". Auto-rolled back.", 0, 0);
        }
    }

    public static int undo(GraphApplyTarget applyTarget, int undoSteps) {
        if (applyTarget == null || undoSteps <= 0) {
            return 0;
        }

        int undone = 0;
        for (int i = 0; i < undoSteps; i++) {
            if (!applyTarget.undo()) {
                break;
            }
            undone++;
        }
        return undone;
    }

    private static int rollback(GraphApplyTarget applyTarget, int undoSteps) {
        return undo(applyTarget, undoSteps);
    }
}
