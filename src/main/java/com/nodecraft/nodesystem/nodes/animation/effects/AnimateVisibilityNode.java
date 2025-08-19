package com.nodecraft.nodesystem.nodes.animation.effects;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Animate Visibility Node: 动画可见性节点
 * 实现方块的淡入淡出效果
 */
@NodeInfo(
    id = "animation.effects.animate_visibility",
    displayName = "Animate Visibility",
    description = "实现方块的淡入淡出效果",
    category = "animation.effects"
)
public class AnimateVisibilityNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_GEOMETRY_ID = "input_geometry";
    private static final String INPUT_TIME_FACTOR_ID = "input_time_factor";
    private static final String INPUT_TRANSITION_MODE_ID = "input_transition_mode";
    private static final String INPUT_STEPS_ID = "input_steps";
    private static final String INPUT_DEFAULT_BLOCK_ID = "input_default_block";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_RENDERED_GEOMETRY_ID = "output_rendered_geometry";
    
    // --- 过渡模式枚举 ---
    public enum TransitionMode {
        FADE_IN(0, "Fade In", "从不可见到可见"),
        FADE_OUT(1, "Fade Out", "从可见到不可见"),
        TOGGLE(2, "Toggle", "在0.5前淡入，0.5后淡出");
        
        private final int id;
        private final String name;
        private final String description;
        
        TransitionMode(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
        
        public static TransitionMode fromId(int id) {
            for (TransitionMode mode : values()) {
                if (mode.id == id) {
                    return mode;
                }
            }
            return FADE_IN; // 默认模式
        }
    }
    
    // --- 构造函数 ---
    public AnimateVisibilityNode() {
        super(UUID.randomUUID(), "animation.effects.animate_visibility");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_GEOMETRY_ID, "Target Blocks", "目标方块", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_TIME_FACTOR_ID, "Time Factor", "时间因子（0-1）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_TRANSITION_MODE_ID, "Transition Mode", "过渡模式（0=淡入, 1=淡出, 2=切换）", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_STEPS_ID, "Steps", "淡入淡出的步骤数", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_DEFAULT_BLOCK_ID, "Default Block", "默认替换方块（通常为空气）", NodeDataType.ANY, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_RENDERED_GEOMETRY_ID, "Rendered Blocks", "渲染后的方块", NodeDataType.LIST, this));
    }
    
    @Override
    public String getDescription() {
        return "实现方块的淡入淡出效果";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object geometryObj = inputValues.get(INPUT_GEOMETRY_ID);
        Float timeFactor = (Float) inputValues.getOrDefault(INPUT_TIME_FACTOR_ID, 0.0f);
        Integer transitionModeId = (Integer) inputValues.getOrDefault(INPUT_TRANSITION_MODE_ID, TransitionMode.FADE_IN.id);
        Integer steps = (Integer) inputValues.getOrDefault(INPUT_STEPS_ID, 10);
        Object defaultBlock = inputValues.get(INPUT_DEFAULT_BLOCK_ID);
        
        // 确保时间因子在0-1范围内
        timeFactor = Math.max(0.0f, Math.min(1.0f, timeFactor));
        
        // 确保步骤数至少为2
        steps = Math.max(2, steps);
        
        // 获取过渡模式
        TransitionMode transitionMode = TransitionMode.fromId(transitionModeId);
        
        // 处理几何体
        List<Object> renderedGeometry = processGeometry(geometryObj, timeFactor, transitionMode, steps, defaultBlock);
        
        // 设置输出值
        outputValues.put(OUTPUT_RENDERED_GEOMETRY_ID, renderedGeometry);
    }
    
    /**
     * 处理几何体，根据时间因子和过渡模式计算可见性
     */
    private List<Object> processGeometry(Object geometryObj, float timeFactor, TransitionMode transitionMode, int steps, Object defaultBlock) {
        List<Object> result = new ArrayList<>();
        
        if (geometryObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> geometryList = (List<Object>) geometryObj;
            
            // 如果几何体为空，直接返回空列表
            if (geometryList.isEmpty()) {
                return result;
            }
            
            // 计算显示的概率
            float visibilityFactor = calculateVisibilityFactor(timeFactor, transitionMode);
            
            // 对每个方块应用可见性
            for (Object block : geometryList) {
                Object processedBlock = applyVisibility(block, visibilityFactor, steps, defaultBlock);
                if (processedBlock != null) {
                    result.add(processedBlock);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 计算可见性因子
     */
    private float calculateVisibilityFactor(float timeFactor, TransitionMode transitionMode) {
        switch (transitionMode) {
            case FADE_IN:
                // 淡入：从0增加到1
                return timeFactor;
                
            case FADE_OUT:
                // 淡出：从1减少到0
                return 1.0f - timeFactor;
                
            case TOGGLE:
                // 切换：0-0.5淡入，0.5-1淡出
                if (timeFactor < 0.5f) {
                    return timeFactor * 2.0f; // 0-0.5 映射到 0-1
                } else {
                    return (1.0f - timeFactor) * 2.0f; // 0.5-1 映射到 1-0
                }
                
            default:
                return timeFactor;
        }
    }
    
    /**
     * 根据可见性因子决定方块是否可见
     */
    private Object applyVisibility(Object block, float visibilityFactor, int steps, Object defaultBlock) {
        // 如果可见性因子为0，则返回默认方块（通常为空气或null）
        if (visibilityFactor <= 0.0f) {
            return defaultBlock;
        }
        
        // 如果可见性因子为1，则完全显示原始方块
        if (visibilityFactor >= 1.0f) {
            return block;
        }
        
        // 计算当前的过渡阶段（基于步骤数）
        int currentStep = Math.round(visibilityFactor * (steps - 1));
        float stepFactor = (float) currentStep / (steps - 1);
        
        // 如果方块在当前阶段应该显示，则返回原始方块
        // 使用随机化处理来创建"淡入淡出"的视觉效果
        boolean shouldShow = visibilityFactor >= (currentStep + 1.0f) / steps;
        
        return shouldShow ? block : defaultBlock;
    }
} 