package com.nodecraft.gui.ai;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.graph.NodeGraph;
import com.nodecraft.gui.editor.base.GraphApplyHistoryView;
import com.nodecraft.gui.editor.base.GraphApplyTarget;
import com.nodecraft.gui.editor.base.GraphNodeAnchor;
import com.nodecraft.nodesystem.api.INode;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiGraphApplyServiceTest {

    @Test
    void applyPatchCreatesNodesAndConnectionsThroughGraphApplyTarget() {
        NodeGraph graph = new NodeGraph("apply-test");
        RecordingGraphApplyTarget applyTarget = new RecordingGraphApplyTarget(graph);

        TestNode source = new TestNode("source");
        TestNode sink = new TestNode("sink");
        graph.addNode(source);
        graph.addNode(sink);

        AiGraphApplyService.ApplyResult result = AiGraphApplyService.applyPatch(
                applyTarget,
                graph,
                List.of(
                        new AiGraphApplyService.ApplyNode("source", "test.apply.source", 0f, 0f, null),
                        new AiGraphApplyService.ApplyNode("new_node", "test.apply.created", 40f, 20f, null)
                ),
                List.of(new AiGraphApplyService.ApplyConnection(
                        "source", "out", "new_node", "in"
                )),
                new float[]{100f, 200f},
                false,
                false
        );

        assertTrue(result.success());
        assertEquals(1, applyTarget.connectCalls);
        assertEquals(1, applyTarget.createdNodes.size());
        assertTrue(graph.getIncomingConnections(sink.getId()).isEmpty());
    }

    private static final class RecordingGraphApplyTarget implements GraphApplyTarget {
        private final NodeGraph graph;
        private final List<INode> createdNodes = new ArrayList<>();
        private int connectCalls = 0;
        private int undoCalls = 0;

        private RecordingGraphApplyTarget(NodeGraph graph) {
            this.graph = graph;
        }

        @Override
        public INode addNode(String typeId, float x, float y) {
            TestNode node = new TestNode(typeId);
            graph.addNode(node);
            createdNodes.add(node);
            return node;
        }

        @Override
        public INode addNodeWithState(String typeId, UUID oldNodeId, float x, float y, Object nodeState) {
            return addNode(typeId, x, y);
        }

        @Override
        public boolean connectPorts(UUID sourceNodeId, String sourcePortId, UUID targetNodeId, String targetPortId) {
            connectCalls++;
            return graph.connect(sourceNodeId, sourcePortId, targetNodeId, targetPortId);
        }

        @Override
        public boolean disconnectPorts(UUID sourceNodeId, String sourcePortId, UUID targetNodeId, String targetPortId) {
            return graph.disconnectPorts(sourceNodeId, sourcePortId, targetNodeId, targetPortId);
        }

        @Override
        public boolean undo() {
            undoCalls++;
            return true;
        }

        @Override
        public void recordAiPatchApply(String summary, Map<UUID, Object> previousStates, int undoStepsTaken) {
        }

        @Override
        public GraphApplyHistoryView getApplyHistoryView() {
            return GraphApplyHistoryView.EMPTY;
        }

        @Override
        public GraphNodeAnchor getNodeAnchor(UUID nodeId) {
            return null;
        }
    }

    private static final class TestNode extends BaseNode {
        private TestNode(String suffix) {
            super(UUID.randomUUID(), "test.apply." + suffix);
            addInputPort(new BasePort("in", "In", "input", NodeDataType.ANY, this));
            addOutputPort(new BasePort("out", "Out", "output", NodeDataType.ANY, this));
        }

        @Override
        public void processNode(@Nullable com.nodecraft.nodesystem.execution.ExecutionContext context) {
            outputValues.put("out", inputValues.get("in"));
        }
    }
}
