package com.nodecraft.nodesystem.nodes.data.lists;

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
 * 获取列表项节点，从列表中获取指定索引位置的元素
 */
@NodeInfo(
    id = "data.lists.get_item",
    displayName = "获取列表项",
    description = "根据索引获取列表中的特定项",
    category = "data.lists"
)
public class GetItemNode extends BaseNode {
    
    // --- 节点属性 ---
    private boolean allowNegativeIndex = true; // 是否允许负索引（从列表末尾开始计算）
    private boolean wrapIndex = false; // 是否对索引进行循环包装
    private String description = "Gets an item from a list at a specified index"; // 节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_INDEX_ID = "input_index";
    private static final String OUTPUT_ITEM_ID = "output_item";
    private static final String OUTPUT_FOUND_ID = "output_found";
    
    /**
     * 构造一个新的获取列表项节点
     */
    public GetItemNode() {
        // 使用分类命名 - data.lists.get_item
        super(UUID.randomUUID(), "data.lists.get_item");
        
        // 创建输入端口
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list to get an item from", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        IPort indexInput = new BasePort(INPUT_INDEX_ID, "Index", 
                "The index of the item (0-based)", NodeDataType.INTEGER, this);
        addInputPort(indexInput);
        
        // 创建输出端口
        IPort itemOutput = new BasePort(OUTPUT_ITEM_ID, "Item", 
                "The item at the specified index", NodeDataType.ANY, this);
        addOutputPort(itemOutput);
        
        IPort foundOutput = new BasePort(OUTPUT_FOUND_ID, "Found", 
                "Whether an item was found at the specified index", NodeDataType.BOOLEAN, this);
        addOutputPort(foundOutput);
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
        // 获取输入列表和索引
        Object inputObj = inputValues.get(INPUT_LIST_ID);
        Object indexObj = inputValues.get(INPUT_INDEX_ID);
        
        Object item = null;
        boolean found = false;
        
        // 计算列表项
        if (inputObj instanceof List && indexObj instanceof Number) {
            List<?> list = (List<?>) inputObj;
            int listSize = list.size();
            
            if (listSize > 0) {
                int index = ((Number) indexObj).intValue();
                
                // 处理负索引（从列表末尾开始计算）
                if (index < 0 && allowNegativeIndex) {
                    index = listSize + index;
                }
                
                // 处理索引包装
                if (wrapIndex && listSize > 0) {
                    // 对列表长度取模，确保索引总是有效的
                    index = ((index % listSize) + listSize) % listSize;
                    found = true;
                    item = list.get(index);
                } else if (index >= 0 && index < listSize) {
                    // 常规索引访问
                    found = true;
                    item = list.get(index);
                }
            }
        }
        
        // 设置输出
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
    
    // --- 节点状态序列化 ---
    
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