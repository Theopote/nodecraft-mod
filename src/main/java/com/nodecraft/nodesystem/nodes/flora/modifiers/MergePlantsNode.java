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
import java.util.Map;
import java.util.UUID;

/**
 * Merge Plants 节点: 合并多个植物结构为一个
 */
@NodeInfo(
    id = "flora.modifiers.merge_plants",
    displayName = "Merge Plants",
    description = "Merges multiple plant structures into one",
    category = "flora.modifiers"
)
public class MergePlantsNode extends BaseNode {
    
    /**
     * 合并策略枚举
     */
    public enum MergeStrategy {
        SIMPLE("简单合并", "直接合并所有植物的方块"),
        DEDUPLICATION("去重合并", "合并时移除重复位置的方块"),
        PRIORITY("优先级合并", "按输入顺序决定重复位置的方块"),
        MATERIAL_PRIORITY("材质优先", "根据材质类型决定重复位置的方块");
        
        private final String displayName;
        private final String description;
        
        MergeStrategy(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getId() { return name().toLowerCase(); }
    }
    
    // --- 节点属性 ---
    private MergeStrategy mergeStrategy = MergeStrategy.SIMPLE;
    private boolean preserveOriginalMetadata = true;      // 保留原始元数据
    private boolean combineMetadata = false;              // 合并元数据
    private boolean validatePositions = true;             // 验证位置
    private String newPlantName = "Merged Plant";         // 新植物名称
    private String description = "合并多个植物结构为一个";
    
    // --- 输入端口 IDs ---
    private static final String INPUT_PLANT_LIST_ID = "input_plant_list";
    private static final String INPUT_MERGE_STRATEGY_ID = "input_merge_strategy";
    private static final String INPUT_PRESERVE_METADATA_ID = "input_preserve_metadata";
    private static final String INPUT_COMBINE_METADATA_ID = "input_combine_metadata";
    private static final String INPUT_VALIDATE_POSITIONS_ID = "input_validate_positions";
    private static final String INPUT_PLANT_NAME_ID = "input_plant_name";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_MERGED_PLANT_ID = "output_merged_plant";
    private static final String OUTPUT_MERGE_STATS_ID = "output_merge_stats";
    private static final String OUTPUT_CONFLICTS_ID = "output_conflicts";
    
