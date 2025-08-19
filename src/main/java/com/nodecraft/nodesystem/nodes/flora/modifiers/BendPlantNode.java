package com.nodecraft.nodesystem.nodes.flora.modifiers;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.PlantStructure;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Bend Plant 节点: 弯曲植物结构
 */
@NodeInfo(
    id = "flora.modifiers.bend_plant",
    displayName = "Bend Plant",
    description = "Bends plant structures",
    category = "flora.modifiers"
)
public class BendPlantNode extends BaseNode {
    
    /**
     * 弯曲类型枚举
     */
    public enum BendType {
        GRAVITY("Gravity", "重力弯曲"),
        WIND("Wind", "风力弯曲"),
        CURVE("Curve", "曲线弯曲"),
        SPIRAL("Spiral", "螺旋弯曲");
        
        private final String id;
        private final String displayName;
        
        BendType(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
        
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        
        public static BendType fromString(String str) {
            for (BendType type : values()) {
                if (type.id.equalsIgnoreCase(str) || type.displayName.equals(str)) {
                    return type;
                }
            }
            return GRAVITY; // 默认返回重力弯曲
        }
    }
    
    // --- 节点属性 ---
    private BendType bendType = BendType.GRAVITY;     // 弯曲类型
    private float bendStrength = 0.3f;                // 弯曲强度（0-1）
    private float bendDirection = 0.0f;               // 弯曲方向（度，0=东，90=北，180=西，270=南）
    private float heightFactor = 0.7f;                // 高度影响因子（0-1）
    private boolean affectBranches = true;            // 是否影响分支
    private boolean affectLeaves = false;             // 是否影响叶子
    private String description = "弯曲植物结构，模拟重力、风力等效果";
    
    // --- 输入端口 IDs ---
    private static final String INPUT_PLANT_STRUCTURE_ID = "input_plant_structure";
    private static final String INPUT_BEND_TYPE_ID = "input_bend_type";
    private static final String INPUT_BEND_STRENGTH_ID = "input_bend_strength";
    private static final String INPUT_BEND_DIRECTION_ID = "input_bend_direction";
    private static final String INPUT_HEIGHT_FACTOR_ID = "input_height_factor";
    private static final String INPUT_AFFECT_BRANCHES_ID = "input_affect_branches";
    private static final String INPUT_AFFECT_LEAVES_ID = "input_affect_leaves";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_PLANT_STRUCTURE_ID = "output_plant_structure";
    private static final String OUTPUT_BEND_INFO_ID = "output_bend_info";
    
