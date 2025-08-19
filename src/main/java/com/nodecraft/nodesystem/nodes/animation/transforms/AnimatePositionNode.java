package com.nodecraft.nodesystem.nodes.animation.transforms;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Animate Position: 位置动画节点
 * 使方块随时间移动，可用于创建平滑的移动效果
 */
@NodeInfo(
    id = "animation.properties.animate_position",
    displayName = "Animate Position",
    description = "使方块随时间移动",
    category = "animation.properties"
)
public class AnimatePositionNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_COORDINATES_ID = "input_coordinates";
    private static final String INPUT_START_POSITION_ID = "input_start_position";
    private static final String INPUT_END_POSITION_ID = "input_end_position";
    private static final String INPUT_TIME_FACTOR_ID = "input_time_factor";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_ANIMATED_COORDINATES_ID = "output_animated_coordinates";

    // --- 构造函数 ---
    public AnimatePositionNode() {
        super(UUID.randomUUID(), "animation.properties.animate_position");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_COORDINATES_ID, "Coordinates", "要移动的方块坐标列表", NodeDataType.BLOCK_LIST, this));
        addInputPort(new BasePort(INPUT_START_POSITION_ID, "Start Position", "起始位置向量", NodeDataType.VECTOR, this));
        addInputPort(new BasePort(INPUT_END_POSITION_ID, "End Position", "结束位置向量", NodeDataType.VECTOR, this));
        addInputPort(new BasePort(INPUT_TIME_FACTOR_ID, "Time Factor", "时间因子 (0-1)", NodeDataType.FLOAT, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_ANIMATED_COORDINATES_ID, "Animated Coordinates", "随时间变化的方块坐标列表", NodeDataType.BLOCK_LIST, this));
    }
    
    @Override
    public String getDescription() {
        return "使方块随时间移动";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        BlockPosList coordinates = (BlockPosList) inputValues.get(INPUT_COORDINATES_ID);
        Vector3d startPosition = (Vector3d) inputValues.get(INPUT_START_POSITION_ID);
        Vector3d endPosition = (Vector3d) inputValues.get(INPUT_END_POSITION_ID);
        Float timeFactor = (Float) inputValues.getOrDefault(INPUT_TIME_FACTOR_ID, 0.0f);
        
        // 确保时间因子在[0,1]范围内
        timeFactor = Math.max(0f, Math.min(1f, timeFactor));
        
        // 如果任何必要输入为null，则输出null
        if (coordinates == null || startPosition == null || endPosition == null) {
            outputValues.put(OUTPUT_ANIMATED_COORDINATES_ID, null);
            return;
        }
        
        // 计算当前位置向量（插值）
        Vector3d currentPosition = lerpVector3d(startPosition, endPosition, timeFactor);
        
        // 计算位移向量（从起始位置到当前位置）
        Vector3d displacement = new Vector3d(
            currentPosition.x - startPosition.x,
            currentPosition.y - startPosition.y,
            currentPosition.z - startPosition.z
        );
        
        // 应用位移到每个方块坐标
        List<BlockPos> originalCoordinates = coordinates.getPositions();
        List<BlockPos> animatedCoordinates = new ArrayList<>(originalCoordinates.size());
        
        for (BlockPos pos : originalCoordinates) {
            // 计算新位置（加上位移）
            BlockPos newPos = new BlockPos(
                (int) Math.round(pos.getX() + displacement.x),
                (int) Math.round(pos.getY() + displacement.y),
                (int) Math.round(pos.getZ() + displacement.z)
            );
            animatedCoordinates.add(newPos);
        }
        
        // 创建新的BlockPosList并设置为输出
        BlockPosList result = new BlockPosList(animatedCoordinates);
        outputValues.put(OUTPUT_ANIMATED_COORDINATES_ID, result);
    }
    
    /**
     * 3D向量的线性插值
     * @param start 起始向量
     * @param end 结束向量
     * @param factor 插值因子 (0-1)
     * @return 插值结果
     */
    private Vector3d lerpVector3d(Vector3d start, Vector3d end, float factor) {
        Vector3d result = new Vector3d();
        result.x = start.x + (end.x - start.x) * factor;
        result.y = start.y + (end.y - start.y) * factor;
        result.z = start.z + (end.z - start.z) * factor;
        return result;
    }
} 