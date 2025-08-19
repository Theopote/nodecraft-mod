package com.nodecraft.minecraft.registry;

/**
 * 表示方块对应的物品
 */
public class BlockItem extends Item {
    private final Block block;
    
    /**
     * 创建方块物品
     * @param id 物品ID
     * @param block 对应的方块
     */
    public BlockItem(ResourceLocation id, Block block) {
        super(id);
        this.block = block;
        
        // 设置方块物品的最大堆叠数量为64
        setMaxStackSize(64);
    }
    
    /**
     * 获取对应的方块
     * @return 方块
     */
    public Block getBlock() {
        return block;
    }
    
    @Override
    public String getTranslationKey() {
        // 对于方块物品，翻译键通常和方块相同
        if (block != null) {
            return block.getTranslationKey();
        }
        return super.getTranslationKey();
    }
} 