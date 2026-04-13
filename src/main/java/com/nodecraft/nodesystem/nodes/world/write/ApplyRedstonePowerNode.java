package com.nodecraft.nodesystem.nodes.world.write;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Apply Redstone Power 节点: 应用红石信号。
 */
@NodeInfo(
    id = "world.write.apply_redstone_power",
    displayName = "应用红石信号",
    description = "应用红石信号",
    category = "world.write"
)
public class ApplyRedstonePowerNode extends BaseNode {

    // --- 节点属性 ---
    private int powerLevel = 15; // 默认最大红石能量
    private int duration = 1; // 持续时间（刻）
    private String description = "在 Coordinate 模拟施加红石信号";

    // --- 输入端口 IDs ---
    private static final String INPUT_COORDINATE_ID = "input_coordinate";
    private static final String INPUT_POWER_LEVEL_ID = "input_power_level";
    private static final String INPUT_DURATION_ID = "input_duration"; 
    private static final String INPUT_PLAY_SOUND_ID = "input_play_sound";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_BLOCK_TYPE_ID = "output_block_type";

    // --- 构造函数 ---
    public ApplyRedstonePowerNode() {
        super(UUID.randomUUID(), "world.write.apply_redstone_power");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_COORDINATE_ID, "Coordinate", 
                "目标坐标", NodeDataType.COORDINATE, this));
        addInputPort(new BasePort(INPUT_POWER_LEVEL_ID, "Power Level", 
                "红石信号强度 (1-15)", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_DURATION_ID, "Duration", 
                "信号持续时间（刻）", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_PLAY_SOUND_ID, "Play Sound", 
                "是否播放红石声音", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功应用红石信号", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_BLOCK_TYPE_ID, "Block Type", 
                "目标方块类型", NodeDataType.STRING, this));
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
        
        // 获取输入值
        Object coordinateObj = inputValues.get(INPUT_COORDINATE_ID);
        Object powerLevelObj = inputValues.get(INPUT_POWER_LEVEL_ID);
        Object durationObj = inputValues.get(INPUT_DURATION_ID);
        Object playSoundObj = inputValues.get(INPUT_PLAY_SOUND_ID);
        
        // 确定红石能量级别
        int powerLevel = this.powerLevel;
        if (powerLevelObj instanceof Number) {
            powerLevel = Math.max(0, Math.min(15, ((Number) powerLevelObj).intValue()));
        }
        
        // 确定持续时间
        int duration = this.duration;
        if (durationObj instanceof Number) {
            duration = Math.max(1, ((Number) durationObj).intValue());
        }
        
        // 确定是否播放声音
        boolean playSound = true;
        if (playSoundObj instanceof Boolean) {
            playSound = (Boolean) playSoundObj;
        }
        
        // 检查必要的输入是否存在
        if (coordinateObj != null) {
            try {
                /*
                在实际实现中，需要根据Minecraft API实现以下操作:
                1. 获取指定坐标的方块
                2. 应用红石能量
                3. 设置一个任务，在指定的时间后取消红石能量
                
                if (coordinateObj instanceof BlockPos) {
                    BlockPos pos = (BlockPos) coordinateObj;
                    ServerWorld world = server.getWorld(World.OVERWORLD); // 或从上下文中获取
                    
                    // 获取方块状态
                    BlockState blockState = world.getBlockState(pos);
                    blockType = Registry.BLOCK.getId(blockState.getBlock()).toString();
                    
                    // 保存原始方块状态
                    BlockState originalState = blockState;
                    
                    // 创建临时红石能量源
                    // 注意：这需要根据具体实现调整，例如可能需要使用特定的能量提供器方块
                    // 或者可以通过特殊的游戏机制临时加入红石能量
                    
                    // 例如，可以临时放置一个红石火把或红石块
                    BlockPos powerSourcePos = pos.offset(Direction.UP);
                    BlockState powerSource = Blocks.REDSTONE_BLOCK.getDefaultState();
                    world.setBlockState(powerSourcePos, powerSource);
                    
                    // 播放红石相关声音
                    if (playSound) {
                        world.playSound(
                                null, 
                                pos, 
                                SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, 
                                SoundCategory.BLOCKS, 
                                0.5f, 
                                2.6f + (world.random.nextFloat() - world.random.nextFloat()) * 0.8f
                        );
                    }
                    
                    // 设置定时任务，在指定时间后移除红石能量
                    world.getServer().getWorldScheduler().schedule(new SingleTask(world, () -> {
                        world.setBlockState(powerSourcePos, Blocks.AIR.getDefaultState());
                    }, duration));
                    
                    success = true;
                }
                */
                
                // 模拟应用红石能量 (在实际实现中替换为上面的逻辑)
                success = true;
                blockType = "minecraft:stone";
                
                // 打印调试信息
                System.out.println("模拟在坐标 " + coordinateObj + " 应用红石能量 " + powerLevel + "，持续 " + duration + " 刻");
            } catch (Exception e) {
                success = false;
                System.err.println("Error applying redstone power: " + e.getMessage());
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_BLOCK_TYPE_ID, blockType);
    }
    
    // --- Getters/Setters for Properties ---
    
    public int getPowerLevel() {
        return powerLevel;
    }
    
    public void setPowerLevel(int powerLevel) {
        this.powerLevel = Math.max(0, Math.min(15, powerLevel));
        markDirty();
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = Math.max(1, duration);
        markDirty();
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        int[] state = new int[2];
        state[0] = powerLevel;
        state[1] = duration;
        return state;
    }
    
    /**
     * 恢复节点状态
     */
    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof int[]) {
            int[] intState = (int[]) state;
            if (intState.length >= 2) {
                powerLevel = intState[0];
                duration = intState[1];
            }
        }
    }
} 
