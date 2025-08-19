package com.nodecraft.nodesystem.nodes.flora.modifiers;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.PlantStructure;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Prune Plant 节点: 修剪植物结构
 */
@NodeInfo(
    id = "flora.modifiers.prune_plant",
    displayName = "Prune Plant",
    description = "Prunes plant structures by removing parts",
    category = "flora.modifiers"
)
public class PrunePlantNode extends BaseNode {
    
    // --- 节点属性 ---
    private float prunePercentage = 0.3f;     // 修剪比例（0-1）
    private boolean pruneRandomly = true;     // 是否随机修剪
    private boolean pruneTopFirst = false;    // 是否优先修剪顶部
    private float heightThreshold = 0.5f;     // 高度阈值（0-1，相对于植物总高度）
    private int randomSeed = 12345;           // 随机种子
    private String description = "修剪植物结构的指定部分";
    
    // --- 输入端口 IDs ---
    private static final String INPUT_PLANT_STRUCTURE_ID = "input_plant_structure";
    private static final String INPUT_PRUNE_PERCENTAGE_ID = "input_prune_percentage";
    private static final String INPUT_PRUNE_RANDOMLY_ID = "input_prune_randomly";
    private static final String INPUT_PRUNE_TOP_FIRST_ID = "input_prune_top_first";
    private static final String INPUT_HEIGHT_THRESHOLD_ID = "input_height_threshold";
    private static final String INPUT_RANDOM_SEED_ID = "input_random_seed";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_PLANT_STRUCTURE_ID = "output_plant_structure";
    private static final String OUTPUT_PRUNE_INFO_ID = "output_prune_info";
    
