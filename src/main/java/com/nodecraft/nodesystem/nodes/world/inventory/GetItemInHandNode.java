package com.nodecraft.nodesystem.nodes.world.inventory;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Get Item in Hand 节点: 获取玩家手中的物品。
 */
@NodeInfo(
    id = "world.inventory.get_item_in_hand",
    displayName = "获取手中物品",
    description = "获取玩家手中的物品",
    category = "world.inventory"
)
public class GetItemInHandNode extends BaseNode {

    // --- 节点属性 ---
    private boolean useOffHand = false; // 是否使用副手
    private String description = "获取玩家手持 ItemStack";

    // --- 输入端口 IDs ---
    private static final String INPUT_PLAYER_ID = "input_player";
    private static final String INPUT_USE_OFFHAND_ID = "input_use_offhand";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_ITEM_ID = "output_item";
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_IS_EMPTY_ID = "output_is_empty";
    private static final String OUTPUT_ITEM_ID_ID = "output_item_id";

    // --- 构造函数 ---
    public GetItemInHandNode() {
        super(UUID.randomUUID(), "world.inventory.get_item_in_hand");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_PLAYER_ID, "Player", 
                "目标玩家", NodeDataType.PLAYER, this));
        addInputPort(new BasePort(INPUT_USE_OFFHAND_ID, "Use Off Hand", 
                "是否使用副手", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_ITEM_ID, "Item", 
                "玩家手持的物品", NodeDataType.ITEM_STACK, this));
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功获取物品", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_IS_EMPTY_ID, "Is Empty", 
                "物品是否为空", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_ITEM_ID_ID, "Item ID", 
                "物品的ID", NodeDataType.STRING, this));
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 默认输出值
        Object itemStack = null;
        boolean success = false;
        boolean isEmpty = true;
        String itemId = "";
        
        // 获取输入值
        Object playerObj = inputValues.get(INPUT_PLAYER_ID);
        Object useOffHandObj = inputValues.get(INPUT_USE_OFFHAND_ID);
        
        // 确定是否使用副手
        boolean useOffHand = this.useOffHand;
        if (useOffHandObj instanceof Boolean) {
            useOffHand = (Boolean) useOffHandObj;
        }
        
        // 检查必要的输入是否存在
        if (playerObj != null) {
            try {
                /*
                在实际实现中，需要根据Minecraft API实现以下操作:
                1. 从玩家对象获取手持物品
                2. 确定是主手还是副手
                3. 提取物品信息
                
                if (playerObj instanceof PlayerEntity) {
                    PlayerEntity player = (PlayerEntity) playerObj;
                    
                    // 获取手持物品
                    ItemStack heldItem = useOffHand ? 
                            player.getOffHandStack() : 
                            player.getMainHandStack();
                    
                    itemStack = heldItem;
                    isEmpty = heldItem.isEmpty();
                    
                    if (!isEmpty) {
                        // 获取物品ID
                        itemId = Registry.ITEM.getId(heldItem.getItem()).toString();
                    }
                    
                    success = true;
                }
                */
                
                // 模拟成功获取手持物品 (在实际实现中替换为上面的逻辑)
                itemStack = new Object(); // 模拟物品
                isEmpty = false;
                itemId = "minecraft:diamond_sword";
                success = true;
            } catch (Exception e) {
                success = false;
                System.err.println("Error getting item in hand: " + e.getMessage());
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_ITEM_ID, itemStack);
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_IS_EMPTY_ID, isEmpty);
        outputValues.put(OUTPUT_ITEM_ID_ID, itemId);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isUseOffHand() {
        return useOffHand;
    }
    
    public void setUseOffHand(boolean useOffHand) {
        this.useOffHand = useOffHand;
        markDirty();
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        return useOffHand;
    }
    
    /**
     * 恢复节点状态
     */
    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof Boolean) {
            useOffHand = (Boolean) state;
        }
    }
} 