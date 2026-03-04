package com.nodecraft.nodesystem.nodes.visualization.debugging;

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
 * Value Monitor 节点: 在画布上悬停显示连接端口的数据值。
 * 提供复选框控制显示选项和刷新间隔滑条。
 */
@NodeInfo(
    id = "visualization.debugging.value_monitor",
    displayName = "值监视器",
    description = "在画布上悬停显示连接端口的数据值",
    category = "visualization.debugging"
)
public class ValueMonitorNode extends BaseCustomUINode {

    @NodeProperty(displayName = "显示标签", category = "显示", order = 1)
    private boolean showLabel = true;

    @NodeProperty(displayName = "显示类型", category = "显示", order = 2)
    private boolean showType = true;

    @NodeProperty(displayName = "紧凑视图", category = "显示", order = 3)
    private boolean compactView = false;

    @NodeProperty(displayName = "刷新间隔", category = "显示", order = 4)
    private int refreshInterval = 500;

    private String displayText = "";

    private static final String INPUT_VALUE_ID = "input_value";
    private static final String INPUT_LABEL_ID = "input_label";
    private static final String INPUT_SHOW_TYPE_ID = "input_show_type";
    private static final String INPUT_REFRESH_INTERVAL_ID = "input_refresh_interval";
    private static final String OUTPUT_VALUE_ID = "output_value";
    private static final String OUTPUT_DISPLAY_TEXT_ID = "output_display_text";
    private static final String OUTPUT_TYPE_ID = "output_type";

    public ValueMonitorNode() {
        super(UUID.randomUUID(), "visualization.debugging.value_monitor");
        addInputPort(new BasePort(INPUT_VALUE_ID, "Value", "要监视的值", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_LABEL_ID, "Label", "显示值的标签", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_SHOW_TYPE_ID, "Show Type", "是否显示类型信息", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_REFRESH_INTERVAL_ID, "Refresh Interval", "刷新间隔（毫秒）", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_VALUE_ID, "Value", "监视的值（直接传递）", NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_DISPLAY_TEXT_ID, "Display Text", "显示的文本", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_TYPE_ID, "Type", "监视值的类型", NodeDataType.STRING, this));
    }

    @Override
    public String getDescription() { return "在画布上悬停显示连接端口的数据值"; }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object valueObj = inputValues.get(INPUT_VALUE_ID);
        Object labelObj = inputValues.get(INPUT_LABEL_ID);
        Object showTypeObj = inputValues.get(INPUT_SHOW_TYPE_ID);
        Object refreshIntervalObj = inputValues.get(INPUT_REFRESH_INTERVAL_ID);

        boolean st = this.showType;
        if (showTypeObj instanceof Boolean) st = (Boolean) showTypeObj;
        if (refreshIntervalObj instanceof Number) refreshInterval = Math.max(100, ((Number) refreshIntervalObj).intValue());

        String label = (labelObj instanceof String) ? (String) labelObj : "";
        String typeName = getDataTypeName(valueObj);
        displayText = formatDisplayText(valueObj, label, typeName, st);

