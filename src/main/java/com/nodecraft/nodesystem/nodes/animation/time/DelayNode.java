package com.nodecraft.nodesystem.nodes.animation.time;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

/**
 * Delay Node: 延迟节点
 * 延迟数据流的传递，将输入值在指定时间后输出
 */
@NodeInfo(
    id = "animation.time.delay",
    displayName = "Delay",
    description = "延迟数据流的传递，将输入值在指定时间后输出",
    category = "animation.time"
)
public class DelayNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_VALUE_ID = "input_value";
    private static final String INPUT_DELAY_DURATION_ID = "input_delay_duration";
    private static final String INPUT_TIME_ID = "input_time";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_VALUE_ID = "output_value";
    
    // --- 状态变量 ---
    private final Queue<DelayedValue> delayQueue = new LinkedList<>();
    private double lastProcessedTime = 0.0;
    
    // --- 内部类：延迟值 ---
    private static class DelayedValue {
        final Object value;       // 要延迟的值
        final double releaseTime; // 释放时间
        
        DelayedValue(Object value, double releaseTime) {
            this.value = value;
            this.releaseTime = releaseTime;
        }
    }
    
    // --- 构造函数 ---
    public DelayNode() {
        super(UUID.randomUUID(), "animation.time.delay");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_VALUE_ID, "Input", "要延迟的输入值", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_DELAY_DURATION_ID, "Delay Duration", "延迟时长（秒）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_TIME_ID, "Time", "当前时间（秒），通常来自时钟", NodeDataType.FLOAT, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_VALUE_ID, "Output", "延迟后的输出值", NodeDataType.ANY, this));
    }
    
    @Override
    public String getDescription() {
        return "延迟数据流的传递，将输入值在指定时间后输出";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object inputValue = inputValues.get(INPUT_VALUE_ID);
        Float delayDuration = (Float) inputValues.getOrDefault(INPUT_DELAY_DURATION_ID, 1.0f); // 默认延迟1秒
        Float currentTime = (Float) inputValues.getOrDefault(INPUT_TIME_ID, 0.0f);
        
        // 确保延迟时间是正数
        delayDuration = Math.max(0.0f, delayDuration);
        
        // 如果时间回退了（例如动画重置），则清空队列
        if (currentTime < lastProcessedTime) {
            delayQueue.clear();
        }
        lastProcessedTime = currentTime;
        
        // 如果有新输入，加入延迟队列
        if (inputValue != null) {
            double releaseTime = currentTime + delayDuration;
            delayQueue.add(new DelayedValue(inputValue, releaseTime));
            // 清除输入值，防止重复处理
            inputValues.remove(INPUT_VALUE_ID);
        }
        
        // 检查队列中是否有需要释放的值
        Object outputValue = null;
        
        // 移除所有已过期的值，保留最后一个作为输出
        while (!delayQueue.isEmpty() && delayQueue.peek().releaseTime <= currentTime) {
            outputValue = delayQueue.poll().value;
        }
        
        // 如果有要输出的值，设置到输出端口
        if (outputValue != null) {
            outputValues.put(OUTPUT_VALUE_ID, outputValue);
        }
    }
} 