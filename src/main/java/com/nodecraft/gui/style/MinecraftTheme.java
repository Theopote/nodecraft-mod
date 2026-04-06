package com.nodecraft.gui.style;

import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;

/**
 * Minecraft风格的ImGui主题
 * 将样式配置集中管理，与业务逻辑解耦
 */
public class MinecraftTheme {

    // 面板背景透明度（0.0 = 完全透明, 1.0 = 完全不透明）
    private static float panelAlpha = 0.94f;

    public static float getPanelAlpha() {
        return panelAlpha;
    }

    public static void setPanelAlpha(float alpha) {
        panelAlpha = Math.max(0.0f, Math.min(1.0f, alpha));
    }

    /**
     * 应用Minecraft风格的ImGui主题
     * @param scope ImGui样式作用域
     */
    public void apply(ImGuiStyleScope scope) {
        // 应用窗口样式变量
        scope.pushStyleVar(ImGuiStyleVar.WindowRounding, 0.0f)  // 方形窗口，无圆角
             .pushStyleVar(ImGuiStyleVar.FrameRounding, 0.0f)   // 方形控件
             .pushStyleVar(ImGuiStyleVar.PopupRounding, 0.0f);  // 方形弹出窗口
        
        // 应用各种颜色
        applyColors(scope);
    }
    
    /**
     * 应用窗口标题对齐样式
     * @param scope ImGui样式作用域
     */
    public void applyTitleAlignment(ImGuiStyleScope scope) {
        scope.pushStyleVar(ImGuiStyleVar.WindowTitleAlign, 0.5f, 0.5f); // 标题居中
    }
    
    /**
     * 应用所有颜色样式
     * @param scope ImGui样式作用域
     */
    private void applyColors(ImGuiStyleScope scope) {
        // 窗口和背景颜色
        scope.pushStyleColor(ImGuiCol.WindowBg, 0.16f, 0.16f, 0.20f, panelAlpha);
        
        // 边框颜色
        scope.pushStyleColor(ImGuiCol.Border, 0.40f, 0.40f, 0.50f, 0.7f);
        
        // 输入框背景
        scope.pushStyleColor(ImGuiCol.FrameBg, 0.20f, 0.20f, 0.25f, 0.85f);
        
        // 标题栏
        scope.pushStyleColor(ImGuiCol.TitleBg, 0.25f, 0.25f, 0.40f, 0.90f);
        
        // 活动标题栏
        scope.pushStyleColor(ImGuiCol.TitleBgActive, 0.32f, 0.32f, 0.63f, 1.00f);
        
        // 按钮相关颜色
        scope.pushStyleColor(ImGuiCol.Button, 0.25f, 0.25f, 0.45f, 0.85f);
        
        scope.pushStyleColor(ImGuiCol.ButtonHovered, 0.35f, 0.35f, 0.60f, 0.95f);
        
        scope.pushStyleColor(ImGuiCol.ButtonActive, 0.40f, 0.40f, 0.70f, 1.00f);
        
        // 标题和选择器相关颜色
        scope.pushStyleColor(ImGuiCol.Header, 0.30f, 0.50f, 0.80f, 0.45f);
        
        scope.pushStyleColor(ImGuiCol.HeaderHovered, 0.35f, 0.60f, 0.90f, 0.55f);
    }
    
    /**
     * 应用背景颜色
     * @param scope ImGui样式作用域
     * @param bgColor 背景颜色数组 [r, g, b, a]
     */
    public void applyBackgroundColor(ImGuiStyleScope scope, float[] bgColor) {
        scope.pushStyleColor(ImGuiCol.ChildBg, bgColor[0], bgColor[1], bgColor[2], bgColor[3]);
    }
} 