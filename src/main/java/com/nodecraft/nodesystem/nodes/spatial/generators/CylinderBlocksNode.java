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
 * Cylinder (Blocks) 节点: 生成圆柱体区域的坐标列表
 */
@NodeInfo(
    id = "spatial.generators.cylinder_blocks",
    displayName = "圆柱体生成器",
    description = "生成圆柱体区域的坐标列表",
    category = "spatial.generators"
)
public class CylinderBlocksNode extends BaseNode {

    // --- 节点属性 ---
    private boolean hollow = false; // 默认为实心
    private boolean capEnds = true; // 默认封闭两端
    private boolean useEuclideanDistance = true; // 默认使用欧几里得距离（真圆）

    // --- 输入端口 IDs ---
    private static final String INPUT_START_ID = "input_start";
    private static final String INPUT_END_ID = "input_end";
    private static final String INPUT_RADIUS_ID = "input_radius";
    private static final String INPUT_THICKNESS_ID = "input_thickness";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_CYLINDER_BLOCKS_ID = "output_cylinder_blocks";

    // --- 构造函数 ---
    public CylinderBlocksNode() {
        super(UUID.randomUUID(), "spatial.generators.cylinder_blocks");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_START_ID, "Start Point", 
                "The starting point of the cylinder axis", NodeDataType.BLOCK_POS, this));
        addInputPort(new BasePort(INPUT_END_ID, "End Point", 
                "The ending point of the cylinder axis", NodeDataType.BLOCK_POS, this));
        addInputPort(new BasePort(INPUT_RADIUS_ID, "Radius", 
                "The radius of the cylinder", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_THICKNESS_ID, "Thickness", 
                "Shell thickness (for hollow cylinders)", NodeDataType.INTEGER, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_CYLINDER_BLOCKS_ID, "Cylinder Blocks", 
                "The blocks forming the cylinder", NodeDataType.BLOCK_LIST, this));
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "Generates a cylinder of blocks";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "Cylinder (Blocks)";
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object startObj = inputValues.get(INPUT_START_ID);
        Object endObj = inputValues.get(INPUT_END_ID);
        Object radiusObj = inputValues.get(INPUT_RADIUS_ID);
        Object thicknessObj = inputValues.get(INPUT_THICKNESS_ID);
        
        // 默认空的坐标列表
        BlockPosList result = new BlockPosList();
        
        // 检查输入是否合法
        if (startObj instanceof BlockPos && 
            endObj instanceof BlockPos && 
            radiusObj instanceof Number) {
            
            BlockPos start = (BlockPos) startObj;
            BlockPos end = (BlockPos) endObj;
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
            
            // 计算圆柱体轴向矢量
            int axisX = end.getX() - start.getX();
            int axisY = end.getY() - start.getY();
            int axisZ = end.getZ() - start.getZ();
            
            // 找出主轴（最长轴）
            int absX = Math.abs(axisX);
            int absY = Math.abs(axisY);
            int absZ = Math.abs(axisZ);
            
            // 圆柱体轴向是X轴、Y轴还是Z轴的变体
            boolean isAlongX = (absX >= absY && absX >= absZ);
            boolean isAlongY = (absY >= absX && absY >= absZ);
            boolean isAlongZ = (absZ >= absX && absZ >= absY);
            
            // 生成圆柱体
            generateCylinder(start, end, radius, innerRadius, thickness, isAlongX, isAlongY, isAlongZ, result);
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_CYLINDER_BLOCKS_ID, result);
    }
    
    /**
     * 生成圆柱体
     */
    private void generateCylinder(BlockPos start, BlockPos end, double radius, double innerRadius,
                               int thickness, boolean isAlongX, boolean isAlongY, boolean isAlongZ,
                               BlockPosList result) {
        
        int x1 = start.getX(), y1 = start.getY(), z1 = start.getZ();
        int x2 = end.getX(), y2 = end.getY(), z2 = end.getZ();
        
        // 确保起点小于终点
        if (x2 < x1) { int temp = x1; x1 = x2; x2 = temp; }
        if (y2 < y1) { int temp = y1; y1 = y2; y2 = temp; }
        if (z2 < z1) { int temp = z1; z1 = z2; z2 = temp; }
        
        int radiusCeil = (int) Math.ceil(radius);
        
        // 对于X轴为主要轴的圆柱体
        if (isAlongX) {
            // 沿着X轴枚举每个切片
            for (int x = x1; x <= x2; x++) {
                boolean isEnd = (x == x1 || x == x2); // 是否为圆柱体端点
                
                // 生成圆形截面
                for (int dy = -radiusCeil; dy <= radiusCeil; dy++) {
                    for (int dz = -radiusCeil; dz <= radiusCeil; dz++) {
                        double distance;
                        if (useEuclideanDistance) {
                            distance = Math.sqrt(dy * dy + dz * dz);
                        } else {
                            distance = Math.abs(dy) + Math.abs(dz);
                        }
                        
                        // 用于确定是否添加方块的条件
                        boolean inRadiusRange = distance <= radius && (!hollow || distance >= innerRadius);
                        boolean isShellPoint = distance <= radius && distance >= radius - thickness;
                        
                        // 如果是实心或在壳范围内，或者是端点并且需要封盖
                        if (inRadiusRange && (!hollow || isShellPoint || (isEnd && capEnds))) {
                            result.add(new BlockPos(x, y1 + dy, z1 + dz));
                        }
                    }
                }
            }
        } 
        // 对于Y轴为主要轴的圆柱体
        else if (isAlongY) {
            // 沿着Y轴枚举每个切片
            for (int y = y1; y <= y2; y++) {
                boolean isEnd = (y == y1 || y == y2); // 是否为圆柱体端点
                
                // 生成圆形截面
                for (int dx = -radiusCeil; dx <= radiusCeil; dx++) {
                    for (int dz = -radiusCeil; dz <= radiusCeil; dz++) {
                        double distance;
                        if (useEuclideanDistance) {
                            distance = Math.sqrt(dx * dx + dz * dz);
                        } else {
                            distance = Math.abs(dx) + Math.abs(dz);
                        }
                        
                        // 用于确定是否添加方块的条件
                        boolean inRadiusRange = distance <= radius && (!hollow || distance >= innerRadius);
                        boolean isShellPoint = distance <= radius && distance >= radius - thickness;
                        
                        // 如果是实心或在壳范围内，或者是端点并且需要封盖
                        if (inRadiusRange && (!hollow || isShellPoint || (isEnd && capEnds))) {
                            result.add(new BlockPos(x1 + dx, y, z1 + dz));
                        }
                    }
                }
            }
        } 
        // 对于Z轴为主要轴的圆柱体（默认）
        else {
            // 沿着Z轴枚举每个切片
            for (int z = z1; z <= z2; z++) {
                boolean isEnd = (z == z1 || z == z2); // 是否为圆柱体端点
                
                // 生成圆形截面
                for (int dx = -radiusCeil; dx <= radiusCeil; dx++) {
                    for (int dy = -radiusCeil; dy <= radiusCeil; dy++) {
                        double distance;
                        if (useEuclideanDistance) {
                            distance = Math.sqrt(dx * dx + dy * dy);
                        } else {
                            distance = Math.abs(dx) + Math.abs(dy);
                        }
                        
                        // 用于确定是否添加方块的条件
                        boolean inRadiusRange = distance <= radius && (!hollow || distance >= innerRadius);
                        boolean isShellPoint = distance <= radius && distance >= radius - thickness;
                        
                        // 如果是实心或在壳范围内，或者是端点并且需要封盖
                        if (inRadiusRange && (!hollow || isShellPoint || (isEnd && capEnds))) {
                            result.add(new BlockPos(x1 + dx, y1 + dy, z));
                        }
                    }
                }
            }
        }
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isHollow() {
        return hollow;
    }
    
    public void setHollow(boolean hollow) {
        this.hollow = hollow;
        markDirty();
    }
    
    public boolean isCapEnds() {
        return capEnds;
    }
    
    public void setCapEnds(boolean capEnds) {
        this.capEnds = capEnds;
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
        state.put("hollow", hollow);
        state.put("capEnds", capEnds);
        state.put("useEuclideanDistance", useEuclideanDistance);
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("hollow")) {
                Object hollowObj = stateMap.get("hollow");
                if (hollowObj instanceof Boolean) {
                    setHollow((Boolean) hollowObj);
                }
            }
            
            if (stateMap.containsKey("capEnds")) {
                Object capEndsObj = stateMap.get("capEnds");
                if (capEndsObj instanceof Boolean) {
                    setCapEnds((Boolean) capEndsObj);
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