package com.nodecraft.nodesystem.nodes.inputs.basic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 浮点数输入节点，允许用户手动输入浮点数值。
 */
@NodeInfo(
    id = "inputs.basic.float_input",
    displayName = "浮点数输入",
    description = "允许用户手动输入浮点数值",
    category = "inputs.basic"
)
public class FloatInputNode extends BaseNode {
    
    // --- 节点属性 ---
    private float value = 0.0f;
    private float minValue = Float.NEGATIVE_INFINITY;
    private float maxValue = Float.POSITIVE_INFINITY;
    private int precision = 2; // 默认保留2位小数
    private String description = "Allows manual input of a floating point value."; // 节点描述
    
    // --- 输出端口 ---
    private static final String OUTPUT_VALUE_ID = "output_value";
    
    /**
     * 构造一个新的浮点数输入节点
     */
    public FloatInputNode() {
        // 使用新的分类命名 - inputs.basic.float_input
        super(UUID.randomUUID(), "inputs.basic.float_input");
        
        // 创建并添加输出端口
        IPort valueOutput = new BasePort(OUTPUT_VALUE_ID, "Value", "The float value", NodeDataType.FLOAT, this);
        addOutputPort(valueOutput);
        
        // 初始化输出值
        updateOutput();
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
        updateOutput();
    }
    
    /**
     * 当用户在UI中输入值时调用此方法
     * @param value 新的浮点值
     */
    public void setValue(float value) {
        // 应用范围约束
        float clampedValue = Math.max(minValue, Math.min(maxValue, value));
        
        // 应用精度约束
        if (precision >= 0) {
            float multiplier = (float) Math.pow(10, precision);
            clampedValue = Math.round(clampedValue * multiplier) / multiplier;
        }
        
        // 如果值变化了，更新节点状态
        if (this.value != clampedValue) {
            this.value = clampedValue;
            updateOutput();
            
            // 通知系统此节点的输出已更新
            markDirty();
        }
    }
    
    /**
     * 更新输出端口的值
     */
    private void updateOutput() {
        outputValues.put(OUTPUT_VALUE_ID, this.value);
    }
    
    // --- Getters/Setters for Properties ---
    
    public float getValue() {
        return value;
    }
    
    public float getMinValue() {
        return minValue;
    }
    
    public void setMinValue(float minValue) {
        this.minValue = minValue;
        // 确保当前值仍在范围内
        setValue(this.value);
    }
    
    public float getMaxValue() {
        return maxValue;
    }
    
    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
        // 确保当前值仍在范围内
        setValue(this.value);
    }
    
    public int getPrecision() {
        return precision;
    }
    
    public void setPrecision(int precision) {
        if (precision < 0) {
            precision = 0; // 保证精度为非负数
        }
        
        if (this.precision != precision) {
            this.precision = precision;
            // 更新值以应用新的精度
            setValue(this.value);
        }
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        // 返回节点所有可序列化的状态
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("value", getValue());
        state.put("min", getMinValue());
        state.put("max", getMaxValue());
        state.put("precision", getPrecision());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            // 首先设置精度和范围
            if (stateMap.containsKey("precision")) {
                Object precision = stateMap.get("precision");
                if (precision instanceof Number) {
                    setPrecision(((Number) precision).intValue());
                }
            }
            
            if (stateMap.containsKey("min")) {
                Object min = stateMap.get("min");
                if (min instanceof Number) {
                    setMinValue(((Number) min).floatValue());
                }
            }
            
            if (stateMap.containsKey("max")) {
                Object max = stateMap.get("max");
                if (max instanceof Number) {
                    setMaxValue(((Number) max).floatValue());
                }
            }
            
            // 最后设置当前值，确保应用所有约束
            if (stateMap.containsKey("value")) {
                Object value = stateMap.get("value");
                if (value instanceof Number) {
                    setValue(((Number) value).floatValue());
                } else if (value instanceof String) {
                    try {
                        setValue(Float.parseFloat((String) value));
                    } catch (NumberFormatException e) {
                        System.err.println("Failed to parse state value for FloatInputNode: " + value);
                    }
                }
            }
        } else if (state instanceof Number) {
            // 向后兼容：如果状态只是一个数字，直接使用它作为当前值
            setValue(((Number) state).floatValue());
        }
    }
} 