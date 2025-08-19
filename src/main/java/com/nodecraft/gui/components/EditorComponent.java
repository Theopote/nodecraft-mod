package com.nodecraft.gui.components;

/**
 * NodeCraft编辑器组件接口
 * 所有编辑器组件必须实现此接口，以确保标准的渲染行为和生命周期管理
 */
public interface EditorComponent {
    
    /**
     * 渲染组件
     * 
     * @param x 组件起始X坐标
     * @param y 组件起始Y坐标
     * @param width 组件宽度
     * @param height 组件高度
     * @param paddingX 水平内边距
     * @param paddingY 垂直内边距
     */
    void render(float x, float y, float width, float height, float paddingX, float paddingY);
    
    /**
     * 初始化组件
     * 在组件首次创建时调用，用于设置初始状态
     */
    void init();
    
    /**
     * 清理组件资源
     * 在编辑器关闭时调用，确保资源被正确释放
     */
    void cleanup();
    
    /**
     * 设置组件是否可见
     * 
     * @param visible 可见性状态
     */
    void setVisible(boolean visible);
    
    /**
     * 检查组件是否可见
     * 
     * @return 组件是否可见
     */
    boolean isVisible();
    
    /**
     * 获取组件ID
     * 用于在布局管理器中唯一标识组件
     * 
     * @return 组件ID
     */
    String getComponentId();
    
    /**
     * 接收事件
     * 组件可以接收编辑器的事件通知
     * 
     * @param eventType 事件类型
     * @param data 事件数据
     * @return 是否已处理事件
     */
    boolean handleEvent(String eventType, Object data);
} 