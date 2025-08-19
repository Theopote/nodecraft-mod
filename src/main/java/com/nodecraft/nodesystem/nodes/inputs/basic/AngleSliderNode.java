package com.nodecraft.nodesystem.nodes.inputs.basic;

// 导入必要的 NodeCraft 基类或接口
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import imgui.ImVec2;
import org.jetbrains.annotations.Nullable;

// 导入ImGui相关类用于自定义UI渲染
import imgui.ImGui;
import imgui.ImDrawList;
import imgui.type.ImInt;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar; // 确保导入 ImGuiStyleVar

// 导入属性注解
import com.nodecraft.nodesystem.api.NodeProperty;
import com.nodecraft.nodesystem.api.NodeInfo;

// 导入自定义UI基类
import com.nodecraft.gui.editor.impl.BaseCustomUINode;
import com.nodecraft.gui.editor.impl.ZoomHelper;

import java.util.UUID;

/**
 * 角度滑动条节点 - 现代化实现
 *
 * 继承BaseCustomUINode，自动获得缩放感知和布局管理功能。
 *
 * 特性：
 * - 0-360度角度范围
 * - 度数/弧度输出选择
 * - 自动缩放友好的UI布局
 * - ViewPort感知渲染
 * - 防裁剪的内容渲染
 */
@NodeInfo(
    id = "inputs.basic.angle_slider",
    displayName = "角度滑动条",
    description = "输出一个可通过滑动条调节的角度值（0-360度）。支持度数和弧度输出。",
    category = "inputs.basic"
)
public class AngleSliderNode extends BaseCustomUINode {

    // --- 样式常量 ---
    /** 滑块手柄颜色 */
    private static final int ANGLE_SLIDER_GRAB_COLOR = ImGui.colorConvertFloat4ToU32(0.8f, 0.5f, 0.2f, 1.0f); // 橙色
    /** 滑块手柄活跃颜色 */
    private static final int ANGLE_SLIDER_GRAB_ACTIVE_COLOR = ImGui.colorConvertFloat4ToU32(1.0f, 0.6f, 0.3f, 1.0f); // 更亮的橙色

    // --- 节点属性 ---
    @NodeProperty(displayName = "当前角度", category = "角度设置", order = 1,
            description = "滑动条的当前角度值（度）")
    private double currentAngle = 0.0;

    @NodeProperty(displayName = "最小角度", category = "角度设置", order = 2,
            description = "滑动条的最小角度值（度）")
    private double minAngle = 0.0;

    @NodeProperty(displayName = "最大角度", category = "角度设置", order = 3,
            description = "滑动条的最大角度值（度）")
    private double maxAngle = 360.0;

    @NodeProperty(displayName = "角度单位", category = "输出设置", order = 10,
            description = "输出角度的单位：度或弧度")
    private AngleUnit angleUnit = AngleUnit.DEGREES;

    @NodeProperty(displayName = "显示范围输入", category = "UI设置", order = 20,
            description = "是否在滑动条两侧显示可编辑的范围输入框")
    private boolean showRangeInputs = true;

    @NodeProperty(displayName = "启用直接绘制", category = "性能", order = 30,
            description = "启用直接绘制模式以提升渲染性能。注意：此模式仅支持只读显示")
    private boolean allowDirectDrawing = false;

    // 角度单位枚举
    public enum AngleUnit {
        DEGREES("度", "°"),
        RADIANS("弧度", "rad");

        private final String displayName;
        private final String symbol;

        AngleUnit(String displayName, String symbol) {
            this.displayName = displayName;
            this.symbol = symbol;
        }

        public String getDisplayName() { return displayName; }
        public String getSymbol() { return symbol; }
    }

    // --- 输出端口 ---
    private static final String OUTPUT_ANGLE_ID = "output_angle";

    /**
     * 构造一个新的角度滑动条节点
     */
    public AngleSliderNode() {
        // 使用BaseCustomUINode构造函数
        super(UUID.randomUUID(), "inputs.basic.angle_slider");

        // 创建并添加输出端口 (固定为 Double 类型)
        IPort angleOutput = new BasePort(OUTPUT_ANGLE_ID, "Angle", "The selected angle value", NodeDataType.DOUBLE, this);
        addOutputPort(angleOutput);

        // 初始化输出值
        updateOutput();
    }

    @Override
    public String getDescription() {
        return "输出一个可通过滑动条调节的角度值（0-360度）。支持度数和弧度输出。";
    }

