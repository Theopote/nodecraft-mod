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
import java.util.Objects;
import java.util.UUID;

/**
 * 移除列表项节点，从列表中移除指定索引或值的元素
 */
@NodeInfo(
    id = "data.lists.remove_item",
    displayName = "Remove Item",
    description = "Removes an item from a list by index or value",
    category = "data.lists"
)
public class RemoveItemNode extends BaseNode {
    
    // --- 节点属性 ---
    private boolean useIndex = true; // 是否使用索引移除（否则按值移除）
    private boolean allowNegativeIndex = true; // 是否允许负索引（从列表末尾开始计算）
    private boolean removeAllMatches = false; // 是否移除所有匹配项（仅在按值移除时生效）
    private String description; // 存储节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_INDEX_ID = "input_index";
    private static final String INPUT_VALUE_ID = "input_value";
    private static final String OUTPUT_LIST_ID = "output_list";
    private static final String OUTPUT_REMOVED_ID = "output_removed";
    private static final String OUTPUT_COUNT_ID = "output_remove_count";
    
    /**
     * 构造一个新的移除列表项节点
     */
    public RemoveItemNode() {
        // 调用父类构造函数，使用UUID.randomUUID()生成新的ID
        super(UUID.randomUUID(), "data.lists.remove_item");
        
        // 设置节点描述
        this.description = "Removes an item from a list by index or value";
        
        // 创建输入端口
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list to remove from", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        IPort indexInput = new BasePort(INPUT_INDEX_ID, "Index", 
                "The index of the item to remove (0-based)", NodeDataType.INTEGER, this);
        addInputPort(indexInput);
        
        IPort valueInput = new BasePort(INPUT_VALUE_ID, "Value", 
                "The value to remove (used if 'Use Index' is false)", NodeDataType.ANY, this);
        addInputPort(valueInput);
        
        // 创建输出端口
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
        Object inputObj = inputValues.get(INPUT_LIST_ID);
        Object indexObj = inputValues.get(INPUT_INDEX_ID);
        Object valueObj = inputValues.get(INPUT_VALUE_ID);
        
        List<Object> resultList = new ArrayList<>();
        Object removedItem = null;
        int removeCount = 0;
        
        // 处理列表
        if (inputObj instanceof List) {
            List<?> inputList = (List<?>) inputObj;
            
            // 创建一个可修改的新列表
            for (Object item : inputList) {
                resultList.add(item);
            }
            
            // 按索引移除
            if (useIndex && indexObj instanceof Number) {
                int index = ((Number) indexObj).intValue();
                int listSize = resultList.size();
                
                // 处理负索引（从列表末尾开始计算）
                if (index < 0 && allowNegativeIndex) {
                    index = listSize + index;
                }
                
                // 移除指定索引的项
                if (index >= 0 && index < listSize) {
                    removedItem = resultList.remove(index);
                    removeCount = 1;
                }
            } 
            // 按值移除
            else if (!useIndex && valueObj != null) {
                // 如果要移除所有匹配项
                if (removeAllMatches) {
                    List<Object> toRemove = new ArrayList<>();
                    
                    // 找出所有匹配项
                    for (Object item : resultList) {
                        if (Objects.equals(item, valueObj)) {
                            toRemove.add(item);
                            removeCount++;
                            
                            // 记录第一个被移除的项
                            if (removedItem == null) {
                                removedItem = item;
                            }
                        }
                    }
                    
                    // 移除所有匹配项
                    resultList.removeAll(toRemove);
                } 
                // 只移除第一个匹配项
                else {
                    int index = resultList.indexOf(valueObj);
                    if (index >= 0) {
                        removedItem = resultList.remove(index);
                        removeCount = 1;
                    }
                }
            }
        }
        
        // 设置输出
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
    
    // --- 节点状态序列化 ---
    
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