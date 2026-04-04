package com.nodecraft.nodesystem.nodes.spatial.generators;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.NodeProperty;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.RegionData;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Generates a box / cuboid either from center + size or from two corners.
 */
@NodeInfo(
    id = "spatial.generators.box_blocks",
    displayName = "Box (Blocks)",
    description = "Generates a filled or hollow box from center + size or from two corner points",
    category = "spatial.generators"
)
public class BoxBlocksNode extends BaseNode {

    @NodeProperty(displayName = "Fill Box", category = "Shape", order = 1,
        description = "When disabled, only the outer shell is generated")
    private boolean fillBox = true;

    @NodeProperty(displayName = "Output Region Only", category = "Output", order = 10,
        description = "When enabled, the node skips block generation and outputs only the region")
    private boolean outputAsRegion = false;

    private static final String INPUT_CENTER_ID = "input_center";
    private static final String INPUT_SIZE_X_ID = "input_size_x";
    private static final String INPUT_SIZE_Y_ID = "input_size_y";
    private static final String INPUT_SIZE_Z_ID = "input_size_z";
    private static final String INPUT_CORNER_A_ID = "input_corner_a";
    private static final String INPUT_CORNER_B_ID = "input_corner_b";

    private static final String OUTPUT_BOX_BLOCKS_ID = "output_box_blocks";
    private static final String OUTPUT_REGION_ID = "output_region";
    private static final String OUTPUT_MIN_CORNER_ID = "output_min_corner";
    private static final String OUTPUT_MAX_CORNER_ID = "output_max_corner";
    private static final String OUTPUT_COUNT_ID = "output_count";

    public BoxBlocksNode() {
        super(UUID.randomUUID(), "spatial.generators.box_blocks");

        addInputPort(new BasePort(INPUT_CENTER_ID, "Center", "Center point of the box", NodeDataType.BLOCK_POS, this));
        addInputPort(new BasePort(INPUT_SIZE_X_ID, "Size X", "Width in blocks on the X axis", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_SIZE_Y_ID, "Size Y", "Height in blocks on the Y axis", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_SIZE_Z_ID, "Size Z", "Depth in blocks on the Z axis", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_CORNER_A_ID, "Corner A", "Optional first corner of the box", NodeDataType.BLOCK_POS, this));
        addInputPort(new BasePort(INPUT_CORNER_B_ID, "Corner B", "Optional second corner of the box", NodeDataType.BLOCK_POS, this));

        addOutputPort(new BasePort(OUTPUT_BOX_BLOCKS_ID, "Blocks", "Blocks forming the box", NodeDataType.BLOCK_LIST, this));
        addOutputPort(new BasePort(OUTPUT_REGION_ID, "Region", "Box region", NodeDataType.REGION, this));
        addOutputPort(new BasePort(OUTPUT_MIN_CORNER_ID, "Min Corner", "Minimum corner of the box", NodeDataType.BLOCK_POS, this));
        addOutputPort(new BasePort(OUTPUT_MAX_CORNER_ID, "Max Corner", "Maximum corner of the box", NodeDataType.BLOCK_POS, this));
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Count", "Generated block count", NodeDataType.INTEGER, this));
    }

    @Override
    public String getDescription() {
        return "Generates a filled or hollow box from center + size or from two corner points";
    }

    @Override
    public String getDisplayName() {
        return "Box (Blocks)";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        RegionData region = resolveRegion();
        BlockPosList blocksList = new BlockPosList();
        BlockPos minCorner = null;
        BlockPos maxCorner = null;

        if (region != null && region.isComplete()) {
            minCorner = region.getMinCorner();
            maxCorner = region.getMaxCorner();

            if (!outputAsRegion && minCorner != null && maxCorner != null) {
                populateBlocks(blocksList, minCorner, maxCorner);
            }
        }

        outputValues.put(OUTPUT_BOX_BLOCKS_ID, blocksList);
        outputValues.put(OUTPUT_REGION_ID, region);
        outputValues.put(OUTPUT_MIN_CORNER_ID, minCorner);
        outputValues.put(OUTPUT_MAX_CORNER_ID, maxCorner);
        outputValues.put(OUTPUT_COUNT_ID, blocksList.size());
    }

