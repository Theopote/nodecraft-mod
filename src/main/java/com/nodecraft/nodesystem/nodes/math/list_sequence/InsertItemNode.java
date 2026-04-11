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
 * 鍒楄〃鎻掑叆椤硅妭鐐癸紝鍦ㄥ垪琛ㄤ腑鎸囧畾浣嶇疆鎻掑叆涓€涓柊鍏冪礌
 */
@NodeInfo(
    id = "data.lists.insert_item",
    displayName = "Insert Item",
    description = "Inserts an item into a list at a specified index",
    category = "data.lists"
)
public class InsertItemNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    private boolean allowNegativeIndex = true; // 鏄惁鍏佽璐熺储寮曪紙浠庡垪琛ㄦ湯灏惧紑濮嬭绠楋級
    private boolean append = true; // 濡傛灉绱㈠紩瓒呭嚭鑼冨洿锛屾槸鍚﹁拷鍔犲埌鍒楄〃鏈熬
    private String description; // 瀛樺偍鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_INDEX_ID = "input_index";
    private static final String INPUT_VALUE_ID = "input_value";
    private static final String OUTPUT_LIST_ID = "output_list";
    
    /**
     * 鏋勯€犱竴涓柊鐨勫垪琛ㄦ彃鍏ラ」鑺傜偣
     */
    public InsertItemNode() {
        // 璋冪敤鐖剁被鏋勯€犲嚱鏁帮紝浣跨敤UUID.randomUUID()鐢熸垚鏂扮殑ID
        super(UUID.randomUUID(), "data.lists.insert_item");
        
        // 璁剧疆鑺傜偣鎻忚堪
        this.description = "Inserts an item into a list at a specified index";
        
        // 鍒涘缓杈撳叆绔彛
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list to insert into", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        IPort indexInput = new BasePort(INPUT_INDEX_ID, "Index", 
                "The index where to insert the item (0-based)", NodeDataType.INTEGER, this);
        addInputPort(indexInput);
        
        IPort valueInput = new BasePort(INPUT_VALUE_ID, "Value", 
                "The value to insert at the specified index", NodeDataType.ANY, this);
        addInputPort(valueInput);
        
        // 鍒涘缓杈撳嚭绔彛
        IPort listOutput = new BasePort(OUTPUT_LIST_ID, "Modified List", 
                "The list with the inserted item", NodeDataType.LIST, this);
        addOutputPort(listOutput);
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
        
        // 澶勭悊鍒楄〃
        if (inputObj instanceof List) {
            List<?> inputList = (List<?>) inputObj;
            
            // 鍒涘缓涓€涓彲淇敼鐨勬柊鍒楄〃
            for (Object item : inputList) {
                resultList.add(item);
            }
            
            int listSize = resultList.size();
            
            // 榛樿鎻掑叆浣嶇疆锛堟湯灏撅級
            int insertIndex = listSize;
            
            // 濡傛灉鎻愪緵浜嗙储寮?
            if (indexObj instanceof Number) {
                int index = ((Number) indexObj).intValue();
                
                // 澶勭悊璐熺储寮曪紙浠庡垪琛ㄦ湯灏惧紑濮嬭绠楋級
                if (index < 0 && allowNegativeIndex) {
                    index = listSize + index;
                }
                
                // 纭畾鏈€缁堟彃鍏ヤ綅缃?
                if (index >= 0 && index <= listSize) {
                    insertIndex = index;
                } else if (!append) {
                    // 濡傛灉涓嶅厑璁歌拷鍔犱笖绱㈠紩瓒呭嚭鑼冨洿锛屽垯涓嶄慨鏀瑰垪琛?
                    outputValues.put(OUTPUT_LIST_ID, resultList);
                    return;
                }
            }
            
            // 鎻掑叆鍏冪礌
            resultList.add(insertIndex, valueObj);
        } else if (valueObj != null) {
            // 濡傛灉杈撳叆涓嶆槸鍒楄〃浣嗘湁鍊艰鎻掑叆锛屽垱寤哄彧鍖呭惈璇ュ€肩殑鏂板垪琛?
            resultList.add(valueObj);
        }
        
        // 璁剧疆杈撳嚭
        outputValues.put(OUTPUT_LIST_ID, resultList);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isAllowNegativeIndex() {
        return allowNegativeIndex;
    }
    
    public void setAllowNegativeIndex(boolean allow) {
        this.allowNegativeIndex = allow;
        markDirty();
    }
    
    public boolean isAppend() {
        return append;
    }
    
    public void setAppend(boolean append) {
        this.append = append;
        markDirty();
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("allowNegativeIndex", isAllowNegativeIndex());
        state.put("append", isAppend());
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
            
            if (stateMap.containsKey("append")) {
                Object appendValue = stateMap.get("append");
                if (appendValue instanceof Boolean) {
                    setAppend((Boolean) appendValue);
                }
            }
        }
    }
} 