package com.nodecraft.nodesystem.nodes.visualization.preview;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Clear All Previews 节点: 清除所有当前预览
 */
@NodeInfo(
    id = "visualization.preview.clear_all_previews",
    displayName = "清除所有预览",
    description = "清除所有当前预览",
    category = "visualization.preview"
)
public class ClearAllPreviewsNode extends BaseNode {

    // --- 节点属性 ---
    private String description = "清除所有当前预览";

    // --- 输入端口 IDs ---
    private static final String INPUT_TRIGGER_ID = "input_trigger";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_CLEARED_COUNT_ID = "output_cleared_count";

    // --- 构造函数 ---
    public ClearAllPreviewsNode() {
        super(UUID.randomUUID(), "visualization.preview.clear_all_previews");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_TRIGGER_ID, "Trigger", 
                "触发清除操作的信号", NodeDataType.ANY, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功清除预览", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_CLEARED_COUNT_ID, "Cleared Count", 
                "清除的预览数量", NodeDataType.INTEGER, this));
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 默认输出值
        boolean success = false;
        int clearedCount = 0;
        
        // 获取输入值
        Object triggerObj = inputValues.get(INPUT_TRIGGER_ID);
        
        // 只要有任何输入值，就触发清除
        if (triggerObj != null) {
            try {
                /*
                在实际实现中，需要根据Minecraft API实现以下操作:
                1. 清除所有预览
                
                // 清除所有预览
                // clearedCount = PreviewManager.clearAllPreviews();
                // success = true;
                */
                
                // 模拟清除预览 (在实际实现中替换为上面的逻辑)
                clearedCount = 5; // 假设清除了5个预览
                success = true;
                
                // 打印调试信息
                System.out.println("模拟清除 " + clearedCount + " 个预览");
            } catch (Exception e) {
                success = false;
                System.err.println("Error clearing previews: " + e.getMessage());
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_CLEARED_COUNT_ID, clearedCount);
    }
} 