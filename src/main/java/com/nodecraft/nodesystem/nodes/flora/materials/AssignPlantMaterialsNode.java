package com.nodecraft.nodesystem.nodes.flora.materials;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.PlantStructure;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Assign Plant Materials 节点: 为植物的不同部分分配材质
 */
@NodeInfo(
    id = "flora.materials.assign_plant_materials",
    displayName = "Assign Plant Materials",
    description = "Assigns materials to plant structures",
    category = "flora.materials"
)
public class AssignPlantMaterialsNode extends BaseNode {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssignPlantMaterialsNode.class);
    
    // --- 节点属性 ---
    private String trunkMaterial = "minecraft:oak_log";
    private String branchMaterial = "minecraft:oak_log";
    private String leafMaterial = "minecraft:oak_leaves";
    private String flowerMaterial = "minecraft:poppy";
    private String rootMaterial = "minecraft:rooted_dirt";
    private String description = "为植物的不同部分分配材质";
    
    // --- 输入端口 IDs ---
    private static final String INPUT_PLANT_STRUCTURE_ID = "input_plant_structure";
    private static final String INPUT_TRUNK_MATERIAL_ID = "input_trunk_material";
    private static final String INPUT_BRANCH_MATERIAL_ID = "input_branch_material";
    private static final String INPUT_LEAF_MATERIAL_ID = "input_leaf_material";
    private static final String INPUT_FLOWER_MATERIAL_ID = "input_flower_material";
    private static final String INPUT_ROOT_MATERIAL_ID = "input_root_material";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_PLANT_STRUCTURE_ID = "output_plant_structure";
    private static final String OUTPUT_MATERIAL_INFO_ID = "output_material_info";
    
    /**
     * 构造一个新的分配植物材质节点
     */
    public AssignPlantMaterialsNode() {
        super(UUID.randomUUID(), "flora.materials.assign_plant_materials");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_PLANT_STRUCTURE_ID, "Plant Structure", 
                "要分配材质的植物结构", NodeDataType.PLANT_STRUCTURE, this));
        addInputPort(new BasePort(INPUT_TRUNK_MATERIAL_ID, "Trunk Material", 
                "树干材质（Minecraft方块ID）", NodeDataType.BLOCK_TYPE, this));
        addInputPort(new BasePort(INPUT_BRANCH_MATERIAL_ID, "Branch Material", 
                "树枝材质（Minecraft方块ID）", NodeDataType.BLOCK_TYPE, this));
        addInputPort(new BasePort(INPUT_LEAF_MATERIAL_ID, "Leaf Material", 
                "叶子材质（Minecraft方块ID）", NodeDataType.BLOCK_TYPE, this));
        addInputPort(new BasePort(INPUT_FLOWER_MATERIAL_ID, "Flower Material", 
                "花朵材质（Minecraft方块ID）", NodeDataType.BLOCK_TYPE, this));
        addInputPort(new BasePort(INPUT_ROOT_MATERIAL_ID, "Root Material", 
                "根系材质（Minecraft方块ID）", NodeDataType.BLOCK_TYPE, this));
        
        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_PLANT_STRUCTURE_ID, "Plant Structure", 
                "分配材质后的植物结构", NodeDataType.PLANT_STRUCTURE, this));
        addOutputPort(new BasePort(OUTPUT_MATERIAL_INFO_ID, "Material Info", 
                "材质分配的详细信息", NodeDataType.STRING, this));
    }
    
    @Override
    public String getDescription() {
        return this.description;
    }
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        PlantStructure inputPlant = getInputValue(INPUT_PLANT_STRUCTURE_ID, null);
        String trunkMaterialValue = getInputValue(INPUT_TRUNK_MATERIAL_ID, this.trunkMaterial);
        String branchMaterialValue = getInputValue(INPUT_BRANCH_MATERIAL_ID, this.branchMaterial);
        String leafMaterialValue = getInputValue(INPUT_LEAF_MATERIAL_ID, this.leafMaterial);
        String flowerMaterialValue = getInputValue(INPUT_FLOWER_MATERIAL_ID, this.flowerMaterial);
        String rootMaterialValue = getInputValue(INPUT_ROOT_MATERIAL_ID, this.rootMaterial);
        
        // 默认输出值
        PlantStructure materializedPlant = new PlantStructure();
        String materialInfo = "No plant to assign materials";
        
        if (inputPlant != null && !inputPlant.isEmpty()) {
            try {
                // 验证材质输入
                trunkMaterialValue = validateMaterial(trunkMaterialValue, "minecraft:oak_log");
                branchMaterialValue = validateMaterial(branchMaterialValue, "minecraft:oak_log");
                leafMaterialValue = validateMaterial(leafMaterialValue, "minecraft:oak_leaves");
                flowerMaterialValue = validateMaterial(flowerMaterialValue, "minecraft:poppy");
                rootMaterialValue = validateMaterial(rootMaterialValue, "minecraft:rooted_dirt");
                
                // 创建带有新材质的植物结构
                materializedPlant = assignMaterials(inputPlant, trunkMaterialValue, branchMaterialValue,
                                                  leafMaterialValue, flowerMaterialValue, rootMaterialValue);
                
                // 复制原始元数据并添加材质信息
                if (inputPlant.getMetadata() != null) {
                    for (java.util.Map.Entry<String, Object> entry : inputPlant.getMetadata().entrySet()) {
                        materializedPlant.setMetadata(entry.getKey(), entry.getValue());
                    }
                }
                materializedPlant.setMetadata("trunk_material", trunkMaterialValue);
                materializedPlant.setMetadata("branch_material", branchMaterialValue);
                materializedPlant.setMetadata("leaf_material", leafMaterialValue);
                materializedPlant.setMetadata("flower_material", flowerMaterialValue);
                materializedPlant.setMetadata("root_material", rootMaterialValue);
                
                // 生成材质信息
                materialInfo = String.format("Materials: Trunk=%s, Branch=%s, Leaf=%s, Flower=%s, Root=%s, Total Blocks=%d",
                    trunkMaterialValue, branchMaterialValue, leafMaterialValue, 
                    flowerMaterialValue, rootMaterialValue, materializedPlant.getTotalBlockCount());
                
            } catch (Exception e) {
                LOGGER.error("Error in Assign Plant Materials", e);
                materializedPlant = inputPlant; // 返回原始植物
                materialInfo = "Error during material assignment";
            }
        }
        
        // 设置输出
        outputValues.put(OUTPUT_PLANT_STRUCTURE_ID, materializedPlant);
        outputValues.put(OUTPUT_MATERIAL_INFO_ID, materialInfo);
    }
    
    /**
     * 为植物分配材质
     */
    private PlantStructure assignMaterials(PlantStructure original, String trunkMaterial, String branchMaterial,
                                         String leafMaterial, String flowerMaterial, String rootMaterial) {
        PlantStructure materialized = new PlantStructure();
        
        // 分配树干材质
        for (PlantStructure.PlantBlock block : original.getTrunkBlocks()) {
            materialized.addTrunkBlock(block.getPosition(), trunkMaterial, block.getThickness());
        }
        
        // 分配树枝材质
        for (PlantStructure.PlantBlock block : original.getBranchBlocks()) {
            materialized.addBranchBlock(block.getPosition(), branchMaterial, block.getThickness());
        }
        
        // 分配叶子材质
        for (PlantStructure.PlantBlock block : original.getLeafBlocks()) {
            materialized.addLeafBlock(block.getPosition(), leafMaterial);
        }
        
        // 分配花朵材质
        for (PlantStructure.PlantBlock block : original.getFlowerBlocks()) {
            materialized.addFlowerBlock(block.getPosition(), flowerMaterial);
        }
        
        // 分配根系材质
        for (PlantStructure.PlantBlock block : original.getRootBlocks()) {
            materialized.addRootBlock(block.getPosition(), rootMaterial);
        }
        
        return materialized;
    }
    
    /**
     * 验证材质名称
     */
    private String validateMaterial(String material, String defaultMaterial) {
        if (material == null || material.trim().isEmpty()) {
            return defaultMaterial;
        }
        
        String trimmed = material.trim();
        
        // 如果没有命名空间前缀，添加minecraft:
        if (!trimmed.contains(":")) {
            return "minecraft:" + trimmed;
        }
        
        return trimmed;
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
    
    public String getTrunkMaterial() {
        return trunkMaterial;
    }
    
    public void setTrunkMaterial(String trunkMaterial) {
        this.trunkMaterial = validateMaterial(trunkMaterial, "minecraft:oak_log");
        markDirty();
    }
    
    public String getBranchMaterial() {
        return branchMaterial;
    }
    
    public void setBranchMaterial(String branchMaterial) {
        this.branchMaterial = validateMaterial(branchMaterial, "minecraft:oak_log");
        markDirty();
    }
    
    public String getLeafMaterial() {
        return leafMaterial;
    }
    
    public void setLeafMaterial(String leafMaterial) {
        this.leafMaterial = validateMaterial(leafMaterial, "minecraft:oak_leaves");
        markDirty();
    }
    
    public String getFlowerMaterial() {
        return flowerMaterial;
    }
    
    public void setFlowerMaterial(String flowerMaterial) {
        this.flowerMaterial = validateMaterial(flowerMaterial, "minecraft:poppy");
        markDirty();
    }
    
    public String getRootMaterial() {
        return rootMaterial;
    }
    
    public void setRootMaterial(String rootMaterial) {
        this.rootMaterial = validateMaterial(rootMaterial, "minecraft:rooted_dirt");
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("trunkMaterial", getTrunkMaterial());
        state.put("branchMaterial", getBranchMaterial());
        state.put("leafMaterial", getLeafMaterial());
        state.put("flowerMaterial", getFlowerMaterial());
        state.put("rootMaterial", getRootMaterial());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("trunkMaterial")) {
                Object trunkMaterialObj = stateMap.get("trunkMaterial");
                if (trunkMaterialObj instanceof String) {
                    setTrunkMaterial((String) trunkMaterialObj);
                }
            }
            
            if (stateMap.containsKey("branchMaterial")) {
                Object branchMaterialObj = stateMap.get("branchMaterial");
                if (branchMaterialObj instanceof String) {
                    setBranchMaterial((String) branchMaterialObj);
                }
            }
            
            if (stateMap.containsKey("leafMaterial")) {
                Object leafMaterialObj = stateMap.get("leafMaterial");
                if (leafMaterialObj instanceof String) {
                    setLeafMaterial((String) leafMaterialObj);
                }
            }
            
            if (stateMap.containsKey("flowerMaterial")) {
                Object flowerMaterialObj = stateMap.get("flowerMaterial");
                if (flowerMaterialObj instanceof String) {
                    setFlowerMaterial((String) flowerMaterialObj);
                }
            }
            
            if (stateMap.containsKey("rootMaterial")) {
                Object rootMaterialObj = stateMap.get("rootMaterial");
                if (rootMaterialObj instanceof String) {
                    setRootMaterial((String) rootMaterialObj);
                }
            }
        }
    }
} 