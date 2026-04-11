package com.nodecraft.nodesystem.nodes.utilities.legacy.spatial.generators;

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
 * Cone (Blocks) 鑺傜偣: 鐢熸垚鍦嗛敟浣撳尯鍩熺殑鍧愭爣鍒楄〃
 */
@NodeInfo(
    id = "spatial.generators.cone_blocks",
    displayName = "鍦嗛敟浣撶敓鎴愬櫒",
    description = "鐢熸垚鍦嗛敟浣撳尯鍩熺殑鍧愭爣鍒楄〃",
    category = "spatial.generators"
)
public class ConeBlocksNode extends BaseNode {

    // --- 鑺傜偣灞炴€?---
    private boolean hollow = false;
    private int thickness = 1;

    // --- 杈撳叆绔彛 IDs ---
    private static final String INPUT_CENTER_ID = "input_center";
    private static final String INPUT_RADIUS_ID = "input_radius";
    private static final String INPUT_HEIGHT_ID = "input_height";
    private static final String INPUT_HOLLOW_ID = "input_hollow";
    private static final String INPUT_THICKNESS_ID = "input_thickness";

    // --- 杈撳嚭绔彛 IDs ---
    private static final String OUTPUT_BLOCKS_ID = "output_blocks";
    private static final String OUTPUT_COUNT_ID = "output_count";

    public ConeBlocksNode() {
        super(UUID.randomUUID(), "spatial.generators.cone_blocks");

        addInputPort(new BasePort(INPUT_CENTER_ID, "Base Center", "鍦嗛敟浣撳簳闈腑蹇冪偣", NodeDataType.BLOCK_POS, this));
        addInputPort(new BasePort(INPUT_RADIUS_ID, "Radius", "搴曢潰鍗婂緞", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_HEIGHT_ID, "Height", "鍦嗛敟浣撻珮搴?, NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_HOLLOW_ID, "Hollow", "鏄惁绌哄績", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_THICKNESS_ID, "Thickness", "澹佸帤锛堢┖蹇冩椂鏈夋晥锛?, NodeDataType.INTEGER, this));

        addOutputPort(new BasePort(OUTPUT_BLOCKS_ID, "Blocks", "缁勬垚鍦嗛敟浣撶殑鏂瑰潡鍒楄〃", NodeDataType.BLOCK_LIST, this));
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Count", "鏂瑰潡鏁伴噺", NodeDataType.INTEGER, this));
    }

    @Override
    public String getDescription() {
        return "鐢熸垚鍦嗛敟浣撳尯鍩熺殑鍧愭爣鍒楄〃";
    }

    @Override
    public String getDisplayName() {
        return "Cone (Blocks)";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object centerObj = inputValues.get(INPUT_CENTER_ID);
        Object radiusObj = inputValues.get(INPUT_RADIUS_ID);
        Object heightObj = inputValues.get(INPUT_HEIGHT_ID);
        Object hollowObj = inputValues.get(INPUT_HOLLOW_ID);
        Object thicknessObj = inputValues.get(INPUT_THICKNESS_ID);

        BlockPosList result = new BlockPosList();

        boolean isHollow = (hollowObj instanceof Boolean) ? (Boolean) hollowObj : this.hollow;
        int shellThickness = (thicknessObj instanceof Number) ? Math.max(1, ((Number) thicknessObj).intValue()) : this.thickness;

        if (centerObj instanceof BlockPos &&
            radiusObj instanceof Number &&
            heightObj instanceof Number) {

            BlockPos center = (BlockPos) centerObj;
            double baseRadius = Math.max(1, ((Number) radiusObj).doubleValue());
            int height = Math.max(1, ((Number) heightObj).intValue());

            int cx = center.getX();
            int cy = center.getY();
            int cz = center.getZ();

            // 浠庡簳闈?y=0)鍒伴《鐐?y=height-1)閫愬眰鐢熸垚
            for (int dy = 0; dy < height; dy++) {
                // 褰撳墠灞傜殑鍗婂緞绾挎€ч€掑噺: 搴曢潰鏈€澶? 椤剁偣涓?
                double layerRadius = baseRadius * (1.0 - (double) dy / height);
                double innerRadius = isHollow ? Math.max(0, layerRadius - shellThickness) : 0;

                int radiusCeil = (int) Math.ceil(layerRadius);

                for (int dx = -radiusCeil; dx <= radiusCeil; dx++) {
                    for (int dz = -radiusCeil; dz <= radiusCeil; dz++) {
                        double distance = Math.sqrt(dx * dx + dz * dz);

                        if (distance <= layerRadius) {
                            if (isHollow) {
                                // 绌哄績: 鍙繚鐣欏澹?+ 搴曢潰
                                boolean isBottom = (dy == 0);
                                boolean isShell = (distance >= innerRadius);
                                // 椤堕儴鍑犲眰鍗婂緞寰堝皬鏃跺叏閮ㄥ～鍏?
                                boolean isNearTip = (layerRadius <= shellThickness);
                                if (isBottom || isShell || isNearTip) {
                                    result.add(new BlockPos(cx + dx, cy + dy, cz + dz));
                                }
                            } else {
                                result.add(new BlockPos(cx + dx, cy + dy, cz + dz));
                            }
                        }
                    }
                }
            }
        }

        outputValues.put(OUTPUT_BLOCKS_ID, result);
        outputValues.put(OUTPUT_COUNT_ID, result.size());
    }

    // --- Getters/Setters ---
    public boolean isHollow() { return hollow; }
    public void setHollow(boolean hollow) { this.hollow = hollow; markDirty(); }
    public int getThickness() { return thickness; }
    public void setThickness(int thickness) { this.thickness = Math.max(1, thickness); markDirty(); }

    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("hollow", hollow);
        state.put("thickness", thickness);
        return state;
    }

    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> m = (java.util.Map<?, ?>) state;
            if (m.get("hollow") instanceof Boolean) setHollow((Boolean) m.get("hollow"));
            if (m.get("thickness") instanceof Number) setThickness(((Number) m.get("thickness")).intValue());
        }
    }
}
