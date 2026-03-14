package com.nodecraft.nodesystem.nodes.world.modification;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPlacementData;
import com.nodecraft.nodesystem.util.BlockPosList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 材质映射节点：按相对高度将形状坐标分为底层/中层/顶层，并分配不同方块类型。
 * 形状与材质解耦，可与 Set Blocks / 应用修改 等节点配合使用。
 */
@NodeInfo(
    id = "world.modification.material_mapper",
    displayName = "材质映射",
    description = "按高度为坐标分配不同方块类型（底层/中层/顶层）",
    category = "world.modification"
)
public class MaterialMapperNode extends BaseNode {

    private static final String INPUT_COORDINATES_ID = "input_coordinates";
    private static final String INPUT_BOTTOM_ID = "input_bottom";
    private static final String INPUT_MIDDLE_ID = "input_middle";
    private static final String INPUT_TOP_ID = "input_top";

    private static final String OUTPUT_POSITIONS_ID = "output_positions";
    private static final String OUTPUT_BLOCK_IDS_ID = "output_block_ids";
    private static final String OUTPUT_PLACEMENTS_ID = "output_placements";

    public MaterialMapperNode() {
        super(UUID.randomUUID(), "world.modification.material_mapper");
        addInputPort(new BasePort(INPUT_COORDINATES_ID, "Coordinates", "坐标列表", NodeDataType.BLOCK_LIST, this));
        addInputPort(new BasePort(INPUT_BOTTOM_ID, "Bottom", "底层方块（相对高度 0–33%）", NodeDataType.BLOCK_TYPE, this));
        addInputPort(new BasePort(INPUT_MIDDLE_ID, "Middle", "中层方块（33–66%）", NodeDataType.BLOCK_TYPE, this));
        addInputPort(new BasePort(INPUT_TOP_ID, "Top", "顶层方块（66–100%）", NodeDataType.BLOCK_TYPE, this));
        addOutputPort(new BasePort(OUTPUT_POSITIONS_ID, "Positions", "与 block_ids 同序的坐标列表", NodeDataType.BLOCK_LIST, this));
        addOutputPort(new BasePort(OUTPUT_BLOCK_IDS_ID, "Block IDs", "与坐标一一对应的方块 ID 列表", NodeDataType.BLOCK_INFO_LIST, this));
        addOutputPort(new BasePort(OUTPUT_PLACEMENTS_ID, "Block Placements", "坐标+方块ID列表，可接应用修改", NodeDataType.BLOCK_PLACEMENT_LIST, this));
    }

    @Override
    public String getDescription() {
        return "按高度为坐标分配不同方块类型（底层/中层/顶层）";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object coordsObj = inputValues.get(INPUT_COORDINATES_ID);
        String bottom = getInputString(INPUT_BOTTOM_ID, "minecraft:stone");
        String middle = getInputString(INPUT_MIDDLE_ID, "minecraft:dirt");
        String top = getInputString(INPUT_TOP_ID, "minecraft:grass_block");

        BlockPosList outPositions = new BlockPosList();
        List<String> outBlockIds = new ArrayList<>();
        List<BlockPlacementData> outPlacements = new ArrayList<>();

        if (!(coordsObj instanceof BlockPosList)) {
            outputValues.put(OUTPUT_POSITIONS_ID, outPositions);
            outputValues.put(OUTPUT_BLOCK_IDS_ID, outBlockIds);
            outputValues.put(OUTPUT_PLACEMENTS_ID, outPlacements);
            return;
        }

        BlockPosList coords = (BlockPosList) coordsObj;
        if (coords.isEmpty()) {
            outputValues.put(OUTPUT_POSITIONS_ID, outPositions);
            outputValues.put(OUTPUT_BLOCK_IDS_ID, outBlockIds);
            outputValues.put(OUTPUT_PLACEMENTS_ID, outPlacements);
            return;
        }

        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (BlockPos p : coords) {
            minY = Math.min(minY, p.getY());
            maxY = Math.max(maxY, p.getY());
        }
        double span = (double) (maxY - minY);
        if (span < 1e-6) span = 1.0;

        for (BlockPos pos : coords) {
            double t = (pos.getY() - minY) / span;
            String blockId;
            if (t < 1.0 / 3.0) blockId = bottom;
            else if (t < 2.0 / 3.0) blockId = middle;
            else blockId = top;
            outPositions.add(pos);
            outBlockIds.add(blockId);
            outPlacements.add(new BlockPlacementData(pos, blockId));
        }

        outputValues.put(OUTPUT_POSITIONS_ID, outPositions);
        outputValues.put(OUTPUT_BLOCK_IDS_ID, outBlockIds);
        outputValues.put(OUTPUT_PLACEMENTS_ID, outPlacements);
    }

    private String getInputString(String portId, String fallback) {
        Object v = inputValues.get(portId);
        return (v instanceof String && !((String) v).isEmpty()) ? (String) v : fallback;
    }
}
