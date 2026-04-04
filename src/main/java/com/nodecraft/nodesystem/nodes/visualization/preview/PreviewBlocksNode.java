package com.nodecraft.nodesystem.nodes.visualization.preview;

import com.nodecraft.gui.editor.impl.BaseCustomUINode;
import com.nodecraft.gui.editor.impl.ZoomHelper;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.NodeProperty;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.preview.PreviewManager;
import com.nodecraft.nodesystem.preview.PreviewOptions;
import com.nodecraft.nodesystem.preview.elements.GhostBlockElement;
import com.nodecraft.nodesystem.util.BlockPosList;
import com.nodecraft.nodesystem.util.Coordinate;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import imgui.flag.ImGuiCol;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Preview Blocks 节点: 在世界中将输入的 List<Coordinate> 或 List<MinecraftBlock> 预览为"幽灵方块"
 */
@NodeInfo(
    id = "visualization.preview.preview_blocks",
    displayName = "预览方块",
    description = "在世界中将输入的方块数据预览为幽灵方块",
    category = "visualization.preview"
)
public class PreviewBlocksNode extends BaseCustomUINode {

    @NodeProperty(displayName = "预览颜色", category = "显示", order = 1)
    private String previewColor = "#3498db";

    @NodeProperty(displayName = "透明度", category = "显示", order = 2)
    private float transparency = 0.5f;

    @NodeProperty(displayName = "持续时间", category = "显示", order = 3)
    private int duration = 30;

    @NodeProperty(displayName = "显示轮廓", category = "显示", order = 4)
    private boolean showOutline = true;

    private UUID previewId = UUID.randomUUID();

    private static final String INPUT_BLOCKS_ID = "input_blocks";
    private static final String INPUT_COORDS_ID = "input_coords";
    private static final String INPUT_BLOCK_TYPE_ID = "input_block_type";
    private static final String INPUT_COLOR_ID = "input_color";
    private static final String INPUT_TRANSPARENCY_ID = "input_transparency";
    private static final String INPUT_DURATION_ID = "input_duration";
    private static final String INPUT_SHOW_OUTLINE_ID = "input_show_outline";
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_PREVIEW_ID_ID = "output_preview_id";
    private static final String OUTPUT_BLOCK_COUNT_ID = "output_block_count";

    private transient ImString colorBuffer = new ImString(16);
    private transient boolean colorNeedsSync = true;

    public PreviewBlocksNode() {
        super(UUID.randomUUID(), "visualization.preview.preview_blocks");
        addInputPort(new BasePort(INPUT_BLOCKS_ID, "Blocks", "要预览的方块列表", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_COORDS_ID, "Coordinates", "要预览方块的坐标列表", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_BLOCK_TYPE_ID, "Block Type", "方块类型（默认minecraft:stone）", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_COLOR_ID, "Color", "预览颜色（十六进制）", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_TRANSPARENCY_ID, "Transparency", "透明度（0.0-1.0）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_DURATION_ID, "Duration", "预览持续时间（秒）", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_SHOW_OUTLINE_ID, "Show Outline", "是否显示方块轮廓", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", "是否成功显示预览", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_PREVIEW_ID_ID, "Preview ID", "预览实例ID", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_BLOCK_COUNT_ID, "Block Count", "预览的方块数量", NodeDataType.INTEGER, this));
    }

    @Override
    public String getDescription() { return "在世界中将输入的方块数据预览为幽灵方块"; }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        boolean success = false;
        String previewIdStr = previewId.toString();
        int blockCount = 0;

        Object blocksObj = inputValues.get(INPUT_BLOCKS_ID);
        Object coordsObj = inputValues.get(INPUT_COORDS_ID);
        Object blockTypeObj = inputValues.get(INPUT_BLOCK_TYPE_ID);
        Object colorObj = inputValues.get(INPUT_COLOR_ID);
        Object transparencyObj = inputValues.get(INPUT_TRANSPARENCY_ID);
        Object durationObj = inputValues.get(INPUT_DURATION_ID);
        Object showOutlineObj = inputValues.get(INPUT_SHOW_OUTLINE_ID);

        String color = (colorObj instanceof String) ? (String) colorObj : this.previewColor;
        float trans = (transparencyObj instanceof Number) ? Math.max(0f, Math.min(1f, ((Number) transparencyObj).floatValue())) : this.transparency;
        int dur = (durationObj instanceof Number) ? Math.max(1, ((Number) durationObj).intValue()) : this.duration;
        boolean outline = (showOutlineObj instanceof Boolean) ? (Boolean) showOutlineObj : this.showOutline;
        String defaultBlockType = (blockTypeObj instanceof String) ? (String) blockTypeObj : "minecraft:stone";

        Iterable<?> blocksList = null;
        if (blocksObj instanceof BlockPosList blockPosList && !blockPosList.isEmpty()) {
            blocksList = blockPosList;
        } else if (blocksObj instanceof List<?> list && !list.isEmpty()) {
            blocksList = list;
        }
        List<?> coordsList = (coordsObj instanceof List && !((List<?>) coordsObj).isEmpty()) ? (List<?>) coordsObj : null;