    private RegionData resolveRegion() {
        Object cornerAObj = inputValues.get(INPUT_CORNER_A_ID);
        Object cornerBObj = inputValues.get(INPUT_CORNER_B_ID);

        if (cornerAObj instanceof BlockPos cornerA && cornerBObj instanceof BlockPos cornerB) {
            return new RegionData(cornerA.toImmutable(), cornerB.toImmutable());
        }

        Object centerObj = inputValues.get(INPUT_CENTER_ID);
        Object sizeXObj = inputValues.get(INPUT_SIZE_X_ID);
        Object sizeYObj = inputValues.get(INPUT_SIZE_Y_ID);
        Object sizeZObj = inputValues.get(INPUT_SIZE_Z_ID);

        if (!(centerObj instanceof BlockPos center) ||
            !(sizeXObj instanceof Number sizeXNumber) ||
            !(sizeYObj instanceof Number sizeYNumber) ||
            !(sizeZObj instanceof Number sizeZNumber)) {
            return null;
        }

        int sizeX = Math.max(1, sizeXNumber.intValue());
        int sizeY = Math.max(1, sizeYNumber.intValue());
        int sizeZ = Math.max(1, sizeZNumber.intValue());

        BlockPos minCorner = new BlockPos(
            center.getX() - ((sizeX - 1) / 2),
            center.getY() - ((sizeY - 1) / 2),
            center.getZ() - ((sizeZ - 1) / 2)
        );

        BlockPos maxCorner = new BlockPos(
            minCorner.getX() + sizeX - 1,
            minCorner.getY() + sizeY - 1,
            minCorner.getZ() + sizeZ - 1
        );

        return new RegionData(minCorner, maxCorner);
    }

    private void populateBlocks(BlockPosList blocksList, BlockPos minCorner, BlockPos maxCorner) {
        for (int x = minCorner.getX(); x <= maxCorner.getX(); x++) {
            for (int y = minCorner.getY(); y <= maxCorner.getY(); y++) {
                for (int z = minCorner.getZ(); z <= maxCorner.getZ(); z++) {
                    if (fillBox || isShellBlock(x, y, z, minCorner, maxCorner)) {
                        blocksList.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
    }

    private boolean isShellBlock(int x, int y, int z, BlockPos minCorner, BlockPos maxCorner) {
        return x == minCorner.getX() || x == maxCorner.getX()
            || y == minCorner.getY() || y == maxCorner.getY()
            || z == minCorner.getZ() || z == maxCorner.getZ();
    }

    public boolean isFillBox() {
        return fillBox;
    }

    public void setFillBox(boolean fillBox) {
        if (this.fillBox != fillBox) {
            this.fillBox = fillBox;
            markDirty();
        }
    }

    public boolean isOutputAsRegion() {
        return outputAsRegion;
    }

    public void setOutputAsRegion(boolean outputAsRegion) {
        if (this.outputAsRegion != outputAsRegion) {
            this.outputAsRegion = outputAsRegion;
            markDirty();
        }
    }

    @Override
    public Object getNodeState() {
        Map<String, Object> state = new HashMap<>();
        state.put("fillBox", fillBox);
        state.put("outputAsRegion", outputAsRegion);
        return state;
    }

    @Override
    public void setNodeState(Object state) {
        if (!(state instanceof Map<?, ?> stateMap)) {
            return;
        }

        if (stateMap.get("fillBox") instanceof Boolean fillBoxValue) {
            setFillBox(fillBoxValue);
        }
        if (stateMap.get("outputAsRegion") instanceof Boolean outputAsRegionValue) {
            setOutputAsRegion(outputAsRegionValue);
        }
    }
}
