package com.nodecraft.gui.dialogs;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

/**
 * 通用消息对话框类
 */
public class MessageDialog {
    // 对话框状态
    private static boolean isShowing = false;
    private static String title = "";
    private static String message = "";
    private static Runnable okAction = null;

    /**
     * 创建一个消息对话框
     * 
     * @param title 对话框标题
     * @param message 对话框消息
     */
    public MessageDialog(String title, String message) {
        this(title, message, null);
    }

    /**
     * 创建一个消息对话框
     * 
     * @param title 对话框标题
     * @param message 对话框消息
     * @param okAction "确定"按钮的操作，可以为null
     */
    public MessageDialog(String title, String message, Runnable okAction) {
        MessageDialog.title = title;
        MessageDialog.message = message;
        MessageDialog.okAction = okAction;
    }

    /**
     * 显示消息对话框
     */
    public void show() {
        isShowing = true;
    }

    /**
     * 渲染消息对话框
     * 应该在每一帧调用此方法来渲染和处理消息对话框
     */
    public static void renderDialog() {
        if (!isShowing) {
            return;
        }

        int windowFlags = ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoDocking |
                         ImGuiWindowFlags.NoResize | ImGuiWindowFlags.AlwaysAutoResize;
        
        ImGui.setNextWindowSize(350, 150, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowPos(ImGui.getIO().getDisplaySizeX() / 2, ImGui.getIO().getDisplaySizeY() / 2, 
                              ImGuiCond.FirstUseEver, 0.5f, 0.5f);
        
        ImBoolean isOpen = new ImBoolean(true);
        if (ImGui.begin(title + "###MessageDialog", isOpen, windowFlags)) {
            // 显示消息
            ImGui.textWrapped(message);
            
            ImGui.separator();
            
            // 确定按钮
            float buttonWidth = 100;
            float startX = (ImGui.getWindowWidth() - buttonWidth) / 2;
            if (startX < 0) startX = 0;
            
            ImGui.setCursorPosX(startX);
            
            if (ImGui.button("确定", buttonWidth, 0)) {
                isShowing = false;
                if (okAction != null) {
                    okAction.run();
                }
            }
        }
        ImGui.end();
        
        // 检查窗口是否被关闭（点击右上角X按钮）
        if (!isOpen.get() && isShowing) {
            isShowing = false;
            if (okAction != null) {
                okAction.run();
            }
        }
    }
} 