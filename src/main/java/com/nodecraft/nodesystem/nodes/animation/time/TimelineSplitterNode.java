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
 * Timeline Splitter Node: 时间线分割器
 * 将动画时间线分割成多个阶段，计算当前所处阶段和阶段内进度
 */
@NodeInfo(
    id = "animation.time.timeline_splitter",
    displayName = "Timeline Splitter",
    description = "将动画时间线分割成多个阶段，输出当前阶段信息",
    category = "animation.time"
)
public class TimelineSplitterNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_NORMALIZED_TIME_ID = "input_normalized_time";
    private static final String INPUT_STAGE_POINTS_ID = "input_stage_points";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_ACTIVE_STAGE_ID = "output_active_stage";
    private static final String OUTPUT_STAGE_PROGRESS_ID = "output_stage_progress";
    private static final String OUTPUT_ON_STAGE_START_ID = "output_on_stage_start";
    
    // --- 状态变量 ---
    private int lastActiveStage = -1; // 上一个活动阶段
    
    // --- 构造函数 ---
    public TimelineSplitterNode() {
        super(UUID.randomUUID(), "animation.time.timeline_splitter");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_NORMALIZED_TIME_ID, "Normalized Time", "归一化时间（0-1）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_STAGE_POINTS_ID, "Stage Points", "每个阶段结束的归一化时间点列表", NodeDataType.LIST, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_ACTIVE_STAGE_ID, "Active Stage", "当前活动阶段索引", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_STAGE_PROGRESS_ID, "Stage Progress", "当前阶段内的进度（0-1）", NodeDataType.FLOAT, this));
        addOutputPort(new BasePort(OUTPUT_ON_STAGE_START_ID, "On Stage Start", "阶段开始时发出脉冲", NodeDataType.BOOLEAN, this));
    }
    
    @Override
    public String getDescription() {
        return "将动画时间线分割成多个阶段，输出当前阶段信息";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Float normalizedTime = (Float) inputValues.getOrDefault(INPUT_NORMALIZED_TIME_ID, 0.0f);
        Object stagePointsObj = inputValues.get(INPUT_STAGE_POINTS_ID);
        
        // 确保归一化时间在0-1范围内
        normalizedTime = Math.max(0.0f, Math.min(1.0f, normalizedTime));
        
        // 默认阶段点（如果未提供）
        List<Float> stagePoints = new ArrayList<>();
        stagePoints.add(1.0f); // 默认只有一个阶段，结束点为1.0
        
        // 如果提供了阶段点，则使用提供的值
        if (stagePointsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> inputList = (List<Object>) stagePointsObj;
            if (!inputList.isEmpty()) {
                stagePoints.clear();
                
                // 转换并验证每个阶段点
                for (Object pointObj : inputList) {
                    if (pointObj instanceof Number) {
                        float point = ((Number) pointObj).floatValue();
                        // 确保点在0-1范围内
                        point = Math.max(0.0f, Math.min(1.0f, point));
                        stagePoints.add(point);
                    }
                }
                
                // 确保列表有序（从小到大）
                stagePoints.sort(Float::compare);
                
                // 确保最后一个点是1.0
                if (stagePoints.isEmpty() || stagePoints.get(stagePoints.size() - 1) < 1.0f) {
                    stagePoints.add(1.0f);
                }
            }
        }
        
        // 计算当前活动阶段
        int activeStage = 0;
        float prevPoint = 0.0f;
        
        for (int i = 0; i < stagePoints.size(); i++) {
            float point = stagePoints.get(i);
            if (normalizedTime <= point) {
                activeStage = i;
                break;
            }
            prevPoint = point;
        }
        
        // 计算当前阶段内的进度
        float currentPoint = stagePoints.get(activeStage);
        float stageLength = currentPoint - prevPoint;
        float stageProgress = 0.0f;
        
        if (stageLength > 0) {
            stageProgress = (normalizedTime - prevPoint) / stageLength;
            // 确保进度在0-1范围内
            stageProgress = Math.max(0.0f, Math.min(1.0f, stageProgress));
        }
        
        // 检测阶段开始
        boolean onStageStart = (activeStage != lastActiveStage);
        lastActiveStage = activeStage;
        
        // 设置输出值
        outputValues.put(OUTPUT_ACTIVE_STAGE_ID, activeStage);
        outputValues.put(OUTPUT_STAGE_PROGRESS_ID, stageProgress);
        outputValues.put(OUTPUT_ON_STAGE_START_ID, onStageStart);
    }
} 