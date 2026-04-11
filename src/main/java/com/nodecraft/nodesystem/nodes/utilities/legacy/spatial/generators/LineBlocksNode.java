package com.nodecraft.nodesystem.nodes.utilities.legacy.spatial.generators;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import java.util.UUID;

/**
 * Line (Blocks) 鑺傜偣: 鐢熸垚涓ょ偣涔嬮棿鐨勭洿绾垮潗鏍囧垪琛? */
@NodeInfo(
    id = "spatial.generators.line_blocks",
    displayName = "鐩寸嚎鐢熸垚鍣?,
    description = "鐢熸垚涓ょ偣涔嬮棿鐨勭洿绾垮潗鏍囧垪琛?,
    category = "spatial.generators"
)
public class LineBlocksNode extends BaseNode {

    private boolean useBresenham = true;

    private static final String INPUT_START_POINT_ID = "input_start_point";
    private static final String INPUT_END_POINT_ID = "input_end_point";
    private static final String OUTPUT_LINE_BLOCKS_ID = "output_line_blocks";

    public LineBlocksNode() {
        super(UUID.randomUUID(), "spatial.generators.line_blocks");
        addInputPort(new BasePort(INPUT_START_POINT_ID, "Start Point", "The start point of the line", NodeDataType.BLOCK_POS, this));
        addInputPort(new BasePort(INPUT_END_POINT_ID, "End Point", "The end point of the line", NodeDataType.BLOCK_POS, this));
        addOutputPort(new BasePort(OUTPUT_LINE_BLOCKS_ID, "Line Blocks", "The blocks along the line path", NodeDataType.BLOCK_LIST, this));
    }

    @Override
    public String getDescription() { return "Generates a path of blocks between two points"; }

    @Override
    public String getDisplayName() { return "Line (Blocks)"; }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        BlockPosList result = new BlockPosList();
        Object startObj = inputValues.get(INPUT_START_POINT_ID);
        Object endObj = inputValues.get(INPUT_END_POINT_ID);
        if (startObj instanceof BlockPos && endObj instanceof BlockPos) {
            BlockPos start = (BlockPos) startObj;
            BlockPos end = (BlockPos) endObj;
            if (useBresenham) generateBresenhamLine(start, end, result);
            else generateParametricLine(start, end, result);
        }
        outputValues.put(OUTPUT_LINE_BLOCKS_ID, result);
    }

    private void generateBresenhamLine(BlockPos start, BlockPos end, BlockPosList result) {
        int x1 = start.getX(), y1 = start.getY(), z1 = start.getZ();
        int x2 = end.getX(), y2 = end.getY(), z2 = end.getZ();
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1), dz = Math.abs(z2 - z1);
        int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1, sz = z1 < z2 ? 1 : -1;
        int dm = Math.max(Math.max(dx, dy), dz);
        if (dm == 0) { result.add(new BlockPos(x1, y1, z1)); return; }
        int x = x1, y = y1, z = z1;
        result.add(new BlockPos(x, y, z));
        for (int i = 0; i < dm; i++) {
            int err1 = (i + 1) * dx - dm, err2 = (i + 1) * dy - dm, err3 = (i + 1) * dz - dm;
            if (err1 > 0) x += sx;
            if (err2 > 0) y += sy;
            if (err3 > 0) z += sz;
            result.add(new BlockPos(x, y, z));
        }
    }

    private void generateParametricLine(BlockPos start, BlockPos end, BlockPosList result) {
        Vector3d startVec = new Vector3d(start.getX(), start.getY(), start.getZ());
        Vector3d endVec = new Vector3d(end.getX(), end.getY(), end.getZ());
        Vector3d dirVec = new Vector3d(endVec).sub(startVec);
        int distance = Math.abs(end.getX() - start.getX()) + Math.abs(end.getY() - start.getY()) + Math.abs(end.getZ() - start.getZ());
        distance = Math.max(distance, 1);
        for (int i = 0; i <= distance; i++) {
            double t = (double) i / distance;
            Vector3d pos = new Vector3d(startVec).add(new Vector3d(dirVec).mul(t));
            BlockPos blockPos = new BlockPos((int) Math.round(pos.x), (int) Math.round(pos.y), (int) Math.round(pos.z));
            if (i == 0 || !blockPos.equals(result.getPositions().get(result.size() - 1))) result.add(blockPos);
        }
    }

    public boolean isUseBresenham() { return useBresenham; }
    public void setUseBresenham(boolean useBresenham) { this.useBresenham = useBresenham; markDirty(); }

    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("useBresenham", useBresenham);
        return state;
    }

    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> m = (java.util.Map<?, ?>) state;
            if (m.get("useBresenham") instanceof Boolean) setUseBresenham((Boolean) m.get("useBresenham"));
        }
    }
}
