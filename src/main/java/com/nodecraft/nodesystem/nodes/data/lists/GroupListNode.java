package com.nodecraft.nodesystem.nodes.data.lists;

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
 * 分组列表节点，根据键列表对主列表进行分组
 */
@NodeInfo(
    id = "data.lists.group_list",
    displayName = "Group List",
    description = "Groups items in a list based on a key list",
    category = "data.lists"
)
public class GroupListNode extends BaseNode {
    
    // --- 节点属性 ---
    private boolean skipInvalidKeys = false; // 是否跳过无效的键
    private String description; // 存储节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_KEYS_ID = "input_keys";
    private static final String OUTPUT_GROUPS_ID = "output_groups";
    private static final String OUTPUT_KEYS_ID = "output_unique_keys";
    private static final String OUTPUT_COUNT_ID = "output_group_count";
    
    /**
     * 构造一个新的分组列表节点
     */
    public GroupListNode() {
        // 调用父类构造函数，使用UUID.randomUUID()生成新的ID
        super(UUID.randomUUID(), "data.lists.group_list");
        
        // 设置节点描述
        this.description = "Groups items in a list based on a key list";
        
        // 创建输入端口
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list to group", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        IPort keysInput = new BasePort(INPUT_KEYS_ID, "Keys", 
                "List of keys to group by", NodeDataType.LIST, this);
        addInputPort(keysInput);
        
        // 创建输出端口
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
     * 实现INode接口的getDescription方法
     * @return 节点描述
     */
    @Override
    public String getDescription() {
        return this.description;
    }
    
    /**
     * 节点的计算逻辑
     * @param context 执行上下文
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入
        Object listObj = inputValues.get(INPUT_LIST_ID);
        Object keysObj = inputValues.get(INPUT_KEYS_ID);
        
        // 初始化结果
        Map<Object, List<Object>> groups = new HashMap<>();
        List<Object> uniqueKeys = new ArrayList<>();
        
        // 处理分组
        if (listObj instanceof List && keysObj instanceof List) {
            List<?> inputList = (List<?>) listObj;
            List<?> keysList = (List<?>) keysObj;
            
            // 遍历主列表和键列表
            for (int i = 0; i < inputList.size(); i++) {
                Object item = inputList.get(i);
                
                // 获取键（如果可用）
                Object key = null;
                if (i < keysList.size()) {
                    key = keysList.get(i);
                }
                
                // 跳过无效键
                if (key == null && skipInvalidKeys) {
                    continue;
                }
                
                // 将项目添加到对应的组
                if (!groups.containsKey(key)) {
                    // 新键，创建新组
                    List<Object> group = new ArrayList<>();
                    group.add(item);
                    groups.put(key, group);
                    uniqueKeys.add(key);
                } else {
                    // 现有键，添加到现有组
                    groups.get(key).add(item);
                }
            }
        }
        
        // 准备输出分组列表
        List<Object> groupsList = new ArrayList<>();
        for (Object key : uniqueKeys) {
            groupsList.add(groups.get(key));
        }
        
        // 设置输出
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
    
    // --- 节点状态序列化 ---
    
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