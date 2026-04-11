package com.nodecraft.nodesystem.nodes.utilities.text_processing;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Concatenate Text 鑺傜偣锛岃繛鎺ュ涓枃鏈垨鏁版嵁涓哄瓧绗︿覆
 */
@NodeInfo(
    id = "data.text.concatenate",
    displayName = "鏂囨湰杩炴帴",
    description = "灏嗗涓枃鏈緭鍏ヨ繛鎺ユ垚鍗曚釜瀛楃涓?,
    category = "data.text"
)
public class ConcatenateTextNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    private boolean addSpacesBetween = true; // 鏄惁鍦ㄨ繛鎺ョ殑鏂囨湰涔嬮棿娣诲姞绌烘牸
    private int inputCount = 3; // 榛樿杈撳叆鏁伴噺
    private String description = "Concatenates multiple text inputs into a single string"; // 鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID鍓嶇紑 ---
    private static final String INPUT_PREFIX = "input_text_";
    private static final String OUTPUT_RESULT_ID = "output_result";
    
    /**
     * 鏋勯€犱竴涓柊鐨勬枃鏈繛鎺ヨ妭鐐?
     */
    public ConcatenateTextNode() {
        // 浣跨敤涓嶧indReplaceTextNode鐩稿悓鐨勬瀯閫犲嚱鏁版牸寮?
        super(UUID.randomUUID(), "data.text.concatenate");
        
        // 鍒涘缓鍔ㄦ€佽緭鍏ョ鍙?
        recreateInputPorts();
        
        // 鍒涘缓杈撳嚭绔彛
        IPort resultOutput = new BasePort(OUTPUT_RESULT_ID, "Result", 
                "The concatenated string", NodeDataType.STRING, this);
        addOutputPort(resultOutput);
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
     * 閲嶆柊鍒涘缓鎵€鏈夎緭鍏ョ鍙?
     */
    private void recreateInputPorts() {
        // 娓呴櫎鐜版湁杈撳叆绔彛
        inputPorts.clear();
        
        // 鍒涘缓鏂扮殑杈撳叆绔彛
        for (int i = 0; i < inputCount; i++) {
            String portId = INPUT_PREFIX + i;
            String displayName = "Text " + (i + 1);
            IPort textInput = new BasePort(portId, displayName, 
                    "Text input " + (i + 1), NodeDataType.ANY, this);
            addInputPort(textInput);
        }
    }
    
    /**
     * 鑺傜偣鐨勮绠楅€昏緫
     * @param context 鎵ц涓婁笅鏂?
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        StringBuilder result = new StringBuilder();
        
        // 鏀堕泦闈炵┖杈撳叆骞惰繛鎺?
        boolean isFirst = true;
        for (int i = 0; i < inputCount; i++) {
            String portId = INPUT_PREFIX + i;
            Object input = inputValues.get(portId);
            
            if (input != null) {
                String textValue = input.toString();
                
                // 濡傛灉涓嶆槸绗竴涓笖闇€瑕佹坊鍔犵┖鏍?
                if (!isFirst && addSpacesBetween) {
                    result.append(" ");
                }
                
                result.append(textValue);
                isFirst = false;
            }
        }
        
        // 璁剧疆杈撳嚭
        outputValues.put(OUTPUT_RESULT_ID, result.toString());
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isAddSpacesBetween() {
        return addSpacesBetween;
    }
    
    public void setAddSpacesBetween(boolean addSpaces) {
        this.addSpacesBetween = addSpaces;
        markDirty();
    }
    
    public int getInputCount() {
        return inputCount;
    }
    
    public void setInputCount(int count) {
        // 纭繚杈撳叆鏁伴噺鏈夋晥
        count = Math.max(1, Math.min(10, count)); // 闄愬埗鍦?-10涔嬮棿
        
        if (this.inputCount != count) {
            this.inputCount = count;
            recreateInputPorts();
            markDirty();
        }
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("addSpacesBetween", isAddSpacesBetween());
        state.put("inputCount", getInputCount());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("addSpacesBetween")) {
                Object addSpacesObj = stateMap.get("addSpacesBetween");
                if (addSpacesObj instanceof Boolean) {
                    setAddSpacesBetween((Boolean) addSpacesObj);
                }
            }
            
            if (stateMap.containsKey("inputCount")) {
                Object countObj = stateMap.get("inputCount");
                if (countObj instanceof Number) {
                    setInputCount(((Number) countObj).intValue());
                }
            }
        }
    }
} 