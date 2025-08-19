package com.nodecraft.minecraft.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 方块注册表实现
 */
public class BlockRegistry implements MinecraftRegistry<Block> {
    private static BlockRegistry instance;
    private final Map<ResourceLocation, Block> blocks = new HashMap<>();
    
    // 单例模式
    private BlockRegistry() {
        initializeDefaults();
    }
    
    /**
     * 获取单例实例
     * @return 方块注册表实例
     */
    public static synchronized BlockRegistry getInstance() {
        if (instance == null) {
            instance = new BlockRegistry();
        }
        return instance;
    }
    
    /**
     * 初始化默认方块
     */
    private void initializeDefaults() {
        // 添加一些常见的方块用于测试
        String[] vanillaBlocks = {
            "stone", "granite", "polished_granite", "diorite", "polished_diorite", "andesite", "polished_andesite",
            "grass_block", "dirt", "coarse_dirt", "podzol", "cobblestone", "oak_planks", "spruce_planks",
            "birch_planks", "jungle_planks", "acacia_planks", "dark_oak_planks", "sand", "red_sand",
            "gravel", "gold_ore", "iron_ore", "coal_ore", "oak_log", "spruce_log", "birch_log", "jungle_log",
            "acacia_log", "dark_oak_log", "stripped_oak_log", "stripped_spruce_log", "stripped_birch_log",
            "stripped_jungle_log", "stripped_acacia_log", "stripped_dark_oak_log", "oak_wood", "spruce_wood",
            "birch_wood", "jungle_wood", "acacia_wood", "dark_oak_wood"
        };
        
        // 添加更多更近版本的方块
        String[] newerBlocks = {
            "amethyst_block", "ancient_debris", "basalt", "blackstone", "calcite", "copper_block", "copper_ore",
            "crying_obsidian", "deepslate", "deepslate_coal_ore", "deepslate_copper_ore", "deepslate_diamond_ore",
            "deepslate_emerald_ore", "deepslate_gold_ore", "deepslate_iron_ore", "deepslate_lapis_ore", 
            "deepslate_redstone_ore", "dripstone_block", "moss_block", "netherite_block", "raw_copper_block",
            "raw_gold_block", "raw_iron_block", "sculk", "sculk_catalyst", "sculk_sensor", "sculk_shrieker",
            "sculk_vein", "tuff", "warped_planks", "warped_stem", "crimson_planks", "crimson_stem"
        };
        
        // 向注册表添加方块
        for (String blockName : vanillaBlocks) {
            ResourceLocation key = new ResourceLocation("minecraft", blockName);
            blocks.put(key, new Block(key));
        }
        
        for (String blockName : newerBlocks) {
            ResourceLocation key = new ResourceLocation("minecraft", blockName);
            blocks.put(key, new Block(key));
        }
        
        // 添加一些模组方块示例
        String[] modBlocks = {
            "example:custom_block", "example:magical_ore", "example:ancient_stone", 
            "nodecraft:node_block", "nodecraft:redstone_interface", "nodecraft:logic_block"
        };
        
        for (String blockId : modBlocks) {
            ResourceLocation key = ResourceLocation.fromString(blockId);
            blocks.put(key, new Block(key));
        }
    }
    
    @Override
    public List<ResourceLocation> getKeys() {
        return new ArrayList<>(blocks.keySet());
    }
    
    /**
     * 获取所有方块ID的字符串表示
     * @return 方块ID列表
     */
    public List<String> getBlockIds() {
        return blocks.keySet().stream()
                .map(ResourceLocation::toString)
                .sorted()
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Block> getValue(ResourceLocation key) {
        return Optional.ofNullable(blocks.get(key));
    }
    
    @Override
    public boolean containsKey(ResourceLocation key) {
        return blocks.containsKey(key);
    }
    
    /**
     * 注册一个新方块
     * @param key 方块ID
     * @param block 方块
     */
    public void register(ResourceLocation key, Block block) {
        blocks.put(key, block);
    }
} 