package com.nodecraft.nodesystem.nodes.math.list_sequence;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 閲嶅鏁版嵁鑺傜偣锛屽皢鍗曚釜鏁版嵁鎴栧垪琛ㄩ噸澶嶆寚瀹氭鏁?
 */
@NodeInfo(
    id = "data.sequence.repeat",
    displayName = "Repeat",
    description = "Repeats a single data item or list multiple times",
    category = "data.sequence"
)
public class RepeatNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    private int defaultCount = 3; // 榛樿閲嶅娆℃暟
    private String description; // 瀛樺偍鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_DATA_ID = "input_data";
    private static final String INPUT_COUNT_ID = "input_count";
    private static final String OUTPUT_RESULT_ID = "output_result";
    private static final String OUTPUT_LENGTH_ID = "output_length";
    
    /**
     * 鏋勯€犱竴涓柊鐨勯噸澶嶆暟鎹妭鐐?
     */
    public RepeatNode() {
        // 璋冪敤鐖剁被鏋勯€犲嚱鏁帮紝浣跨敤UUID.randomUUID()鐢熸垚鏂扮殑ID
        super(UUID.randomUUID(), "data.sequence.repeat");
        
        // 璁剧疆鑺傜偣鎻忚堪
        this.description = "Repeats a single data item or list multiple times";
        
        // 鍒涘缓杈撳叆绔彛
        IPort dataInput = new BasePort(INPUT_DATA_ID, "Data", 
                "The data to repeat (single value or list)", NodeDataType.ANY, this);
        addInputPort(dataInput);
        
        IPort countInput = new BasePort(INPUT_COUNT_ID, "Count", 
                "Number of times to repeat", NodeDataType.INTEGER, this);
        addInputPort(countInput);
        
        // 鍒涘缓杈撳嚭绔彛
        IPort resultOutput = new BasePort(OUTPUT_RESULT_ID, "Result", 
                "The repeated data as a list", NodeDataType.LIST, this);
        addOutputPort(resultOutput);
        
        IPort lengthOutput = new BasePort(OUTPUT_LENGTH_ID, "Length", 
                "Length of the resulting list", NodeDataType.INTEGER, this);
        addOutputPort(lengthOutput);
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
        Object dataObj = inputValues.get(INPUT_DATA_ID);
        Object countObj = inputValues.get(INPUT_COUNT_ID);
        
        // 璁剧疆榛樿鍊煎苟澶勭悊杈撳叆
        int count = defaultCount;
        if (countObj instanceof Number) {
            count = ((Number) countObj).intValue();
            // 纭繚鏁伴噺涓烘鏁?
            count = Math.max(0, count);
        }
        
        // 澶勭悊杈撳叆鏁版嵁鏄垪琛ㄧ殑鎯呭喌
        if (dataObj instanceof List) {
            List<?> inputList = (List<?>) dataObj;
            List<Object> result = new ArrayList<>();
            
            // 閲嶅鍒楄〃鎸囧畾娆℃暟
            for (int i = 0; i < count; i++) {
                result.addAll(inputList);
            }
            
            // 璁剧疆杈撳嚭
            outputValues.put(OUTPUT_RESULT_ID, result);
            outputValues.put(OUTPUT_LENGTH_ID, result.size());
        } 
        // 澶勭悊杈撳叆鏁版嵁鏄崟涓€肩殑鎯呭喌
        else {
            List<Object> result = new ArrayList<>();
            
            // 閲嶅鍗曚釜鍊兼寚瀹氭鏁?
            for (int i = 0; i < count; i++) {
                result.add(dataObj);
            }
            
            // 璁剧疆杈撳嚭
            outputValues.put(OUTPUT_RESULT_ID, result);
            outputValues.put(OUTPUT_LENGTH_ID, result.size());
        }
    }
    
    // --- Getters/Setters for Properties ---
    
    public int getDefaultCount() {
        return defaultCount;
    }
    
    public void setDefaultCount(int count) {
        this.defaultCount = Math.max(0, count);
        markDirty();
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("defaultCount", getDefaultCount());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("defaultCount")) {
                Object count = stateMap.get("defaultCount");
                if (count instanceof Number) {
                    setDefaultCount(((Number) count).intValue());
                }
            }
        }
    }
} 