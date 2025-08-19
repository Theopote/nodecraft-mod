package com.nodecraft.core.event;

/**
 * 按键按下事件。
 */
public class KeyPressedEvent extends Event {
    public final int keyCode;
    public final int scanCode;
    public final int modifiers;

    public KeyPressedEvent(int keyCode, int scanCode, int modifiers) {
        this.keyCode = keyCode;
        this.scanCode = scanCode;
        this.modifiers = modifiers;
    }
} 