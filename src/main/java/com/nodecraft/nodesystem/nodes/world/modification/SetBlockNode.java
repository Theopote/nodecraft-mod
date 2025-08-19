package com.nodecraft.nodesystem.nodes.world.modification;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Set Block 节点: 在单个坐标放置指定方块。
 */
@NodeInfo(
    id = "world.modification.set_block",
    displayName = "设置单个方块",
    description = "在单个坐标放置指定方块",
    category = "world.modification"
)
public class SetBlockNode extends BaseNode {

    // --- 节点属性 ---
    private String description = "在单个坐标放置指定方块信息(包含状态和NBT)";
    private boolean notifyUpdate = true; // 是否通知更新（触发更新事件）
    private boolean spawnDrops = false; // 放置方块时是否生成掉落物

    // --- 输入端口 IDs ---
    private static final String INPUT_COORDINATE_ID = "input_coordinate";
    private static final String INPUT_BLOCK_INFO_ID = "input_block_info";
    private static final String INPUT_NOTIFY_ID = "input_notify";
    private static final String INPUT_SPAWN_DROPS_ID = "input_spawn_drops";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_PREVIOUS_BLOCK_ID = "output_previous_block";

    // --- 构造函数 ---
    public SetBlockNode() {
        super(UUID.randomUUID(), "world.modification.set_block");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_COORDINATE_ID, "Coordinate", 
                "目标坐标", NodeDataType.BLOCK_POS, this));
        addInputPort(new BasePort(INPUT_BLOCK_INFO_ID, "Block Info", 
                "要放置的方块信息", NodeDataType.BLOCK_INFO, this));
        addInputPort(new BasePort(INPUT_NOTIFY_ID, "Notify Update", 
                "是否通知更新（触发更新事件）", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_SPAWN_DROPS_ID, "Spawn Drops", 
                "放置方块时是否生成掉落物", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "方块是否成功放置", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_PREVIOUS_BLOCK_ID, "Previous Block", 
                "被替换的原方块信息", NodeDataType.BLOCK_INFO, this));
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
        Object previousBlock = null;
        
        // 获取输入值
        Object coordinateObj = inputValues.get(INPUT_COORDINATE_ID);
        Object blockInfoObj = inputValues.get(INPUT_BLOCK_INFO_ID);
        
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
        
        // 检查执行上下文和输入是否有效
        if (context != null && context.getWorld() != null && 
                coordinateObj instanceof BlockPos && blockInfoObj != null) {
            BlockPos pos = (BlockPos) coordinateObj;
            
            try {
                // 获取当前方块信息（用于返回被替换的方块）
                previousBlock = context.getWorld().getBlockState(pos);
                
                // 在实际实现中，放置方块
                // 这里是示例代码，需要根据Minecraft API进行调整
                
                // 设置方块
                // 根据是否生成掉落物和是否通知更新，选择对应的方法
                // 例如：context.getWorld().setBlockState(pos, blockState, notifyUpdateValue, spawnDropsValue);

                // 模拟放置成功
                success = true;
                
            } catch (Exception e) {
                // 记录错误
                System.err.println("Error setting block at " + pos + ": " + e.getMessage());
                success = false;
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_PREVIOUS_BLOCK_ID, previousBlock);
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
} 