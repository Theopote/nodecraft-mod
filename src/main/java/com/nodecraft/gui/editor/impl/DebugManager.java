package com.nodecraft.gui.editor.impl;

import com.nodecraft.core.NodeCraft;

/**
 * 调试模式管理器
 * 
 * 负责管理所有UI相关的调试模式，包括配置检测、状态缓存和安全控制。
 * 使用单例模式确保全局唯一性和资源效率。
 * 
 * ### 支持的调试模式
 * - **通用调试** (`DEBUG_UI`) - 控制基础调试功能
 * - **缓存调试** (`DEBUG_UI_CACHE`) - 控制缓存操作日志
 * - **ImGui ID调试** (`DEBUG_UI_IMGUI_ID`) - 控制ID生成和碰撞检测
 * - **布局调试** (`DEBUG_UI_LAYOUT`) - 控制布局计算和参数验证
 * - **边界调试** (`DEBUG_UI_BOUNDS`) - 控制内容边界计算
 * - **直接绘制调试** (`DEBUG_UI_DIRECT`) - 控制直接绘制过程
 * 
 * ### 配置优先级
 * 1. **环境变量** - 最高优先级，推荐用于生产环境控制
 * 2. **系统属性** - 中等优先级，用于开发和测试
 * 3. **配置属性** - 最低优先级，用于持久化配置
 * 
 * ### 安全特性
 * - 默认关闭所有调试模式
 * - 首次启用时显示安全警告
 * - 自动检测生产环境并发出警报
 * - 支持运行时动态切换
 * 
 * @author NodeCraft Team
 * @since 1.21.4
 */
public final class DebugManager {
    
    /** 单例实例 */
    private static final DebugManager INSTANCE = new DebugManager();
    
    // ### 调试模式缓存字段
    
    /** 通用调试模式缓存 */
    private volatile Boolean generalDebugCache = null;
    /** 缓存调试模式缓存 */
    private volatile Boolean cacheDebugCache = null;
    /** ImGui ID调试模式缓存 */
    private volatile Boolean imguiIdDebugCache = null;
    /** 布局调试模式缓存 */
    private volatile Boolean layoutDebugCache = null;
    /** 边界调试模式缓存 */
    private volatile Boolean boundsDebugCache = null;
    /** 直接绘制调试模式缓存 */
    private volatile Boolean directDrawDebugCache = null;
    
    // ### 状态管理字段
    
    /** 调试模式启用警告是否已显示 */
    private volatile boolean debugWarningShown = false;
    /** 生产环境警告是否已显示 */
    private volatile boolean productionWarningShown = false;
    
    /**
     * 私有构造函数，防止外部实例化
     */
    private DebugManager() {
        // 单例模式，禁止外部实例化
    }
    
    /**
     * 获取单例实例
     * 
     * @return DebugManager实例
     */
    public static DebugManager getInstance() {
        return INSTANCE;
    }
    
    // ### 公共调试模式检查方法
    
    /**
     * 是否启用通用调试模式
     * 
     * @return 通用调试模式状态
     */
    public boolean isGeneralDebugEnabled() {
        if (generalDebugCache == null) {
            synchronized (this) {
                if (generalDebugCache == null) {
                    generalDebugCache = determineDebugMode("DEBUG_UI", "debug.ui", "nodecraft.debug.ui");
                    
                    // 首次启用时显示警告
                    if (generalDebugCache && !debugWarningShown) {
                        NodeCraft.LOGGER.warn("[Security Warning] UI Debug mode is ENABLED. This should be disabled in production environments.");
                        NodeCraft.LOGGER.warn("[Security Warning] Debug mode may impact performance and expose internal information.");
                        debugWarningShown = true;
                        
                        // 检查是否在生产环境中启用
                        checkProductionEnvironment();
                    }
                }
            }
        }
        return generalDebugCache;
    }
    
    /**
     * 是否启用缓存调试模式
     * 
     * @return 缓存调试模式状态
     */
    public boolean isCacheDebugEnabled() {
        if (cacheDebugCache == null) {
            synchronized (this) {
                if (cacheDebugCache == null) {
                    cacheDebugCache = determineSpecificDebugMode("CACHE", "debug.ui.cache", "nodecraft.debug.ui.cache");
                }
            }
        }
        return cacheDebugCache;
    }
    
