package com.nodecraft.nodesystem.nodes.utilities.legacy.spatial.generators;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 鍦嗗姬鐢熸垚鍣細鍦ㄦ寚瀹氬钩闈㈢敓鎴愬渾寮э紙鍦嗗績銆佸崐寰勩€佽捣姝㈣搴︼級鐨勬柟鍧楀潗鏍囧垪琛ㄣ€? * 瑙掑害涓哄害锛? 涓?X 姝ｈ酱鏂瑰悜锛岄€嗘椂閽堜负姝ｃ€? */
@NodeInfo(
    id = "spatial.generators.arc_blocks",
    displayName = "鍦嗗姬",
    description = "鐢熸垚鍦嗗姬涓婄殑鏂瑰潡鍧愭爣鍒楄〃",
    category = "spatial.generators"
)
public class ArcBlocksNode extends BaseNode {

    public enum Plane { XY, YZ, XZ }

    private static final String INPUT_CENTER_ID = "input_center";
    private static final String INPUT_RADIUS_ID = "input_radius";
    private static final String INPUT_START_ANGLE_ID = "input_start_angle";
    private static final String INPUT_END_ANGLE_ID = "input_end_angle";
    private static final String OUTPUT_BLOCKS_ID = "output_blocks";

    private Plane plane = Plane.XZ;

    public ArcBlocksNode() {
        super(UUID.randomUUID(), "spatial.generators.arc_blocks");
        addInputPort(new BasePort(INPUT_CENTER_ID, "Center", "鍦嗗績", NodeDataType.BLOCK_POS, this));
        addInputPort(new BasePort(INPUT_RADIUS_ID, "Radius", "鍗婂緞锛堟牸锛?, NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_START_ANGLE_ID, "Start Angle", "璧峰瑙掞紙搴︼紝0=鍙筹級", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_END_ANGLE_ID, "End Angle", "缁撴潫瑙掞紙搴︼級", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_BLOCKS_ID, "Blocks", "鍦嗗姬涓婄殑鏂瑰潡鍧愭爣", NodeDataType.BLOCK_LIST, this));
    }

    @Override
    public String getDescription() { return "鐢熸垚鍦嗗姬涓婄殑鏂瑰潡鍧愭爣鍒楄〃"; }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        BlockPosList result = new BlockPosList();
        Object centerObj = inputValues.get(INPUT_CENTER_ID);
        Object radiusObj = inputValues.get(INPUT_RADIUS_ID);
        Object startObj = inputValues.get(INPUT_START_ANGLE_ID);
        Object endObj = inputValues.get(INPUT_END_ANGLE_ID);

        if (!(centerObj instanceof BlockPos) || !(radiusObj instanceof Number)) {
            outputValues.put(OUTPUT_BLOCKS_ID, result);
            return;
        }

        BlockPos center = (BlockPos) centerObj;
        double r = Math.max(0.5, ((Number) radiusObj).doubleValue());
        double startDeg = startObj instanceof Number ? ((Number) startObj).doubleValue() : 0;
        double endDeg = endObj instanceof Number ? ((Number) endObj).doubleValue() : 90;

        double startRad = Math.toRadians(startDeg);
        double endRad = Math.toRadians(endDeg);
        if (endRad <= startRad) endRad += 2 * Math.PI;
        int steps = Math.max(4, (int) (r * Math.abs(endRad - startRad) * 2));
        double step = (endRad - startRad) / steps;

        int cx = center.getX(), cy = center.getY(), cz = center.getZ();
        Set<String> seen = new HashSet<>();

        for (int i = 0; i <= steps; i++) {
            double a = startRad + i * step;
            int ix = (int) Math.round(r * Math.cos(a));
            int iz = (int) Math.round(r * Math.sin(a));
            String key = ix + "," + iz;
            if (seen.add(key)) {
                if (plane == Plane.XZ) {
                    result.add(new BlockPos(cx + ix, cy, cz + iz));
                } else if (plane == Plane.XY) {
                    result.add(new BlockPos(cx + ix, cy + iz, cz));
                } else {
                    result.add(new BlockPos(cx, cy + ix, cz + iz));
                }
            }
        }
        outputValues.put(OUTPUT_BLOCKS_ID, result);
    }

    public Plane getPlane() { return plane; }
    public void setPlane(Plane v) { if (plane != v) { plane = v; markDirty(); } }

    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        m.put("plane", plane.name());
        return m;
    }

    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map && ((java.util.Map<?, ?>) state).get("plane") instanceof String) {
            try { setPlane(Plane.valueOf((String) ((java.util.Map<?, ?>) state).get("plane"))); } catch (Exception ignored) {}
        }
    }
}
