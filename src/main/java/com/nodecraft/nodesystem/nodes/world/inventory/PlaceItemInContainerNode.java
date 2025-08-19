package com.nodecraft.nodesystem.nodes.world.inventory;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Place Item in Container 节点: 将物品放入容器。
 */
@NodeInfo(
    id = "world.inventory.place_item_in_container",
    displayName = "放置物品到容器",
    description = "将物品放入容器",
    category = "world.inventory"
)
public class PlaceItemInContainerNode extends BaseNode {

    // --- 节点属性 ---
    private boolean playSound = true; // 是否播放放置声音
    private boolean mergeItems = true; // 是否合并相同物品
    private String description = "将 ItemStack 放入指定 Coordinate 的容器";

    // --- 输入端口 IDs ---
    private static final String INPUT_COORDINATE_ID = "input_coordinate";
    private static final String INPUT_ITEM_ID = "input_item";
    private static final String INPUT_SLOT_ID = "input_slot";
    private static final String INPUT_PLAY_SOUND_ID = "input_play_sound";
    private static final String INPUT_MERGE_ITEMS_ID = "input_merge_items";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_ITEM_LEFT_ID = "output_item_left";
    private static final String OUTPUT_AMOUNT_PLACED_ID = "output_amount_placed";
    private static final String OUTPUT_CONTAINER_TYPE_ID = "output_container_type";

