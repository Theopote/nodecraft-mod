package com.nodecraft.nodesystem.nodes.utilities.data_conversion;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Number to Boolean 鑺傜偣锛屽皢鏁板瓧杞崲涓哄竷灏斿€?
 * 0杞崲涓篺alse锛岄潪0杞崲涓簍rue
 */
@NodeInfo(
    id = "data.conversion.number_to_boolean",
    displayName = "Number to Boolean",
    description = "Converts a number to boolean (0 = false, non-zero = true)",
    category = "data.conversion"
)
public class NumberToBooleanNode extends BaseNode {
    
    // --- 灞炴€?---
    private boolean invertResult = false; // 鏄惁鍙嶈浆缁撴灉
    private String description; // 瀛樺偍鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_NUMBER_ID = "input_number";
    private static final String OUTPUT_BOOLEAN_ID = "output_boolean";
    
    /**
     * 鏋勯€犱竴涓柊鐨勬暟瀛楄浆甯冨皵鍊艰妭鐐?
     */
    public NumberToBooleanNode() {
        // 璋冪敤鐖剁被鏋勯€犲嚱鏁帮紝浣跨敤UUID.randomUUID()鐢熸垚鏂扮殑ID
        super(UUID.randomUUID(), "data.conversion.number_to_boolean");
        
        // 璁剧疆鑺傜偣鎻忚堪
        this.description = "Converts a number to boolean (0 = false, non-zero = true)";
        
        // 鍒涘缓杈撳叆绔彛
        IPort numberInput = new BasePort(INPUT_NUMBER_ID, "Number", 
                "The input number", NodeDataType.DOUBLE, this);
        addInputPort(numberInput);
        
        // 鍒涘缓杈撳嚭绔彛
        IPort booleanOutput = new BasePort(OUTPUT_BOOLEAN_ID, "Boolean", 
                "The converted boolean value", NodeDataType.BOOLEAN, this);
        addOutputPort(booleanOutput);
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
        Object numberObj = inputValues.get(INPUT_NUMBER_ID);
        
        // 榛樿杈撳嚭涓篺alse
        boolean result = false;
        
        // 妫€鏌ヨ緭鍏ユ槸鍚︿负鏁板瓧
        if (numberObj instanceof Number) {
            double number = ((Number) numberObj).doubleValue();
            // 濡傛灉鏁板瓧涓嶇瓑浜?锛屽垯涓簍rue
            result = number != 0;
        }
        
        // 濡傛灉璁剧疆浜嗗弽杞粨鏋滐紝鍒欏彇鍙?
        if (invertResult) {
            result = !result;
        }
        
        // 璁剧疆杈撳嚭
        outputValues.put(OUTPUT_BOOLEAN_ID, result);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isInvertResult() {
        return invertResult;
    }
    
    public void setInvertResult(boolean invert) {
        this.invertResult = invert;
        markDirty();
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("invertResult", isInvertResult());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("invertResult")) {
                Object invertObj = stateMap.get("invertResult");
                if (invertObj instanceof Boolean) {
                    setInvertResult((Boolean) invertObj);
                }
            }
        }
    }
} 