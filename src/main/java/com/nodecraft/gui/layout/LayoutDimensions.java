package com.nodecraft.gui.layout;

/**
 * 存储计算出的组件布局信息（位置和尺寸）。
 * (恢复为 Record 定义)
 */
public record LayoutDimensions(
    float x,
    float y,
    float width,
    float height
) {} 