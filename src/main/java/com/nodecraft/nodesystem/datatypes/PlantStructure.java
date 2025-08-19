package com.nodecraft.nodesystem.datatypes;

import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 植物结构数据类型，表示生成的植物的三维结构信息
 */
public class PlantStructure {
    private final List<PlantBlock> trunkBlocks;      // 树干方块列表
    private final List<PlantBlock> branchBlocks;     // 树枝方块列表
    private final List<PlantBlock> leafBlocks;       // 叶子方块列表
    private final List<PlantBlock> flowerBlocks;     // 花朵方块列表
    private final List<PlantBlock> rootBlocks;       // 根系方块列表
    private final Map<String, Object> metadata;     // 附加元数据
    
    /**
     * 植物方块信息，包含坐标、材质和NBT数据
     */
    public static class PlantBlock {
        private final BlockPos position;
        private String blockType;      // 方块类型ID
        private Object nbtData;        // NBT数据
        private float thickness;       // 厚度/半径（用于渲染）
        private Map<String, Object> properties; // 附加属性
        
        public PlantBlock(BlockPos position, String blockType) {
            this.position = position;
            this.blockType = blockType != null ? blockType : "minecraft:air";
            this.thickness = 1.0f;
            this.properties = new HashMap<>();
        }
        
        public PlantBlock(BlockPos position, String blockType, float thickness) {
            this(position, blockType);
            this.thickness = Math.max(0.1f, thickness);
        }
        
        // Getters and setters
        public BlockPos getPosition() { return position; }
        public String getBlockType() { return blockType; }
        public void setBlockType(String blockType) { this.blockType = blockType; }
        public Object getNbtData() { return nbtData; }
        public void setNbtData(Object nbtData) { this.nbtData = nbtData; }
        public float getThickness() { return thickness; }
        public void setThickness(float thickness) { this.thickness = Math.max(0.1f, thickness); }
        public Map<String, Object> getProperties() { return properties; }
        public void setProperty(String key, Object value) { properties.put(key, value); }
        public Object getProperty(String key) { return properties.get(key); }
        
        @Override
        public String toString() {
            return String.format("PlantBlock{pos=%s, type=%s, thickness=%.2f}", 
                               position, blockType, thickness);
        }
    }
    
    /**
     * 创建一个空的植物结构
     */
    public PlantStructure() {
        this.trunkBlocks = new ArrayList<>();
        this.branchBlocks = new ArrayList<>();
        this.leafBlocks = new ArrayList<>();
        this.flowerBlocks = new ArrayList<>();
        this.rootBlocks = new ArrayList<>();
        this.metadata = new HashMap<>();
    }
    
    /**
     * 创建一个植物结构，使用预定义的方块列表
     */
    public PlantStructure(List<PlantBlock> trunkBlocks, 
                         List<PlantBlock> branchBlocks,
                         List<PlantBlock> leafBlocks,
                         List<PlantBlock> flowerBlocks,
                         List<PlantBlock> rootBlocks) {
        this.trunkBlocks = new ArrayList<>(trunkBlocks != null ? trunkBlocks : new ArrayList<>());
        this.branchBlocks = new ArrayList<>(branchBlocks != null ? branchBlocks : new ArrayList<>());
        this.leafBlocks = new ArrayList<>(leafBlocks != null ? leafBlocks : new ArrayList<>());
        this.flowerBlocks = new ArrayList<>(flowerBlocks != null ? flowerBlocks : new ArrayList<>());
        this.rootBlocks = new ArrayList<>(rootBlocks != null ? rootBlocks : new ArrayList<>());
        this.metadata = new HashMap<>();
    }
    
    // --- 方块添加方法 ---
    
    public void addTrunkBlock(BlockPos pos, String blockType) {
        trunkBlocks.add(new PlantBlock(pos, blockType));
    }
    
    public void addTrunkBlock(BlockPos pos, String blockType, float thickness) {
        trunkBlocks.add(new PlantBlock(pos, blockType, thickness));
    }
    
    public void addBranchBlock(BlockPos pos, String blockType) {
        branchBlocks.add(new PlantBlock(pos, blockType));
    }
    
    public void addBranchBlock(BlockPos pos, String blockType, float thickness) {
        branchBlocks.add(new PlantBlock(pos, blockType, thickness));
    }
    
    public void addLeafBlock(BlockPos pos, String blockType) {
        leafBlocks.add(new PlantBlock(pos, blockType));
    }
    
    public void addFlowerBlock(BlockPos pos, String blockType) {
        flowerBlocks.add(new PlantBlock(pos, blockType));
    }
    
