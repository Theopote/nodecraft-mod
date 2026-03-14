package com.nodecraft.nodesystem.nodes.spatial.sdf;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * SDF 球体节点：按有符号距离场（SDF）生成球体内部的方块坐标列表。
 * 满足 |p - center| <= radius 的体素。
 */
@NodeInfo(
    id = "spatial.sdf.sphere",
    displayName = "SDF 球体",
    description = "生成球体 SDF 的体素（方块坐标列表）",
    category = "spatial.sdf"
)
public class SDFSphereNode extends BaseNode {

    private static final String INPUT_CENTER_ID = "input_center";
    private static final String INPUT_RADIUS_ID = "input_radius";
    private static final String OUTPUT_BLOCKS_ID = "output_blocks";

    public SDFSphereNode() {
        super(UUID.randomUUID(), "spatial.sdf.sphere");
        addInputPort(new BasePort(INPUT_CENTER_ID, "Center", "球心", NodeDataType.BLOCK_POS, this));
        addInputPort(new BasePort(INPUT_RADIUS_ID, "Radius", "半径", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_BLOCKS_ID, "Blocks", "球体内的方块坐标", NodeDataType.BLOCK_LIST, this));
    }

    @Override
    public String getDescription() {
        return "生成球体 SDF 的体素（方块坐标列表）";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        BlockPosList result = new BlockPosList();
        Object centerObj = inputValues.get(INPUT_CENTER_ID);
        Object radiusObj = inputValues.get(INPUT_RADIUS_ID);

        if (!(centerObj instanceof BlockPos) || !(radiusObj instanceof Number)) {
            outputValues.put(OUTPUT_BLOCKS_ID, result);
            return;
        }

        BlockPos center = (BlockPos) centerObj;
        double radius = ((Number) radiusObj).doubleValue();
        if (radius < 0.5) {
            outputValues.put(OUTPUT_BLOCKS_ID, result);
            return;
        }

        int r = (int) Math.ceil(radius);
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (d <= radius) {
                        result.add(center.add(dx, dy, dz));
                    }
                }
            }
        }
        outputValues.put(OUTPUT_BLOCKS_ID, result);
    }
}
