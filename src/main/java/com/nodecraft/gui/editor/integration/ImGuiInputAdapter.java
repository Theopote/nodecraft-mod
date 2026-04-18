package com.nodecraft.gui.editor.integration;

public final class ImGuiInputAdapter {
    private ImGuiInputAdapter() {
    }

    public static boolean isMouseClicked(final int button) {
        return ImGuiRenderer.getInstance().isMouseClicked(button);
    }

    public static boolean isMouseDown(final int button) {
        return ImGuiRenderer.getInstance().isMouseDown(button);
    }

    public static boolean isMouseReleased(final int button) {
        return ImGuiRenderer.getInstance().isMouseReleased(button);
    }
}
