package com.nodecraft.nodesystem.execution;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.graph.NodeGraph;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncrementalExecutionPlannerTest {

    @Test
    void resolveInvalidationScopeIncludesChangedNodeAndDownstream() {
        NodeGraph graph = linearGraph("single-dirty");
        UUID sourceId = graph.getNodes().get(0).getId();
        UUID middleId = graph.getNodes().get(1).getId();
        UUID sinkId = graph.getNodes().get(2).getId();

        Set<UUID> scope = IncrementalExecutionPlanner.resolveInvalidationScope(graph, sourceId);

        assertEquals(Set.of(sourceId, middleId, sinkId), scope);
    }

    @Test
    void resolveInvalidationScopeMergesMultipleDirtyNodes() {
        NodeGraph graph = forkGraph("merge-dirty");
        UUID leftSourceId = graph.getNodes().get(0).getId();
        UUID rightSourceId = graph.getNodes().get(1).getId();
        UUID leftSinkId = graph.getNodes().get(2).getId();
        UUID rightSinkId = graph.getNodes().get(3).getId();

        Set<UUID> scope = IncrementalExecutionPlanner.resolveInvalidationScope(
                graph,
                List.of(leftSourceId, rightSourceId)
        );

        assertEquals(Set.of(leftSourceId, rightSourceId, leftSinkId, rightSinkId), scope);
    }

    @Test
    void resolveInvalidationScopeReturnsEmptyForUnknownNode() {
        NodeGraph graph = linearGraph("unknown");

        assertTrue(IncrementalExecutionPlanner.resolveInvalidationScope(graph, UUID.randomUUID()).isEmpty());
    }

    private static NodeGraph linearGraph(String name) {
        NodeGraph graph = new NodeGraph(name);
        StubNode source = new StubNode("source");
        StubNode middle = new StubNode("middle");
        StubNode sink = new StubNode("sink");
        graph.addNode(source);
        graph.addNode(middle);
        graph.addNode(sink);
        graph.connect(source.getId(), "out", middle.getId(), "in");
        graph.connect(middle.getId(), "out", sink.getId(), "in");
        return graph;
    }

    private static NodeGraph forkGraph(String name) {
        NodeGraph graph = new NodeGraph(name);
        StubNode leftSource = new StubNode("left-source");
        StubNode rightSource = new StubNode("right-source");
        StubNode leftSink = new StubNode("left-sink");
        StubNode rightSink = new StubNode("right-sink");
        graph.addNode(leftSource);
        graph.addNode(rightSource);
        graph.addNode(leftSink);
        graph.addNode(rightSink);
        graph.connect(leftSource.getId(), "out", leftSink.getId(), "in");
        graph.connect(rightSource.getId(), "out", rightSink.getId(), "in");
        return graph;
    }

    private static final class StubNode extends BaseNode {
        private StubNode(String suffix) {
            super(UUID.randomUUID(), "test.stub." + suffix);
            addInputPort(new BasePort("in", "In", "input", NodeDataType.ANY, this));
            addOutputPort(new BasePort("out", "Out", "output", NodeDataType.ANY, this));
        }

        @Override
        public void processNode(@Nullable ExecutionContext context) {
            outputValues.put("out", "value");
        }
    }
}
