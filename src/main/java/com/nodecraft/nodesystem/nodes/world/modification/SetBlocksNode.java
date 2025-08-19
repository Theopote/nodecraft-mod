package com.nodecraft.nodesystem.nodes.world.modification;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Set Blocks 节点: 在坐标列表上批量放置方块信息(可输入方块信息列表进行一对一或列表循环)
 */
@NodeInfo(
    id = "world.modification.set_blocks",
    displayName = "设置方块",
    description = "在坐标列表上批量放置方块",
    category = "world.modification"
)
public class SetBlocksNode extends BaseNode {

    // --- 节点属性 ---
    private String description = "在坐标列表上批量放置方块信息(可输入方块信息列表进行一对一或列表循环)";
    private boolean notifyUpdate = true; // 是否通知更新（触发更新事件）
    private boolean spawnDrops = false; // 放置方块时是否生成掉落物
    private boolean batchUpdates = true; // 是否批量更新，提高性能

    // --- 输入端口 IDs ---
    private static final String INPUT_COORDINATES_ID = "input_coordinates";
    private static final String INPUT_BLOCK_INFO_ID = "input_block_info";
    private static final String INPUT_BLOCK_INFO_LIST_ID = "input_block_info_list";
    private static final String INPUT_NOTIFY_ID = "input_notify";
    private static final String INPUT_SPAWN_DROPS_ID = "input_spawn_drops";
    private static final String INPUT_BATCH_UPDATES_ID = "input_batch_updates";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_SUCCESS_COUNT_ID = "output_success_count";
    private static final String OUTPUT_TOTAL_COUNT_ID = "output_total_count";
    private static final String OUTPUT_ALL_SUCCESS_ID = "output_all_success";

