package com.nodecraft.nodesystem.nodes.world.interaction;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Simulate Right Click 节点: 模拟右键点击。
 */
@NodeInfo(
    id = "world.interaction.simulate_right_click",
    displayName = "模拟右键点击",
    description = "模拟右键点击",
    category = "world.interaction"
)
public class SimulateRightClickNode extends BaseNode {

    // --- 节点属性 ---
    private boolean playSound = true; // 是否播放交互声音
    private String description = "模拟玩家在 Coordinate 右键点击";

    // --- 输入端口 IDs ---
    private static final String INPUT_COORDINATE_ID = "input_coordinate";
    private static final String INPUT_PLAYER_ID = "input_player";
    private static final String INPUT_ITEM_IN_HAND_ID = "input_item_in_hand";
    private static final String INPUT_PLAY_SOUND_ID = "input_play_sound";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_BLOCK_TYPE_ID = "output_block_type";
    private static final String OUTPUT_INTERACTION_RESULT_ID = "output_interaction_result";

    // --- 构造函数 ---
    public SimulateRightClickNode() {
        super(UUID.randomUUID(), "world.interaction.simulate_right_click");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_COORDINATE_ID, "Coordinate", 
                "右键点击的坐标", NodeDataType.COORDINATE, this));
        addInputPort(new BasePort(INPUT_PLAYER_ID, "Player", 
                "模拟点击的玩家", NodeDataType.PLAYER, this));
        addInputPort(new BasePort(INPUT_ITEM_IN_HAND_ID, "Item in Hand", 
                "模拟点击时玩家手持的物品（可选）", NodeDataType.ITEM_STACK, this));
        addInputPort(new BasePort(INPUT_PLAY_SOUND_ID, "Play Sound", 
                "是否播放交互声音", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "交互是否成功", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_BLOCK_TYPE_ID, "Block Type", 
                "交互的方块类型", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_INTERACTION_RESULT_ID, "Interaction Result", 
                "交互结果", NodeDataType.ANY, this));
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
        String blockType = "";
        Object interactionResult = null;
        
        // 获取输入值
        Object coordinateObj = inputValues.get(INPUT_COORDINATE_ID);
        Object playerObj = inputValues.get(INPUT_PLAYER_ID);
        Object itemInHandObj = inputValues.get(INPUT_ITEM_IN_HAND_ID);
        Object playSoundObj = inputValues.get(INPUT_PLAY_SOUND_ID);
        
        // 确定是否播放声音
        boolean playSound = this.playSound;
        if (playSoundObj instanceof Boolean) {
            playSound = (Boolean) playSoundObj;
        }
        
        // 检查必要的输入是否存在
        if (coordinateObj != null && playerObj != null) {
            try {
                /*
                在实际实现中，需要根据Minecraft API实现以下操作:
                1. 获取指定坐标的方块
                2. 确定使用的物品和玩家上下文
                3. 模拟右键点击行为
                4. 处理交互结果
                
                if (coordinateObj instanceof BlockPos && playerObj instanceof PlayerEntity) {
                    BlockPos pos = (BlockPos) coordinateObj;
                    PlayerEntity player = (PlayerEntity) playerObj;
                    World world = player.getWorld();
                    
                    // 获取方块状态
                    BlockState blockState = world.getBlockState(pos);
                    blockType = Registry.BLOCK.getId(blockState.getBlock()).toString();
                    
                    // 确定手持物品
                    ItemStack heldItem;
                    if (itemInHandObj instanceof ItemStack) {
                        heldItem = (ItemStack) itemInHandObj;
                    } else {
                        heldItem = player.getMainHandStack();
                    }
                    
                    // 计算命中的方向和位置
                    Direction direction = player.getHorizontalFacing();
                    float hitX = 0.5f;
                    float hitY = 0.5f;
                    float hitZ = 0.5f;
                    
                    // 创建命中结果
                    BlockHitResult hitResult = new BlockHitResult(
                            new Vec3d(pos.getX() + hitX, pos.getY() + hitY, pos.getZ() + hitZ),
                            direction,
                            pos,
                            false
                    );
                    
                    // 保存原始物品
                    ItemStack originalStack = player.getMainHandStack();
                    
                    // 临时替换玩家手持物品（如果需要）
                    if (itemInHandObj instanceof ItemStack) {
                        player.setStackInHand(Hand.MAIN_HAND, heldItem.copy());
                    }
                    
                    // 模拟交互
                    ActionResult result = world.getBlockState(pos)
                            .onUse(world, player, Hand.MAIN_HAND, hitResult);
                    
                    // 恢复原始物品
                    if (itemInHandObj instanceof ItemStack) {
                        player.setStackInHand(Hand.MAIN_HAND, originalStack);
                    }
                    
                    success = result.isAccepted();
                    interactionResult = result.toString();
                    
                    // 播放交互声音
                    if (playSound && success) {
                        world.playSound(
                                null, 
                                pos, 
                                blockState.getSoundGroup().getUseSound(), 
                                SoundCategory.BLOCKS, 
                                1.0f, 
                                1.0f
                        );
                    }
                }
                */
                
                // 模拟交互成功 (在实际实现中替换为上面的逻辑)
                success = true;
                blockType = "minecraft:chest";
                interactionResult = "SUCCESS";
            } catch (Exception e) {
                success = false;
                System.err.println("Error simulating right click: " + e.getMessage());
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_BLOCK_TYPE_ID, blockType);
        outputValues.put(OUTPUT_INTERACTION_RESULT_ID, interactionResult);
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