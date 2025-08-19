package com.nodecraft.gui.editor.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.nodecraft.nodesystem.api.INode;
import com.nodecraft.nodesystem.api.IPort;

import imgui.ImVec2;

/**
 * 高效的端口位置管理器
 * 优化端口位置存储，减少重复计算和内存分配
 */
public class PortPositionManager {
    
    /**
     * 节点状态信息，用于检测变化
     */
    private static class NodeState {
        final float x, y;           // 节点位置
        final float zoom;           // 缩放级别
        final float offsetX, offsetY; // 画布偏移
        final int portCount;        // 端口数量（用于检测端口变化）
        final long lastUpdateFrame; // 最后更新帧
        
        NodeState(float x, float y, float zoom, float offsetX, float offsetY, int portCount, long frame) {
            this.x = x;
            this.y = y;
            this.zoom = zoom;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.portCount = portCount;
            this.lastUpdateFrame = frame;
        }
        
        /**
         * 检查是否需要更新
         */
        boolean needsUpdate(float newX, float newY, float newZoom, float newOffsetX, float newOffsetY, 
                           int newPortCount, long currentFrame) {
            // 检查位置变化（使用小的容差值避免浮点精度问题）
            final float EPSILON = 0.001f;
            return Math.abs(x - newX) > EPSILON ||
                   Math.abs(y - newY) > EPSILON ||
                   Math.abs(zoom - newZoom) > EPSILON ||
                   Math.abs(offsetX - newOffsetX) > EPSILON ||
                   Math.abs(offsetY - newOffsetY) > EPSILON ||
                   portCount != newPortCount ||
                   (currentFrame - lastUpdateFrame) > MAX_CACHE_AGE_FRAMES;
        }
    }
    
    /**
     * 端口位置信息
     */
    public static class PortPositions {
        final Map<String, ImVec2> positions;
        
        PortPositions() {
            this.positions = new HashMap<>();
        }
        
        PortPositions(int expectedSize) {
            this.positions = new HashMap<>(expectedSize);
        }
        
        public void setPosition(String portId, float x, float y) {
            positions.put(portId, new ImVec2(x, y));
        }
        
        public ImVec2 getPosition(String portId) {
            return positions.get(portId);
        }
        
        public void clear() {
            positions.clear();
        }
        
        public boolean isEmpty() {
            return positions.isEmpty();
        }
        
        public int size() {
            return positions.size();
        }
    }
    
    // 缓存设置
    private static final int MAX_CACHE_SIZE = 1000;
    private static final long MAX_CACHE_AGE_FRAMES = 300; // 5秒@60fps
    
    // 节点状态缓存
    private final Map<UUID, NodeState> nodeStates = new ConcurrentHashMap<>();
    
    // 端口位置缓存
    private final Map<UUID, PortPositions> portPositions = new ConcurrentHashMap<>();
    
    // 帧计数器
    private long currentFrame = 0;
    
    // 缓存清理计数器
    private int cleanupCounter = 0;
    private static final int CLEANUP_INTERVAL = 100;
    
    /**
     * 更新帧计数器
     */
    public void nextFrame() {
        currentFrame++;
        
        // 定期清理过期缓存
        if (++cleanupCounter >= CLEANUP_INTERVAL) {
            cleanupCounter = 0;
            cleanupExpiredEntries();
        }
    }
    
    /**
     * 检查节点是否需要更新端口位置
     */
    public boolean needsUpdate(UUID nodeId, NodePosition nodePos, float zoom, float offsetX, float offsetY, INode node) {
        NodeState state = nodeStates.get(nodeId);
        if (state == null) {
            return true; // 首次计算
        }
        
        int portCount = node.getInputPorts().size() + node.getOutputPorts().size();
        return state.needsUpdate(nodePos.x, nodePos.y, zoom, offsetX, offsetY, portCount, currentFrame);
    }
    
