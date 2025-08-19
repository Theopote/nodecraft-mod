package com.nodecraft.nodesystem.nodes.data.conversion;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Number to Boolean 节点，将数字转换为布尔值
 * 0转换为false，非0转换为true
 */
@NodeInfo(
    id = "data.conversion.number_to_boolean",
    displayName = "Number to Boolean",
    description = "Converts a number to boolean (0 = false, non-zero = true)",
    category = "data.conversion"
)
public class NumberToBooleanNode extends BaseNode {
    
    // --- 属性 ---
    private boolean invertResult = false; // 是否反转结果
    private String description; // 存储节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_NUMBER_ID = "input_number";
    private static final String OUTPUT_BOOLEAN_ID = "output_boolean";
    
    /**
     * 构造一个新的数字转布尔值节点
     */
    public NumberToBooleanNode() {
        // 调用父类构造函数，使用UUID.randomUUID()生成新的ID
        super(UUID.randomUUID(), "data.conversion.number_to_boolean");
        
        // 设置节点描述
        this.description = "Converts a number to boolean (0 = false, non-zero = true)";
        
        // 创建输入端口
        IPort numberInput = new BasePort(INPUT_NUMBER_ID, "Number", 
                "The input number", NodeDataType.DOUBLE, this);
        addInputPort(numberInput);
        
        // 创建输出端口
        IPort booleanOutput = new BasePort(OUTPUT_BOOLEAN_ID, "Boolean", 
                "The converted boolean value", NodeDataType.BOOLEAN, this);
        addOutputPort(booleanOutput);
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
        Object numberObj = inputValues.get(INPUT_NUMBER_ID);
        
        // 默认输出为false
        boolean result = false;
        
        // 检查输入是否为数字
        if (numberObj instanceof Number) {
            double number = ((Number) numberObj).doubleValue();
            // 如果数字不等于0，则为true
            result = number != 0;
        }
        
        // 如果设置了反转结果，则取反
        if (invertResult) {
            result = !result;
        }
        
        // 设置输出
        outputValues.put(OUTPUT_BOOLEAN_ID, result);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isInvertResult() {
        return invertResult;
    }
    
    public void setInvertResult(boolean invert) {
        this.invertResult = invert;
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("invertResult", isInvertResult());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("invertResult")) {
                Object invertObj = stateMap.get("invertResult");
                if (invertObj instanceof Boolean) {
                    setInvertResult((Boolean) invertObj);
                }
            }
        }
    }
} 