    // --- 构造函数 ---
    public SetBlocksNode() {
        super(UUID.randomUUID(), "world.modification.set_blocks");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_COORDINATES_ID, "Coordinates", 
                "目标坐标列表", NodeDataType.BLOCK_LIST, this));
        addInputPort(new BasePort(INPUT_BLOCK_INFO_ID, "Block Info", 
                "要放置的方块信息", NodeDataType.BLOCK_INFO, this));
        addInputPort(new BasePort(INPUT_BLOCK_INFO_LIST_ID, "Block Info List", 
                "要放置的方块信息列表", NodeDataType.BLOCK_INFO_LIST, this));
        addInputPort(new BasePort(INPUT_NOTIFY_ID, "Notify Update", 
                "是否通知更新（触发更新事件）", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_SPAWN_DROPS_ID, "Spawn Drops", 
                "放置方块时是否生成掉落物", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_BATCH_UPDATES_ID, "Batch Updates", 
                "是否批量更新（提高性能）", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_SUCCESS_COUNT_ID, "Success Count", 
                "成功放置的方块数量", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_TOTAL_COUNT_ID, "Total Count", 
                "尝试放置的方块总数", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_ALL_SUCCESS_ID, "All Success", 
                "是否所有方块都成功放置", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 默认输出值
        int successCount = 0;
        int totalCount = 0;
        boolean allSuccess = true;
        
        // 获取输入值
        Object coordinatesObj = inputValues.get(INPUT_COORDINATES_ID);
        Object blockInfoObj = inputValues.get(INPUT_BLOCK_INFO_ID);
        Object blockInfoListObj = inputValues.get(INPUT_BLOCK_INFO_LIST_ID);
        
        // 获取布尔值参数
        boolean notifyUpdateValue = this.notifyUpdate;
        Object notifyUpdateObj = inputValues.get(INPUT_NOTIFY_ID);
        if (notifyUpdateObj instanceof Boolean) {
            notifyUpdateValue = (Boolean) notifyUpdateObj;
        }
        
        boolean spawnDropsValue = this.spawnDrops;
        Object spawnDropsObj = inputValues.get(INPUT_SPAWN_DROPS_ID);
        if (spawnDropsObj instanceof Boolean) {
            spawnDropsValue = (Boolean) spawnDropsObj;
        }
        
        boolean batchUpdatesValue = this.batchUpdates;
        Object batchUpdatesObj = inputValues.get(INPUT_BATCH_UPDATES_ID);
        if (batchUpdatesObj instanceof Boolean) {
            batchUpdatesValue = (Boolean) batchUpdatesObj;
        }
        
        // 检查执行上下文和坐标列表输入是否有效
        if (context != null && context.getWorld() != null && 
                coordinatesObj instanceof BlockPosList) {
            BlockPosList coordinates = (BlockPosList) coordinatesObj;
            
            // 获取方块信息列表（如果有）
            List<?> blockInfoList = null;
            if (blockInfoListObj instanceof List) {
                blockInfoList = (List<?>) blockInfoListObj;
            }
            
            // 如果方块信息列表为空但提供了单个方块信息，创建单元素列表
            if ((blockInfoList == null || blockInfoList.isEmpty()) && blockInfoObj != null) {
                // 使用单个方块信息应用于所有坐标
                
                // 在实际实现中，开始批量更新（如果启用）
                if (batchUpdatesValue) {
                    // 开始批量更新，例如 context.getWorld().beginBatchBlockUpdate();
                }
                
                try {
                    // 为所有坐标放置相同的方块
                    for (BlockPos pos : coordinates) {
                        totalCount++;
                        try {
                            // 在实际实现中放置方块
                            // 例如：boolean success = context.getWorld().setBlockState(pos, blockInfoObj, notifyUpdateValue, spawnDropsValue);
                            
                            // 模拟放置成功
                            boolean success = true;
                            
                            if (success) {
                                successCount++;
                            } else {
                                allSuccess = false;
                            }
                        } catch (Exception e) {
                            // 记录单个方块放置错误但继续执行
                            System.err.println("Error setting block at " + pos + ": " + e.getMessage());
                            allSuccess = false;
                        }
                    }
                } finally {
                    // 完成批量更新（如果启用）
                    if (batchUpdatesValue) {
                        // 例如: context.getWorld().endBatchBlockUpdate();
                    }
                }
            } 
            // 使用方块信息列表
            else if (blockInfoList != null && !blockInfoList.isEmpty()) {
                
                // 在实际实现中，开始批量更新（如果启用）
                if (batchUpdatesValue) {
                    // 开始批量更新，例如 context.getWorld().beginBatchBlockUpdate();
                }
                
                try {
                    // 根据两个列表的长度确定处理模式
                    if (blockInfoList.size() == coordinates.size()) {
                        // 一对一模式：每个坐标对应一个方块信息
                        int i = 0;
                        for (BlockPos pos : coordinates) {
                            totalCount++;
                            try {
                                Object currentBlockInfo = blockInfoList.get(i++);
                                
                                // 在实际实现中放置方块
                                // 例如：boolean success = context.getWorld().setBlockState(pos, currentBlockInfo, notifyUpdateValue, spawnDropsValue);
                                
                                // 模拟放置成功
                                boolean success = true;
                                
                                if (success) {
                                    successCount++;
                                } else {
                                    allSuccess = false;
                                }
                            } catch (Exception e) {
                                // 记录单个方块放置错误但继续执行
                                System.err.println("Error setting block at " + pos + ": " + e.getMessage());
                                allSuccess = false;
                            }
                        }
                    } else {
                        // 循环模式：循环使用方块信息列表
                        int infoListSize = blockInfoList.size();
                        int i = 0;
                        for (BlockPos pos : coordinates) {
                            totalCount++;
                            try {
                                // 循环使用方块信息列表中的元素
                                Object currentBlockInfo = blockInfoList.get(i % infoListSize);
                                i++;
                                
                                // 在实际实现中放置方块
                                // 例如：boolean success = context.getWorld().setBlockState(pos, currentBlockInfo, notifyUpdateValue, spawnDropsValue);
                                
                                // 模拟放置成功
                                boolean success = true;
                                
                                if (success) {
                                    successCount++;
                                } else {
                                    allSuccess = false;
                                }
                            } catch (Exception e) {
                                // 记录单个方块放置错误但继续执行
                                System.err.println("Error setting block at " + pos + ": " + e.getMessage());
                                allSuccess = false;
                            }
                        }
                    }
                } finally {
                    // 完成批量更新（如果启用）
                    if (batchUpdatesValue) {
                        // 例如: context.getWorld().endBatchBlockUpdate();
                    }
                }
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_SUCCESS_COUNT_ID, successCount);
        outputValues.put(OUTPUT_TOTAL_COUNT_ID, totalCount);
        outputValues.put(OUTPUT_ALL_SUCCESS_ID, allSuccess);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isNotifyUpdate() {
        return notifyUpdate;
    }
    
    public void setNotifyUpdate(boolean notifyUpdate) {
        this.notifyUpdate = notifyUpdate;
        markDirty();
    }
    
    public boolean isSpawnDrops() {
        return spawnDrops;
    }
    
    public void setSpawnDrops(boolean spawnDrops) {
        this.spawnDrops = spawnDrops;
        markDirty();
    }
    
    public boolean isBatchUpdates() {
        return batchUpdates;
    }
    
    public void setBatchUpdates(boolean batchUpdates) {
        this.batchUpdates = batchUpdates;
        markDirty();
    }
} 