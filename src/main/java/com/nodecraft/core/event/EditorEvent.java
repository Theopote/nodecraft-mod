package com.nodecraft.core.event;

/**
 * 编辑器事件基类
 * 所有编辑器中发生的事件都应该继承自这个类
 */
public class EditorEvent {
    private boolean handled = false;
    private final String eventType;
    private final long timestamp;
    
    /**
     * 创建一个编辑器事件
     * @param eventType 事件类型，建议使用子类的简单类名
     */
    public EditorEvent(String eventType) {
        this.eventType = eventType;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 简化构造函数，自动使用类名作为事件类型
     */
    public EditorEvent() {
        this(EditorEvent.class.getSimpleName());
    }
    
    /**
     * 获取事件类型
     * @return 事件类型
     */
    public String getEventType() {
        return eventType;
    }
    
    /**
     * 获取事件发生的时间戳
     * @return 事件时间戳（毫秒）
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * 检查事件是否已被处理
     * @return 如果事件已被标记为已处理，则返回true
     */
    public boolean isHandled() {
        return handled;
    }
    
    /**
     * 将事件标记为已处理
     * 这通常表示事件已被某个监听器处理，可能不需要继续传播
     */
    public void setHandled() {
        this.handled = true;
    }
    
    @Override
    public String toString() {
        return "EditorEvent{" +
                "type='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                ", handled=" + handled +
                '}';
    }
} 