    /**
     * 节点的计算逻辑
     * @param context 执行上下文
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        updateOutput();
    }

    // === BaseCustomUINode 抽象方法实现 ===

    @Override
    protected float calculateUIHeight() {
        float height = getMediumPadding(); // 顶部边距

        // 范围输入框（如果启用）
        if (showRangeInputs) {
            height += 20f; // 范围输入框的高度（逻辑单位）
            height += getSmallPadding();
        }

        // 滑动条 (ImGui.getFrameHeight() 是缩放后的，这里用一个基准值，如 20f)
        height += 20f; // 滑动条的高度（逻辑单位）

        // 滑动条后间距
        height += getMediumPadding();

        // 单位选择器 (ImGui.getFrameHeight() 是缩放后的，这里用一个基准值，如 20f)
        height += 20f; // 下拉框的高度（逻辑单位）

        // 底部边距
        height += getMediumPadding();

        return height;
    }

    @Override
    protected float calculateMinUIWidth() {
        float minWidth = 0;

        // 滑动条的最小宽度 (逻辑单位)
        float sliderMinWidth = 120.0f;

        // 如果显示范围输入框，需要额外空间
        if (showRangeInputs) {
            // 两个输入框的宽度 + 间距
            float inputBoxWidth = 60.0f; // 每个输入框的宽度
            float spacing = 12.0f; // 间距
            sliderMinWidth += (inputBoxWidth * 2 + spacing * 2);
        }

        // 单位选择器的宽度需求
        float unitSelectorLogicalWidth = 80.0f; // 下拉框逻辑宽度
        minWidth = Math.max(minWidth, unitSelectorLogicalWidth);

        // 取最大值并加上节点内容边距 (逻辑单位)
        minWidth = Math.max(minWidth, sliderMinWidth) + getContentMargin() * 2; // 左右各有一个 content margin

        return minWidth;
    }

    @Override
    public boolean supportsDirectDrawing() {
        return allowDirectDrawing;
    }

    @Override
    public boolean renderCustomUIDirect(ImDrawList drawList, float screenX, float screenY,
                                        float width, float height, float zoom) {
        if (!supportsDirectDrawing()) {
            return false;
        }

        try {
            // 参数 width 和 height 已经是缩放后的实际像素值
            // screenX 和 screenY 是组件在屏幕上的左上角像素坐标

            // 获取主题颜色
            int bgColor = ImGui.getColorU32(ImGuiCol.FrameBg);
            int borderColor = ImGui.getColorU32(ImGuiCol.Border);
            int textColor = ImGui.getColorU32(ImGuiCol.Text);
            int sliderBgColor = ImGui.getColorU32(ImGuiCol.FrameBgHovered);

            // 计算缩放后的字体大小
            float scaledFontSize = ImGui.getFontSize() * zoom; // ImGui.getFontSize() 已经是基准字体大小

            // 绘制背景框架 (不使用圆角)
            float borderThickness = ZoomHelper.applyZoom(1.0f, zoom);

            drawList.addRectFilled(screenX, screenY, screenX + width, screenY + height, bgColor);
            drawList.addRect(screenX, screenY, screenX + width, screenY + height,
                    borderColor, 0, 0, borderThickness);

            // 计算布局参数 (所有尺寸和间距都需要手动缩放)
            float padding = ZoomHelper.applyZoom(getMediumPadding(), zoom);
            float sliderLogicalHeight = 20.0f; // 滑块在逻辑单位下的高度
            float sliderPixelHeight = ZoomHelper.applyZoom(sliderLogicalHeight, zoom);

            float currentY = screenY + padding; // 当前绘制的Y坐标

            // 绘制范围输入框（如果启用）
            if (showRangeInputs) {
                // 绘制最小值和最大值标签
                String minText = String.format("%.0f°", minAngle);
                String maxText = String.format("%.0f°", maxAngle);
                
                ImVec2 minTextSize = ImGui.calcTextSize(minText);
                ImVec2 maxTextSize = ImGui.calcTextSize(maxText);
                
                float minTextX = screenX + padding;
                float maxTextX = screenX + width - padding - maxTextSize.x;
                
                drawList.addText(ImGui.getFont(), scaledFontSize * 0.8f, minTextX, currentY, textColor, minText);
                drawList.addText(ImGui.getFont(), scaledFontSize * 0.8f, maxTextX, currentY, textColor, maxText);
                
                currentY += ZoomHelper.applyZoom(20.0f, zoom) + ZoomHelper.applyZoom(getSmallPadding(), zoom);
            }

            // 绘制滑块轨道
            float sliderX = screenX + padding;
            float sliderActualWidth = width - 2 * padding;

            // 绘制滑块轨道背景 (不使用圆角)
            drawList.addRectFilled(sliderX, currentY, sliderX + sliderActualWidth, currentY + sliderPixelHeight,
                    sliderBgColor);

            // 绘制滑块手柄
            double normalizedValue = (currentAngle - minAngle) / (maxAngle - minAngle);
            float handleLogicalWidth = 12.0f; // 手柄在逻辑单位下的宽度
            float handlePixelWidth = ZoomHelper.applyZoom(handleLogicalWidth, zoom);
            float handleX = sliderX + (float)((sliderActualWidth - handlePixelWidth) * normalizedValue);

            drawList.addRectFilled(handleX, currentY, handleX + handlePixelWidth, currentY + sliderPixelHeight,
                    ANGLE_SLIDER_GRAB_COLOR);

            currentY += sliderPixelHeight + padding;

            // 绘制单位指示
            String unitText = "单位: " + angleUnit.getDisplayName();
            ImVec2 unitTextSize = ImGui.calcTextSize(unitText);
            float unitTextWidth = unitTextSize.x;
            float unitTextX = screenX + (width - unitTextWidth) / 2; // 居中

            drawList.addText(ImGui.getFont(), scaledFontSize * 0.9f, unitTextX, currentY, textColor, unitText);

        } catch (Exception e) {
            System.err.println("AngleSliderNode直接绘制失败: " + e.getMessage());
            e.printStackTrace(); // 调试时打印堆栈
        }

        return false; // Direct Drawing 模式通常不处理交互，所以返回 false
    }

    @Override
    protected boolean renderCustomUIScaled(float width, float height, float zoom) {
        boolean valueChanged = false;
        boolean hasUIInteraction = false; // 跟踪是否有UI交互

        try {
            // 使用助手方法计算可用宽度
            float availableWidth = getAvailableWidth(width, zoom);

            // 添加顶部间距
            addVerticalSpacing(getMediumPadding(), zoom);

            // --- 范围输入框部分 ---
            if (showRangeInputs) {
                // 计算输入框布局
                float inputBoxWidth = ZoomHelper.applyZoom(60.0f, zoom);
                float spacing = ZoomHelper.applyZoom(8.0f, zoom);
                float sliderWidth = availableWidth - (inputBoxWidth * 2 + spacing * 2);

                // 最小值输入框
                ImGui.pushItemWidth(inputBoxWidth);
                imgui.type.ImFloat minValue = new imgui.type.ImFloat((float) minAngle);
                if (ImGui.inputFloat("##min_angle", minValue, 0.0f, 0.0f, "%.0f")) {
                    setMinAngle(minValue.get());
                    valueChanged = true;
                }
                hasUIInteraction |= ImGui.isItemHovered() || ImGui.isItemActive();
                ImGui.popItemWidth();

                ImGui.sameLine(0, spacing);

                // 滑动条（使用动态范围）
                ImGui.pushItemWidth(sliderWidth);
                
                // 设置样式，移除圆角
                ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 0.0f);
                ImGui.pushStyleVar(ImGuiStyleVar.GrabRounding, 0.0f);
                ImGui.pushStyleVar(ImGuiStyleVar.GrabMinSize, ZoomHelper.applyZoom(14.0f, zoom));
                ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, ZoomHelper.applyZoom(2.0f, zoom), ZoomHelper.applyZoom(4.0f, zoom));

                // 设置角度滑动条的特殊颜色
                ImGui.pushStyleColor(ImGuiCol.FrameBg, 0.1f, 0.1f, 0.15f, 0.9f);
                ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, 0.15f, 0.15f, 0.2f, 0.95f);
                
                // 修复：正确使用colorConvertU32ToFloat4方法
                imgui.ImVec4 grabColor = new imgui.ImVec4();
                imgui.ImVec4 grabActiveColor = new imgui.ImVec4();
                ImGui.colorConvertU32ToFloat4(ANGLE_SLIDER_GRAB_COLOR, grabColor);
                ImGui.colorConvertU32ToFloat4(ANGLE_SLIDER_GRAB_ACTIVE_COLOR, grabActiveColor);
                
                ImGui.pushStyleColor(ImGuiCol.SliderGrab, grabColor.x, grabColor.y, grabColor.z, grabColor.w);
                ImGui.pushStyleColor(ImGuiCol.SliderGrabActive, grabActiveColor.x, grabActiveColor.y, grabActiveColor.z, grabActiveColor.w);

                // 角度滑动条（使用动态范围）
                float[] angleValue = {(float) currentAngle};
                if (ImGui.sliderFloat("##angle_slider", angleValue, (float) minAngle, (float) maxAngle, "%.1f°")) {
                    setCurrentAngle(angleValue[0]);
                    valueChanged = true;
                }

                // 修复：检测滑动条的交互状态，确保在悬停或拖动时阻止节点拖动
                boolean isSliderHovered = ImGui.isItemHovered();
                boolean isSliderActive = ImGui.isItemActive();
                hasUIInteraction |= isSliderHovered || isSliderActive;

                // 恢复样式
                ImGui.popStyleColor(4); // 弹出4个颜色样式
                ImGui.popStyleVar(4); // 弹出4个样式变量
                ImGui.popItemWidth();

                ImGui.sameLine(0, spacing);

                // 最大值输入框
                ImGui.pushItemWidth(inputBoxWidth);
                imgui.type.ImFloat maxValue = new imgui.type.ImFloat((float) maxAngle);
                if (ImGui.inputFloat("##max_angle", maxValue, 0.0f, 0.0f, "%.0f")) {
                    setMaxAngle(maxValue.get());
                    valueChanged = true;
                }
                hasUIInteraction |= ImGui.isItemHovered() || ImGui.isItemActive();
                ImGui.popItemWidth();
            } else {
                // 如果不显示范围输入框，使用全宽滑动条
                ImGui.pushItemWidth(availableWidth);
                
                // 设置样式，移除圆角
                ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 0.0f);
                ImGui.pushStyleVar(ImGuiStyleVar.GrabRounding, 0.0f);
                ImGui.pushStyleVar(ImGuiStyleVar.GrabMinSize, ZoomHelper.applyZoom(14.0f, zoom));
                ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, ZoomHelper.applyZoom(2.0f, zoom), ZoomHelper.applyZoom(4.0f, zoom));

                // 设置角度滑动条的特殊颜色
                ImGui.pushStyleColor(ImGuiCol.FrameBg, 0.1f, 0.1f, 0.15f, 0.9f);
                ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, 0.15f, 0.15f, 0.2f, 0.95f);
                
                imgui.ImVec4 grabColor = new imgui.ImVec4();
                imgui.ImVec4 grabActiveColor = new imgui.ImVec4();
                ImGui.colorConvertU32ToFloat4(ANGLE_SLIDER_GRAB_COLOR, grabColor);
                ImGui.colorConvertU32ToFloat4(ANGLE_SLIDER_GRAB_ACTIVE_COLOR, grabActiveColor);
                
                ImGui.pushStyleColor(ImGuiCol.SliderGrab, grabColor.x, grabColor.y, grabColor.z, grabColor.w);
                ImGui.pushStyleColor(ImGuiCol.SliderGrabActive, grabActiveColor.x, grabActiveColor.y, grabActiveColor.z, grabActiveColor.w);

                // 角度滑动条（使用动态范围）
                float[] angleValue = {(float) currentAngle};
                if (ImGui.sliderFloat("##angle_slider", angleValue, (float) minAngle, (float) maxAngle, "%.1f°")) {
                    setCurrentAngle(angleValue[0]);
                    valueChanged = true;
                }

                boolean isSliderHovered = ImGui.isItemHovered();
                boolean isSliderActive = ImGui.isItemActive();
                hasUIInteraction |= isSliderHovered || isSliderActive;

                // 恢复样式
                ImGui.popStyleColor(4);
                ImGui.popStyleVar(4);
                ImGui.popItemWidth();
            }

            // 添加角度单位选择器的间距
            addVerticalSpacing(getMediumPadding(), zoom);

            // --- 角度单位选择器部分 ---
            float unitSelectorLogicalWidth = 80.0f;
            setCenterX(availableWidth, ZoomHelper.applyZoom(unitSelectorLogicalWidth, zoom)); // 居中下拉框

            // 应用缩放到框架内边距和 ItemWidth
            ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, ZoomHelper.applyZoom(2.0f, zoom), ZoomHelper.applyZoom(1.0f, zoom));
            ImGui.pushItemWidth(ZoomHelper.applyZoom(unitSelectorLogicalWidth, zoom));

            String[] unitNames = {"度", "弧度"};
            int currentUnitIndex = angleUnit == AngleUnit.DEGREES ? 0 : 1;
            ImInt unitIndex = new ImInt(currentUnitIndex);

            if (ImGui.combo("##unit_selector", unitIndex, unitNames)) {
                angleUnit = unitIndex.get() == 0 ? AngleUnit.DEGREES : AngleUnit.RADIANS;
                updateOutput();
                valueChanged = true;
            }

            // 修复：检测下拉框的交互状态，确保在交互时阻止节点拖动
            boolean isComboHovered = ImGui.isItemHovered();
            boolean isComboActive = ImGui.isItemActive();
            hasUIInteraction |= isComboHovered || isComboActive;

            ImGui.popItemWidth();
            ImGui.popStyleVar(); // 弹出 FramePadding

            // 底部间距
            addVerticalSpacing(getMediumPadding(), zoom);

        } catch (Exception e) {
            System.err.println("AngleSliderNode UI渲染失败: " + e.getMessage());
            e.printStackTrace(); // 调试时打印堆栈跟踪
        }

        // 如果有UI交互，阻止事件传播到底层画布和游戏世界
        if (hasUIInteraction) {
            return true;
        }

        return valueChanged;
    }

    /**
     * 更新角度滑块的当前值
     * @param newAngle 新的角度值（度）
     */
    public void setCurrentAngle(double newAngle) {
        // 将角度规范化到 0-360 度范围
        double normalizedAngle = normalizeAngle(newAngle);

        // 四舍五入到一位小数
        double roundedAngle = Math.round(normalizedAngle * 10.0) / 10.0;

        if (Math.abs(this.currentAngle - roundedAngle) > 1e-9) {
            this.currentAngle = roundedAngle;
            updateOutput();
            markDirty(); // 标记节点为脏，以便重新计算
        }
    }

