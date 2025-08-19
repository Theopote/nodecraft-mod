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
 * 设置列表项节点，修改列表中指定索引位置的元素
 */
@NodeInfo(
    id = "data.lists.set_item",
    displayName = "Set Item",
    description = "Sets an item in a list at a specified index",
    category = "data.lists"
)
public class SetItemNode extends BaseNode {
    
    // --- 节点属性 ---
    private boolean allowNegativeIndex = true; // 是否允许负索引（从列表末尾开始计算）
    private boolean wrapIndex = false; // 是否对索引进行循环包装
    private boolean expandList = false; // 如果索引超出范围，是否扩展列表
    private String description; // 存储节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_INDEX_ID = "input_index";
    private static final String INPUT_VALUE_ID = "input_value";
    private static final String OUTPUT_LIST_ID = "output_list";
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    
    /**
     * 构造一个新的设置列表项节点
     */
    public SetItemNode() {
        // 调用父类构造函数，使用UUID.randomUUID()生成新的ID
        super(UUID.randomUUID(), "data.lists.set_item");
        
        // 设置节点描述
        this.description = "Sets an item in a list at a specified index";
        
        // 创建输入端口
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list to modify", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        IPort indexInput = new BasePort(INPUT_INDEX_ID, "Index", 
                "The index of the item to set (0-based)", NodeDataType.INTEGER, this);
        addInputPort(indexInput);
        
        IPort valueInput = new BasePort(INPUT_VALUE_ID, "Value", 
                "The new value to set at the specified index", NodeDataType.ANY, this);
        addInputPort(valueInput);
        
        // 创建输出端口
        IPort listOutput = new BasePort(OUTPUT_LIST_ID, "Modified List", 
                "The modified list with the new value set", NodeDataType.LIST, this);
        addOutputPort(listOutput);
        
        IPort successOutput = new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "Whether the operation was successful", NodeDataType.BOOLEAN, this);
        addOutputPort(successOutput);
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
        boolean success = false;
        
        // 处理列表
        if (inputObj instanceof List && indexObj instanceof Number) {
            List<?> inputList = (List<?>) inputObj;
            
            // 创建一个可修改的新列表
            for (Object item : inputList) {
                resultList.add(item);
            }
            
            int listSize = resultList.size();
            int index = ((Number) indexObj).intValue();
            
            // 处理负索引（从列表末尾开始计算）
            if (index < 0 && allowNegativeIndex) {
                index = listSize + index;
            }
            
            // 处理索引包装
            if (wrapIndex && listSize > 0) {
                // 对列表长度取模，确保索引总是有效的
                index = ((index % listSize) + listSize) % listSize;
                resultList.set(index, valueObj);
                success = true;
            } else if (index >= 0 && index < listSize) {
                // 常规索引设置
                resultList.set(index, valueObj);
                success = true;
            } else if (expandList && index >= 0) {
                // 扩展列表以适应索引
                while (resultList.size() <= index) {
                    resultList.add(null);
                }
                resultList.set(index, valueObj);
                success = true;
            }
        }
        
        // 设置输出
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
    
    // --- 节点状态序列化 ---
    
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