    /**
     * 构造一个新的弯曲植物节点
     */
    public BendPlantNode() {
        super(UUID.randomUUID(), "flora.modifiers.bend_plant");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_PLANT_STRUCTURE_ID, "Plant Structure", 
                "要弯曲的植物结构", NodeDataType.PLANT_STRUCTURE, this));
        addInputPort(new BasePort(INPUT_BEND_TYPE_ID, "Bend Type", 
                "弯曲类型（Gravity、Wind、Curve、Spiral）", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_BEND_STRENGTH_ID, "Bend Strength", 
                "弯曲强度（0-1）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_BEND_DIRECTION_ID, "Bend Direction", 
                "弯曲方向（度）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_HEIGHT_FACTOR_ID, "Height Factor", 
                "高度影响因子（0-1）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_AFFECT_BRANCHES_ID, "Affect Branches", 
                "是否影响分支", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_AFFECT_LEAVES_ID, "Affect Leaves", 
                "是否影响叶子", NodeDataType.BOOLEAN, this));
        
        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_PLANT_STRUCTURE_ID, "Bent Plant Structure", 
                "弯曲后的植物结构", NodeDataType.PLANT_STRUCTURE, this));
        addOutputPort(new BasePort(OUTPUT_BEND_INFO_ID, "Bend Info", 
                "弯曲操作的详细信息", NodeDataType.STRING, this));
    }
    
    @Override
    public String getDescription() {
        return this.description;
    }
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        PlantStructure inputPlant = getInputValue(INPUT_PLANT_STRUCTURE_ID, null);
        String bendTypeStr = getInputValue(INPUT_BEND_TYPE_ID, this.bendType.getId());
        Float bendStrengthValue = getInputValue(INPUT_BEND_STRENGTH_ID, this.bendStrength);
        Float bendDirectionValue = getInputValue(INPUT_BEND_DIRECTION_ID, this.bendDirection);
        Float heightFactorValue = getInputValue(INPUT_HEIGHT_FACTOR_ID, this.heightFactor);
        Boolean affectBranchesValue = getInputValue(INPUT_AFFECT_BRANCHES_ID, this.affectBranches);
        Boolean affectLeavesValue = getInputValue(INPUT_AFFECT_LEAVES_ID, this.affectLeaves);
        
        // 默认输出值
        PlantStructure bentPlant = new PlantStructure();
        String bendInfo = "No plant to bend";
        
        if (inputPlant != null && !inputPlant.isEmpty()) {
            try {
                // 验证弯曲参数
                BendType currentBendType = BendType.fromString(bendTypeStr);
                bendStrengthValue = Math.max(0.0f, Math.min(1.0f, bendStrengthValue != null ? bendStrengthValue : 0.3f));
                bendDirectionValue = normalizeAngle(bendDirectionValue != null ? bendDirectionValue : 0.0f);
                heightFactorValue = Math.max(0.0f, Math.min(1.0f, heightFactorValue != null ? heightFactorValue : 0.7f));
                affectBranchesValue = affectBranchesValue != null ? affectBranchesValue : true;
                affectLeavesValue = affectLeavesValue != null ? affectLeavesValue : false;
                
                // 如果弯曲强度为0，直接返回原植物
                if (bendStrengthValue < 0.01f) {
                    bentPlant = inputPlant.copy();
                    bendInfo = "No bending applied (strength is 0)";
                } else {
                    // 执行弯曲
                    bentPlant = bendFullPlant(inputPlant, currentBendType, bendStrengthValue, 
                                            bendDirectionValue, heightFactorValue, affectBranchesValue, affectLeavesValue);
                    
                    // 复制原始元数据并添加弯曲信息
                    if (inputPlant.getMetadata() != null) {
                        for (java.util.Map.Entry<String, Object> entry : inputPlant.getMetadata().entrySet()) {
                            bentPlant.setMetadata(entry.getKey(), entry.getValue());
                        }
                    }
                    bentPlant.setMetadata("bend_type", currentBendType.getId());
                    bentPlant.setMetadata("bend_strength", bendStrengthValue);
                    bentPlant.setMetadata("bend_direction", bendDirectionValue);
                    bentPlant.setMetadata("height_factor", heightFactorValue);
                    
                    // 生成弯曲信息
                    bendInfo = String.format("Bent: %s, Strength: %.1f%%, Direction: %.1f°, Blocks: %d",
                        currentBendType.getDisplayName(), bendStrengthValue * 100, bendDirectionValue,
                        bentPlant.getTotalBlockCount());
                }
                
            } catch (Exception e) {
                System.err.println("Error in Bend Plant: " + e.getMessage());
                e.printStackTrace();
                bentPlant = inputPlant; // 返回原始植物
                bendInfo = "Error during bending";
            }
        }
        
        // 设置输出
        outputValues.put(OUTPUT_PLANT_STRUCTURE_ID, bentPlant);
        outputValues.put(OUTPUT_BEND_INFO_ID, bendInfo);
    }
    
    /**
     * 弯曲整个植物结构
     */
    private PlantStructure bendFullPlant(PlantStructure original, BendType bendType, float bendStrength,
                                       float bendDirection, float heightFactor, boolean affectBranches, boolean affectLeaves) {
        PlantStructure bent = new PlantStructure();
        
        // 计算植物的高度范围
        int minY = getMinY(original);
        int maxY = getMaxY(original);
        int heightRange = maxY - minY + 1;
        
        // 计算弯曲基点（通常是植物底部）
        BlockPos basePoint = calculatePlantBase(original);
        
        // 转换弯曲方向到弧度
        double bendRadians = Math.toRadians(bendDirection);
        double bendDirX = Math.cos(bendRadians);
        double bendDirZ = Math.sin(bendRadians);
        
        // 弯曲主干（主干总是受影响）
        for (PlantStructure.PlantBlock block : original.getTrunkBlocks()) {
            BlockPos bentPos = calculateBentPosition(block.getPosition(), basePoint, bendType, 
                                                   bendStrength, bendDirX, bendDirZ, heightFactor, minY, heightRange);
            bent.addTrunkBlock(bentPos, block.getBlockType(), block.getThickness());
        }
        
        // 弯曲分支（如果启用）
        if (affectBranches) {
            for (PlantStructure.PlantBlock block : original.getBranchBlocks()) {
                BlockPos bentPos = calculateBentPosition(block.getPosition(), basePoint, bendType, 
                                                       bendStrength, bendDirX, bendDirZ, heightFactor, minY, heightRange);
                bent.addBranchBlock(bentPos, block.getBlockType(), block.getThickness());
            }
        } else {
            bent.addBranchBlocks(original.getBranchBlocks());
        }
        
        // 弯曲叶子（如果启用）
        if (affectLeaves) {
            for (PlantStructure.PlantBlock block : original.getLeafBlocks()) {
                BlockPos bentPos = calculateBentPosition(block.getPosition(), basePoint, bendType, 
                                                       bendStrength, bendDirX, bendDirZ, heightFactor, minY, heightRange);
                bent.addLeafBlock(bentPos, block.getBlockType());
            }
        } else {
            bent.addLeafBlocks(original.getLeafBlocks());
        }
        
        // 花朵跟随叶子的设置
        if (affectLeaves) {
            for (PlantStructure.PlantBlock block : original.getFlowerBlocks()) {
                BlockPos bentPos = calculateBentPosition(block.getPosition(), basePoint, bendType, 
                                                       bendStrength, bendDirX, bendDirZ, heightFactor, minY, heightRange);
                bent.addFlowerBlock(bentPos, block.getBlockType());
            }
        } else {
            bent.addFlowerBlocks(original.getFlowerBlocks());
        }
        
        // 根系不受弯曲影响
        bent.addRootBlocks(original.getRootBlocks());
        
        return bent;
    }
    
    /**
     * 计算弯曲后的位置
     */
    private BlockPos calculateBentPosition(BlockPos original, BlockPos basePoint, BendType bendType,
                                         float bendStrength, double bendDirX, double bendDirZ,
                                         float heightFactor, int minY, int heightRange) {
        // 计算相对高度（0-1）
        double relativeHeight = heightRange > 0 ? (double)(original.getY() - minY) / heightRange : 0;
        
        // 计算高度影响
        double heightInfluence = Math.pow(relativeHeight, heightFactor);
        
        // 根据弯曲类型计算偏移
        double offsetX = 0, offsetZ = 0;
        
        switch (bendType) {
            case GRAVITY:
                // 重力弯曲：越高弯曲越明显，呈抛物线
                double gravityFactor = heightInfluence * heightInfluence * bendStrength * 3.0;
                offsetX = bendDirX * gravityFactor;
                offsetZ = bendDirZ * gravityFactor;
                break;
                
            case WIND:
                // 风力弯曲：线性弯曲，顶部最大
                double windFactor = heightInfluence * bendStrength * 2.0;
                offsetX = bendDirX * windFactor;
                offsetZ = bendDirZ * windFactor;
                break;
                
            case CURVE:
                // 曲线弯曲：正弦曲线弯曲
                double curveFactor = Math.sin(relativeHeight * Math.PI) * bendStrength * 2.0;
                offsetX = bendDirX * curveFactor;
                offsetZ = bendDirZ * curveFactor;
                break;
                
            case SPIRAL:
                // 螺旋弯曲：随高度旋转
                double spiralAngle = relativeHeight * Math.PI * 2 * bendStrength;
                double spiralRadius = heightInfluence * bendStrength * 1.5;
                offsetX = Math.cos(spiralAngle) * spiralRadius;
                offsetZ = Math.sin(spiralAngle) * spiralRadius;
                break;
        }
        
        return new BlockPos(
            (int) Math.round(original.getX() + offsetX),
            original.getY(),
            (int) Math.round(original.getZ() + offsetZ)
        );
    }
    
    /**
     * 计算植物的底部中心点
     */
    private BlockPos calculatePlantBase(PlantStructure plant) {
        java.util.List<PlantStructure.PlantBlock> allBlocks = plant.getAllBlocks();
        if (allBlocks.isEmpty()) {
            return new BlockPos(0, 0, 0);
        }
        
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        
        for (PlantStructure.PlantBlock block : allBlocks) {
            BlockPos pos = block.getPosition();
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        
        return new BlockPos(
            (minX + maxX) / 2,
            minY, // 使用最低点作为底部
            (minZ + maxZ) / 2
        );
    }
    
    /**
     * 获取植物的最小Y坐标
     */
    private int getMinY(PlantStructure plant) {
        java.util.List<PlantStructure.PlantBlock> allBlocks = plant.getAllBlocks();
        if (allBlocks.isEmpty()) return 0;
        
        int minY = Integer.MAX_VALUE;
        for (PlantStructure.PlantBlock block : allBlocks) {
            minY = Math.min(minY, block.getPosition().getY());
        }
        return minY;
    }
    
    /**
     * 获取植物的最大Y坐标
     */
    private int getMaxY(PlantStructure plant) {
        java.util.List<PlantStructure.PlantBlock> allBlocks = plant.getAllBlocks();
        if (allBlocks.isEmpty()) return 0;
        
        int maxY = Integer.MIN_VALUE;
        for (PlantStructure.PlantBlock block : allBlocks) {
            maxY = Math.max(maxY, block.getPosition().getY());
        }
        return maxY;
    }
    
    /**
     * 标准化角度到0-360度范围
     */
    private float normalizeAngle(float angle) {
        while (angle < 0) angle += 360;
        while (angle >= 360) angle -= 360;
        return angle;
    }
    
    /**
     * 获取输入值的辅助方法
     */
    @SuppressWarnings("unchecked")
    private <T> T getInputValue(String portId, T defaultValue) {
        Object value = inputValues.get(portId);
        if (value != null) {
            try {
                return (T) value;
            } catch (ClassCastException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    // --- Getters and Setters ---
    
    public BendType getBendType() {
        return bendType;
    }
    
    public void setBendType(BendType bendType) {
        this.bendType = bendType != null ? bendType : BendType.GRAVITY;
        markDirty();
    }
    
    public void setBendType(String bendTypeStr) {
        setBendType(BendType.fromString(bendTypeStr));
    }
    
    public float getBendStrength() {
        return bendStrength;
    }
    
    public void setBendStrength(float bendStrength) {
        this.bendStrength = Math.max(0.0f, Math.min(1.0f, bendStrength));
        markDirty();
    }
    
    public float getBendDirection() {
        return bendDirection;
    }
    
    public void setBendDirection(float bendDirection) {
        this.bendDirection = normalizeAngle(bendDirection);
        markDirty();
    }
    
    public float getHeightFactor() {
        return heightFactor;
    }
    
    public void setHeightFactor(float heightFactor) {
        this.heightFactor = Math.max(0.0f, Math.min(1.0f, heightFactor));
        markDirty();
    }
    
    public boolean isAffectBranches() {
        return affectBranches;
    }
    
    public void setAffectBranches(boolean affectBranches) {
        this.affectBranches = affectBranches;
        markDirty();
    }
    
    public boolean isAffectLeaves() {
        return affectLeaves;
    }
    
    public void setAffectLeaves(boolean affectLeaves) {
        this.affectLeaves = affectLeaves;
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("bendType", getBendType().getId());
        state.put("bendStrength", getBendStrength());
        state.put("bendDirection", getBendDirection());
        state.put("heightFactor", getHeightFactor());
        state.put("affectBranches", isAffectBranches());
        state.put("affectLeaves", isAffectLeaves());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("bendType")) {
                Object bendTypeObj = stateMap.get("bendType");
                if (bendTypeObj instanceof String) {
                    setBendType((String) bendTypeObj);
                }
            }
            
            if (stateMap.containsKey("bendStrength")) {
                Object bendStrengthObj = stateMap.get("bendStrength");
                if (bendStrengthObj instanceof Number) {
                    setBendStrength(((Number) bendStrengthObj).floatValue());
                }
            }
            
            if (stateMap.containsKey("bendDirection")) {
                Object bendDirectionObj = stateMap.get("bendDirection");
                if (bendDirectionObj instanceof Number) {
                    setBendDirection(((Number) bendDirectionObj).floatValue());
                }
            }
            
            if (stateMap.containsKey("heightFactor")) {
                Object heightFactorObj = stateMap.get("heightFactor");
                if (heightFactorObj instanceof Number) {
                    setHeightFactor(((Number) heightFactorObj).floatValue());
                }
            }
            
            if (stateMap.containsKey("affectBranches")) {
                Object affectBranchesObj = stateMap.get("affectBranches");
                if (affectBranchesObj instanceof Boolean) {
                    setAffectBranches((Boolean) affectBranchesObj);
                }
            }
            
            if (stateMap.containsKey("affectLeaves")) {
                Object affectLeavesObj = stateMap.get("affectLeaves");
                if (affectLeavesObj instanceof Boolean) {
                    setAffectLeaves((Boolean) affectLeavesObj);
                }
            }
        }
    }
} 