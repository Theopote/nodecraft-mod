package com.nodecraft.nodesystem.nodes.animation.time;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Animation Clock: 动画时钟节点，控制动画的时间流
 * 核心节点，输出当前动画的进度（通常在0到1之间，代表从开始到结束的比例）
 */
@NodeInfo(
    id = "animation.time.animation_clock",
    displayName = "Animation Clock",
    description = "控制动画的时间流，输出当前动画的进度",
    category = "animation.time"
)
public class AnimationClockNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_DURATION_ID = "input_duration";
    private static final String INPUT_LOOP_ID = "input_loop";
    private static final String INPUT_RESET_ID = "input_reset";
    private static final String INPUT_PAUSE_ID = "input_pause";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_CURRENT_TIME_ID = "output_current_time";
    private static final String OUTPUT_NORMALIZED_TIME_ID = "output_normalized_time";
    private static final String OUTPUT_CURRENT_FRAME_ID = "output_current_frame";
    private static final String OUTPUT_IS_PLAYING_ID = "output_is_playing";

    // --- 状态变量 ---
    private double lastUpdateTime; // 上次更新的时间（毫秒）
    private double currentTime; // 当前时间（秒）
    private boolean isPlaying; // 是否正在播放
    private double frameRate = 20.0; // 默认帧率（每秒帧数）

    // --- 构造函数 ---
    public AnimationClockNode() {
        super(UUID.randomUUID(), "animation.time.animation_clock");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_DURATION_ID, "Duration", "动画总时长（秒）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_LOOP_ID, "Loop", "是否循环播放", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_RESET_ID, "Reset", "重置动画（接收任意信号触发）", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_PAUSE_ID, "Pause", "暂停/播放切换（接收任意信号触发）", NodeDataType.ANY, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_CURRENT_TIME_ID, "Current Time", "当前时间（秒）", NodeDataType.FLOAT, this));
        addOutputPort(new BasePort(OUTPUT_NORMALIZED_TIME_ID, "Normalized Time", "归一化时间（0-1）", NodeDataType.FLOAT, this));
        addOutputPort(new BasePort(OUTPUT_CURRENT_FRAME_ID, "Current Frame", "当前帧", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_IS_PLAYING_ID, "Is Playing", "是否正在播放", NodeDataType.BOOLEAN, this));

        // 初始化状态
        reset();
    }
    
    @Override
    public String getDescription() {
        return "控制动画的时间流，输出当前动画的进度";
    }
    
    /**
     * 重置动画时钟
     */
    private void reset() {
        currentTime = 0.0;
        lastUpdateTime = System.currentTimeMillis();
        isPlaying = true;
    }
    
    /**
     * 切换播放/暂停状态
     */
    private void togglePlayPause() {
        isPlaying = !isPlaying;
        if (isPlaying) {
            // 重新开始计时，避免暂停期间的时间跳跃
            lastUpdateTime = System.currentTimeMillis();
        }
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Float duration = (Float) inputValues.getOrDefault(INPUT_DURATION_ID, 5.0f); // 默认5秒
        Boolean loop = (Boolean) inputValues.getOrDefault(INPUT_LOOP_ID, false); // 默认不循环
        
        // 检查Reset信号
        if (inputValues.containsKey(INPUT_RESET_ID) && inputValues.get(INPUT_RESET_ID) != null) {
            reset();
            // 清除Reset信号，避免重复触发
            inputValues.remove(INPUT_RESET_ID);
        }
        
        // 检查Pause信号
        if (inputValues.containsKey(INPUT_PAUSE_ID) && inputValues.get(INPUT_PAUSE_ID) != null) {
            togglePlayPause();
            // 清除Pause信号，避免重复触发
            inputValues.remove(INPUT_PAUSE_ID);
        }
        
        // 如果正在播放，则更新时间
        if (isPlaying) {
            // 计算时间增量
            double currentTimeMillis = System.currentTimeMillis();
            double deltaTime = (currentTimeMillis - lastUpdateTime) / 1000.0; // 转换为秒
            lastUpdateTime = currentTimeMillis;
            
            // 更新当前时间
            currentTime += deltaTime;
            
            // 处理循环
            if (currentTime > duration) {
                if (loop) {
                    // 循环播放：取模
                    currentTime = currentTime % duration;
                } else {
                    // 非循环：停在结束位置并停止播放
                    currentTime = duration;
                    isPlaying = false;
                }
            }
        }
        
        // 计算归一化时间（0-1）
        double normalizedTime = (duration > 0) ? (currentTime / duration) : 0;
        // 限制在0-1范围内
        normalizedTime = Math.max(0, Math.min(1, normalizedTime));
        
        // 计算当前帧
        int currentFrame = (int) Math.floor(currentTime * frameRate);
        
        // 设置输出值
        outputValues.put(OUTPUT_CURRENT_TIME_ID, (float) currentTime);
        outputValues.put(OUTPUT_NORMALIZED_TIME_ID, (float) normalizedTime);
        outputValues.put(OUTPUT_CURRENT_FRAME_ID, currentFrame);
        outputValues.put(OUTPUT_IS_PLAYING_ID, isPlaying);
        
        // 标记节点为脏，确保下一帧会重新计算
        if (isPlaying) {
            markDirty();
        }
    }

    /**
     * 设置帧率
     * @param frameRate 每秒帧数
     */
    public void setFrameRate(double frameRate) {
        if (frameRate > 0) {
            this.frameRate = frameRate;
        }
    }
} 