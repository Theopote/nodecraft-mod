package com.nodecraft.nodesystem.nodes.flora.materials;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.PlantStructure;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Apply Plant NBT 节点: 为植物方块应用NBT数据
 */
@NodeInfo(
    id = "flora.materials.apply_plant_nbt",
    displayName = "Apply Plant NBT",
    description = "Applies NBT data to plant blocks",
    category = "flora.materials"
)
public class ApplyPlantNBTNode extends BaseNode {
    
    // --- 节点属性 ---
    private String customName = "";                   // 自定义名称
    private int age = 0;                             // 年龄
    private String growthStage = "mature";           // 生长阶段
    private boolean persistent = true;               // 是否持久化
    private String customNBT = "";                   // 自定义NBT字符串
    private boolean applyToTrunk = true;             // 是否应用到主干
    private boolean applyToBranches = true;          // 是否应用到分支
    private boolean applyToLeaves = false;           // 是否应用到叶子
    private boolean applyToFlowers = false;          // 是否应用到花朵
    private boolean applyToRoots = false;            // 是否应用到根系
    private String description = "为植物方块应用NBT标签数据";
    
    // --- 输入端口 IDs ---
    private static final String INPUT_PLANT_STRUCTURE_ID = "input_plant_structure";
    private static final String INPUT_CUSTOM_NAME_ID = "input_custom_name";
    private static final String INPUT_AGE_ID = "input_age";
    private static final String INPUT_GROWTH_STAGE_ID = "input_growth_stage";
    private static final String INPUT_PERSISTENT_ID = "input_persistent";
    private static final String INPUT_CUSTOM_NBT_ID = "input_custom_nbt";
    private static final String INPUT_APPLY_TO_TRUNK_ID = "input_apply_to_trunk";
    private static final String INPUT_APPLY_TO_BRANCHES_ID = "input_apply_to_branches";
    private static final String INPUT_APPLY_TO_LEAVES_ID = "input_apply_to_leaves";
    private static final String INPUT_APPLY_TO_FLOWERS_ID = "input_apply_to_flowers";
    private static final String INPUT_APPLY_TO_ROOTS_ID = "input_apply_to_roots";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_PLANT_STRUCTURE_ID = "output_plant_structure";
    private static final String OUTPUT_NBT_INFO_ID = "output_nbt_info";
    
