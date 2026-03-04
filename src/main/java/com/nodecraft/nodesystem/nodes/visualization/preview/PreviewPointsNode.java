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
 * Preview Points 节点: 预览点 (List<Position> 或 List<Coordinate>) 为粒子或小标记
 */
@NodeInfo(
    id = "visualization.preview.preview_points",
    displayName = "预览点",
    description = "预览点列表为粒子或小标记",
    category = "visualization.preview"
)
public class PreviewPointsNode extends BaseCustomUINode {

    @NodeProperty(displayName = "预览颜色", category = "显示", order = 1)
    private String previewColor = "#FF0000";

    @NodeProperty(displayName = "点大小", category = "显示", order = 2)
    private float pointSize = 0.2f;

    @NodeProperty(displayName = "持续时间", category = "显示", order = 3)
    private int duration = 30;

    @NodeProperty(displayName = "粒子类型", category = "显示", order = 4)
    private String particleType = "flame";

    @NodeProperty(displayName = "使用粒子", category = "显示", order = 5)
    private boolean useParticles = true;

    private UUID previewId = UUID.randomUUID();

    private static final String INPUT_POINTS_ID = "input_points";
    private static final String INPUT_COLOR_ID = "input_color";
    private static final String INPUT_POINT_SIZE_ID = "input_point_size";
    private static final String INPUT_DURATION_ID = "input_duration";
    private static final String INPUT_PARTICLE_TYPE_ID = "input_particle_type";
    private static final String INPUT_USE_PARTICLES_ID = "input_use_particles";
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_PREVIEW_ID_ID = "output_preview_id";
    private static final String OUTPUT_POINT_COUNT_ID = "output_point_count";

    private transient ImString colorBuffer = new ImString(16);
    private transient boolean colorNeedsSync = true;
    private transient ImString particleBuffer = new ImString(64);
    private transient boolean particleNeedsSync = true;

    public PreviewPointsNode() {
        super(UUID.randomUUID(), "visualization.preview.preview_points");
        addInputPort(new BasePort(INPUT_POINTS_ID, "Points", "要预览的点列表", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_COLOR_ID, "Color", "预览颜色（十六进制）", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_POINT_SIZE_ID, "Point Size", "点大小", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_DURATION_ID, "Duration", "预览持续时间（秒）", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_PARTICLE_TYPE_ID, "Particle Type", "粒子类型", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_USE_PARTICLES_ID, "Use Particles", "是否使用粒子", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", "是否成功显示预览", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_PREVIEW_ID_ID, "Preview ID", "预览实例ID", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_POINT_COUNT_ID, "Point Count", "预览的点数量", NodeDataType.INTEGER, this));
    }

    @Override
    public String getDescription() { return "预览点列表为粒子或小标记"; }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        boolean success = false;
        String previewIdStr = previewId.toString();
        int pointCount = 0;

        Object pointsObj = inputValues.get(INPUT_POINTS_ID);
        Object colorObj = inputValues.get(INPUT_COLOR_ID);
        Object pointSizeObj = inputValues.get(INPUT_POINT_SIZE_ID);
        Object durationObj = inputValues.get(INPUT_DURATION_ID);
        Object particleTypeObj = inputValues.get(INPUT_PARTICLE_TYPE_ID);
        Object useParticlesObj = inputValues.get(INPUT_USE_PARTICLES_ID);

        String color = (colorObj instanceof String) ? (String) colorObj : this.previewColor;
        float ps = (pointSizeObj instanceof Number) ? Math.max(0.05f, Math.min(2.0f, ((Number) pointSizeObj).floatValue())) : this.pointSize;
        int dur = (durationObj instanceof Number) ? Math.max(1, ((Number) durationObj).intValue()) : this.duration;
        String pt = (particleTypeObj instanceof String) ? (String) particleTypeObj : this.particleType;
        boolean up = (useParticlesObj instanceof Boolean) ? (Boolean) useParticlesObj : this.useParticles;

        List<?> pointsList = (pointsObj instanceof List && !((List<?>) pointsObj).isEmpty()) ? (List<?>) pointsObj : null;

        if (pointsList != null) {
            try {
                pointCount = pointsList.size();
                success = pointCount > 0;
                System.out.println("预览 " + pointCount + " 个点，颜色: " + color +
                    "，大小: " + ps + "，持续: " + dur + "s，粒子: " + up + " (" + pt + ")");
            } catch (Exception e) {
                System.err.println("Error creating point preview: " + e.getMessage());
            }
        }
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_PREVIEW_ID_ID, previewIdStr);
        outputValues.put(OUTPUT_POINT_COUNT_ID, pointCount);
    }

