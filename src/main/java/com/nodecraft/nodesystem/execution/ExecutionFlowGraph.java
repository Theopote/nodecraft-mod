package com.nodecraft.nodesystem.execution;

import com.nodecraft.nodesystem.api.INode;
import com.nodecraft.nodesystem.graph.NodeGraph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Exec-edge topology extracted from a {@link NodeGraph}.
 *
 * <p>Data edges are ignored here. When {@link #hasExecEdges()} is false, {@link NodeExecutor}
 * falls back to pure dataflow scheduling.</p>
 */
public final class ExecutionFlowGraph {

    private final boolean hasExecEdges;
    private final Set<UUID> entryNodeIds;
    private final Map<UUID, Set<UUID>> execSuccessors;

    private ExecutionFlowGraph(boolean hasExecEdges, Set<UUID> entryNodeIds, Map<UUID, Set<UUID>> execSuccessors) {
        this.hasExecEdges = hasExecEdges;
        this.entryNodeIds = Set.copyOf(entryNodeIds);
        this.execSuccessors = copySuccessorMap(execSuccessors);
    }

    public static ExecutionFlowGraph analyze(NodeGraph graph) {
        if (graph == null) {
            return new ExecutionFlowGraph(false, Set.of(), Map.of());
        }

        Map<UUID, Set<UUID>> successors = new HashMap<>();
        Set<UUID> nodesWithIncomingExec = new HashSet<>();
        Set<UUID> execParticipatingNodes = new HashSet<>();
        boolean hasExecEdges = false;

        for (INode node : graph.getNodes()) {
            successors.computeIfAbsent(node.getId(), ignored -> new LinkedHashSet<>());
        }

        for (NodeGraph.Connection connection : graph.getConnections()) {
            if (!ExecutionPortKind.isExecConnection(connection)) {
                continue;
            }
            hasExecEdges = true;
            UUID sourceId = connection.sourceNode.getId();
            UUID targetId = connection.targetNode.getId();
            execParticipatingNodes.add(sourceId);
            execParticipatingNodes.add(targetId);
            successors.computeIfAbsent(sourceId, ignored -> new LinkedHashSet<>()).add(targetId);
            nodesWithIncomingExec.add(targetId);
        }

        if (!hasExecEdges) {
            return new ExecutionFlowGraph(false, Set.of(), Map.of());
        }

        Set<UUID> entryNodes = new LinkedHashSet<>();
        for (UUID nodeId : execParticipatingNodes) {
            if (!nodesWithIncomingExec.contains(nodeId)) {
                entryNodes.add(nodeId);
            }
        }

        return new ExecutionFlowGraph(true, entryNodes, successors);
    }

    public boolean hasExecEdges() {
        return hasExecEdges;
    }

    public Set<UUID> entryNodeIds() {
        return entryNodeIds;
    }

    public Set<UUID> execSuccessors(UUID nodeId) {
        if (nodeId == null) {
            return Set.of();
        }
        return execSuccessors.getOrDefault(nodeId, Set.of());
    }

    /**
     * Returns exec nodes reachable from entry nodes (including entries).
     */
    public Set<UUID> reachableExecNodeIds() {
        if (!hasExecEdges) {
            return Set.of();
        }

        Set<UUID> reachable = new LinkedHashSet<>();
        ArrayDeque<UUID> queue = new ArrayDeque<>(entryNodeIds);
        while (!queue.isEmpty()) {
            UUID current = queue.removeFirst();
            if (!reachable.add(current)) {
                continue;
            }
            for (UUID next : execSuccessors(current)) {
                if (!reachable.contains(next)) {
                    queue.addLast(next);
                }
            }
        }
        return Set.copyOf(reachable);
    }

    private static Map<UUID, Set<UUID>> copySuccessorMap(Map<UUID, Set<UUID>> source) {
        Map<UUID, Set<UUID>> copy = new HashMap<>();
        for (Map.Entry<UUID, Set<UUID>> entry : source.entrySet()) {
            copy.put(entry.getKey(), Set.copyOf(entry.getValue()));
        }
        return Map.copyOf(copy);
    }
}
