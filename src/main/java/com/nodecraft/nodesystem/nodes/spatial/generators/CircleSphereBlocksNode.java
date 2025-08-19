package com.nodecraft.nodesystem.nodes.spatial.generators;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Circle / Sphere (Blocks) 节点: 生成圆形或球形区域的坐标列表
 */
@NodeInfo(
    id = "spatial.generators.circle_sphere_blocks",
    displayName = "圆形/球形生成器",
    description = "生成圆形或球形区域的坐标列表",
    category = "spatial.generators"
)
public class CircleSphereBlocksNode extends BaseNode {

    // --- 节点属性 ---
    private boolean is3D = false; // 默认生成2D圆形
    private boolean hollow = false; // 默认为实心
    private boolean useEuclideanDistance = true; // 默认使用欧几里得距离（真圆/球）

    // --- 输入端口 IDs ---
    private static final String INPUT_CENTER_ID = "input_center";
    private static final String INPUT_RADIUS_ID = "input_radius";
    private static final String INPUT_THICKNESS_ID = "input_thickness";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_BLOCKS_ID = "output_blocks";

    // --- 构造函数 ---
    public CircleSphereBlocksNode() {
        super(UUID.randomUUID(), "spatial.generators.circle_sphere_blocks");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_CENTER_ID, "Center", 
                "The center point of the circle/sphere", NodeDataType.BLOCK_POS, this));
        addInputPort(new BasePort(INPUT_RADIUS_ID, "Radius", 
                "The radius of the circle/sphere", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_THICKNESS_ID, "Thickness", 
                "Shell thickness (for hollow shapes)", NodeDataType.INTEGER, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_BLOCKS_ID, "Blocks", 
                "The blocks forming the circle/sphere", NodeDataType.BLOCK_LIST, this));
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "Generates a circle or sphere of blocks";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "Circle / Sphere (Blocks)";
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object centerObj = inputValues.get(INPUT_CENTER_ID);
        Object radiusObj = inputValues.get(INPUT_RADIUS_ID);
        Object thicknessObj = inputValues.get(INPUT_THICKNESS_ID);
        
        // 默认空的坐标列表
        BlockPosList result = new BlockPosList();
        
        // 检查输入是否合法
        if (centerObj instanceof BlockPos && radiusObj instanceof Number) {
            BlockPos center = (BlockPos) centerObj;
            double radius = ((Number) radiusObj).doubleValue();
            int thickness = 1; // 默认壁厚为1
            
            if (thicknessObj instanceof Number) {
                thickness = ((Number) thicknessObj).intValue();
                thickness = Math.max(1, thickness); // 确保壁厚最小为1
            }
            
            // 确保半径为正数
            radius = Math.max(1, radius);
            
            // 计算内径（用于空心形状）
            double innerRadius = hollow ? Math.max(0, radius - thickness) : 0;
            
            // 根据形状类型生成方块
            if (is3D) {
                generateSphere(center, radius, innerRadius, result);
            } else {
                generateCircle(center, radius, innerRadius, result);
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_BLOCKS_ID, result);
    }
    
    /**
     * 生成圆形（2D）
     */
    private void generateCircle(BlockPos center, double radius, double innerRadius, BlockPosList result) {
        int x0 = center.getX();
        int y0 = center.getY();
        int z0 = center.getZ();
        
        int radiusCeil = (int) Math.ceil(radius);
        
        // 在XZ平面上生成圆形
        for (int dx = -radiusCeil; dx <= radiusCeil; dx++) {
            for (int dz = -radiusCeil; dz <= radiusCeil; dz++) {
                // 计算点到中心的距离
                double distance;
                if (useEuclideanDistance) {
                    // 欧几里得距离（真圆）
                    distance = Math.sqrt(dx * dx + dz * dz);
                } else {
                    // 曼哈顿距离（菱形）
                    distance = Math.abs(dx) + Math.abs(dz);
                }
                
                // 基于距离确定是否添加此点
                if (distance <= radius && (!hollow || distance >= innerRadius)) {
                    result.add(new BlockPos(x0 + dx, y0, z0 + dz));
                }
            }
        }
    }
    
    /**
     * 生成球体（3D）
     */
    private void generateSphere(BlockPos center, double radius, double innerRadius, BlockPosList result) {
        int x0 = center.getX();
        int y0 = center.getY();
        int z0 = center.getZ();
        
        int radiusCeil = (int) Math.ceil(radius);
        
        // 在三维空间生成球体
        for (int dx = -radiusCeil; dx <= radiusCeil; dx++) {
            for (int dy = -radiusCeil; dy <= radiusCeil; dy++) {
                for (int dz = -radiusCeil; dz <= radiusCeil; dz++) {
                    // 计算点到中心的距离
                    double distance;
                    if (useEuclideanDistance) {
                        // 欧几里得距离（真球）
                        distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    } else {
                        // 曼哈顿距离（八面体）
                        distance = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
                    }
                    
                    // 基于距离确定是否添加此点
                    if (distance <= radius && (!hollow || distance >= innerRadius)) {
                        result.add(new BlockPos(x0 + dx, y0 + dy, z0 + dz));
                    }
                }
            }
        }
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean is3D() {
        return is3D;
    }
    
    public void set3D(boolean is3D) {
        this.is3D = is3D;
        markDirty();
    }
    
    public boolean isHollow() {
        return hollow;
    }
    
    public void setHollow(boolean hollow) {
        this.hollow = hollow;
        markDirty();
    }
    
    public boolean isUseEuclideanDistance() {
        return useEuclideanDistance;
    }
    
    public void setUseEuclideanDistance(boolean useEuclideanDistance) {
        this.useEuclideanDistance = useEuclideanDistance;
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("is3D", is3D);
        state.put("hollow", hollow);
        state.put("useEuclideanDistance", useEuclideanDistance);
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("is3D")) {
                Object is3DObj = stateMap.get("is3D");
                if (is3DObj instanceof Boolean) {
                    set3D((Boolean) is3DObj);
                }
            }
            
            if (stateMap.containsKey("hollow")) {
                Object hollowObj = stateMap.get("hollow");
                if (hollowObj instanceof Boolean) {
                    setHollow((Boolean) hollowObj);
                }
            }
            
            if (stateMap.containsKey("useEuclideanDistance")) {
                Object distanceObj = stateMap.get("useEuclideanDistance");
                if (distanceObj instanceof Boolean) {
                    setUseEuclideanDistance((Boolean) distanceObj);
                }
            }
        }
    }
} 