package com.nodecraft.core.debug;

import com.nodecraft.core.NodeCraft;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.Version;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.Platform;

/**
 * 系统信息收集工具类，用于收集和记录系统相关信息，帮助诊断问题
 */
public class SystemInfo {
    
    /**
     * 记录系统信息到日志
     */
    public static void logSystemInfo() {
        NodeCraft.LOGGER.info("============== 系统信息 ==============");
        logJavaInfo();
        logOSInfo();
        logLWJGLInfo();
        logGPUInfo();
        logMemoryInfo();
        NodeCraft.LOGGER.info("======================================");
    }
    
    /**
     * 记录Java相关信息
     */
    private static void logJavaInfo() {
        NodeCraft.LOGGER.info("Java版本: {}", System.getProperty("java.version"));
        NodeCraft.LOGGER.info("Java供应商: {}", System.getProperty("java.vendor"));
        NodeCraft.LOGGER.info("Java主目录: {}", System.getProperty("java.home"));
    }
    
    /**
     * 记录操作系统相关信息
     */
    private static void logOSInfo() {
        NodeCraft.LOGGER.info("操作系统: {} {} {}", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
        NodeCraft.LOGGER.info("当前平台: {}", Platform.get().getName());
    }
    
    /**
     * 记录LWJGL相关信息
     */
    private static void logLWJGLInfo() {
        NodeCraft.LOGGER.info("LWJGL版本: {}", Version.getVersion());
    }
    
    /**
     * 记录GPU相关信息
     */
    private static void logGPUInfo() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.getWindow() != null) {
                String vendor = GL11.glGetString(GL11.GL_VENDOR);
                String renderer = GL11.glGetString(GL11.GL_RENDERER);
                String version = GL11.glGetString(GL11.GL_VERSION);

                NodeCraft.LOGGER.info("GPU厂商: {}", vendor != null ? vendor : "未知");
                NodeCraft.LOGGER.info("GPU渲染器: {}", renderer != null ? renderer : "未知");
                NodeCraft.LOGGER.info("OpenGL版本: {}", version != null ? version : "未知");
            } else {
                NodeCraft.LOGGER.info("无法获取GPU信息: Minecraft客户端或窗口未初始化");
            }
        } catch (Exception e) {
            NodeCraft.LOGGER.error("获取GPU信息时发生错误", e);
        }
    }
    
    /**
     * 记录内存相关信息
     */
    private static void logMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;

        NodeCraft.LOGGER.info("最大内存: {} MB", maxMemory);
        NodeCraft.LOGGER.info("已分配内存: {} MB", totalMemory);
        NodeCraft.LOGGER.info("已使用内存: {} MB", usedMemory);
        NodeCraft.LOGGER.info("可用处理器核心数: {}", runtime.availableProcessors());
    }
} 