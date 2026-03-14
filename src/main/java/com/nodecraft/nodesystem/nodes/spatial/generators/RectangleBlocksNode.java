package com.nodecraft.nodesystem.nodes.spatial.generators;

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
 * Rectangle (Blocks) 节点: 生成一个平面矩形区域的坐标列表
 */
@NodeInfo(
    id = "spatial.generators.rectangle_blocks",
    displayName = "矩形生成器",
    description = "生成二维矩形区域的坐标列表",
    category = "spatial.generators"
)
public class RectangleBlocksNode extends BaseNode {

    private boolean fillRectangle = true;
    public enum Plane { XY, YZ, XZ }
    private Plane plane = Plane.XZ;

    private static final String INPUT_CENTER_ID = "input_center";
    private static final String INPUT_WIDTH_ID = "input_width";
    private static final String INPUT_HEIGHT_ID = "input_height";
    private static final String OUTPUT_RECTANGLE_BLOCKS_ID = "output_rectangle_blocks";

    public RectangleBlocksNode() {
        super(UUID.randomUUID(), "spatial.generators.rectangle_blocks");
        addInputPort(new BasePort(INPUT_CENTER_ID, "Center", "The center point of the rectangle", NodeDataType.BLOCK_POS, this));
        addInputPort(new BasePort(INPUT_WIDTH_ID, "Width", "The width of the rectangle", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_HEIGHT_ID, "Height", "The height of the rectangle", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_RECTANGLE_BLOCKS_ID, "Rectangle Blocks", "The blocks forming the rectangle", NodeDataType.BLOCK_LIST, this));
    }

    @Override
    public String getDescription() { return "Generates a planar rectangle of blocks"; }

    @Override
    public String getDisplayName() { return "Rectangle (Blocks)"; }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        BlockPosList result = new BlockPosList();
        Object centerObj = inputValues.get(INPUT_CENTER_ID);
        Object widthObj = inputValues.get(INPUT_WIDTH_ID);
        Object heightObj = inputValues.get(INPUT_HEIGHT_ID);
        if (centerObj instanceof BlockPos && widthObj instanceof Number && heightObj instanceof Number) {
            BlockPos center = (BlockPos) centerObj;
            int width = Math.max(1, ((Number) widthObj).intValue());
            int height = Math.max(1, ((Number) heightObj).intValue());
            int halfWidth = width / 2, halfHeight = height / 2;
            if (fillRectangle) generateFilledRectangle(center, halfWidth, halfHeight, result);
            else generateOutlineRectangle(center, halfWidth, halfHeight, result);
        }
        outputValues.put(OUTPUT_RECTANGLE_BLOCKS_ID, result);
    }

    private void generateFilledRectangle(BlockPos center, int halfWidth, int halfHeight, BlockPosList result) {
        int x = center.getX(), y = center.getY(), z = center.getZ();
        switch (plane) {
            case XY:
                for (int dx = -halfWidth; dx <= halfWidth; dx++)
                    for (int dy = -halfHeight; dy <= halfHeight; dy++)
                        result.add(new BlockPos(x + dx, y + dy, z));
                break;
            case YZ:
                for (int dy = -halfHeight; dy <= halfHeight; dy++)
                    for (int dz = -halfWidth; dz <= halfWidth; dz++)
                        result.add(new BlockPos(x, y + dy, z + dz));
                break;
            case XZ:
            default:
                for (int dx = -halfWidth; dx <= halfWidth; dx++)
                    for (int dz = -halfHeight; dz <= halfHeight; dz++)
                        result.add(new BlockPos(x + dx, y, z + dz));
                break;
        }
    }

    private void generateOutlineRectangle(BlockPos center, int halfWidth, int halfHeight, BlockPosList result) {
        int x = center.getX(), y = center.getY(), z = center.getZ();
        switch (plane) {
            case XY:
                for (int dx = -halfWidth; dx <= halfWidth; dx++) {
                    result.add(new BlockPos(x + dx, y - halfHeight, z));
                    result.add(new BlockPos(x + dx, y + halfHeight, z));
                }
                for (int dy = -halfHeight + 1; dy < halfHeight; dy++) {
                    result.add(new BlockPos(x - halfWidth, y + dy, z));
                    result.add(new BlockPos(x + halfWidth, y + dy, z));
                }
                break;
            case YZ:
                for (int dz = -halfWidth; dz <= halfWidth; dz++) {
                    result.add(new BlockPos(x, y - halfHeight, z + dz));
                    result.add(new BlockPos(x, y + halfHeight, z + dz));
                }
                for (int dy = -halfHeight + 1; dy < halfHeight; dy++) {
                    result.add(new BlockPos(x, y + dy, z - halfWidth));
                    result.add(new BlockPos(x, y + dy, z + halfWidth));
                }
                break;
            case XZ:
            default:
                for (int dx = -halfWidth; dx <= halfWidth; dx++) {
                    result.add(new BlockPos(x + dx, y, z - halfHeight));
                    result.add(new BlockPos(x + dx, y, z + halfHeight));
                }
                for (int dz = -halfHeight + 1; dz < halfHeight; dz++) {
                    result.add(new BlockPos(x - halfWidth, y, z + dz));
                    result.add(new BlockPos(x + halfWidth, y, z + dz));
                }
                break;
        }
    }

    public boolean isFillRectangle() { return fillRectangle; }
    public void setFillRectangle(boolean fillRectangle) { this.fillRectangle = fillRectangle; markDirty(); }
    public Plane getPlane() { return plane; }
    public void setPlane(Plane plane) { this.plane = plane; markDirty(); }

    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("fillRectangle", fillRectangle);
        state.put("plane", plane.name());
        return state;
    }

    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> m = (java.util.Map<?, ?>) state;
            if (m.get("fillRectangle") instanceof Boolean) setFillRectangle((Boolean) m.get("fillRectangle"));
            if (m.get("plane") instanceof String) try { setPlane(Plane.valueOf((String) m.get("plane"))); } catch (Exception ignored) {}
        }
    }
}
