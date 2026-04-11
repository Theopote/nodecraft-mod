package com.nodecraft.nodesystem.nodes.utilities.data_conversion;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3d;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Vector to Coordinate 鑺傜偣锛屽皢娴偣鍧愭爣/鍚戦噺杞崲涓烘暣鏁板潗鏍?
 */
@NodeInfo(
    id = "data.conversion.vector_to_coordinate",
    displayName = "Vector to Coordinate",
    description = "Converts floating-point vector to integer block coordinates",
    category = "data.conversion"
)
public class VectorToCoordinateNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    public enum RoundingMode {
        FLOOR, ROUND, CEIL
    }
    
    private RoundingMode roundingMode = RoundingMode.FLOOR; // 榛樿涓哄悜涓嬪彇鏁?
    private String description; // 瀛樺偍鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_VECTOR_ID = "input_vector";
    private static final String OUTPUT_COORDINATE_ID = "output_coordinate";
    
    /**
     * 鏋勯€犱竴涓柊鐨勫悜閲忚浆鍧愭爣鑺傜偣
     */
    public VectorToCoordinateNode() {
        // 璋冪敤鐖剁被鏋勯€犲嚱鏁帮紝浣跨敤UUID.randomUUID()鐢熸垚鏂扮殑ID
        super(UUID.randomUUID(), "data.conversion.vector_to_coordinate");
        
        // 璁剧疆鑺傜偣鎻忚堪
        this.description = "Converts floating-point vector to integer block coordinates";
        
        // 鍒涘缓杈撳叆绔彛
        IPort vectorInput = new BasePort(INPUT_VECTOR_ID, "Vector", 
                "The vector (float)", NodeDataType.VECTOR, this);
        addInputPort(vectorInput);
        
        // 鍒涘缓杈撳嚭绔彛
        IPort coordinateOutput = new BasePort(OUTPUT_COORDINATE_ID, "Coordinate", 
                "The converted block coordinates (integer)", NodeDataType.BLOCK_POS, this);
        addOutputPort(coordinateOutput);
    }
    
    /**
     * 瀹炵幇INode鎺ュ彛鐨刧etDescription鏂规硶
     * @return 鑺傜偣鎻忚堪
     */
    @Override
    public String getDescription() {
        return this.description;
    }
    
    /**
     * 鑺傜偣鐨勮绠楅€昏緫
     * @param context 鎵ц涓婁笅鏂?
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 鑾峰彇杈撳叆
        Object vectorObj = inputValues.get(INPUT_VECTOR_ID);
        
        // 榛樿鍧愭爣 (0,0,0)
        BlockPos coordinate = new BlockPos(0, 0, 0);
        
        // 妫€鏌ヨ緭鍏ユ槸鍚︿负鍚戦噺
        if (vectorObj instanceof Vector3d) {
            Vector3d vec = (Vector3d) vectorObj;
            
            // 鏍规嵁鑸嶅叆妯″紡杞崲涓烘暣鏁板潗鏍?
            int x, y, z;
            
            switch (roundingMode) {
                case FLOOR:
                    x = (int) Math.floor(vec.x);
                    y = (int) Math.floor(vec.y);
                    z = (int) Math.floor(vec.z);
                    break;
                case CEIL:
                    x = (int) Math.ceil(vec.x);
                    y = (int) Math.ceil(vec.y);
                    z = (int) Math.ceil(vec.z);
                    break;
                case ROUND:
                default:
                    x = (int) Math.round(vec.x);
                    y = (int) Math.round(vec.y);
                    z = (int) Math.round(vec.z);
                    break;
            }
            
            coordinate = new BlockPos(x, y, z);
        }
        
        // 璁剧疆杈撳嚭
        outputValues.put(OUTPUT_COORDINATE_ID, coordinate);
    }
    
    // --- Getters/Setters for Properties ---
    
    public RoundingMode getRoundingMode() {
        return roundingMode;
    }
    
    public void setRoundingMode(RoundingMode mode) {
        this.roundingMode = mode;
        markDirty();
    }
    
    /**
     * 璁剧疆鑸嶅叆妯″紡锛堝瓧绗︿覆褰㈠紡锛岀敤浜庝粠UI鎴栭厤缃腑璁剧疆锛?
     * @param modeStr 鑸嶅叆妯″紡瀛楃涓诧細"FLOOR", "ROUND", "CEIL"
     */
    public void setRoundingModeString(String modeStr) {
        try {
            setRoundingMode(RoundingMode.valueOf(modeStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            // 濡傛灉瀛楃涓蹭笉鍖归厤浠讳綍妯″紡锛屼娇鐢ㄩ粯璁ょ殑FLOOR
            setRoundingMode(RoundingMode.FLOOR);
        }
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("roundingMode", getRoundingMode().name());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("roundingMode")) {
                Object modeObj = stateMap.get("roundingMode");
                if (modeObj instanceof String) {
                    setRoundingModeString((String) modeObj);
                }
            }
        }
    }
} 