    /**
     * 构造一个新的修剪植物节点
     */
    public PrunePlantNode() {
        super(UUID.randomUUID(), "flora.modifiers.prune_plant");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_PLANT_STRUCTURE_ID, "Plant Structure", 
                "要修剪的植物结构", NodeDataType.PLANT_STRUCTURE, this));
        addInputPort(new BasePort(INPUT_PRUNE_PERCENTAGE_ID, "Prune Percentage", 
                "修剪比例（0-1，0=不修剪，1=全部修剪）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_PRUNE_RANDOMLY_ID, "Prune Randomly", 
                "是否随机修剪（否则按高度修剪）", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_PRUNE_TOP_FIRST_ID, "Prune Top First", 
                "是否优先修剪顶部（仅在非随机模式下生效）", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_HEIGHT_THRESHOLD_ID, "Height Threshold", 
                "高度阈值（0-1，相对于植物总高度）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_RANDOM_SEED_ID, "Random Seed", 
                "随机种子（用于随机修剪）", NodeDataType.INTEGER, this));
        
        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_PLANT_STRUCTURE_ID, "Pruned Plant Structure", 
                "修剪后的植物结构", NodeDataType.PLANT_STRUCTURE, this));
        addOutputPort(new BasePort(OUTPUT_PRUNE_INFO_ID, "Prune Info", 
                "修剪操作的详细信息", NodeDataType.STRING, this));
    }
    
    @Override
    public String getDescription() {
        return this.description;
    }
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        PlantStructure inputPlant = getInputValue(INPUT_PLANT_STRUCTURE_ID, null);
        Float prunePercentageValue = getInputValue(INPUT_PRUNE_PERCENTAGE_ID, this.prunePercentage);
        Boolean pruneRandomlyValue = getInputValue(INPUT_PRUNE_RANDOMLY_ID, this.pruneRandomly);
        Boolean pruneTopFirstValue = getInputValue(INPUT_PRUNE_TOP_FIRST_ID, this.pruneTopFirst);
        Float heightThresholdValue = getInputValue(INPUT_HEIGHT_THRESHOLD_ID, this.heightThreshold);
        Integer randomSeedValue = getInputValue(INPUT_RANDOM_SEED_ID, this.randomSeed);
        
        // 默认输出值
        PlantStructure prunedPlant = new PlantStructure();
        String pruneInfo = "No plant to prune";
        
        if (inputPlant != null && !inputPlant.isEmpty()) {
            try {
                // 验证修剪参数
                prunePercentageValue = Math.max(0.0f, Math.min(1.0f, prunePercentageValue != null ? prunePercentageValue : 0.3f));
                pruneRandomlyValue = pruneRandomlyValue != null ? pruneRandomlyValue : true;
                pruneTopFirstValue = pruneTopFirstValue != null ? pruneTopFirstValue : false;
                heightThresholdValue = Math.max(0.0f, Math.min(1.0f, heightThresholdValue != null ? heightThresholdValue : 0.5f));
                randomSeedValue = randomSeedValue != null ? randomSeedValue : 12345;
                
                // 如果修剪比例为0，直接返回原植物
                if (prunePercentageValue < 0.01f) {
                    prunedPlant = inputPlant.copy();
                    pruneInfo = "No pruning applied (percentage is 0)";
                } else {
                    // 执行修剪
                    prunedPlant = pruneFullPlant(inputPlant, prunePercentageValue, pruneRandomlyValue, 
                                                pruneTopFirstValue, heightThresholdValue, randomSeedValue);
                    
                    // 复制原始元数据并添加修剪信息
                    if (inputPlant.getMetadata() != null) {
                        for (java.util.Map.Entry<String, Object> entry : inputPlant.getMetadata().entrySet()) {
                            prunedPlant.setMetadata(entry.getKey(), entry.getValue());
                        }
                    }
                    prunedPlant.setMetadata("prune_percentage", prunePercentageValue);
                    prunedPlant.setMetadata("prune_randomly", pruneRandomlyValue);
                    prunedPlant.setMetadata("prune_top_first", pruneTopFirstValue);
                    prunedPlant.setMetadata("height_threshold", heightThresholdValue);
                    prunedPlant.setMetadata("prune_seed", randomSeedValue);
                    
                    // 生成修剪信息
                    int originalCount = inputPlant.getTotalBlockCount();
                    int prunedCount = prunedPlant.getTotalBlockCount();
                    int removedCount = originalCount - prunedCount;
                    
                    pruneInfo = String.format("Pruned: %.1f%% (%s), Removed: %d blocks, Remaining: %d blocks",
                        prunePercentageValue * 100, pruneRandomlyValue ? "Random" : (pruneTopFirstValue ? "Top-First" : "Bottom-First"),
                        removedCount, prunedCount);
                }
                
            } catch (Exception e) {
                System.err.println("Error in Prune Plant: " + e.getMessage());
                e.printStackTrace();
                prunedPlant = inputPlant; // 返回原始植物
                pruneInfo = "Error during pruning";
            }
        }
        
        // 设置输出
        outputValues.put(OUTPUT_PLANT_STRUCTURE_ID, prunedPlant);
        outputValues.put(OUTPUT_PRUNE_INFO_ID, pruneInfo);
    }
    
    /**
     * 修剪整个植物结构
     */
    private PlantStructure pruneFullPlant(PlantStructure original, float prunePercentage, boolean pruneRandomly,
                                         boolean pruneTopFirst, float heightThreshold, int randomSeed) {
        PlantStructure pruned = new PlantStructure();
        Random random = new Random(randomSeed);
        
        // 计算植物的高度范围
        int minY = getMinY(original);
        int maxY = getMaxY(original);
        int heightRange = maxY - minY + 1;
        int thresholdY = minY + (int) (heightRange * heightThreshold);
        
        // 修剪各个部分（主要修剪分支和叶子，保留主干）
        pruned.addTrunkBlocks(original.getTrunkBlocks()); // 保留所有主干
        
        // 修剪分支
        List<PlantStructure.PlantBlock> prunedBranches = pruneBlockList(
            original.getBranchBlocks(), prunePercentage, pruneRandomly, pruneTopFirst, thresholdY, random);
        pruned.addBranchBlocks(prunedBranches);
        
        // 修剪叶子
        List<PlantStructure.PlantBlock> prunedLeaves = pruneBlockList(
            original.getLeafBlocks(), prunePercentage, pruneRandomly, pruneTopFirst, thresholdY, random);
        pruned.addLeafBlocks(prunedLeaves);
        
        // 修剪花朵
        List<PlantStructure.PlantBlock> prunedFlowers = pruneBlockList(
            original.getFlowerBlocks(), prunePercentage * 0.5f, pruneRandomly, pruneTopFirst, thresholdY, random);
        pruned.addFlowerBlocks(prunedFlowers);
        
        // 保留根系
        pruned.addRootBlocks(original.getRootBlocks());
        
        return pruned;
    }
    
    /**
     * 修剪方块列表
     */
    private List<PlantStructure.PlantBlock> pruneBlockList(List<PlantStructure.PlantBlock> originalBlocks,
                                                          float prunePercentage, boolean pruneRandomly,
                                                          boolean pruneTopFirst, int thresholdY, Random random) {
        if (originalBlocks.isEmpty() || prunePercentage <= 0.0f) {
            return new ArrayList<>(originalBlocks);
        }
        
        List<PlantStructure.PlantBlock> blocksToPrune = new ArrayList<>(originalBlocks);
        int totalCount = blocksToPrune.size();
        int removeCount = (int) (totalCount * prunePercentage);
        
        if (removeCount <= 0) {
            return new ArrayList<>(originalBlocks);
        }
        
        if (pruneRandomly) {
            // 随机修剪
            for (int i = 0; i < removeCount && !blocksToPrune.isEmpty(); i++) {
                int indexToRemove = random.nextInt(blocksToPrune.size());
                blocksToPrune.remove(indexToRemove);
            }
        } else {
            // 按高度修剪
            blocksToPrune.sort((a, b) -> {
                if (pruneTopFirst) {
                    return Integer.compare(b.getPosition().getY(), a.getPosition().getY()); // 从高到低
                } else {
                    return Integer.compare(a.getPosition().getY(), b.getPosition().getY()); // 从低到高
                }
            });
            
            // 移除前removeCount个方块
            for (int i = 0; i < removeCount && !blocksToPrune.isEmpty(); i++) {
                blocksToPrune.remove(0);
            }
        }
        
        return blocksToPrune;
    }
    
    /**
     * 获取植物的最小Y坐标
     */
    private int getMinY(PlantStructure plant) {
        List<PlantStructure.PlantBlock> allBlocks = plant.getAllBlocks();
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
        List<PlantStructure.PlantBlock> allBlocks = plant.getAllBlocks();
        if (allBlocks.isEmpty()) return 0;
        
        int maxY = Integer.MIN_VALUE;
        for (PlantStructure.PlantBlock block : allBlocks) {
            maxY = Math.max(maxY, block.getPosition().getY());
        }
        return maxY;
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
    
    public float getPrunePercentage() {
        return prunePercentage;
    }
    
    public void setPrunePercentage(float prunePercentage) {
        this.prunePercentage = Math.max(0.0f, Math.min(1.0f, prunePercentage));
        markDirty();
    }
    
    public boolean isPruneRandomly() {
        return pruneRandomly;
    }
    
    public void setPruneRandomly(boolean pruneRandomly) {
        this.pruneRandomly = pruneRandomly;
        markDirty();
    }
    
    public boolean isPruneTopFirst() {
        return pruneTopFirst;
    }
    
    public void setPruneTopFirst(boolean pruneTopFirst) {
        this.pruneTopFirst = pruneTopFirst;
        markDirty();
    }
    
    public float getHeightThreshold() {
        return heightThreshold;
    }
    
    public void setHeightThreshold(float heightThreshold) {
        this.heightThreshold = Math.max(0.0f, Math.min(1.0f, heightThreshold));
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
        state.put("prunePercentage", getPrunePercentage());
        state.put("pruneRandomly", isPruneRandomly());
        state.put("pruneTopFirst", isPruneTopFirst());
        state.put("heightThreshold", getHeightThreshold());
        state.put("randomSeed", getRandomSeed());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("prunePercentage")) {
                Object prunePercentageObj = stateMap.get("prunePercentage");
                if (prunePercentageObj instanceof Number) {
                    setPrunePercentage(((Number) prunePercentageObj).floatValue());
                }
            }
            
            if (stateMap.containsKey("pruneRandomly")) {
                Object pruneRandomlyObj = stateMap.get("pruneRandomly");
                if (pruneRandomlyObj instanceof Boolean) {
                    setPruneRandomly((Boolean) pruneRandomlyObj);
                }
            }
            
            if (stateMap.containsKey("pruneTopFirst")) {
                Object pruneTopFirstObj = stateMap.get("pruneTopFirst");
                if (pruneTopFirstObj instanceof Boolean) {
                    setPruneTopFirst((Boolean) pruneTopFirstObj);
                }
            }
            
            if (stateMap.containsKey("heightThreshold")) {
                Object heightThresholdObj = stateMap.get("heightThreshold");
                if (heightThresholdObj instanceof Number) {
                    setHeightThreshold(((Number) heightThresholdObj).floatValue());
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