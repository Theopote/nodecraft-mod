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
import imgui.type.ImString;
import imgui.flag.ImGuiCol;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Preview Vectors 节点: 预览 List<Vector> 为带箭头的线
 */
@NodeInfo(
    id = "visualization.preview.preview_vectors",
    displayName = "预览向量",
    description = "预览向量列表为带箭头的线",
    category = "visualization.preview"
)
public class PreviewVectorsNode extends BaseCustomUINode {

    @NodeProperty(displayName = "线条颜色", category = "显示", order = 1)
    private String lineColor = "#00FF00";

    @NodeProperty(displayName = "线宽度", category = "显示", order = 2)
    private float lineWidth = 0.1f;

    @NodeProperty(displayName = "箭头大小", category = "显示", order = 3)
    private float arrowSize = 0.3f;

    @NodeProperty(displayName = "持续时间", category = "显示", order = 4)
    private int duration = 30;

    @NodeProperty(displayName = "显示箭头", category = "显示", order = 5)
    private boolean showArrows = true;

    private UUID previewId = UUID.randomUUID();

    private static final String INPUT_VECTORS_ID = "input_vectors";
    private static final String INPUT_START_POINTS_ID = "input_start_points";
    private static final String INPUT_COLOR_ID = "input_color";
    private static final String INPUT_LINE_WIDTH_ID = "input_line_width";
    private static final String INPUT_ARROW_SIZE_ID = "input_arrow_size";
    private static final String INPUT_DURATION_ID = "input_duration";
    private static final String INPUT_SHOW_ARROWS_ID = "input_show_arrows";
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_PREVIEW_ID_ID = "output_preview_id";
    private static final String OUTPUT_VECTOR_COUNT_ID = "output_vector_count";

    private transient ImString colorBuffer = new ImString(16);
    private transient boolean colorNeedsSync = true;

    public PreviewVectorsNode() {
        super(UUID.randomUUID(), "visualization.preview.preview_vectors");
        addInputPort(new BasePort(INPUT_VECTORS_ID, "Vectors", "要预览的向量列表", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_START_POINTS_ID, "Start Points", "向量起点列表（可选）", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_COLOR_ID, "Color", "线条颜色（十六进制）", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_LINE_WIDTH_ID, "Line Width", "线宽度", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_ARROW_SIZE_ID, "Arrow Size", "箭头大小", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_DURATION_ID, "Duration", "预览持续时间（秒）", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_SHOW_ARROWS_ID, "Show Arrows", "是否显示箭头", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", "是否成功显示预览", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_PREVIEW_ID_ID, "Preview ID", "预览实例ID", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_VECTOR_COUNT_ID, "Vector Count", "预览的向量数量", NodeDataType.INTEGER, this));
    }

    @Override
    public String getDescription() { return "预览向量列表为带箭头的线"; }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        boolean success = false;
        String previewIdStr = previewId.toString();
        int vectorCount = 0;

        Object vectorsObj = inputValues.get(INPUT_VECTORS_ID);
        Object colorObj = inputValues.get(INPUT_COLOR_ID);
        Object lineWidthObj = inputValues.get(INPUT_LINE_WIDTH_ID);
        Object arrowSizeObj = inputValues.get(INPUT_ARROW_SIZE_ID);
        Object durationObj = inputValues.get(INPUT_DURATION_ID);
        Object showArrowsObj = inputValues.get(INPUT_SHOW_ARROWS_ID);

        String color = (colorObj instanceof String) ? (String) colorObj : this.lineColor;
        float lw = (lineWidthObj instanceof Number) ? Math.max(0.01f, Math.min(1.0f, ((Number) lineWidthObj).floatValue())) : this.lineWidth;
        float as = (arrowSizeObj instanceof Number) ? Math.max(0.1f, Math.min(1.0f, ((Number) arrowSizeObj).floatValue())) : this.arrowSize;
        int dur = (durationObj instanceof Number) ? Math.max(1, ((Number) durationObj).intValue()) : this.duration;
        boolean sa = (showArrowsObj instanceof Boolean) ? (Boolean) showArrowsObj : this.showArrows;

        List<?> vectorsList = (vectorsObj instanceof List && !((List<?>) vectorsObj).isEmpty()) ? (List<?>) vectorsObj : null;

        if (vectorsList != null) {
            try {
                vectorCount = vectorsList.size();
                success = vectorCount > 0;
                System.out.println("预览 " + vectorCount + " 个向量，颜色: " + color +
                    "，线宽: " + lw + "，箭头: " + as + "，持续: " + dur + "s，显示箭头: " + sa);
            } catch (Exception e) {
                System.err.println("Error creating vector preview: " + e.getMessage());
            }
        }
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_PREVIEW_ID_ID, previewIdStr);
        outputValues.put(OUTPUT_VECTOR_COUNT_ID, vectorCount);
    }

    @Override
    protected float calculateUIHeight() {
        float h = getMediumPadding();
        h += ImGui.getFrameHeight();       // 颜色输入
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // 线宽度滑条
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // 箭头大小滑条
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // 持续时间滑条
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // showArrows 复选框
        h += getMediumPadding();
        return h;
    }

