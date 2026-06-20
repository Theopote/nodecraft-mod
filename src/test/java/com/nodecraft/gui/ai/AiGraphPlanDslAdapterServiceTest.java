package com.nodecraft.gui.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiGraphPlanDslAdapterServiceTest {

    @Test
    void planSurvivesDslJsonRoundTrip() {
        Map<String, Object> sourceParams = new LinkedHashMap<>();
        sourceParams.put("value", 2.5d);
        sourceParams.put("badNumber", Double.POSITIVE_INFINITY);

        AiGraphPlanDslAdapterService.GraphPlan plan = new AiGraphPlanDslAdapterService.GraphPlan(
                "round trip",
                List.of(
                        new AiGraphPlanDslAdapterService.PlanNode("source", "math.scalar", 10.0f, 20.0f, sourceParams),
                        new AiGraphPlanDslAdapterService.PlanNode("target", "output.debug", 300.0f, 20.0f, Map.of())
                ),
                List.of(new AiGraphPlanDslAdapterService.PlanConnection("source", "out", "target", "in")),
                List.of()
        );

        String json = AiGraphPlanDslAdapterService.toDslJson(plan);
        AiGraphDslSupport.DslGraph dslGraph = parseDslGraph(json);
        AiGraphPlanDslAdapterService.GraphPlan restored = AiGraphPlanDslAdapterService.fromDsl(dslGraph);

        assertEquals(plan.summary(), restored.summary());
        assertEquals(2, restored.nodes().size());
        assertEquals("source", restored.nodes().get(0).ref());
        assertEquals("math.scalar", restored.nodes().get(0).typeId());
        assertEquals(10.0f, restored.nodes().get(0).offsetX(), 0.001f);
        assertEquals(20.0f, restored.nodes().get(0).offsetY(), 0.001f);
        assertEquals(2.5d, ((Map<?, ?>) restored.nodes().get(0).nodeState()).get("value"));
        assertEquals(0.0d, ((Map<?, ?>) restored.nodes().get(0).nodeState()).get("badNumber"));
        assertEquals(1, restored.connections().size());
        assertEquals("source", restored.connections().get(0).sourceRef());
        assertEquals("out", restored.connections().get(0).sourcePortId());
        assertEquals("target", restored.connections().get(0).targetRef());
        assertEquals("in", restored.connections().get(0).targetPortId());
        assertTrue(restored.validationErrors().isEmpty());
    }

    @Test
    void extractsDslJsonFromFencedModelResponse() {
        String compactJson = AiGraphPlanDslAdapterService.toDslJsonCompact(new AiGraphPlanDslAdapterService.GraphPlan(
                "extract",
                List.of(new AiGraphPlanDslAdapterService.PlanNode("n1", "debug.node", 0.0f, 0.0f, Map.of())),
                List.of(),
                List.of()
        ));
        String response = "Here is the graph:\n```json\n" + compactJson + "\n```";

        String extracted = AiGraphDslSupport.extractJsonPayload(response);

        assertFalse(extracted.isBlank());
        assertEquals("extract", JsonParser.parseString(extracted).getAsJsonObject().get("description").getAsString());
    }

    private static AiGraphDslSupport.DslGraph parseDslGraph(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        List<AiGraphDslSupport.DslNode> nodes = new ArrayList<>();
        JsonArray nodeArray = root.getAsJsonArray("nodes");
        for (int i = 0; i < nodeArray.size(); i++) {
            JsonObject node = nodeArray.get(i).getAsJsonObject();
            JsonObject position = node.getAsJsonObject("position");
            Map<String, Object> params = new LinkedHashMap<>();
            for (Map.Entry<String, com.google.gson.JsonElement> entry : node.getAsJsonObject("params").entrySet()) {
                params.put(entry.getKey(), entry.getValue().getAsDouble());
            }
            nodes.add(new AiGraphDslSupport.DslNode(
                    node.get("id").getAsString(),
                    node.get("type").getAsString(),
                    params,
                    new AiGraphDslSupport.DslPosition(
                            position.get("x").getAsFloat(),
                            position.get("y").getAsFloat()
                    )
            ));
        }

        List<AiGraphDslSupport.DslConnection> connections = new ArrayList<>();
        JsonArray connectionArray = root.getAsJsonArray("connections");
        for (int i = 0; i < connectionArray.size(); i++) {
            JsonObject connection = connectionArray.get(i).getAsJsonObject();
            JsonObject from = connection.getAsJsonObject("from");
            JsonObject to = connection.getAsJsonObject("to");
            connections.add(new AiGraphDslSupport.DslConnection(
                    new AiGraphDslSupport.DslEndpoint(from.get("nodeId").getAsString(), from.get("port").getAsString()),
                    new AiGraphDslSupport.DslEndpoint(to.get("nodeId").getAsString(), to.get("port").getAsString())
            ));
        }

        return new AiGraphDslSupport.DslGraph(nodes, connections, root.get("description").getAsString());
    }
}
