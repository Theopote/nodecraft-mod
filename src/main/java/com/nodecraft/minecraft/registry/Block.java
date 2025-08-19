package com.nodecraft.minecraft.registry;

/**
 * 表示Minecraft方块的类
 */
public class Block {
    private final ResourceLocation id;
    private String translationKey;
    private boolean solid = true;
    private float hardness = 1.0f;
    private float resistance = 1.0f;
    
    /**
     * 创建方块
     * @param id 方块ID
     */
    public Block(ResourceLocation id) {
        this.id = id;
        this.translationKey = "block." + id.getNamespace() + "." + id.getPath();
    }
    
    /**
     * 获取方块ID
     * @return 方块ID
     */
    public ResourceLocation getId() {
        return id;
    }
    
    /**
     * 获取方块翻译键
     * @return 翻译键
     */
    public String getTranslationKey() {
        return translationKey;
    }
    
    /**
     * 设置方块翻译键
     * @param translationKey 翻译键
     * @return 方块自身（链式调用）
     */
    public Block setTranslationKey(String translationKey) {
        this.translationKey = translationKey;
        return this;
    }
    
    /**
     * 方块是否是实心的
     * @return 是否实心
     */
    public boolean isSolid() {
        return solid;
    }
    
    /**
     * 设置方块是否是实心的
     * @param solid 是否实心
     * @return 方块自身
     */
    public Block setSolid(boolean solid) {
        this.solid = solid;
        return this;
    }
    
    /**
     * 获取方块硬度
     * @return 硬度
     */
    public float getHardness() {
        return hardness;
    }
    
    /**
     * 设置方块硬度
     * @param hardness 硬度
     * @return 方块自身
     */
    public Block setHardness(float hardness) {
        this.hardness = hardness;
        return this;
    }
    
    /**
     * 获取方块抗爆炸值
     * @return 抗爆炸值
     */
    public float getResistance() {
        return resistance;
    }
    
    /**
     * 设置方块抗爆炸值
     * @param resistance 抗爆炸值
     * @return 方块自身
     */
    public Block setResistance(float resistance) {
        this.resistance = resistance;
        return this;
    }
    
    @Override
    public String toString() {
        return id.toString();
    }
} 