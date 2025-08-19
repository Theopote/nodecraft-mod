package com.nodecraft.nodesystem.interaction;

import com.nodecraft.nodesystem.util.Coordinate;
import com.nodecraft.nodesystem.util.BlockStateData;

/**
 * 方块拾取回调接口
 * 当玩家在游戏世界中选择方块时，通过此接口通知相关节点
 */
public interface IBlockPickerCallback extends NodeEditorInteractionManager.IInteractionCallback {
    
    /**
     * 当方块被成功拾取时调用
     * @param position 方块的整数坐标
     * @param blockId 方块ID（如"minecraft:stone"）
     * @param blockStateData 方块状态数据（如朝向、是否点亮等）
     */
    void onBlockPicked(Coordinate position, String blockId, BlockStateData blockStateData);
    
    /**
     * 当拾取操作被取消时调用
     */
    default void onPickingCancelled() {
        // 默认空实现
    }
    
    /**
     * 实现父接口的取消方法
     */
    @Override
    default void onInteractionCancelled() {
        onPickingCancelled();
    }
    
    /**
     * 获取拾取配置参数
     * @return 拾取配置
     */
    default BlockPickingConfig getPickingConfig() {
        return new BlockPickingConfig();
    }
    
    /**
     * 方块拾取配置
     */
    class BlockPickingConfig {
        private float maxDistance = 100.0f;
        private boolean includeFluids = false;
        private boolean useHandItem = false;
        
        public float getMaxDistance() {
            return maxDistance;
        }
        
        public void setMaxDistance(float maxDistance) {
            this.maxDistance = maxDistance;
        }
        
        public boolean isIncludeFluids() {
            return includeFluids;
        }
        
        public void setIncludeFluids(boolean includeFluids) {
            this.includeFluids = includeFluids;
        }
        
        public boolean isUseHandItem() {
            return useHandItem;
        }
        
        public void setUseHandItem(boolean useHandItem) {
            this.useHandItem = useHandItem;
        }
    }
} 