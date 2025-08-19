package com.nodecraft.core.event;

/**
 * 鼠标点击事件。
 */
public class MouseClickedEvent extends Event {
    public final double mouseX;
    public final double mouseY;
    public final int button;

    public MouseClickedEvent(double mouseX, double mouseY, int button) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.button = button;
    }
} 