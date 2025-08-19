package com.nodecraft.nodesystem.nodes.inputs.basic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.api.NodeInfo;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 布尔值开关节点，允许用户切换真/假状态。
 */
@NodeInfo(
    id = "inputs.basic.boolean_toggle",
    displayName = "布尔开关",
    description = "提供一个可以切换的布尔值开关控制",
    category = "inputs.basic"
)
public class BooleanToggleNode extends BaseNode {
    
    // --- 节点属性 ---
    private boolean value = false;
    private String trueLabel = "True";
    private String falseLabel = "False";
    private String description = "A switch that can be turned on (true) or off (false)."; // 节点描述
    
    // --- 输出端口 ---
    private static final String OUTPUT_VALUE_ID = "output_value";
    
    /**
     * 构造一个新的布尔值开关节点
     */
    public BooleanToggleNode() {
        // 使用新的分类命名 - inputs.basic.boolean_toggle
        super(UUID.randomUUID(), "inputs.basic.boolean_toggle");
        
        // 创建并添加输出端口
        IPort valueOutput = new BasePort(OUTPUT_VALUE_ID, "Value", "The boolean value", NodeDataType.BOOLEAN, this);
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
     * 切换布尔值状态
     */
    public void toggle() {
        setValue(!value);
    }
    
    /**
     * 设置布尔值
     * @param value 新的布尔值
     */
    public void setValue(boolean value) {
        if (this.value != value) {
            this.value = value;
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
    
    public boolean getValue() {
        return value;
    }
    
    public String getTrueLabel() {
        return trueLabel;
    }
    
    public void setTrueLabel(String trueLabel) {
        this.trueLabel = trueLabel != null ? trueLabel : "True";
    }
    
    public String getFalseLabel() {
        return falseLabel;
    }
    
    public void setFalseLabel(String falseLabel) {
        this.falseLabel = falseLabel != null ? falseLabel : "False";
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        // 返回节点所有可序列化的状态
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("value", getValue());
        state.put("trueLabel", getTrueLabel());
        state.put("falseLabel", getFalseLabel());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("trueLabel")) {
                Object label = stateMap.get("trueLabel");
                if (label instanceof String) {
                    setTrueLabel((String) label);
                }
            }
            
            if (stateMap.containsKey("falseLabel")) {
                Object label = stateMap.get("falseLabel");
                if (label instanceof String) {
                    setFalseLabel((String) label);
                }
            }
            
            if (stateMap.containsKey("value")) {
                Object value = stateMap.get("value");
                if (value instanceof Boolean) {
                    setValue((Boolean) value);
                } else if (value instanceof String) {
                    setValue(Boolean.parseBoolean((String) value));
                } else if (value instanceof Number) {
                    setValue(((Number) value).intValue() != 0);
                }
            }
        } else if (state instanceof Boolean) {
            // 如果状态直接是布尔值
            setValue((Boolean) state);
        } else if (state instanceof String) {
            // 如果状态是字符串，尝试解析为布尔值
            setValue(Boolean.parseBoolean((String) state));
        } else if (state instanceof Number) {
            // 如果状态是数字，非零为true
            setValue(((Number) state).intValue() != 0);
        }
    }
} 