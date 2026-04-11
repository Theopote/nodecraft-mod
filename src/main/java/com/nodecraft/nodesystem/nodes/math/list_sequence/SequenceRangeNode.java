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
 * 鑼冨洿鐢熸垚鑺傜偣锛岀敓鎴愭寚瀹氳寖鍥村拰姝ラ暱鐨勬暟瀛楀簭鍒?
 */
@NodeInfo(
    id = "data.sequence.range",
    displayName = "Sequence Range",
    description = "Generates a sequence of numbers within a specified range",
    category = "data.sequence"
)
public class SequenceRangeNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    private boolean useIntegerType = true; // 鏄惁浣跨敤鏁存暟绫诲瀷锛堝惁鍒欎娇鐢ㄦ诞鐐规暟锛?
    private boolean includeEnd = false; // 鏄惁鍖呭惈缁撴潫鍊?
    private String description; // 瀛樺偍鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_START_ID = "input_start";
    private static final String INPUT_END_ID = "input_end";
    private static final String INPUT_STEP_ID = "input_step";
    private static final String OUTPUT_RANGE_ID = "output_range";
    private static final String OUTPUT_COUNT_ID = "output_count";
    
    /**
     * 鏋勯€犱竴涓柊鐨勮寖鍥寸敓鎴愯妭鐐?
     */
    public SequenceRangeNode() {
        // 璋冪敤鐖剁被鏋勯€犲嚱鏁帮紝浣跨敤UUID.randomUUID()鐢熸垚鏂扮殑ID
        super(UUID.randomUUID(), "data.sequence.range");
        
        // 璁剧疆鑺傜偣鎻忚堪
        this.description = "Generates a sequence of numbers within a specified range";
        
        // 鍒涘缓杈撳叆绔彛
        IPort startInput = new BasePort(INPUT_START_ID, "Start", 
                "Starting value (inclusive)", NodeDataType.DOUBLE, this);
        addInputPort(startInput);
        
        IPort endInput = new BasePort(INPUT_END_ID, "End", 
                "Ending value (exclusive by default)", NodeDataType.DOUBLE, this);
        addInputPort(endInput);
        
        IPort stepInput = new BasePort(INPUT_STEP_ID, "Step", 
                "Increment between values", NodeDataType.DOUBLE, this);
        addInputPort(stepInput);
        
        // 鍒涘缓杈撳嚭绔彛
        IPort rangeOutput = new BasePort(OUTPUT_RANGE_ID, "Range", 
                "The generated number sequence", NodeDataType.LIST, this);
        addOutputPort(rangeOutput);
        
        IPort countOutput = new BasePort(OUTPUT_COUNT_ID, "Count", 
                "The number of elements in the range", NodeDataType.INTEGER, this);
        addOutputPort(countOutput);
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
        Object startObj = inputValues.get(INPUT_START_ID);
        Object endObj = inputValues.get(INPUT_END_ID);
        Object stepObj = inputValues.get(INPUT_STEP_ID);
        
        // 璁剧疆榛樿鍊煎苟澶勭悊杈撳叆
        double start = 0;
        if (startObj instanceof Number) {
            start = ((Number) startObj).doubleValue();
        }
        
        double end = 10;
        if (endObj instanceof Number) {
            end = ((Number) endObj).doubleValue();
        }
        
        double step = 1;
        if (stepObj instanceof Number) {
            step = ((Number) stepObj).doubleValue();
            if (step == 0) {
                step = 1; // 闃叉姝ラ暱涓?瀵艰嚧鏃犻檺寰幆
            }
        }
        
        // 鐢熸垚搴忓垪
        List<Object> range = new ArrayList<>();
        
        // 澶勭悊姝ｅ悜鍜屽弽鍚戣寖鍥?
        boolean ascending = step > 0;
        
        // 纭畾缁堟鏉′欢
        double limitValue = end;
        if (includeEnd) {
            // 濡傛灉鍖呭惈缁撴潫鍊硷紝鍒欏湪鎭板綋鐨勬柟鍚戣皟鏁撮檺鍒跺€?
            if (ascending) {
                limitValue = end + step * 0.5; // 绋嶅井瓒呰繃end锛岀‘淇濆寘鍚玡nd
            } else {
                limitValue = end - step * 0.5; // 绋嶅井浣庝簬end锛岀‘淇濆寘鍚玡nd
            }
        }
        
        // 鐢熸垚搴忓垪
        if (ascending) {
            for (double value = start; value < limitValue; value += step) {
                addValueToRange(range, value);
            }
        } else {
            for (double value = start; value > limitValue; value += step) {
                addValueToRange(range, value);
            }
        }
        
        // 璁剧疆杈撳嚭
        outputValues.put(OUTPUT_RANGE_ID, range);
        outputValues.put(OUTPUT_COUNT_ID, range.size());
    }
    
    /**
     * 灏嗗€兼坊鍔犲埌鑼冨洿鍒楄〃涓紝鏍规嵁鎵€閫夌被鍨嬭浆鎹?
     */
    private void addValueToRange(List<Object> range, double value) {
        if (useIntegerType) {
            range.add((int) Math.round(value));
        } else {
            range.add(value);
        }
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isUseIntegerType() {
        return useIntegerType;
    }
    
    public void setUseIntegerType(boolean useInt) {
        this.useIntegerType = useInt;
        markDirty();
    }
    
    public boolean isIncludeEnd() {
        return includeEnd;
    }
    
    public void setIncludeEnd(boolean include) {
        this.includeEnd = include;
        markDirty();
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("useIntegerType", isUseIntegerType());
        state.put("includeEnd", isIncludeEnd());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("useIntegerType")) {
                Object useInt = stateMap.get("useIntegerType");
                if (useInt instanceof Boolean) {
                    setUseIntegerType((Boolean) useInt);
                }
            }
            
            if (stateMap.containsKey("includeEnd")) {
                Object include = stateMap.get("includeEnd");
                if (include instanceof Boolean) {
                    setIncludeEnd((Boolean) include);
                }
            }
        }
    }
} 