    /**
     * 构造一个新的应用植物NBT节点
     */
    public ApplyPlantNBTNode() {
        super(UUID.randomUUID(), "flora.materials.apply_plant_nbt");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_PLANT_STRUCTURE_ID, "Plant Structure", 
                "要应用NBT的植物结构", NodeDataType.PLANT_STRUCTURE, this));
        addInputPort(new BasePort(INPUT_CUSTOM_NAME_ID, "Custom Name", 
                "自定义名称", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_AGE_ID, "Age", 
                "植物年龄", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_GROWTH_STAGE_ID, "Growth Stage", 
                "生长阶段（sapling、young、mature、old）", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_PERSISTENT_ID, "Persistent", 
                "是否持久化", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_CUSTOM_NBT_ID, "Custom NBT", 
                "自定义NBT字符串（JSON格式）", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_APPLY_TO_TRUNK_ID, "Apply to Trunk", 
                "是否应用到主干", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_APPLY_TO_BRANCHES_ID, "Apply to Branches", 
                "是否应用到分支", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_APPLY_TO_LEAVES_ID, "Apply to Leaves", 
                "是否应用到叶子", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_APPLY_TO_FLOWERS_ID, "Apply to Flowers", 
                "是否应用到花朵", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_APPLY_TO_ROOTS_ID, "Apply to Roots", 
                "是否应用到根系", NodeDataType.BOOLEAN, this));
        
        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_PLANT_STRUCTURE_ID, "Plant Structure", 
                "应用NBT后的植物结构", NodeDataType.PLANT_STRUCTURE, this));
        addOutputPort(new BasePort(OUTPUT_NBT_INFO_ID, "NBT Info", 
                "NBT应用操作的详细信息", NodeDataType.STRING, this));
    }
    
    @Override
    public String getDescription() {
        return this.description;
    }
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        PlantStructure inputPlant = getInputValue(INPUT_PLANT_STRUCTURE_ID, null);
        String customNameValue = getInputValue(INPUT_CUSTOM_NAME_ID, this.customName);
        Integer ageValue = getInputValue(INPUT_AGE_ID, this.age);
        String growthStageValue = getInputValue(INPUT_GROWTH_STAGE_ID, this.growthStage);
        Boolean persistentValue = getInputValue(INPUT_PERSISTENT_ID, this.persistent);
        String customNBTValue = getInputValue(INPUT_CUSTOM_NBT_ID, this.customNBT);
        Boolean applyToTrunkValue = getInputValue(INPUT_APPLY_TO_TRUNK_ID, this.applyToTrunk);
        Boolean applyToBranchesValue = getInputValue(INPUT_APPLY_TO_BRANCHES_ID, this.applyToBranches);
        Boolean applyToLeavesValue = getInputValue(INPUT_APPLY_TO_LEAVES_ID, this.applyToLeaves);
        Boolean applyToFlowersValue = getInputValue(INPUT_APPLY_TO_FLOWERS_ID, this.applyToFlowers);
        Boolean applyToRootsValue = getInputValue(INPUT_APPLY_TO_ROOTS_ID, this.applyToRoots);
        
        // 默认输出值
        PlantStructure nbtPlant = new PlantStructure();
        String nbtInfo = "No plant to apply NBT";
        
        if (inputPlant != null && !inputPlant.isEmpty()) {
            try {
                // 验证NBT参数
                customNameValue = customNameValue != null ? customNameValue.trim() : "";
                ageValue = Math.max(0, ageValue != null ? ageValue : 0);
                growthStageValue = validateGrowthStage(growthStageValue);
                persistentValue = persistentValue != null ? persistentValue : true;
                customNBTValue = customNBTValue != null ? customNBTValue.trim() : "";
                applyToTrunkValue = applyToTrunkValue != null ? applyToTrunkValue : true;
                applyToBranchesValue = applyToBranchesValue != null ? applyToBranchesValue : true;
                applyToLeavesValue = applyToLeavesValue != null ? applyToLeavesValue : false;
                applyToFlowersValue = applyToFlowersValue != null ? applyToFlowersValue : false;
                applyToRootsValue = applyToRootsValue != null ? applyToRootsValue : false;
                
                // 创建NBT数据
                Map<String, Object> nbtData = createNBTData(customNameValue, ageValue, growthStageValue, 
                                                          persistentValue, customNBTValue);
                
                // 应用NBT到植物结构
                nbtPlant = applyNBTToPlant(inputPlant, nbtData, applyToTrunkValue, applyToBranchesValue,
                                         applyToLeavesValue, applyToFlowersValue, applyToRootsValue);
                
                // 复制原始元数据并添加NBT信息
                if (inputPlant.getMetadata() != null) {
                    for (Map.Entry<String, Object> entry : inputPlant.getMetadata().entrySet()) {
                        nbtPlant.setMetadata(entry.getKey(), entry.getValue());
                    }
                }
                nbtPlant.setMetadata("custom_name", customNameValue);
                nbtPlant.setMetadata("age", ageValue);
                nbtPlant.setMetadata("growth_stage", growthStageValue);
                nbtPlant.setMetadata("persistent", persistentValue);
                nbtPlant.setMetadata("nbt_applied", true);
                
                // 生成NBT信息
                int blocksAffected = countAffectedBlocks(nbtPlant, applyToTrunkValue, applyToBranchesValue,
                                                       applyToLeavesValue, applyToFlowersValue, applyToRootsValue);
                nbtInfo = String.format("Applied NBT: Name='%s', Age=%d, Stage=%s, Persistent=%s, Blocks Affected=%d",
                    customNameValue.isEmpty() ? "None" : customNameValue, ageValue, growthStageValue, 
                    persistentValue, blocksAffected);
                
            } catch (Exception e) {
                System.err.println("Error in Apply Plant NBT: " + e.getMessage());
                e.printStackTrace();
                nbtPlant = inputPlant; // 返回原始植物
                nbtInfo = "Error during NBT application";
            }
        }
        
        // 设置输出
        outputValues.put(OUTPUT_PLANT_STRUCTURE_ID, nbtPlant);
        outputValues.put(OUTPUT_NBT_INFO_ID, nbtInfo);
    }
    
    /**
     * 创建NBT数据
     */
    private Map<String, Object> createNBTData(String customName, int age, String growthStage, 
                                            boolean persistent, String customNBT) {
        Map<String, Object> nbtData = new HashMap<>();
        
        // 基础NBT标签
        if (!customName.isEmpty()) {
            nbtData.put("CustomName", "\"" + customName + "\"");
        }
        
        nbtData.put("Age", age);
        nbtData.put("GrowthStage", growthStage);
        nbtData.put("Persistent", persistent);
        
        // 植物特定的NBT
        nbtData.put("PlantGenerated", true);
        nbtData.put("GenerationTime", System.currentTimeMillis());
        
        // 根据生长阶段添加额外属性
        switch (growthStage) {
            case "sapling":
                nbtData.put("CanGrow", true);
                nbtData.put("GrowthRate", 0.1f);
                break;
            case "young":
                nbtData.put("CanGrow", true);
                nbtData.put("GrowthRate", 0.05f);
                break;
            case "mature":
                nbtData.put("CanGrow", false);
                nbtData.put("CanReproduce", true);
                break;
            case "old":
                nbtData.put("CanGrow", false);
                nbtData.put("CanReproduce", false);
                nbtData.put("DecayRate", 0.01f);
                break;
        }
        
        // 解析自定义NBT
        if (!customNBT.isEmpty()) {
            try {
                Map<String, Object> customData = parseCustomNBT(customNBT);
                nbtData.putAll(customData);
            } catch (Exception e) {
                System.err.println("Warning: Failed to parse custom NBT: " + e.getMessage());
            }
        }
        
        return nbtData;
    }
    
    /**
     * 解析自定义NBT字符串（简化的JSON解析）
     */
    private Map<String, Object> parseCustomNBT(String customNBT) {
        Map<String, Object> result = new HashMap<>();
        
        // 简化的JSON解析（仅支持基本键值对）
        if (customNBT.startsWith("{") && customNBT.endsWith("}")) {
            String content = customNBT.substring(1, customNBT.length() - 1);
            String[] pairs = content.split(",");
            
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().replace("\"", "");
                    String value = keyValue[1].trim();
                    
                    // 尝试解析值类型
                    Object parsedValue;
                    if (value.equals("true") || value.equals("false")) {
                        parsedValue = Boolean.parseBoolean(value);
                    } else if (value.replace("\"", "").matches("-?\\d+")) {
                        parsedValue = Integer.parseInt(value.replace("\"", ""));
                    } else if (value.replace("\"", "").matches("-?\\d*\\.\\d+")) {
                        parsedValue = Float.parseFloat(value.replace("\"", ""));
                    } else {
                        parsedValue = value.replace("\"", "");
                    }
                    
                    result.put(key, parsedValue);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 将NBT应用到植物结构
     */
    private PlantStructure applyNBTToPlant(PlantStructure original, Map<String, Object> nbtData,
                                         boolean applyToTrunk, boolean applyToBranches,
                                         boolean applyToLeaves, boolean applyToFlowers, boolean applyToRoots) {
        PlantStructure nbtPlant = new PlantStructure();
        
        // 应用到主干
        java.util.List<PlantStructure.PlantBlock> nbtTrunkBlocks = new java.util.ArrayList<>();
        for (PlantStructure.PlantBlock block : original.getTrunkBlocks()) {
            PlantStructure.PlantBlock newBlock = new PlantStructure.PlantBlock(
                block.getPosition(), block.getBlockType(), block.getThickness());
            if (applyToTrunk) {
                newBlock.setNbtData(new HashMap<>(nbtData));
            }
            nbtTrunkBlocks.add(newBlock);
        }
        nbtPlant.addTrunkBlocks(nbtTrunkBlocks);
        
        // 应用到分支
        java.util.List<PlantStructure.PlantBlock> nbtBranchBlocks = new java.util.ArrayList<>();
        for (PlantStructure.PlantBlock block : original.getBranchBlocks()) {
            PlantStructure.PlantBlock newBlock = new PlantStructure.PlantBlock(
                block.getPosition(), block.getBlockType(), block.getThickness());
            if (applyToBranches) {
                newBlock.setNbtData(new HashMap<>(nbtData));
            }
            nbtBranchBlocks.add(newBlock);
        }
        nbtPlant.addBranchBlocks(nbtBranchBlocks);
        
        // 应用到叶子
        java.util.List<PlantStructure.PlantBlock> nbtLeafBlocks = new java.util.ArrayList<>();
        for (PlantStructure.PlantBlock block : original.getLeafBlocks()) {
            PlantStructure.PlantBlock newBlock = new PlantStructure.PlantBlock(
                block.getPosition(), block.getBlockType());
            if (applyToLeaves) {
                newBlock.setNbtData(new HashMap<>(nbtData));
            }
            nbtLeafBlocks.add(newBlock);
        }
        nbtPlant.addLeafBlocks(nbtLeafBlocks);
        
        // 应用到花朵
        java.util.List<PlantStructure.PlantBlock> nbtFlowerBlocks = new java.util.ArrayList<>();
        for (PlantStructure.PlantBlock block : original.getFlowerBlocks()) {
            PlantStructure.PlantBlock newBlock = new PlantStructure.PlantBlock(
                block.getPosition(), block.getBlockType());
            if (applyToFlowers) {
                newBlock.setNbtData(new HashMap<>(nbtData));
            }
            nbtFlowerBlocks.add(newBlock);
        }
        nbtPlant.addFlowerBlocks(nbtFlowerBlocks);
        
        // 应用到根系
        java.util.List<PlantStructure.PlantBlock> nbtRootBlocks = new java.util.ArrayList<>();
        for (PlantStructure.PlantBlock block : original.getRootBlocks()) {
            PlantStructure.PlantBlock newBlock = new PlantStructure.PlantBlock(
                block.getPosition(), block.getBlockType());
            if (applyToRoots) {
                newBlock.setNbtData(new HashMap<>(nbtData));
            }
            nbtRootBlocks.add(newBlock);
        }
        nbtPlant.addRootBlocks(nbtRootBlocks);
        
        return nbtPlant;
    }
    
    /**
     * 计算受影响的方块数量
     */
    private int countAffectedBlocks(PlantStructure plant, boolean applyToTrunk, boolean applyToBranches,
                                  boolean applyToLeaves, boolean applyToFlowers, boolean applyToRoots) {
        int count = 0;
        
        if (applyToTrunk) count += plant.getTrunkBlocks().size();
        if (applyToBranches) count += plant.getBranchBlocks().size();
        if (applyToLeaves) count += plant.getLeafBlocks().size();
        if (applyToFlowers) count += plant.getFlowerBlocks().size();
        if (applyToRoots) count += plant.getRootBlocks().size();
        
        return count;
    }
    
    /**
     * 验证生长阶段
     */
    private String validateGrowthStage(String growthStage) {
        if (growthStage == null || growthStage.trim().isEmpty()) {
            return "mature";
        }
        
        String trimmed = growthStage.trim().toLowerCase();
        switch (trimmed) {
            case "sapling":
            case "young":
            case "mature":
            case "old":
                return trimmed;
            default:
                return "mature";
        }
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
    
    public String getCustomName() {
        return customName;
    }
    
    public void setCustomName(String customName) {
        this.customName = customName != null ? customName.trim() : "";
        markDirty();
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = Math.max(0, age);
        markDirty();
    }
    
    public String getGrowthStage() {
        return growthStage;
    }
    
    public void setGrowthStage(String growthStage) {
        this.growthStage = validateGrowthStage(growthStage);
        markDirty();
    }
    
    public boolean isPersistent() {
        return persistent;
    }
    
    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
        markDirty();
    }
    
    public String getCustomNBT() {
        return customNBT;
    }
    
    public void setCustomNBT(String customNBT) {
        this.customNBT = customNBT != null ? customNBT.trim() : "";
        markDirty();
    }
    
    public boolean isApplyToTrunk() {
        return applyToTrunk;
    }
    
    public void setApplyToTrunk(boolean applyToTrunk) {
        this.applyToTrunk = applyToTrunk;
        markDirty();
    }
    
    public boolean isApplyToBranches() {
        return applyToBranches;
    }
    
    public void setApplyToBranches(boolean applyToBranches) {
        this.applyToBranches = applyToBranches;
        markDirty();
    }
    
    public boolean isApplyToLeaves() {
        return applyToLeaves;
    }
    
    public void setApplyToLeaves(boolean applyToLeaves) {
        this.applyToLeaves = applyToLeaves;
        markDirty();
    }
    
    public boolean isApplyToFlowers() {
        return applyToFlowers;
    }
    
    public void setApplyToFlowers(boolean applyToFlowers) {
        this.applyToFlowers = applyToFlowers;
        markDirty();
    }
    
    public boolean isApplyToRoots() {
        return applyToRoots;
    }
    
    public void setApplyToRoots(boolean applyToRoots) {
        this.applyToRoots = applyToRoots;
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        Map<String, Object> state = new HashMap<>();
        state.put("customName", getCustomName());
        state.put("age", getAge());
        state.put("growthStage", getGrowthStage());
        state.put("persistent", isPersistent());
        state.put("customNBT", getCustomNBT());
        state.put("applyToTrunk", isApplyToTrunk());
        state.put("applyToBranches", isApplyToBranches());
        state.put("applyToLeaves", isApplyToLeaves());
        state.put("applyToFlowers", isApplyToFlowers());
        state.put("applyToRoots", isApplyToRoots());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof Map) {
            Map<?, ?> stateMap = (Map<?, ?>) state;
            
            if (stateMap.containsKey("customName")) {
                Object customNameObj = stateMap.get("customName");
                if (customNameObj instanceof String) {
                    setCustomName((String) customNameObj);
                }
            }
            
            if (stateMap.containsKey("age")) {
                Object ageObj = stateMap.get("age");
                if (ageObj instanceof Number) {
                    setAge(((Number) ageObj).intValue());
                }
            }
            
            if (stateMap.containsKey("growthStage")) {
                Object growthStageObj = stateMap.get("growthStage");
                if (growthStageObj instanceof String) {
                    setGrowthStage((String) growthStageObj);
                }
            }
            
            if (stateMap.containsKey("persistent")) {
                Object persistentObj = stateMap.get("persistent");
                if (persistentObj instanceof Boolean) {
                    setPersistent((Boolean) persistentObj);
                }
            }
            
            if (stateMap.containsKey("customNBT")) {
                Object customNBTObj = stateMap.get("customNBT");
                if (customNBTObj instanceof String) {
                    setCustomNBT((String) customNBTObj);
                }
            }
            
            if (stateMap.containsKey("applyToTrunk")) {
                Object applyToTrunkObj = stateMap.get("applyToTrunk");
                if (applyToTrunkObj instanceof Boolean) {
                    setApplyToTrunk((Boolean) applyToTrunkObj);
                }
            }
            
            if (stateMap.containsKey("applyToBranches")) {
                Object applyToBranchesObj = stateMap.get("applyToBranches");
                if (applyToBranchesObj instanceof Boolean) {
                    setApplyToBranches((Boolean) applyToBranchesObj);
                }
            }
            
            if (stateMap.containsKey("applyToLeaves")) {
                Object applyToLeavesObj = stateMap.get("applyToLeaves");
                if (applyToLeavesObj instanceof Boolean) {
                    setApplyToLeaves((Boolean) applyToLeavesObj);
                }
            }
            
            if (stateMap.containsKey("applyToFlowers")) {
                Object applyToFlowersObj = stateMap.get("applyToFlowers");
                if (applyToFlowersObj instanceof Boolean) {
                    setApplyToFlowers((Boolean) applyToFlowersObj);
                }
            }
            
            if (stateMap.containsKey("applyToRoots")) {
                Object applyToRootsObj = stateMap.get("applyToRoots");
                if (applyToRootsObj instanceof Boolean) {
                    setApplyToRoots((Boolean) applyToRootsObj);
                }
            }
        }
    }
} 