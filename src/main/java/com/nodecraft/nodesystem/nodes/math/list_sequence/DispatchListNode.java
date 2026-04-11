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
 * 鍒嗗彂鍒楄〃鑺傜偣锛屾牴鎹竷灏旀潯浠跺垪琛ㄥ皢涓诲垪琛ㄥ垎鎴愪袱涓垪琛?
 */
@NodeInfo(
    id = "data.lists.dispatch_list",
    displayName = "Dispatch List",
    description = "Splits a list into two based on boolean conditions",
    category = "data.lists"
)
public class DispatchListNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    private boolean useDefaultValue = false; // 鏄惁浣跨敤榛樿鍊煎鐞嗙储寮曚笉鍖归厤鐨勬儏鍐?
    private boolean defaultValue = false; // 榛樿甯冨皵鍊?
    private String description; // 瀛樺偍鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_CONDITION_ID = "input_condition";
    private static final String OUTPUT_TRUE_LIST_ID = "output_true";
    private static final String OUTPUT_FALSE_LIST_ID = "output_false";
    
    /**
     * 鏋勯€犱竴涓柊鐨勫垎鍙戝垪琛ㄨ妭鐐?
     */
    public DispatchListNode() {
        // 璋冪敤鐖剁被鏋勯€犲嚱鏁帮紝浣跨敤UUID.randomUUID()鐢熸垚鏂扮殑ID
        super(UUID.randomUUID(), "data.lists.dispatch_list");
        
        // 璁剧疆鑺傜偣鎻忚堪
        this.description = "Splits a list into two based on boolean conditions";
        
        // 鍒涘缓杈撳叆绔彛
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list to split", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        IPort conditionInput = new BasePort(INPUT_CONDITION_ID, "Condition", 
                "Boolean list or single boolean value", NodeDataType.ANY, this);
        addInputPort(conditionInput);
        
        // 鍒涘缓杈撳嚭绔彛
        IPort trueOutput = new BasePort(OUTPUT_TRUE_LIST_ID, "True List", 
                "Items for which the condition was true", NodeDataType.LIST, this);
        addOutputPort(trueOutput);
        
        IPort falseOutput = new BasePort(OUTPUT_FALSE_LIST_ID, "False List", 
                "Items for which the condition was false", NodeDataType.LIST, this);
        addOutputPort(falseOutput);
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
        
        List<Object> trueList = new ArrayList<>();
        List<Object> falseList = new ArrayList<>();
        
        // 澶勭悊鍒楄〃
        if (listObj instanceof List) {
            List<?> inputList = (List<?>) listObj;
            
            // 妫€鏌ユ潯浠舵槸鍚︿负鍒楄〃鎴栧崟涓竷灏斿€?
            if (conditionObj instanceof List) {
                List<?> conditionList = (List<?>) conditionObj;
                processWithConditionList(inputList, conditionList, trueList, falseList);
            } else if (conditionObj instanceof Boolean) {
                // 鍗曚釜甯冨皵鍊?- 鍏ㄩ儴鏀惧叆鐩稿簲鍒楄〃
                boolean condition = (Boolean) conditionObj;
                if (condition) {
                    trueList.addAll(inputList);
                } else {
                    falseList.addAll(inputList);
                }
            } else {
                // 鏃犳潯浠?- 鏍规嵁榛樿鍊煎鐞?
                if (useDefaultValue && defaultValue) {
                    trueList.addAll(inputList);
                } else {
                    falseList.addAll(inputList);
                }
            }
        }
        
        // 璁剧疆杈撳嚭
        outputValues.put(OUTPUT_TRUE_LIST_ID, trueList);
        outputValues.put(OUTPUT_FALSE_LIST_ID, falseList);
    }
    
    /**
     * 浣跨敤鏉′欢鍒楄〃澶勭悊鍒嗗彂
     */
    private void processWithConditionList(List<?> inputList, List<?> conditionList, 
                                         List<Object> trueList, List<Object> falseList) {
        // 閬嶅巻涓诲垪琛?
        for (int i = 0; i < inputList.size(); i++) {
            Object item = inputList.get(i);
            
            // 妫€鏌ユ潯浠?
            boolean condition;
            if (i < conditionList.size()) {
                Object condObj = conditionList.get(i);
                if (condObj instanceof Boolean) {
                    condition = (Boolean) condObj;
                } else {
                    // 闈炲竷灏斿€硷紝瑙嗕负false
                    condition = false;
                }
            } else {
                // 鏉′欢鍒楄〃闀垮害涓嶈冻
                condition = useDefaultValue ? defaultValue : false;
            }
            
            // 鏍规嵁鏉′欢鍒嗛厤
            if (condition) {
                trueList.add(item);
            } else {
                falseList.add(item);
            }
        }
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isUseDefaultValue() {
        return useDefaultValue;
    }
    
    public void setUseDefaultValue(boolean use) {
        this.useDefaultValue = use;
        markDirty();
    }
    
    public boolean getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(boolean value) {
        this.defaultValue = value;
        markDirty();
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("useDefaultValue", isUseDefaultValue());
        state.put("defaultValue", getDefaultValue());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("useDefaultValue")) {
                Object use = stateMap.get("useDefaultValue");
                if (use instanceof Boolean) {
                    setUseDefaultValue((Boolean) use);
                }
            }
            
            if (stateMap.containsKey("defaultValue")) {
                Object value = stateMap.get("defaultValue");
                if (value instanceof Boolean) {
                    setDefaultValue((Boolean) value);
                }
            }
        }
    }
} 