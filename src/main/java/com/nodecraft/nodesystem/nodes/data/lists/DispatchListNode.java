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
 * 分发列表节点，根据布尔条件列表将主列表分成两个列表
 */
@NodeInfo(
    id = "data.lists.dispatch_list",
    displayName = "Dispatch List",
    description = "Splits a list into two based on boolean conditions",
    category = "data.lists"
)
public class DispatchListNode extends BaseNode {
    
    // --- 节点属性 ---
    private boolean useDefaultValue = false; // 是否使用默认值处理索引不匹配的情况
    private boolean defaultValue = false; // 默认布尔值
    private String description; // 存储节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_CONDITION_ID = "input_condition";
    private static final String OUTPUT_TRUE_LIST_ID = "output_true";
    private static final String OUTPUT_FALSE_LIST_ID = "output_false";
    
    /**
     * 构造一个新的分发列表节点
     */
    public DispatchListNode() {
        // 调用父类构造函数，使用UUID.randomUUID()生成新的ID
        super(UUID.randomUUID(), "data.lists.dispatch_list");
        
        // 设置节点描述
        this.description = "Splits a list into two based on boolean conditions";
        
        // 创建输入端口
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list to split", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        IPort conditionInput = new BasePort(INPUT_CONDITION_ID, "Condition", 
                "Boolean list or single boolean value", NodeDataType.ANY, this);
        addInputPort(conditionInput);
        
        // 创建输出端口
        IPort trueOutput = new BasePort(OUTPUT_TRUE_LIST_ID, "True List", 
                "Items for which the condition was true", NodeDataType.LIST, this);
        addOutputPort(trueOutput);
        
        IPort falseOutput = new BasePort(OUTPUT_FALSE_LIST_ID, "False List", 
                "Items for which the condition was false", NodeDataType.LIST, this);
        addOutputPort(falseOutput);
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
        
        List<Object> trueList = new ArrayList<>();
        List<Object> falseList = new ArrayList<>();
        
        // 处理列表
        if (listObj instanceof List) {
            List<?> inputList = (List<?>) listObj;
            
            // 检查条件是否为列表或单个布尔值
            if (conditionObj instanceof List) {
                List<?> conditionList = (List<?>) conditionObj;
                processWithConditionList(inputList, conditionList, trueList, falseList);
            } else if (conditionObj instanceof Boolean) {
                // 单个布尔值 - 全部放入相应列表
                boolean condition = (Boolean) conditionObj;
                if (condition) {
                    trueList.addAll(inputList);
                } else {
                    falseList.addAll(inputList);
                }
            } else {
                // 无条件 - 根据默认值处理
                if (useDefaultValue && defaultValue) {
                    trueList.addAll(inputList);
                } else {
                    falseList.addAll(inputList);
                }
            }
        }
        
        // 设置输出
        outputValues.put(OUTPUT_TRUE_LIST_ID, trueList);
        outputValues.put(OUTPUT_FALSE_LIST_ID, falseList);
    }
    
    /**
     * 使用条件列表处理分发
     */
    private void processWithConditionList(List<?> inputList, List<?> conditionList, 
                                         List<Object> trueList, List<Object> falseList) {
        // 遍历主列表
        for (int i = 0; i < inputList.size(); i++) {
            Object item = inputList.get(i);
            
            // 检查条件
            boolean condition;
            if (i < conditionList.size()) {
                Object condObj = conditionList.get(i);
                if (condObj instanceof Boolean) {
                    condition = (Boolean) condObj;
                } else {
                    // 非布尔值，视为false
                    condition = false;
                }
            } else {
                // 条件列表长度不足
                condition = useDefaultValue ? defaultValue : false;
            }
            
            // 根据条件分配
            if (condition) {
                trueList.add(item);
            } else {
                falseList.add(item);
            }
        }
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isUseDefaultValue() {
        return useDefaultValue;
    }
    
    public void setUseDefaultValue(boolean use) {
        this.useDefaultValue = use;
        markDirty();
    }
    
    public boolean getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(boolean value) {
        this.defaultValue = value;
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("useDefaultValue", isUseDefaultValue());
        state.put("defaultValue", getDefaultValue());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("useDefaultValue")) {
                Object use = stateMap.get("useDefaultValue");
                if (use instanceof Boolean) {
                    setUseDefaultValue((Boolean) use);
                }
            }
            
            if (stateMap.containsKey("defaultValue")) {
                Object value = stateMap.get("defaultValue");
                if (value instanceof Boolean) {
                    setDefaultValue((Boolean) value);
                }
            }
        }
    }
} 