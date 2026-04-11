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
 * 瀛愬垪琛ㄨ妭鐐癸紝鑾峰彇鍒楄〃鐨勫瓙闆?
 */
@NodeInfo(
    id = "data.lists.sub_list",
    displayName = "瀛愬垪琛?,
    description = "鑾峰彇鍒楄〃鐨勫瓙闆嗭紙鎸囧畾璧峰鍜岀粨鏉熺储寮曪級",
    category = "data.lists"
)
public class SubListNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    private boolean allowNegativeIndex = true; // 鏄惁鍏佽璐熺储寮曪紙浠庡垪琛ㄦ湯灏惧紑濮嬭绠楋級
    private boolean clampToList = true; // 鏄惁灏嗙储寮曢檺鍒跺湪鍒楄〃鑼冨洿鍐?
    private String description; // 瀛樺偍鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_START_ID = "input_start";
    private static final String INPUT_END_ID = "input_end";
    private static final String OUTPUT_SUBLIST_ID = "output_sublist";
    
    /**
     * 鏋勯€犱竴涓柊鐨勫瓙鍒楄〃鑺傜偣
     */
    public SubListNode() {
        // 璋冪敤鐖剁被鏋勯€犲嚱鏁帮紝浣跨敤UUID.randomUUID()鐢熸垚鏂扮殑ID
        super(UUID.randomUUID(), "data.lists.sub_list");
        
        // 璁剧疆鑺傜偣鎻忚堪
        this.description = "Gets a portion of a list between start and end indexes";
        
        // 鍒涘缓杈撳叆绔彛
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The source list", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        IPort startInput = new BasePort(INPUT_START_ID, "Start Index", 
                "The starting index (inclusive, 0-based)", NodeDataType.INTEGER, this);
        addInputPort(startInput);
        
        IPort endInput = new BasePort(INPUT_END_ID, "End Index", 
                "The ending index (exclusive)", NodeDataType.INTEGER, this);
        addInputPort(endInput);
        
        // 鍒涘缓杈撳嚭绔彛
        IPort sublistOutput = new BasePort(OUTPUT_SUBLIST_ID, "Sub List", 
                "The resulting sub list", NodeDataType.LIST, this);
        addOutputPort(sublistOutput);
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
        Object inputObj = inputValues.get(INPUT_LIST_ID);
        Object startObj = inputValues.get(INPUT_START_ID);
        Object endObj = inputValues.get(INPUT_END_ID);
        
        List<Object> resultList = new ArrayList<>();
        
        // 澶勭悊鍒楄〃
        if (inputObj instanceof List) {
            List<?> inputList = (List<?>) inputObj;
            int listSize = inputList.size();
            
            // 榛樿璧峰鍜岀粨鏉熺储寮?
            int startIndex = 0;
            int endIndex = listSize;
            
            // 澶勭悊璧峰绱㈠紩
            if (startObj instanceof Number) {
                startIndex = ((Number) startObj).intValue();
                
                // 澶勭悊璐熺储寮?
                if (startIndex < 0 && allowNegativeIndex) {
                    startIndex = listSize + startIndex;
                }
                
                // 闄愬埗鍦ㄥ垪琛ㄨ寖鍥村唴
                if (clampToList) {
                    startIndex = Math.max(0, Math.min(startIndex, listSize));
                } else if (startIndex < 0 || startIndex > listSize) {
                    // 濡傛灉涓嶉檺鍒朵笖瓒呭嚭鑼冨洿锛岃繑鍥炵┖鍒楄〃
                    outputValues.put(OUTPUT_SUBLIST_ID, resultList);
                    return;
                }
            }
            
            // 澶勭悊缁撴潫绱㈠紩
            if (endObj instanceof Number) {
                endIndex = ((Number) endObj).intValue();
                
                // 澶勭悊璐熺储寮?
                if (endIndex < 0 && allowNegativeIndex) {
                    endIndex = listSize + endIndex;
                }
                
                // 闄愬埗鍦ㄥ垪琛ㄨ寖鍥村唴
                if (clampToList) {
                    endIndex = Math.max(startIndex, Math.min(endIndex, listSize));
                } else if (endIndex < startIndex || endIndex > listSize) {
                    // 濡傛灉涓嶉檺鍒朵笖缁撴潫绱㈠紩鏃犳晥锛屽垯璋冩暣澶勭悊
                    if (endIndex < startIndex) {
                        outputValues.put(OUTPUT_SUBLIST_ID, resultList);
                        return;
                    }
                    endIndex = Math.min(endIndex, listSize);
                }
            }
            
            // 鍒涘缓瀛愬垪琛?
            for (int i = startIndex; i < endIndex; i++) {
                resultList.add(inputList.get(i));
            }
        }
        
        // 璁剧疆杈撳嚭
        outputValues.put(OUTPUT_SUBLIST_ID, resultList);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isAllowNegativeIndex() {
        return allowNegativeIndex;
    }
    
    public void setAllowNegativeIndex(boolean allow) {
        this.allowNegativeIndex = allow;
        markDirty();
    }
    
    public boolean isClampToList() {
        return clampToList;
    }
    
    public void setClampToList(boolean clamp) {
        this.clampToList = clamp;
        markDirty();
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("allowNegativeIndex", isAllowNegativeIndex());
        state.put("clampToList", isClampToList());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("allowNegativeIndex")) {
                Object allow = stateMap.get("allowNegativeIndex");
                if (allow instanceof Boolean) {
                    setAllowNegativeIndex((Boolean) allow);
                }
            }
            
            if (stateMap.containsKey("clampToList")) {
                Object clamp = stateMap.get("clampToList");
                if (clamp instanceof Boolean) {
                    setClampToList((Boolean) clamp);
                }
            }
        }
    }
} 