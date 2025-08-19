package com.nodecraft.gui.style;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import com.nodecraft.core.NodeCraft;

/**
 * Minecraft 风格的 ImGui 主题系统
 * 
 * 提供统一的样式管理和缩放处理，确保自定义UI元素
 * 与 Minecraft 的视觉风格保持一致，同时解决缩放问题。
 */
public class MinecraftUITheme {
    
    // === 颜色常量 ===
    
    // Minecraft GUI 标准颜色
    private static final float[] MINECRAFT_BUTTON_NORMAL = {0.2f, 0.2f, 0.2f, 0.8f};
    private static final float[] MINECRAFT_BUTTON_HOVERED = {0.3f, 0.3f, 0.3f, 0.9f};
    private static final float[] MINECRAFT_BUTTON_ACTIVE = {0.15f, 0.15f, 0.15f, 0.95f};
    
    private static final float[] MINECRAFT_FRAME_BG = {0.16f, 0.16f, 0.16f, 0.54f};
    private static final float[] MINECRAFT_FRAME_BG_HOVERED = {0.26f, 0.26f, 0.26f, 0.64f};
    private static final float[] MINECRAFT_FRAME_BG_ACTIVE = {0.36f, 0.36f, 0.36f, 0.74f};
    
    private static final float[] MINECRAFT_TEXT_NORMAL = {1.0f, 1.0f, 1.0f, 1.0f};
    private static final float[] MINECRAFT_TEXT_DISABLED = {0.5f, 0.5f, 0.5f, 1.0f};
    
    private static final float[] MINECRAFT_BORDER = {0.43f, 0.43f, 0.50f, 0.5f};
    private static final float[] MINECRAFT_SEPARATOR = {0.43f, 0.43f, 0.50f, 0.5f};
    
    // === 样式状态管理 ===
    
    private static class StyleState {
        // 原始样式值
        float originalFramePaddingX, originalFramePaddingY;
        float originalItemSpacingX, originalItemSpacingY;
        float originalIndentSpacing, originalFrameBorderSize;
        float originalFrameRounding, originalGrabRounding;
        
        // 当前缩放
        float currentZoom = 1.0f;
        
        // 是否已应用
        boolean applied = false;
    }
    
    private static final ThreadLocal<StyleState> currentState = ThreadLocal.withInitial(StyleState::new);
    
    // === 公共 API ===
    
    /**
     * 应用 Minecraft 风格主题
     * 
     * @param zoom 缩放级别（现在主要用于兼容性，实际缩放由 CustomUIRenderer 处理）
     * @return 样式作用域，用于自动恢复
     */
    public static MinecraftStyleScope apply(float zoom) {
        StyleState state = currentState.get();
        
        if (state.applied) {
            NodeCraft.LOGGER.warn("MinecraftUITheme: 尝试重复应用主题，将先恢复之前的状态");
            restore();
        }
        
        // 保存原始样式
        saveOriginalStyle(state);
        
        // 应用 Minecraft 颜色主题
        applyMinecraftColors();
        
        // 注意：缩放样式现在由 CustomUIRenderer 统一处理，这里只应用颜色主题
        
        state.applied = true;
        state.currentZoom = zoom;
        
        return new MinecraftStyleScope();
    }
    
    /**
     * 恢复原始样式
     */
    public static void restore() {
        StyleState state = currentState.get();
        
        if (!state.applied) {
            return; // 没有应用过，无需恢复
        }
        
        try {
            // 恢复样式值
            ImGui.getStyle().setFramePadding(state.originalFramePaddingX, state.originalFramePaddingY);
            ImGui.getStyle().setItemSpacing(state.originalItemSpacingX, state.originalItemSpacingY);
            ImGui.getStyle().setIndentSpacing(state.originalIndentSpacing);
            ImGui.getStyle().setFrameBorderSize(state.originalFrameBorderSize);
            ImGui.getStyle().setFrameRounding(state.originalFrameRounding);
            ImGui.getStyle().setGrabRounding(state.originalGrabRounding);
            
            state.applied = false;
            
        } catch (Exception e) {
            NodeCraft.LOGGER.error("MinecraftUITheme: 恢复样式时发生错误", e);
        }
    }
    
    // === 内部实现 ===
    
