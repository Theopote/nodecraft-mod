package com.nodecraft.minecraft.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 物品注册表实现
 */
public class ItemRegistry implements MinecraftRegistry<Item> {
    private static ItemRegistry instance;
    private final Map<ResourceLocation, Item> items = new HashMap<>();
    
    // 单例模式
    private ItemRegistry() {
        initializeDefaults();
    }
    
    /**
     * 获取单例实例
     * @return 物品注册表实例
     */
    public static synchronized ItemRegistry getInstance() {
        if (instance == null) {
            instance = new ItemRegistry();
        }
        return instance;
    }
    
    /**
     * 初始化默认物品
     */
    private void initializeDefaults() {
        // 添加一些常见的物品用于测试
        String[] vanillaItems = {
            "apple", "arrow", "baked_potato", "beef", "blaze_powder", "blaze_rod", "bone", "bone_meal",
            "book", "bowl", "bread", "brick", "bucket", "carrot", "charcoal", "coal", "compass", "cookie",
            "diamond", "diamond_axe", "diamond_boots", "diamond_chestplate", "diamond_helmet", "diamond_hoe",
            "diamond_leggings", "diamond_pickaxe", "diamond_shovel", "diamond_sword", "egg", "emerald",
            "ender_pearl", "experience_bottle", "feather", "fishing_rod", "flint", "flint_and_steel", "ghast_tear",
            "glass_bottle", "glowstone_dust", "gold_ingot", "gold_nugget", "golden_apple", "golden_axe",
            "golden_boots", "golden_carrot", "golden_chestplate", "golden_helmet", "golden_hoe"
        };
        
        // 添加更多更近版本的物品
        String[] newerItems = {
            "amethyst_shard", "axolotl_bucket", "bundle", "copper_ingot", "glow_berries", "glow_ink_sac",
            "goat_horn", "honey_bottle", "honeycomb", "netherite_axe", "netherite_boots", "netherite_chestplate",
            "netherite_helmet", "netherite_hoe", "netherite_ingot", "netherite_leggings", "netherite_pickaxe",
            "netherite_scrap", "netherite_shovel", "netherite_sword", "nether_star", "potion", "raw_copper",
            "raw_gold", "raw_iron", "recovery_compass", "spyglass", "totem_of_undying", "trident", "warped_fungus_on_a_stick"
        };
        
        // 向注册表添加物品
        for (String itemName : vanillaItems) {
            ResourceLocation key = new ResourceLocation("minecraft", itemName);
            items.put(key, new Item(key));
        }
        
        for (String itemName : newerItems) {
            ResourceLocation key = new ResourceLocation("minecraft", itemName);
            items.put(key, new Item(key));
        }
        
        // 添加一些模组物品示例
        String[] modItems = {
            "example:magical_wand", "example:energy_crystal", "example:ancient_artifact", 
            "nodecraft:node_configurator", "nodecraft:redstone_remote", "nodecraft:logic_blueprint"
        };
        
        for (String itemId : modItems) {
            ResourceLocation key = ResourceLocation.fromString(itemId);
            items.put(key, new Item(key));
        }
        
        // 添加方块物品 (从BlockRegistry中获取方块并为它们创建物品)
        BlockRegistry blockRegistry = BlockRegistry.getInstance();
        for (ResourceLocation blockId : blockRegistry.getKeys()) {
            if (!items.containsKey(blockId)) {
                items.put(blockId, new BlockItem(blockId, blockRegistry.getValue(blockId).orElse(null)));
            }
        }
    }
    
    @Override
    public List<ResourceLocation> getKeys() {
        return new ArrayList<>(items.keySet());
    }
    
    /**
     * 获取所有物品ID的字符串表示
     * @return 物品ID列表
     */
    public List<String> getItemIds() {
        return items.keySet().stream()
                .map(ResourceLocation::toString)
                .sorted()
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Item> getValue(ResourceLocation key) {
        return Optional.ofNullable(items.get(key));
    }
    
    @Override
    public boolean containsKey(ResourceLocation key) {
        return items.containsKey(key);
    }
    
    /**
     * 注册一个新物品
     * @param key 物品ID
     * @param item 物品
     */
    public void register(ResourceLocation key, Item item) {
        items.put(key, item);
    }
} 