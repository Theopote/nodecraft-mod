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
 * Animate Rotate Node: 动画旋转节点
 * 围绕轴心点旋转几何体
 */
@NodeInfo(
    id = "animation.transforms.animate_rotate",
    displayName = "Animate Rotate",
    description = "围绕轴心点旋转几何体",
    category = "animation.transforms"
)
public class AnimateRotateNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_GEOMETRY_ID = "input_geometry";
    private static final String INPUT_PIVOT_ID = "input_pivot";
    private static final String INPUT_AXIS_ID = "input_axis";
    private static final String INPUT_START_ANGLE_ID = "input_start_angle";
    private static final String INPUT_END_ANGLE_ID = "input_end_angle";
    private static final String INPUT_FACTOR_ID = "input_factor";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_TRANSFORMED_GEOMETRY_ID = "output_transformed_geometry";
    
    // --- 构造函数 ---
    public AnimateRotateNode() {
        super(UUID.randomUUID(), "animation.transforms.animate_rotate");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_GEOMETRY_ID, "Geometry", "需要变换的几何体（坐标列表）", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_PIVOT_ID, "Pivot", "旋转轴心点", NodeDataType.VECTOR, this));
        addInputPort(new BasePort(INPUT_AXIS_ID, "Axis", "旋转轴", NodeDataType.VECTOR, this));
        addInputPort(new BasePort(INPUT_START_ANGLE_ID, "Start Angle", "起始角度（度）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_END_ANGLE_ID, "End Angle", "结束角度（度）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_FACTOR_ID, "Factor", "动画进度因子（0-1）", NodeDataType.FLOAT, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_TRANSFORMED_GEOMETRY_ID, "Transformed Geometry", "变换后的几何体", NodeDataType.LIST, this));
    }
    
    @Override
    public String getDescription() {
        return "围绕轴心点旋转几何体";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object geometryObj = inputValues.get(INPUT_GEOMETRY_ID);
        float[] pivot = (float[]) inputValues.getOrDefault(INPUT_PIVOT_ID, new float[]{0.0f, 0.0f, 0.0f});
        float[] axis = (float[]) inputValues.getOrDefault(INPUT_AXIS_ID, new float[]{0.0f, 1.0f, 0.0f}); // 默认Y轴
        Float startAngle = (Float) inputValues.getOrDefault(INPUT_START_ANGLE_ID, 0.0f);
        Float endAngle = (Float) inputValues.getOrDefault(INPUT_END_ANGLE_ID, 360.0f);
        Float factor = (Float) inputValues.getOrDefault(INPUT_FACTOR_ID, 0.0f);
        
        // 确保插值因子在0-1范围内
        factor = Math.max(0.0f, Math.min(1.0f, factor));
        
        // 确保向量有效
        if (pivot.length < 3) pivot = new float[]{0.0f, 0.0f, 0.0f};
        if (axis.length < 3) axis = new float[]{0.0f, 1.0f, 0.0f};
        
        // 归一化旋转轴
        normalizeVector(axis);
        
        // 计算当前角度（度）
        float currentAngle = startAngle + (endAngle - startAngle) * factor;
        
        // 将角度转换为弧度
        float angleRadians = (float) Math.toRadians(currentAngle);
        
        // 变换几何体
        List<int[]> transformedGeometry = transformGeometry(geometryObj, pivot, axis, angleRadians);
        
        // 设置输出值
        outputValues.put(OUTPUT_TRANSFORMED_GEOMETRY_ID, transformedGeometry);
    }
    
    /**
     * 变换几何体
     * @param geometryObj 输入几何体（坐标列表）
     * @param pivot 旋转轴心点
     * @param axis 旋转轴
     * @param angle 旋转角度（弧度）
     * @return 变换后的几何体
     */
    @SuppressWarnings("unchecked")
    private List<int[]> transformGeometry(Object geometryObj, float[] pivot, float[] axis, float angle) {
        List<int[]> result = new ArrayList<>();
        
        // 确保几何体是有效的坐标列表
        if (geometryObj instanceof List) {
            List<?> geometryList = (List<?>) geometryObj;
            
            for (Object coordObj : geometryList) {
                int[] transformedCoord = transformCoordinate(coordObj, pivot, axis, angle);
                if (transformedCoord != null) {
                    result.add(transformedCoord);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 变换单个坐标
     * @param coordObj 坐标对象
     * @param pivot 旋转轴心点
     * @param axis 旋转轴
     * @param angle 旋转角度（弧度）
     * @return 变换后的坐标
     */
    private int[] transformCoordinate(Object coordObj, float[] pivot, float[] axis, float angle) {
        float[] coord = getCoordinateAsFloatArray(coordObj);
        if (coord == null) return null;
        
        // 步骤1: 将坐标移动到轴心点为原点
        float[] translatedCoord = new float[3];
        for (int i = 0; i < 3; i++) {
            translatedCoord[i] = coord[i] - pivot[i];
        }
        
        // 步骤2: 应用旋转（使用罗德里格旋转公式）
        float[] rotatedCoord = rotatePointAroundAxis(translatedCoord, axis, angle);
        
        // 步骤3: 将坐标移回原来的位置
        for (int i = 0; i < 3; i++) {
            rotatedCoord[i] += pivot[i];
        }
        
        // 步骤4: 将浮点坐标转换为整数坐标（四舍五入）
        return new int[]{
            Math.round(rotatedCoord[0]),
            Math.round(rotatedCoord[1]),
            Math.round(rotatedCoord[2])
        };
    }
    
    /**
     * 将坐标对象转换为浮点数组
     * @param coordObj 坐标对象
     * @return 浮点数组[x, y, z]
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
    
    /**
     * 归一化向量
     * @param vector 向量
     */
    private void normalizeVector(float[] vector) {
        float length = (float) Math.sqrt(
            vector[0] * vector[0] +
            vector[1] * vector[1] +
            vector[2] * vector[2]
        );
        
        // 防止除以零
        if (length > 0.0001f) {
            vector[0] /= length;
            vector[1] /= length;
            vector[2] /= length;
        } else {
            // 如果向量长度为0，设置为默认Y轴
            vector[0] = 0.0f;
            vector[1] = 1.0f;
            vector[2] = 0.0f;
        }
    }
    
    /**
     * 使用罗德里格旋转公式围绕任意轴旋转点
     * @param point 要旋转的点
     * @param axis 旋转轴（已归一化）
     * @param angle 旋转角度（弧度）
     * @return 旋转后的点
     */
    private float[] rotatePointAroundAxis(float[] point, float[] axis, float angle) {
        float[] rotated = new float[3];
        
        // 罗德里格旋转公式: v_rot = v*cos(θ) + (k×v)*sin(θ) + k*(k·v)*(1-cos(θ))
        float cosAngle = (float) Math.cos(angle);
        float sinAngle = (float) Math.sin(angle);
        
        // 计算 k·v (点积)
        float dotProduct = 
            axis[0] * point[0] +
            axis[1] * point[1] +
            axis[2] * point[2];
        
        // 计算 k×v (叉积)
        float[] crossProduct = new float[3];
        crossProduct[0] = axis[1] * point[2] - axis[2] * point[1];
        crossProduct[1] = axis[2] * point[0] - axis[0] * point[2];
        crossProduct[2] = axis[0] * point[1] - axis[1] * point[0];
        
        // 应用罗德里格旋转公式
        for (int i = 0; i < 3; i++) {
            rotated[i] = point[i] * cosAngle + 
                          crossProduct[i] * sinAngle + 
                          axis[i] * dotProduct * (1 - cosAngle);
        }
        
        return rotated;
    }
} 