    private static void saveOriginalStyle(StyleState state) {
        var style = ImGui.getStyle();
        state.originalFramePaddingX = style.getFramePaddingX();
        state.originalFramePaddingY = style.getFramePaddingY();
        state.originalItemSpacingX = style.getItemSpacingX();
        state.originalItemSpacingY = style.getItemSpacingY();
        state.originalIndentSpacing = style.getIndentSpacing();
        state.originalFrameBorderSize = style.getFrameBorderSize();
        state.originalFrameRounding = style.getFrameRounding();
        state.originalGrabRounding = style.getGrabRounding();
    }
    
    private static void applyMinecraftColors() {
        var style = ImGui.getStyle();
        
        // 按钮颜色
        style.setColor(ImGuiCol.Button, MINECRAFT_BUTTON_NORMAL[0], MINECRAFT_BUTTON_NORMAL[1], 
                      MINECRAFT_BUTTON_NORMAL[2], MINECRAFT_BUTTON_NORMAL[3]);
        style.setColor(ImGuiCol.ButtonHovered, MINECRAFT_BUTTON_HOVERED[0], MINECRAFT_BUTTON_HOVERED[1], 
                      MINECRAFT_BUTTON_HOVERED[2], MINECRAFT_BUTTON_HOVERED[3]);
        style.setColor(ImGuiCol.ButtonActive, MINECRAFT_BUTTON_ACTIVE[0], MINECRAFT_BUTTON_ACTIVE[1], 
                      MINECRAFT_BUTTON_ACTIVE[2], MINECRAFT_BUTTON_ACTIVE[3]);
        
        // 框架颜色
        style.setColor(ImGuiCol.FrameBg, MINECRAFT_FRAME_BG[0], MINECRAFT_FRAME_BG[1], 
                      MINECRAFT_FRAME_BG[2], MINECRAFT_FRAME_BG[3]);
        style.setColor(ImGuiCol.FrameBgHovered, MINECRAFT_FRAME_BG_HOVERED[0], MINECRAFT_FRAME_BG_HOVERED[1], 
                      MINECRAFT_FRAME_BG_HOVERED[2], MINECRAFT_FRAME_BG_HOVERED[3]);
        style.setColor(ImGuiCol.FrameBgActive, MINECRAFT_FRAME_BG_ACTIVE[0], MINECRAFT_FRAME_BG_ACTIVE[1], 
                      MINECRAFT_FRAME_BG_ACTIVE[2], MINECRAFT_FRAME_BG_ACTIVE[3]);
        
        // 文字颜色
        style.setColor(ImGuiCol.Text, MINECRAFT_TEXT_NORMAL[0], MINECRAFT_TEXT_NORMAL[1], 
                      MINECRAFT_TEXT_NORMAL[2], MINECRAFT_TEXT_NORMAL[3]);
        style.setColor(ImGuiCol.TextDisabled, MINECRAFT_TEXT_DISABLED[0], MINECRAFT_TEXT_DISABLED[1], 
                      MINECRAFT_TEXT_DISABLED[2], MINECRAFT_TEXT_DISABLED[3]);
        
        // 边框和分隔符
        style.setColor(ImGuiCol.Border, MINECRAFT_BORDER[0], MINECRAFT_BORDER[1], 
                      MINECRAFT_BORDER[2], MINECRAFT_BORDER[3]);
        style.setColor(ImGuiCol.Separator, MINECRAFT_SEPARATOR[0], MINECRAFT_SEPARATOR[1], 
                      MINECRAFT_SEPARATOR[2], MINECRAFT_SEPARATOR[3]);
    }
    
    private static void applyScaledStyle(StyleState state, float zoom) {
        var style = ImGui.getStyle();
        
        // 应用缩放到尺寸相关的样式
        style.setFramePadding(state.originalFramePaddingX * zoom, state.originalFramePaddingY * zoom);
        style.setItemSpacing(state.originalItemSpacingX * zoom, state.originalItemSpacingY * zoom);
        style.setIndentSpacing(state.originalIndentSpacing * zoom);
        style.setFrameBorderSize(state.originalFrameBorderSize * zoom);
        
        // Minecraft 风格：无圆角或最小圆角
        style.setFrameRounding(Math.max(0.0f, state.originalFrameRounding * zoom * 0.5f)); // 减少圆角
        style.setGrabRounding(Math.max(0.0f, state.originalGrabRounding * zoom * 0.5f));
    }
    
