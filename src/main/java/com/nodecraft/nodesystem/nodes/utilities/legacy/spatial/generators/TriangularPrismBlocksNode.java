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
 * Triangular Prism (Blocks) 鑺傜偣: 鐢熸垚涓夋１鏌卞尯鍩熺殑鍧愭爣鍒楄〃
 * 搴曢潰涓虹瓑杈逛笁瑙掑舰锛屾部Y杞村悜涓婂欢浼?
 */
@NodeInfo(
    id = "spatial.generators.triangular_prism_blocks",
    displayName = "涓夋１鏌辩敓鎴愬櫒",
    description = "鐢熸垚涓夋１鏌卞尯鍩熺殑鍧愭爣鍒楄〃锛屽簳闈负绛夎竟涓夎褰?,
    category = "spatial.generators"
)
public class TriangularPrismBlocksNode extends BaseNode {

    // --- 鑺傜偣灞炴€?---
    private boolean hollow = false;
    private int thickness = 1;

    // --- 杈撳叆绔彛 IDs ---
    private static final String INPUT_CENTER_ID = "input_center";
    private static final String INPUT_BASE_SIZE_ID = "input_base_size";
    private static final String INPUT_HEIGHT_ID = "input_height";
    private static final String INPUT_HOLLOW_ID = "input_hollow";
    private static final String INPUT_THICKNESS_ID = "input_thickness";

    // --- 杈撳嚭绔彛 IDs ---
    private static final String OUTPUT_BLOCKS_ID = "output_blocks";
    private static final String OUTPUT_COUNT_ID = "output_count";

    public TriangularPrismBlocksNode() {
        super(UUID.randomUUID(), "spatial.generators.triangular_prism_blocks");

        addInputPort(new BasePort(INPUT_CENTER_ID, "Base Center", "搴曢潰涓績鐐?, NodeDataType.BLOCK_POS, this));
        addInputPort(new BasePort(INPUT_BASE_SIZE_ID, "Base Size", "搴曢潰绛夎竟涓夎褰㈣竟闀?, NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_HEIGHT_ID, "Height", "妫辨煴楂樺害", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_HOLLOW_ID, "Hollow", "鏄惁绌哄績", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_THICKNESS_ID, "Thickness", "澹佸帤锛堢┖蹇冩椂鏈夋晥锛?, NodeDataType.INTEGER, this));

        addOutputPort(new BasePort(OUTPUT_BLOCKS_ID, "Blocks", "缁勬垚涓夋１鏌辩殑鏂瑰潡鍒楄〃", NodeDataType.BLOCK_LIST, this));
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Count", "鏂瑰潡鏁伴噺", NodeDataType.INTEGER, this));
    }

    @Override
    public String getDescription() {
        return "鐢熸垚涓夋１鏌卞尯鍩熺殑鍧愭爣鍒楄〃";
    }

    @Override
    public String getDisplayName() {
        return "Triangular Prism (Blocks)";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object centerObj = inputValues.get(INPUT_CENTER_ID);
        Object baseSizeObj = inputValues.get(INPUT_BASE_SIZE_ID);
        Object heightObj = inputValues.get(INPUT_HEIGHT_ID);
        Object hollowObj = inputValues.get(INPUT_HOLLOW_ID);
        Object thicknessObj = inputValues.get(INPUT_THICKNESS_ID);

        BlockPosList result = new BlockPosList();

        boolean isHollow = (hollowObj instanceof Boolean) ? (Boolean) hollowObj : this.hollow;
        int shellThickness = (thicknessObj instanceof Number) ? Math.max(1, ((Number) thicknessObj).intValue()) : this.thickness;

        if (centerObj instanceof BlockPos &&
            baseSizeObj instanceof Number &&
            heightObj instanceof Number) {

            BlockPos center = (BlockPos) centerObj;
            int baseSize = Math.max(1, ((Number) baseSizeObj).intValue());
            int height = Math.max(1, ((Number) heightObj).intValue());

            int cx = center.getX();
            int cy = center.getY();
            int cz = center.getZ();

            // 搴曢潰涓虹瓑杈逛笁瑙掑舰锛屼腑蹇冨湪(cx, cy, cz)
            double circumR = baseSize / Math.sqrt(3.0); // 澶栨帴鍦嗗崐寰?

            // 绛夎竟涓夎褰?涓《鐐癸紙鍦╔Z骞抽潰涓婏級
            double[][] vertices = {
                {0, circumR},                                          // 鍓嶆柟
                {-circumR * Math.sqrt(3.0) / 2.0, -circumR / 2.0},    // 宸﹀悗
                {circumR * Math.sqrt(3.0) / 2.0, -circumR / 2.0}      // 鍙冲悗
            };

            // 鍐呬笁瑙掑舰椤剁偣锛堢敤浜庣┖蹇冨垽鏂級
            double innerCircumR = Math.max(0, circumR - shellThickness);
            double[][] innerVertices = null;
            if (isHollow && innerCircumR > 0) {
                innerVertices = new double[][] {
                    {0, innerCircumR},
                    {-innerCircumR * Math.sqrt(3.0) / 2.0, -innerCircumR / 2.0},
                    {innerCircumR * Math.sqrt(3.0) / 2.0, -innerCircumR / 2.0}
                };
            }

            int bound = (int) Math.ceil(circumR) + 1;

            // 閫愬眰鐢熸垚
            for (int dy = 0; dy < height; dy++) {
                boolean isTopBottom = (dy == 0 || dy == height - 1);

                for (int dx = -bound; dx <= bound; dx++) {
                    for (int dz = -bound; dz <= bound; dz++) {
                        if (isInsideTriangle(dx, dz, vertices)) {
                            if (isHollow) {
                                // 绌哄績妯″紡: 椤?搴曢潰瀹屾暣, 渚ч潰鍙繚鐣欏澹?
                                boolean isInner = (innerVertices != null) && isInsideTriangle(dx, dz, innerVertices);
                                if (isTopBottom || !isInner) {
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

    /**
     * 浣跨敤閲嶅績鍧愭爣娉曞垽鏂偣鏄惁鍦ㄤ笁瑙掑舰鍐呴儴
     */
    private boolean isInsideTriangle(double px, double pz, double[][] verts) {
        double x1 = verts[0][0], z1 = verts[0][1];
        double x2 = verts[1][0], z2 = verts[1][1];
        double x3 = verts[2][0], z3 = verts[2][1];

        double denom = (z2 - z3) * (x1 - x3) + (x3 - x2) * (z1 - z3);
        if (Math.abs(denom) < 1e-10) return false;

        double a = ((z2 - z3) * (px - x3) + (x3 - x2) * (pz - z3)) / denom;
        double b = ((z3 - z1) * (px - x3) + (x1 - x3) * (pz - z3)) / denom;
        double c = 1.0 - a - b;

        double tolerance = 0.5 / Math.max(1, Math.max(Math.abs(x1 - x2), Math.abs(x1 - x3)));
        return a >= -tolerance && b >= -tolerance && c >= -tolerance;
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
