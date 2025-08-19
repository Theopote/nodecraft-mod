package com.nodecraft.nodesystem.nodes.animation.time;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Pulse Generator Node: 脉冲生成器
 * 在特定时间点或间隔生成脉冲信号
 */
@NodeInfo(
    id = "animation.time.pulse_generator",
    displayName = "Pulse Generator",
    description = "在特定时间点或间隔生成脉冲信号",
    category = "animation.time"
)
public class PulseGeneratorNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_TIME_ID = "input_time";
    private static final String INPUT_TRIGGER_TIMES_ID = "input_trigger_times";
    private static final String INPUT_INTERVAL_ID = "input_interval";
    private static final String INPUT_PULSE_DURATION_ID = "input_pulse_duration";
    private static final String INPUT_MODE_ID = "input_mode";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_PULSE_ID = "output_pulse";
    private static final String OUTPUT_PULSE_COUNT_ID = "output_pulse_count";
    
    // --- 模式枚举 ---
    public enum PulseMode {
        SPECIFIC_TIMES(0, "Specific Times", "在指定时间点生成脉冲"),
        INTERVAL(1, "Interval", "按固定间隔生成脉冲");
        
        private final int id;
        private final String name;
        private final String description;
        
        PulseMode(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
        
        public static PulseMode fromId(int id) {
            for (PulseMode mode : values()) {
                if (mode.id == id) {
                    return mode;
                }
            }
            return SPECIFIC_TIMES; // 默认
        }
    }
    
    // --- 状态变量 ---
    private int pulseCount = 0; // 已生成的脉冲数量
    private List<Float> lastTriggeredTimes = new ArrayList<>(); // 上次触发的时间点列表
    
    // --- 构造函数 ---
    public PulseGeneratorNode() {
        super(UUID.randomUUID(), "animation.time.pulse_generator");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_TIME_ID, "Time", "当前时间（秒）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_TRIGGER_TIMES_ID, "Trigger Times", "触发脉冲的时间点列表", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_INTERVAL_ID, "Interval", "脉冲间隔（秒）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_PULSE_DURATION_ID, "Pulse Duration", "脉冲持续时间（秒）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_MODE_ID, "Mode", "模式 (0=特定时间点, 1=固定间隔)", NodeDataType.INTEGER, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_PULSE_ID, "Pulse", "脉冲信号（触发时为true）", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_PULSE_COUNT_ID, "Pulse Count", "已生成的脉冲数量", NodeDataType.INTEGER, this));
    }
    
    @Override
    public String getDescription() {
        return "在特定时间点或间隔生成脉冲信号";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Float currentTime = (Float) inputValues.getOrDefault(INPUT_TIME_ID, 0.0f);
        Object triggerTimesObj = inputValues.get(INPUT_TRIGGER_TIMES_ID);
        Float interval = (Float) inputValues.getOrDefault(INPUT_INTERVAL_ID, 1.0f); // 默认1秒间隔
        Float pulseDurationInput = (Float) inputValues.getOrDefault(INPUT_PULSE_DURATION_ID, 0.1f); // 默认0.1秒持续时间
        Integer modeId = (Integer) inputValues.getOrDefault(INPUT_MODE_ID, 0); // 默认特定时间点模式
        
        // 确保参数合法
        interval = Math.max(0.01f, interval); // 最小间隔0.01秒
        final float pulseDuration = Math.max(0.01f, Math.min(interval, pulseDurationInput)); // 持续时间至少0.01秒，但不超过间隔
        
        // 获取模式
        PulseMode mode = PulseMode.fromId(modeId);
        
        // 默认不输出脉冲
        boolean pulse = false;
        
        if (mode == PulseMode.SPECIFIC_TIMES) {
            // 特定时间点模式
            List<Float> triggerTimes = new ArrayList<>();
            
            // 处理触发时间列表
            if (triggerTimesObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> inputList = (List<Object>) triggerTimesObj;
                for (Object timeObj : inputList) {
                    if (timeObj instanceof Number) {
                        triggerTimes.add(((Number) timeObj).floatValue());
                    }
                }
            }
            
            // 如果没有提供时间点，使用默认值
            if (triggerTimes.isEmpty()) {
                triggerTimes.add(1.0f); // 默认在1秒处触发
            }
            
            // 检查是否到达触发时间
            for (Float triggerTime : triggerTimes) {
                if (currentTime >= triggerTime && 
                    currentTime <= triggerTime + pulseDuration &&
                    !lastTriggeredTimes.contains(triggerTime)) {
                    
                    // 触发脉冲
                    pulse = true;
                    
                    // 如果是新触发的时间点，增加计数
                    if (!lastTriggeredTimes.contains(triggerTime)) {
                        pulseCount++;
                        lastTriggeredTimes.add(triggerTime);
                    }
                }
            }
            
            // 移除已经过期的触发时间（超过脉冲持续时间）
            lastTriggeredTimes.removeIf(time -> currentTime > time + pulseDuration);
            
        } else if (mode == PulseMode.INTERVAL) {
            // 固定间隔模式
            
            // 计算当前所处的间隔
            int currentInterval = (int) Math.floor(currentTime / interval);
            
            // 计算当前间隔内的时间
            float timeInInterval = currentTime % interval;
            
            // 检查是否在脉冲持续时间内
            pulse = timeInInterval <= pulseDuration;
            
            // 更新脉冲计数（只有当进入新间隔且在脉冲持续时间内时才增加）
            if (pulse && currentInterval >= pulseCount) {
                pulseCount = currentInterval + 1;
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_PULSE_ID, pulse);
        outputValues.put(OUTPUT_PULSE_COUNT_ID, pulseCount);
    }
    
    /**
     * 重置节点状态
     */
    public void reset() {
        pulseCount = 0;
        lastTriggeredTimes.clear();
    }
} 