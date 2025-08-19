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
 * Sequence Driver: 序列驱动器节点
 * 根据时间因子，按顺序激活列表中的不同项
 * 用于创建序列动画效果（如方块依次出现）
 */
@NodeInfo(
    id = "animation.time.sequence_driver",
    displayName = "Sequence Driver",
    description = "根据时间因子，按顺序激活列表中的不同项",
    category = "animation.time"
)
public class SequenceDriverNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_TIME_ID = "input_time";
    private static final String INPUT_STEP_DURATION_ID = "input_step_duration";
    private static final String INPUT_OVERLAP_ID = "input_overlap";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_CURRENT_ITEM_ID = "output_current_item";
    private static final String OUTPUT_CURRENT_INDEX_ID = "output_current_index";
    private static final String OUTPUT_ACTIVE_ITEMS_ID = "output_active_items";
    private static final String OUTPUT_IS_ACTIVE_ID = "output_is_active";
    private static final String OUTPUT_PROGRESS_ID = "output_progress";

    // --- 构造函数 ---
    public SequenceDriverNode() {
        super(UUID.randomUUID(), "animation.time.sequence_driver");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_LIST_ID, "List", "序列项列表", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_TIME_ID, "Time", "当前时间（0-1）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_STEP_DURATION_ID, "Step Duration", "每个步骤的持续时间（0-1之间）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_OVERLAP_ID, "Overlap", "步骤重叠因子（0=无重叠，1=完全重叠）", NodeDataType.FLOAT, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_CURRENT_ITEM_ID, "Current Item", "当前活动项", NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_CURRENT_INDEX_ID, "Current Index", "当前活动项索引", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_ACTIVE_ITEMS_ID, "Active Items", "所有当前活动项的列表", NodeDataType.LIST, this));
        addOutputPort(new BasePort(OUTPUT_IS_ACTIVE_ID, "Is Active", "序列是否处于活动状态", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_PROGRESS_ID, "Progress", "当前项的进度（0-1）", NodeDataType.FLOAT, this));
    }
    
    @Override
    public String getDescription() {
        return "根据时间因子，按顺序激活列表中的不同项";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        @SuppressWarnings("unchecked")
        List<Object> itemList = (List<Object>) inputValues.get(INPUT_LIST_ID);
        Float time = (Float) inputValues.getOrDefault(INPUT_TIME_ID, 0.0f);
        Float stepDuration = (Float) inputValues.getOrDefault(INPUT_STEP_DURATION_ID, 0.2f);
        Float overlap = (Float) inputValues.getOrDefault(INPUT_OVERLAP_ID, 0.0f);
        
        // 限制输入值范围
        time = Math.max(0f, Math.min(1f, time));
        stepDuration = Math.max(0.01f, Math.min(1f, stepDuration));
        overlap = Math.max(0f, Math.min(1f, overlap));
        
        // 默认输出
        Object currentItem = null;
        int currentIndex = -1;
        List<Object> activeItems = new ArrayList<>();
        boolean isActive = false;
        float progress = 0.0f;
        
        // 如果列表为空，则输出默认值
        if (itemList == null || itemList.isEmpty()) {
            outputValues.put(OUTPUT_CURRENT_ITEM_ID, currentItem);
            outputValues.put(OUTPUT_CURRENT_INDEX_ID, currentIndex);
            outputValues.put(OUTPUT_ACTIVE_ITEMS_ID, activeItems);
            outputValues.put(OUTPUT_IS_ACTIVE_ID, isActive);
            outputValues.put(OUTPUT_PROGRESS_ID, progress);
            return;
        }
        
        // 计算每个项目在时间线上的位置
        int itemCount = itemList.size();
        
        // 如果stepDuration很小，则进行调整以确保所有项都能在时间轴上显示
        if (stepDuration * itemCount < 1.0f) {
            stepDuration = 1.0f / itemCount;
        }
        
        // 基于重叠计算有效步骤持续时间
        float effectiveStepDuration = stepDuration * (1.0f + overlap);
        
        // 计算当前活动的项
        for (int i = 0; i < itemCount; i++) {
            // 计算此项的开始和结束时间
            float startTime = i * stepDuration;
            float endTime = startTime + effectiveStepDuration;
            
            // 检查项目是否处于活动状态
            if (time >= startTime && time <= endTime) {
                Object item = itemList.get(i);
                activeItems.add(item);
                
                // 更新当前项（使用最后一个活动项）
                currentItem = item;
                currentIndex = i;
                isActive = true;
                
                // 计算此项的进度
                progress = Math.min(1.0f, (time - startTime) / effectiveStepDuration);
            }
        }
        
        // 如果没有任何活动项，但时间大于0，则使用最后一项
        if (activeItems.isEmpty() && time > 0) {
            currentItem = itemList.get(itemList.size() - 1);
            currentIndex = itemList.size() - 1;
            activeItems.add(currentItem);
            isActive = time < 1.0f; // 只有在时间未结束时才活动
            progress = 1.0f;
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_CURRENT_ITEM_ID, currentItem);
        outputValues.put(OUTPUT_CURRENT_INDEX_ID, currentIndex);
        outputValues.put(OUTPUT_ACTIVE_ITEMS_ID, activeItems);
        outputValues.put(OUTPUT_IS_ACTIVE_ID, isActive);
        outputValues.put(OUTPUT_PROGRESS_ID, progress);
    }
} 