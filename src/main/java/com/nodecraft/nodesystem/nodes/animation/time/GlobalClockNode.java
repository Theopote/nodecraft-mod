package com.nodecraft.nodesystem.nodes.animation.time;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Global Clock: 全局时钟节点
 * 作为动画系统的核心时间源，生成和管理动画的时间流
 */
@NodeInfo(
    id = "animation.time.global_clock",
    displayName = "Global Clock",
    description = "生成和管理动画的时间流，作为核心动画时间源",
    category = "animation.time"
)
public class GlobalClockNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_DURATION_ID = "input_duration";
    private static final String INPUT_LOOP_MODE_ID = "input_loop_mode";
    private static final String INPUT_FRAMERATE_ID = "input_framerate";
    private static final String INPUT_PLAY_ID = "input_play";
    private static final String INPUT_PAUSE_ID = "input_pause";
    private static final String INPUT_STOP_ID = "input_stop";
    private static final String INPUT_RESET_ID = "input_reset";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_TIME_ID = "output_time";
    private static final String OUTPUT_NORMALIZED_TIME_ID = "output_normalized_time";
    private static final String OUTPUT_CURRENT_FRAME_ID = "output_current_frame";
    private static final String OUTPUT_IS_PLAYING_ID = "output_is_playing";
    private static final String OUTPUT_ON_START_ID = "output_on_start";
    private static final String OUTPUT_ON_END_ID = "output_on_end";

    // --- 循环模式枚举 ---
    public enum LoopMode {
        ONCE(0, "Once", "播放一次后停止"),
        LOOP(1, "Loop", "循环播放"),
        PING_PONG(2, "PingPong", "来回播放");
        
        private final int id;
        private final String name;
        private final String description;
        
        LoopMode(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
        
        public static LoopMode fromId(int id) {
            for (LoopMode mode : values()) {
                if (mode.id == id) {
                    return mode;
                }
            }
            return ONCE; // 默认一次
        }
    }

    // --- 状态变量 ---
    private double lastUpdateTime; // 上次更新的时间（毫秒）
    private double currentTime; // 当前时间（秒）
    private boolean isPlaying; // 是否正在播放
    private boolean isForward; // 方向(用于PING_PONG模式)
    private boolean wasStarted; // 是否已经开始过(用于触发开始事件)
    private boolean wasEnded; // 是否已经结束过(用于触发结束事件)
    private double frameRate = 30.0; // 默认帧率（每秒帧数）
    
    // 全局时钟实例（单例模式）
    private static GlobalClockNode instance;

    // --- 构造函数 ---
    public GlobalClockNode() {
        super(UUID.randomUUID(), "animation.time.global_clock");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_DURATION_ID, "Duration", "动画总时长（秒）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_LOOP_MODE_ID, "Loop Mode", "循环模式 (0=Once, 1=Loop, 2=PingPong)", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_FRAMERATE_ID, "Framerate", "帧率（每秒帧数）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_PLAY_ID, "Play", "播放信号", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_PAUSE_ID, "Pause", "暂停信号", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_STOP_ID, "Stop", "停止信号", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_RESET_ID, "Reset", "重置信号", NodeDataType.ANY, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_TIME_ID, "Time", "当前时间（秒）", NodeDataType.FLOAT, this));
        addOutputPort(new BasePort(OUTPUT_NORMALIZED_TIME_ID, "Normalized Time", "归一化时间（0-1）", NodeDataType.FLOAT, this));
        addOutputPort(new BasePort(OUTPUT_CURRENT_FRAME_ID, "Current Frame", "当前帧", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_IS_PLAYING_ID, "Is Playing", "是否正在播放", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_ON_START_ID, "On Start", "动画开始时发出脉冲", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_ON_END_ID, "On End", "动画结束时发出脉冲", NodeDataType.BOOLEAN, this));

        // 初始化状态
        reset();
        
        // 设置为全局实例
        instance = this;
    }
    
    /**
     * 获取全局时钟实例
     */
    public static GlobalClockNode getInstance() {
        return instance;
    }
    
    @Override
    public String getDescription() {
        return "生成和管理动画的时间流，作为核心动画时间源";
    }
    
    /**
     * 重置时钟
     */
    private void reset() {
        currentTime = 0.0;
        lastUpdateTime = System.currentTimeMillis();
        isPlaying = false;
        isForward = true;
        wasStarted = false;
        wasEnded = false;
    }
    
    /**
     * 开始播放
     */
    private void play() {
        if (!isPlaying) {
            isPlaying = true;
            lastUpdateTime = System.currentTimeMillis();
        }
    }
    
    /**
     * 暂停播放
     */
    private void pause() {
        isPlaying = false;
    }
    
    /**
     * 停止播放并重置
     */
    private void stop() {
        isPlaying = false;
        currentTime = 0.0;
        isForward = true;
        wasStarted = false;
        wasEnded = false;
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Float duration = (Float) inputValues.getOrDefault(INPUT_DURATION_ID, 10.0f); // 默认10秒
        Integer loopModeId = (Integer) inputValues.getOrDefault(INPUT_LOOP_MODE_ID, 0); // 默认Once
        Float framerateInput = (Float) inputValues.getOrDefault(INPUT_FRAMERATE_ID, 30.0f); // 默认30fps
        Object playSignal = inputValues.get(INPUT_PLAY_ID);
        Object pauseSignal = inputValues.get(INPUT_PAUSE_ID);
        Object stopSignal = inputValues.get(INPUT_STOP_ID);
        Object resetSignal = inputValues.get(INPUT_RESET_ID);
        
        // 更新帧率
        if (framerateInput != null && framerateInput > 0) {
            frameRate = framerateInput;
        }
        
        // 处理控制信号
        if (playSignal != null) {
            play();
            inputValues.remove(INPUT_PLAY_ID); // 清除信号防止重复触发
        }
        
        if (pauseSignal != null) {
            pause();
            inputValues.remove(INPUT_PAUSE_ID);
        }
        
        if (stopSignal != null) {
            stop();
            inputValues.remove(INPUT_STOP_ID);
        }
        
        if (resetSignal != null) {
            reset();
            inputValues.remove(INPUT_RESET_ID);
        }
        
        // 获取循环模式
        LoopMode loopMode = LoopMode.fromId(loopModeId);
        
        // 初始化事件状态
        boolean onStart = false;
        boolean onEnd = false;
        
        // 如果正在播放，则更新时间
        if (isPlaying) {
            // 开始事件
            if (!wasStarted && currentTime == 0) {
                onStart = true;
                wasStarted = true;
            }
            
            // 计算时间增量
            double currentTimeMillis = System.currentTimeMillis();
            double deltaTime = (currentTimeMillis - lastUpdateTime) / 1000.0; // 转换为秒
            lastUpdateTime = currentTimeMillis;
            
            // 更新当前时间
            if (isForward) {
                currentTime += deltaTime;
            } else {
                currentTime -= deltaTime;
            }
            
            // 处理循环逻辑
            switch (loopMode) {
                case ONCE:
                    if (currentTime >= duration) {
                        currentTime = duration;
                        isPlaying = false;
                        if (!wasEnded) {
                            onEnd = true;
                            wasEnded = true;
                        }
                    }
                    break;
                    
                case LOOP:
                    if (currentTime >= duration) {
                        // 循环：回到开始
                        currentTime = currentTime % duration;
                        onEnd = true;
                        // 循环模式下，下一轮可以再次触发开始和结束事件
                        wasStarted = false;
                    }
                    break;
                    
                case PING_PONG:
                    if (isForward && currentTime >= duration) {
                        // 达到最大值，开始反向
                        currentTime = duration;
                        isForward = false;
                        onEnd = true;
                    } else if (!isForward && currentTime <= 0) {
                        // 达到最小值，开始正向
                        currentTime = 0;
                        isForward = true;
                        wasStarted = false; // 允许再次触发开始事件
                    }
                    break;
            }
        }
        
        // 计算归一化时间（0-1）
        double normalizedTime = (duration > 0) ? (currentTime / duration) : 0;
        // 限制在0-1范围内
        normalizedTime = Math.max(0, Math.min(1, normalizedTime));
        
        // 计算当前帧
        int currentFrame = (int) Math.floor(currentTime * frameRate);
        
        // 设置输出值
        outputValues.put(OUTPUT_TIME_ID, (float) currentTime);
        outputValues.put(OUTPUT_NORMALIZED_TIME_ID, (float) normalizedTime);
        outputValues.put(OUTPUT_CURRENT_FRAME_ID, currentFrame);
        outputValues.put(OUTPUT_IS_PLAYING_ID, isPlaying);
        outputValues.put(OUTPUT_ON_START_ID, onStart);
        outputValues.put(OUTPUT_ON_END_ID, onEnd);
        
        // 标记节点为脏，确保下一帧会重新计算
        if (isPlaying) {
            markDirty();
        }
    }
} 