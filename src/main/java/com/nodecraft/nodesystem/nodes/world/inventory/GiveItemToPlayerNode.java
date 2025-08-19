package com.nodecraft.nodesystem.nodes.world.inventory;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Give Item to Player 节点: 给玩家物品。
 */
@NodeInfo(
    id = "world.inventory.give_item_to_player",
    displayName = "给玩家物品",
    description = "给玩家物品",
    category = "world.inventory"
)
public class GiveItemToPlayerNode extends BaseNode {

    // --- 节点属性 ---
    private boolean playSound = true; // 是否播放拾取声音
    private String description = "将 ItemStack 给予玩家";

    // --- 输入端口 IDs ---
    private static final String INPUT_PLAYER_ID = "input_player";
    private static final String INPUT_ITEM_ID = "input_item";
    private static final String INPUT_PLAY_SOUND_ID = "input_play_sound";
    private static final String INPUT_FORCE_GIVE_ID = "input_force_give";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_ITEM_LEFT_ID = "output_item_left";
    private static final String OUTPUT_AMOUNT_GIVEN_ID = "output_amount_given";

    // --- 构造函数 ---
    public GiveItemToPlayerNode() {
        super(UUID.randomUUID(), "world.inventory.give_item_to_player");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_PLAYER_ID, "Player", 
                "目标玩家", NodeDataType.PLAYER, this));
        addInputPort(new BasePort(INPUT_ITEM_ID, "Item", 
                "要给予的物品", NodeDataType.ITEM_STACK, this));
        addInputPort(new BasePort(INPUT_PLAY_SOUND_ID, "Play Sound", 
                "是否播放拾取声音", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_FORCE_GIVE_ID, "Force Give", 
                "是否强制给予（如果库存满则丢弃在地上）", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功给予物品", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_ITEM_LEFT_ID, "Item Left", 
                "无法放入的剩余物品", NodeDataType.ITEM_STACK, this));
        addOutputPort(new BasePort(OUTPUT_AMOUNT_GIVEN_ID, "Amount Given", 
                "成功给予的物品数量", NodeDataType.INTEGER, this));
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
        int amountGiven = 0;
        
        // 获取输入值
        Object playerObj = inputValues.get(INPUT_PLAYER_ID);
        Object itemObj = inputValues.get(INPUT_ITEM_ID);
        Object playSoundObj = inputValues.get(INPUT_PLAY_SOUND_ID);
        Object forceGiveObj = inputValues.get(INPUT_FORCE_GIVE_ID);
        
        // 确定是否播放声音
        boolean playSound = this.playSound;
        if (playSoundObj instanceof Boolean) {
            playSound = (Boolean) playSoundObj;
        }
        
        // 确定是否强制给予
        boolean forceGive = false;
        if (forceGiveObj instanceof Boolean) {
            forceGive = (Boolean) forceGiveObj;
        }
        
        // 检查必要的输入是否存在
        if (playerObj != null && itemObj != null) {
            try {
                /*
                在实际实现中，需要根据Minecraft API实现以下操作:
                1. 尝试将物品添加到玩家库存
                2. 处理无法放入的物品
                3. 可选播放拾取声音
                
                if (playerObj instanceof PlayerEntity && itemObj instanceof ItemStack) {
                    PlayerEntity player = (PlayerEntity) playerObj;
                    ItemStack originalStack = (ItemStack) itemObj;
                    
                    // 复制一份物品，防止修改原始物品
                    ItemStack stack = originalStack.copy();
                    
                    if (stack.isEmpty()) {
                        // 空物品堆，直接算成功
                        success = true;
                        amountGiven = 0;
                    } else {
                        int originalCount = stack.getCount();
                        
                        // 尝试将物品添加到玩家库存
                        boolean couldMergeCompletely = player.getInventory().insertStack(stack);
                        
                        if (couldMergeCompletely) {
                            // 全部成功放入库存
                            amountGiven = originalCount;
                            success = true;
                        } else {
                            // 部分成功或全部失败
                            amountGiven = originalCount - stack.getCount();
                            
                            if (forceGive && !stack.isEmpty()) {
                                // 如果强制给予，则将剩余物品丢在地上
                                player.dropItem(stack.copy(), false);
                                stack.setCount(0);
                                amountGiven = originalCount;
                                success = true;
                            } else {
                                // 返回剩余物品
                                itemLeft = stack;
                                success = (amountGiven > 0);
                            }
                        }
                        
                        // 播放拾取声音
                        if (playSound && amountGiven > 0) {
                            player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 0.2f, 
                                    ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
                        }
                    }
                }
                */
                
                // 模拟成功给予物品 (在实际实现中替换为上面的逻辑)
                success = true;
                amountGiven = 1;
            } catch (Exception e) {
                success = false;
                System.err.println("Error giving item to player: " + e.getMessage());
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_ITEM_LEFT_ID, itemLeft);
        outputValues.put(OUTPUT_AMOUNT_GIVEN_ID, amountGiven);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isPlaySound() {
        return playSound;
    }
    
    public void setPlaySound(boolean playSound) {
        this.playSound = playSound;
        markDirty();
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        return playSound;
    }
    
    /**
     * 恢复节点状态
     */
    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof Boolean) {
            playSound = (Boolean) state;
        }
    }
} 