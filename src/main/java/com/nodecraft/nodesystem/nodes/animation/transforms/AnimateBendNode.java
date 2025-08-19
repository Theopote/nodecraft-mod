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
 * Animate Bend Node: 动画弯曲节点
 * 沿特定平面弯曲几何体
 */
@NodeInfo(
    id = "animation.transforms.animate_bend",
    displayName = "Animate Bend",
    description = "沿特定平面弯曲几何体",
    category = "animation.transforms"
)
public class AnimateBendNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_GEOMETRY_ID = "input_geometry";
    private static final String INPUT_CENTER_ID = "input_center";
    private static final String INPUT_DIRECTION_ID = "input_direction";
    private static final String INPUT_UP_ID = "input_up";
    private static final String INPUT_START_ANGLE_ID = "input_start_angle";
    private static final String INPUT_END_ANGLE_ID = "input_end_angle";
    private static final String INPUT_FACTOR_ID = "input_factor";
    private static final String INPUT_RADIUS_ID = "input_radius";
    private static final String INPUT_BOUNDS_ID = "input_bounds";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_TRANSFORMED_GEOMETRY_ID = "output_transformed_geometry";
    
    // --- 构造函数 ---
    public AnimateBendNode() {
        super(UUID.randomUUID(), "animation.transforms.animate_bend");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_GEOMETRY_ID, "Geometry", "需要变换的几何体（坐标列表）", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_CENTER_ID, "Center", "弯曲中心点", NodeDataType.VECTOR, this));
        addInputPort(new BasePort(INPUT_DIRECTION_ID, "Direction", "弯曲方向", NodeDataType.VECTOR, this));
        addInputPort(new BasePort(INPUT_UP_ID, "Up", "弯曲平面法线", NodeDataType.VECTOR, this));
        addInputPort(new BasePort(INPUT_START_ANGLE_ID, "Start Angle", "起始弯曲角度（度）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_END_ANGLE_ID, "End Angle", "结束弯曲角度（度）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_FACTOR_ID, "Factor", "动画进度因子（0-1）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_RADIUS_ID, "Radius", "弯曲半径", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_BOUNDS_ID, "Bounds", "弯曲边界（超出边界不弯曲）", NodeDataType.FLOAT, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_TRANSFORMED_GEOMETRY_ID, "Transformed Geometry", "变换后的几何体", NodeDataType.LIST, this));
    }
    
    @Override
    public String getDescription() {
        return "沿特定平面弯曲几何体";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object geometryObj = inputValues.get(INPUT_GEOMETRY_ID);
        float[] center = (float[]) inputValues.getOrDefault(INPUT_CENTER_ID, new float[]{0.0f, 0.0f, 0.0f});
        float[] direction = (float[]) inputValues.getOrDefault(INPUT_DIRECTION_ID, new float[]{1.0f, 0.0f, 0.0f}); // 默认X轴
        float[] up = (float[]) inputValues.getOrDefault(INPUT_UP_ID, new float[]{0.0f, 1.0f, 0.0f}); // 默认Y轴
        Float startAngle = (Float) inputValues.getOrDefault(INPUT_START_ANGLE_ID, 0.0f);
        Float endAngle = (Float) inputValues.getOrDefault(INPUT_END_ANGLE_ID, 90.0f);
        Float factor = (Float) inputValues.getOrDefault(INPUT_FACTOR_ID, 0.0f);
        Float radius = (Float) inputValues.getOrDefault(INPUT_RADIUS_ID, 5.0f);
        Float bounds = (Float) inputValues.getOrDefault(INPUT_BOUNDS_ID, 10.0f);
        
        // 确保插值因子在0-1范围内
        factor = Math.max(0.0f, Math.min(1.0f, factor));
        
        // 确保向量有效
        if (center.length < 3) center = new float[]{0.0f, 0.0f, 0.0f};
        if (direction.length < 3) direction = new float[]{1.0f, 0.0f, 0.0f};
        if (up.length < 3) up = new float[]{0.0f, 1.0f, 0.0f};
        
        // 确保弯曲半径大于0
        radius = Math.max(0.1f, radius);
        
        // 归一化方向向量
        normalizeVector(direction);
        normalizeVector(up);
        
        // 确保up和direction正交
        // 计算叉积得到第三个正交轴
        float[] right = crossProduct(direction, up);
        normalizeVector(right);
        // 重新计算up以确保三个轴完全正交
        up = crossProduct(right, direction);
        normalizeVector(up);
        
        // 计算当前弯曲角度（度）
        float currentAngle = startAngle + (endAngle - startAngle) * factor;
        
        // 将角度转换为弧度
        float angleRadians = (float) Math.toRadians(currentAngle);
        
        // 变换几何体
        List<int[]> transformedGeometry = transformGeometry(geometryObj, center, direction, up, right, angleRadians, radius, bounds);
        
        // 设置输出值
        outputValues.put(OUTPUT_TRANSFORMED_GEOMETRY_ID, transformedGeometry);
    }
    
    /**
     * 变换几何体
     */
    @SuppressWarnings("unchecked")
    private List<int[]> transformGeometry(Object geometryObj, float[] center, float[] direction, float[] up, float[] right, 
                                         float angle, float radius, float bounds) {
        List<int[]> result = new ArrayList<>();
        
        if (geometryObj instanceof List) {
            List<?> geometryList = (List<?>) geometryObj;
            
            for (Object coordObj : geometryList) {
                float[] coord = getCoordinateAsFloatArray(coordObj);
                if (coord != null) {
                    // 弯曲坐标
                    int[] transformedCoord = bendCoordinate(coord, center, direction, up, right, angle, radius, bounds);
                    result.add(transformedCoord);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 弯曲单个坐标
     */
    private int[] bendCoordinate(float[] coord, float[] center, float[] direction, float[] up, float[] right, 
                                float angle, float radius, float bounds) {
        // 步骤1: 计算点相对于中心点的位置
        float[] relativePos = new float[3];
        for (int i = 0; i < 3; i++) {
            relativePos[i] = coord[i] - center[i];
        }
        
        // 步骤2: 将相对位置转换到弯曲坐标系（direction, up, right）
        float dirComponent = dotProduct(relativePos, direction);
        float upComponent = dotProduct(relativePos, up);
        float rightComponent = dotProduct(relativePos, right);
        
        // 步骤3: 检查是否在弯曲边界内
        if (Math.abs(dirComponent) > bounds) {
            // 超出边界，不弯曲
            return new int[]{Math.round(coord[0]), Math.round(coord[1]), Math.round(coord[2])};
        }
        
        // 步骤4: 计算弯曲角度（基于沿方向轴的位置进行插值）
        float bendAngle = angle * (dirComponent / bounds);
        
        // 步骤5: 应用弯曲变换
        // 首先，计算点在弯曲平面上的位置
        float distanceFromAxis = upComponent;
        
        // 计算弯曲后的位置（使用极坐标变换）
        float bendRadius = radius + distanceFromAxis;
        
        // 计算弯曲后的坐标（在弯曲坐标系中）
        float bendDirComponent = (float) (Math.sin(bendAngle) * bendRadius);
        float bendUpComponent = (float) (Math.cos(bendAngle) * bendRadius - radius);
        
        // 步骤6: 将弯曲后的坐标转换回世界坐标系
        float[] bentPos = new float[3];
        for (int i = 0; i < 3; i++) {
            bentPos[i] = center[i] + 
                         direction[i] * bendDirComponent + 
                         up[i] * bendUpComponent + 
                         right[i] * rightComponent;
        }
        
        // 步骤7: 转换为整数坐标并返回
        return new int[]{
            Math.round(bentPos[0]),
            Math.round(bentPos[1]),
            Math.round(bentPos[2])
        };
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
     * 归一化向量
     */
    private void normalizeVector(float[] v) {
        float length = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        
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