package com.nodecraft.nodesystem.nodes.inputs.basic;

import com.nodecraft.gui.editor.impl.BaseCustomUINode;
import com.nodecraft.gui.editor.impl.ZoomHelper;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeProperty;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@NodeInfo(
    id = "inputs.basic.integer_slider",
    displayName = "整数滑动条",
    description = "输出一个可通过滑动条调节的整数值",
    category = "inputs.basic"
)
public class IntegerSliderNode extends BaseCustomUINode {
    // Node properties
    @NodeProperty(displayName = "Value", category = "Number", order = 1)
    private int value = 50;
    @NodeProperty(displayName = "Min", category = "Range", order = 2)
    private int min = 0;
    @NodeProperty(displayName = "Max", category = "Range", order = 13) // Updated order to make it fit with existing code, was 3
    private int max = 100;
    @NodeProperty(displayName = "Step", category = "Range", order = 14) // Updated order, was 4
    private int step = 1;
    @NodeProperty(displayName = "Compact Mode", category = "UI", order = 11)
    private boolean compact = false;

    // UI state
    private final int[] sliderValue = {50};

    private static final String OUTPUT_ID = "value";

    public IntegerSliderNode() {
        super(UUID.randomUUID(), "inputs.basic.integer_slider");
        addOutputPort(new BasePort(OUTPUT_ID, "Value", "Selected integer", NodeDataType.INTEGER, this));
        sliderValue[0] = value;
        updateOutput();
    }

    @Override
    public String getDescription() {
        return "A high-performance integer slider for selecting numeric values.";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        updateOutput();
    }

    // === BaseCustomUINode 抽象方法实现 ===

    @Override
    protected float calculateUIHeight() {
        // Simple height calculation (unscaled logical units)
        // This height determines the *unscaled* base height for the node's UI
        // The actual scaled height will be calculateUIHeight() * zoom externally.
        // Needs to account for padding and content.
        float baseHeight = compact ? 20f : 40f; // Base height of the slider row
        if (!compact) {
            // Add space for the info text below the slider if not compact
            // Roughly add 2 * small padding + text line height (approx. 14-16 for default font)
            baseHeight += getSmallPadding() * 2 + ImGui.getFontSize(); // Use ImGui.getFontSize() for a more accurate scaled line height
        }
        return baseHeight + getMediumPadding() * 2; // Add top and bottom medium padding
    }

    @Override
    protected float calculateMinUIWidth() {
        return 180f; // 确保有足够的宽度容纳滑动条和两侧按钮 (unscaled logical units)
    }

    @Override
    protected boolean renderCustomUIScaled(float width, float height, float zoom) {
        return layout(zoom, l -> {
            boolean changed = false;
            boolean hasUIInteraction = false; // 跟踪是否有UI交互

            try {
                // 计算可用宽度
                float availableWidth = getAvailableWidth(width, zoom);

                // 添加顶部间距
                l.addVerticalSpacing(getMediumPadding());

                // 设置样式
                float padding = compact ? 2f : 4f;
                l.pushFramePadding(padding, padding);

                // 计算按钮尺寸
                float buttonHeight = ImGui.getFrameHeight();
                float buttonWidth = buttonHeight;

                // 渲染左侧减号按钮
                ImGui.pushID("minus_btn");
                if (ImGui.button("-", buttonWidth, buttonHeight)) {
                    setValue(value - step);
                    changed = true;
                }
                hasUIInteraction |= ImGui.isItemHovered() || ImGui.isItemActive();
                ImGui.popID();

                ImGui.sameLine();

                // 计算滑动条宽度
                float spacingX = ImGui.getStyle().getItemSpacingX();
                float sliderWidth = availableWidth - (buttonWidth * 2 + spacingX * 2);

                // 设置滑动条宽度
                l.setItemWidth(sliderWidth / zoom);

                // 渲染滑动条
                sliderValue[0] = value;
                if (ImGui.sliderInt("##slider", sliderValue, min, max)) {
                    setValue(sliderValue[0]);
                    changed = true;
                }
                hasUIInteraction |= ImGui.isItemHovered() || ImGui.isItemActive();
                l.popItemWidth();
                ImGui.sameLine();

                // 渲染右侧加号按钮
                ImGui.pushID("plus_btn");
                if (ImGui.button("+", buttonWidth, buttonHeight)) {
                    setValue(value + step);
                    changed = true;
                }
                hasUIInteraction |= ImGui.isItemHovered() || ImGui.isItemActive();
                ImGui.popID();

                // 如果不是紧凑模式，渲染设置信息
                if (!compact) {
                    l.addVerticalSpacing(getSmallPadding());
                    String infoText = "Min: " + min + " Max: " + max + " Step: " + step;
                    float textWidth = ImGui.calcTextSize(infoText).x;
                    setCenterX(availableWidth, textWidth);

                    ImGui.pushStyleColor(ImGuiCol.Text, 0.7f, 0.7f, 0.7f, 1.0f);
                    ImGui.text(infoText);
                    ImGui.popStyleColor();
                }

                // 恢复样式
                l.popStyleVar();

                // 底部间距
                l.addVerticalSpacing(getMediumPadding());

            } catch (Exception e) {
                System.err.println("IntegerSliderNode UI渲染失败: " + e.getMessage());
            }

            // 如果有UI交互，阻止事件传播到底层画布和游戏世界
            return hasUIInteraction || changed;
        });
    }

    public void setValue(int newValue) {
        newValue = Math.max(min, Math.min(max, newValue));
        if (value != newValue) {
            value = newValue;
            sliderValue[0] = newValue;
            updateOutput();
        }
    }

    private void updateOutput() {
        outputValues.put(OUTPUT_ID, value);
    }

    // === Property Getters and Setters ===

    public int getValue() {
        return value;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        // Invalidate cache if properties affecting UI size are changed
        if (this.min != min) {
            this.min = min;
            if (value < min) {
                setValue(min);
            }
            invalidateCache(); // Min/Max changes might affect calculated width/height for display
        }
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        // Invalidate cache if properties affecting UI size are changed
        if (this.max != max) {
            this.max = max;
            if (value > max) {
                setValue(max);
            }
            invalidateCache(); // Min/Max changes might affect calculated width/height for display
        }
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        // Invalidate cache if properties affecting UI size are changed
        if (this.step != step) {
            this.step = Math.max(1, step); // 确保步长至少为1
            invalidateCache(); // Step changes might affect calculated width/height for display (unlikely for this node)
        }
    }

    public boolean isCompact() {
        return compact;
    }

    public void setCompact(boolean compact) {
        // Invalidate cache if properties affecting UI size are changed
        if (this.compact != compact) {
            this.compact = compact;
            invalidateCache(); // Compact mode *definitely* affects height
        }
    }

    // === 添加缺失的助手方法 ===

    /**
     * 计算可用的内容宽度（减去边距）
     * @param totalWidth 总宽度
     * @param zoom 缩放因子
     * @return 可用宽度
     */
    protected final float getAvailableWidth(float totalWidth, float zoom) {
        // Assume getContentMargin() is a per-side margin.
        // For total available content width, subtract margin from both sides.
        // This is a common pattern for ImGui layouts.
        return getAvailableContentWidth(totalWidth, zoom);
    }

    /**
     * 计算居中偏移量
     * @param availableWidth 可用宽度
     * @param elementWidth 元素宽度
     * @return 居中偏移量
     */
    protected final float getCenterOffset(float availableWidth, float elementWidth) {
        return ZoomHelper.getCenterOffset(availableWidth, elementWidth);
    }
}