        if (blocksList != null || coordsList != null) {
            try {
                PreviewManager.hideNodePreviews(getId().toString());
                List<GhostBlockElement.BlockPlacement> placements = new ArrayList<>();
                if (blocksList != null) {
                    for (Object obj : blocksList) {
                        GhostBlockElement.BlockPlacement placement = toPlacement(obj, defaultBlockType, trans);
                        if (placement != null) {
                            placements.add(placement);
                            blockCount++;
                        }
                    }
                }
                if (coordsList != null) {
                    for (Object obj : coordsList) {
                        GhostBlockElement.BlockPlacement placement = toPlacement(obj, defaultBlockType, trans);
                        if (placement != null) {
                            placements.add(placement);
                            blockCount++;
                        }
                    }
                }
                if (!placements.isEmpty()) {
                    PreviewOptions options = new PreviewOptions().ghostBlockMode().setOpacity(trans).setDuration(dur);
                    String newPreviewId = PreviewManager.showGhostBlockPlacements(getId().toString(), placements, options);
                    if (newPreviewId != null) success = true;
                }
                if (blockCount > 0) success = true;
            } catch (Exception e) {
                System.err.println("Error creating block preview: " + e.getMessage());
            }
        }
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_PREVIEW_ID_ID, previewIdStr);
        outputValues.put(OUTPUT_BLOCK_COUNT_ID, blockCount);
    }

    private GhostBlockElement.BlockPlacement toPlacement(Object obj, String blockType, float opacity) {
        if (obj instanceof Coordinate coord) {
            return new GhostBlockElement.BlockPlacement(new Vec3d(coord.getX(), coord.getY(), coord.getZ()), blockType, opacity);
        }
        if (obj instanceof BlockPos pos) {
            return new GhostBlockElement.BlockPlacement(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), blockType, opacity);
        }
        return null;
    }

    @Override
    protected float calculateUIHeight() {
        float h = getMediumPadding();
        h += ImGui.getFrameHeight();       // 颜色输入
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // 透明度滑条
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // 持续时间滑条
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // showOutline 复选框
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
                float aw = l.getAvailableContentWidth(width);
                l.addVerticalSpacing(getMediumPadding());

                // 颜色输入
                ensureColorBuffer();
                l.pushFramePadding(4.0f, 3.0f);
                l.setItemWidth(aw / zoom);
                if (ImGui.inputTextWithHint("##color", "#3498db", colorBuffer)) {
                    setPreviewColor(colorBuffer.get()); changed = true;
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

                // 显示轮廓
                ImBoolean olBool = new ImBoolean(showOutline);
                if (ImGui.checkbox("显示轮廓##ol", olBool)) { setShowOutline(olBool.get()); changed = true; }

                l.addVerticalSpacing(getMediumPadding());
            } catch (Exception e) {
                System.err.println("PreviewBlocksNode UI渲染失败: " + e.getMessage());
            }
            return changed;
        });
    }

    private void ensureColorBuffer() {
        if (colorBuffer == null) colorBuffer = new ImString(16);
        if (colorNeedsSync) { colorBuffer.set(previewColor != null ? previewColor : "#3498db"); colorNeedsSync = false; }
    }

    public String getPreviewColor() { return previewColor; }
    public void setPreviewColor(String v) { if (v != null) { this.previewColor = v; colorNeedsSync = true; markDirty(); } }
    public float getTransparency() { return transparency; }
    public void setTransparency(float v) { v = Math.max(0f, Math.min(1f, v)); if (this.transparency != v) { this.transparency = v; markDirty(); } }
    public int getDuration() { return duration; }
    public void setDuration(int v) { v = Math.max(1, v); if (this.duration != v) { this.duration = v; markDirty(); } }
    public boolean isShowOutline() { return showOutline; }
    public void setShowOutline(boolean v) { if (this.showOutline != v) { this.showOutline = v; markDirty(); } }
    public UUID getPreviewId() { return previewId; }
    public void resetPreviewId() { previewId = UUID.randomUUID(); markDirty(); }

    @Override
    public @Nullable Object getNodeState() {
        java.util.Map<String, Object> s = new java.util.HashMap<>();
        s.put("previewColor", previewColor); s.put("transparency", transparency);
        s.put("duration", duration); s.put("showOutline", showOutline);
        s.put("previewId", previewId.toString());
        return s;
    }

    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> m = (java.util.Map<?, ?>) state;
            if (m.get("previewColor") instanceof String) setPreviewColor((String) m.get("previewColor"));
            if (m.get("transparency") instanceof Number) setTransparency(((Number) m.get("transparency")).floatValue());
            if (m.get("duration") instanceof Number) setDuration(((Number) m.get("duration")).intValue());
            if (m.get("showOutline") instanceof Boolean) setShowOutline((Boolean) m.get("showOutline"));
            if (m.get("previewId") instanceof String) {
                try { previewId = UUID.fromString((String) m.get("previewId")); }
                catch (IllegalArgumentException e) { resetPreviewId(); }
            }
        }
    }
}
