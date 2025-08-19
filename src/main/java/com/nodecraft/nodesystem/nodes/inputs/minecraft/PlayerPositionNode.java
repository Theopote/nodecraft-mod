package com.nodecraft.nodesystem.nodes.inputs.minecraft;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.util.Vector3;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.minecraft.PlayerAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 获取玩家当前位置的节点
 */
@NodeInfo(
    id = "inputs.minecraft.player_position",
    displayName = "玩家位置",
    description = "获取玩家当前的位置坐标",
    category = "inputs.minecraft"
)
public class PlayerPositionNode extends BaseNode {
    
    // --- 节点属性 ---
    private boolean useEyePosition = false; // 是否使用眼睛位置而非脚底位置
    private String description = "Gets the current player's position in the world."; // 节点描述
    
    // --- 输出端口 ---
    private static final String OUTPUT_POSITION_ID = "output_position";
    private static final String OUTPUT_X_ID = "output_x";
    private static final String OUTPUT_Y_ID = "output_y";
    private static final String OUTPUT_Z_ID = "output_z";
    
    /**
     * 构造一个新的玩家位置节点
     */
    public PlayerPositionNode() {
        // 使用新的分类命名 - inputs.minecraft.player_position
        super(UUID.randomUUID(), "inputs.minecraft.player_position");
        
        // 创建并添加输出端口
        IPort positionOutput = new BasePort(OUTPUT_POSITION_ID, "Position", "The player's position vector", NodeDataType.VECTOR, this);
        addOutputPort(positionOutput);
        
        IPort xOutput = new BasePort(OUTPUT_X_ID, "X", "X coordinate", NodeDataType.FLOAT, this);
        addOutputPort(xOutput);
        
        IPort yOutput = new BasePort(OUTPUT_Y_ID, "Y", "Y coordinate", NodeDataType.FLOAT, this);
        addOutputPort(yOutput);
        
        IPort zOutput = new BasePort(OUTPUT_Z_ID, "Z", "Z coordinate", NodeDataType.FLOAT, this);
        addOutputPort(zOutput);
    }
    
    /**
     * 实现INode接口的getDescription方法
     * @return 节点描述
     */
    @Override
    public String getDescription() {
        return this.description;
    }
    
    /**
     * 节点的计算逻辑
     * @param context 执行上下文
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        if (context == null) {
            // 如果没有执行上下文，将所有输出设为默认值
            Vector3 defaultPosition = new Vector3(0, 0, 0);
            updateOutputs(defaultPosition);
            return;
        }
        
        // 获取玩家位置
        Vector3 playerPosition = getPlayerPosition(context);
        updateOutputs(playerPosition);
    }
    
    /**
     * 获取玩家位置
     */
    private Vector3 getPlayerPosition(ExecutionContext context) {
        PlayerAccessor playerAccessor = context.getPlayerAccessor();
        
        if (playerAccessor == null) {
            return new Vector3(0, 0, 0); // 默认位置
        }
        
        // 根据设置获取眼睛位置或脚底位置
        if (useEyePosition) {
            return playerAccessor.getPlayerEyePosition();
        } else {
            return playerAccessor.getPlayerPosition();
        }
    }
    
    /**
     * 更新输出端口的值
     */
    private void updateOutputs(Vector3 position) {
        outputValues.put(OUTPUT_POSITION_ID, position);
        outputValues.put(OUTPUT_X_ID, position.getX());
        outputValues.put(OUTPUT_Y_ID, position.getY());
        outputValues.put(OUTPUT_Z_ID, position.getZ());
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isUseEyePosition() {
        return useEyePosition;
    }
    
    public void setUseEyePosition(boolean useEyePosition) {
        if (this.useEyePosition != useEyePosition) {
            this.useEyePosition = useEyePosition;
            // 标记节点需要重新计算
            markDirty();
        }
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        // 返回节点所有可序列化的状态
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("useEyePosition", isUseEyePosition());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("useEyePosition")) {
                Object useEyePos = stateMap.get("useEyePosition");
                if (useEyePos instanceof Boolean) {
                    setUseEyePosition((Boolean) useEyePos);
                }
            }
        }
    }
} 