    // === 样式作用域类 ===
    
    /**
     * 自动恢复的样式作用域
     * 实现 AutoCloseable 接口，支持 try-with-resources 语法
     */
    public static class MinecraftStyleScope implements AutoCloseable {
        
        private boolean closed = false;
        
        private MinecraftStyleScope() {
            // 包私有构造函数，只能通过 apply() 创建
        }
        
        @Override
        public void close() {
            if (!closed) {
                restore();
                closed = true;
            }
        }
        
        /**
         * 手动恢复样式（可选）
         */
        public void restore() {
            MinecraftUITheme.restore();
        }
        
        /**
         * 获取当前缩放级别
         */
        public float getCurrentZoom() {
            return currentState.get().currentZoom;
        }
        
        /**
         * 检查样式是否已应用
         */
        public boolean isApplied() {
            return currentState.get().applied;
        }
    }
    
    // === 便利方法 ===
    
    /**
     * 创建 Minecraft 风格的按钮
     * 
     * @param label 按钮文字
     * @param width 按钮宽度（将自动缩放）
     * @param height 按钮高度（将自动缩放）
     * @return 是否被点击
     */
    public static boolean minecraftButton(String label, float width, float height) {
        // 按钮会自动使用当前应用的主题样式
        return ImGui.button(label, width, height);
    }
    
    /**
     * 创建 Minecraft 风格的复选框
     * 
     * @param label 复选框标签
     * @param value 当前值
     * @return 是否发生变化
     */
    public static boolean minecraftCheckbox(String label, boolean value) {
        return ImGui.checkbox(label, value);
    }
    
    /**
     * 创建 Minecraft 风格的滑块
     * 
     * @param label 滑块标签
     * @param value 当前值数组
     * @param min 最小值
     * @param max 最大值
     * @return 是否发生变化
     */
    public static boolean minecraftSlider(String label, float[] value, float min, float max) {
        return ImGui.sliderFloat(label, value, min, max);
    }
    
    /**
     * 添加 Minecraft 风格的间距
     * 
     * @param spacing 间距大小（逻辑单位，将自动缩放）
     */
    public static void minecraftSpacing(float spacing) {
        float zoom = currentState.get().currentZoom;
        ImGui.dummy(0, spacing * zoom);
    }
    
    /**
     * 创建 Minecraft 风格的分隔符
     */
    public static void minecraftSeparator() {
        ImGui.separator();
    }
    
    /**
     * 显示 Minecraft 风格的文字
     * 
     * @param text 文字内容
     */
    public static void minecraftText(String text) {
        ImGui.text(text);
    }
    
    /**
     * 显示 Minecraft 风格的禁用文字
     * 
     * @param text 文字内容
     */
    public static void minecraftTextDisabled(String text) {
        ImGui.textDisabled(text);
    }
    
    /**
     * 创建一个演示UI，展示 Minecraft 主题的效果
     * 
     * @param zoom 缩放级别
     * @return 是否有任何交互发生
     */
    public static boolean renderDemo(float zoom) {
        boolean interacted = false;
        
        try (MinecraftStyleScope scope = apply(zoom)) {
            minecraftText("Minecraft UI 主题演示");
            minecraftSeparator();
            
            minecraftSpacing(5.0f);
            
            if (minecraftButton("示例按钮", 120, 0)) {
                interacted = true;
            }
            
            minecraftSpacing(3.0f);
            
            boolean[] checkboxValue = {false};
            if (minecraftCheckbox("示例复选框", checkboxValue[0])) {
                interacted = true;
            }
            
            minecraftSpacing(3.0f);
            
            float[] sliderValue = {0.5f};
            if (minecraftSlider("示例滑块", sliderValue, 0.0f, 1.0f)) {
                interacted = true;
            }
            
            minecraftSpacing(5.0f);
            minecraftTextDisabled("当前缩放: " + String.format("%.2f", zoom));
            
        } catch (Exception e) {
            NodeCraft.LOGGER.error("MinecraftUITheme 演示渲染失败", e);
        }
        
        return interacted;
    }
} 