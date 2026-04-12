package com.nodecraft.nodesystem.nodes.reference.points;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Closest Point 节点: 在一个 Coordinate 列表中找到距离另一个点最近的点
 */
@NodeInfo(
    id = "reference.points.closest_point",
    displayName = "最近点",
    description = "在坐标列表中找到距离指定点最近的点",
    category = "reference.points",
    order = 7
)
public class ClosestPointNode extends BaseNode {

    // --- 节点属性 ---
    private String description = "在坐标列表中找到距离指定点最近的点";

    // --- 输入端口 IDs ---
    private static final String INPUT_POINT_ID = "input_point";
    private static final String INPUT_COORDINATES_ID = "input_coordinates";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_CLOSEST_POINT_ID = "output_closest_point";
    private static final String OUTPUT_DISTANCE_ID = "output_distance";
    private static final String OUTPUT_INDEX_ID = "output_index";

    // --- 构造函数 ---
    public ClosestPointNode() {
        super(UUID.randomUUID(), "reference.points.closest_point");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_POINT_ID, "Point", 
                "参考点", NodeDataType.BLOCK_POS, this));
        addInputPort(new BasePort(INPUT_COORDINATES_ID, "Coordinates", 
                "坐标列表", NodeDataType.BLOCK_LIST, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_CLOSEST_POINT_ID, "Closest Point", 
                "最近的点", NodeDataType.BLOCK_POS, this));
        addOutputPort(new BasePort(OUTPUT_DISTANCE_ID, "Distance", 
                "最近点到参考点的距离", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_INDEX_ID, "Index", 
                "最近点在坐标列表中的索引", NodeDataType.INTEGER, this));
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object pointObj = inputValues.get(INPUT_POINT_ID);
        Object coordinatesObj = inputValues.get(INPUT_COORDINATES_ID);
        
        // 检查输入是否合法
        if (pointObj instanceof BlockPos && coordinatesObj instanceof BlockPosList) {
            BlockPos referencePoint = (BlockPos) pointObj;
            BlockPosList coordinates = (BlockPosList) coordinatesObj;
            
            // 如果坐标列表不为空，查找最近点
            if (!coordinates.isEmpty()) {
                // 初始化最小距离为最大值
                double minDistanceSquared = Double.MAX_VALUE;
                BlockPos closestPoint = null;
                int closestIndex = -1;
                
                // 遍历所有坐标，寻找距离最小的点
                int index = 0;
                for (BlockPos pos : coordinates) {
                    double distanceSquared = calculateDistanceSquared(referencePoint, pos);
                    if (distanceSquared < minDistanceSquared) {
                        minDistanceSquared = distanceSquared;
                        closestPoint = pos;
                        closestIndex = index;
                    }
                    index++;
                }
                
                // 设置输出值
                if (closestPoint != null) {
                    outputValues.put(OUTPUT_CLOSEST_POINT_ID, closestPoint);
                    outputValues.put(OUTPUT_DISTANCE_ID, Math.sqrt(minDistanceSquared));
                    outputValues.put(OUTPUT_INDEX_ID, closestIndex);
                    return;
                }
            }
        }
        
        // 如果没有有效输入或找不到最近点，清除所有输出
        outputValues.clear();
    }
    
    /**
     * 计算两点之间的距离平方
     * @param p1 第一个点
     * @param p2 第二个点
     * @return 距离平方
     */
    private double calculateDistanceSquared(BlockPos p1, BlockPos p2) {
        double dx = p1.getX() - p2.getX();
        double dy = p1.getY() - p2.getY();
        double dz = p1.getZ() - p2.getZ();
        return dx * dx + dy * dy + dz * dz;
    }
} 
