package com.nodecraft.nodesystem.nodes.world.inventory;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Get Items in Inventory 节点: 获取容器中的物品。
 */
@NodeInfo(
    id = "world.inventory.get_items_in_inventory",
    displayName = "获取容器物品",
    description = "获取容器中的物品",
    category = "world.inventory"
)
public class GetItemsInInventoryNode extends BaseNode {

    // --- 节点属性 ---
    private String inventoryType = "main"; // 背包类型：main, armor, offhand, enderchest, all
    private String description = "获取玩家背包中的所有物品";

    // --- 输入端口 IDs ---
    private static final String INPUT_PLAYER_ID = "input_player";
    private static final String INPUT_INVENTORY_TYPE_ID = "input_inventory_type";
    private static final String INPUT_FILTER_EMPTY_ID = "input_filter_empty";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_ITEMS_ID = "output_items";
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_ITEM_COUNT_ID = "output_item_count";
    private static final String OUTPUT_SLOTS_INFO_ID = "output_slots_info";

    // --- 构造函数 ---
    public GetItemsInInventoryNode() {
        super(UUID.randomUUID(), "world.inventory.get_items_in_inventory");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_PLAYER_ID, "Player", 
                "目标玩家", NodeDataType.PLAYER, this));
        addInputPort(new BasePort(INPUT_INVENTORY_TYPE_ID, "Inventory Type", 
                "背包类型 (main, armor, offhand, enderchest, all)", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_FILTER_EMPTY_ID, "Filter Empty Slots", 
                "是否过滤空槽位", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_ITEMS_ID, "Items", 
                "获取到的物品列表", NodeDataType.LIST, this));
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功获取物品", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_ITEM_COUNT_ID, "Item Count", 
                "获取到的物品数量", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_SLOTS_INFO_ID, "Slots Info", 
                "每个物品对应的槽位信息", NodeDataType.LIST, this));
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 默认输出值
        List<Object> items = new ArrayList<>();
        boolean success = false;
        int itemCount = 0;
        List<Map<String, Object>> slotsInfo = new ArrayList<>();
        
        // 获取输入值
        Object playerObj = inputValues.get(INPUT_PLAYER_ID);
        Object inventoryTypeObj = inputValues.get(INPUT_INVENTORY_TYPE_ID);
        Object filterEmptyObj = inputValues.get(INPUT_FILTER_EMPTY_ID);
        
        // 确定背包类型
        String invType = this.inventoryType;
        if (inventoryTypeObj instanceof String) {
            invType = ((String) inventoryTypeObj).toLowerCase();
        }
        
        // 确定是否过滤空槽位
        boolean filterEmpty = true;
        if (filterEmptyObj instanceof Boolean) {
            filterEmpty = (Boolean) filterEmptyObj;
        }
        
        // 检查必要的输入是否存在
        if (playerObj != null) {
            try {
                /*
                在实际实现中，需要根据Minecraft API实现以下操作:
                1. 获取玩家的背包
                2. 根据背包类型和过滤选项获取物品
                3. 提取物品信息和槽位信息
                
                if (playerObj instanceof PlayerEntity) {
                    PlayerEntity player = (PlayerEntity) playerObj;
                    PlayerInventory inventory = player.getInventory();
                    
                    // 处理不同类型的背包
                    switch (invType) {
                        case "main": 
                            // 主背包（包括快捷栏和主背包）
                            for (int i = 0; i < 36; i++) {
                                processInventorySlot(inventory, i, items, slotsInfo, filterEmpty);
                            }
                            break;
                            
                        case "armor":
                            // 装备栏
                            for (int i = 36; i < 40; i++) {
                                processInventorySlot(inventory, i, items, slotsInfo, filterEmpty);
                            }
                            break;
                            
                        case "offhand":
                            // 副手
                            processInventorySlot(inventory, 40, items, slotsInfo, filterEmpty);
                            break;
                            
                        case "enderchest":
                            // 末影箱
                            PlayerEntity serverPlayer = player;
                            Inventory enderChest = serverPlayer.getEnderChestInventory();
                            
                            for (int i = 0; i < enderChest.size(); i++) {
                                ItemStack item = enderChest.getStack(i);
                                if (!item.isEmpty() || !filterEmpty) {
                                    items.add(item);
                                    
                                    Map<String, Object> slotInfo = new HashMap<>();
                                    slotInfo.put("slot", i);
                                    slotInfo.put("type", "enderchest");
                                    slotsInfo.add(slotInfo);
                                }
                            }
                            break;
                            
                        case "all":
                        default:
                            // 所有物品栏
                            // 主背包（包括快捷栏和主背包）
                            for (int i = 0; i < 36; i++) {
                                processInventorySlot(inventory, i, items, slotsInfo, filterEmpty);
                            }
                            
                            // 装备栏
                            for (int i = 36; i < 40; i++) {
                                processInventorySlot(inventory, i, items, slotsInfo, filterEmpty);
                            }
                            
                            // 副手
                            processInventorySlot(inventory, 40, items, slotsInfo, filterEmpty);
                            break;
                    }
                    
                    itemCount = items.size();
                    success = true;
                }
                */
                
                // 模拟成功获取背包物品 (在实际实现中替换为上面的逻辑)
                for (int i = 0; i < 5; i++) {
                    Object mockItem = new Object(); // 模拟物品
                    items.add(mockItem);
                    
                    Map<String, Object> slotInfo = new HashMap<>();
                    slotInfo.put("slot", i);
                    slotInfo.put("type", invType);
                    slotsInfo.add(slotInfo);
                }
                
                itemCount = items.size();
                success = true;
            } catch (Exception e) {
                success = false;
                System.err.println("Error getting items in inventory: " + e.getMessage());
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_ITEMS_ID, items);
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_ITEM_COUNT_ID, itemCount);
        outputValues.put(OUTPUT_SLOTS_INFO_ID, slotsInfo);
    }
    
    /* 
    // 处理背包槽位的辅助方法
    private void processInventorySlot(PlayerInventory inventory, int slot, 
                                     List<Object> items, List<Map<String, Object>> slotsInfo, 
                                     boolean filterEmpty) {
        ItemStack item = inventory.getStack(slot);
        if (!item.isEmpty() || !filterEmpty) {
            items.add(item);
            
            Map<String, Object> slotInfo = new HashMap<>();
            slotInfo.put("slot", slot);
            
            // 确定槽位类型
            String type = "main";
            if (slot >= 0 && slot < 9) {
                type = "hotbar";
            } else if (slot >= 9 && slot < 36) {
                type = "main";
            } else if (slot >= 36 && slot < 40) {
                type = "armor";
            } else if (slot == 40) {
                type = "offhand";
            }
            
            slotInfo.put("type", type);
            slotsInfo.add(slotInfo);
        }
    }
    */
    
    // --- Getters/Setters for Properties ---
    
    public String getInventoryType() {
        return inventoryType;
    }
    
    public void setInventoryType(String inventoryType) {
        if (isValidInventoryType(inventoryType)) {
            this.inventoryType = inventoryType.toLowerCase();
            markDirty();
        }
    }
    
    /**
     * 检查背包类型是否有效
     */
    private boolean isValidInventoryType(String type) {
        if (type == null) return false;
        
        switch (type.toLowerCase()) {
            case "main":
            case "armor":
            case "offhand":
            case "enderchest":
            case "all":
                return true;
            default:
                return false;
        }
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        return inventoryType;
    }
    
    /**
     * 恢复节点状态
     */
    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof String) {
            setInventoryType((String) state);
        }
    }
} 