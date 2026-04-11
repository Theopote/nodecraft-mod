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
 * Regular Octahedron (Blocks) 鑺傜偣: 鐢熸垚姝ｅ叓闈綋鍖哄煙鐨勫潗鏍囧垪琛?
 * 姝ｅ叓闈綋鐢?|x| + |y| + |z| <= radius 瀹氫箟
 */
@NodeInfo(
    id = "spatial.generators.octahedron_blocks",
    displayName = "姝ｅ叓闈綋鐢熸垚鍣?,
    description = "鐢熸垚姝ｅ叓闈綋鍖哄煙鐨勫潗鏍囧垪琛?,
    category = "spatial.generators"
)
public class OctahedronBlocksNode extends BaseNode {

    // --- 鑺傜偣灞炴€?---
    private boolean hollow = false;
    private int thickness = 1;

    // --- 杈撳叆绔彛 IDs ---
    private static final String INPUT_CENTER_ID = "input_center";
    private static final String INPUT_SIZE_ID = "input_size";
    private static final String INPUT_HOLLOW_ID = "input_hollow";
    private static final String INPUT_THICKNESS_ID = "input_thickness";

    // --- 杈撳嚭绔彛 IDs ---
    private static final String OUTPUT_BLOCKS_ID = "output_blocks";
    private static final String OUTPUT_COUNT_ID = "output_count";

    public OctahedronBlocksNode() {
        super(UUID.randomUUID(), "spatial.generators.octahedron_blocks");

        addInputPort(new BasePort(INPUT_CENTER_ID, "Center", "姝ｅ叓闈綋涓績鐐?, NodeDataType.BLOCK_POS, this));
        addInputPort(new BasePort(INPUT_SIZE_ID, "Size", "浠庝腑蹇冨埌椤剁偣鐨勮窛绂?, NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_HOLLOW_ID, "Hollow", "鏄惁绌哄績", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_THICKNESS_ID, "Thickness", "澹佸帤锛堢┖蹇冩椂鏈夋晥锛?, NodeDataType.INTEGER, this));

        addOutputPort(new BasePort(OUTPUT_BLOCKS_ID, "Blocks", "缁勬垚姝ｅ叓闈綋鐨勬柟鍧楀垪琛?, NodeDataType.BLOCK_LIST, this));
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Count", "鏂瑰潡鏁伴噺", NodeDataType.INTEGER, this));
    }

    @Override
    public String getDescription() {
        return "鐢熸垚姝ｅ叓闈綋鍖哄煙鐨勫潗鏍囧垪琛?;
    }

    @Override
    public String getDisplayName() {
        return "Octahedron (Blocks)";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object centerObj = inputValues.get(INPUT_CENTER_ID);
        Object sizeObj = inputValues.get(INPUT_SIZE_ID);
        Object hollowObj = inputValues.get(INPUT_HOLLOW_ID);
        Object thicknessObj = inputValues.get(INPUT_THICKNESS_ID);

        BlockPosList result = new BlockPosList();

        boolean isHollow = (hollowObj instanceof Boolean) ? (Boolean) hollowObj : this.hollow;
        int shellThickness = (thicknessObj instanceof Number) ? Math.max(1, ((Number) thicknessObj).intValue()) : this.thickness;

        if (centerObj instanceof BlockPos && sizeObj instanceof Number) {
            BlockPos center = (BlockPos) centerObj;
            int size = Math.max(1, ((Number) sizeObj).intValue());

            int cx = center.getX();
            int cy = center.getY();
            int cz = center.getZ();

            // 姝ｅ叓闈綋: |dx| + |dy| + |dz| <= size
            for (int dx = -size; dx <= size; dx++) {
                for (int dy = -size; dy <= size; dy++) {
                    for (int dz = -size; dz <= size; dz++) {
                        int manhattan = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);

                        if (manhattan <= size) {
                            if (isHollow) {
                                int innerSize = size - shellThickness;
                                if (innerSize < 0 || manhattan > innerSize) {
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
