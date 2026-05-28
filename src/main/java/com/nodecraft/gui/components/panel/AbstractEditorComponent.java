package com.nodecraft.gui.components;

import com.nodecraft.core.NodeCraft;

/**
 * 编辑器组件的抽象基类
 * 提供通用实现，简化具体组件的开发
 */
public abstract class AbstractEditorComponent implements EditorComponent {
    
    // 组件状态
    protected boolean visible = true;
    protected String componentId;
    
    // 组件位置和尺寸
    protected float lastX;
    protected float lastY;
    protected float lastWidth;
    protected float lastHeight;
    
    /**
     * 构造函数
     * 
     * @param componentId 组件唯一标识符
     */
    public AbstractEditorComponent(String componentId) {
        this.componentId = componentId;
    }
    
    /**
     * 统一异常日志处理方法
     * 
     * @param context 发生异常的上下文描述
     * @param e 异常对象
     */
    protected void logException(String context, Exception e) {
        NodeCraft.LOGGER.error("{} 时发生错误: {}", context, e.getMessage());
        if (NodeCraft.LOGGER.isDebugEnabled()) {
            NodeCraft.LOGGER.debug("异常类型: {}", e.getClass().getName());
            
            if (e.getCause() != null) {
                NodeCraft.LOGGER.debug("根本原因: {}", e.getCause().getMessage());
            }
            
            StackTraceElement[] stackTrace = e.getStackTrace();
            if (stackTrace.length > 0) {
                NodeCraft.LOGGER.debug("堆栈跟踪:");
                for (int i = 0; i < Math.min(3, stackTrace.length); i++) {
                    NodeCraft.LOGGER.debug("  - {}", stackTrace[i]);
                }
            }
        }
    }
    
    @Override
    public void init() {
        // 默认实现为空，子类可以覆盖
    }
    
    @Override
    public void cleanup() {
        // 默认实现为空，子类可以覆盖
    }
    
    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    @Override
    public boolean isVisible() {
        return visible;
    }
    
    @Override
    public String getComponentId() {
        return componentId;
    }
    
    @Override
    public boolean handleEvent(String eventType, Object data) {
        // 默认实现不处理任何事件
        return false;
    }
    
    @Override
    public void render(float x, float y, float width, float height, float paddingX, float paddingY) {
        // 存储位置和尺寸信息，以便子类使用
        this.lastX = x;
        this.lastY = y;
        this.lastWidth = width;
        this.lastHeight = height;
        
        // 如果组件不可见，则不渲染
        if (!visible) {
            return;
        }
        
        try {
            // 调用子类实现的渲染方法
            renderComponent(x, y, width, height, paddingX, paddingY);
        } catch (Exception e) {
            logException("渲染组件", e);
        }
    }
    
    /**
     * 组件的具体渲染实现
     * 子类必须实现此方法以提供自己的渲染逻辑
     * 
     * @param x 组件起始X坐标
     * @param y 组件起始Y坐标
     * @param width 组件宽度
     * @param height 组件高度
     * @param paddingX 水平内边距
     * @param paddingY 垂直内边距
     */
    protected abstract void renderComponent(float x, float y, float width, float height, float paddingX, float paddingY);
} 