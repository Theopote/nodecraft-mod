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
import java.util.Objects;
import java.util.UUID;

/**
 * 绉婚櫎鍒楄〃椤硅妭鐐癸紝浠庡垪琛ㄤ腑绉婚櫎鎸囧畾绱㈠紩鎴栧€肩殑鍏冪礌
 */
@NodeInfo(
    id = "data.lists.remove_item",
    displayName = "Remove Item",
    description = "Removes an item from a list by index or value",
    category = "data.lists"
)
public class RemoveItemNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    private boolean useIndex = true; // 鏄惁浣跨敤绱㈠紩绉婚櫎锛堝惁鍒欐寜鍊肩Щ闄わ級
    private boolean allowNegativeIndex = true; // 鏄惁鍏佽璐熺储寮曪紙浠庡垪琛ㄦ湯灏惧紑濮嬭绠楋級
    private boolean removeAllMatches = false; // 鏄惁绉婚櫎鎵€鏈夊尮閰嶉」锛堜粎鍦ㄦ寜鍊肩Щ闄ゆ椂鐢熸晥锛?
    private String description; // 瀛樺偍鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_INDEX_ID = "input_index";
    private static final String INPUT_VALUE_ID = "input_value";
    private static final String OUTPUT_LIST_ID = "output_list";
    private static final String OUTPUT_REMOVED_ID = "output_removed";
    private static final String OUTPUT_COUNT_ID = "output_remove_count";
    
    /**
     * 鏋勯€犱竴涓柊鐨勭Щ闄ゅ垪琛ㄩ」鑺傜偣
     */
    public RemoveItemNode() {
        // 璋冪敤鐖剁被鏋勯€犲嚱鏁帮紝浣跨敤UUID.randomUUID()鐢熸垚鏂扮殑ID
        super(UUID.randomUUID(), "data.lists.remove_item");
        
        // 璁剧疆鑺傜偣鎻忚堪
        this.description = "Removes an item from a list by index or value";
        
        // 鍒涘缓杈撳叆绔彛
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list to remove from", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        IPort indexInput = new BasePort(INPUT_INDEX_ID, "Index", 
                "The index of the item to remove (0-based)", NodeDataType.INTEGER, this);
        addInputPort(indexInput);
        
        IPort valueInput = new BasePort(INPUT_VALUE_ID, "Value", 
                "The value to remove (used if 'Use Index' is false)", NodeDataType.ANY, this);
        addInputPort(valueInput);
        
        // 鍒涘缓杈撳嚭绔彛
        IPort listOutput = new BasePort(OUTPUT_LIST_ID, "Modified List", 
                "The list with item(s) removed", NodeDataType.LIST, this);
        addOutputPort(listOutput);
        
        IPort removedOutput = new BasePort(OUTPUT_REMOVED_ID, "Removed Item", 
                "The removed item (first one if multiple)", NodeDataType.ANY, this);
        addOutputPort(removedOutput);
        
        IPort countOutput = new BasePort(OUTPUT_COUNT_ID, "Remove Count", 
                "The number of items removed", NodeDataType.INTEGER, this);
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
        Object inputObj = inputValues.get(INPUT_LIST_ID);
        Object indexObj = inputValues.get(INPUT_INDEX_ID);
        Object valueObj = inputValues.get(INPUT_VALUE_ID);
        
        List<Object> resultList = new ArrayList<>();
        Object removedItem = null;
        int removeCount = 0;
        
        // 澶勭悊鍒楄〃
        if (inputObj instanceof List) {
            List<?> inputList = (List<?>) inputObj;
            
            // 鍒涘缓涓€涓彲淇敼鐨勬柊鍒楄〃
            for (Object item : inputList) {
                resultList.add(item);
            }
            
            // 鎸夌储寮曠Щ闄?
            if (useIndex && indexObj instanceof Number) {
                int index = ((Number) indexObj).intValue();
                int listSize = resultList.size();
                
                // 澶勭悊璐熺储寮曪紙浠庡垪琛ㄦ湯灏惧紑濮嬭绠楋級
                if (index < 0 && allowNegativeIndex) {
                    index = listSize + index;
                }
                
                // 绉婚櫎鎸囧畾绱㈠紩鐨勯」
                if (index >= 0 && index < listSize) {
                    removedItem = resultList.remove(index);
                    removeCount = 1;
                }
            } 
            // 鎸夊€肩Щ闄?
            else if (!useIndex && valueObj != null) {
                // 濡傛灉瑕佺Щ闄ゆ墍鏈夊尮閰嶉」
                if (removeAllMatches) {
                    List<Object> toRemove = new ArrayList<>();
                    
                    // 鎵惧嚭鎵€鏈夊尮閰嶉」
                    for (Object item : resultList) {
                        if (Objects.equals(item, valueObj)) {
                            toRemove.add(item);
                            removeCount++;
                            
                            // 璁板綍绗竴涓绉婚櫎鐨勯」
                            if (removedItem == null) {
                                removedItem = item;
                            }
                        }
                    }
                    
                    // 绉婚櫎鎵€鏈夊尮閰嶉」
                    resultList.removeAll(toRemove);
                } 
                // 鍙Щ闄ょ涓€涓尮閰嶉」
                else {
                    int index = resultList.indexOf(valueObj);
                    if (index >= 0) {
                        removedItem = resultList.remove(index);
                        removeCount = 1;
                    }
                }
            }
        }
        
        // 璁剧疆杈撳嚭
        outputValues.put(OUTPUT_LIST_ID, resultList);
        outputValues.put(OUTPUT_REMOVED_ID, removedItem);
        outputValues.put(OUTPUT_COUNT_ID, removeCount);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isUseIndex() {
        return useIndex;
    }
    
    public void setUseIndex(boolean useIndex) {
        this.useIndex = useIndex;
        markDirty();
    }
    
    public boolean isAllowNegativeIndex() {
        return allowNegativeIndex;
    }
    
    public void setAllowNegativeIndex(boolean allow) {
        this.allowNegativeIndex = allow;
        markDirty();
    }
    
    public boolean isRemoveAllMatches() {
        return removeAllMatches;
    }
    
    public void setRemoveAllMatches(boolean removeAll) {
        this.removeAllMatches = removeAll;
        markDirty();
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("useIndex", isUseIndex());
        state.put("allowNegativeIndex", isAllowNegativeIndex());
        state.put("removeAllMatches", isRemoveAllMatches());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("useIndex")) {
                Object useIdx = stateMap.get("useIndex");
                if (useIdx instanceof Boolean) {
                    setUseIndex((Boolean) useIdx);
                }
            }
            
            if (stateMap.containsKey("allowNegativeIndex")) {
                Object allow = stateMap.get("allowNegativeIndex");
                if (allow instanceof Boolean) {
                    setAllowNegativeIndex((Boolean) allow);
                }
            }
            
            if (stateMap.containsKey("removeAllMatches")) {
                Object removeAll = stateMap.get("removeAllMatches");
                if (removeAll instanceof Boolean) {
                    setRemoveAllMatches((Boolean) removeAll);
                }
            }
        }
    }
} 