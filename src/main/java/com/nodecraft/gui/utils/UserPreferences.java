package com.nodecraft.gui.utils;

import java.util.prefs.Preferences;
import com.nodecraft.core.NodeCraft;

/**
 * 用户首选项管理工具类
 * 负责保存和加载用户界面相关的首选项设置
 */
public class UserPreferences {
    
    // 使用Java Preferences API存储用户设置
    private static final Preferences prefs = Preferences.userNodeForPackage(NodeCraft.class);
    
    /**
     * 保存字符串设置
     * @param key 首选项键名
     * @param value 字符串值
     */
    public static void setString(String key, String value) {
        prefs.put(key, value);
        flush();
    }
    
    /**
     * 获取字符串设置
     * @param key 首选项键名
     * @param defaultValue 默认值（如果没有找到设置）
     * @return 保存的字符串值或默认值
     */
    public static String getString(String key, String defaultValue) {
        return prefs.get(key, defaultValue);
    }
    
    /**
     * 保存整数设置
     * @param key 首选项键名
     * @param value 整数值
     */
    public static void setInt(String key, int value) {
        prefs.putInt(key, value);
        flush();
    }
    
    /**
     * 获取整数设置
     * @param key 首选项键名
     * @param defaultValue 默认值（如果没有找到设置）
     * @return 保存的整数值或默认值
     */
    public static int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }
    
    /**
     * 保存浮点数设置
     * @param key 首选项键名
     * @param value 浮点数值
     */
    public static void setFloat(String key, float value) {
        prefs.putFloat(key, value);
        flush();
    }
    
    /**
     * 获取浮点数设置
     * @param key 首选项键名
     * @param defaultValue 默认值（如果没有找到设置）
     * @return 保存的浮点数值或默认值
     */
    public static float getFloat(String key, float defaultValue) {
        return prefs.getFloat(key, defaultValue);
    }
    
    /**
     * 保存布尔值设置
     * @param key 首选项键名
     * @param value 布尔值
     */
    public static void setBoolean(String key, boolean value) {
        prefs.putBoolean(key, value);
        flush();
    }
    
    /**
     * 获取布尔值设置
     * @param key 首选项键名
     * @param defaultValue 默认值（如果没有找到设置）
     * @return 保存的布尔值或默认值
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }
    
    /**
     * 删除指定的首选项
     * @param key 要删除的首选项键名
     */
    public static void remove(String key) {
        prefs.remove(key);
        flush();
    }
    
    /**
     * 同步首选项到持久化存储
     * 在每次设置后自动调用
     */
    private static void flush() {
        try {
            prefs.flush();
        } catch (Exception e) {
            NodeCraft.LOGGER.error("保存用户首选项时出错: {}", e.getMessage());
        }
    }
} 