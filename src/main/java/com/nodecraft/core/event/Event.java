package com.nodecraft.core.event;

/**
 * 基础事件类，可以包含一个 'handled' 状态。
 */
public abstract class Event {
    private boolean handled = false;

    public boolean isHandled() {
        return handled;
    }

    public void setHandled(boolean handled) {
        this.handled = handled;
    }
} 