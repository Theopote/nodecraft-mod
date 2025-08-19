package com.nodecraft.core.event;

/**
 * 鼠标滚动事件。
 */
public class MouseScrolledEvent extends Event {
    public final double mouseX;
    public final double mouseY;
    public final double horizontalAmount;
    public final double verticalAmount;

    public MouseScrolledEvent(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.horizontalAmount = horizontalAmount;
        this.verticalAmount = verticalAmount;
    }
} 