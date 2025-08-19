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
 * Rotate Plant 节点: 旋转植物结构
 */
@NodeInfo(
    id = "flora.modifiers.rotate_plant",
    displayName = "Rotate Plant",
    description = "Rotates plant structures",
    category = "flora.modifiers"
)
public class RotatePlantNode extends BaseNode {
    
    // --- 节点属性 ---
    private float rotationX = 0.0f;       // X轴旋转角度（度）
    private float rotationY = 0.0f;       // Y轴旋转角度（度）
    private float rotationZ = 0.0f;       // Z轴旋转角度（度）
    private String description = "旋转植物结构";
    
    // --- 输入端口 IDs ---
    private static final String INPUT_PLANT_STRUCTURE_ID = "input_plant_structure";
    private static final String INPUT_ROTATION_X_ID = "input_rotation_x";
    private static final String INPUT_ROTATION_Y_ID = "input_rotation_y";
    private static final String INPUT_ROTATION_Z_ID = "input_rotation_z";
    private static final String INPUT_CENTER_POINT_ID = "input_center_point";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_PLANT_STRUCTURE_ID = "output_plant_structure";
    private static final String OUTPUT_ROTATION_INFO_ID = "output_rotation_info";
    
    /**
     * 构造一个新的旋转植物节点
     */
    public RotatePlantNode() {
        super(UUID.randomUUID(), "flora.modifiers.rotate_plant");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_PLANT_STRUCTURE_ID, "Plant Structure", 
                "要旋转的植物结构", NodeDataType.PLANT_STRUCTURE, this));
        addInputPort(new BasePort(INPUT_ROTATION_X_ID, "Rotation X", 
                "绕X轴旋转角度（度）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_ROTATION_Y_ID, "Rotation Y", 
                "绕Y轴旋转角度（度）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_ROTATION_Z_ID, "Rotation Z", 
                "绕Z轴旋转角度（度）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_CENTER_POINT_ID, "Center Point", 
                "旋转中心点（默认使用植物中心）", NodeDataType.COORDINATE, this));
        
        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_PLANT_STRUCTURE_ID, "Rotated Plant Structure", 
                "旋转后的植物结构", NodeDataType.PLANT_STRUCTURE, this));
        addOutputPort(new BasePort(OUTPUT_ROTATION_INFO_ID, "Rotation Info", 
                "旋转操作的详细信息", NodeDataType.STRING, this));
    }
    
    @Override
    public String getDescription() {
        return this.description;
    }
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        PlantStructure inputPlant = getInputValue(INPUT_PLANT_STRUCTURE_ID, null);
        Float rotationXValue = getInputValue(INPUT_ROTATION_X_ID, this.rotationX);
        Float rotationYValue = getInputValue(INPUT_ROTATION_Y_ID, this.rotationY);
        Float rotationZValue = getInputValue(INPUT_ROTATION_Z_ID, this.rotationZ);
        BlockPos centerPointValue = getInputValue(INPUT_CENTER_POINT_ID, null);
        
        // 默认输出值
        PlantStructure rotatedPlant = new PlantStructure();
        String rotationInfo = "No plant to rotate";
        
        if (inputPlant != null && !inputPlant.isEmpty()) {
            try {
                // 验证旋转参数
                rotationXValue = normalizeAngle(rotationXValue != null ? rotationXValue : 0.0f);
                rotationYValue = normalizeAngle(rotationYValue != null ? rotationYValue : 0.0f);
                rotationZValue = normalizeAngle(rotationZValue != null ? rotationZValue : 0.0f);
                
                // 如果所有旋转角度都是0，直接返回原植物
                if (Math.abs(rotationXValue) < 0.01f && Math.abs(rotationYValue) < 0.01f && Math.abs(rotationZValue) < 0.01f) {
                    rotatedPlant = inputPlant.copy();
                    rotationInfo = "No rotation applied (all angles are 0)";
                } else {
                    // 计算旋转中心点
                    BlockPos centerPoint = centerPointValue != null ? centerPointValue : calculatePlantCenter(inputPlant);
                    
                    // 创建旋转后的植物结构
                    rotatedPlant = rotateFullPlant(inputPlant, rotationXValue, rotationYValue, rotationZValue, centerPoint);
                    
                    // 复制原始元数据并添加旋转信息
                    if (inputPlant.getMetadata() != null) {
                        for (java.util.Map.Entry<String, Object> entry : inputPlant.getMetadata().entrySet()) {
                            rotatedPlant.setMetadata(entry.getKey(), entry.getValue());
                        }
                    }
                    rotatedPlant.setMetadata("rotation_x", rotationXValue);
                    rotatedPlant.setMetadata("rotation_y", rotationYValue);
                    rotatedPlant.setMetadata("rotation_z", rotationZValue);
                    rotatedPlant.setMetadata("rotation_center", centerPoint.toString());
                    
                    // 生成旋转信息
                    rotationInfo = String.format("Rotated: X=%.1f°, Y=%.1f°, Z=%.1f°, Center: %s, Blocks: %d",
                        rotationXValue, rotationYValue, rotationZValue, centerPoint.toString(),
                        rotatedPlant.getTotalBlockCount());
                }
                
            } catch (Exception e) {
                System.err.println("Error in Rotate Plant: " + e.getMessage());
                e.printStackTrace();
                rotatedPlant = inputPlant; // 返回原始植物
                rotationInfo = "Error during rotation";
            }
        }
        
        // 设置输出
        outputValues.put(OUTPUT_PLANT_STRUCTURE_ID, rotatedPlant);
        outputValues.put(OUTPUT_ROTATION_INFO_ID, rotationInfo);
    }
    
    /**
     * 旋转整个植物结构
     */
    private PlantStructure rotateFullPlant(PlantStructure original, float rotX, float rotY, float rotZ, BlockPos center) {
        PlantStructure rotated = new PlantStructure();
        
        // 转换角度到弧度
        double radX = Math.toRadians(rotX);
        double radY = Math.toRadians(rotY);
        double radZ = Math.toRadians(rotZ);
        
        // 旋转各个部分
        rotateBlockList(original.getTrunkBlocks(), (pos, type, thickness) -> rotated.addTrunkBlock(pos, type, thickness), radX, radY, radZ, center);
        rotateBlockList(original.getBranchBlocks(), (pos, type, thickness) -> rotated.addBranchBlock(pos, type, thickness), radX, radY, radZ, center);
        rotateBlockList(original.getLeafBlocks(), (pos, type, thickness) -> rotated.addLeafBlock(pos, type), radX, radY, radZ, center);
        rotateBlockList(original.getFlowerBlocks(), (pos, type, thickness) -> rotated.addFlowerBlock(pos, type), radX, radY, radZ, center);
        rotateBlockList(original.getRootBlocks(), (pos, type, thickness) -> rotated.addRootBlock(pos, type), radX, radY, radZ, center);
        
        return rotated;
    }
    
    /**
     * 旋转方块列表
     */
    private void rotateBlockList(java.util.List<PlantStructure.PlantBlock> originalBlocks,
                               BlockAdder adder,
                               double radX, double radY, double radZ,
                               BlockPos center) {
        for (PlantStructure.PlantBlock block : originalBlocks) {
            BlockPos originalPos = block.getPosition();
            
            // 计算相对于中心点的偏移
            double offsetX = originalPos.getX() - center.getX();
            double offsetY = originalPos.getY() - center.getY();
            double offsetZ = originalPos.getZ() - center.getZ();
            
            // 应用旋转矩阵（先绕X轴，再绕Y轴，最后绕Z轴）
            double[] rotated = rotatePoint(offsetX, offsetY, offsetZ, radX, radY, radZ);
            
            // 计算旋转后的绝对位置
            BlockPos rotatedPos = new BlockPos(
                (int) Math.round(center.getX() + rotated[0]),
                (int) Math.round(center.getY() + rotated[1]),
                (int) Math.round(center.getZ() + rotated[2])
            );
            
            // 添加旋转后的方块
            adder.addBlock(rotatedPos, block.getBlockType(), block.getThickness());
        }
    }
    
    /**
     * 旋转一个点
     */
    private double[] rotatePoint(double x, double y, double z, double radX, double radY, double radZ) {
        // 先绕X轴旋转
        double cosX = Math.cos(radX), sinX = Math.sin(radX);
        double y1 = y * cosX - z * sinX;
        double z1 = y * sinX + z * cosX;
        
        // 再绕Y轴旋转
        double cosY = Math.cos(radY), sinY = Math.sin(radY);
        double x2 = x * cosY + z1 * sinY;
        double z2 = -x * sinY + z1 * cosY;
        
        // 最后绕Z轴旋转
        double cosZ = Math.cos(radZ), sinZ = Math.sin(radZ);
        double x3 = x2 * cosZ - y1 * sinZ;
        double y3 = x2 * sinZ + y1 * cosZ;
        
        return new double[]{x3, y3, z2};
    }
    
    /**
     * 方块添加器接口
     */
    @FunctionalInterface
    private interface BlockAdder {
        void addBlock(BlockPos pos, String blockType, float thickness);
    }
    
    /**
     * 计算植物的中心点
     */
    private BlockPos calculatePlantCenter(PlantStructure plant) {
        java.util.List<PlantStructure.PlantBlock> allBlocks = plant.getAllBlocks();
        if (allBlocks.isEmpty()) {
            return new BlockPos(0, 0, 0);
        }
        
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        
        for (PlantStructure.PlantBlock block : allBlocks) {
            BlockPos pos = block.getPosition();
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        
        return new BlockPos(
            (minX + maxX) / 2,
            (minY + maxY) / 2,
            (minZ + maxZ) / 2
        );
    }
    
    /**
     * 标准化角度到-180到180度范围
     */
    private float normalizeAngle(float angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
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
    
    public float getRotationX() {
        return rotationX;
    }
    
    public void setRotationX(float rotationX) {
        this.rotationX = normalizeAngle(rotationX);
        markDirty();
    }
    
    public float getRotationY() {
        return rotationY;
    }
    
    public void setRotationY(float rotationY) {
        this.rotationY = normalizeAngle(rotationY);
        markDirty();
    }
    
    public float getRotationZ() {
        return rotationZ;
    }
    
    public void setRotationZ(float rotationZ) {
        this.rotationZ = normalizeAngle(rotationZ);
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("rotationX", getRotationX());
        state.put("rotationY", getRotationY());
        state.put("rotationZ", getRotationZ());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("rotationX")) {
                Object rotationXObj = stateMap.get("rotationX");
                if (rotationXObj instanceof Number) {
                    setRotationX(((Number) rotationXObj).floatValue());
                }
            }
            
            if (stateMap.containsKey("rotationY")) {
                Object rotationYObj = stateMap.get("rotationY");
                if (rotationYObj instanceof Number) {
                    setRotationY(((Number) rotationYObj).floatValue());
                }
            }
            
            if (stateMap.containsKey("rotationZ")) {
                Object rotationZObj = stateMap.get("rotationZ");
                if (rotationZObj instanceof Number) {
                    setRotationZ(((Number) rotationZObj).floatValue());
                }
            }
        }
    }
} 