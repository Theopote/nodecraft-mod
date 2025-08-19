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
 * Difference (Coords) 节点: 计算两个 Coordinate 列表的差集
 */
@NodeInfo(
    id = "spatial.voxel.difference_coords",
    displayName = "坐标差集",
    description = "计算两个坐标列表的差集，返回第一个列表中不在第二个列表中的坐标",
    category = "spatial.voxel"
)
public class DifferenceCoordsNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_COORDS_A_ID = "input_coords_a";
    private static final String INPUT_COORDS_B_ID = "input_coords_b";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_DIFFERENCE_ID = "output_difference";
    private static final String OUTPUT_COUNT_ID = "output_count";
    private static final String OUTPUT_REMOVED_COUNT_ID = "output_removed_count";

    // --- 构造函数 ---
    public DifferenceCoordsNode() {
        super(UUID.randomUUID(), "spatial.voxel.difference_coords");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_COORDS_A_ID, "Coordinates A", 
                "基础坐标列表", NodeDataType.BLOCK_LIST, this));
        addInputPort(new BasePort(INPUT_COORDS_B_ID, "Coordinates B", 
                "要移除的坐标列表", NodeDataType.BLOCK_LIST, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_DIFFERENCE_ID, "Difference", 
                "差集结果列表", NodeDataType.BLOCK_LIST, this));
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Count", 
                "结果坐标数量", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_REMOVED_COUNT_ID, "Removed Count", 
                "被移除的坐标数量", NodeDataType.INTEGER, this));
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "从第一个坐标列表中移除第二个列表中的点";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "Difference (Coords)";
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object coordsAObj = inputValues.get(INPUT_COORDS_A_ID);
        Object coordsBObj = inputValues.get(INPUT_COORDS_B_ID);
        
        // 创建结果列表和计数器
        BlockPosList resultList = new BlockPosList();
        int removedCount = 0;
        
        // 检查输入是否为坐标列表
        if (coordsAObj instanceof BlockPosList) {
            BlockPosList coordsA = (BlockPosList) coordsAObj;
            
            // 如果没有第二个列表，结果就是第一个列表的副本
            if (!(coordsBObj instanceof BlockPosList)) {
                resultList.addAll(coordsA.getPositions());
            } else {
                // 将B列表转换为HashSet以便快速查找
                BlockPosList coordsB = (BlockPosList) coordsBObj;
                Set<BlockPos> subtractSet = new HashSet<>();
                
                for (BlockPos pos : coordsB) {
                    subtractSet.add(pos.toImmutable());
                }
                
                // 计算差集
                int originalSize = coordsA.size();
                for (BlockPos pos : coordsA) {
                    if (!subtractSet.contains(pos)) {
                        resultList.add(pos);
                    }
                }
                
                // 计算移除的数量
                removedCount = originalSize - resultList.size();
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_DIFFERENCE_ID, resultList);
        outputValues.put(OUTPUT_COUNT_ID, resultList.size());
        outputValues.put(OUTPUT_REMOVED_COUNT_ID, removedCount);
    }
} 