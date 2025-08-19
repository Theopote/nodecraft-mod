package com.nodecraft.nodesystem.nodes.spatial.generators;

import com.nodecraft.nodesystem.registry.NodeRegistry;
import com.nodecraft.gui.node.NodeInfo;

/**
 * 注册空间形状生成器相关节点
 */
public class SpatialGeneratorNodes {
    
    /**
     * 注册所有空间形状生成器相关节点
     */
    public static void registerNodes() {
        NodeRegistry registry = NodeRegistry.getInstance();
        
        // 注册空间形状生成器节点
        registry.registerNode(new NodeInfo("spatial.generators.lineblocks", "Line Blocks", "", "spatial.generators", LineBlocksNode.class));
        registry.registerNode(new NodeInfo("spatial.generators.rectangleblocks", "Rectangle Blocks", "", "spatial.generators", RectangleBlocksNode.class));
        registry.registerNode(new NodeInfo("spatial.generators.boxblocks", "Box Blocks", "", "spatial.generators", BoxBlocksNode.class));
        registry.registerNode(new NodeInfo("spatial.generators.circlesphereblocks", "Circle Sphere Blocks", "", "spatial.generators", CircleSphereBlocksNode.class));
        registry.registerNode(new NodeInfo("spatial.generators.cylinderblocks", "Cylinder Blocks", "", "spatial.generators", CylinderBlocksNode.class));
        registry.registerNode(new NodeInfo("spatial.generators.polylineblocks", "Polyline Blocks", "", "spatial.generators", PolylineBlocksNode.class));
        registry.registerNode(new NodeInfo("spatial.generators.curveblocks", "Curve Blocks", "", "spatial.generators", CurveBlocksNode.class));
    }
} 