    /**
     * 是否启用ImGui ID调试模式
     * 
     * @return ImGui ID调试模式状态
     */
    public boolean isImGuiIdDebugEnabled() {
        if (imguiIdDebugCache == null) {
            synchronized (this) {
                if (imguiIdDebugCache == null) {
                    imguiIdDebugCache = determineSpecificDebugMode("IMGUI_ID", "debug.ui.imgui.id", "nodecraft.debug.ui.imgui.id");
                }
            }
        }
        return imguiIdDebugCache;
    }
    
    /**
     * 是否启用布局调试模式
     * 
     * @return 布局调试模式状态
     */
    public boolean isLayoutDebugEnabled() {
        if (layoutDebugCache == null) {
            synchronized (this) {
                if (layoutDebugCache == null) {
                    layoutDebugCache = determineSpecificDebugMode("LAYOUT", "debug.ui.layout", "nodecraft.debug.ui.layout");
                }
            }
        }
        return layoutDebugCache;
    }
    
    /**
     * 是否启用边界调试模式
     * 
     * @return 边界调试模式状态
     */
    public boolean isBoundsDebugEnabled() {
        if (boundsDebugCache == null) {
            synchronized (this) {
                if (boundsDebugCache == null) {
                    boundsDebugCache = determineSpecificDebugMode("BOUNDS", "debug.ui.bounds", "nodecraft.debug.ui.bounds");
                }
            }
        }
        return boundsDebugCache;
    }
    
    /**
     * 是否启用直接绘制调试模式
     * 
     * @return 直接绘制调试模式状态
     */
    public boolean isDirectDrawDebugEnabled() {
        if (directDrawDebugCache == null) {
            synchronized (this) {
                if (directDrawDebugCache == null) {
                    directDrawDebugCache = determineSpecificDebugMode("DIRECT", "debug.ui.direct", "nodecraft.debug.ui.direct");
                }
            }
        }
        return directDrawDebugCache;
    }
    
    // ### 缓存管理方法
    
    /**
     * 清除所有调试模式缓存，强制重新检查配置
     * 
     * 这允许在运行时动态切换调试模式，无需重启应用程序。
     */
    public synchronized void clearCache() {
        generalDebugCache = null;
        cacheDebugCache = null;
        imguiIdDebugCache = null;
        layoutDebugCache = null;
        boundsDebugCache = null;
        directDrawDebugCache = null;
        debugWarningShown = false;
        productionWarningShown = false;
        
        NodeCraft.LOGGER.info("[Debug] Debug mode cache cleared. Debug settings will be re-evaluated on next access.");
    }
    
    // ### 状态查询方法
    
    /**
     * 获取当前调试模式的详细状态信息
     * 
     * @return 调试状态信息字符串
     */
    public String getDebugModeStatus() {
        StringBuilder status = new StringBuilder();
        status.append("Debug Mode Status:\n");
        status.append("  - General Debug: ").append(isGeneralDebugEnabled()).append("\n");
        status.append("  - Cache Debug: ").append(isCacheDebugEnabled()).append("\n");
        status.append("  - ImGui ID Debug: ").append(isImGuiIdDebugEnabled()).append("\n");
        status.append("  - Layout Debug: ").append(isLayoutDebugEnabled()).append("\n");
        status.append("  - Bounds Debug: ").append(isBoundsDebugEnabled()).append("\n");
        status.append("  - Direct Draw Debug: ").append(isDirectDrawDebugEnabled()).append("\n");
        
        // 显示配置来源
        status.append("Configuration Sources:\n");
        status.append("  - Environment Variables: DEBUG_UI, DEBUG_UI_CACHE, DEBUG_UI_IMGUI_ID, DEBUG_UI_LAYOUT, DEBUG_UI_BOUNDS, DEBUG_UI_DIRECT\n");
        status.append("  - System Properties: debug.ui, debug.ui.cache, debug.ui.imgui.id, debug.ui.layout, debug.ui.bounds, debug.ui.direct\n");
        status.append("  - Config Properties: nodecraft.debug.ui, nodecraft.debug.ui.cache, nodecraft.debug.ui.imgui.id, nodecraft.debug.ui.layout, nodecraft.debug.ui.bounds, nodecraft.debug.ui.direct\n");
        
        return status.toString();
    }
    
    /**
     * 检查是否在生产环境中意外启用了调试模式
     * 
     * @return 如果可能在生产环境中启用了调试模式，返回true
     */
    public boolean isDebugEnabledInProduction() {
        if (!isGeneralDebugEnabled()) {
            return false; // 调试模式未启用
        }
        
        return checkProductionEnvironment();
    }
    
