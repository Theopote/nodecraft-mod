package com.nodecraft.minecraft.registry;

import java.util.List;
import java.util.Optional;

/**
 * Minecraft注册表接口，提供对游戏内部数据的访问
 */
public interface MinecraftRegistry<T> {
    /**
     * 获取注册表中的所有键
     * @return 键列表
     */
    List<ResourceLocation> getKeys();
    
    /**
     * 根据键获取值
     * @param key 键
     * @return 值（如果存在）
     */
    Optional<T> getValue(ResourceLocation key);
    
    /**
     * 根据键获取值
     * @param keyStr 键字符串 (形如 "minecraft:stone")
     * @return 值（如果存在）
     */
    default Optional<T> getValue(String keyStr) {
        return getValue(ResourceLocation.fromString(keyStr));
    }
    
    /**
     * 检查键是否存在
     * @param key 键
     * @return 是否存在
     */
    boolean containsKey(ResourceLocation key);
    
    /**
     * 检查键是否存在
     * @param keyStr 键字符串
     * @return 是否存在
     */
    default boolean containsKey(String keyStr) {
        return containsKey(ResourceLocation.fromString(keyStr));
    }
} 