    // --- 构造函数 ---
    public PlaceItemInContainerNode() {
        super(UUID.randomUUID(), "world.inventory.place_item_in_container");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_COORDINATE_ID, "Coordinate", 
                "目标容器坐标", NodeDataType.COORDINATE, this));
        addInputPort(new BasePort(INPUT_ITEM_ID, "Item", 
                "要放入的物品", NodeDataType.ITEM_STACK, this));
        addInputPort(new BasePort(INPUT_SLOT_ID, "Slot", 
                "目标槽位（-1表示自动寻找合适槽位）", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_PLAY_SOUND_ID, "Play Sound", 
                "是否播放放置声音", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_MERGE_ITEMS_ID, "Merge Items", 
                "是否合并相同物品", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功放置物品", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_ITEM_LEFT_ID, "Item Left", 
                "无法放入的剩余物品", NodeDataType.ITEM_STACK, this));
        addOutputPort(new BasePort(OUTPUT_AMOUNT_PLACED_ID, "Amount Placed", 
                "成功放入的物品数量", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_CONTAINER_TYPE_ID, "Container Type", 
                "容器类型（如chest, furnace, hopper等）", NodeDataType.STRING, this));
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 默认输出值
        boolean success = false;
        Object itemLeft = null;
        int amountPlaced = 0;
        String containerType = "";
        
        // 获取输入值
        Object coordinateObj = inputValues.get(INPUT_COORDINATE_ID);
        Object itemObj = inputValues.get(INPUT_ITEM_ID);
        Object slotObj = inputValues.get(INPUT_SLOT_ID);
        Object playSoundObj = inputValues.get(INPUT_PLAY_SOUND_ID);
        Object mergeItemsObj = inputValues.get(INPUT_MERGE_ITEMS_ID);
        
        // 确定槽位
        int slot = -1; // 默认为自动寻找合适槽位
        if (slotObj instanceof Number) {
            slot = ((Number) slotObj).intValue();
        }
        
        // 确定是否播放声音
        boolean playSound = this.playSound;
        if (playSoundObj instanceof Boolean) {
            playSound = (Boolean) playSoundObj;
        }
        
        // 确定是否合并物品
        boolean mergeItems = this.mergeItems;
        if (mergeItemsObj instanceof Boolean) {
            mergeItems = (Boolean) mergeItemsObj;
        }
        
        // 检查必要的输入是否存在
        if (coordinateObj != null && itemObj != null) {
            try {
                /*
                在实际实现中，需要根据Minecraft API实现以下操作:
                1. 获取指定坐标的容器方块实体
                2. 尝试将物品放入容器
                3. 处理无法放入的物品
                4. 可选播放放置声音
                
                if (coordinateObj instanceof BlockPos && itemObj instanceof ItemStack) {
                    BlockPos pos = (BlockPos) coordinateObj;
                    ItemStack originalStack = (ItemStack) itemObj;
                    
                    // 检查方块实体是否存在且是容器
                    BlockEntity blockEntity = world.getBlockEntity(pos);
                    if (blockEntity instanceof Inventory) {
                        Inventory inventory = (Inventory) blockEntity;
                        containerType = blockEntity.getType().toString();
                        
                        // 复制一份物品，防止修改原始物品
                        ItemStack stack = originalStack.copy();
                        int originalCount = stack.getCount();
                        
                        if (stack.isEmpty()) {
                            // 空物品堆，直接算成功
                            success = true;
                            amountPlaced = 0;
                        } else {
                            // 处理指定槽位的情况
                            if (slot >= 0 && slot < inventory.size()) {
                                // 放入指定槽位
                                ItemStack slotStack = inventory.getStack(slot);
                                
                                if (slotStack.isEmpty()) {
                                    // 槽位为空，直接放入
                                    inventory.setStack(slot, stack.copy());
                                    amountPlaced = stack.getCount();
                                    stack.setCount(0);
                                    success = true;
                                } else if (mergeItems && ItemStack.canCombine(slotStack, stack)) {
                                    // 可合并物品
                                    int maxStackSize = Math.min(inventory.getMaxCountPerStack(), slotStack.getMaxCount());
                                    int canAdd = Math.max(0, maxStackSize - slotStack.getCount());
                                    
                                    if (canAdd > 0) {
                                        int toAdd = Math.min(canAdd, stack.getCount());
                                        slotStack.increment(toAdd);
                                        stack.decrement(toAdd);
                                        amountPlaced = toAdd;
                                        success = true;
                                    }
                                }
                            } else {
                                // 自动寻找合适槽位
                                if (mergeItems) {
                                    // 先尝试合并到相同物品的槽位
                                    for (int i = 0; i < inventory.size() && !stack.isEmpty(); i++) {
                                        ItemStack slotStack = inventory.getStack(i);
                                        
                                        if (!slotStack.isEmpty() && ItemStack.canCombine(slotStack, stack)) {
                                            int maxStackSize = Math.min(inventory.getMaxCountPerStack(), slotStack.getMaxCount());
                                            int canAdd = Math.max(0, maxStackSize - slotStack.getCount());
                                            
                                            if (canAdd > 0) {
                                                int toAdd = Math.min(canAdd, stack.getCount());
                                                slotStack.increment(toAdd);
                                                stack.decrement(toAdd);
                                                amountPlaced += toAdd;
                                                success = true;
                                            }
                                        }
                                    }
                                }
                                
                                // 再尝试放入空槽位
                                for (int i = 0; i < inventory.size() && !stack.isEmpty(); i++) {
                                    ItemStack slotStack = inventory.getStack(i);
                                    
                                    if (slotStack.isEmpty()) {
                                        int toAdd = stack.getCount();
                                        inventory.setStack(i, stack.copy());
                                        stack.setCount(0);
                                        amountPlaced += toAdd;
                                        success = true;
                                        break;
                                    }
                                }
                            }
                            
                            // 处理剩余物品
                            if (!stack.isEmpty()) {
                                itemLeft = stack;
                            }
                            
                            // 播放放置声音
                            if (playSound && amountPlaced > 0) {
                                world.playSound(null, pos, SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 
                                        0.5f, world.random.nextFloat() * 0.1f + 0.9f);
                            }
                            
                            // 标记方块实体为已更改
                            if (success) {
                                blockEntity.markDirty();
                            }
                        }
                    }
                }
                */
                
                // 模拟成功放置物品 (在实际实现中替换为上面的逻辑)
                success = true;
                amountPlaced = 1;
                containerType = "minecraft:chest";
            } catch (Exception e) {
                success = false;
                System.err.println("Error placing item in container: " + e.getMessage());
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_ITEM_LEFT_ID, itemLeft);
        outputValues.put(OUTPUT_AMOUNT_PLACED_ID, amountPlaced);
        outputValues.put(OUTPUT_CONTAINER_TYPE_ID, containerType);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isPlaySound() {
        return playSound;
    }
    
    public void setPlaySound(boolean playSound) {
        this.playSound = playSound;
        markDirty();
    }
    
    public boolean isMergeItems() {
        return mergeItems;
    }
    
    public void setMergeItems(boolean mergeItems) {
        this.mergeItems = mergeItems;
        markDirty();
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        // 将两个属性合并为一个对象保存
        boolean[] state = new boolean[2];
        state[0] = playSound;
        state[1] = mergeItems;
        return state;
    }
    
    /**
     * 恢复节点状态
     */
    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof boolean[]) {
            boolean[] boolState = (boolean[]) state;
            if (boolState.length >= 2) {
                playSound = boolState[0];
                mergeItems = boolState[1];
            }
        }
    }
} 