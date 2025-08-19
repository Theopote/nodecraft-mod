package com.nodecraft.nodesystem.nodes.animation.transforms;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Animate Twist Node: 动画扭曲节点
 * 沿轴线扭曲几何体
 */
@NodeInfo(
    id = "animation.transforms.animate_twist",
    displayName = "Animate Twist",
    description = "沿轴线扭曲几何体",
    category = "animation.transforms"
)
public class AnimateTwistNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_GEOMETRY_ID = "input_geometry";
    private static final String INPUT_AXIS_ID = "input_axis";
    private static final String INPUT_AXIS_ORIGIN_ID = "input_axis_origin";
    private static final String INPUT_START_ANGLE_ID = "input_start_angle";
    private static final String INPUT_END_ANGLE_ID = "input_end_angle";
    private static final String INPUT_FACTOR_ID = "input_factor";
    private static final String INPUT_FALLOFF_ID = "input_falloff";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_TRANSFORMED_GEOMETRY_ID = "output_transformed_geometry";
    
    // --- 构造函数 ---
    public AnimateTwistNode() {
        super(UUID.randomUUID(), "animation.transforms.animate_twist");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_GEOMETRY_ID, "Geometry", "需要变换的几何体（坐标列表）", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_AXIS_ID, "Axis", "扭曲轴方向", NodeDataType.VECTOR, this));
        addInputPort(new BasePort(INPUT_AXIS_ORIGIN_ID, "Axis Origin", "扭曲轴起点", NodeDataType.VECTOR, this));
        addInputPort(new BasePort(INPUT_START_ANGLE_ID, "Start Angle", "起始扭曲角度（度）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_END_ANGLE_ID, "End Angle", "结束扭曲角度（度）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_FACTOR_ID, "Factor", "动画进度因子（0-1）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_FALLOFF_ID, "Falloff", "扭曲衰减因子（距离轴越远，扭曲越小）", NodeDataType.FLOAT, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_TRANSFORMED_GEOMETRY_ID, "Transformed Geometry", "变换后的几何体", NodeDataType.LIST, this));
    }
    
    @Override
    public String getDescription() {
        return "沿轴线扭曲几何体";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object geometryObj = inputValues.get(INPUT_GEOMETRY_ID);
        float[] axis = (float[]) inputValues.getOrDefault(INPUT_AXIS_ID, new float[]{0.0f, 1.0f, 0.0f}); // 默认Y轴
        float[] axisOrigin = (float[]) inputValues.getOrDefault(INPUT_AXIS_ORIGIN_ID, new float[]{0.0f, 0.0f, 0.0f});
        Float startAngle = (Float) inputValues.getOrDefault(INPUT_START_ANGLE_ID, 0.0f);
        Float endAngle = (Float) inputValues.getOrDefault(INPUT_END_ANGLE_ID, 90.0f);
        Float factor = (Float) inputValues.getOrDefault(INPUT_FACTOR_ID, 0.0f);
        Float falloff = (Float) inputValues.getOrDefault(INPUT_FALLOFF_ID, 0.0f);
        
        // 确保插值因子在0-1范围内
        factor = Math.max(0.0f, Math.min(1.0f, factor));
        
        // 确保向量有效
        if (axis.length < 3) axis = new float[]{0.0f, 1.0f, 0.0f};
        if (axisOrigin.length < 3) axisOrigin = new float[]{0.0f, 0.0f, 0.0f};
        
        // 归一化扭曲轴
        normalizeVector(axis);
        
        // 计算当前扭曲角度（度）
        float currentAngle = startAngle + (endAngle - startAngle) * factor;
        
        // 将角度转换为弧度
        float angleRadians = (float) Math.toRadians(currentAngle);
        
        // 变换几何体
        List<int[]> transformedGeometry = transformGeometry(geometryObj, axisOrigin, axis, angleRadians, falloff);
        
        // 设置输出值
        outputValues.put(OUTPUT_TRANSFORMED_GEOMETRY_ID, transformedGeometry);
    }
    
    /**
     * 变换几何体
     */
    @SuppressWarnings("unchecked")
    private List<int[]> transformGeometry(Object geometryObj, float[] axisOrigin, float[] axis, float angle, float falloff) {
        List<int[]> result = new ArrayList<>();
        
        if (geometryObj instanceof List) {
            List<?> geometryList = (List<?>) geometryObj;
            
            for (Object coordObj : geometryList) {
                float[] coord = getCoordinateAsFloatArray(coordObj);
                if (coord != null) {
                    // 扭曲坐标
                    int[] transformedCoord = twistCoordinate(coord, axisOrigin, axis, angle, falloff);
                    result.add(transformedCoord);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 扭曲单个坐标
     */
    private int[] twistCoordinate(float[] coord, float[] axisOrigin, float[] axis, float angle, float falloff) {
        // 步骤1: 计算点到轴的投影点
        float[] projection = projectPointOnLine(coord, axisOrigin, axis);
        
        // 步骤2: 计算从投影点到实际点的向量
        float[] radialVector = new float[3];
        for (int i = 0; i < 3; i++) {
            radialVector[i] = coord[i] - projection[i];
        }
        
        // 步骤3: 计算径向向量的长度（距离轴的距离）
        float radialDistance = length(radialVector);
        
        // 如果点在轴上或非常接近轴，则不需要扭曲
        if (radialDistance < 0.0001f) {
            return new int[]{Math.round(coord[0]), Math.round(coord[1]), Math.round(coord[2])};
        }
        
        // 步骤4: 计算投影点到轴原点的距离（沿轴的位置）
        float[] axisVector = new float[3];
        for (int i = 0; i < 3; i++) {
            axisVector[i] = projection[i] - axisOrigin[i];
        }
        float axisDistance = dotProduct(axisVector, axis);
        
        // 步骤5: 计算实际扭曲角度（考虑衰减和沿轴的位置）
        float twistAmount = angle;
        
        // 考虑距离衰减（可选）
        if (falloff > 0.0001f) {
            // 衰减公式：使用指数衰减，距离越远，扭曲效果越小
            twistAmount *= (float) Math.exp(-radialDistance / falloff);
        }
        
        // 根据沿轴的位置调整扭曲角度（线性插值，越远扭曲越大）
        twistAmount *= Math.abs(axisDistance);
        
        // 步骤6: 归一化径向向量，作为旋转平面的一部分
        float[] radialDir = new float[3];
        for (int i = 0; i < 3; i++) {
            radialDir[i] = radialVector[i] / radialDistance;
        }
        
        // 步骤7: 计算垂直于轴和径向向量的第二个方向向量，形成旋转平面
        float[] tangentDir = crossProduct(axis, radialDir);
        normalizeVector(tangentDir);
        
        // 步骤8: 在旋转平面内旋转径向向量
        float cosAngle = (float) Math.cos(twistAmount);
        float sinAngle = (float) Math.sin(twistAmount);
        
        float[] twistedRadialVector = new float[3];
        for (int i = 0; i < 3; i++) {
            twistedRadialVector[i] = radialDistance * (radialDir[i] * cosAngle + tangentDir[i] * sinAngle);
        }
        
        // 步骤9: 从投影点添加扭曲后的径向向量，得到最终点
        float[] twistedCoord = new float[3];
        for (int i = 0; i < 3; i++) {
            twistedCoord[i] = projection[i] + twistedRadialVector[i];
        }
        
        // 步骤10: 转换为整数坐标并返回
        return new int[]{
            Math.round(twistedCoord[0]),
            Math.round(twistedCoord[1]),
            Math.round(twistedCoord[2])
        };
    }
    
    /**
     * 计算点到直线的投影点
     */
    private float[] projectPointOnLine(float[] point, float[] lineOrigin, float[] lineDirection) {
        // 计算从直线原点到点的向量
        float[] toPoint = new float[3];
        for (int i = 0; i < 3; i++) {
            toPoint[i] = point[i] - lineOrigin[i];
        }
        
        // 计算向量在直线方向上的投影长度
        float projectionLength = dotProduct(toPoint, lineDirection);
        
        // 计算投影点
        float[] projectionPoint = new float[3];
        for (int i = 0; i < 3; i++) {
            projectionPoint[i] = lineOrigin[i] + lineDirection[i] * projectionLength;
        }
        
        return projectionPoint;
    }
    
    /**
     * 向量点积
     */
    private float dotProduct(float[] v1, float[] v2) {
        return v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
    }
    
    /**
     * 向量叉积
     */
    private float[] crossProduct(float[] v1, float[] v2) {
        return new float[]{
            v1[1] * v2[2] - v1[2] * v2[1],
            v1[2] * v2[0] - v1[0] * v2[2],
            v1[0] * v2[1] - v1[1] * v2[0]
        };
    }
    
    /**
     * 向量长度
     */
    private float length(float[] v) {
        return (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }
    
    /**
     * 归一化向量
     */
    private void normalizeVector(float[] v) {
        float length = length(v);
        
        // 防止除以零
        if (length > 0.0001f) {
            v[0] /= length;
            v[1] /= length;
            v[2] /= length;
        } else {
            // 如果向量长度为0，设置为默认Y轴
            v[0] = 0.0f;
            v[1] = 1.0f;
            v[2] = 0.0f;
        }
    }
    
    /**
     * 将坐标对象转换为浮点数组
     */
    private float[] getCoordinateAsFloatArray(Object coordObj) {
        // 处理int[]格式的坐标
        if (coordObj instanceof int[]) {
            int[] coord = (int[]) coordObj;
            if (coord.length >= 3) {
                return new float[]{
                    (float) coord[0],
                    (float) coord[1],
                    (float) coord[2]
                };
            }
        }
        // 处理包含x,y,z字段的对象（如Coordinate类）
        else if (coordObj instanceof Map) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> coordMap = (Map<String, Object>) coordObj;
                
                if (coordMap.containsKey("x") && coordMap.containsKey("y") && coordMap.containsKey("z")) {
                    float x = ((Number) coordMap.get("x")).floatValue();
                    float y = ((Number) coordMap.get("y")).floatValue();
                    float z = ((Number) coordMap.get("z")).floatValue();
                    
                    return new float[]{x, y, z};
                }
            } catch (Exception e) {
                // 处理失败，返回null
                return null;
            }
        }
        
        return null;
    }
} 