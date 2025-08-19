package com.nodecraft.nodesystem.nodes.data.lists;

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
 * 过滤列表节点，根据布尔条件保留或删除列表中的元素
 */
@NodeInfo(
    id = "data.lists.filter_list",
    displayName = "Filter List",
    description = "Filters a list based on boolean conditions",
    category = "data.lists"
)
public class FilterListNode extends BaseNode {
    
    // --- 节点属性 ---
    private boolean invert = false; // 是否反转过滤条件（保留条件为false的项）
    private String description; // 存储节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_CONDITION_ID = "input_condition";
    private static final String OUTPUT_LIST_ID = "output_list";
    private static final String OUTPUT_REMOVED_ID = "output_removed";
    private static final String OUTPUT_COUNT_ID = "output_count";
    
    /**
     * 构造一个新的过滤列表节点
     */
    public FilterListNode() {
        // 调用父类构造函数，使用UUID.randomUUID()生成新的ID
        super(UUID.randomUUID(), "data.lists.filter_list");
        
        // 设置节点描述
        this.description = "Filters a list based on boolean conditions";
        
        // 创建输入端口
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list to filter", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        IPort conditionInput = new BasePort(INPUT_CONDITION_ID, "Condition", 
                "Boolean list or single boolean value", NodeDataType.ANY, this);
        addInputPort(conditionInput);
        
        // 创建输出端口
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
        Object conditionObj = inputValues.get(INPUT_CONDITION_ID);
        
        List<Object> filteredList = new ArrayList<>();
        List<Object> removedList = new ArrayList<>();
        
        // 处理列表
        if (listObj instanceof List) {
            List<?> inputList = (List<?>) listObj;
            
            // 检查条件是否为列表或单个布尔值
            if (conditionObj instanceof List) {
                List<?> conditionList = (List<?>) conditionObj;
                filterWithConditionList(inputList, conditionList, filteredList, removedList);
            } else if (conditionObj instanceof Boolean) {
                // 单个布尔值 - 全部保留或全部移除
                boolean condition = (Boolean) conditionObj;
                if (condition != invert) { // 根据invert决定是否反转条件
                    filteredList.addAll(inputList);
                } else {
                    removedList.addAll(inputList);
                }
            } else {
                // 无条件 - 全部移除
                removedList.addAll(inputList);
            }
        }
        
        // 设置输出
        outputValues.put(OUTPUT_LIST_ID, filteredList);
        outputValues.put(OUTPUT_REMOVED_ID, removedList);
        outputValues.put(OUTPUT_COUNT_ID, filteredList.size());
    }
    
    /**
     * 使用条件列表进行过滤
     */
    private void filterWithConditionList(List<?> inputList, List<?> conditionList, 
                                       List<Object> filteredList, List<Object> removedList) {
        // 遍历主列表
        for (int i = 0; i < inputList.size(); i++) {
            Object item = inputList.get(i);
            
            // 检查条件
            boolean keep = false;
            if (i < conditionList.size()) {
                Object condObj = conditionList.get(i);
                if (condObj instanceof Boolean) {
                    keep = (Boolean) condObj;
                }
            }
            
            // 根据invert属性反转条件
            if (invert) {
                keep = !keep;
            }
            
            // 根据条件分配
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
    
    // --- 节点状态序列化 ---
    
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