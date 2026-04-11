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
 * Number to Integer 鑺傜偣锛屽皢娴偣鏁拌浆鎹负鏁存暟锛屾敮鎸丗loor銆丷ound銆丆eil妯″紡
 */
@NodeInfo(
    id = "data.conversion.number_to_integer",
    displayName = "Number to Integer",
    description = "Converts a floating point number to an integer with selectable rounding mode",
    category = "data.conversion"
)
public class NumberToIntegerNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    public enum RoundingMode {
        FLOOR, ROUND, CEIL
    }
    
    private RoundingMode roundingMode = RoundingMode.ROUND; // 榛樿涓哄洓鑸嶄簲鍏?
    private String description; // 瀛樺偍鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_NUMBER_ID = "input_number";
    private static final String OUTPUT_INTEGER_ID = "output_integer";
    
    /**
     * 鏋勯€犱竴涓柊鐨勬暟瀛楄浆鏁存暟鑺傜偣
     */
    public NumberToIntegerNode() {
        // 璋冪敤鐖剁被鏋勯€犲嚱鏁帮紝浣跨敤UUID.randomUUID()鐢熸垚鏂扮殑ID
        super(UUID.randomUUID(), "data.conversion.number_to_integer");
        
        // 璁剧疆鑺傜偣鎻忚堪
        this.description = "Converts a floating point number to an integer with selectable rounding mode";
        
        // 鍒涘缓杈撳叆绔彛
        IPort numberInput = new BasePort(INPUT_NUMBER_ID, "Number", 
                "The input number (float/double)", NodeDataType.DOUBLE, this);
        addInputPort(numberInput);
        
        // 鍒涘缓杈撳嚭绔彛
        IPort integerOutput = new BasePort(OUTPUT_INTEGER_ID, "Integer", 
                "The converted integer value", NodeDataType.INTEGER, this);
        addOutputPort(integerOutput);
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
        
        // 榛樿杈撳嚭涓?
        int result = 0;
        
        // 妫€鏌ヨ緭鍏ユ槸鍚︿负鏁板瓧
        if (numberObj instanceof Number) {
            double number = ((Number) numberObj).doubleValue();
            
            // 鏍规嵁鑸嶅叆妯″紡杩涜杞崲
            switch (roundingMode) {
                case FLOOR:
                    result = (int) Math.floor(number);
                    break;
                case CEIL:
                    result = (int) Math.ceil(number);
                    break;
                case ROUND:
                default:
                    result = (int) Math.round(number);
                    break;
            }
        }
        
        // 璁剧疆杈撳嚭
        outputValues.put(OUTPUT_INTEGER_ID, result);
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
            // 濡傛灉瀛楃涓蹭笉鍖归厤浠讳綍妯″紡锛屼娇鐢ㄩ粯璁ょ殑ROUND
            setRoundingMode(RoundingMode.ROUND);
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