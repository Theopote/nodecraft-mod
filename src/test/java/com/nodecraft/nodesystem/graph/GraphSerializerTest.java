package com.nodecraft.nodesystem.graph;

import com.nodecraft.gui.node.NodeInfo;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.execution.NodeExecutor;
import com.nodecraft.nodesystem.io.SavedGraph;
import com.nodecraft.nodesystem.io.SavedNode;
import com.nodecraft.nodesystem.nodes.math.logic.IfNode;
import com.nodecraft.nodesystem.nodes.variable.SetVariableNode;
import com.nodecraft.nodesystem.registry.NodeRegistry;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphSerializerTest {

    private final NodeRegistry registry = NodeRegistry.getInstance();

    @BeforeEach
    void registerNodes() {
        registry.clear();
        registry.registerNode(new NodeInfo("test.pass", "Pass", "pass-through test node", "test", 0, PassNode.class));
        registry.registerNode(new NodeInfo("math.logic.if", "If", "if node", "math.logic", 0, IfNode.class));
        registry.registerNode(new NodeInfo("variable.set", "Set Variable", "set variable", "variable", 0, SetVariableNode.class));
    }

    @AfterEach
    void clearRegistry() {
        registry.clear();
    }

    @Test
    void jsonRoundTripPreservesGraphStructure() {
        NodeGraph original = new NodeGraph("castle gate");
        PassNode source = new PassNode();
        PassNode sink = new PassNode();
        original.addNode(source);
        original.addNode(sink);
        original.connect(source.getId(), "out", sink.getId(), "in");

        String json = GraphSerializer.toJson(original);
        NodeGraph loaded = GraphSerializer.fromJsonToGraph(json);

        assertNotNull(loaded);
        assertEquals("castle gate", loaded.getName());
        assertEquals(2, loaded.getNodes().size());
        assertEquals(1, loaded.getConnections().size());
    }

    @Test
    void roundTripRestoresNodeStateAndExecutionBehavior() {
        NodeGraph original = new NodeGraph("stateful");
        SetVariableNode setter = new SetVariableNode();
        setter.setNodeState(Map.of("defaultName", "count"));
        PassNode value = new PassNode();
        original.addNode(value);
        original.addNode(setter);
        original.connect(value.getId(), "out", setter.getId(), "input_value");

        String json = GraphSerializer.toJson(original);
        NodeGraph loaded = GraphSerializer.fromJsonToGraph(json);

        SetVariableNode loadedSetter = loaded.getNodes().stream()
            .filter(SetVariableNode.class::isInstance)
            .map(SetVariableNode.class::cast)
            .findFirst()
            .orElseThrow();

        ExecutionContext context = ExecutionContext.createEmpty(null);
        assertTrue(new NodeExecutor(loaded, context).executeSync());
        assertEquals(7, context.getVariable("count"));

        Object state = loadedSetter.getNodeState();
        assertTrue(state instanceof Map<?, ?> map && "count".equals(map.get("defaultName")));
    }

    @Test
    void ifChainSurvivesSavedGraphRoundTrip() {
        NodeGraph original = new NodeGraph("if-chain");
        PassNode trueValue = new PassNode();
        PassNode falseValue = new PassNode();
        IfNode ifNode = new IfNode();
        original.addNode(trueValue);
        original.addNode(falseValue);
        original.addNode(ifNode);
        original.connect(trueValue.getId(), "out", ifNode.getId(), "input_true_value");
        original.connect(falseValue.getId(), "out", ifNode.getId(), "input_false_value");

        NodeGraph loaded = GraphSerializer.fromSavedGraph(GraphSerializer.toSavedGraph(original));
        List<PassNode> passNodes = loaded.getNodes().stream()
            .filter(PassNode.class::isInstance)
            .map(PassNode.class::cast)
            .toList();
        assertEquals(2, passNodes.size());
        IfNode loadedIf = loaded.getNodes().stream()
            .filter(IfNode.class::isInstance)
            .map(IfNode.class::cast)
            .findFirst()
            .orElseThrow();

        passNodes.get(0).compute(Map.of("in", "yes"));
        passNodes.get(1).compute(Map.of("in", "no"));
        loadedIf.compute(Map.of(
            "input_condition", true,
            "input_true_value", passNodes.get(0).getOutput("out"),
            "input_false_value", passNodes.get(1).getOutput("out")
        ));
        assertEquals("yes", loadedIf.getOutput("output_result"));
    }

    @Test
    void migrateCompatibilityNodesRemapsLegacyTypeIds() {
        SavedGraph savedGraph = new SavedGraph();
        savedGraph.graphName = "legacy";
        SavedNode legacyNode = new SavedNode();
        legacyNode.nodeId = UUID.randomUUID().toString();
        legacyNode.typeId = "input.numeric.boolean_toggle";
        savedGraph.nodes = List.of(legacyNode);

        GraphSerializer.MigrationReport report = GraphSerializer.migrateCompatibilityNodes(savedGraph);

        assertTrue(report.hasChanges());
        assertEquals(1, report.migratedNodeCount());
        assertEquals("input.basic.boolean_toggle", legacyNode.typeId);
        assertTrue(report.migratedTypeIds().stream().anyMatch(entry -> entry.contains("input.numeric.boolean_toggle")));
    }

    public static final class PassNode extends BaseNode {
        public PassNode() {
            super(UUID.randomUUID(), "test.pass");
            addInputPort(new BasePort("in", "In", "input", NodeDataType.ANY, this));
            addOutputPort(new BasePort("out", "Out", "output", NodeDataType.ANY, this));
        }

        @Override
        public void processNode(@Nullable ExecutionContext context) {
            Object incoming = inputValues.get("in");
            outputValues.put("out", incoming != null ? incoming : 7);
        }
    }
}
