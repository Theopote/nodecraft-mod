package com.nodecraft.nodesystem.nodes.math.basic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.Map;
import java.util.UUID;

/**
 * Remap Node: 将数值从输入范围映射到输出范围
 * 例如: 将 [0-100] 范围内的75映射到 [0-1] 范围 = 0.75
 */
@NodeInfo(
    id = "math.basic.remap",
    displayName = "数值重映射",
    description = "将数值从输入范围映射到输出范围",
    category = "math.basic"
)
public class RemapNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_VALUE_ID = "input_value";
    private static final String INPUT_IN_MIN_ID = "input_in_min";
    private static final String INPUT_IN_MAX_ID = "input_in_max";
    private static final String INPUT_OUT_MIN_ID = "input_out_min";
    private static final String INPUT_OUT_MAX_ID = "input_out_max";
    private static final String INPUT_CLAMP_ID = "input_clamp"; // 可选布尔值, 是否将结果限制在输出范围内

    // --- 输出端口 IDs ---
    private static final String OUTPUT_RESULT_ID = "output_result";

    // --- 默认值 ---
    private double defaultInMin = 0.0;
    private double defaultInMax = 1.0;
    private double defaultOutMin = 0.0;
    private double defaultOutMax = 1.0;
    private boolean defaultClamp = true;

    // --- 构造函数 ---
    public RemapNode() {
        super(UUID.randomUUID(), "math.operators.remap");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_VALUE_ID, "Value", "Value to remap", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_IN_MIN_ID, "In Min", "Input range minimum", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_IN_MAX_ID, "In Max", "Input range maximum", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_OUT_MIN_ID, "Out Min", "Output range minimum", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_OUT_MAX_ID, "Out Max", "Output range maximum", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_CLAMP_ID, "Clamp", "Clamp result to output range", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_RESULT_ID, "Result", "The remapped value", NodeDataType.DOUBLE, this));
    }

    @Override
    public String getDescription() {
        return "Maps a value from an input range to an output range";
    }
    
    @Override
    public String getDisplayName() {
        return "Remap";
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入
        Object valueObj = inputValues.get(INPUT_VALUE_ID);
        Object inMinObj = inputValues.get(INPUT_IN_MIN_ID);
        Object inMaxObj = inputValues.get(INPUT_IN_MAX_ID);
        Object outMinObj = inputValues.get(INPUT_OUT_MIN_ID);
        Object outMaxObj = inputValues.get(INPUT_OUT_MAX_ID);
        Object clampObj = inputValues.get(INPUT_CLAMP_ID);
        
        // 默认值
        double value = 0.0;
        double inMin = defaultInMin;
        double inMax = defaultInMax;
        double outMin = defaultOutMin;
        double outMax = defaultOutMax;
        boolean clamp = defaultClamp;
        
        // 解析输入
        if (valueObj instanceof Number) {
            value = ((Number) valueObj).doubleValue();
        }
        
        if (inMinObj instanceof Number) {
            inMin = ((Number) inMinObj).doubleValue();
        }
        
        if (inMaxObj instanceof Number) {
            inMax = ((Number) inMaxObj).doubleValue();
        }
        
        if (outMinObj instanceof Number) {
            outMin = ((Number) outMinObj).doubleValue();
        }
        
        if (outMaxObj instanceof Number) {
            outMax = ((Number) outMaxObj).doubleValue();
        }
        
        if (clampObj instanceof Boolean) {
            clamp = (Boolean) clampObj;
        }
        
        // 计算映射的值
        double result;
        
        // 检查输入范围是否为零宽度 (避免除以零)
        if (Math.abs(inMax - inMin) < 1e-10) {
            // 如果输入范围为零宽度，直接返回输出范围的中间值
            result = (outMin + outMax) / 2.0;
        } else {
            // 标准映射公式: 
            // result = outMin + (value - inMin) * (outMax - outMin) / (inMax - inMin)
            double normalizedValue = (value - inMin) / (inMax - inMin);
            result = outMin + normalizedValue * (outMax - outMin);
        }
        
        // 如需要, 将结果限制在输出范围内
        if (clamp) {
            double minOut = Math.min(outMin, outMax);
            double maxOut = Math.max(outMin, outMax);
            result = Math.max(minOut, Math.min(maxOut, result));
        }
        
        // 设置输出
        outputValues.put(OUTPUT_RESULT_ID, result);
    }
    
    // --- Getters/Setters for Properties ---
    
    public double getDefaultInMin() {
        return defaultInMin;
    }
    
    public void setDefaultInMin(double value) {
        this.defaultInMin = value;
        markDirty();
    }
    
    public double getDefaultInMax() {
        return defaultInMax;
    }
    
    public void setDefaultInMax(double value) {
        this.defaultInMax = value;
        markDirty();
    }
    
    public double getDefaultOutMin() {
        return defaultOutMin;
    }
    
    public void setDefaultOutMin(double value) {
        this.defaultOutMin = value;
        markDirty();
    }
    
    public double getDefaultOutMax() {
        return defaultOutMax;
    }
    
    public void setDefaultOutMax(double value) {
        this.defaultOutMax = value;
        markDirty();
    }
    
    public boolean getDefaultClamp() {
        return defaultClamp;
    }
    
    public void setDefaultClamp(boolean value) {
        this.defaultClamp = value;
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("defaultInMin", getDefaultInMin());
        state.put("defaultInMax", getDefaultInMax());
        state.put("defaultOutMin", getDefaultOutMin());
        state.put("defaultOutMax", getDefaultOutMax());
        state.put("defaultClamp", getDefaultClamp());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("defaultInMin")) {
                Object obj = stateMap.get("defaultInMin");
                if (obj instanceof Number) {
                    setDefaultInMin(((Number) obj).doubleValue());
                }
            }
            
            if (stateMap.containsKey("defaultInMax")) {
                Object obj = stateMap.get("defaultInMax");
                if (obj instanceof Number) {
                    setDefaultInMax(((Number) obj).doubleValue());
                }
            }
            
            if (stateMap.containsKey("defaultOutMin")) {
                Object obj = stateMap.get("defaultOutMin");
                if (obj instanceof Number) {
                    setDefaultOutMin(((Number) obj).doubleValue());
                }
            }
            
            if (stateMap.containsKey("defaultOutMax")) {
                Object obj = stateMap.get("defaultOutMax");
                if (obj instanceof Number) {
                    setDefaultOutMax(((Number) obj).doubleValue());
                }
            }
            
            if (stateMap.containsKey("defaultClamp")) {
                Object obj = stateMap.get("defaultClamp");
                if (obj instanceof Boolean) {
                    setDefaultClamp((Boolean) obj);
                }
            }
        }
    }
} 