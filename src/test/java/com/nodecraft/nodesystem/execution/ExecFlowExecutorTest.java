package com.nodecraft.nodesystem.execution;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.graph.NodeGraph;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecFlowExecutorTest {

    @Test
    void execFlowSkipsNodesOutsideExecFrontier() {
        NodeGraph graph = new NodeGraph("exec-skip");
        ExecStepNode entry = new ExecStepNode("entry");
        ExecStepNode middle = new ExecStepNode("middle");
        ExecStepNode orphan = new ExecStepNode("orphan");

        graph.addNode(entry);
        graph.addNode(middle);
        graph.addNode(orphan);
        graph.connect(entry.getId(), "exec_out", middle.getId(), "exec_in");

        assertTrue(new NodeExecutor(graph).executeSync());
        assertEquals(1, entry.executions());
        assertEquals(1, middle.executions());
        assertEquals(0, orphan.executions());
    }

    @Test
    void execFlowPullsDataUpstreamOutsideExecFrontier() {
        NodeGraph graph = new NodeGraph("exec-data-pull");
        PassThroughNode source = new PassThroughNode("source", "value");
        ExecStepNode entry = new ExecStepNode("entry");
        CaptureNode sink = new CaptureNode("sink");

        graph.addNode(source);
        graph.addNode(entry);
        graph.addNode(sink);
        graph.connect(source.getId(), "out", sink.getId(), "in");
        graph.connect(entry.getId(), "exec_out", sink.getId(), "exec_in");

        assertTrue(new NodeExecutor(graph).executeSync());
        assertEquals(1, entry.executions());
        assertEquals(1, source.executions());
        assertEquals(1, sink.executions());
        assertEquals("value", sink.getOutput("out"));
    }

    @Test
    void execFlowGuardStopsInfiniteExecCycle() {
        NodeGraph graph = new NodeGraph("exec-cycle");
        ExecStepNode nodeA = new ExecStepNode("a");
        ExecStepNode nodeB = new ExecStepNode("b");
        graph.addNode(nodeA);
        graph.addNode(nodeB);
        graph.connect(nodeA.getId(), "exec_out", nodeB.getId(), "exec_in");
        graph.connect(nodeB.getId(), "exec_out", nodeA.getId(), "exec_in");

        ExecutionRunLimits limits = new ExecutionRunLimits(5L, 5_000L);
        assertFalse(new NodeExecutor(graph, null, null, null, limits).executeSync());
    }

    private static final class ExecStepNode extends BaseNode {
        private final AtomicInteger executions = new AtomicInteger();

        private ExecStepNode(String suffix) {
            super(UUID.randomUUID(), "test.exec." + suffix);
            addInputPort(new BasePort("exec_in", "Exec In", "input", NodeDataType.EXEC, this, false, false));
            addOutputPort(new BasePort("exec_out", "Exec Out", "output", NodeDataType.EXEC, this));
        }

        @Override
        public void processNode(@Nullable ExecutionContext context) {
            executions.incrementAndGet();
            outputValues.put("exec_out", Boolean.TRUE);
        }

        int executions() {
            return executions.get();
        }
    }

    private static final class PassThroughNode extends BaseNode {
        private final Object payload;

        private PassThroughNode(String suffix, Object payload) {
            super(UUID.randomUUID(), "test.pass." + suffix);
            this.payload = payload;
            addOutputPort(new BasePort("out", "Out", "output", NodeDataType.ANY, this));
        }

        private final AtomicInteger executions = new AtomicInteger();

        @Override
        public void processNode(@Nullable ExecutionContext context) {
            executions.incrementAndGet();
            outputValues.put("out", payload);
        }

        int executions() {
            return executions.get();
        }
    }

    private static final class CaptureNode extends BaseNode {
        private final AtomicInteger executions = new AtomicInteger();

        private CaptureNode(String suffix) {
            super(UUID.randomUUID(), "test.capture." + suffix);
            addInputPort(new BasePort("exec_in", "Exec In", "input", NodeDataType.EXEC, this, false, false));
            addInputPort(new BasePort("in", "In", "input", NodeDataType.ANY, this, false, false));
            addOutputPort(new BasePort("out", "Out", "output", NodeDataType.ANY, this));
        }

        @Override
        public void processNode(@Nullable ExecutionContext context) {
            executions.incrementAndGet();
            outputValues.put("out", inputValues.get("in"));
        }

        int executions() {
            return executions.get();
        }
    }
}
