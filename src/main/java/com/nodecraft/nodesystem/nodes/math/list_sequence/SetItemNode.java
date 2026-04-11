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
 * 璁剧疆鍒楄〃椤硅妭鐐癸紝淇敼鍒楄〃涓寚瀹氱储寮曚綅缃殑鍏冪礌
 */
@NodeInfo(
    id = "data.lists.set_item",
    displayName = "Set Item",
    description = "Sets an item in a list at a specified index",
    category = "data.lists"
)
public class SetItemNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    private boolean allowNegativeIndex = true; // 鏄惁鍏佽璐熺储寮曪紙浠庡垪琛ㄦ湯灏惧紑濮嬭绠楋級
    private boolean wrapIndex = false; // 鏄惁瀵圭储寮曡繘琛屽惊鐜寘瑁?
    private boolean expandList = false; // 濡傛灉绱㈠紩瓒呭嚭鑼冨洿锛屾槸鍚︽墿灞曞垪琛?
    private String description; // 瀛樺偍鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_INDEX_ID = "input_index";
    private static final String INPUT_VALUE_ID = "input_value";
    private static final String OUTPUT_LIST_ID = "output_list";
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    
    /**
     * 鏋勯€犱竴涓柊鐨勮缃垪琛ㄩ」鑺傜偣
     */
    public SetItemNode() {
        // 璋冪敤鐖剁被鏋勯€犲嚱鏁帮紝浣跨敤UUID.randomUUID()鐢熸垚鏂扮殑ID
        super(UUID.randomUUID(), "data.lists.set_item");
        
        // 璁剧疆鑺傜偣鎻忚堪
        this.description = "Sets an item in a list at a specified index";
        
        // 鍒涘缓杈撳叆绔彛
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list to modify", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        IPort indexInput = new BasePort(INPUT_INDEX_ID, "Index", 
                "The index of the item to set (0-based)", NodeDataType.INTEGER, this);
        addInputPort(indexInput);
        
        IPort valueInput = new BasePort(INPUT_VALUE_ID, "Value", 
                "The new value to set at the specified index", NodeDataType.ANY, this);
        addInputPort(valueInput);
        
        // 鍒涘缓杈撳嚭绔彛
        IPort listOutput = new BasePort(OUTPUT_LIST_ID, "Modified List", 
                "The modified list with the new value set", NodeDataType.LIST, this);
        addOutputPort(listOutput);
        
        IPort successOutput = new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "Whether the operation was successful", NodeDataType.BOOLEAN, this);
        addOutputPort(successOutput);
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
        Object indexObj = inputValues.get(INPUT_INDEX_ID);
        Object valueObj = inputValues.get(INPUT_VALUE_ID);
        
        List<Object> resultList = new ArrayList<>();
        boolean success = false;
        
        // 澶勭悊鍒楄〃
        if (inputObj instanceof List && indexObj instanceof Number) {
            List<?> inputList = (List<?>) inputObj;
            
            // 鍒涘缓涓€涓彲淇敼鐨勬柊鍒楄〃
            for (Object item : inputList) {
                resultList.add(item);
            }
            
            int listSize = resultList.size();
            int index = ((Number) indexObj).intValue();
            
            // 澶勭悊璐熺储寮曪紙浠庡垪琛ㄦ湯灏惧紑濮嬭绠楋級
            if (index < 0 && allowNegativeIndex) {
                index = listSize + index;
            }
            
            // 澶勭悊绱㈠紩鍖呰
            if (wrapIndex && listSize > 0) {
                // 瀵瑰垪琛ㄩ暱搴﹀彇妯★紝纭繚绱㈠紩鎬绘槸鏈夋晥鐨?
                index = ((index % listSize) + listSize) % listSize;
                resultList.set(index, valueObj);
                success = true;
            } else if (index >= 0 && index < listSize) {
                // 甯歌绱㈠紩璁剧疆
                resultList.set(index, valueObj);
                success = true;
            } else if (expandList && index >= 0) {
                // 鎵╁睍鍒楄〃浠ラ€傚簲绱㈠紩
                while (resultList.size() <= index) {
                    resultList.add(null);
                }
                resultList.set(index, valueObj);
                success = true;
            }
        }
        
        // 璁剧疆杈撳嚭
        outputValues.put(OUTPUT_LIST_ID, resultList);
        outputValues.put(OUTPUT_SUCCESS_ID, success);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isAllowNegativeIndex() {
        return allowNegativeIndex;
    }
    
    public void setAllowNegativeIndex(boolean allow) {
        this.allowNegativeIndex = allow;
        markDirty();
    }
    
    public boolean isWrapIndex() {
        return wrapIndex;
    }
    
    public void setWrapIndex(boolean wrap) {
        this.wrapIndex = wrap;
        markDirty();
    }
    
    public boolean isExpandList() {
        return expandList;
    }
    
    public void setExpandList(boolean expand) {
        this.expandList = expand;
        markDirty();
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("allowNegativeIndex", isAllowNegativeIndex());
        state.put("wrapIndex", isWrapIndex());
        state.put("expandList", isExpandList());
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
            
            if (stateMap.containsKey("wrapIndex")) {
                Object wrap = stateMap.get("wrapIndex");
                if (wrap instanceof Boolean) {
                    setWrapIndex((Boolean) wrap);
                }
            }
            
            if (stateMap.containsKey("expandList")) {
                Object expand = stateMap.get("expandList");
                if (expand instanceof Boolean) {
                    setExpandList((Boolean) expand);
                }
            }
        }
    }
} 