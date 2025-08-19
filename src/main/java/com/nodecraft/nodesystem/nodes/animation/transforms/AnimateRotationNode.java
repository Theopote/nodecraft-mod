package com.nodecraft.nodesystem.nodes.animation.transforms;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Matrix4d;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Animate Rotation: 旋转动画节点
 * 使方块随时间围绕某个轴旋转
 */
@NodeInfo(
    id = "animation.properties.animate_rotation",
    displayName = "Animate Rotation",
    description = "使方块随时间围绕某个轴旋转",
    category = "animation.properties"
)
public class AnimateRotationNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_COORDINATES_ID = "input_coordinates";
    private static final String INPUT_CENTER_ID = "input_center";
    private static final String INPUT_AXIS_ID = "input_axis";
    private static final String INPUT_START_ANGLE_ID = "input_start_angle";
    private static final String INPUT_END_ANGLE_ID = "input_end_angle";
    private static final String INPUT_TIME_FACTOR_ID = "input_time_factor";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_ROTATED_COORDINATES_ID = "output_rotated_coordinates";

    // --- 构造函数 ---
    public AnimateRotationNode() {
        super(UUID.randomUUID(), "animation.properties.animate_rotation");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_COORDINATES_ID, "Coordinates", "要旋转的方块坐标列表", NodeDataType.BLOCK_LIST, this));
        addInputPort(new BasePort(INPUT_CENTER_ID, "Center", "旋转中心点", NodeDataType.BLOCK_POS, this));
        addInputPort(new BasePort(INPUT_AXIS_ID, "Axis", "旋转轴向量", NodeDataType.VECTOR, this));
        addInputPort(new BasePort(INPUT_START_ANGLE_ID, "Start Angle", "起始角度（度）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_END_ANGLE_ID, "End Angle", "结束角度（度）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_TIME_FACTOR_ID, "Time Factor", "时间因子 (0-1)", NodeDataType.FLOAT, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_ROTATED_COORDINATES_ID, "Rotated Coordinates", "旋转后的方块坐标列表", NodeDataType.BLOCK_LIST, this));
    }
    
    @Override
    public String getDescription() {
        return "使方块随时间围绕某个轴旋转";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        BlockPosList coordinates = (BlockPosList) inputValues.get(INPUT_COORDINATES_ID);
        BlockPos center = (BlockPos) inputValues.get(INPUT_CENTER_ID);
        Vector3d axis = (Vector3d) inputValues.get(INPUT_AXIS_ID);
        Float startAngle = (Float) inputValues.getOrDefault(INPUT_START_ANGLE_ID, 0.0f);
        Float endAngle = (Float) inputValues.getOrDefault(INPUT_END_ANGLE_ID, 360.0f);
        Float timeFactor = (Float) inputValues.getOrDefault(INPUT_TIME_FACTOR_ID, 0.0f);
        
        // 确保时间因子在[0,1]范围内
        timeFactor = Math.max(0f, Math.min(1f, timeFactor));
        
        // 如果任何必要输入为null，则输出null
        if (coordinates == null || center == null || axis == null) {
            outputValues.put(OUTPUT_ROTATED_COORDINATES_ID, null);
            return;
        }
        
        // 标准化旋转轴
        Vector3d normalizedAxis = new Vector3d(axis);
        if (normalizedAxis.lengthSquared() > 0) {
            normalizedAxis.normalize();
        } else {
            normalizedAxis.set(0, 1, 0); // 默认使用Y轴
        }
        
        // 计算当前角度（度）
        float currentAngle = startAngle + (endAngle - startAngle) * timeFactor;
        
        // 转换为弧度
        double angleRadians = Math.toRadians(currentAngle);
        
        // 构建旋转矩阵
        Matrix4d rotationMatrix = new Matrix4d();
        rotationMatrix.identity();
        rotationMatrix.rotate(angleRadians, normalizedAxis);
        
        // 应用旋转到每个方块坐标
        List<BlockPos> originalCoordinates = coordinates.getPositions();
        List<BlockPos> rotatedCoordinates = new ArrayList<>(originalCoordinates.size());
        
        Vector3d centerVec = new Vector3d(center.getX(), center.getY(), center.getZ());
        
        for (BlockPos pos : originalCoordinates) {
            // 将方块坐标转换为相对于中心点的向量
            Vector3d relativePos = new Vector3d(
                pos.getX() - centerVec.x,
                pos.getY() - centerVec.y,
                pos.getZ() - centerVec.z
            );
            
            // 应用旋转
            Vector3d rotatedPos = rotationMatrix.transformPosition(relativePos);
            
            // 将旋转后的向量转换回相对于世界的坐标
            BlockPos newPos = new BlockPos(
                (int) Math.round(rotatedPos.x + centerVec.x),
                (int) Math.round(rotatedPos.y + centerVec.y),
                (int) Math.round(rotatedPos.z + centerVec.z)
            );
            
            rotatedCoordinates.add(newPos);
        }
        
        // 创建新的BlockPosList并设置为输出
        BlockPosList result = new BlockPosList(rotatedCoordinates);
        outputValues.put(OUTPUT_ROTATED_COORDINATES_ID, result);
    }
} 