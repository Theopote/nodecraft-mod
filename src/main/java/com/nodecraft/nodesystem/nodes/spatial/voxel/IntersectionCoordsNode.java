package com.nodecraft.nodesystem.nodes.spatial.voxel;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Intersection (Coords) 节点: 计算两个 Coordinate 列表的交集
 */
@NodeInfo(
    id = "spatial.voxel.intersection_coords",
    displayName = "坐标交集",
    description = "计算两个坐标列表的交集，返回共同的坐标",
    category = "spatial.voxel"
)
public class IntersectionCoordsNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_COORDS_A_ID = "input_coords_a";
    private static final String INPUT_COORDS_B_ID = "input_coords_b";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_INTERSECTION_ID = "output_intersection";
    private static final String OUTPUT_COUNT_ID = "output_count";
    private static final String OUTPUT_OVERLAP_RATIO_ID = "output_overlap_ratio";

    // --- 构造函数 ---
    public IntersectionCoordsNode() {
        super(UUID.randomUUID(), "spatial.voxel.intersection_coords");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_COORDS_A_ID, "Coordinates A", 
                "第一个坐标列表", NodeDataType.BLOCK_LIST, this));
        addInputPort(new BasePort(INPUT_COORDS_B_ID, "Coordinates B", 
                "第二个坐标列表", NodeDataType.BLOCK_LIST, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_INTERSECTION_ID, "Intersection", 
                "交集结果列表", NodeDataType.BLOCK_LIST, this));
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Count", 
                "交集坐标数量", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_OVERLAP_RATIO_ID, "Overlap Ratio", 
                "交集占A列表的比例(0-1)", NodeDataType.DOUBLE, this));
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "获取两个坐标列表的交集";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "Intersection (Coords)";
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object coordsAObj = inputValues.get(INPUT_COORDS_A_ID);
        Object coordsBObj = inputValues.get(INPUT_COORDS_B_ID);
        
        // 创建结果列表
        BlockPosList resultList = new BlockPosList();
        double overlapRatio = 0.0;
        
        // 检查输入是否为坐标列表
        if (coordsAObj instanceof BlockPosList && coordsBObj instanceof BlockPosList) {
            BlockPosList coordsA = (BlockPosList) coordsAObj;
            BlockPosList coordsB = (BlockPosList) coordsBObj;
            
            // 如果两个列表都不为空，计算交集
            if (!coordsA.isEmpty() && !coordsB.isEmpty()) {
                // 选择较小的列表进行迭代，较大的列表用于集合查询
                BlockPosList smallerList, largerList;
                if (coordsA.size() <= coordsB.size()) {
                    smallerList = coordsA;
                    largerList = coordsB;
                } else {
                    smallerList = coordsB;
                    largerList = coordsA;
                }
                
                // 将较大的列表转换为HashSet以便快速查找
                Set<BlockPos> largerSet = new HashSet<>();
                for (BlockPos pos : largerList) {
                    largerSet.add(pos.toImmutable());
                }
                
                // 计算交集
                for (BlockPos pos : smallerList) {
                    if (largerSet.contains(pos)) {
                        resultList.add(pos);
                    }
                }
                
                // 计算重叠比例
                if (!coordsA.isEmpty()) {
                    overlapRatio = (double) resultList.size() / coordsA.size();
                }
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_INTERSECTION_ID, resultList);
        outputValues.put(OUTPUT_COUNT_ID, resultList.size());
        outputValues.put(OUTPUT_OVERLAP_RATIO_ID, overlapRatio);
    }
}