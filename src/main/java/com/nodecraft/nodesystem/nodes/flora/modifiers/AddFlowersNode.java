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

import java.util.Random;
import java.util.UUID;

/**
 * Add Flowers 节点: 为植物添加花朵
 */
@NodeInfo(
    id = "flora.modifiers.add_flowers",
    displayName = "Add Flowers",
    description = "Adds flowers to plants",
    category = "flora.modifiers"
)
public class AddFlowersNode extends BaseNode {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddFlowersNode.class);
    
    /**
     * 花朵分布模式枚举
     */
    public enum FlowerPattern {
        RANDOM("Random", "随机分布"),
        BRANCH_TIPS("Branch Tips", "枝端分布"),
        CLUSTER("Cluster", "簇状分布"),
        SPARSE("Sparse", "稀疏分布"),
        DENSE("Dense", "密集分布");
        
        private final String id;
        private final String displayName;
        
        FlowerPattern(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
        
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        
        public static FlowerPattern fromString(String str) {
            for (FlowerPattern pattern : values()) {
                if (pattern.id.equalsIgnoreCase(str) || pattern.displayName.equals(str)) {
                    return pattern;
                }
            }
            return RANDOM; // 默认返回随机分布
        }
    }
    
    // --- 节点属性 ---
    private FlowerPattern flowerPattern = FlowerPattern.RANDOM; // 花朵分布模式
    private float flowerDensity = 0.3f;                         // 花朵密度（0-1）
    private String flowerMaterial = "minecraft:poppy";          // 花朵材质
    private boolean onlyOnLeaves = true;                        // 是否只在叶子附近开花
    private boolean seasonalBloom = false;                      // 是否季节性开花
    private float bloomChance = 0.8f;                           // 开花概率（0-1）
    private int randomSeed = 12345;                             // 随机种子
    private String description = "为植物添加花朵";
    
    // --- 输入端口 IDs ---
    private static final String INPUT_PLANT_STRUCTURE_ID = "input_plant_structure";
    private static final String INPUT_FLOWER_PATTERN_ID = "input_flower_pattern";
    private static final String INPUT_FLOWER_DENSITY_ID = "input_flower_density";
    private static final String INPUT_FLOWER_MATERIAL_ID = "input_flower_material";
    private static final String INPUT_ONLY_ON_LEAVES_ID = "input_only_on_leaves";
    private static final String INPUT_SEASONAL_BLOOM_ID = "input_seasonal_bloom";
    private static final String INPUT_BLOOM_CHANCE_ID = "input_bloom_chance";
    private static final String INPUT_RANDOM_SEED_ID = "input_random_seed";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_PLANT_STRUCTURE_ID = "output_plant_structure";
    private static final String OUTPUT_FLOWER_INFO_ID = "output_flower_info";
    
    /**
     * 构造一个新的添加花朵节点
     */
    public AddFlowersNode() {
        super(UUID.randomUUID(), "flora.modifiers.add_flowers");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_PLANT_STRUCTURE_ID, "Plant Structure", 
                "要添加花朵的植物结构", NodeDataType.PLANT_STRUCTURE, this));
        addInputPort(new BasePort(INPUT_FLOWER_PATTERN_ID, "Flower Pattern", 
                "花朵分布模式（Random、Branch Tips、Cluster等）", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_FLOWER_DENSITY_ID, "Flower Density", 
                "花朵密度（0-1）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_FLOWER_MATERIAL_ID, "Flower Material", 
                "花朵材质（Minecraft方块ID）", NodeDataType.BLOCK_TYPE, this));
        addInputPort(new BasePort(INPUT_ONLY_ON_LEAVES_ID, "Only On Leaves", 
                "是否只在叶子附近开花", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_SEASONAL_BLOOM_ID, "Seasonal Bloom", 
                "是否季节性开花", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_BLOOM_CHANCE_ID, "Bloom Chance", 
                "开花概率（0-1）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_RANDOM_SEED_ID, "Random Seed", 
                "用于花朵生成的随机种子", NodeDataType.INTEGER, this));
        
        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_PLANT_STRUCTURE_ID, "Plant with Flowers", 
                "添加花朵后的植物结构", NodeDataType.PLANT_STRUCTURE, this));
        addOutputPort(new BasePort(OUTPUT_FLOWER_INFO_ID, "Flower Info", 
                "花朵添加操作的详细信息", NodeDataType.STRING, this));
    }
    
    @Override
    public String getDescription() {
        return this.description;
    }
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        PlantStructure inputPlant = getInputValue(INPUT_PLANT_STRUCTURE_ID, null);
        String flowerPatternStr = getInputValue(INPUT_FLOWER_PATTERN_ID, this.flowerPattern.getId());
        Float flowerDensityValue = getInputValue(INPUT_FLOWER_DENSITY_ID, this.flowerDensity);
        String flowerMaterialValue = getInputValue(INPUT_FLOWER_MATERIAL_ID, this.flowerMaterial);
        Boolean onlyOnLeavesValue = getInputValue(INPUT_ONLY_ON_LEAVES_ID, this.onlyOnLeaves);
        Boolean seasonalBloomValue = getInputValue(INPUT_SEASONAL_BLOOM_ID, this.seasonalBloom);
        Float bloomChanceValue = getInputValue(INPUT_BLOOM_CHANCE_ID, this.bloomChance);
        Integer randomSeedValue = getInputValue(INPUT_RANDOM_SEED_ID, this.randomSeed);
        
        // 默认输出值
        PlantStructure plantWithFlowers = new PlantStructure();
        String flowerInfo = "No plant to add flowers";
        
        if (inputPlant != null && !inputPlant.isEmpty()) {
            try {
                // 验证花朵参数
                FlowerPattern currentFlowerPattern = FlowerPattern.fromString(flowerPatternStr);
                flowerDensityValue = Math.max(0.0f, Math.min(1.0f, flowerDensityValue != null ? flowerDensityValue : 0.3f));
                flowerMaterialValue = validateMaterial(flowerMaterialValue, "minecraft:poppy");
                onlyOnLeavesValue = onlyOnLeavesValue != null ? onlyOnLeavesValue : true;
                seasonalBloomValue = seasonalBloomValue != null ? seasonalBloomValue : false;
                bloomChanceValue = Math.max(0.0f, Math.min(1.0f, bloomChanceValue != null ? bloomChanceValue : 0.8f));
                randomSeedValue = randomSeedValue != null ? randomSeedValue : 12345;
                
                // 复制原始植物结构
                plantWithFlowers = inputPlant.copy();
                
                // 添加花朵
                int flowersAdded = addFlowers(plantWithFlowers, currentFlowerPattern, flowerDensityValue, 
                                            flowerMaterialValue, onlyOnLeavesValue, seasonalBloomValue, 
                                            bloomChanceValue, randomSeedValue);
                
                // 复制原始元数据并添加花朵信息
                if (inputPlant.getMetadata() != null) {
                    for (java.util.Map.Entry<String, Object> entry : inputPlant.getMetadata().entrySet()) {
                        plantWithFlowers.setMetadata(entry.getKey(), entry.getValue());
                    }
                }
                plantWithFlowers.setMetadata("flower_pattern", currentFlowerPattern.getId());
                plantWithFlowers.setMetadata("flower_density", flowerDensityValue);
                plantWithFlowers.setMetadata("flower_material", flowerMaterialValue);
                plantWithFlowers.setMetadata("only_on_leaves", onlyOnLeavesValue);
                plantWithFlowers.setMetadata("seasonal_bloom", seasonalBloomValue);
                plantWithFlowers.setMetadata("bloom_chance", bloomChanceValue);
                plantWithFlowers.setMetadata("flowers_added", flowersAdded);
                
                // 生成花朵信息
                flowerInfo = String.format("Added %s flowers: Pattern=%s, Density=%.1f%%, Bloom Chance=%.1f%%, Added %d flowers",
                    flowerMaterialValue, currentFlowerPattern.getDisplayName(), flowerDensityValue * 100, 
                    bloomChanceValue * 100, flowersAdded);
                
            } catch (Exception e) {
                LOGGER.error("Error in Add Flowers", e);
                plantWithFlowers = inputPlant; // 返回原始植物
                flowerInfo = "Error during flower addition";
            }
        }
        
        // 设置输出
        outputValues.put(OUTPUT_PLANT_STRUCTURE_ID, plantWithFlowers);
        outputValues.put(OUTPUT_FLOWER_INFO_ID, flowerInfo);
    }
    
    /**
     * 为植物添加花朵
     */
    private int addFlowers(PlantStructure plant, FlowerPattern pattern, float density, String material,
                          boolean onlyOnLeaves, boolean seasonalBloom, float bloomChance, int seed) {
        Random random = new Random(seed);
        int flowersAdded = 0;
        
        // 季节性开花检查
        if (seasonalBloom && random.nextFloat() > bloomChance) {
            return 0; // 不在开花季节
        }
        
        switch (pattern) {
            case RANDOM:
                flowersAdded = addRandomFlowers(plant, density, material, onlyOnLeaves, random);
                break;
            case BRANCH_TIPS:
                flowersAdded = addBranchTipFlowers(plant, density, material, onlyOnLeaves, random);
                break;
            case CLUSTER:
                flowersAdded = addClusterFlowers(plant, density, material, onlyOnLeaves, random);
                break;
            case SPARSE:
                flowersAdded = addSparseFlowers(plant, density * 0.5f, material, onlyOnLeaves, random);
                break;
            case DENSE:
                flowersAdded = addDenseFlowers(plant, density * 1.5f, material, onlyOnLeaves, random);
                break;
        }
        
        return flowersAdded;
    }
    
    /**
     * 随机添加花朵
     */
    private int addRandomFlowers(PlantStructure plant, float density, String material, boolean onlyOnLeaves, Random random) {
        int flowersAdded = 0;
        
        // 获取候选位置
        java.util.List<BlockPos> candidatePositions = getCandidatePositions(plant, onlyOnLeaves);
        
        for (BlockPos pos : candidatePositions) {
            if (random.nextFloat() < density) {
                // 在附近添加花朵
                BlockPos flowerPos = findNearbyFlowerPosition(plant, pos, random);
                if (flowerPos != null) {
                    plant.addFlowerBlock(flowerPos, material);
                    flowersAdded++;
                }
            }
        }
        
        return flowersAdded;
    }
    
    /**
     * 在枝端添加花朵
     */
    private int addBranchTipFlowers(PlantStructure plant, float density, String material, boolean onlyOnLeaves, Random random) {
        int flowersAdded = 0;
        
        // 找到枝端位置
        java.util.List<BlockPos> branchTips = findBranchTips(plant);
        
        for (BlockPos tip : branchTips) {
            if (random.nextFloat() < density) {
                // 在枝端或叶子附近添加花朵
                if (!onlyOnLeaves || hasNearbyLeaves(plant, tip)) {
                    BlockPos flowerPos = findNearbyFlowerPosition(plant, tip, random);
                    if (flowerPos != null) {
                        plant.addFlowerBlock(flowerPos, material);
                        flowersAdded++;
                    }
                }
            }
        }
        
        return flowersAdded;
    }
    
    /**
     * 簇状添加花朵
     */
    private int addClusterFlowers(PlantStructure plant, float density, String material, boolean onlyOnLeaves, Random random) {
        int flowersAdded = 0;
        
        // 选择几个聚集中心
        java.util.List<BlockPos> candidatePositions = getCandidatePositions(plant, onlyOnLeaves);
        int clusterCount = Math.max(1, (int) (candidatePositions.size() * density * 0.2f));
        
        for (int i = 0; i < clusterCount; i++) {
            if (candidatePositions.isEmpty()) break;
            
            // 随机选择一个聚集中心
            BlockPos center = candidatePositions.get(random.nextInt(candidatePositions.size()));
            
            // 在中心周围添加花朵簇
            int clusterSize = random.nextInt(5) + 3;
            for (int j = 0; j < clusterSize; j++) {
                int dx = random.nextInt(3) - 1;
                int dy = random.nextInt(2) - 1;
                int dz = random.nextInt(3) - 1;
                
                BlockPos flowerPos = new BlockPos(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                
                // 检查位置是否合适
                if (isValidFlowerPosition(plant, flowerPos, onlyOnLeaves)) {
                    plant.addFlowerBlock(flowerPos, material);
                    flowersAdded++;
                }
            }
        }
        
        return flowersAdded;
    }
    
    /**
     * 稀疏添加花朵
     */
    private int addSparseFlowers(PlantStructure plant, float density, String material, boolean onlyOnLeaves, Random random) {
        return addRandomFlowers(plant, density, material, onlyOnLeaves, random);
    }
    
    /**
     * 密集添加花朵
     */
    private int addDenseFlowers(PlantStructure plant, float density, String material, boolean onlyOnLeaves, Random random) {
        int flowersAdded = addRandomFlowers(plant, Math.min(1.0f, density), material, onlyOnLeaves, random);
        
        // 额外添加一轮花朵
        flowersAdded += addRandomFlowers(plant, density * 0.5f, material, onlyOnLeaves, random);
        
        return flowersAdded;
    }
    
    /**
     * 获取候选花朵位置
     */
    private java.util.List<BlockPos> getCandidatePositions(PlantStructure plant, boolean onlyOnLeaves) {
        java.util.List<BlockPos> candidates = new java.util.ArrayList<>();
        
        if (onlyOnLeaves) {
            // 只考虑叶子位置
            for (PlantStructure.PlantBlock leaf : plant.getLeafBlocks()) {
                candidates.add(leaf.getPosition());
            }
        } else {
            // 考虑所有枝干位置
            for (PlantStructure.PlantBlock branch : plant.getBranchBlocks()) {
                candidates.add(branch.getPosition());
            }
            for (PlantStructure.PlantBlock leaf : plant.getLeafBlocks()) {
                candidates.add(leaf.getPosition());
            }
        }
        
        return candidates;
    }
    
    /**
     * 找到枝端位置
     */
    private java.util.List<BlockPos> findBranchTips(PlantStructure plant) {
        java.util.List<BlockPos> tips = new java.util.ArrayList<>();
        java.util.Set<BlockPos> allPositions = new java.util.HashSet<>();
        
        // 收集所有植物方块位置
        for (PlantStructure.PlantBlock block : plant.getAllBlocks()) {
            allPositions.add(block.getPosition());
        }
        
        // 找到枝端（只有很少邻居的位置）
        for (PlantStructure.PlantBlock branch : plant.getBranchBlocks()) {
            BlockPos pos = branch.getPosition();
            int neighborCount = countPlantNeighbors(pos, allPositions);
            
            if (neighborCount <= 2) { // 枝端通常只有1-2个邻居
                tips.add(pos);
            }
        }
        
        return tips;
    }
    
    /**
     * 计算植物邻居数量
     */
    private int countPlantNeighbors(BlockPos pos, java.util.Set<BlockPos> allPositions) {
        int count = 0;
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    
                    BlockPos neighbor = new BlockPos(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    if (allPositions.contains(neighbor)) {
                        count++;
                    }
                }
            }
        }
        
        return count;
    }
    
    /**
     * 检查附近是否有叶子
     */
    private boolean hasNearbyLeaves(PlantStructure plant, BlockPos pos) {
        for (PlantStructure.PlantBlock leaf : plant.getLeafBlocks()) {
            BlockPos leafPos = leaf.getPosition();
            double distance = Math.sqrt(
                Math.pow(pos.getX() - leafPos.getX(), 2) +
                Math.pow(pos.getY() - leafPos.getY(), 2) +
                Math.pow(pos.getZ() - leafPos.getZ(), 2)
            );
            
            if (distance <= 2.0) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 寻找附近的花朵位置
     */
    private BlockPos findNearbyFlowerPosition(PlantStructure plant, BlockPos center, Random random) {
        // 在中心附近寻找合适的花朵位置
        for (int attempt = 0; attempt < 10; attempt++) {
            int dx = random.nextInt(3) - 1;
            int dy = random.nextInt(2);
            int dz = random.nextInt(3) - 1;
            
            BlockPos candidate = new BlockPos(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
            
            if (isValidFlowerPosition(plant, candidate, false)) {
                return candidate;
            }
        }
        
        return center; // 如果找不到更好的位置，就用中心位置
    }
    
    /**
     * 检查是否是有效的花朵位置
     */
    private boolean isValidFlowerPosition(PlantStructure plant, BlockPos pos, boolean onlyOnLeaves) {
        // 不能与现有方块重叠
        for (PlantStructure.PlantBlock block : plant.getAllBlocks()) {
            if (block.getPosition().equals(pos)) {
                return false;
            }
        }
        
        // 如果只能在叶子附近，检查附近是否有叶子
        if (onlyOnLeaves && !hasNearbyLeaves(plant, pos)) {
            return false;
        }
        
        return true;
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
    
    public FlowerPattern getFlowerPattern() {
        return flowerPattern;
    }
    
    public void setFlowerPattern(FlowerPattern flowerPattern) {
        this.flowerPattern = flowerPattern != null ? flowerPattern : FlowerPattern.RANDOM;
        markDirty();
    }
    
    public void setFlowerPattern(String flowerPatternStr) {
        setFlowerPattern(FlowerPattern.fromString(flowerPatternStr));
    }
    
    public float getFlowerDensity() {
        return flowerDensity;
    }
    
    public void setFlowerDensity(float flowerDensity) {
        this.flowerDensity = Math.max(0.0f, Math.min(1.0f, flowerDensity));
        markDirty();
    }
    
    public String getFlowerMaterial() {
        return flowerMaterial;
    }
    
    public void setFlowerMaterial(String flowerMaterial) {
        this.flowerMaterial = validateMaterial(flowerMaterial, "minecraft:poppy");
        markDirty();
    }
    
    public boolean isOnlyOnLeaves() {
        return onlyOnLeaves;
    }
    
    public void setOnlyOnLeaves(boolean onlyOnLeaves) {
        this.onlyOnLeaves = onlyOnLeaves;
        markDirty();
    }
    
    public boolean isSeasonalBloom() {
        return seasonalBloom;
    }
    
    public void setSeasonalBloom(boolean seasonalBloom) {
        this.seasonalBloom = seasonalBloom;
        markDirty();
    }
    
    public float getBloomChance() {
        return bloomChance;
    }
    
    public void setBloomChance(float bloomChance) {
        this.bloomChance = Math.max(0.0f, Math.min(1.0f, bloomChance));
        markDirty();
    }
    
    public int getRandomSeed() {
        return randomSeed;
    }
    
    public void setRandomSeed(int randomSeed) {
        this.randomSeed = randomSeed;
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("flowerPattern", getFlowerPattern().getId());
        state.put("flowerDensity", getFlowerDensity());
        state.put("flowerMaterial", getFlowerMaterial());
        state.put("onlyOnLeaves", isOnlyOnLeaves());
        state.put("seasonalBloom", isSeasonalBloom());
        state.put("bloomChance", getBloomChance());
        state.put("randomSeed", getRandomSeed());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("flowerPattern")) {
                Object flowerPatternObj = stateMap.get("flowerPattern");
                if (flowerPatternObj instanceof String) {
                    setFlowerPattern((String) flowerPatternObj);
                }
            }
            
            if (stateMap.containsKey("flowerDensity")) {
                Object flowerDensityObj = stateMap.get("flowerDensity");
                if (flowerDensityObj instanceof Number) {
                    setFlowerDensity(((Number) flowerDensityObj).floatValue());
                }
            }
            
            if (stateMap.containsKey("flowerMaterial")) {
                Object flowerMaterialObj = stateMap.get("flowerMaterial");
                if (flowerMaterialObj instanceof String) {
                    setFlowerMaterial((String) flowerMaterialObj);
                }
            }
            
            if (stateMap.containsKey("onlyOnLeaves")) {
                Object onlyOnLeavesObj = stateMap.get("onlyOnLeaves");
                if (onlyOnLeavesObj instanceof Boolean) {
                    setOnlyOnLeaves((Boolean) onlyOnLeavesObj);
                }
            }
            
            if (stateMap.containsKey("seasonalBloom")) {
                Object seasonalBloomObj = stateMap.get("seasonalBloom");
                if (seasonalBloomObj instanceof Boolean) {
                    setSeasonalBloom((Boolean) seasonalBloomObj);
                }
            }
            
            if (stateMap.containsKey("bloomChance")) {
                Object bloomChanceObj = stateMap.get("bloomChance");
                if (bloomChanceObj instanceof Number) {
                    setBloomChance(((Number) bloomChanceObj).floatValue());
                }
            }
            
            if (stateMap.containsKey("randomSeed")) {
                Object randomSeedObj = stateMap.get("randomSeed");
                if (randomSeedObj instanceof Number) {
                    setRandomSeed(((Number) randomSeedObj).intValue());
                }
            }
        }
    }
} 