    @Override
    protected float calculateMinUIWidth() {
        return 180f + getContentMargin();
    }

    @Override
    protected boolean renderCustomUIScaled(float width, float height, float zoom) {
        return layout(zoom, l -> {
            boolean changed = false;
            try {
                float aw = width - ZoomHelper.applyZoom(getContentMargin() * 2, zoom);
                l.addVerticalSpacing(getMediumPadding());

                // 颜色输入
                ensureColorBuffer();
                l.pushFramePadding(4.0f, 3.0f);
                l.setItemWidth(aw / zoom);
                if (ImGui.inputTextWithHint("##color", "#00FF00", colorBuffer)) {
                    setLineColor(colorBuffer.get()); changed = true;
                }
                l.popItemWidth();
                l.popStyleVar();
                l.addVerticalSpacing(getSmallPadding());

                // 线宽度滑条
                float[] lw = {lineWidth};
                l.pushFramePadding(4.0f, 3.0f);
                l.setItemWidth(aw / zoom);
                if (ImGui.sliderFloat("##lw", lw, 0.01f, 1.0f, "线宽: %.2f")) {
                    setLineWidth(lw[0]); changed = true;
                }
                l.popItemWidth();
                l.popStyleVar();
                l.addVerticalSpacing(getSmallPadding());

                // 箭头大小滑条
                float[] as = {arrowSize};
                l.pushFramePadding(4.0f, 3.0f);
                l.setItemWidth(aw / zoom);
                if (ImGui.sliderFloat("##as", as, 0.1f, 1.0f, "箭头: %.2f")) {
                    setArrowSize(as[0]); changed = true;
                }
                l.popItemWidth();
                l.popStyleVar();
                l.addVerticalSpacing(getSmallPadding());

                // 持续时间滑条
                int[] dur = {duration};
                l.pushFramePadding(4.0f, 3.0f);
                l.setItemWidth(aw / zoom);
                if (ImGui.sliderInt("##dur", dur, 1, 300, "持续: %d 秒")) {
                    setDuration(dur[0]); changed = true;
                }
                l.popItemWidth();
                l.popStyleVar();
                l.addVerticalSpacing(getSmallPadding());

                // 显示箭头
                ImBoolean saBool = new ImBoolean(showArrows);
                if (ImGui.checkbox("显示箭头##sa", saBool)) { setShowArrows(saBool.get()); changed = true; }

                l.addVerticalSpacing(getMediumPadding());
            } catch (Exception e) {
                System.err.println("PreviewVectorsNode UI渲染失败: " + e.getMessage());
            }
            return changed;
        });
    }

    private void ensureColorBuffer() {
        if (colorBuffer == null) colorBuffer = new ImString(16);
        if (colorNeedsSync) { colorBuffer.set(lineColor != null ? lineColor : "#00FF00"); colorNeedsSync = false; }
    }

    public String getLineColor() { return lineColor; }
    public void setLineColor(String v) { if (v != null) { this.lineColor = v; colorNeedsSync = true; markDirty(); } }
    public float getLineWidth() { return lineWidth; }
    public void setLineWidth(float v) { v = Math.max(0.01f, Math.min(1.0f, v)); if (this.lineWidth != v) { this.lineWidth = v; markDirty(); } }
    public float getArrowSize() { return arrowSize; }
    public void setArrowSize(float v) { v = Math.max(0.1f, Math.min(1.0f, v)); if (this.arrowSize != v) { this.arrowSize = v; markDirty(); } }
    public int getDuration() { return duration; }
    public void setDuration(int v) { v = Math.max(1, v); if (this.duration != v) { this.duration = v; markDirty(); } }
    public boolean isShowArrows() { return showArrows; }
    public void setShowArrows(boolean v) { if (this.showArrows != v) { this.showArrows = v; markDirty(); } }
    public UUID getPreviewId() { return previewId; }
    public void resetPreviewId() { previewId = UUID.randomUUID(); markDirty(); }

    @Override
    public @Nullable Object getNodeState() {
        java.util.Map<String, Object> s = new java.util.HashMap<>();
        s.put("lineColor", lineColor); s.put("lineWidth", lineWidth);
        s.put("arrowSize", arrowSize); s.put("duration", duration);
        s.put("showArrows", showArrows); s.put("previewId", previewId.toString());
        return s;
    }

    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> m = (java.util.Map<?, ?>) state;
            if (m.get("lineColor") instanceof String) setLineColor((String) m.get("lineColor"));
            if (m.get("lineWidth") instanceof Number) setLineWidth(((Number) m.get("lineWidth")).floatValue());
            if (m.get("arrowSize") instanceof Number) setArrowSize(((Number) m.get("arrowSize")).floatValue());
            if (m.get("duration") instanceof Number) setDuration(((Number) m.get("duration")).intValue());
            if (m.get("showArrows") instanceof Boolean) setShowArrows((Boolean) m.get("showArrows"));
            if (m.get("previewId") instanceof String) {
                try { previewId = UUID.fromString((String) m.get("previewId")); }
                catch (IllegalArgumentException e) { resetPreviewId(); }
            }
        }
    }
}
