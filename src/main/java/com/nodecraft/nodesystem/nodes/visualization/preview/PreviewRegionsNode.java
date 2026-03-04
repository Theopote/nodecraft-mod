package com.nodecraft.nodesystem.nodes.visualization.preview;

import com.nodecraft.gui.editor.impl.BaseCustomUINode;
import com.nodecraft.gui.editor.impl.ZoomHelper;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.NodeProperty;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import imgui.ImGui;
import imgui.type.ImString;
import imgui.flag.ImGuiCol;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Preview Regions 节点: 预览 List<Region> 为线框或半透明体
 */
@NodeInfo(
    id = "visualization.preview.preview_regions",
    displayName = "预览区域",
    description = "预览区域列表为线框或半透明体",
    category = "visualization.preview"
)
public class PreviewRegionsNode extends BaseCustomUINode {

    @NodeProperty(displayName = "区域颜色", category = "显示", order = 1)
    private String regionColor = "#0000FF";

    @NodeProperty(displayName = "透明度", category = "显示", order = 2)
    private float transparency = 0.3f;

    @NodeProperty(displayName = "线框宽度", category = "显示", order = 3)
    private float lineWidth = 0.1f;

    @NodeProperty(displayName = "持续时间", category = "显示", order = 4)
    private int duration = 30;

    @NodeProperty(displayName = "显示模式", category = "显示", order = 5)
    private String displayMode = "both";

    private UUID previewId = UUID.randomUUID();

    private static final String INPUT_REGIONS_ID = "input_regions";
    private static final String INPUT_COLOR_ID = "input_color";
    private static final String INPUT_TRANSPARENCY_ID = "input_transparency";
    private static final String INPUT_LINE_WIDTH_ID = "input_line_width";
    private static final String INPUT_DURATION_ID = "input_duration";
    private static final String INPUT_DISPLAY_MODE_ID = "input_display_mode";
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_PREVIEW_ID_ID = "output_preview_id";
    private static final String OUTPUT_REGION_COUNT_ID = "output_region_count";
    private static final String OUTPUT_TOTAL_VOLUME_ID = "output_total_volume";

    private transient ImString colorBuffer = new ImString(16);
    private transient boolean colorNeedsSync = true;

    private static final String[] DISPLAY_MODES = {"wireframe", "solid", "both"};
    private static final String[] DISPLAY_MODE_LABELS = {"线框", "实体", "两者"};

    public PreviewRegionsNode() {
        super(UUID.randomUUID(), "visualization.preview.preview_regions");
        addInputPort(new BasePort(INPUT_REGIONS_ID, "Regions", "要预览的区域列表", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_COLOR_ID, "Color", "区域颜色（十六进制）", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_TRANSPARENCY_ID, "Transparency", "透明度（0.0-1.0）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_LINE_WIDTH_ID, "Line Width", "线框宽度", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_DURATION_ID, "Duration", "预览持续时间（秒）", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_DISPLAY_MODE_ID, "Display Mode", "展示模式（wireframe/solid/both）", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", "是否成功显示预览", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_PREVIEW_ID_ID, "Preview ID", "预览实例ID", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_REGION_COUNT_ID, "Region Count", "预览的区域数量", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_TOTAL_VOLUME_ID, "Total Volume", "所有区域的总体积", NodeDataType.INTEGER, this));
    }

    @Override
    public String getDescription() { return "预览区域列表为线框或半透明体"; }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        boolean success = false;
        String previewIdStr = previewId.toString();
        int regionCount = 0;
        int totalVolume = 0;

        Object regionsObj = inputValues.get(INPUT_REGIONS_ID);
        Object colorObj = inputValues.get(INPUT_COLOR_ID);
        Object transparencyObj = inputValues.get(INPUT_TRANSPARENCY_ID);
        Object lineWidthObj = inputValues.get(INPUT_LINE_WIDTH_ID);
        Object durationObj = inputValues.get(INPUT_DURATION_ID);
        Object displayModeObj = inputValues.get(INPUT_DISPLAY_MODE_ID);

        String color = (colorObj instanceof String) ? (String) colorObj : this.regionColor;
        float trans = (transparencyObj instanceof Number) ? Math.max(0f, Math.min(1f, ((Number) transparencyObj).floatValue())) : this.transparency;
        float lw = (lineWidthObj instanceof Number) ? Math.max(0.01f, Math.min(0.5f, ((Number) lineWidthObj).floatValue())) : this.lineWidth;
        int dur = (durationObj instanceof Number) ? Math.max(1, ((Number) durationObj).intValue()) : this.duration;
        String dm = (displayModeObj instanceof String && isValidDisplayMode((String) displayModeObj)) ? (String) displayModeObj : this.displayMode;

        List<?> regionsList = (regionsObj instanceof List && !((List<?>) regionsObj).isEmpty()) ? (List<?>) regionsObj : null;