    /**
     * 更新节点的端口位置
     */
    public void updateNodePortPositions(UUID nodeId, NodePosition nodePos, float zoom, float offsetX, float offsetY, 
                                       INode node, PortPositionCalculator calculator) {
        // 更新节点状态
        int portCount = node.getInputPorts().size() + node.getOutputPorts().size();
        nodeStates.put(nodeId, new NodeState(nodePos.x, nodePos.y, zoom, offsetX, offsetY, portCount, currentFrame));
        
        // 获取或创建端口位置容器
        PortPositions positions = portPositions.computeIfAbsent(nodeId, k -> new PortPositions(portCount));
        positions.clear();
        
        // 计算端口位置
        calculator.calculatePortPositions(nodeId, node, positions);
    }
    
    /**
     * 获取端口位置
     */
    public ImVec2 getPortPosition(UUID nodeId, String portId) {
        PortPositions positions = portPositions.get(nodeId);
        return positions != null ? positions.getPosition(portId) : null;
    }
    
    /**
     * 获取节点的所有端口位置
     */
    public Map<String, ImVec2> getNodePortPositions(UUID nodeId) {
        PortPositions positions = portPositions.get(nodeId);
        return positions != null ? positions.positions : null;
    }
    
    /**
     * 检查是否有端口位置数据
     */
    public boolean hasPortPositions(UUID nodeId) {
        PortPositions positions = portPositions.get(nodeId);
        return positions != null && !positions.isEmpty();
    }
    
    /**
     * 清理指定节点的缓存
     */
    public void clearNode(UUID nodeId) {
        nodeStates.remove(nodeId);
        portPositions.remove(nodeId);
    }
    
    /**
     * 清理所有缓存
     */
    public void clearAll() {
        nodeStates.clear();
        portPositions.clear();
        currentFrame = 0;
        cleanupCounter = 0;
    }
    
    /**
     * 清理过期的缓存条目
     */
    private void cleanupExpiredEntries() {
        // 如果缓存过大，清理最旧的条目
        if (nodeStates.size() > MAX_CACHE_SIZE) {
            // 找到最旧的条目并清理
            UUID oldestNodeId = null;
            long oldestFrame = currentFrame;
            
            for (Map.Entry<UUID, NodeState> entry : nodeStates.entrySet()) {
                if (entry.getValue().lastUpdateFrame < oldestFrame) {
                    oldestFrame = entry.getValue().lastUpdateFrame;
                    oldestNodeId = entry.getKey();
                }
            }
            
            if (oldestNodeId != null) {
                clearNode(oldestNodeId);
            }
        }
        
        // 清理过期条目
        nodeStates.entrySet().removeIf(entry -> 
            (currentFrame - entry.getValue().lastUpdateFrame) > MAX_CACHE_AGE_FRAMES);
        
        // 清理对应的端口位置
        portPositions.entrySet().removeIf(entry -> !nodeStates.containsKey(entry.getKey()));
    }
    
    /**
     * 获取缓存统计信息
     */
    public String getCacheStats() {
        return String.format("Port Position Cache: %d nodes, %d port maps, Frame: %d", 
                nodeStates.size(), portPositions.size(), currentFrame);
    }
    
    /**
     * 端口位置计算器接口
     */
    @FunctionalInterface
    public interface PortPositionCalculator {
        void calculatePortPositions(UUID nodeId, INode node, PortPositions positions);
    }
    
    /**
     * 兼容性方法：转换为传统的嵌套Map格式
     * 仅在需要与现有代码兼容时使用
     */
    public Map<UUID, Map<String, ImVec2>> toNestedMap() {
        Map<UUID, Map<String, ImVec2>> result = new HashMap<>();
        for (Map.Entry<UUID, PortPositions> entry : portPositions.entrySet()) {
            result.put(entry.getKey(), new HashMap<>(entry.getValue().positions));
        }
        return result;
    }
    
    /**
     * 从传统格式导入数据（用于迁移）
     */
    public void fromNestedMap(Map<UUID, Map<String, ImVec2>> nestedMap) {
        clearAll();
        for (Map.Entry<UUID, Map<String, ImVec2>> nodeEntry : nestedMap.entrySet()) {
            UUID nodeId = nodeEntry.getKey();
            PortPositions positions = new PortPositions(nodeEntry.getValue().size());
            for (Map.Entry<String, ImVec2> portEntry : nodeEntry.getValue().entrySet()) {
                ImVec2 pos = portEntry.getValue();
                positions.setPosition(portEntry.getKey(), pos.x, pos.y);
            }
            portPositions.put(nodeId, positions);
        }
    }
} 