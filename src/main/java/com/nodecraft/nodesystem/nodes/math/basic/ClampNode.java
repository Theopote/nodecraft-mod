package com.nodecraft.nodesystem.nodes.math.basic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;
import com.nodecraft.nodesystem.api.NodeInfo;

/**
 * Clamp Node: 将数值限制在指定的最小值和最大值之间
 */
@NodeInfo(
    id = "math.basic.clamp",
    displayName = "限制值",
    description = "将数值限制在指定的最小值和最大值之间",
    category = "math.basic"
)
public class ClampNode extends BaseNode {

    // --- 节点属性 ---
    private double defaultMin = 0.0;
    private double defaultMax = 1.0;

    // --- 输入端口 IDs ---
    private static final String INPUT_VALUE_ID = "input_value";
    private static final String INPUT_MIN_ID = "input_min";
    private static final String INPUT_MAX_ID = "input_max";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_RESULT_ID = "output_result";

    // --- 构造函数 ---
    public ClampNode() {
        super(UUID.randomUUID(), "math.basic.clamp");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_VALUE_ID, "Value", "Value to clamp", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_MIN_ID, "Min", "Minimum allowed value", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_MAX_ID, "Max", "Maximum allowed value", NodeDataType.DOUBLE, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_RESULT_ID, "Result", "The clamped value", NodeDataType.DOUBLE, this));
    }
    
    @Override
    public String getDescription() {
        return "Restricts a value to specified minimum and maximum values";
    }
    
    @Override
    public String getDisplayName() {
        return "Clamp";
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入
        Object valueObj = inputValues.get(INPUT_VALUE_ID);
        Object minObj = inputValues.get(INPUT_MIN_ID);
        Object maxObj = inputValues.get(INPUT_MAX_ID);
        
        // 默认值
        double value = 0.0;
        double min = defaultMin;
        double max = defaultMax;
        
        // 解析输入
        if (valueObj instanceof Number) {
            value = ((Number) valueObj).doubleValue();
        }
        
        if (minObj instanceof Number) {
            min = ((Number) minObj).doubleValue();
        }
        
        if (maxObj instanceof Number) {
            max = ((Number) maxObj).doubleValue();
        }
        
        // 确保最小值不大于最大值
        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }
        
        // 将值限制在 min 和 max 之间
        double result = Math.max(min, Math.min(max, value));
        
        // 设置输出
        outputValues.put(OUTPUT_RESULT_ID, result);
    }
    
    // --- Getters/Setters for Properties ---
    
    public double getDefaultMin() {
        return defaultMin;
    }
    
    public void setDefaultMin(double min) {
        this.defaultMin = min;
        markDirty();
    }
    
    public double getDefaultMax() {
        return defaultMax;
    }
    
    public void setDefaultMax(double max) {
        this.defaultMax = max;
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("defaultMin", getDefaultMin());
        state.put("defaultMax", getDefaultMax());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("defaultMin")) {
                Object minObj = stateMap.get("defaultMin");
                if (minObj instanceof Number) {
                    setDefaultMin(((Number) minObj).doubleValue());
                }
            }
            
            if (stateMap.containsKey("defaultMax")) {
                Object maxObj = stateMap.get("defaultMax");
                if (maxObj instanceof Number) {
                    setDefaultMax(((Number) maxObj).doubleValue());
                }
            }
        }
    }
} 