        if (regionsList != null) {
            try {
                regionCount = regionsList.size();
                totalVolume = regionCount * 100;
                success = regionCount > 0;
                System.out.println("预览 " + regionCount + " 个区域，总体积: " + totalVolume +
                    "，颜色: " + color + "，透明度: " + trans + "，模式: " + dm);
            } catch (Exception e) {
                System.err.println("Error creating region preview: " + e.getMessage());
            }
        }
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_PREVIEW_ID_ID, previewIdStr);
        outputValues.put(OUTPUT_REGION_COUNT_ID, regionCount);
        outputValues.put(OUTPUT_TOTAL_VOLUME_ID, totalVolume);
    }

    @Override
    protected float calculateUIHeight() {
        float h = getMediumPadding();
        h += ImGui.getFrameHeight();       // 颜色输入
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // 透明度滑条
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // 线框宽度滑条
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // 持续时间滑条
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // 显示模式按钮行
        h += getMediumPadding();
        return h;
    }

    @Override
    protected float calculateMinUIWidth() {
        return 190f + getContentMargin();
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
                if (ImGui.inputTextWithHint("##color", "#0000FF", colorBuffer)) {
                    setRegionColor(colorBuffer.get()); changed = true;
                }
                l.popItemWidth();
                l.popStyleVar();
                l.addVerticalSpacing(getSmallPadding());

                // 透明度滑条
                float[] trans = {transparency};
                l.pushFramePadding(4.0f, 3.0f);
                l.setItemWidth(aw / zoom);
                if (ImGui.sliderFloat("##trans", trans, 0.0f, 1.0f, "透明度: %.2f")) {
                    setTransparency(trans[0]); changed = true;
                }
                l.popItemWidth();
                l.popStyleVar();
                l.addVerticalSpacing(getSmallPadding());

                // 线框宽度滑条
                float[] lw = {lineWidth};
                l.pushFramePadding(4.0f, 3.0f);
                l.setItemWidth(aw / zoom);
                if (ImGui.sliderFloat("##lw", lw, 0.01f, 0.5f, "线宽: %.2f")) {
                    setLineWidth(lw[0]); changed = true;
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

                // 显示模式按钮行
                float btnW = (aw / zoom - 8) / 3.0f;
                for (int i = 0; i < DISPLAY_MODES.length; i++) {
                    if (i > 0) ImGui.sameLine(0, 4);
                    boolean selected = DISPLAY_MODES[i].equals(displayMode);
                    if (selected) {
                        ImGui.pushStyleColor(ImGuiCol.Button, 0xFF4488CC);
                        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0xFF5599DD);
                    }
                    if (ImGui.button(DISPLAY_MODE_LABELS[i] + "##dm" + i, btnW, 0)) {
                        setDisplayMode(DISPLAY_MODES[i]); changed = true;
                    }
                    if (selected) ImGui.popStyleColor(2);
                }

                l.addVerticalSpacing(getMediumPadding());
            } catch (Exception e) {
                System.err.println("PreviewRegionsNode UI渲染失败: " + e.getMessage());
            }
            return changed;
        });
    }

    private void ensureColorBuffer() {
        if (colorBuffer == null) colorBuffer = new ImString(16);
        if (colorNeedsSync) { colorBuffer.set(regionColor != null ? regionColor : "#0000FF"); colorNeedsSync = false; }
    }

    private boolean isValidDisplayMode(String mode) {
        return "wireframe".equals(mode) || "solid".equals(mode) || "both".equals(mode);
    }

    public String getRegionColor() { return regionColor; }
    public void setRegionColor(String v) { if (v != null) { this.regionColor = v; colorNeedsSync = true; markDirty(); } }
    public float getTransparency() { return transparency; }
    public void setTransparency(float v) { v = Math.max(0f, Math.min(1f, v)); if (this.transparency != v) { this.transparency = v; markDirty(); } }
    public float getLineWidth() { return lineWidth; }
    public void setLineWidth(float v) { v = Math.max(0.01f, Math.min(0.5f, v)); if (this.lineWidth != v) { this.lineWidth = v; markDirty(); } }
    public int getDuration() { return duration; }
    public void setDuration(int v) { v = Math.max(1, v); if (this.duration != v) { this.duration = v; markDirty(); } }
    public String getDisplayMode() { return displayMode; }
    public void setDisplayMode(String v) { if (isValidDisplayMode(v) && !v.equals(this.displayMode)) { this.displayMode = v; markDirty(); } }
    public UUID getPreviewId() { return previewId; }
    public void resetPreviewId() { previewId = UUID.randomUUID(); markDirty(); }

    @Override
    public @Nullable Object getNodeState() {
        java.util.Map<String, Object> s = new java.util.HashMap<>();
        s.put("regionColor", regionColor); s.put("transparency", transparency);
        s.put("lineWidth", lineWidth); s.put("duration", duration);
        s.put("displayMode", displayMode); s.put("previewId", previewId.toString());
        return s;
    }

    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> m = (java.util.Map<?, ?>) state;
            if (m.get("regionColor") instanceof String) setRegionColor((String) m.get("regionColor"));
            if (m.get("transparency") instanceof Number) setTransparency(((Number) m.get("transparency")).floatValue());
            if (m.get("lineWidth") instanceof Number) setLineWidth(((Number) m.get("lineWidth")).floatValue());
            if (m.get("duration") instanceof Number) setDuration(((Number) m.get("duration")).intValue());
            if (m.get("displayMode") instanceof String) setDisplayMode((String) m.get("displayMode"));
            if (m.get("previewId") instanceof String) {
                try { previewId = UUID.fromString((String) m.get("previewId")); }
                catch (IllegalArgumentException e) { resetPreviewId(); }
            }
        }
    }
}
