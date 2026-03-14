package com.nodecraft.nodesystem.nodes.spatial.sdf;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.RegionData;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 体素化节点：在给定区域内按步长生成网格方块坐标。
 * 可与 SDF 节点输出的区域配合，或单独用于填充区域网格。
 */
@NodeInfo(
    id = "spatial.sdf.voxelizer",
    displayName = "体素化",
    description = "在区域内按步长生成体素（方块坐标）网格",
    category = "spatial.sdf"
)
public class VoxelizerNode extends BaseNode {

    private static final String INPUT_REGION_ID = "input_region";
    private static final String INPUT_STEP_ID = "input_step";
    private static final String OUTPUT_BLOCKS_ID = "output_blocks";

    public VoxelizerNode() {
        super(UUID.randomUUID(), "spatial.sdf.voxelizer");
        addInputPort(new BasePort(INPUT_REGION_ID, "Region", "体素化区域", NodeDataType.REGION, this));
        addInputPort(new BasePort(INPUT_STEP_ID, "Step", "步长（1=每格）", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_BLOCKS_ID, "Blocks", "网格方块坐标列表", NodeDataType.BLOCK_LIST, this));
    }

    @Override
    public String getDescription() {
        return "在区域内按步长生成体素（方块坐标）网格";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        BlockPosList result = new BlockPosList();
        Object regionObj = inputValues.get(INPUT_REGION_ID);
        Object stepObj = inputValues.get(INPUT_STEP_ID);

        if (!(regionObj instanceof RegionData rd) || !rd.isComplete()) {
            outputValues.put(OUTPUT_BLOCKS_ID, result);
            return;
        }

        int step = 1;
        if (stepObj instanceof Number) {
            step = Math.max(1, ((Number) stepObj).intValue());
        }

        BlockPos min = rd.getMinCorner();
        BlockPos max = rd.getMaxCorner();
        for (int x = min.getX(); x <= max.getX(); x += step) {
            for (int y = min.getY(); y <= max.getY(); y += step) {
                for (int z = min.getZ(); z <= max.getZ(); z += step) {
                    result.add(new BlockPos(x, y, z));
                }
            }
        }
        outputValues.put(OUTPUT_BLOCKS_ID, result);
    }
}
