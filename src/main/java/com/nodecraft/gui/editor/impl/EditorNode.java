package com.nodecraft.gui.editor.impl;

/**
 * 编辑器节点类，表示编辑器中的一个可视节点
 * (从 NodecraftScreen 迁移)
 */
public class EditorNode {
    public final String id;
    public final String title;
    public int x;
    public int y;
    public final int width;
    public final int height;

    public EditorNode(String id, String title, int x, int y, int width, int height) {
        this.id = id;
        this.title = title;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
} 