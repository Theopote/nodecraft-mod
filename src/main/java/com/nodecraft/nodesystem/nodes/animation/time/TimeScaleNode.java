package com.nodecraft.nodesystem.nodes.animation.time;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Time Scale Node: 时间缩放节点
 * 调整时间流逝的速度，可实现慢放或快放效果
 */
@NodeInfo(
    id = "animation.time.time_scale",
    displayName = "Time Scale",
    description = "调整时间流逝的速度，可用于慢放或快放",
    category = "animation.time"
)
public class TimeScaleNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_TIME_ID = "input_time";
    private static final String INPUT_SCALE_FACTOR_ID = "input_scale_factor";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_SCALED_TIME_ID = "output_scaled_time";
    
    // --- 构造函数 ---
    public TimeScaleNode() {
        super(UUID.randomUUID(), "animation.time.time_scale");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_TIME_ID, "Time", "输入时间（秒）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_SCALE_FACTOR_ID, "Scale Factor", "时间缩放系数（<1慢放，>1快放）", NodeDataType.FLOAT, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_SCALED_TIME_ID, "Scaled Time", "缩放后的时间（秒）", NodeDataType.FLOAT, this));
    }
    
    @Override
    public String getDescription() {
        return "调整时间流逝的速度，可用于慢放或快放";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Float time = (Float) inputValues.getOrDefault(INPUT_TIME_ID, 0.0f);
        Float scaleFactor = (Float) inputValues.getOrDefault(INPUT_SCALE_FACTOR_ID, 1.0f);
        
        // 防止缩放因子为零或负数
        if (scaleFactor == null || scaleFactor <= 0) {
            scaleFactor = 1.0f; // 默认为1，不缩放
        }
        
        // 计算缩放后的时间
        float scaledTime = time * scaleFactor;
        
        // 设置输出值
        outputValues.put(OUTPUT_SCALED_TIME_ID, scaledTime);
    }
} 