package com.nodecraft.core.event;

/**
 * UI相关的编辑器事件
 * 处理与用户界面交互相关的事件
 */
public class EditorUIEvent extends EditorEvent {
    
    /**
     * UI事件类型
     */
    public enum Type {
        MENU_ITEM_CLICKED,   // 菜单项点击
        BUTTON_CLICKED,      // 按钮点击
        TAB_CHANGED,         // 标签页切换
        PANEL_RESIZED,       // 面板大小改变
        PROPERTY_CHANGED,    // 属性变更
        VIEW_ZOOMED,         // 视图缩放
        VIEW_PANNED,         // 视图平移
        VIEW_RESET,          // 视图重置
        THEME_CHANGED,       // 主题切换
        LAYOUT_CHANGED,      // 布局改变
        COMPONENT_HIDDEN,    // 组件隐藏
        COMPONENT_SHOWN      // 组件显示
    }
    
    private final Type uiEventType;
    private final String componentId;
    private final Object eventData;
    
    /**
     * 创建一个UI事件
     * @param uiEventType UI事件类型
     * @param componentId 相关组件的ID
     * @param eventData 事件相关数据
     */
    public EditorUIEvent(Type uiEventType, String componentId, Object eventData) {
        super(EditorUIEvent.class.getSimpleName() + "." + uiEventType);
        this.uiEventType = uiEventType;
        this.componentId = componentId;
        this.eventData = eventData;
    }
    
    /**
     * 创建一个UI事件（无附加数据）
     * @param uiEventType UI事件类型
     * @param componentId 相关组件的ID
     */
    public EditorUIEvent(Type uiEventType, String componentId) {
        this(uiEventType, componentId, null);
    }
    
    /**
     * 获取UI事件类型
     * @return UI事件类型
     */
    public Type getUiEventType() {
        return uiEventType;
    }
    
    /**
     * 获取组件ID
     * @return 组件ID
     */
    public String getComponentId() {
        return componentId;
    }
    
    /**
     * 获取事件数据
     * @return 事件数据
     */
    public Object getEventData() {
        return eventData;
    }
    
    @Override
    public String toString() {
        return "EditorUIEvent{" +
                "type='" + getEventType() + '\'' +
                ", uiEventType=" + uiEventType +
                ", componentId='" + componentId + '\'' +
                ", eventData=" + eventData +
                ", handled=" + isHandled() +
                '}';
    }
} 