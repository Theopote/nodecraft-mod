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
 * Union (Coords) 节点: 合并两个 Coordinate 列表
 */
@NodeInfo(
    id = "spatial.voxel.union_coords",
    displayName = "坐标合并",
    description = "合并两个坐标列表，去除重复项",
    category = "spatial.voxel"
)
public class UnionCoordsNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_COORDS_A_ID = "input_coords_a";
    private static final String INPUT_COORDS_B_ID = "input_coords_b";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_UNION_ID = "output_union";
    private static final String OUTPUT_COUNT_ID = "output_count";

    // --- 构造函数 ---
    public UnionCoordsNode() {
        super(UUID.randomUUID(), "spatial.voxel.union_coords");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_COORDS_A_ID, "Coordinates A", 
                "第一个坐标列表", NodeDataType.BLOCK_LIST, this));
        addInputPort(new BasePort(INPUT_COORDS_B_ID, "Coordinates B", 
                "第二个坐标列表", NodeDataType.BLOCK_LIST, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_UNION_ID, "Union", 
                "合并后的坐标列表", NodeDataType.BLOCK_LIST, this));
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Count", 
                "合并后的坐标数量", NodeDataType.INTEGER, this));
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "合并两个坐标列表";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "Union (Coords)";
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object coordsAObj = inputValues.get(INPUT_COORDS_A_ID);
        Object coordsBObj = inputValues.get(INPUT_COORDS_B_ID);
        
        // 创建结果列表
        BlockPosList resultList = new BlockPosList();
        
        // 检查输入是否为坐标列表
        if (coordsAObj instanceof BlockPosList || coordsBObj instanceof BlockPosList) {
            // 使用HashSet来确保不重复
            Set<BlockPos> unionSet = new HashSet<>();
            
            // 处理第一个坐标列表
            if (coordsAObj instanceof BlockPosList) {
                BlockPosList coordsA = (BlockPosList) coordsAObj;
                for (BlockPos pos : coordsA) {
                    unionSet.add(pos.toImmutable());
                }
            }
            
            // 处理第二个坐标列表
            if (coordsBObj instanceof BlockPosList) {
                BlockPosList coordsB = (BlockPosList) coordsBObj;
                for (BlockPos pos : coordsB) {
                    unionSet.add(pos.toImmutable());
                }
            }
            
            // 将结果集合转换为列表
            for (BlockPos pos : unionSet) {
                resultList.add(pos);
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_UNION_ID, resultList);
        outputValues.put(OUTPUT_COUNT_ID, resultList.size());
    }
} 