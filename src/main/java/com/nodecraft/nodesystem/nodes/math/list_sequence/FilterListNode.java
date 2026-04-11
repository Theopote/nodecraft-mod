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
 * 杩囨护鍒楄〃鑺傜偣锛屾牴鎹竷灏旀潯浠朵繚鐣欐垨鍒犻櫎鍒楄〃涓殑鍏冪礌
 */
@NodeInfo(
    id = "data.lists.filter_list",
    displayName = "Filter List",
    description = "Filters a list based on boolean conditions",
    category = "data.lists"
)
public class FilterListNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    private boolean invert = false; // 鏄惁鍙嶈浆杩囨护鏉′欢锛堜繚鐣欐潯浠朵负false鐨勯」锛?
    private String description; // 瀛樺偍鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_CONDITION_ID = "input_condition";
    private static final String OUTPUT_LIST_ID = "output_list";
    private static final String OUTPUT_REMOVED_ID = "output_removed";
    private static final String OUTPUT_COUNT_ID = "output_count";
    
    /**
     * 鏋勯€犱竴涓柊鐨勮繃婊ゅ垪琛ㄨ妭鐐?
     */
    public FilterListNode() {
        // 璋冪敤鐖剁被鏋勯€犲嚱鏁帮紝浣跨敤UUID.randomUUID()鐢熸垚鏂扮殑ID
        super(UUID.randomUUID(), "data.lists.filter_list");
        
        // 璁剧疆鑺傜偣鎻忚堪
        this.description = "Filters a list based on boolean conditions";
        
        // 鍒涘缓杈撳叆绔彛
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list to filter", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        IPort conditionInput = new BasePort(INPUT_CONDITION_ID, "Condition", 
                "Boolean list or single boolean value", NodeDataType.ANY, this);
        addInputPort(conditionInput);
        
        // 鍒涘缓杈撳嚭绔彛
        IPort listOutput = new BasePort(OUTPUT_LIST_ID, "Filtered List", 
                "The list after filtering", NodeDataType.LIST, this);
        addOutputPort(listOutput);
        
        IPort removedOutput = new BasePort(OUTPUT_REMOVED_ID, "Removed Items", 
                "Items that were filtered out", NodeDataType.LIST, this);
        addOutputPort(removedOutput);
        
        IPort countOutput = new BasePort(OUTPUT_COUNT_ID, "Count", 
                "Number of items after filtering", NodeDataType.INTEGER, this);
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
        Object listObj = inputValues.get(INPUT_LIST_ID);
        Object conditionObj = inputValues.get(INPUT_CONDITION_ID);
        
        List<Object> filteredList = new ArrayList<>();
        List<Object> removedList = new ArrayList<>();
        
        // 澶勭悊鍒楄〃
        if (listObj instanceof List) {
            List<?> inputList = (List<?>) listObj;
            
            // 妫€鏌ユ潯浠舵槸鍚︿负鍒楄〃鎴栧崟涓竷灏斿€?
            if (conditionObj instanceof List) {
                List<?> conditionList = (List<?>) conditionObj;
                filterWithConditionList(inputList, conditionList, filteredList, removedList);
            } else if (conditionObj instanceof Boolean) {
                // 鍗曚釜甯冨皵鍊?- 鍏ㄩ儴淇濈暀鎴栧叏閮ㄧЩ闄?
                boolean condition = (Boolean) conditionObj;
                if (condition != invert) { // 鏍规嵁invert鍐冲畾鏄惁鍙嶈浆鏉′欢
                    filteredList.addAll(inputList);
                } else {
                    removedList.addAll(inputList);
                }
            } else {
                // 鏃犳潯浠?- 鍏ㄩ儴绉婚櫎
                removedList.addAll(inputList);
            }
        }
        
        // 璁剧疆杈撳嚭
        outputValues.put(OUTPUT_LIST_ID, filteredList);
        outputValues.put(OUTPUT_REMOVED_ID, removedList);
        outputValues.put(OUTPUT_COUNT_ID, filteredList.size());
    }
    
    /**
     * 浣跨敤鏉′欢鍒楄〃杩涜杩囨护
     */
    private void filterWithConditionList(List<?> inputList, List<?> conditionList, 
                                       List<Object> filteredList, List<Object> removedList) {
        // 閬嶅巻涓诲垪琛?
        for (int i = 0; i < inputList.size(); i++) {
            Object item = inputList.get(i);
            
            // 妫€鏌ユ潯浠?
            boolean keep = false;
            if (i < conditionList.size()) {
                Object condObj = conditionList.get(i);
                if (condObj instanceof Boolean) {
                    keep = (Boolean) condObj;
                }
            }
            
            // 鏍规嵁invert灞炴€у弽杞潯浠?
            if (invert) {
                keep = !keep;
            }
            
            // 鏍规嵁鏉′欢鍒嗛厤
            if (keep) {
                filteredList.add(item);
            } else {
                removedList.add(item);
            }
        }
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isInvert() {
        return invert;
    }
    
    public void setInvert(boolean invert) {
        this.invert = invert;
        markDirty();
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("invert", isInvert());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("invert")) {
                Object invertObj = stateMap.get("invert");
                if (invertObj instanceof Boolean) {
                    setInvert((Boolean) invertObj);
                }
            }
        }
    }
} 