        outputValues.put(OUTPUT_VALUE_ID, valueObj);
        outputValues.put(OUTPUT_DISPLAY_TEXT_ID, displayText);
        outputValues.put(OUTPUT_TYPE_ID, typeName);
    }

    @Override
    protected float calculateUIHeight() {
        float h = getMediumPadding();
        h += ImGui.getTextLineHeight();    // 值文本
        h += ImGui.getTextLineHeight();    // 类型行
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // showLabel
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // showType
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // compactView
        h += getSmallPadding();
        h += ImGui.getFrameHeight();       // refreshInterval
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

                ImGui.pushStyleColor(ImGuiCol.Text, 0xFF88CCFF);
                String show = displayText.isEmpty() ? "(无数据)" : displayText;
                if (show.length() > 40) show = show.substring(0, 37) + "...";
                ImGui.text(show);
                ImGui.popStyleColor();

                ImGui.pushStyleColor(ImGuiCol.Text, 0xFF888888);
                ImGui.text("类型: " + getDataTypeName(inputValues.get(INPUT_VALUE_ID)));
                ImGui.popStyleColor();

                l.addVerticalSpacing(getSmallPadding());

                ImBoolean slBool = new ImBoolean(showLabel);
                if (ImGui.checkbox("显示标签##sl", slBool)) { setShowLabel(slBool.get()); changed = true; }
                l.addVerticalSpacing(getSmallPadding());

                ImBoolean stBool = new ImBoolean(showType);
                if (ImGui.checkbox("显示类型##st", stBool)) { setShowType(stBool.get()); changed = true; }
                l.addVerticalSpacing(getSmallPadding());

                ImBoolean cvBool = new ImBoolean(compactView);
                if (ImGui.checkbox("紧凑视图##cv", cvBool)) { setCompactView(cvBool.get()); changed = true; }
                l.addVerticalSpacing(getSmallPadding());

                int[] ri = {refreshInterval};
                l.pushFramePadding(4.0f, 3.0f);
                l.setItemWidth(aw / zoom);
                if (ImGui.sliderInt("##refresh", ri, 100, 5000, "%d ms")) {
                    setRefreshInterval(ri[0]); changed = true;
                }
                l.popItemWidth();
                l.popStyleVar();

                l.addVerticalSpacing(getMediumPadding());
            } catch (Exception e) {
                System.err.println("ValueMonitorNode UI渲染失败: " + e.getMessage());
            }
            return changed;
        });
    }

    private String getDataTypeName(Object obj) {
        return switch (obj) {
            case null -> "null";
            case Iterable iterable -> "List";
            case Object[] objects -> "Array";
            case String s -> "String";
            case Integer i -> "Int";
            case Float v -> "Float";
            case Double v -> "Double";
            case Boolean b -> "Boolean";
            case Number number -> "Number";
            default -> obj.getClass().getSimpleName();
        };
    }

    private String formatDisplayText(Object value, String label, String typeName, boolean showType) {
        StringBuilder sb = new StringBuilder();
        if (showLabel && !label.isEmpty()) sb.append(label).append(": ");
        if (showType) sb.append("(").append(typeName).append(") ");
        switch (value) {
            case null -> sb.append("null");
            case String str ->
                    sb.append(compactView && str.length() > 30 ? "\"" + str.substring(0, 27) + "...\"" : "\"" + str + "\"");
            case Iterable iterable -> {
                if (compactView) sb.append("[...]");
                else {
                    sb.append("[");
                    boolean first = true;
                    int count = 0;
                    for (Object item : (Iterable<?>) value) {
                        if (count >= 3) {
                            sb.append(", ...");
                            break;
                        }
                        if (!first) sb.append(", ");
                        first = false;
                        sb.append(item instanceof String ? "\"" + item + "\"" : String.valueOf(item));
                        count++;
                    }
                    sb.append("]");
                }
            }
            default -> sb.append(value);
        }
        return sb.toString();
    }

    public boolean isShowLabel() { return showLabel; }
    public void setShowLabel(boolean v) { if (this.showLabel != v) { this.showLabel = v; markDirty(); } }
    public boolean isShowType() { return showType; }
    public void setShowType(boolean v) { if (this.showType != v) { this.showType = v; markDirty(); } }
    public boolean isCompactView() { return compactView; }
    public void setCompactView(boolean v) { if (this.compactView != v) { this.compactView = v; markDirty(); } }
    public int getRefreshInterval() { return refreshInterval; }
    public void setRefreshInterval(int v) { v = Math.max(100, v); if (this.refreshInterval != v) { this.refreshInterval = v; markDirty(); } }
    public String getDisplayText() { return displayText; }

    @Override
    public @Nullable Object getNodeState() {
        java.util.Map<String, Object> s = new java.util.HashMap<>();
        s.put("showLabel", showLabel); s.put("showType", showType);
        s.put("compactView", compactView); s.put("refreshInterval", refreshInterval);
        return s;
    }

    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof java.util.Map<?, ?> m) {
            if (m.get("showLabel") instanceof Boolean) setShowLabel((Boolean) m.get("showLabel"));
            if (m.get("showType") instanceof Boolean) setShowType((Boolean) m.get("showType"));
            if (m.get("compactView") instanceof Boolean) setCompactView((Boolean) m.get("compactView"));
            if (m.get("refreshInterval") instanceof Number) setRefreshInterval(((Number) m.get("refreshInterval")).intValue());
        }
    }
}
