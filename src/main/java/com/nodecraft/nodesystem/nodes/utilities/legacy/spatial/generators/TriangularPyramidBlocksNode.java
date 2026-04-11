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
 * Triangular Pyramid (Blocks) 鑺傜偣: 鐢熸垚涓€鑸笁妫遍敟锛堥潪姝ｅ洓闈綋锛夊尯鍩熺殑鍧愭爣鍒楄〃
 * 鐢ㄦ埛鍙互鑷畾涔夊簳闈笁瑙掑舰灏哄鍜岄珮搴?
 */
@NodeInfo(
    id = "spatial.generators.triangular_pyramid_blocks",
    displayName = "涓夋１閿ョ敓鎴愬櫒",
    description = "鐢熸垚涓夋１閿ュ尯鍩熺殑鍧愭爣鍒楄〃锛屽彲鑷畾涔夊簳杈瑰拰楂樺害",
    category = "spatial.generators"
)
public class TriangularPyramidBlocksNode extends BaseNode {

    // --- 杈撳叆绔彛 IDs ---
    private static final String INPUT_CENTER_ID = "input_center";
    private static final String INPUT_BASE_SIZE_ID = "input_base_size";
    private static final String INPUT_HEIGHT_ID = "input_height";

    // --- 杈撳嚭绔彛 IDs ---
    private static final String OUTPUT_BLOCKS_ID = "output_blocks";
    private static final String OUTPUT_COUNT_ID = "output_count";

    public TriangularPyramidBlocksNode() {
        super(UUID.randomUUID(), "spatial.generators.triangular_pyramid_blocks");

        addInputPort(new BasePort(INPUT_CENTER_ID, "Base Center", "搴曢潰涓績鐐?, NodeDataType.BLOCK_POS, this));
        addInputPort(new BasePort(INPUT_BASE_SIZE_ID, "Base Size", "搴曢潰绛夎竟涓夎褰㈣竟闀?, NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_HEIGHT_ID, "Height", "妫遍敟楂樺害", NodeDataType.INTEGER, this));

        addOutputPort(new BasePort(OUTPUT_BLOCKS_ID, "Blocks", "缁勬垚涓夋１閿ョ殑鏂瑰潡鍒楄〃", NodeDataType.BLOCK_LIST, this));
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Count", "鏂瑰潡鏁伴噺", NodeDataType.INTEGER, this));
    }

    @Override
    public String getDescription() {
        return "鐢熸垚涓夋１閿ュ尯鍩熺殑鍧愭爣鍒楄〃";
    }

    @Override
    public String getDisplayName() {
        return "Triangular Pyramid (Blocks)";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object centerObj = inputValues.get(INPUT_CENTER_ID);
        Object baseSizeObj = inputValues.get(INPUT_BASE_SIZE_ID);
        Object heightObj = inputValues.get(INPUT_HEIGHT_ID);

        BlockPosList result = new BlockPosList();

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
            // 绛夎竟涓夎褰?涓《鐐癸紙鍦╔Z骞抽潰涓婏級
            double circumR = baseSize / Math.sqrt(3.0); // 澶栨帴鍦嗗崐寰?

            double[][] baseVertices = {
                {0, circumR},                                          // 鍓嶆柟
                {-circumR * Math.sqrt(3.0) / 2.0, -circumR / 2.0},    // 宸﹀悗
                {circumR * Math.sqrt(3.0) / 2.0, -circumR / 2.0}      // 鍙冲悗
            };

            // 灏栭《鍦?(cx, cy + height, cz)
            // 閫愬眰鐢熸垚锛屾瘡灞傜殑涓夎褰㈡寜姣斾緥缂╁皬
            for (int dy = 0; dy < height; dy++) {
                double ratio = 1.0 - (double) dy / height;

                // 褰撳墠灞傜殑涓夎褰㈤《鐐?
                double[][] layerVerts = new double[3][2];
                for (int i = 0; i < 3; i++) {
                    layerVerts[i][0] = baseVertices[i][0] * ratio;
                    layerVerts[i][1] = baseVertices[i][1] * ratio;
                }

                // 鎵弿褰撳墠灞傜殑鍖呭洿鐩?
                int bound = (int) Math.ceil(circumR * ratio) + 1;

                for (int dx = -bound; dx <= bound; dx++) {
                    for (int dz = -bound; dz <= bound; dz++) {
                        if (isInsideTriangle(dx, dz, layerVerts)) {
                            result.add(new BlockPos(cx + dx, cy + dy, cz + dz));
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

        // 浣跨敤 +0.5 鐨勫宸潵琛ュ伩鏂瑰潡绂绘暎鍖?
        double tolerance = 0.5 / Math.max(1, Math.max(Math.abs(x1 - x2), Math.abs(x1 - x3)));
        return a >= -tolerance && b >= -tolerance && c >= -tolerance;
    }

    @Override
    public Object getNodeState() {
        return new java.util.HashMap<>();
    }

    @Override
    public void setNodeState(Object state) {
        // 鏃犻澶栫姸鎬?
    }
}
