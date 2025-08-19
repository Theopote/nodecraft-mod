package com.nodecraft.nodesystem.nodes.animation.interpolation;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Remap Time Node: 时间重映射节点
 * 将一个时间范围映射到另一个范围，用于调整动画速度和时序
 */
@NodeInfo(
    id = "animation.interpolation.remap_time",
    displayName = "Remap Time",
    description = "将一个时间范围映射到另一个范围",
    category = "animation.interpolation"
)
public class RemapTimeNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_TIME_ID = "input_time";
    private static final String INPUT_SOURCE_MIN_ID = "input_source_min";
    private static final String INPUT_SOURCE_MAX_ID = "input_source_max";
    private static final String INPUT_TARGET_MIN_ID = "input_target_min";
    private static final String INPUT_TARGET_MAX_ID = "input_target_max";
    private static final String INPUT_CLAMP_ID = "input_clamp";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_REMAPPED_TIME_ID = "output_remapped_time";
    private static final String OUTPUT_NORMALIZED_TIME_ID = "output_normalized_time";
    
    // --- 构造函数 ---
    public RemapTimeNode() {
        super(UUID.randomUUID(), "animation.interpolation.remap_time");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_TIME_ID, "Time", "输入时间值", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_SOURCE_MIN_ID, "Source Min", "源范围最小值", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_SOURCE_MAX_ID, "Source Max", "源范围最大值", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_TARGET_MIN_ID, "Target Min", "目标范围最小值", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_TARGET_MAX_ID, "Target Max", "目标范围最大值", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_CLAMP_ID, "Clamp", "是否限制结果在目标范围内", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_REMAPPED_TIME_ID, "Remapped Time", "重映射后的时间值", NodeDataType.FLOAT, this));
        addOutputPort(new BasePort(OUTPUT_NORMALIZED_TIME_ID, "Normalized Time", "归一化的时间值（0-1）", NodeDataType.FLOAT, this));
    }
    
    @Override
    public String getDescription() {
        return "将一个时间范围映射到另一个范围";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Float time = (Float) inputValues.getOrDefault(INPUT_TIME_ID, 0.0f);
        Float sourceMin = (Float) inputValues.getOrDefault(INPUT_SOURCE_MIN_ID, 0.0f);
        Float sourceMax = (Float) inputValues.getOrDefault(INPUT_SOURCE_MAX_ID, 1.0f);
        Float targetMin = (Float) inputValues.getOrDefault(INPUT_TARGET_MIN_ID, 0.0f);
        Float targetMax = (Float) inputValues.getOrDefault(INPUT_TARGET_MAX_ID, 1.0f);
        Boolean clamp = (Boolean) inputValues.getOrDefault(INPUT_CLAMP_ID, true);
        
        // 确保源范围和目标范围有效
        if (sourceMin.equals(sourceMax)) {
            sourceMax = sourceMin + 1.0f; // 防止除以零
        }
        
        // 计算归一化时间
        float normalizedTime = (time - sourceMin) / (sourceMax - sourceMin);
        
        // 如果需要限制范围
        if (clamp) {
            normalizedTime = Math.max(0.0f, Math.min(1.0f, normalizedTime));
        }
        
        // 重映射到目标范围
        float remappedTime = targetMin + normalizedTime * (targetMax - targetMin);
        
        // 设置输出值
        outputValues.put(OUTPUT_REMAPPED_TIME_ID, remappedTime);
        outputValues.put(OUTPUT_NORMALIZED_TIME_ID, normalizedTime);
    }
    
    /**
     * 重映射值
     */
    public static float remap(float value, float fromMin, float fromMax, float toMin, float toMax, boolean clamp) {
        // 计算归一化值
        float normalized = (value - fromMin) / (fromMax - fromMin);
        
        // 如果需要限制范围
        if (clamp) {
            normalized = Math.max(0.0f, Math.min(1.0f, normalized));
        }
        
        // 重映射到目标范围
        return toMin + normalized * (toMax - toMin);
    }
} 