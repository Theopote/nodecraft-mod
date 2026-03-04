package com.nodecraft.nodesystem.graph;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nodecraft.nodesystem.api.INode;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.io.SavedConnection;
import com.nodecraft.nodesystem.io.SavedGraph;
import com.nodecraft.nodesystem.io.SavedNode;
import com.nodecraft.nodesystem.registry.NodeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 节点图序列化/反序列化工具类。
 * 负责将 {@link NodeGraph} 转换为 JSON 字符串（通过 {@link SavedGraph}），或反向操作。
 * 此类不处理编辑器特定的状态（如节点位置），这由调用方处理。
 */
public class GraphSerializer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphSerializer.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private GraphSerializer() {
        // 工具类，不允许实例化
    }
    
    // === 序列化 ===
    
    /**
     * 将节点图转换为 SavedGraph 数据对象
     * @param graph 节点图
     * @return SavedGraph 数据对象
     */
    public static SavedGraph toSavedGraph(NodeGraph graph) {
        if (graph == null) return null;
        
        SavedGraph savedGraph = new SavedGraph();
        savedGraph.graphName = graph.getName();
        savedGraph.nodes = new ArrayList<>();
        savedGraph.connections = new ArrayList<>();
        savedGraph.nodePositions = new HashMap<>();
        
        // 保存节点
        for (INode node : graph.getNodes()) {
            if (node instanceof BaseNode baseNode) {
                SavedNode savedNode = new SavedNode();
                savedNode.nodeId = baseNode.getId().toString();
                savedNode.typeId = baseNode.getTypeId();
                savedNode.state = baseNode.getNodeState();
                savedGraph.nodes.add(savedNode);
            } else {
                LOGGER.warn("跳过保存非 BaseNode 节点: {}", node.getId());
            }
        }
        
        // 保存连接
        for (NodeGraph.Connection conn : graph.getConnections()) {
            SavedConnection savedConn = new SavedConnection();
            savedConn.sourceNodeId = conn.sourceNode.getId().toString();
            savedConn.sourcePortId = conn.sourcePort.getId();
            savedConn.targetNodeId = conn.targetNode.getId().toString();
            savedConn.targetPortId = conn.targetPort.getId();
            savedGraph.connections.add(savedConn);
        }
        
        return savedGraph;
    }
    
    /**
     * 将 SavedGraph 数据对象转换为 JSON 字符串
     * @param savedGraph 数据对象
     * @return JSON 字符串
     */
    public static String toJson(SavedGraph savedGraph) {
        return GSON.toJson(savedGraph);
    }
    
    /**
     * 将节点图直接转换为 JSON 字符串
     * @param graph 节点图
     * @return JSON 字符串
     */
    public static String toJson(NodeGraph graph) {
        return toJson(toSavedGraph(graph));
    }
    
    /**
     * 将节点图保存到文件
     * @param graph 节点图
     * @param filePath 文件路径
     * @throws IOException 如果写入失败
     */
    public static void saveToFile(NodeGraph graph, Path filePath) throws IOException {
        String json = toJson(graph);
        Files.writeString(filePath, json, StandardCharsets.UTF_8);
        LOGGER.info("节点图已保存到: {}", filePath);
    }
    
    // === 反序列化 ===
    
    /**
     * 从 JSON 字符串解析 SavedGraph
     * @param json JSON 字符串
     * @return SavedGraph 对象
     */
    public static SavedGraph fromJson(String json) {
        return GSON.fromJson(json, SavedGraph.class);
    }
    
    /**
     * 从 SavedGraph 重建节点图
     * @param savedGraph 保存的图数据
     * @return 重建的节点图，如果失败返回 null
     */
    public static NodeGraph fromSavedGraph(SavedGraph savedGraph) {
        if (savedGraph == null) return null;
        
        String graphName = savedGraph.graphName != null ? savedGraph.graphName : "Loaded Graph";
        NodeGraph graph = new NodeGraph(graphName);
        NodeRegistry registry = NodeRegistry.getInstance();
        
        // 旧 ID → 新节点实例的映射
        Map<String, BaseNode> loadedNodesMap = new HashMap<>();
        
        // 1. 重建节点
        if (savedGraph.nodes != null) {
            for (SavedNode savedNode : savedGraph.nodes) {
                INode iNode = registry.createNodeInstance(savedNode.typeId);
                if (iNode instanceof BaseNode newNode) {
                    try {
                        newNode.setNodeState(savedNode.state);
                        loadedNodesMap.put(savedNode.nodeId, newNode);
                        graph.addNode(newNode);
                    } catch (Exception e) {
                        LOGGER.error("恢复节点状态时出错: Type={}, ID={}", savedNode.typeId, savedNode.nodeId, e);
                    }
                } else {
                    LOGGER.warn("无法创建节点: Type={}, ID={}", savedNode.typeId, savedNode.nodeId);
                }
            }
        }
        
        // 2. 重建连接
        if (savedGraph.connections != null) {
            for (SavedConnection conn : savedGraph.connections) {
                BaseNode sourceNode = loadedNodesMap.get(conn.sourceNodeId);
                BaseNode targetNode = loadedNodesMap.get(conn.targetNodeId);
                
                if (sourceNode != null && targetNode != null) {
                    boolean success = graph.connect(
                        sourceNode.getId(), conn.sourcePortId,
                        targetNode.getId(), conn.targetPortId
                    );
                    if (!success) {
                        LOGGER.warn("重建连接失败: {} → {}", conn.sourcePortId, conn.targetPortId);
                    }
                }
            }
        }
        
        return graph;
    }
    
    /**
     * 从 JSON 字符串直接重建节点图
     * @param json JSON 字符串
     * @return 重建的节点图
     */
    public static NodeGraph fromJsonToGraph(String json) {
        SavedGraph savedGraph = fromJson(json);
        return fromSavedGraph(savedGraph);
    }
    
    /**
     * 从文件加载节点图
     * @param filePath 文件路径
     * @return 重建的节点图
     * @throws IOException 如果读取失败
     */
    public static NodeGraph loadFromFile(Path filePath) throws IOException {
        String json = Files.readString(filePath, StandardCharsets.UTF_8);
        return fromJsonToGraph(json);
    }
    
    /**
     * 获取旧节点ID到新节点实例的映射（用于需要恢复编辑器位置等外部状态的场景）
     * @param savedGraph 保存的图数据
     * @param graph 已重建的节点图
     * @return 旧 nodeId (String) → 新节点实例的映射
     */
    public static Map<String, BaseNode> buildNodeIdMapping(SavedGraph savedGraph, NodeGraph graph) {
        Map<String, BaseNode> mapping = new HashMap<>();
        if (savedGraph == null || savedGraph.nodes == null || graph == null) return mapping;
        
        // 按 typeId 和节点图中的顺序匹配
        var graphNodes = graph.getNodes();
        int graphIndex = 0;
        for (SavedNode savedNode : savedGraph.nodes) {
            if (graphIndex < graphNodes.size()) {
                INode node = graphNodes.get(graphIndex);
                if (node instanceof BaseNode baseNode) {
                    mapping.put(savedNode.nodeId, baseNode);
                }
                graphIndex++;
            }
        }
        
        return mapping;
    }
}