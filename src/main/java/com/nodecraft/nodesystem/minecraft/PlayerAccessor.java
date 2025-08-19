package com.nodecraft.nodesystem.minecraft;

import com.nodecraft.nodesystem.util.Vector3;

/**
 * 提供访问玩家数据的接口
 */
public interface PlayerAccessor {
    
    /**
     * 获取玩家当前位置（脚底中心点）
     * @return 玩家位置向量
     */
    Vector3 getPlayerPosition();
    
    /**
     * 获取玩家眼睛位置
     * @return 玩家眼睛位置向量
     */
    Vector3 getPlayerEyePosition();
    
    /**
     * 获取玩家朝向向量
     * @return 玩家视线朝向向量（单位向量）
     */
    Vector3 getPlayerLookVector();
    
    /**
     * 获取玩家所在维度ID
     * @return 维度ID字符串
     */
    String getPlayerDimension();
    
    /**
     * 获取玩家所在生物群系ID
     * @return 生物群系ID字符串
     */
    String getPlayerBiome();
    
    /**
     * 获取玩家世界的当前游戏时间
     * @return 游戏时间
     */
    long getWorldTime();
    
    /**
     * 获取玩家世界的当前日期（天数）
     * @return 世界的天数
     */
    int getWorldDay();
    
    /**
     * 检查当前是否为白天
     * @return 是否为白天
     */
    boolean isDaytime();
    
    /**
     * 检查当前是否在下雨
     * @return 是否在下雨
     */
    boolean isRaining();
    
    /**
     * 检查当前是否在打雷
     * @return 是否在打雷
     */
    boolean isThundering();
} 