package com.nodecraft.gui.dialogs;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

/**
 * 通用确认对话框类
 */
public class ConfirmationDialog {
    // 对话框状态
    private static boolean isShowing = false;
    private static String title = "";
    private static String message = "";
    private static Runnable yesAction = null;
    private static Runnable noAction = null;
    private static Runnable cancelAction = null;

    /**
     * 创建一个确认对话框
     * 
     * @param title 对话框标题
     * @param message 对话框消息
     * @param yesAction "是"按钮的操作，如果为null则不显示此按钮
     * @param noAction "否"按钮的操作，如果为null则不显示此按钮
     * @param cancelAction "取消"按钮的操作，如果为null则不显示此按钮
     */
    public ConfirmationDialog(String title, String message, Runnable yesAction, Runnable noAction, Runnable cancelAction) {
        ConfirmationDialog.title = title;
        ConfirmationDialog.message = message;
        ConfirmationDialog.yesAction = yesAction;
        ConfirmationDialog.noAction = noAction;
        ConfirmationDialog.cancelAction = cancelAction;
    }

    /**
     * 显示确认对话框
     */
    public void show() {
        isShowing = true;
    }

    /**
     * 渲染确认对话框
     * 应该在每一帧调用此方法来渲染和处理确认对话框
     */
    public static void renderDialog() {
        if (!isShowing) {
            return;
        }

        int windowFlags = ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoDocking |
                         ImGuiWindowFlags.NoResize | ImGuiWindowFlags.AlwaysAutoResize;
        
        ImGui.setNextWindowSize(300, 150, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowPos(ImGui.getIO().getDisplaySizeX() / 2, ImGui.getIO().getDisplaySizeY() / 2, 
                              ImGuiCond.FirstUseEver, 0.5f, 0.5f);
        
        ImBoolean isOpen = new ImBoolean(true);
        if (ImGui.begin(title + "###ConfirmationDialog", isOpen, windowFlags)) {
            // 显示消息
            ImGui.textWrapped(message);
            
            ImGui.separator();
            
            // 按钮行
            float buttonWidth = 80;
            float totalWidth = 0;
            
            // 计算按钮总宽度
            if (yesAction != null) totalWidth += buttonWidth + 10;
            if (noAction != null) totalWidth += buttonWidth + 10;
            if (cancelAction != null) totalWidth += buttonWidth;
            
            // 计算起始位置，使按钮居中
            float startX = (ImGui.getWindowWidth() - totalWidth) / 2;
            if (startX < 0) startX = 0;
            
            ImGui.setCursorPosX(startX);
            
            // 是按钮
            if (yesAction != null) {
                if (ImGui.button("是", buttonWidth, 0)) {
                    isShowing = false;
                    yesAction.run();
                }
                
                if ((noAction != null || cancelAction != null) && totalWidth > buttonWidth) {
                    ImGui.sameLine();
                }
            }
            
            // 否按钮
            if (noAction != null) {
                if (ImGui.button("否", buttonWidth, 0)) {
                    isShowing = false;
                    noAction.run();
                }
                
                if (cancelAction != null && totalWidth > buttonWidth * 2) {
                    ImGui.sameLine();
                }
            }
            
            // 取消按钮
            if (cancelAction != null) {
                if (ImGui.button("取消", buttonWidth, 0)) {
                    isShowing = false;
                    cancelAction.run();
                }
            }
        }
        ImGui.end();
        
        // 检查窗口是否被关闭（点击右上角X按钮）
        if (!isOpen.get() && isShowing) {
            isShowing = false;
            // 如果有取消操作，则执行；否则什么也不做
            if (cancelAction != null) {
                cancelAction.run();
            }
        }
    }
} 