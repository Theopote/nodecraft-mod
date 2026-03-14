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
 * Circle / Sphere (Blocks) 节点: 生成圆形或球形区域的坐标列表
 */
@NodeInfo(
    id = "spatial.generators.circle_sphere_blocks",
    displayName = "圆形/球形生成器",
    description = "生成圆形或球形区域的坐标列表",
    category = "spatial.generators"
)
public class CircleSphereBlocksNode extends BaseNode {

    private boolean is3D = false;
    private boolean hollow = false;
    private boolean useEuclideanDistance = true;

    private static final String INPUT_CENTER_ID = "input_center";
    private static final String INPUT_RADIUS_ID = "input_radius";
    private static final String INPUT_THICKNESS_ID = "input_thickness";
    private static final String OUTPUT_BLOCKS_ID = "output_blocks";

    public CircleSphereBlocksNode() {
        super(UUID.randomUUID(), "spatial.generators.circle_sphere_blocks");
        addInputPort(new BasePort(INPUT_CENTER_ID, "Center", "The center point of the circle/sphere", NodeDataType.BLOCK_POS, this));
        addInputPort(new BasePort(INPUT_RADIUS_ID, "Radius", "The radius of the circle/sphere", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_THICKNESS_ID, "Thickness", "Shell thickness (for hollow shapes)", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_BLOCKS_ID, "Blocks", "The blocks forming the circle/sphere", NodeDataType.BLOCK_LIST, this));
    }

    @Override
    public String getDescription() { return "Generates a circle or sphere of blocks"; }

    @Override
    public String getDisplayName() { return "Circle / Sphere (Blocks)"; }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        BlockPosList result = new BlockPosList();
        Object centerObj = inputValues.get(INPUT_CENTER_ID);
        Object radiusObj = inputValues.get(INPUT_RADIUS_ID);
        Object thicknessObj = inputValues.get(INPUT_THICKNESS_ID);
        if (centerObj instanceof BlockPos && radiusObj instanceof Number) {
            BlockPos center = (BlockPos) centerObj;
            double radius = Math.max(1, ((Number) radiusObj).doubleValue());
            int thickness = thicknessObj instanceof Number ? Math.max(1, ((Number) thicknessObj).intValue()) : 1;
            double innerRadius = hollow ? Math.max(0, radius - thickness) : 0;
            if (is3D) generateSphere(center, radius, innerRadius, result);
            else generateCircle(center, radius, innerRadius, result);
        }
        outputValues.put(OUTPUT_BLOCKS_ID, result);
    }

    private void generateCircle(BlockPos center, double radius, double innerRadius, BlockPosList result) {
        int x0 = center.getX(), y0 = center.getY(), z0 = center.getZ();
        int radiusCeil = (int) Math.ceil(radius);
        for (int dx = -radiusCeil; dx <= radiusCeil; dx++) {
            for (int dz = -radiusCeil; dz <= radiusCeil; dz++) {
                double distance = useEuclideanDistance ? Math.sqrt(dx * dx + dz * dz) : Math.abs(dx) + Math.abs(dz);
                if (distance <= radius && (!hollow || distance >= innerRadius))
                    result.add(new BlockPos(x0 + dx, y0, z0 + dz));
            }
        }
    }

    private void generateSphere(BlockPos center, double radius, double innerRadius, BlockPosList result) {
        int x0 = center.getX(), y0 = center.getY(), z0 = center.getZ();
        int radiusCeil = (int) Math.ceil(radius);
        for (int dx = -radiusCeil; dx <= radiusCeil; dx++) {
            for (int dy = -radiusCeil; dy <= radiusCeil; dy++) {
                for (int dz = -radiusCeil; dz <= radiusCeil; dz++) {
                    double distance = useEuclideanDistance ? Math.sqrt(dx * dx + dy * dy + dz * dz) : Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
                    if (distance <= radius && (!hollow || distance >= innerRadius))
                        result.add(new BlockPos(x0 + dx, y0 + dy, z0 + dz));
                }
            }
        }
    }

    public boolean is3D() { return is3D; }
    public void set3D(boolean is3D) { this.is3D = is3D; markDirty(); }
    public boolean isHollow() { return hollow; }
    public void setHollow(boolean hollow) { this.hollow = hollow; markDirty(); }
    public boolean isUseEuclideanDistance() { return useEuclideanDistance; }
    public void setUseEuclideanDistance(boolean useEuclideanDistance) { this.useEuclideanDistance = useEuclideanDistance; markDirty(); }

    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("is3D", is3D);
        state.put("hollow", hollow);
        state.put("useEuclideanDistance", useEuclideanDistance);
        return state;
    }

    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> m = (java.util.Map<?, ?>) state;
            if (m.get("is3D") instanceof Boolean) set3D((Boolean) m.get("is3D"));
            if (m.get("hollow") instanceof Boolean) setHollow((Boolean) m.get("hollow"));
            if (m.get("useEuclideanDistance") instanceof Boolean) setUseEuclideanDistance((Boolean) m.get("useEuclideanDistance"));
        }
    }
}
