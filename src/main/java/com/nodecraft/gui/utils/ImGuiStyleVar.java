package com.nodecraft.gui.utils;

/**
 * ImGui样式变量常量类
 * 包装ImGui的样式变量标志，使其更易于在Java代码中使用
 */
public class ImGuiStyleVar {
    // 窗口相关样式变量
    public static final int WindowPadding = imgui.flag.ImGuiStyleVar.WindowPadding;
    public static final int WindowRounding = imgui.flag.ImGuiStyleVar.WindowRounding;
    public static final int WindowBorderSize = imgui.flag.ImGuiStyleVar.WindowBorderSize;
    public static final int WindowMinSize = imgui.flag.ImGuiStyleVar.WindowMinSize;
    public static final int WindowTitleAlign = imgui.flag.ImGuiStyleVar.WindowTitleAlign;
    
    // 子窗口相关样式变量
    public static final int ChildRounding = imgui.flag.ImGuiStyleVar.ChildRounding;
    public static final int ChildBorderSize = imgui.flag.ImGuiStyleVar.ChildBorderSize;
    
    // 框架相关样式变量
    public static final int FramePadding = imgui.flag.ImGuiStyleVar.FramePadding;
    public static final int FrameRounding = imgui.flag.ImGuiStyleVar.FrameRounding;
    public static final int FrameBorderSize = imgui.flag.ImGuiStyleVar.FrameBorderSize;
    
    // 其他常用样式变量
    public static final int ItemSpacing = imgui.flag.ImGuiStyleVar.ItemSpacing;
    public static final int ItemInnerSpacing = imgui.flag.ImGuiStyleVar.ItemInnerSpacing;
    public static final int IndentSpacing = imgui.flag.ImGuiStyleVar.IndentSpacing;
    public static final int CellPadding = imgui.flag.ImGuiStyleVar.CellPadding;
    public static final int ScrollbarSize = imgui.flag.ImGuiStyleVar.ScrollbarSize;
    public static final int ScrollbarRounding = imgui.flag.ImGuiStyleVar.ScrollbarRounding;
    public static final int GrabMinSize = imgui.flag.ImGuiStyleVar.GrabMinSize;
    public static final int GrabRounding = imgui.flag.ImGuiStyleVar.GrabRounding;
    public static final int TabRounding = imgui.flag.ImGuiStyleVar.TabRounding;
    public static final int ButtonTextAlign = imgui.flag.ImGuiStyleVar.ButtonTextAlign;
    public static final int SelectableTextAlign = imgui.flag.ImGuiStyleVar.SelectableTextAlign;
    public static final int PopupBorderSize = imgui.flag.ImGuiStyleVar.PopupBorderSize;
    public static final int PopupRounding = imgui.flag.ImGuiStyleVar.PopupRounding;
} 