    // ### 内部实现方法
    
    /**
     * 确定通用调试模式状态
     * 
     * @param envKey 环境变量键
     * @param sysPropKey 系统属性键
     * @param configPropKey 配置属性键
     * @return 调试模式状态
     */
    private boolean determineDebugMode(String envKey, String sysPropKey, String configPropKey) {
        // 1. 优先检查环境变量（最安全，推荐用于生产环境）
        String envValue = System.getenv(envKey);
        if (envValue != null) {
            boolean enabled = "true".equalsIgnoreCase(envValue) || "1".equals(envValue);
            if (enabled) {
                NodeCraft.LOGGER.info("[Debug] UI Debug mode enabled via environment variable {}={}", envKey, envValue);
            }
            return enabled;
        }
        
        // 2. 检查系统属性（用于开发和测试）
        String sysPropValue = System.getProperty(sysPropKey);
        if (sysPropValue != null) {
            boolean enabled = Boolean.parseBoolean(sysPropValue);
            if (enabled) {
                NodeCraft.LOGGER.info("[Debug] UI Debug mode enabled via system property {}={}", sysPropKey, sysPropValue);
            }
            return enabled;
        }
        
        // 3. 检查配置文件属性（用于持久化配置）
        String configValue = System.getProperty(configPropKey);
        if (configValue != null) {
            boolean enabled = Boolean.parseBoolean(configValue);
            if (enabled) {
                NodeCraft.LOGGER.info("[Debug] UI Debug mode enabled via config property {}={}", configPropKey, configValue);
            }
            return enabled;
        }
        
        // 4. 默认关闭
        return false;
    }
    
    /**
     * 确定特定调试模式状态
     * 
     * @param envSuffix 环境变量后缀
     * @param sysPropKey 系统属性键
     * @param configPropKey 配置属性键
     * @return 调试模式状态
     */
    private boolean determineSpecificDebugMode(String envSuffix, String sysPropKey, String configPropKey) {
        // 1. 检查环境变量
        String envKey = "DEBUG_UI_" + envSuffix;
        String envValue = System.getenv(envKey);
        if (envValue != null) {
            boolean enabled = "true".equalsIgnoreCase(envValue) || "1".equals(envValue);
            if (enabled) {
                NodeCraft.LOGGER.info("[Debug] {} debug mode enabled via environment variable {}={}", envSuffix, envKey, envValue);
            }
            return enabled;
        }
        
        // 2. 检查系统属性
        String sysPropValue = System.getProperty(sysPropKey);
        if (sysPropValue != null) {
            boolean enabled = Boolean.parseBoolean(sysPropValue);
            if (enabled) {
                NodeCraft.LOGGER.info("[Debug] {} debug mode enabled via system property {}={}", envSuffix, sysPropKey, sysPropValue);
            }
            return enabled;
        }
        
        // 3. 检查配置属性
        String configValue = System.getProperty(configPropKey);
        if (configValue != null) {
            boolean enabled = Boolean.parseBoolean(configValue);
            if (enabled) {
                NodeCraft.LOGGER.info("[Debug] {} debug mode enabled via config property {}={}", envSuffix, configPropKey, configValue);
            }
            return enabled;
        }
        
        // 4. 默认关闭
        return false;
    }
    
    /**
     * 检查生产环境并发出警告
     * 
     * @return 如果在生产环境中返回true
     */
    private boolean checkProductionEnvironment() {
        // 检查常见的生产环境指标
        String env = System.getenv("ENVIRONMENT");
        String profile = System.getProperty("spring.profiles.active");
        String nodeEnv = System.getenv("NODE_ENV");
        
        boolean isProd = "production".equalsIgnoreCase(env) ||
                        "prod".equalsIgnoreCase(env) ||
                        (profile != null && profile.toLowerCase().contains("prod")) ||
                        "production".equalsIgnoreCase(nodeEnv);
        
        if (isProd && !productionWarningShown) {
            NodeCraft.LOGGER.error("[Security Alert] Debug mode is enabled in what appears to be a PRODUCTION environment!");
            NodeCraft.LOGGER.error("[Security Alert] Environment indicators: ENVIRONMENT={}, spring.profiles.active={}, NODE_ENV={}", 
                                 env, profile, nodeEnv);
            NodeCraft.LOGGER.error("[Security Alert] Please disable debug mode in production for security and performance reasons.");
            productionWarningShown = true;
        }
        
        return isProd;
    }
}