    /**
     * 将角度规范化到 0-360 度范围
     * @param angle 输入角度
     * @return 规范化后的角度
     */
    private double normalizeAngle(double angle) {
        angle = angle % 360.0;
        if (angle < 0) {
            angle += 360.0;
        }
        return angle;
    }

    /**
     * 更新输出端口的值
     */
    private void updateOutput() {
        double outputValue;
        if (angleUnit == AngleUnit.RADIANS) {
            // 转换为弧度
            outputValue = Math.toRadians(currentAngle);
        } else {
            // 输出度数
            outputValue = currentAngle;
        }
        outputValues.put(OUTPUT_ANGLE_ID, outputValue);
    }

    // --- Getters/Setters for Properties ---

    public double getCurrentAngle() {
        return currentAngle;
    }

    public AngleUnit getAngleUnit() {
        return angleUnit;
    }

    public void setAngleUnit(AngleUnit angleUnit) {
        if (this.angleUnit != angleUnit) {
            this.angleUnit = angleUnit;
            updateOutput();
            markDirty();
        }
    }

    public boolean isShowRangeInputs() {
        return showRangeInputs;
    }

    public void setShowRangeInputs(boolean showRangeInputs) {
        if (this.showRangeInputs != showRangeInputs) {
            this.showRangeInputs = showRangeInputs;
            invalidateCache(); // 范围输入框显示会影响宽度，需要重新计算布局
            markDirty();
        }
    }