    /**
     * 构造一个新的植物合并节点
     */
    public MergePlantsNode() {
        super(UUID.randomUUID(), "flora.modifiers.merge_plants");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_PLANT_LIST_ID, "Plant List", 
                "要合并的植物结构列表", NodeDataType.PLANT_STRUCTURE_LIST, this));
        addInputPort(new BasePort(INPUT_MERGE_STRATEGY_ID, "Merge Strategy", 
                "合并策略: simple, deduplication, priority, material_priority", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_PRESERVE_METADATA_ID, "Preserve Original Metadata", 
                "是否保留第一个植物的原始元数据", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_COMBINE_METADATA_ID, "Combine Metadata", 
                "是否将所有植物的元数据合并", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_VALIDATE_POSITIONS_ID, "Validate Positions", 
                "是否验证方块位置的有效性", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_PLANT_NAME_ID, "Plant Name", 
                "合并后植物的名称", NodeDataType.STRING, this));
        
        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_MERGED_PLANT_ID, "Merged Plant", 
                "合并后的植物结构", NodeDataType.PLANT_STRUCTURE, this));
        addOutputPort(new BasePort(OUTPUT_MERGE_STATS_ID, "Merge Statistics", 
                "合并操作的统计信息", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_CONFLICTS_ID, "Position Conflicts", 
                "位置冲突的详细信息", NodeDataType.STRING, this));
    }
    
    @Override
    public String getDescription() {
        return this.description;
    }
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        List<?> plantListObj = getInputValue(INPUT_PLANT_LIST_ID, new ArrayList<>());
        String mergeStrategyValue = getInputValue(INPUT_MERGE_STRATEGY_ID, this.mergeStrategy.getId());
        Boolean preserveMetadataValue = getInputValue(INPUT_PRESERVE_METADATA_ID, this.preserveOriginalMetadata);
        Boolean combineMetadataValue = getInputValue(INPUT_COMBINE_METADATA_ID, this.combineMetadata);
        Boolean validatePositionsValue = getInputValue(INPUT_VALIDATE_POSITIONS_ID, this.validatePositions);
        String plantNameValue = getInputValue(INPUT_PLANT_NAME_ID, this.newPlantName);
        
        // 默认输出值
        PlantStructure mergedPlant = new PlantStructure();
        String mergeStats = "No plants to merge";
        String conflicts = "No conflicts";
        
        try {
            // 转换植物列表
            List<PlantStructure> plants = new ArrayList<>();
            if (plantListObj instanceof List) {
                for (Object obj : plantListObj) {
                    if (obj instanceof PlantStructure) {
                        plants.add((PlantStructure) obj);
                    }
                }
            }
            
            if (!plants.isEmpty()) {
                // 解析合并策略
                MergeStrategy strategy = parseMergeStrategy(mergeStrategyValue);
                
                // 验证参数
                preserveMetadataValue = preserveMetadataValue != null ? preserveMetadataValue : true;
                combineMetadataValue = combineMetadataValue != null ? combineMetadataValue : false;
                validatePositionsValue = validatePositionsValue != null ? validatePositionsValue : true;
                plantNameValue = plantNameValue != null && !plantNameValue.trim().isEmpty() 
                    ? plantNameValue.trim() : "Merged Plant";
                
                // 执行合并
                MergeResult result = mergePlants(plants, strategy, preserveMetadataValue, 
                    combineMetadataValue, validatePositionsValue, plantNameValue);
                
                mergedPlant = result.mergedPlant;
                mergeStats = result.statistics;
                conflicts = result.conflicts;
            }
            
        } catch (Exception e) {
            System.err.println("Error in Merge Plants: " + e.getMessage());
            e.printStackTrace();
            mergeStats = "Error during merge: " + e.getMessage();
        }
        
        // 设置输出
        outputValues.put(OUTPUT_MERGED_PLANT_ID, mergedPlant);
        outputValues.put(OUTPUT_MERGE_STATS_ID, mergeStats);
        outputValues.put(OUTPUT_CONFLICTS_ID, conflicts);
    }
    
    /**
     * 合并结果类
     */
    private static class MergeResult {
        PlantStructure mergedPlant;
        String statistics;
        String conflicts;
        
        MergeResult(PlantStructure mergedPlant, String statistics, String conflicts) {
            this.mergedPlant = mergedPlant;
            this.statistics = statistics;
            this.conflicts = conflicts;
        }
    }
    
    /**
     * 执行植物合并
     */
    private MergeResult mergePlants(List<PlantStructure> plants, MergeStrategy strategy,
                                    boolean preserveMetadata, boolean combineMetadata,
                                    boolean validatePositions, String plantName) {
        
        PlantStructure merged = new PlantStructure();
        StringBuilder statsBuilder = new StringBuilder();
        StringBuilder conflictsBuilder = new StringBuilder();
        
        // 统计信息
        int totalInputPlants = plants.size();
        int totalInputBlocks = 0;
        int conflictCount = 0;
        Map<BlockPos, String> positionMap = new java.util.HashMap<>();
        
        // 计算输入方块总数
        for (PlantStructure plant : plants) {
            totalInputBlocks += plant.getTotalBlockCount();
        }
        
        // 根据策略合并植物
        for (int i = 0; i < plants.size(); i++) {
            PlantStructure plant = plants.get(i);
            
            // 合并各类型方块
            mergeBlocks(merged, plant.getTrunkBlocks(), strategy, positionMap, 
                conflictsBuilder, "trunk", i, validatePositions);
            mergeBlocks(merged, plant.getBranchBlocks(), strategy, positionMap, 
                conflictsBuilder, "branch", i, validatePositions);
            mergeBlocks(merged, plant.getLeafBlocks(), strategy, positionMap, 
                conflictsBuilder, "leaf", i, validatePositions);
            mergeBlocks(merged, plant.getFlowerBlocks(), strategy, positionMap, 
                conflictsBuilder, "flower", i, validatePositions);
            mergeBlocks(merged, plant.getRootBlocks(), strategy, positionMap, 
                conflictsBuilder, "root", i, validatePositions);
        }
        
        // 处理元数据
        if (preserveMetadata && !plants.isEmpty()) {
            // 保留第一个植物的元数据
            Map<String, Object> firstMetadata = plants.get(0).getMetadata();
            if (firstMetadata != null) {
                for (Map.Entry<String, Object> entry : firstMetadata.entrySet()) {
                    merged.setMetadata(entry.getKey(), entry.getValue());
                }
            }
        }
        
        if (combineMetadata) {
            // 合并所有元数据
            for (int i = 0; i < plants.size(); i++) {
                Map<String, Object> metadata = plants.get(i).getMetadata();
                if (metadata != null) {
                    for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                        String key = entry.getKey() + "_plant" + i;
                        merged.setMetadata(key, entry.getValue());
                    }
                }
            }
        }
        
        // 设置合并信息
        merged.setMetadata("merged_plant_name", plantName);
        merged.setMetadata("merge_strategy", strategy.getId());
        merged.setMetadata("source_plant_count", totalInputPlants);
        merged.setMetadata("merge_timestamp", System.currentTimeMillis());
        
        // 生成统计信息
        conflictCount = conflictsBuilder.length() > 0 ? 
            conflictsBuilder.toString().split("Conflict").length - 1 : 0;
        
        statsBuilder.append(String.format("Merge Statistics:\n"));
        statsBuilder.append(String.format("Strategy: %s\n", strategy.getDisplayName()));
        statsBuilder.append(String.format("Input Plants: %d\n", totalInputPlants));
        statsBuilder.append(String.format("Input Blocks: %d\n", totalInputBlocks));
        statsBuilder.append(String.format("Output Blocks: %d\n", merged.getTotalBlockCount()));
        statsBuilder.append(String.format("Position Conflicts: %d\n", conflictCount));
        statsBuilder.append(String.format("Compression Ratio: %.2f%%\n", 
            totalInputBlocks > 0 ? (double) merged.getTotalBlockCount() / totalInputBlocks * 100 : 0));
        
        String conflictStr = conflictsBuilder.length() > 0 ? 
            conflictsBuilder.toString() : "No position conflicts detected";
        
        return new MergeResult(merged, statsBuilder.toString(), conflictStr);
    }
    
    /**
     * 合并方块列表
     */
    private void mergeBlocks(PlantStructure merged, List<PlantStructure.PlantBlock> blocks,
                           MergeStrategy strategy, Map<BlockPos, String> positionMap,
                           StringBuilder conflictsBuilder, String blockType, int plantIndex,
                           boolean validatePositions) {
        
        for (PlantStructure.PlantBlock block : blocks) {
            BlockPos pos = block.getPosition();
            
            // 验证位置
            if (validatePositions && pos == null) {
                continue;
            }
            
            // 检查位置冲突
            String existingType = positionMap.get(pos);
            if (existingType != null) {
                conflictsBuilder.append(String.format("Conflict at %s: %s (plant %d) vs %s\n",
                    pos, blockType, plantIndex, existingType));
                
                // 根据策略处理冲突
                if (strategy == MergeStrategy.SIMPLE || strategy == MergeStrategy.PRIORITY) {
                    // 简单合并或优先级合并：跳过重复
                    continue;
                } else if (strategy == MergeStrategy.DEDUPLICATION) {
                    // 去重合并：移除旧的，添加新的
                    removeBlockAtPosition(merged, pos);
                } else if (strategy == MergeStrategy.MATERIAL_PRIORITY) {
                    // 材质优先：根据材质类型决定
                    if (!shouldReplaceByMaterial(existingType, blockType)) {
                        continue;
                    }
                    removeBlockAtPosition(merged, pos);
                }
            }
            
            // 添加方块到合并结果
            positionMap.put(pos, blockType);
            switch (blockType) {
                case "trunk":
                    merged.addTrunkBlock(pos, block.getBlockType(), block.getThickness());
                    break;
                case "branch":
                    merged.addBranchBlock(pos, block.getBlockType(), block.getThickness());
                    break;
                case "leaf":
                    merged.addLeafBlock(pos, block.getBlockType());
                    break;
                case "flower":
                    merged.addFlowerBlock(pos, block.getBlockType());
                    break;
                case "root":
                    merged.addRootBlock(pos, block.getBlockType());
                    break;
            }
            
            // 复制NBT数据
            if (block.getNbtData() != null) {
                PlantStructure.PlantBlock mergedBlock = findBlockAtPosition(merged, pos);
                if (mergedBlock != null) {
                    mergedBlock.setNbtData(block.getNbtData());
                }
            }
        }
    }
    
    /**
     * 根据材质优先级决定是否替换
     */
    private boolean shouldReplaceByMaterial(String existingType, String newType) {
        // 定义材质优先级：trunk > branch > root > leaf > flower
        int existingPriority = getMaterialPriority(existingType);
        int newPriority = getMaterialPriority(newType);
        return newPriority > existingPriority;
    }
    
    /**
     * 获取材质优先级
     */
    private int getMaterialPriority(String materialType) {
        switch (materialType.toLowerCase()) {
            case "trunk": return 5;
            case "branch": return 4;
            case "root": return 3;
            case "leaf": return 2;
            case "flower": return 1;
            default: return 0;
        }
    }
    
    /**
     * 移除指定位置的方块
     */
    private void removeBlockAtPosition(PlantStructure plant, BlockPos pos) {
        // 注意：这里需要PlantStructure提供移除方块的方法
        // 目前简化处理，实际实现可能需要扩展PlantStructure类
    }
    
    /**
     * 查找指定位置的方块
     */
    private PlantStructure.PlantBlock findBlockAtPosition(PlantStructure plant, BlockPos pos) {
        for (PlantStructure.PlantBlock block : plant.getAllBlocks()) {
            if (pos.equals(block.getPosition())) {
                return block;
            }
        }
        return null;
    }
    
    /**
     * 解析合并策略
     */
    private MergeStrategy parseMergeStrategy(String strategyStr) {
        if (strategyStr == null || strategyStr.trim().isEmpty()) {
            return MergeStrategy.SIMPLE;
        }
        
        String normalized = strategyStr.trim().toLowerCase();
        for (MergeStrategy strategy : MergeStrategy.values()) {
            if (strategy.getId().equals(normalized) || 
                strategy.name().toLowerCase().equals(normalized)) {
                return strategy;
            }
        }
        
        return MergeStrategy.SIMPLE;
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
    
    public MergeStrategy getMergeStrategy() {
        return mergeStrategy;
    }
    
    public void setMergeStrategy(MergeStrategy mergeStrategy) {
        this.mergeStrategy = mergeStrategy != null ? mergeStrategy : MergeStrategy.SIMPLE;
        markDirty();
    }
    
    public boolean isPreserveOriginalMetadata() {
        return preserveOriginalMetadata;
    }
    
    public void setPreserveOriginalMetadata(boolean preserveOriginalMetadata) {
        this.preserveOriginalMetadata = preserveOriginalMetadata;
        markDirty();
    }
    
    public boolean isCombineMetadata() {
        return combineMetadata;
    }
    
    public void setCombineMetadata(boolean combineMetadata) {
        this.combineMetadata = combineMetadata;
        markDirty();
    }
    
    public boolean isValidatePositions() {
        return validatePositions;
    }
    
    public void setValidatePositions(boolean validatePositions) {
        this.validatePositions = validatePositions;
        markDirty();
    }
    
    public String getNewPlantName() {
        return newPlantName;
    }
    
    public void setNewPlantName(String newPlantName) {
        this.newPlantName = newPlantName != null ? newPlantName : "Merged Plant";
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        Map<String, Object> state = new java.util.HashMap<>();
        state.put("mergeStrategy", getMergeStrategy().getId());
        state.put("preserveOriginalMetadata", isPreserveOriginalMetadata());
        state.put("combineMetadata", isCombineMetadata());
        state.put("validatePositions", isValidatePositions());
        state.put("newPlantName", getNewPlantName());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof Map) {
            Map<?, ?> stateMap = (Map<?, ?>) state;
            
            if (stateMap.containsKey("mergeStrategy")) {
                Object strategyObj = stateMap.get("mergeStrategy");
                if (strategyObj instanceof String) {
                    setMergeStrategy(parseMergeStrategy((String) strategyObj));
                }
            }
            
            if (stateMap.containsKey("preserveOriginalMetadata")) {
                Object preserveObj = stateMap.get("preserveOriginalMetadata");
                if (preserveObj instanceof Boolean) {
                    setPreserveOriginalMetadata((Boolean) preserveObj);
                }
            }
            
            if (stateMap.containsKey("combineMetadata")) {
                Object combineObj = stateMap.get("combineMetadata");
                if (combineObj instanceof Boolean) {
                    setCombineMetadata((Boolean) combineObj);
                }
            }
            
            if (stateMap.containsKey("validatePositions")) {
                Object validateObj = stateMap.get("validatePositions");
                if (validateObj instanceof Boolean) {
                    setValidatePositions((Boolean) validateObj);
                }
            }
            
            if (stateMap.containsKey("newPlantName")) {
                Object nameObj = stateMap.get("newPlantName");
                if (nameObj instanceof String) {
                    setNewPlantName((String) nameObj);
                }
            }
        }
    }
} 