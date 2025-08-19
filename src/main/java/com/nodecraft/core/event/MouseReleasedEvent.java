package com.nodecraft.core.event;

/**
 * 鼠标释放事件。
 */
public class MouseReleasedEvent extends Event {
    public final double mouseX;
    public final double mouseY;
    public final int button;

    public MouseReleasedEvent(double mouseX, double mouseY, int button) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.button = button;
    }
} 