package com.nodecraft.core.event;

/**
 * 鼠标拖动事件。
 */
public class MouseDraggedEvent extends Event {
    public final double mouseX;
    public final double mouseY;
    public final int button;
    public final double deltaX;
    public final double deltaY;

    public MouseDraggedEvent(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.button = button;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }
} 