package com.nodecraft.core.item;

import com.nodecraft.core.NodeCraft;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

/**
 * 模组物品注册类
 */
public class ModItems {
    
    // NodeCraft工具物品
    public static final NodeCraftToolItem NODECRAFT_TOOL = registerItem(
            NodeCraftToolItem::new,
            new Item.Settings().maxCount(1));
    
    /**
     * 注册所有模组物品
     */
    public static void registerItems() {
        // 物品已在静态初始化时注册
        NodeCraft.LOGGER.info("已注册NodeCraft物品");
    }
    
    /**
     * 注册物品的辅助方法
     */
    @SuppressWarnings("unchecked")
    private static <T extends Item> T registerItem(Function<Item.Settings, Item> factory, Item.Settings settings) {
        RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(NodeCraft.MOD_ID, "nodecraft_tool"));
        return (T) Items.register(registryKey, factory, settings);
    }
} 