    public void addRootBlock(BlockPos pos, String blockType) {
        rootBlocks.add(new PlantBlock(pos, blockType));
    }
    
    // --- 批量操作方法 ---
    
    public void addTrunkBlocks(List<PlantBlock> blocks) {
        if (blocks != null) trunkBlocks.addAll(blocks);
    }
    
    public void addBranchBlocks(List<PlantBlock> blocks) {
        if (blocks != null) branchBlocks.addAll(blocks);
    }
    
    public void addLeafBlocks(List<PlantBlock> blocks) {
        if (blocks != null) leafBlocks.addAll(blocks);
    }
    
    public void addFlowerBlocks(List<PlantBlock> blocks) {
        if (blocks != null) flowerBlocks.addAll(blocks);
    }
    
    public void addRootBlocks(List<PlantBlock> blocks) {
        if (blocks != null) rootBlocks.addAll(blocks);
    }
    
    // --- Getters ---
    
    public List<PlantBlock> getTrunkBlocks() { return new ArrayList<>(trunkBlocks); }
    public List<PlantBlock> getBranchBlocks() { return new ArrayList<>(branchBlocks); }
    public List<PlantBlock> getLeafBlocks() { return new ArrayList<>(leafBlocks); }
    public List<PlantBlock> getFlowerBlocks() { return new ArrayList<>(flowerBlocks); }
    public List<PlantBlock> getRootBlocks() { return new ArrayList<>(rootBlocks); }
    
    /**
     * 获取所有方块的扁平列表
     * @return 所有方块的列表
     */
    public List<PlantBlock> getAllBlocks() {
        List<PlantBlock> allBlocks = new ArrayList<>();
        allBlocks.addAll(trunkBlocks);
        allBlocks.addAll(branchBlocks);
        allBlocks.addAll(leafBlocks);
        allBlocks.addAll(flowerBlocks);
        allBlocks.addAll(rootBlocks);
        return allBlocks;
    }
    
    /**
     * 获取所有方块的坐标列表
     * @return 所有方块的坐标列表
     */
    public List<BlockPos> getAllPositions() {
        List<BlockPos> positions = new ArrayList<>();
        for (PlantBlock block : getAllBlocks()) {
            positions.add(block.getPosition());
        }
        return positions;
    }
    
    // --- 统计信息 ---
    
    public int getTotalBlockCount() {
        return trunkBlocks.size() + branchBlocks.size() + leafBlocks.size() + 
               flowerBlocks.size() + rootBlocks.size();
    }
    
    public int getTrunkBlockCount() { return trunkBlocks.size(); }
    public int getBranchBlockCount() { return branchBlocks.size(); }
    public int getLeafBlockCount() { return leafBlocks.size(); }
    public int getFlowerBlockCount() { return flowerBlocks.size(); }
    public int getRootBlockCount() { return rootBlocks.size(); }
    
    // --- 元数据管理 ---
    
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    public void setMetadata(String key, Object value) { metadata.put(key, value); }
    public Object getMetadata(String key) { return metadata.get(key); }
    public void removeMetadata(String key) { metadata.remove(key); }
    
    // --- 实用方法 ---
    
    /**
     * 清空植物结构
     */
    public void clear() {
        trunkBlocks.clear();
        branchBlocks.clear();
        leafBlocks.clear();
        flowerBlocks.clear();
        rootBlocks.clear();
        metadata.clear();
    }
    
    /**
     * 检查植物结构是否为空
     * @return 如果没有任何方块返回true
     */
    public boolean isEmpty() {
        return getTotalBlockCount() == 0;
    }
    
    /**
     * 复制植物结构
     * @return 植物结构的深拷贝
     */
    public PlantStructure copy() {
        PlantStructure copy = new PlantStructure();
        copy.addTrunkBlocks(new ArrayList<>(this.trunkBlocks));
        copy.addBranchBlocks(new ArrayList<>(this.branchBlocks));
        copy.addLeafBlocks(new ArrayList<>(this.leafBlocks));
        copy.addFlowerBlocks(new ArrayList<>(this.flowerBlocks));
        copy.addRootBlocks(new ArrayList<>(this.rootBlocks));
        copy.metadata.putAll(this.metadata);
        return copy;
    }
    
    @Override
    public String toString() {
        return String.format("PlantStructure{trunk=%d, branch=%d, leaf=%d, flower=%d, root=%d, total=%d}",
                           getTrunkBlockCount(), getBranchBlockCount(), getLeafBlockCount(),
                           getFlowerBlockCount(), getRootBlockCount(), getTotalBlockCount());
    }
} 