    @Override
    protected float calculateUIHeight() {
        float h = getMediumPadding();
        h += ImGui.getFrameHeight();       // 颜色输入
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // 点大小滑条
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // 持续时间滑条
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // 粒子类型输入
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // useParticles 复选框
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
                if (ImGui.inputTextWithHint("##color", "#FF0000", colorBuffer)) {
                    setPreviewColor(colorBuffer.get()); changed = true;
                }
                l.popItemWidth();
                l.popStyleVar();
                l.addVerticalSpacing(getSmallPadding());

                // 点大小滑条
                float[] ps = {pointSize};
                l.pushFramePadding(4.0f, 3.0f);
                l.setItemWidth(aw / zoom);
                if (ImGui.sliderFloat("##ps", ps, 0.05f, 2.0f, "大小: %.2f")) {
                    setPointSize(ps[0]); changed = true;
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

                // 粒子类型输入
                ensureParticleBuffer();
                l.pushFramePadding(4.0f, 3.0f);
                l.setItemWidth(aw / zoom);
                if (ImGui.inputTextWithHint("##particle", "flame", particleBuffer)) {
                    setParticleType(particleBuffer.get()); changed = true;
                }
                l.popItemWidth();
                l.popStyleVar();
                l.addVerticalSpacing(getSmallPadding());

                // 使用粒子
                ImBoolean upBool = new ImBoolean(useParticles);
                if (ImGui.checkbox("使用粒子##up", upBool)) { setUseParticles(upBool.get()); changed = true; }

                l.addVerticalSpacing(getMediumPadding());
            } catch (Exception e) {
                System.err.println("PreviewPointsNode UI渲染失败: " + e.getMessage());
            }
            return changed;
        });
    }

    private void ensureColorBuffer() {
        if (colorBuffer == null) colorBuffer = new ImString(16);
        if (colorNeedsSync) { colorBuffer.set(previewColor != null ? previewColor : "#FF0000"); colorNeedsSync = false; }
    }

    private void ensureParticleBuffer() {
        if (particleBuffer == null) particleBuffer = new ImString(64);
        if (particleNeedsSync) { particleBuffer.set(particleType != null ? particleType : "flame"); particleNeedsSync = false; }
    }

    public String getPreviewColor() { return previewColor; }
    public void setPreviewColor(String v) { if (v != null) { this.previewColor = v; colorNeedsSync = true; markDirty(); } }
    public float getPointSize() { return pointSize; }
    public void setPointSize(float v) { v = Math.max(0.05f, Math.min(2.0f, v)); if (this.pointSize != v) { this.pointSize = v; markDirty(); } }
    public int getDuration() { return duration; }
    public void setDuration(int v) { v = Math.max(1, v); if (this.duration != v) { this.duration = v; markDirty(); } }
    public String getParticleType() { return particleType; }
    public void setParticleType(String v) { if (v != null && !v.equals(this.particleType)) { this.particleType = v; particleNeedsSync = true; markDirty(); } }
    public boolean isUseParticles() { return useParticles; }
    public void setUseParticles(boolean v) { if (this.useParticles != v) { this.useParticles = v; markDirty(); } }
    public UUID getPreviewId() { return previewId; }
    public void resetPreviewId() { previewId = UUID.randomUUID(); markDirty(); }

    @Override
    public @Nullable Object getNodeState() {
        java.util.Map<String, Object> s = new java.util.HashMap<>();
        s.put("previewColor", previewColor); s.put("pointSize", pointSize);
        s.put("duration", duration); s.put("particleType", particleType);
        s.put("useParticles", useParticles); s.put("previewId", previewId.toString());
        return s;
    }

    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> m = (java.util.Map<?, ?>) state;
            if (m.get("previewColor") instanceof String) setPreviewColor((String) m.get("previewColor"));
            if (m.get("pointSize") instanceof Number) setPointSize(((Number) m.get("pointSize")).floatValue());
            if (m.get("duration") instanceof Number) setDuration(((Number) m.get("duration")).intValue());
            if (m.get("particleType") instanceof String) setParticleType((String) m.get("particleType"));
            if (m.get("useParticles") instanceof Boolean) setUseParticles((Boolean) m.get("useParticles"));
            if (m.get("previewId") instanceof String) {
                try { previewId = UUID.fromString((String) m.get("previewId")); }
                catch (IllegalArgumentException e) { resetPreviewId(); }
            }
        }
    }
}
