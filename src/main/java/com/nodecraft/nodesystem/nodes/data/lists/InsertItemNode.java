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
 * 列表插入项节点，在列表中指定位置插入一个新元素
 */
@NodeInfo(
    id = "data.lists.insert_item",
    displayName = "Insert Item",
    description = "Inserts an item into a list at a specified index",
    category = "data.lists"
)
public class InsertItemNode extends BaseNode {
    
    // --- 节点属性 ---
    private boolean allowNegativeIndex = true; // 是否允许负索引（从列表末尾开始计算）
    private boolean append = true; // 如果索引超出范围，是否追加到列表末尾
    private String description; // 存储节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_INDEX_ID = "input_index";
    private static final String INPUT_VALUE_ID = "input_value";
    private static final String OUTPUT_LIST_ID = "output_list";
    
    /**
     * 构造一个新的列表插入项节点
     */
    public InsertItemNode() {
        // 调用父类构造函数，使用UUID.randomUUID()生成新的ID
        super(UUID.randomUUID(), "data.lists.insert_item");
        
        // 设置节点描述
        this.description = "Inserts an item into a list at a specified index";
        
        // 创建输入端口
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list to insert into", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        IPort indexInput = new BasePort(INPUT_INDEX_ID, "Index", 
                "The index where to insert the item (0-based)", NodeDataType.INTEGER, this);
        addInputPort(indexInput);
        
        IPort valueInput = new BasePort(INPUT_VALUE_ID, "Value", 
                "The value to insert at the specified index", NodeDataType.ANY, this);
        addInputPort(valueInput);
        
        // 创建输出端口
        IPort listOutput = new BasePort(OUTPUT_LIST_ID, "Modified List", 
                "The list with the inserted item", NodeDataType.LIST, this);
        addOutputPort(listOutput);
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
        
        // 处理列表
        if (inputObj instanceof List) {
            List<?> inputList = (List<?>) inputObj;
            
            // 创建一个可修改的新列表
            for (Object item : inputList) {
                resultList.add(item);
            }
            
            int listSize = resultList.size();
            
            // 默认插入位置（末尾）
            int insertIndex = listSize;
            
            // 如果提供了索引
            if (indexObj instanceof Number) {
                int index = ((Number) indexObj).intValue();
                
                // 处理负索引（从列表末尾开始计算）
                if (index < 0 && allowNegativeIndex) {
                    index = listSize + index;
                }
                
                // 确定最终插入位置
                if (index >= 0 && index <= listSize) {
                    insertIndex = index;
                } else if (!append) {
                    // 如果不允许追加且索引超出范围，则不修改列表
                    outputValues.put(OUTPUT_LIST_ID, resultList);
                    return;
                }
            }
            
            // 插入元素
            resultList.add(insertIndex, valueObj);
        } else if (valueObj != null) {
            // 如果输入不是列表但有值要插入，创建只包含该值的新列表
            resultList.add(valueObj);
        }
        
        // 设置输出
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
    
    // --- 节点状态序列化 ---
    
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