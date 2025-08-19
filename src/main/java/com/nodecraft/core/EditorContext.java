package com.nodecraft.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于存储和检索编辑器相关状态的静态上下文。
 */
public final class EditorContext {

    // 使用 ConcurrentHashMap 保证线程安全
    private static final Map<String, Object> state = new ConcurrentHashMap<>();

    // 私有构造函数防止实例化
    private EditorContext() {}

    /**
     * 存储一个状态值。
     *
     * @param key   状态的键。
     * @param value 状态的值。
     */
    public static void putState(String key, Object value) {
        if (key == null) {
            NodeCraft.LOGGER.error("EditorContext key cannot be null");
            return;
        }
        if (value == null) {
            state.remove(key); // 如果值为 null，则移除键
            // 或者记录警告: NodeCraft.LOGGER.warn("Storing null value for key '" + key + "' in EditorContext");
        } else {
             state.put(key, value);
        }
    }

    /**
     * 检索一个状态值。
     *
     * @param key  状态的键。
     * @param type 期望的值类型。
     * @param <T>  值的类型。
     * @return 存储的值，如果键不存在或类型不匹配则返回 null。
     */
    public static <T> T getState(String key, Class<T> type) {
        if (key == null || type == null) {
            return null;
        }
        Object value = state.get(key);
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return type.cast(value);
        } else {
            NodeCraft.LOGGER.warn("Type mismatch in EditorContext for key '" + key +
                                  "'. Expected " + type.getName() + " but got " + value.getClass().getName());
            return null;
        }
    }

    /**
     * 检索一个状态值，如果不存在或类型不匹配则返回默认值。
     *
     * @param key          状态的键。
     * @param type         期望的值类型。
     * @param defaultValue 如果未找到或类型不匹配时返回的默认值。
     * @param <T>          值的类型。
     * @return 存储的值或默认值。
     */
    public static <T> T getStateOrDefault(String key, Class<T> type, T defaultValue) {
        T value = getState(key, type);
        return (value != null) ? value : defaultValue;
    }

    /**
     * 移除一个状态值。
     *
     * @param key 要移除的状态的键。
     */
    public static void removeState(String key) {
        if (key != null) {
            state.remove(key);
        }
    }

    /**
     * 清除所有状态。
     * 谨慎使用。
     */
    public static void clearAllState() {
        state.clear();
    }
}
