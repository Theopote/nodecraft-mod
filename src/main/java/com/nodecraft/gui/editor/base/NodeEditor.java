package com.nodecraft.gui.editor.base;

/**
 * 节点编辑器接口，定义节点编辑器的基本行为
 */
public interface NodeEditor {
    
    /**
     * 打开编辑器
     */
    void open();
    
    /**
     * 关闭编辑器
     */
    void close();
    
    /**
     * 检查编辑器是否打开
     * @return 如果编辑器打开返回true
     */
    boolean isOpen();
    
    /**
     * 渲染编辑器
     */
    void render();
} 