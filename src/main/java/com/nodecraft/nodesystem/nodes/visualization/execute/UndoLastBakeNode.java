package com.nodecraft.nodesystem.nodes.visualization.execute;

import com.nodecraft.gui.editor.impl.BaseCustomUINode;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.bake.BakePlacementService;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 撤销上次 Bake 节点：恢复最近一次通过 Bake 放置的方块。
 * 依赖 BakePlacementService 的撤销堆栈，需在放置时启用「记录撤销」才可撤销。
 */
@NodeInfo(
    id = "visualization.execute.undo_last_bake",
    displayName = "撤销上次 Bake",
    description = "恢复最近一次 Bake 放置的方块，需在放置时启用记录撤销",
    category = "visualization.execute"
)
public class UndoLastBakeNode extends BaseCustomUINode {

    private static final String INPUT_TRIGGER_ID = "input_trigger";
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_MESSAGE_ID = "output_message";

    private String statusMessage = "就绪";
    private boolean lastSuccess = false;

    public UndoLastBakeNode() {
        super(UUID.randomUUID(), "visualization.execute.undo_last_bake");
        addInputPort(new BasePort(INPUT_TRIGGER_ID, "Trigger", "触发撤销", NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", "是否成功撤销", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_MESSAGE_ID, "Message", "状态信息", NodeDataType.STRING, this));
    }

    @Override
    public String getDescription() {
        return "恢复最近一次 Bake 放置的方块，需在放置时启用记录撤销";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        lastSuccess = false;
        outputValues.put(OUTPUT_SUCCESS_ID, false);
        outputValues.put(OUTPUT_MESSAGE_ID, "未执行");

        Object trigger = inputValues.get(INPUT_TRIGGER_ID);
        if (trigger == null || context == null || context.getWorld() == null) {
            statusMessage = "需要触发信号与执行上下文";
            outputValues.put(OUTPUT_MESSAGE_ID, statusMessage);
            return;
        }

        BakePlacementService service = BakePlacementService.getInstance();
        if (!service.getHistory().hasUndo()) {
            statusMessage = "无可用撤销记录";
            outputValues.put(OUTPUT_MESSAGE_ID, statusMessage);
            return;
        }

        lastSuccess = service.undoLast(context.getWorld());
        statusMessage = lastSuccess ? "已撤销上次 Bake" : "撤销失败";
        outputValues.put(OUTPUT_SUCCESS_ID, lastSuccess);
        outputValues.put(OUTPUT_MESSAGE_ID, statusMessage);
    }

    @Override
    protected float calculateUIHeight() {
        float h = 2 * getMediumPadding();
        h += ImGui.getTextLineHeight();
        h += getSmallPadding();
        h += ImGui.getTextLineHeight();
        return h;
    }

    @Override
    protected float calculateMinUIWidth() {
        return 180f + getContentMargin();
    }

    @Override
    protected boolean renderCustomUIScaled(float width, float height, float zoom) {
        return layout(zoom, l -> {
            l.addVerticalSpacing(getMediumPadding());
            int color = lastSuccess ? 0xFF44DD44 : 0xFF888888;
            ImGui.pushStyleColor(ImGuiCol.Text, color);
            ImGui.text(statusMessage);
            ImGui.popStyleColor();
            l.addVerticalSpacing(getSmallPadding());
            boolean hasUndo = BakePlacementService.getInstance().getHistory().hasUndo();
            ImGui.text("可撤销: " + (hasUndo ? "是" : "否"));
            l.addVerticalSpacing(getMediumPadding());
            return false;
        });
    }
}
