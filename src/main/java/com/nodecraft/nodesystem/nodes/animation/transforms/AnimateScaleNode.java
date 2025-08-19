package com.nodecraft.nodesystem.nodes.animation.transforms;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Animate Scale Node: 动画缩放节点
 * 从轴心点缩放几何体，处理方块的重叠和消失
 */
@NodeInfo(
    id = "animation.transforms.animate_scale",
    displayName = "Animate Scale",
    description = "从轴心点缩放几何体",
    category = "animation.transforms"
)
public class AnimateScaleNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_GEOMETRY_ID = "input_geometry";
    private static final String INPUT_PIVOT_ID = "input_pivot";
    private static final String INPUT_START_SCALE_ID = "input_start_scale";
    private static final String INPUT_END_SCALE_ID = "input_end_scale";
    private static final String INPUT_FACTOR_ID = "input_factor";
    private static final String INPUT_UNIFORM_SCALE_ID = "input_uniform_scale";
    private static final String INPUT_SCALE_X_ID = "input_scale_x";
    private static final String INPUT_SCALE_Y_ID = "input_scale_y";
    private static final String INPUT_SCALE_Z_ID = "input_scale_z";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_TRANSFORMED_GEOMETRY_ID = "output_transformed_geometry";
    
    // --- 构造函数 ---
    public AnimateScaleNode() {
        super(UUID.randomUUID(), "animation.transforms.animate_scale");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_GEOMETRY_ID, "Geometry", "需要变换的几何体（坐标列表）", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_PIVOT_ID, "Pivot", "缩放轴心点", NodeDataType.VECTOR, this));
        addInputPort(new BasePort(INPUT_START_SCALE_ID, "Start Scale", "起始缩放比例", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_END_SCALE_ID, "End Scale", "结束缩放比例", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_FACTOR_ID, "Factor", "动画进度因子（0-1）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_UNIFORM_SCALE_ID, "Uniform Scale", "是否统一缩放所有轴", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_SCALE_X_ID, "Scale X", "X轴缩放比例（仅非统一缩放时使用）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_SCALE_Y_ID, "Scale Y", "Y轴缩放比例（仅非统一缩放时使用）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_SCALE_Z_ID, "Scale Z", "Z轴缩放比例（仅非统一缩放时使用）", NodeDataType.FLOAT, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_TRANSFORMED_GEOMETRY_ID, "Transformed Geometry", "变换后的几何体", NodeDataType.LIST, this));
    }
    
    @Override
    public String getDescription() {
        return "从轴心点缩放几何体";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object geometryObj = inputValues.get(INPUT_GEOMETRY_ID);
        float[] pivot = (float[]) inputValues.getOrDefault(INPUT_PIVOT_ID, new float[]{0.0f, 0.0f, 0.0f});
        Float startScale = (Float) inputValues.getOrDefault(INPUT_START_SCALE_ID, 1.0f);
        Float endScale = (Float) inputValues.getOrDefault(INPUT_END_SCALE_ID, 2.0f);
        Float factor = (Float) inputValues.getOrDefault(INPUT_FACTOR_ID, 0.0f);
        Boolean uniformScale = (Boolean) inputValues.getOrDefault(INPUT_UNIFORM_SCALE_ID, true);
        
        // 确保插值因子在0-1范围内
        factor = Math.max(0.0f, Math.min(1.0f, factor));
        
        // 确保位置向量有效
        if (pivot.length < 3) pivot = new float[]{0.0f, 0.0f, 0.0f};
        
        // 计算当前缩放比例
        float currentScale = startScale + (endScale - startScale) * factor;
        
        // 非统一缩放时的各轴缩放比例
        float[] scaleFactors = new float[3];
        if (uniformScale) {
            scaleFactors[0] = scaleFactors[1] = scaleFactors[2] = currentScale;
        } else {
            Float scaleX = (Float) inputValues.getOrDefault(INPUT_SCALE_X_ID, 1.0f);
            Float scaleY = (Float) inputValues.getOrDefault(INPUT_SCALE_Y_ID, 1.0f);
            Float scaleZ = (Float) inputValues.getOrDefault(INPUT_SCALE_Z_ID, 1.0f);
            
            scaleFactors[0] = startScale + (scaleX - startScale) * factor;
            scaleFactors[1] = startScale + (scaleY - startScale) * factor;
            scaleFactors[2] = startScale + (scaleZ - startScale) * factor;
        }
        
        // 变换几何体
        List<int[]> transformedGeometry = transformGeometry(geometryObj, pivot, scaleFactors);
        
        // 设置输出值
        outputValues.put(OUTPUT_TRANSFORMED_GEOMETRY_ID, transformedGeometry);
    }
    
    /**
     * 变换几何体
     * @param geometryObj 输入几何体（坐标列表）
     * @param pivot 缩放轴心点
     * @param scaleFactors 各轴缩放比例[x,y,z]
     * @return 变换后的几何体
     */
    @SuppressWarnings("unchecked")
    private List<int[]> transformGeometry(Object geometryObj, float[] pivot, float[] scaleFactors) {
        // 使用Set存储变换后的坐标，防止重复
        Set<String> uniqueCoords = new HashSet<>();
        List<int[]> result = new ArrayList<>();
        
        // 确保几何体是有效的坐标列表
        if (geometryObj instanceof List) {
            List<?> geometryList = (List<?>) geometryObj;
            
            // 首先收集所有原始坐标
            List<float[]> originalCoords = new ArrayList<>();
            for (Object coordObj : geometryList) {
                float[] coord = getCoordinateAsFloatArray(coordObj);
                if (coord != null) {
                    originalCoords.add(coord);
                }
            }
            
            // 对每个坐标应用缩放变换
            for (float[] coord : originalCoords) {
                int[] scaledCoord = scaleCoordinate(coord, pivot, scaleFactors);
                
                // 使用字符串表示来检查重复
                String coordKey = scaledCoord[0] + "," + scaledCoord[1] + "," + scaledCoord[2];
                if (!uniqueCoords.contains(coordKey)) {
                    uniqueCoords.add(coordKey);
                    result.add(scaledCoord);
                }
            }
            
            // 如果缩放使得方块消失（缩小）或重叠（放大），需要特殊处理
            handleVoxelScaling(originalCoords, pivot, scaleFactors, uniqueCoords, result);
        }
        
        return result;
    }
    
    /**
     * 处理体素化缩放的特殊情况
     * 1. 缩小可能导致方块消失
     * 2. 放大可能导致方块之间出现空隙
     */
    private void handleVoxelScaling(List<float[]> originalCoords, float[] pivot, float[] scaleFactors, 
                                   Set<String> uniqueCoords, List<int[]> result) {
        // 判断是放大还是缩小
        boolean isEnlarging = scaleFactors[0] > 1.0f || scaleFactors[1] > 1.0f || scaleFactors[2] > 1.0f;
        
        if (isEnlarging) {
            // 处理放大时可能出现的空隙
            fillGaps(originalCoords, pivot, scaleFactors, uniqueCoords, result);
        }
    }
    
    /**
     * 填充放大时可能出现的空隙
     */
    private void fillGaps(List<float[]> originalCoords, float[] pivot, float[] scaleFactors, 
                         Set<String> uniqueCoords, List<int[]> result) {
        // 寻找最大缩放因子
        float maxScale = Math.max(Math.max(scaleFactors[0], scaleFactors[1]), scaleFactors[2]);
        
        // 如果最大缩放因子小于1.5，通常不需要填充空隙
        if (maxScale < 1.5f) return;
        
        // 对于每一对相邻的原始方块，检查它们之间是否有空隙
        for (int i = 0; i < originalCoords.size(); i++) {
            for (int j = i + 1; j < originalCoords.size(); j++) {
                float[] coordA = originalCoords.get(i);
                float[] coordB = originalCoords.get(j);
                
                // 检查两个方块是否相邻（曼哈顿距离为1）
                int manhattanDistance = 
                    Math.abs(Math.round(coordA[0]) - Math.round(coordB[0])) +
                    Math.abs(Math.round(coordA[1]) - Math.round(coordB[1])) +
                    Math.abs(Math.round(coordA[2]) - Math.round(coordB[2]));
                
                if (manhattanDistance == 1) {
                    // 相邻方块，检查它们缩放后是否有空隙
                    int[] scaledCoordA = scaleCoordinate(coordA, pivot, scaleFactors);
                    int[] scaledCoordB = scaleCoordinate(coordB, pivot, scaleFactors);
                    
                    // 计算缩放后的曼哈顿距离
                    int scaledDistance = 
                        Math.abs(scaledCoordA[0] - scaledCoordB[0]) +
                        Math.abs(scaledCoordA[1] - scaledCoordB[1]) +
                        Math.abs(scaledCoordA[2] - scaledCoordB[2]);
                    
                    // 如果缩放后距离大于1，需要填充空隙
                    if (scaledDistance > 1) {
                        fillLine(scaledCoordA, scaledCoordB, uniqueCoords, result);
                    }
                }
            }
        }
    }
    
    /**
     * 在两点之间填充线段
     */
    private void fillLine(int[] start, int[] end, Set<String> uniqueCoords, List<int[]> result) {
        // 确定每个轴的方向和步数
        int dx = Integer.compare(end[0], start[0]);
        int dy = Integer.compare(end[1], start[1]);
        int dz = Integer.compare(end[2], start[2]);
        
        // 当前位置
        int x = start[0];
        int y = start[1];
        int z = start[2];
        
        // 填充直线上的所有点
        while (x != end[0] || y != end[1] || z != end[2]) {
            // 优先移动距离较大的轴
            int distX = Math.abs(end[0] - x);
            int distY = Math.abs(end[1] - y);
            int distZ = Math.abs(end[2] - z);
            
            if (distX >= distY && distX >= distZ && x != end[0]) {
                x += dx;
            } else if (distY >= distX && distY >= distZ && y != end[1]) {
                y += dy;
            } else if (z != end[2]) {
                z += dz;
            }
            
            // 添加新点（避免重复）
            String coordKey = x + "," + y + "," + z;
            if (!uniqueCoords.contains(coordKey)) {
                uniqueCoords.add(coordKey);
                result.add(new int[]{x, y, z});
            }
        }
    }
    
    /**
     * 缩放单个坐标
     * @param coord 原始坐标[x,y,z]
     * @param pivot 缩放轴心点[x,y,z]
     * @param scaleFactors 各轴缩放比例[x,y,z]
     * @return 缩放后的坐标
     */
    private int[] scaleCoordinate(float[] coord, float[] pivot, float[] scaleFactors) {
        float[] scaled = new float[3];
        
        // 应用缩放公式: newPos = pivot + (pos - pivot) * scale
        for (int i = 0; i < 3; i++) {
            scaled[i] = pivot[i] + (coord[i] - pivot[i]) * scaleFactors[i];
        }
        
        // 将浮点坐标转换为整数坐标（四舍五入）
        return new int[]{
            Math.round(scaled[0]),
            Math.round(scaled[1]),
            Math.round(scaled[2])
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
} 