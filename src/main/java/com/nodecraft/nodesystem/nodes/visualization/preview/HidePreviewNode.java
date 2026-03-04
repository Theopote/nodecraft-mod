package com.nodecraft.nodesystem.nodes.visualization.preview;

import com.nodecraft.gui.editor.impl.BaseCustomUINode;
import com.nodecraft.gui.editor.impl.ZoomHelper;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.NodeProperty;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.flag.ImGuiCol;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Hide Preview 节点: 隐藏/显示指定ID的预览
 */
@NodeInfo(
    id = "visualization.preview.hide_preview",
    displayName = "隐藏预览",
    description = "隐藏当前连接到此节点的预览",
    category = "visualization.preview"
)
public class HidePreviewNode extends BaseCustomUINode {

    @NodeProperty(displayName = "隐藏状态", category = "控制", order = 1)
    private boolean isHidden = false;

    private static final String INPUT_PREVIEW_ID_ID = "input_preview_id";
    private static final String INPUT_HIDE_ID = "input_hide";
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_IS_HIDDEN_ID = "output_is_hidden";

    public HidePreviewNode() {
        super(UUID.randomUUID(), "visualization.preview.hide_preview");
        addInputPort(new BasePort(INPUT_PREVIEW_ID_ID, "Preview ID", "要控制的预览ID", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_HIDE_ID, "Hide", "是否隐藏预览", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", "操作是否成功", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_IS_HIDDEN_ID, "Is Hidden", "预览是否隐藏", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDescription() { return "隐藏当前连接到此节点的预览"; }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        boolean success = false;
        Object previewIdObj = inputValues.get(INPUT_PREVIEW_ID_ID);
        Object hideObj = inputValues.get(INPUT_HIDE_ID);

        boolean shouldHide = (hideObj instanceof Boolean) ? (Boolean) hideObj : this.isHidden;
        String previewId = (previewIdObj instanceof String) ? (String) previewIdObj : null;

        if (previewId != null) {
            try {
                this.isHidden = shouldHide;
                success = true;
                System.out.println((isHidden ? "隐藏" : "显示") + "预览，ID: " + previewId);
            } catch (Exception e) {
                System.err.println("Error hiding/showing preview: " + e.getMessage());
            }
        }
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_IS_HIDDEN_ID, isHidden);
    }

    @Override
    protected float calculateUIHeight() {
        float h = getMediumPadding();
        h += ImGui.getTextLineHeight();    // 状态标签
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // 隐藏复选框
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
            boolean changed = false;
            try {
                l.addVerticalSpacing(getMediumPadding());

                // 状态标签
                if (isHidden) {
                    ImGui.pushStyleColor(ImGuiCol.Text, 0xFF4444AA);
                    ImGui.text("状态: 已隐藏");
                } else {
                    ImGui.pushStyleColor(ImGuiCol.Text, 0xFF44AA44);
                    ImGui.text("状态: 可见");
                }
                ImGui.popStyleColor();
                l.addVerticalSpacing(getSmallPadding());

                // 隐藏复选框
                ImBoolean hideBool = new ImBoolean(isHidden);
                if (ImGui.checkbox("隐藏##hide", hideBool)) { setHidden(hideBool.get()); changed = true; }

                l.addVerticalSpacing(getMediumPadding());
            } catch (Exception e) {
                System.err.println("HidePreviewNode UI渲染失败: " + e.getMessage());
            }
            return changed;
        });
    }

    public boolean isHidden() { return isHidden; }
    public void setHidden(boolean v) { if (this.isHidden != v) { this.isHidden = v; markDirty(); } }

    @Override
    public @Nullable Object getNodeState() {
        java.util.Map<String, Object> s = new java.util.HashMap<>();
        s.put("isHidden", isHidden);
        return s;
    }

    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> m = (java.util.Map<?, ?>) state;
            if (m.get("isHidden") instanceof Boolean) setHidden((Boolean) m.get("isHidden"));
        } else if (state instanceof Boolean) {
            setHidden((Boolean) state); // 向后兼容旧状态格式
        }
    }
}
