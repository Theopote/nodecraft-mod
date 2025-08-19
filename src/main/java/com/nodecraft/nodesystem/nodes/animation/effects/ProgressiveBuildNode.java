package com.nodecraft.nodesystem.nodes.animation.effects;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Progressive Build Node: 逐步构建节点
 * 根据时间因子逐个或逐组地显示几何体，实现"生长"效果
 */
@NodeInfo(
    id = "animation.effects.progressive_build",
    displayName = "Progressive Build",
    description = "随时间逐步显示几何体",
    category = "animation.effects"
)
public class ProgressiveBuildNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_GEOMETRY_ID = "input_geometry";
    private static final String INPUT_TIME_FACTOR_ID = "input_time_factor";
    private static final String INPUT_BUILD_ORDER_ID = "input_build_order";
    private static final String INPUT_GROUP_SIZE_ID = "input_group_size";
    private static final String INPUT_REFERENCE_POINT_ID = "input_reference_point";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_VISIBLE_GEOMETRY_ID = "output_visible_geometry";
    
    // --- 构建顺序枚举 ---
    public enum BuildOrder {
        RANDOM(0, "Random", "随机顺序"),
        BY_HEIGHT(1, "By Height", "按高度从下到上"),
        BY_DISTANCE(2, "By Distance", "按到参考点的距离"),
        SEQUENTIAL(3, "Sequential", "按索引顺序");
        
        private final int id;
        private final String name;
        private final String description;
        
        BuildOrder(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
        
        public static BuildOrder fromId(int id) {
            for (BuildOrder order : values()) {
                if (order.id == id) {
                    return order;
                }
            }
            return SEQUENTIAL; // 默认顺序
        }
    }
    
    // 随机数生成器（用于随机顺序）
    private final Random random = new Random();
    
    // --- 构造函数 ---
    public ProgressiveBuildNode() {
        super(UUID.randomUUID(), "animation.effects.progressive_build");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_GEOMETRY_ID, "Full Geometry", "完整的几何体", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_TIME_FACTOR_ID, "Time Factor", "时间因子（0-1）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_BUILD_ORDER_ID, "Build Order", "构建顺序（0=随机, 1=按高度, 2=按距离, 3=按索引）", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_GROUP_SIZE_ID, "Group Size", "每次显示的方块数", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_REFERENCE_POINT_ID, "Reference Point", "参考点（用于距离排序）", NodeDataType.VECTOR, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_VISIBLE_GEOMETRY_ID, "Visible Geometry", "当前可见的几何体", NodeDataType.LIST, this));
    }
    
    @Override
    public String getDescription() {
        return "随时间逐步显示几何体";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object geometryObj = inputValues.get(INPUT_GEOMETRY_ID);
        Float timeFactor = (Float) inputValues.getOrDefault(INPUT_TIME_FACTOR_ID, 0.0f);
        Integer buildOrderId = (Integer) inputValues.getOrDefault(INPUT_BUILD_ORDER_ID, BuildOrder.SEQUENTIAL.id);
        Integer groupSize = (Integer) inputValues.getOrDefault(INPUT_GROUP_SIZE_ID, 1);
        float[] referencePoint = (float[]) inputValues.getOrDefault(INPUT_REFERENCE_POINT_ID, new float[]{0.0f, 0.0f, 0.0f});
        
        // 确保时间因子在0-1范围内
        timeFactor = Math.max(0.0f, Math.min(1.0f, timeFactor));
        
        // 确保组大小至少为1
        groupSize = Math.max(1, groupSize);
        
        // 确保参考点有效
        if (referencePoint.length < 3) {
            referencePoint = new float[]{0.0f, 0.0f, 0.0f};
        }
        
        // 获取构建顺序
        BuildOrder buildOrder = BuildOrder.fromId(buildOrderId);
        
        // 处理几何体
        List<Object> visibleGeometry = processGeometry(geometryObj, timeFactor, buildOrder, groupSize, referencePoint);
        
        // 设置输出值
        outputValues.put(OUTPUT_VISIBLE_GEOMETRY_ID, visibleGeometry);
    }
    
    /**
     * 处理几何体，根据时间因子决定显示哪些部分
     */
    private List<Object> processGeometry(Object geometryObj, float timeFactor, BuildOrder buildOrder, int groupSize, float[] referencePoint) {
        List<Object> result = new ArrayList<>();
        
        if (geometryObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> geometryList = (List<Object>) geometryObj;
            
            // 如果几何体为空，直接返回空列表
            if (geometryList.isEmpty()) {
                return result;
            }
            
            // 根据构建顺序对几何体排序
            List<Object> sortedGeometry = sortGeometry(geometryList, buildOrder, referencePoint);
            
            // 计算要显示的方块数量
            int totalBlocks = sortedGeometry.size();
            int visibleBlocks = Math.min(totalBlocks, Math.round(totalBlocks * timeFactor));
            
            // 确保visibleBlocks是groupSize的倍数
            visibleBlocks = (visibleBlocks / groupSize) * groupSize;
            
            // 添加可见的方块到结果
            for (int i = 0; i < visibleBlocks; i++) {
                result.add(sortedGeometry.get(i));
            }
        }
        
        return result;
    }
    
    /**
     * 根据构建顺序对几何体排序
     */
    private List<Object> sortGeometry(List<Object> geometry, BuildOrder buildOrder, float[] referencePoint) {
        List<Object> sortedGeometry = new ArrayList<>(geometry);
        
        switch (buildOrder) {
            case RANDOM:
                // 随机排序
                Collections.shuffle(sortedGeometry, random);
                break;
                
            case BY_HEIGHT:
                // 按Y坐标（高度）排序，从低到高
                sortedGeometry.sort(Comparator.comparingDouble(block -> getCoordinateY(block)));
                break;
                
            case BY_DISTANCE:
                // 按到参考点的距离排序，从近到远
                final float[] refPoint = referencePoint;
                sortedGeometry.sort(Comparator.comparingDouble(block -> getDistanceToPoint(block, refPoint)));
                break;
                
            case SEQUENTIAL:
                // 已经按索引顺序，不需要额外排序
                break;
        }
        
        return sortedGeometry;
    }
    
    /**
     * 获取方块的Y坐标
     */
    private double getCoordinateY(Object block) {
        float[] coord = getCoordinateAsFloatArray(block);
        return (coord != null) ? coord[1] : 0.0;
    }
    
    /**
     * 计算方块到点的距离
     */
    private double getDistanceToPoint(Object block, float[] point) {
        float[] coord = getCoordinateAsFloatArray(block);
        if (coord == null) return Double.MAX_VALUE;
        
        double dx = coord[0] - point[0];
        double dy = coord[1] - point[1];
        double dz = coord[2] - point[2];
        
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    /**
     * 将坐标对象转换为浮点数组
     */
    private float[] getCoordinateAsFloatArray(Object coordObj) {
        // 处理int[]格式的坐标
        if (coordObj instanceof int[]) {
            int[] coord = (int[]) coordObj;
            if (coord.length >= 3) {
                return new float[]{(float) coord[0], (float) coord[1], (float) coord[2]};
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