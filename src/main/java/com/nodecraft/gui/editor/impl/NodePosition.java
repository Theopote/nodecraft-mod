package com.nodecraft.gui.editor.impl;

/**
 * 节点在编辑器中的位置信息。
 * 此外，还存储了节点的未缩放（逻辑）宽度和高度，
 * 这些尺寸在渲染器中计算得出，用于精确的交互检测。
 */
public class NodePosition {
    public float x; // 节点在世界坐标系中的X位置 (未缩放)
    public float y; // 节点在世界坐标系中的Y位置 (未缩放)
    public float width;  // 节点的逻辑宽度（未缩放），由渲染器计算并更新
    public float height; // 节点的逻辑高度（未缩放），由渲染器计算并更新

    /**
     * 构造函数，初始化节点的世界坐标位置。
     * 宽度和高度将由渲染器在渲染时动态计算和更新。
     * @param x 节点的世界X坐标
     * @param y 节点的世界Y坐标
     */
    public NodePosition(float x, float y) {
        this.x = x;
        this.y = y;
        this.width = 0; // 初始值为0，等待渲染器计算更新
        this.height = 0; // 初始值为0，等待渲染器计算更新
    }

    /**
     * 构造函数，初始化节点的世界坐标位置、宽度和高度。
     * @param x 节点的世界X坐标
     * @param y 节点的世界Y坐标
     * @param width 节点的逻辑宽度（未缩放）
     * @param height 节点的逻辑高度（未缩放）
     */
    public NodePosition(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * 复制一个位置对象。
     * 复制操作包括位置和尺寸信息。
     * @return 一个新的 NodePosition 实例，包含相同的位置和尺寸信息
     */
    public NodePosition copy() {
        return new NodePosition(this.x, this.y, this.width, this.height);
    }

    /**
     * 更新节点的尺寸。
     * @param width 节点的逻辑宽度（未缩放）
     * @param height 节点的逻辑高度（未缩放）
     */
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    /**
     * 更新节点的位置。
     * @param x 节点的世界X坐标
     * @param y 节点的世界Y坐标
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "NodePosition{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}