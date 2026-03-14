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
 * SDF 方盒节点：生成轴对齐方盒（AABB）内部的方块坐标列表。
 * 与 SDF 方盒有符号距离场等价，输出盒内体素。
 */
@NodeInfo(
    id = "spatial.sdf.box",
    displayName = "SDF 方盒",
    description = "生成方盒内的方块坐标列表",
    category = "spatial.sdf"
)
public class SDFBoxNode extends BaseNode {

    private static final String INPUT_REGION_ID = "input_region";
    private static final String INPUT_MIN_ID = "input_min";
    private static final String INPUT_MAX_ID = "input_max";
    private static final String OUTPUT_BLOCKS_ID = "output_blocks";

    public SDFBoxNode() {
        super(UUID.randomUUID(), "spatial.sdf.box");
        addInputPort(new BasePort(INPUT_REGION_ID, "Region", "区域（可选）", NodeDataType.REGION, this));
        addInputPort(new BasePort(INPUT_MIN_ID, "Min", "最小角（无 Region 时）", NodeDataType.BLOCK_POS, this));
        addInputPort(new BasePort(INPUT_MAX_ID, "Max", "最大角（无 Region 时）", NodeDataType.BLOCK_POS, this));
        addOutputPort(new BasePort(OUTPUT_BLOCKS_ID, "Blocks", "方盒内的方块坐标", NodeDataType.BLOCK_LIST, this));
    }

    @Override
    public String getDescription() {
        return "生成方盒内的方块坐标列表";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        BlockPosList result = new BlockPosList();
        Object regionObj = inputValues.get(INPUT_REGION_ID);
        Object minObj = inputValues.get(INPUT_MIN_ID);
        Object maxObj = inputValues.get(INPUT_MAX_ID);

        BlockPos min = null;
        BlockPos max = null;
        if (regionObj instanceof RegionData rd && rd.isComplete()) {
            min = rd.getMinCorner();
            max = rd.getMaxCorner();
        } else if (minObj instanceof BlockPos && maxObj instanceof BlockPos) {
            BlockPos a = (BlockPos) minObj;
            BlockPos b = (BlockPos) maxObj;
            min = new BlockPos(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
            max = new BlockPos(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
        }
        if (min == null || max == null) {
            outputValues.put(OUTPUT_BLOCKS_ID, result);
            return;
        }

        for (BlockPos pos : BlockPos.iterate(min, max)) {
            result.add(pos.toImmutable());
        }
        outputValues.put(OUTPUT_BLOCKS_ID, result);
    }
}
