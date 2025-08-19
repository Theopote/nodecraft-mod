package com.nodecraft.core.event;

/**
 * 编辑器事件监听器接口
 * 所有需要响应编辑器事件的组件都应实现此接口
 */
public interface EditorEventListener {
    
    /**
     * 处理一个编辑器事件
     * @param event 要处理的事件
     * @return 如果事件已被完全处理并且不应继续传播，则返回true
     */
    boolean onEvent(EditorEvent event);
    
    /**
     * 获取监听器的优先级
     * 优先级高的监听器将首先收到事件
     * @return 监听器优先级，默认为0（普通优先级）
     */
    default int getPriority() {
        return 0;
    }
    
    /**
     * 获取监听器的名称，用于日志和调试
     * @return 监听器名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * 检查此监听器是否处理指定类型的事件
     * 默认处理所有事件，子类可以覆盖此方法进行事件过滤
     * @param eventClass 事件类
     * @return 如果此监听器应处理该类型的事件，则返回true
     */
    default boolean handlesEventType(Class<? extends EditorEvent> eventClass) {
        return true;
    }
} 