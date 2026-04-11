package com.nodecraft.nodesystem.nodes.math.list_sequence;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import com.nodecraft.nodesystem.api.NodeInfo;

import java.util.List;
import java.util.UUID;

/**
 * 鑾峰彇鍒楄〃椤硅妭鐐癸紝浠庡垪琛ㄤ腑鑾峰彇鎸囧畾绱㈠紩浣嶇疆鐨勫厓绱?
 */
@NodeInfo(
    id = "data.lists.get_item",
    displayName = "鑾峰彇鍒楄〃椤?,
    description = "鏍规嵁绱㈠紩鑾峰彇鍒楄〃涓殑鐗瑰畾椤?,
    category = "data.lists"
)
public class GetItemNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    private boolean allowNegativeIndex = true; // 鏄惁鍏佽璐熺储寮曪紙浠庡垪琛ㄦ湯灏惧紑濮嬭绠楋級
    private boolean wrapIndex = false; // 鏄惁瀵圭储寮曡繘琛屽惊鐜寘瑁?
    private String description = "Gets an item from a list at a specified index"; // 鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_INDEX_ID = "input_index";
    private static final String OUTPUT_ITEM_ID = "output_item";
    private static final String OUTPUT_FOUND_ID = "output_found";
    
    /**
     * 鏋勯€犱竴涓柊鐨勮幏鍙栧垪琛ㄩ」鑺傜偣
     */
    public GetItemNode() {
        // 浣跨敤鍒嗙被鍛藉悕 - data.lists.get_item
        super(UUID.randomUUID(), "data.lists.get_item");
        
        // 鍒涘缓杈撳叆绔彛
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list to get an item from", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        IPort indexInput = new BasePort(INPUT_INDEX_ID, "Index", 
                "The index of the item (0-based)", NodeDataType.INTEGER, this);
        addInputPort(indexInput);
        
        // 鍒涘缓杈撳嚭绔彛
        IPort itemOutput = new BasePort(OUTPUT_ITEM_ID, "Item", 
                "The item at the specified index", NodeDataType.ANY, this);
        addOutputPort(itemOutput);
        
        IPort foundOutput = new BasePort(OUTPUT_FOUND_ID, "Found", 
                "Whether an item was found at the specified index", NodeDataType.BOOLEAN, this);
        addOutputPort(foundOutput);
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
        // 鑾峰彇杈撳叆鍒楄〃鍜岀储寮?
        Object inputObj = inputValues.get(INPUT_LIST_ID);
        Object indexObj = inputValues.get(INPUT_INDEX_ID);
        
        Object item = null;
        boolean found = false;
        
        // 璁＄畻鍒楄〃椤?
        if (inputObj instanceof List && indexObj instanceof Number) {
            List<?> list = (List<?>) inputObj;
            int listSize = list.size();
            
            if (listSize > 0) {
                int index = ((Number) indexObj).intValue();
                
                // 澶勭悊璐熺储寮曪紙浠庡垪琛ㄦ湯灏惧紑濮嬭绠楋級
                if (index < 0 && allowNegativeIndex) {
                    index = listSize + index;
                }
                
                // 澶勭悊绱㈠紩鍖呰
                if (wrapIndex && listSize > 0) {
                    // 瀵瑰垪琛ㄩ暱搴﹀彇妯★紝纭繚绱㈠紩鎬绘槸鏈夋晥鐨?
                    index = ((index % listSize) + listSize) % listSize;
                    found = true;
                    item = list.get(index);
                } else if (index >= 0 && index < listSize) {
                    // 甯歌绱㈠紩璁块棶
                    found = true;
                    item = list.get(index);
                }
            }
        }
        
        // 璁剧疆杈撳嚭
        outputValues.put(OUTPUT_ITEM_ID, item);
        outputValues.put(OUTPUT_FOUND_ID, found);
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
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("allowNegativeIndex", isAllowNegativeIndex());
        state.put("wrapIndex", isWrapIndex());
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
        }
    }
} 