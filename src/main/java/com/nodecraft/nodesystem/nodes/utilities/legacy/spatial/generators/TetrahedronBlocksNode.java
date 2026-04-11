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
 * Regular Tetrahedron (Blocks) 鑺傜偣: 鐢熸垚姝ｅ洓闈綋鍖哄煙鐨勫潗鏍囧垪琛?
 * 姝ｅ洓闈綋浠ヤ腑蹇冪偣涓哄熀鍑嗭紝閫氳繃edge闀垮害瀹氫箟澶у皬銆?
 * 浣跨敤鍗婄┖闂翠氦闆嗘硶鏉ュ垽鏂偣鏄惁鍦ㄦ鍥涢潰浣撳唴閮ㄣ€?
 */
@NodeInfo(
    id = "spatial.generators.tetrahedron_blocks",
    displayName = "姝ｅ洓闈綋鐢熸垚鍣?,
    description = "鐢熸垚姝ｅ洓闈綋鍖哄煙鐨勫潗鏍囧垪琛?,
    category = "spatial.generators"
)
public class TetrahedronBlocksNode extends BaseNode {

    // --- 杈撳叆绔彛 IDs ---
    private static final String INPUT_CENTER_ID = "input_center";
    private static final String INPUT_SIZE_ID = "input_size";

    // --- 杈撳嚭绔彛 IDs ---
    private static final String OUTPUT_BLOCKS_ID = "output_blocks";
    private static final String OUTPUT_COUNT_ID = "output_count";

    public TetrahedronBlocksNode() {
        super(UUID.randomUUID(), "spatial.generators.tetrahedron_blocks");

        addInputPort(new BasePort(INPUT_CENTER_ID, "Center", "姝ｅ洓闈綋涓績鐐?, NodeDataType.BLOCK_POS, this));
        addInputPort(new BasePort(INPUT_SIZE_ID, "Edge Length", "妫遍暱", NodeDataType.INTEGER, this));

        addOutputPort(new BasePort(OUTPUT_BLOCKS_ID, "Blocks", "缁勬垚姝ｅ洓闈綋鐨勬柟鍧楀垪琛?, NodeDataType.BLOCK_LIST, this));
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Count", "鏂瑰潡鏁伴噺", NodeDataType.INTEGER, this));
    }

    @Override
    public String getDescription() {
        return "鐢熸垚姝ｅ洓闈綋鍖哄煙鐨勫潗鏍囧垪琛?;
    }

    @Override
    public String getDisplayName() {
        return "Tetrahedron (Blocks)";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object centerObj = inputValues.get(INPUT_CENTER_ID);
        Object sizeObj = inputValues.get(INPUT_SIZE_ID);

        BlockPosList result = new BlockPosList();

        if (centerObj instanceof BlockPos && sizeObj instanceof Number) {
            BlockPos center = (BlockPos) centerObj;
            int edgeLength = Math.max(1, ((Number) sizeObj).intValue());

            int cx = center.getX();
            int cy = center.getY();
            int cz = center.getZ();

            // 姝ｅ洓闈綋鐨?涓《鐐?(浠ヤ腑蹇冧负鍘熺偣锛屽鎺ョ悆鍗婂緞 = edge * sqrt(6)/4)
            double circumR = edgeLength * Math.sqrt(6.0) / 4.0;

            // 姝ｅ洓闈綋4涓《鐐瑰潗鏍囷紙鏍囧噯鏈濆悜锛?
            // V0 = (0, circumR, 0)                    -- 椤堕儴
            // V1 = (0, -circumR/3, 2*circumR*sqrt(2)/3) -- 鍓嶆柟
            // V2 = (-circumR*sqrt(6)/3, -circumR/3, -circumR*sqrt(2)/3) -- 宸﹀悗
            // V3 = (circumR*sqrt(6)/3, -circumR/3, -circumR*sqrt(2)/3)  -- 鍙冲悗
            double h = circumR;
            double hBottom = circumR / 3.0;
            double frontZ = 2.0 * circumR * Math.sqrt(2.0) / 3.0;
            double backZ = -circumR * Math.sqrt(2.0) / 3.0;
            double sideX = circumR * Math.sqrt(6.0) / 3.0;

            double[][] vertices = {
                {0, h, 0},                    // V0: 涓?
                {0, -hBottom, frontZ},         // V1: 鍓?
                {-sideX, -hBottom, backZ},     // V2: 宸﹀悗
                {sideX, -hBottom, backZ}       // V3: 鍙冲悗
            };

            // 璁＄畻4涓潰鐨勬硶绾垮拰d鍊硷紙闈㈡湞鍐咃級
            // 闈鐢遍櫎椤剁偣i浠ュ鐨?涓《鐐规瀯鎴?
            int[][] faces = {{1, 2, 3}, {0, 3, 2}, {0, 1, 3}, {0, 2, 1}};

            double[][] normals = new double[4][3];
            double[] dValues = new double[4];

            for (int f = 0; f < 4; f++) {
                double[] a = vertices[faces[f][0]];
                double[] b = vertices[faces[f][1]];
                double[] c = vertices[faces[f][2]];

                // 杈瑰悜閲?
                double[] ab = {b[0] - a[0], b[1] - a[1], b[2] - a[2]};
                double[] ac = {c[0] - a[0], c[1] - a[1], c[2] - a[2]};

                // 娉曠嚎 = ab 脳 ac
                normals[f][0] = ab[1] * ac[2] - ab[2] * ac[1];
                normals[f][1] = ab[2] * ac[0] - ab[0] * ac[2];
                normals[f][2] = ab[0] * ac[1] - ab[1] * ac[0];

                // d = normal 路 a
                dValues[f] = normals[f][0] * a[0] + normals[f][1] * a[1] + normals[f][2] * a[2];

                // 纭繚娉曠嚎鏈濆唴锛堜腑蹇冨湪姝ｄ晶锛?
                // 涓績鍦ㄥ師鐐?0,0,0)锛屾墍浠ユ鏌?normal 路 (0,0,0) - d 鐨勭鍙?
                if (-dValues[f] < 0) {
                    normals[f][0] = -normals[f][0];
                    normals[f][1] = -normals[f][1];
                    normals[f][2] = -normals[f][2];
                    dValues[f] = -dValues[f];
                }
            }

            // 鎵弿鍖呭洿鐩?
            int bound = (int) Math.ceil(circumR) + 1;

            for (int dx = -bound; dx <= bound; dx++) {
                for (int dy = -bound; dy <= bound; dy++) {
                    for (int dz = -bound; dz <= bound; dz++) {
                        // 妫€鏌ョ偣鏄惁鍦ㄦ墍鏈?涓潰鐨勫唴渚?
                        boolean inside = true;
                        for (int f = 0; f < 4; f++) {
                            double dot = normals[f][0] * dx + normals[f][1] * dy + normals[f][2] * dz;
                            if (dot > dValues[f] + 0.5) { // +0.5 琛ュ伩鏂瑰潡绂绘暎鍖?
                                inside = false;
                                break;
                            }
                        }
                        if (inside) {
                            result.add(new BlockPos(cx + dx, cy + dy, cz + dz));
                        }
                    }
                }
            }
        }

        outputValues.put(OUTPUT_BLOCKS_ID, result);
        outputValues.put(OUTPUT_COUNT_ID, result.size());
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
