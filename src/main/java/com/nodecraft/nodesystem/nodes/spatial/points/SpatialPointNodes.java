package com.nodecraft.nodesystem.nodes.spatial.points;

import com.nodecraft.nodesystem.registry.NodeRegistry;
import com.nodecraft.gui.node.NodeInfo;

/**
 * 注册空间坐标点相关节点
 */
public class SpatialPointNodes {
    
    /**
     * 注册所有空间坐标点相关节点
     */
    public static void registerNodes() {
        NodeRegistry registry = NodeRegistry.getInstance();
        
        // 注册坐标操作节点
        registry.registerNode(new NodeInfo("spatial.points.offsetcoordinates", "Offset Coordinates", "", "spatial.points", OffsetCoordinatesNode.class));
        registry.registerNode(new NodeInfo("spatial.points.rotatecoordinates", "Rotate Coordinates", "", "spatial.points", RotateCoordinatesNode.class));
        registry.registerNode(new NodeInfo("spatial.points.scalecoordinates", "Scale Coordinates", "", "spatial.points", ScaleCoordinatesNode.class));
        registry.registerNode(new NodeInfo("spatial.points.mirrorcoordinates", "Mirror Coordinates", "", "spatial.points", MirrorCoordinatesNode.class));
        registry.registerNode(new NodeInfo("spatial.points.randomizecoordinates", "Randomize Coordinates", "", "spatial.points", RandomizeCoordinatesNode.class));
    }
} 