    public boolean isAllowDirectDrawing() {
        return allowDirectDrawing;
    }

    public void setAllowDirectDrawing(boolean allowDirectDrawing) {
        if (this.allowDirectDrawing != allowDirectDrawing) {
            this.allowDirectDrawing = allowDirectDrawing;
            invalidateCache(); // 改变绘制模式可能影响某些布局属性，尽管此处不直接影响
            markDirty();
        }
    }

    public double getMinAngle() {
        return minAngle;
    }

    public void setMinAngle(double minAngle) {
        if (Math.abs(this.minAngle - minAngle) > 1e-9) {
            this.minAngle = minAngle;
            updateOutput();
            markDirty();
        }
    }

    public double getMaxAngle() {
        return maxAngle;
    }

    public void setMaxAngle(double maxAngle) {
        if (Math.abs(this.maxAngle - maxAngle) > 1e-9) {
            this.maxAngle = maxAngle;
            updateOutput();
            markDirty();
        }
    }

    // --- 节点状态序列化 ---

    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("angle", getCurrentAngle());
        state.put("unit", getAngleUnit().name());
        state.put("showRangeInputs", isShowRangeInputs());
        state.put("allowDirectDrawing", isAllowDirectDrawing());
        state.put("minAngle", getMinAngle());
        state.put("maxAngle", getMaxAngle());
        return state;
    }

    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;

            // 加载角度单位
            if (stateMap.containsKey("unit")) {
                Object unit = stateMap.get("unit");
                if (unit instanceof String) {
                    try {
                        this.angleUnit = AngleUnit.valueOf((String) unit);
                    } catch (IllegalArgumentException e) {
                        this.angleUnit = AngleUnit.DEGREES; // 默认值
                    }
                }
            }

            // UI设置
            if (stateMap.containsKey("showRangeInputs")) {
                Object showRange = stateMap.get("showRangeInputs");
                if (showRange instanceof Boolean) {
                    setShowRangeInputs((Boolean) showRange);
                }
            }

            if (stateMap.containsKey("allowDirectDrawing")) {
                Object allowDirect = stateMap.get("allowDirectDrawing");
                if (allowDirect instanceof Boolean) {
                    setAllowDirectDrawing((Boolean) allowDirect);
                }
            }

            // 最后加载角度值
            if (stateMap.containsKey("angle")) {
                Object angle = stateMap.get("angle");
                if (angle instanceof Number) {
                    this.currentAngle = ((Number) angle).doubleValue();
                    setCurrentAngle(this.currentAngle);
                } else if (angle instanceof String) {
                    try {
                        this.currentAngle = Double.parseDouble((String) angle);
                        setCurrentAngle(this.currentAngle);
                    } catch (NumberFormatException e) {
                        System.err.println("Failed to parse angle value for AngleSliderNode: " + angle);
                        this.currentAngle = 0.0;
                        setCurrentAngle(this.currentAngle);
                    }
                }
            } else {
                // 如果状态中没有角度值，设置默认值
                this.currentAngle = 0.0;
                this.angleUnit = AngleUnit.DEGREES;
                this.minAngle = 0.0;
                this.maxAngle = 360.0;
                setCurrentAngle(this.currentAngle);
            }

            if (stateMap.containsKey("minAngle")) {
                Object minAngle = stateMap.get("minAngle");
                if (minAngle instanceof Number) {
                    this.minAngle = ((Number) minAngle).doubleValue();
                    setMinAngle(this.minAngle);
                }
            }

            if (stateMap.containsKey("maxAngle")) {
                Object maxAngle = stateMap.get("maxAngle");
                if (maxAngle instanceof Number) {
                    this.maxAngle = ((Number) maxAngle).doubleValue();
                    setMaxAngle(this.maxAngle);
                }
            }

        } else if (state instanceof Number) {
            // 向后兼容：如果状态只是一个数字，直接使用它作为角度值
            this.currentAngle = ((Number) state).doubleValue();
            setCurrentAngle(this.currentAngle);
        } else {
            // 无法加载状态时，设置默认值
            this.currentAngle = 0.0;
            this.angleUnit = AngleUnit.DEGREES;
            this.minAngle = 0.0;
            this.maxAngle = 360.0;
            this.showRangeInputs = true;
            this.allowDirectDrawing = false;
            setCurrentAngle(this.currentAngle);
        }
        markDirty(); // 确保加载后重新计算下游
    }

    // === 添加缺失的助手方法 ===
    // 假设这些方法在 BaseCustomUINode 或 ZoomHelper 中，如果不在，请确保它们实现正确。
    // 为了使此文件可独立编译，我将它们放在这里，但理想情况它们应该在父类或独立工具类中。

    /**
     * 计算可用的内容宽度（减去边距）
     * @param totalWidth 总宽度
     * @param zoom 缩放因子
     * @return 可用宽度
     */
    protected final float getAvailableWidth(float totalWidth, float zoom) {
        // 假设 getContentMargin() 返回的是逻辑单位的边距
        return totalWidth - ZoomHelper.applyZoom(getContentMargin() * 2, zoom);
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

    // 由于您原始代码的尾部已经提供了这些助手方法，我将保留你原始代码中的 helpers。
    // 我已经将它们放置在了 `BaseCustomUINode` 的方法实现之外，如果它们是该类的一部分，请根据你的结构调整。
    // 最好的实践是把它们放在 `BaseCustomUINode` 中，或者一个公共的 `ImGuiUIHelper` 类中。
}