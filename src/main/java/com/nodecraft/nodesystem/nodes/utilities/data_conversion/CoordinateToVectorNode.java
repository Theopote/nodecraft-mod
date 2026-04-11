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
 * Coordinate to Vector 鑺傜偣锛屽皢鏁存暟鍧愭爣杞崲涓烘诞鐐瑰潗鏍?鍚戦噺
 */
@NodeInfo(
    id = "data.conversion.coordinate_to_vector",
    displayName = "Coordinate to Vector",
    description = "Converts integer block coordinates to floating-point vector",
    category = "data.conversion"
)
public class CoordinateToVectorNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    private boolean addHalfBlock = false; // 鏄惁娣诲姞0.5鍋忕Щ锛堟柟鍧椾腑蹇冿級
    private String description; // 瀛樺偍鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_COORDINATE_ID = "input_coordinate";
    private static final String OUTPUT_VECTOR_ID = "output_vector";
    
    /**
     * 鏋勯€犱竴涓柊鐨勫潗鏍囪浆鍚戦噺鑺傜偣
     */
    public CoordinateToVectorNode() {
        // 璋冪敤鐖剁被鏋勯€犲嚱鏁帮紝浣跨敤UUID.randomUUID()鐢熸垚鏂扮殑ID
        super(UUID.randomUUID(), "data.conversion.coordinate_to_vector");
        
        // 璁剧疆鑺傜偣鎻忚堪
        this.description = "Converts integer block coordinates to floating-point vector";
        
        // 鍒涘缓杈撳叆绔彛
        IPort coordinateInput = new BasePort(INPUT_COORDINATE_ID, "Coordinate", 
                "The block coordinates (integer)", NodeDataType.BLOCK_POS, this);
        addInputPort(coordinateInput);
        
        // 鍒涘缓杈撳嚭绔彛
        IPort vectorOutput = new BasePort(OUTPUT_VECTOR_ID, "Vector", 
                "The converted vector (float)", NodeDataType.VECTOR, this);
        addOutputPort(vectorOutput);
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
        Object coordinateObj = inputValues.get(INPUT_COORDINATE_ID);
        
        // 榛樿鍚戦噺 (0,0,0)
        Vector3d vector = new Vector3d(0, 0, 0);
        
        // 妫€鏌ヨ緭鍏ユ槸鍚︿负鏂瑰潡鍧愭爣
        if (coordinateObj instanceof BlockPos) {
            BlockPos pos = (BlockPos) coordinateObj;
            double offset = addHalfBlock ? 0.5 : 0.0;
            vector = new Vector3d(
                pos.getX() + offset,
                pos.getY() + offset,
                pos.getZ() + offset
            );
        }
        
        // 璁剧疆杈撳嚭
        outputValues.put(OUTPUT_VECTOR_ID, vector);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isAddHalfBlock() {
        return addHalfBlock;
    }
    
    public void setAddHalfBlock(boolean addOffset) {
        this.addHalfBlock = addOffset;
        markDirty();
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("addHalfBlock", isAddHalfBlock());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("addHalfBlock")) {
                Object addOffsetObj = stateMap.get("addHalfBlock");
                if (addOffsetObj instanceof Boolean) {
                    setAddHalfBlock((Boolean) addOffsetObj);
                }
            }
        }
    }
} 