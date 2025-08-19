package com.nodecraft.minecraft.registry;

/**
 * 表示Minecraft物品的类
 */
public class Item {
    private final ResourceLocation id;
    private String translationKey;
    private int maxStackSize = 64;
    private int maxDurability = 0;
    
    /**
     * 创建物品
     * @param id 物品ID
     */
    public Item(ResourceLocation id) {
        this.id = id;
        this.translationKey = "item." + id.getNamespace() + "." + id.getPath();
    }
    
    /**
     * 获取物品ID
     * @return 物品ID
     */
    public ResourceLocation getId() {
        return id;
    }
    
    /**
     * 获取物品翻译键
     * @return 翻译键
     */
    public String getTranslationKey() {
        return translationKey;
    }
    
    /**
     * 设置物品翻译键
     * @param translationKey 翻译键
     * @return 物品自身（链式调用）
     */
    public Item setTranslationKey(String translationKey) {
        this.translationKey = translationKey;
        return this;
    }
    
    /**
     * 获取最大堆叠数量
     * @return 最大堆叠数量
     */
    public int getMaxStackSize() {
        return maxStackSize;
    }
    
    /**
     * 设置最大堆叠数量
     * @param maxStackSize 最大堆叠数量
     * @return 物品自身
     */
    public Item setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
        return this;
    }
    
    /**
     * 获取最大耐久度
     * @return 最大耐久度
     */
    public int getMaxDurability() {
        return maxDurability;
    }
    
    /**
     * 设置最大耐久度
     * @param maxDurability 最大耐久度
     * @return 物品自身
     */
    public Item setMaxDurability(int maxDurability) {
        this.maxDurability = maxDurability;
        return this;
    }
    
    /**
     * 物品是否有耐久度
     * @return 是否有耐久度
     */
    public boolean isDamageable() {
        return maxDurability > 0;
    }
    
    @Override
    public String toString() {
        return id.toString();
    }
} 