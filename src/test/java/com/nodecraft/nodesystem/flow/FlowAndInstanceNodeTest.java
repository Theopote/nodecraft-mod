package com.nodecraft.nodesystem.flow;

import com.nodecraft.nodesystem.nodes.pattern.linear.InstanceOnPointsNode;
import com.nodecraft.nodesystem.nodes.world.query.FilterPointsByRuleNode;
import com.nodecraft.nodesystem.util.BlockPlacementData;
import com.nodecraft.nodesystem.util.BlockPosList;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class FlowAndInstanceNodeTest {

    @Test
    void instanceOnPointsCopiesTemplateAtEveryAnchor() {
        InstanceOnPointsNode node = new InstanceOnPointsNode();
        BlockPosList anchors = new BlockPosList(List.of(new BlockPos(10, 64, 10), new BlockPos(20, 64, 20)));
        List<BlockPlacementData> template = List.of(
                new BlockPlacementData(new BlockPos(0, 0, 0), "minecraft:oak_fence"),
                new BlockPlacementData(new BlockPos(0, 1, 0), "minecraft:lantern")
        );

        Map<String, Object> outputs = node.compute(Map.of(
                "input_points", anchors,
                "input_template_placements", template
        ));

        assertEquals(true, outputs.get("output_valid"));
        assertEquals(2, outputs.get("output_instance_count"));
        assertEquals(4, outputs.get("output_count"));
        BlockPosList positions = assertInstanceOf(BlockPosList.class, outputs.get("output_positions"));
        assertEquals(true, positions.contains(new BlockPos(20, 65, 20)));
    }

    @Test
    void filterPointsByRuleCanKeepHighSteepPoints() {
        FilterPointsByRuleNode node = new FilterPointsByRuleNode();

        Map<String, Object> outputs = node.compute(Map.of(
                "input_points", List.of(
                        new Vector3d(0.0d, 80.0d, 0.0d),
                        new Vector3d(0.0d, 120.0d, 0.0d)
                ),
                "input_normals", List.of(
                        new Vector3d(0.0d, 1.0d, 0.0d),
                        new Vector3d(1.0d, 1.0d, 0.0d)
                ),
                "input_min_height", 100.0d,
                "input_min_slope", 40.0d
        ));

        assertEquals(true, outputs.get("output_valid"));
        assertEquals(1, outputs.get("output_count"));
        assertEquals(List.of(false, true), outputs.get("output_mask"));
    }
}
