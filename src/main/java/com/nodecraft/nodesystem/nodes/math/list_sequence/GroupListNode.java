package com.nodecraft.nodesystem.nodes.math.list_sequence;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 鍒嗙粍鍒楄〃鑺傜偣锛屾牴鎹敭鍒楄〃瀵逛富鍒楄〃杩涜鍒嗙粍
 */
@NodeInfo(
    id = "data.lists.group_list",
    displayName = "Group List",
    description = "Groups items in a list based on a key list",
    category = "data.lists"
)
public class GroupListNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    private boolean skipInvalidKeys = false; // 鏄惁璺宠繃鏃犳晥鐨勯敭
    private String description; // 瀛樺偍鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_KEYS_ID = "input_keys";
    private static final String OUTPUT_GROUPS_ID = "output_groups";
    private static final String OUTPUT_KEYS_ID = "output_unique_keys";
    private static final String OUTPUT_COUNT_ID = "output_group_count";
    
    /**
     * 鏋勯€犱竴涓柊鐨勫垎缁勫垪琛ㄨ妭鐐?
     */
    public GroupListNode() {
        // 璋冪敤鐖剁被鏋勯€犲嚱鏁帮紝浣跨敤UUID.randomUUID()鐢熸垚鏂扮殑ID
        super(UUID.randomUUID(), "data.lists.group_list");
        
        // 璁剧疆鑺傜偣鎻忚堪
        this.description = "Groups items in a list based on a key list";
        
        // 鍒涘缓杈撳叆绔彛
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list to group", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        IPort keysInput = new BasePort(INPUT_KEYS_ID, "Keys", 
                "List of keys to group by", NodeDataType.LIST, this);
        addInputPort(keysInput);
        
        // 鍒涘缓杈撳嚭绔彛
        IPort groupsOutput = new BasePort(OUTPUT_GROUPS_ID, "Groups", 
                "List of grouped items (lists)", NodeDataType.LIST, this);
        addOutputPort(groupsOutput);
        
        IPort keysOutput = new BasePort(OUTPUT_KEYS_ID, "Unique Keys", 
                "List of unique keys found", NodeDataType.LIST, this);
        addOutputPort(keysOutput);
        
        IPort countOutput = new BasePort(OUTPUT_COUNT_ID, "Group Count", 
                "Number of groups created", NodeDataType.INTEGER, this);
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
        Object keysObj = inputValues.get(INPUT_KEYS_ID);
        
        // 鍒濆鍖栫粨鏋?
        Map<Object, List<Object>> groups = new HashMap<>();
        List<Object> uniqueKeys = new ArrayList<>();
        
        // 澶勭悊鍒嗙粍
        if (listObj instanceof List && keysObj instanceof List) {
            List<?> inputList = (List<?>) listObj;
            List<?> keysList = (List<?>) keysObj;
            
            // 閬嶅巻涓诲垪琛ㄥ拰閿垪琛?
            for (int i = 0; i < inputList.size(); i++) {
                Object item = inputList.get(i);
                
                // 鑾峰彇閿紙濡傛灉鍙敤锛?
                Object key = null;
                if (i < keysList.size()) {
                    key = keysList.get(i);
                }
                
                // 璺宠繃鏃犳晥閿?
                if (key == null && skipInvalidKeys) {
                    continue;
                }
                
                // 灏嗛」鐩坊鍔犲埌瀵瑰簲鐨勭粍
                if (!groups.containsKey(key)) {
                    // 鏂伴敭锛屽垱寤烘柊缁?
                    List<Object> group = new ArrayList<>();
                    group.add(item);
                    groups.put(key, group);
                    uniqueKeys.add(key);
                } else {
                    // 鐜版湁閿紝娣诲姞鍒扮幇鏈夌粍
                    groups.get(key).add(item);
                }
            }
        }
        
        // 鍑嗗杈撳嚭鍒嗙粍鍒楄〃
        List<Object> groupsList = new ArrayList<>();
        for (Object key : uniqueKeys) {
            groupsList.add(groups.get(key));
        }
        
        // 璁剧疆杈撳嚭
        outputValues.put(OUTPUT_GROUPS_ID, groupsList);
        outputValues.put(OUTPUT_KEYS_ID, uniqueKeys);
        outputValues.put(OUTPUT_COUNT_ID, uniqueKeys.size());
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isSkipInvalidKeys() {
        return skipInvalidKeys;
    }
    
    public void setSkipInvalidKeys(boolean skip) {
        this.skipInvalidKeys = skip;
        markDirty();
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("skipInvalidKeys", isSkipInvalidKeys());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("skipInvalidKeys")) {
                Object skip = stateMap.get("skipInvalidKeys");
                if (skip instanceof Boolean) {
                    setSkipInvalidKeys((Boolean) skip);
                }
            }
        }
    }
} 