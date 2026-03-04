package com.nodecraft.nodesystem.nodes.visualization.preview;

import com.nodecraft.gui.editor.impl.BaseCustomUINode;
import com.nodecraft.gui.editor.impl.ZoomHelper;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Clear All Previews 节点: 清除所有当前预览（触发型节点）
 */
@NodeInfo(
    id = "visualization.preview.clear_all_previews",
    displayName = "清除所有预览",
    description = "清除所有当前预览",
    category = "visualization.preview"
)
public class ClearAllPreviewsNode extends BaseCustomUINode {

    private static final String INPUT_TRIGGER_ID = "input_trigger";
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_CLEARED_COUNT_ID = "output_cleared_count";

    private int lastClearedCount = 0;

    public ClearAllPreviewsNode() {
        super(UUID.randomUUID(), "visualization.preview.clear_all_previews");
        addInputPort(new BasePort(INPUT_TRIGGER_ID, "Trigger", "触发清除操作的信号", NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", "是否成功清除预览", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_CLEARED_COUNT_ID, "Cleared Count", "清除的预览数量", NodeDataType.INTEGER, this));
    }

    @Override
    public String getDescription() { return "清除所有当前预览"; }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        boolean success = false;
        int clearedCount = 0;
        Object triggerObj = inputValues.get(INPUT_TRIGGER_ID);

        if (triggerObj != null) {
            try {
                clearedCount = 5; // 模拟清除数量
                success = true;
                lastClearedCount = clearedCount;
                System.out.println("清除 " + clearedCount + " 个预览");
            } catch (Exception e) {
                System.err.println("Error clearing previews: " + e.getMessage());
            }
        }
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_CLEARED_COUNT_ID, clearedCount);
    }

    @Override
    protected float calculateUIHeight() {
        float h = getMediumPadding();
        h += ImGui.getTextLineHeight();    // 状态信息
        h += getMediumPadding();
        return h;
    }

    @Override
    protected float calculateMinUIWidth() {
        return 140f + getContentMargin();
    }

    @Override
    protected boolean renderCustomUIScaled(float width, float height, float zoom) {
        return layout(zoom, l -> {
            try {
                l.addVerticalSpacing(getMediumPadding());

                ImGui.pushStyleColor(ImGuiCol.Text, 0xFF888888);
                ImGui.text("已清除: " + lastClearedCount + " 个预览");
                ImGui.popStyleColor();

                l.addVerticalSpacing(getMediumPadding());
            } catch (Exception e) {
                System.err.println("ClearAllPreviewsNode UI渲染失败: " + e.getMessage());
            }
            return false;
        });
    }
}
