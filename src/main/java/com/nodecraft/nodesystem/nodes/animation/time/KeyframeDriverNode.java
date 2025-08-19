package com.nodecraft.nodesystem.nodes.animation.time;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Keyframe Driver Node: 关键帧驱动器
 * 从一组时间-值对中输出插值值，实现复杂的动画曲线
 */
@NodeInfo(
    id = "animation.time.keyframe_driver",
    displayName = "Keyframe Driver",
    description = "从关键帧序列计算插值值，实现动画曲线",
    category = "animation.time"
)
public class KeyframeDriverNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_TIME_ID = "input_time";
    private static final String INPUT_NORMALIZED_TIME_ID = "input_normalized_time";
    private static final String INPUT_KEYFRAMES_ID = "input_keyframes";
    private static final String INPUT_INTERPOLATION_TYPE_ID = "input_interpolation_type";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_VALUE_ID = "output_value";
    private static final String OUTPUT_ACTIVE_KEYFRAME_INDEX_ID = "output_active_keyframe_index";
    private static final String OUTPUT_PROGRESS_ID = "output_progress";
    
    // --- 插值类型枚举 ---
    public enum InterpolationType {
        LINEAR(0, "Linear", "线性插值"),
        STEP(1, "Step", "阶梯式插值（无过渡）"),
        SMOOTH(2, "Smooth", "平滑插值（缓入缓出）");
        
        private final int id;
        private final String name;
        private final String description;
        
        InterpolationType(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
        
        public static InterpolationType fromId(int id) {
            for (InterpolationType type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return LINEAR; // 默认线性插值
        }
    }
    
    // --- 关键帧数据类 ---
    private static class Keyframe {
        final float time;     // 关键帧时间点
        final Object value;   // 关键帧值
        
        Keyframe(float time, Object value) {
            this.time = time;
            this.value = value;
        }
    }
    
    // --- 构造函数 ---
    public KeyframeDriverNode() {
        super(UUID.randomUUID(), "animation.time.keyframe_driver");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_TIME_ID, "Time", "当前时间（秒）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_NORMALIZED_TIME_ID, "Normalized Time", "归一化时间（0-1）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_KEYFRAMES_ID, "Keyframes", "关键帧列表，格式为[{time:t1, value:v1}, ...]", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_INTERPOLATION_TYPE_ID, "Interpolation Type", "插值类型 (0=线性, 1=阶梯, 2=平滑)", NodeDataType.INTEGER, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_VALUE_ID, "Value", "当前插值计算结果", NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_ACTIVE_KEYFRAME_INDEX_ID, "Active Keyframe", "当前所处关键帧索引", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_PROGRESS_ID, "Progress", "当前关键帧段内的进度（0-1）", NodeDataType.FLOAT, this));
    }
    
    @Override
    public String getDescription() {
        return "从关键帧序列计算插值值，实现动画曲线";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Float time = (Float) inputValues.getOrDefault(INPUT_TIME_ID, 0.0f);
        Float normalizedTime = (Float) inputValues.getOrDefault(INPUT_NORMALIZED_TIME_ID, null);
        Object keyframesObj = inputValues.get(INPUT_KEYFRAMES_ID);
        Integer interpolationTypeId = (Integer) inputValues.getOrDefault(INPUT_INTERPOLATION_TYPE_ID, 0);
        
        // 如果提供了归一化时间，优先使用
        float currentTime = (normalizedTime != null) ? normalizedTime : time;
        
        // 确保时间在有效范围内
        currentTime = Math.max(0.0f, Math.min(1.0f, currentTime));
        
        // 获取插值类型
        InterpolationType interpolationType = InterpolationType.fromId(interpolationTypeId);
        
        // 解析关键帧数据
        List<Keyframe> keyframes = parseKeyframes(keyframesObj);
        
        // 如果没有关键帧，则不做任何处理
        if (keyframes.isEmpty()) {
            return;
        }
        
        // 如果只有一个关键帧，直接输出该值
        if (keyframes.size() == 1) {
            outputValues.put(OUTPUT_VALUE_ID, keyframes.get(0).value);
            outputValues.put(OUTPUT_ACTIVE_KEYFRAME_INDEX_ID, 0);
            outputValues.put(OUTPUT_PROGRESS_ID, 1.0f);
            return;
        }
        
        // 找到当前时间所处的关键帧段
        int activeKeyframeIndex = -1;
        Keyframe prevKeyframe = null;
        Keyframe nextKeyframe = null;
        
        for (int i = 0; i < keyframes.size() - 1; i++) {
            Keyframe k1 = keyframes.get(i);
            Keyframe k2 = keyframes.get(i + 1);
            
            if (currentTime >= k1.time && currentTime <= k2.time) {
                activeKeyframeIndex = i;
                prevKeyframe = k1;
                nextKeyframe = k2;
                break;
            }
        }
        
        // 如果没找到适合的段，则使用首尾关键帧
        if (activeKeyframeIndex == -1) {
            if (currentTime < keyframes.get(0).time) {
                // 时间在第一个关键帧之前
                outputValues.put(OUTPUT_VALUE_ID, keyframes.get(0).value);
                outputValues.put(OUTPUT_ACTIVE_KEYFRAME_INDEX_ID, 0);
                outputValues.put(OUTPUT_PROGRESS_ID, 0.0f);
            } else {
                // 时间在最后一个关键帧之后
                int lastIndex = keyframes.size() - 1;
                outputValues.put(OUTPUT_VALUE_ID, keyframes.get(lastIndex).value);
                outputValues.put(OUTPUT_ACTIVE_KEYFRAME_INDEX_ID, lastIndex);
                outputValues.put(OUTPUT_PROGRESS_ID, 1.0f);
            }
            return;
        }
        
        // 计算当前段内的进度
        float segmentLength = nextKeyframe.time - prevKeyframe.time;
        float progress = 0.0f;
        if (segmentLength > 0) {
            progress = (currentTime - prevKeyframe.time) / segmentLength;
        }
        
        // 根据插值类型调整进度
        float adjustedProgress = progress;
        switch (interpolationType) {
            case STEP:
                // 阶梯式：只有到达下一个关键帧才变化
                adjustedProgress = (progress >= 1.0f) ? 1.0f : 0.0f;
                break;
                
            case SMOOTH:
                // 平滑插值：使用缓入缓出曲线
                adjustedProgress = smoothStep(progress);
                break;
                
            case LINEAR:
            default:
                // 线性插值：不调整
                break;
        }
        
        // 根据进度计算插值结果
        Object result = interpolateValue(prevKeyframe.value, nextKeyframe.value, adjustedProgress);
        
        // 设置输出值
        outputValues.put(OUTPUT_VALUE_ID, result);
        outputValues.put(OUTPUT_ACTIVE_KEYFRAME_INDEX_ID, activeKeyframeIndex);
        outputValues.put(OUTPUT_PROGRESS_ID, progress);
    }
    
    /**
     * 解析关键帧数据
     */
    private List<Keyframe> parseKeyframes(Object keyframesObj) {
        List<Keyframe> result = new ArrayList<>();
        
        // 创建默认关键帧（如果未提供）
        if (keyframesObj == null) {
            result.add(new Keyframe(0.0f, 0.0f));
            result.add(new Keyframe(1.0f, 1.0f));
            return result;
        }
        
        // 处理关键帧列表
        if (keyframesObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> keyframeList = (List<Object>) keyframesObj;
            
            for (Object item : keyframeList) {
                float time = 0.0f;
                Object value = null;
                
                // 处理关键帧对象
                if (item instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> keyframeMap = (Map<String, Object>) item;
                    
                    // 获取时间
                    Object timeObj = keyframeMap.get("time");
                    if (timeObj instanceof Number) {
                        time = ((Number) timeObj).floatValue();
                    }
                    
                    // 获取值
                    value = keyframeMap.get("value");
                }
                // 处理数组格式 [time, value]
                else if (item instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> pair = (List<Object>) item;
                    if (pair.size() >= 2) {
                        if (pair.get(0) instanceof Number) {
                            time = ((Number) pair.get(0)).floatValue();
                        }
                        value = pair.get(1);
                    }
                }
                
                // 添加有效的关键帧
                if (value != null) {
                    result.add(new Keyframe(time, value));
                }
            }
        }
        
        // 确保关键帧按时间排序
        result.sort(Comparator.comparing(k -> k.time));
        
        // 如果没有关键帧，添加默认关键帧
        if (result.isEmpty()) {
            result.add(new Keyframe(0.0f, 0.0f));
            result.add(new Keyframe(1.0f, 1.0f));
        }
        
        return result;
    }
    
    /**
     * 平滑步进函数（缓入缓出）
     */
    private float smoothStep(float x) {
        // 使用3阶平滑函数: x^2 * (3 - 2x)
        return x * x * (3 - 2 * x);
    }
    
    /**
     * 插值计算
     */
    private Object interpolateValue(Object start, Object end, float progress) {
        // 数值类型之间的插值
        if (start instanceof Number && end instanceof Number) {
            float startVal = ((Number) start).floatValue();
            float endVal = ((Number) end).floatValue();
            return startVal + (endVal - startVal) * progress;
        }
        
        // 布尔类型之间的插值（阈值为0.5）
        if (start instanceof Boolean && end instanceof Boolean) {
            boolean startVal = (Boolean) start;
            boolean endVal = (Boolean) end;
            if (startVal == endVal) {
                return startVal;
            }
            return progress >= 0.5f ? endVal : startVal;
        }
        
        // 字符串之间无法插值，直接根据进度选择
        if (start instanceof String && end instanceof String) {
            return progress >= 0.5f ? end : start;
        }
        
        // 对于其他类型，直接根据进度选择
        return progress >= 0.5f ? end : start;
    }
} 