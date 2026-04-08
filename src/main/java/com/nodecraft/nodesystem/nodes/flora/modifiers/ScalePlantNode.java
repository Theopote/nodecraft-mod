package com.nodecraft.nodesystem.nodes.flora.modifiers;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.PlantStructure;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Scale Plant 节点: 按比例缩放植物结构
 */
@NodeInfo(
    id = "flora.modifiers.scale_plant",
    displayName = "Scale Plant",
    description = "Scales plant structures",
    category = "flora.modifiers"
)
public class ScalePlantNode extends BaseNode {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScalePlantNode.class);
    
    // --- 节点属性 ---
    private float scaleX = 1.0f;          // X轴缩放比例
    private float scaleY = 1.0f;          // Y轴缩放比例
    private float scaleZ = 1.0f;          // Z轴缩放比例
    private boolean uniformScale = true;   // 是否均匀缩放
    private String description = "按比例缩放植物结构";
    
    // --- 输入端口 IDs ---
    private static final String INPUT_PLANT_STRUCTURE_ID = "input_plant_structure";
    private static final String INPUT_SCALE_X_ID = "input_scale_x";
    private static final String INPUT_SCALE_Y_ID = "input_scale_y";
    private static final String INPUT_SCALE_Z_ID = "input_scale_z";
    private static final String INPUT_UNIFORM_SCALE_ID = "input_uniform_scale";
    private static final String INPUT_CENTER_POINT_ID = "input_center_point";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_PLANT_STRUCTURE_ID = "output_plant_structure";
    private static final String OUTPUT_SCALE_INFO_ID = "output_scale_info";
    
    /**
     * 构造一个新的缩放植物节点
     */
    public ScalePlantNode() {
        super(UUID.randomUUID(), "flora.modifiers.scale_plant");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_PLANT_STRUCTURE_ID, "Plant Structure", 
                "要缩放的植物结构", NodeDataType.PLANT_STRUCTURE, this));
        addInputPort(new BasePort(INPUT_SCALE_X_ID, "Scale X", 
                "X轴缩放比例", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_SCALE_Y_ID, "Scale Y", 
                "Y轴缩放比例", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_SCALE_Z_ID, "Scale Z", 
                "Z轴缩放比例", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_UNIFORM_SCALE_ID, "Uniform Scale", 
                "如果为true，则只使用Scale X作为所有轴的比例", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_CENTER_POINT_ID, "Center Point", 
                "缩放中心点（默认使用植物中心）", NodeDataType.COORDINATE, this));
        
        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_PLANT_STRUCTURE_ID, "Scaled Plant Structure", 
                "缩放后的植物结构", NodeDataType.PLANT_STRUCTURE, this));
        addOutputPort(new BasePort(OUTPUT_SCALE_INFO_ID, "Scale Info", 
                "缩放操作的详细信息", NodeDataType.STRING, this));
    }
    
    @Override
    public String getDescription() {
        return this.description;
    }
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        PlantStructure inputPlant = getInputValue(INPUT_PLANT_STRUCTURE_ID, null);
        Float scaleXValue = getInputValue(INPUT_SCALE_X_ID, this.scaleX);
        Float scaleYValue = getInputValue(INPUT_SCALE_Y_ID, this.scaleY);
        Float scaleZValue = getInputValue(INPUT_SCALE_Z_ID, this.scaleZ);
        Boolean uniformScaleValue = getInputValue(INPUT_UNIFORM_SCALE_ID, this.uniformScale);
        BlockPos centerPointValue = getInputValue(INPUT_CENTER_POINT_ID, null);
        
        // 默认输出值
        PlantStructure scaledPlant = new PlantStructure();
        String scaleInfo = "No plant to scale";
        
        if (inputPlant != null && !inputPlant.isEmpty()) {
            try {
                // 验证缩放参数
                scaleXValue = Math.max(0.1f, Math.min(10.0f, scaleXValue != null ? scaleXValue : 1.0f));
                scaleYValue = Math.max(0.1f, Math.min(10.0f, scaleYValue != null ? scaleYValue : 1.0f));
                scaleZValue = Math.max(0.1f, Math.min(10.0f, scaleZValue != null ? scaleZValue : 1.0f));
                uniformScaleValue = uniformScaleValue != null ? uniformScaleValue : true;
                
                // 如果是均匀缩放，使用X轴比例
                if (uniformScaleValue) {
                    scaleYValue = scaleXValue;
                    scaleZValue = scaleXValue;
                }
                
                // 计算缩放中心点
                BlockPos centerPoint = centerPointValue != null ? centerPointValue : calculatePlantCenter(inputPlant);
                
                // 创建缩放后的植物结构
                scaledPlant = scaleFullPlant(inputPlant, scaleXValue, scaleYValue, scaleZValue, centerPoint);
                
                // 复制原始元数据并添加缩放信息
                if (inputPlant.getMetadata() != null) {
                    for (java.util.Map.Entry<String, Object> entry : inputPlant.getMetadata().entrySet()) {
                        scaledPlant.setMetadata(entry.getKey(), entry.getValue());
                    }
                }
                scaledPlant.setMetadata("scale_x", scaleXValue);
                scaledPlant.setMetadata("scale_y", scaleYValue);
                scaledPlant.setMetadata("scale_z", scaleZValue);
                scaledPlant.setMetadata("scale_center", centerPoint.toString());
                
                // 生成缩放信息
                scaleInfo = String.format("Scaled: %.2fx%.2fx%.2f, Center: %s, Blocks: %d → %d",
                    scaleXValue, scaleYValue, scaleZValue, centerPoint.toString(),
                    inputPlant.getTotalBlockCount(), scaledPlant.getTotalBlockCount());
                
            } catch (Exception e) {
                LOGGER.error("Error in Scale Plant", e);
                scaledPlant = inputPlant; // 返回原始植物
                scaleInfo = "Error during scaling";
            }
        }
        
        // 设置输出
        outputValues.put(OUTPUT_PLANT_STRUCTURE_ID, scaledPlant);
        outputValues.put(OUTPUT_SCALE_INFO_ID, scaleInfo);
    }
    
    /**
     * 缩放整个植物结构
     */
    private PlantStructure scaleFullPlant(PlantStructure original, float scaleX, float scaleY, float scaleZ, BlockPos center) {
        PlantStructure scaled = new PlantStructure();
        
        // 缩放各个部分
        scaleBlockList(original.getTrunkBlocks(), (pos, type, thickness) -> scaled.addTrunkBlock(pos, type, thickness), scaleX, scaleY, scaleZ, center);
        scaleBlockList(original.getBranchBlocks(), (pos, type, thickness) -> scaled.addBranchBlock(pos, type, thickness), scaleX, scaleY, scaleZ, center);
        scaleBlockList(original.getLeafBlocks(), (pos, type, thickness) -> scaled.addLeafBlock(pos, type), scaleX, scaleY, scaleZ, center);
        scaleBlockList(original.getFlowerBlocks(), (pos, type, thickness) -> scaled.addFlowerBlock(pos, type), scaleX, scaleY, scaleZ, center);
        scaleBlockList(original.getRootBlocks(), (pos, type, thickness) -> scaled.addRootBlock(pos, type), scaleX, scaleY, scaleZ, center);
        
        return scaled;
    }
    
    /**
     * 缩放方块列表
     */
    private void scaleBlockList(java.util.List<PlantStructure.PlantBlock> originalBlocks,
                               BlockAdder adder,
                               float scaleX, float scaleY, float scaleZ,
                               BlockPos center) {
        for (PlantStructure.PlantBlock block : originalBlocks) {
            BlockPos originalPos = block.getPosition();
            
            // 计算相对于中心点的偏移
            int offsetX = originalPos.getX() - center.getX();
            int offsetY = originalPos.getY() - center.getY();
            int offsetZ = originalPos.getZ() - center.getZ();
            
            // 应用缩放
            int scaledOffsetX = Math.round(offsetX * scaleX);
            int scaledOffsetY = Math.round(offsetY * scaleY);
            int scaledOffsetZ = Math.round(offsetZ * scaleZ);
            
            // 计算缩放后的绝对位置
            BlockPos scaledPos = new BlockPos(
                center.getX() + scaledOffsetX,
                center.getY() + scaledOffsetY,
                center.getZ() + scaledOffsetZ
            );
            
            // 缩放厚度
            float scaledThickness = block.getThickness() * Math.min(scaleX, Math.min(scaleY, scaleZ));
            
            // 添加缩放后的方块
            adder.addBlock(scaledPos, block.getBlockType(), scaledThickness);
        }
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
    
    public float getScaleX() {
        return scaleX;
    }
    
    public void setScaleX(float scaleX) {
        this.scaleX = Math.max(0.1f, Math.min(10.0f, scaleX));
        markDirty();
    }
    
    public float getScaleY() {
        return scaleY;
    }
    
    public void setScaleY(float scaleY) {
        this.scaleY = Math.max(0.1f, Math.min(10.0f, scaleY));
        markDirty();
    }
    
    public float getScaleZ() {
        return scaleZ;
    }
    
    public void setScaleZ(float scaleZ) {
        this.scaleZ = Math.max(0.1f, Math.min(10.0f, scaleZ));
        markDirty();
    }
    
    public boolean isUniformScale() {
        return uniformScale;
    }
    
    public void setUniformScale(boolean uniformScale) {
        this.uniformScale = uniformScale;
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("scaleX", getScaleX());
        state.put("scaleY", getScaleY());
        state.put("scaleZ", getScaleZ());
        state.put("uniformScale", isUniformScale());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("scaleX")) {
                Object scaleXObj = stateMap.get("scaleX");
                if (scaleXObj instanceof Number) {
                    setScaleX(((Number) scaleXObj).floatValue());
                }
            }
            
            if (stateMap.containsKey("scaleY")) {
                Object scaleYObj = stateMap.get("scaleY");
                if (scaleYObj instanceof Number) {
                    setScaleY(((Number) scaleYObj).floatValue());
                }
            }
            
            if (stateMap.containsKey("scaleZ")) {
                Object scaleZObj = stateMap.get("scaleZ");
                if (scaleZObj instanceof Number) {
                    setScaleZ(((Number) scaleZObj).floatValue());
                }
            }
            
            if (stateMap.containsKey("uniformScale")) {
                Object uniformScaleObj = stateMap.get("uniformScale");
                if (uniformScaleObj instanceof Boolean) {
                    setUniformScale((Boolean) uniformScaleObj);
                }
            }
        }
    }
} 