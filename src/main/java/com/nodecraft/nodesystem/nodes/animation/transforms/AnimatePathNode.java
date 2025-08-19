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
 * Animate Path Node: 动画路径节点
 * 让几何体沿着特定路径移动
 */
@NodeInfo(
    id = "animation.transforms.animate_path",
    displayName = "Animate Path",
    description = "让几何体沿特定路径移动",
    category = "animation.transforms"
)
public class AnimatePathNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_GEOMETRY_ID = "input_geometry";
    private static final String INPUT_PATH_POINTS_ID = "input_path_points";
    private static final String INPUT_FACTOR_ID = "input_factor";
    private static final String INPUT_LOOP_ID = "input_loop";
    private static final String INPUT_ORIENT_ID = "input_orient";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_TRANSFORMED_GEOMETRY_ID = "output_transformed_geometry";
    private static final String OUTPUT_CURRENT_POSITION_ID = "output_current_position";
    private static final String OUTPUT_CURRENT_DIRECTION_ID = "output_current_direction";
    
    // --- 构造函数 ---
    public AnimatePathNode() {
        super(UUID.randomUUID(), "animation.transforms.animate_path");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_GEOMETRY_ID, "Geometry", "需要变换的几何体（坐标列表）", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_PATH_POINTS_ID, "Path Points", "路径点列表（Vector3列表）", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_FACTOR_ID, "Factor", "动画进度因子（0-1）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_LOOP_ID, "Loop", "是否循环路径", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_ORIENT_ID, "Orient to Path", "是否沿路径方向旋转几何体", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_TRANSFORMED_GEOMETRY_ID, "Transformed Geometry", "变换后的几何体", NodeDataType.LIST, this));
        addOutputPort(new BasePort(OUTPUT_CURRENT_POSITION_ID, "Current Position", "当前位置", NodeDataType.VECTOR, this));
        addOutputPort(new BasePort(OUTPUT_CURRENT_DIRECTION_ID, "Current Direction", "当前方向", NodeDataType.VECTOR, this));
    }
    
    @Override
    public String getDescription() {
        return "让几何体沿特定路径移动";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object geometryObj = inputValues.get(INPUT_GEOMETRY_ID);
        Object pathPointsObj = inputValues.get(INPUT_PATH_POINTS_ID);
        Float factor = (Float) inputValues.getOrDefault(INPUT_FACTOR_ID, 0.0f);
        Boolean loop = (Boolean) inputValues.getOrDefault(INPUT_LOOP_ID, false);
        Boolean orient = (Boolean) inputValues.getOrDefault(INPUT_ORIENT_ID, false);
        
        // 确保插值因子在0-1范围内
        factor = Math.max(0.0f, Math.min(1.0f, factor));
        
        // 解析路径点
        List<float[]> pathPoints = getPathPoints(pathPointsObj);
        
        // 如果路径点不足，则直接返回原始几何体
        if (pathPoints.size() < 2) {
            outputValues.put(OUTPUT_TRANSFORMED_GEOMETRY_ID, geometryObj);
            if (pathPoints.size() == 1) {
                outputValues.put(OUTPUT_CURRENT_POSITION_ID, pathPoints.get(0));
                outputValues.put(OUTPUT_CURRENT_DIRECTION_ID, new float[]{0.0f, 0.0f, 1.0f}); // 默认朝向Z轴正方向
            }
            return;
        }
        
        // 计算当前路径位置和方向
        PathPosition pathPosition = calculatePathPosition(pathPoints, factor, loop);
        float[] currentPosition = pathPosition.position;
        float[] currentDirection = pathPosition.direction;
        
        // 变换几何体
        List<int[]> transformedGeometry;
        if (orient) {
            // 沿路径方向旋转并平移几何体
            transformedGeometry = transformGeometryWithOrientation(geometryObj, currentPosition, currentDirection);
        } else {
            // 仅平移几何体
            transformedGeometry = transformGeometry(geometryObj, currentPosition);
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_TRANSFORMED_GEOMETRY_ID, transformedGeometry);
        outputValues.put(OUTPUT_CURRENT_POSITION_ID, currentPosition);
        outputValues.put(OUTPUT_CURRENT_DIRECTION_ID, currentDirection);
    }
    
    /**
     * 解析路径点列表
     */
    @SuppressWarnings("unchecked")
    private List<float[]> getPathPoints(Object pathPointsObj) {
        List<float[]> result = new ArrayList<>();
        
        if (pathPointsObj instanceof List) {
            List<?> pointsList = (List<?>) pathPointsObj;
            
            for (Object pointObj : pointsList) {
                float[] point = null;
                
                // 处理不同类型的点对象
                if (pointObj instanceof float[]) {
                    float[] p = (float[]) pointObj;
                    if (p.length >= 3) {
                        point = new float[]{p[0], p[1], p[2]};
                    }
                } else if (pointObj instanceof int[]) {
                    int[] p = (int[]) pointObj;
                    if (p.length >= 3) {
                        point = new float[]{(float) p[0], (float) p[1], (float) p[2]};
                    }
                } else if (pointObj instanceof Map) {
                    try {
                        Map<String, Object> pointMap = (Map<String, Object>) pointObj;
                        if (pointMap.containsKey("x") && pointMap.containsKey("y") && pointMap.containsKey("z")) {
                            float x = ((Number) pointMap.get("x")).floatValue();
                            float y = ((Number) pointMap.get("y")).floatValue();
                            float z = ((Number) pointMap.get("z")).floatValue();
                            point = new float[]{x, y, z};
                        }
                    } catch (Exception e) {
                        // 处理失败，跳过这个点
                        continue;
                    }
                }
                
                if (point != null) {
                    result.add(point);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 路径位置信息
     */
    private static class PathPosition {
        public float[] position;
        public float[] direction;
        
        public PathPosition(float[] position, float[] direction) {
            this.position = position;
            this.direction = direction;
        }
    }
    
    /**
     * 计算路径上的当前位置和方向
     */
    private PathPosition calculatePathPosition(List<float[]> pathPoints, float factor, boolean loop) {
        int numSegments = pathPoints.size() - 1;
        float totalLength = 0.0f;
        
        // 计算路径总长度
        float[] segmentLengths = new float[numSegments];
        for (int i = 0; i < numSegments; i++) {
            float[] p1 = pathPoints.get(i);
            float[] p2 = pathPoints.get(i + 1);
            segmentLengths[i] = distance(p1, p2);
            totalLength += segmentLengths[i];
        }
        
        // 如果总长度为0，直接返回第一个点
        if (totalLength < 0.0001f) {
            float[] direction = {0.0f, 0.0f, 1.0f}; // 默认方向
            return new PathPosition(pathPoints.get(0), direction);
        }
        
        // 根据factor计算在路径上的位置
        float targetDistance = factor * totalLength;
        
        // 循环路径处理
        if (loop && factor >= 1.0f) {
            targetDistance = targetDistance % totalLength;
        }
        
        // 找到对应的路径段
        int segmentIndex = 0;
        float accumulatedDistance = 0.0f;
        
        while (segmentIndex < numSegments && accumulatedDistance + segmentLengths[segmentIndex] < targetDistance) {
            accumulatedDistance += segmentLengths[segmentIndex];
            segmentIndex++;
        }
        
        // 确保不越界
        if (segmentIndex >= numSegments) {
            segmentIndex = numSegments - 1;
            float[] direction = normalizeVector(
                subtractVectors(pathPoints.get(segmentIndex + 1), pathPoints.get(segmentIndex))
            );
            return new PathPosition(pathPoints.get(segmentIndex + 1), direction);
        }
        
        // 计算在当前段的插值因子
        float segmentFactor = 0.0f;
        if (segmentLengths[segmentIndex] > 0.0001f) {
            segmentFactor = (targetDistance - accumulatedDistance) / segmentLengths[segmentIndex];
        }
        
        // 限制在0-1范围内
        segmentFactor = Math.max(0.0f, Math.min(1.0f, segmentFactor));
        
        // 计算当前位置（线性插值）
        float[] p1 = pathPoints.get(segmentIndex);
        float[] p2 = pathPoints.get(segmentIndex + 1);
        float[] currentPosition = new float[3];
        for (int i = 0; i < 3; i++) {
            currentPosition[i] = p1[i] + (p2[i] - p1[i]) * segmentFactor;
        }
        
        // 计算当前方向
        float[] currentDirection = normalizeVector(subtractVectors(p2, p1));
        
        return new PathPosition(currentPosition, currentDirection);
    }
    
    /**
     * 计算两点之间的距离
     */
    private float distance(float[] p1, float[] p2) {
        float dx = p2[0] - p1[0];
        float dy = p2[1] - p1[1];
        float dz = p2[2] - p1[2];
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    /**
     * 向量减法
     */
    private float[] subtractVectors(float[] v1, float[] v2) {
        return new float[]{
            v1[0] - v2[0],
            v1[1] - v2[1],
            v1[2] - v2[2]
        };
    }
    
    /**
     * 归一化向量
     */
    private float[] normalizeVector(float[] v) {
        float length = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        
        if (length > 0.0001f) {
            return new float[]{
                v[0] / length,
                v[1] / length,
                v[2] / length
            };
        } else {
            return new float[]{0.0f, 0.0f, 1.0f}; // 默认Z轴正方向
        }
    }
    
    /**
     * 仅平移几何体（不旋转）
     */
    @SuppressWarnings("unchecked")
    private List<int[]> transformGeometry(Object geometryObj, float[] position) {
        List<int[]> result = new ArrayList<>();
        
        if (geometryObj instanceof List) {
            List<?> geometryList = (List<?>) geometryObj;
            
            for (Object coordObj : geometryList) {
                float[] coord = getCoordinateAsFloatArray(coordObj);
                if (coord != null) {
                    // 平移坐标
                    int[] transformedCoord = new int[]{
                        Math.round(coord[0] + position[0]),
                        Math.round(coord[1] + position[1]),
                        Math.round(coord[2] + position[2])
                    };
                    result.add(transformedCoord);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 平移并旋转几何体（使其朝向路径方向）
     */
    @SuppressWarnings("unchecked")
    private List<int[]> transformGeometryWithOrientation(Object geometryObj, float[] position, float[] direction) {
        List<int[]> result = new ArrayList<>();
        
        if (geometryObj instanceof List) {
            List<?> geometryList = (List<?>) geometryObj;
            
            // 计算从Z轴正方向(0,0,1)到当前方向的旋转
            float[] zAxis = {0.0f, 0.0f, 1.0f};
            float[] rotationAxis = crossProduct(zAxis, direction);
            float rotationAngle = (float) Math.acos(dotProduct(zAxis, direction));
            
            // 如果旋转轴接近零向量，说明方向已经是Z轴正方向或负方向
            boolean needRotation = length(rotationAxis) > 0.0001f;
            if (needRotation) {
                normalizeVectorInPlace(rotationAxis);
            }
            
            for (Object coordObj : geometryList) {
                float[] coord = getCoordinateAsFloatArray(coordObj);
                if (coord != null) {
                    float[] transformedCoord = coord.clone();
                    
                    // 先旋转
                    if (needRotation) {
                        transformedCoord = rotatePointAroundAxis(transformedCoord, rotationAxis, rotationAngle);
                    }
                    
                    // 再平移
                    transformedCoord[0] += position[0];
                    transformedCoord[1] += position[1];
                    transformedCoord[2] += position[2];
                    
                    // 转换为整数坐标
                    int[] finalCoord = new int[]{
                        Math.round(transformedCoord[0]),
                        Math.round(transformedCoord[1]),
                        Math.round(transformedCoord[2])
                    };
                    
                    result.add(finalCoord);
                }
            }
        }
        
        return result;
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
     * 原地归一化向量
     */
    private void normalizeVectorInPlace(float[] v) {
        float length = length(v);
        if (length > 0.0001f) {
            v[0] /= length;
            v[1] /= length;
            v[2] /= length;
        }
    }
    
    /**
     * 使用罗德里格旋转公式围绕任意轴旋转点
     */
    private float[] rotatePointAroundAxis(float[] point, float[] axis, float angle) {
        float[] rotated = new float[3];
        
        // 罗德里格旋转公式: v_rot = v*cos(θ) + (k×v)*sin(θ) + k*(k·v)*(1-cos(θ))
        float cosAngle = (float) Math.cos(angle);
        float sinAngle = (float) Math.sin(angle);
        
        // 计算 k·v (点积)
        float dotProduct = dotProduct(axis, point);
        
        // 计算 k×v (叉积)
        float[] crossProduct = crossProduct(axis, point);
        
        // 应用罗德里格旋转公式
        for (int i = 0; i < 3; i++) {
            rotated[i] = point[i] * cosAngle + 
                          crossProduct[i] * sinAngle + 
                          axis[i] * dotProduct * (1 - cosAngle);
        }
        
        return rotated;
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