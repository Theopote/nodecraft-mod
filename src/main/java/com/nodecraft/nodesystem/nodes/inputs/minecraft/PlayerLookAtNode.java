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
 * 获取玩家视线方向和瞄准信息的节点
 */
@NodeInfo(
    id = "inputs.minecraft.player_look_at",
    displayName = "玩家视线",
    description = "获取玩家视线方向和瞄准的位置信息",
    category = "inputs.minecraft"
)
public class PlayerLookAtNode extends BaseNode {
    
    // --- 节点属性 ---
    private float maxDistance = 100.0f; // 最大射线距离
    private boolean includeEntities = true; // 是否包含实体
    private boolean includeFluids = false; // 是否包含流体方块
    private String description = "Gets the block or entity the player is looking at."; // 节点描述
    
    // --- 输出端口 ---
    private static final String OUTPUT_HIT_POSITION_ID = "output_hit_position";
    private static final String OUTPUT_HIT_BLOCK_ID = "output_hit_block";
    private static final String OUTPUT_HIT_ENTITY_ID = "output_hit_entity";
    private static final String OUTPUT_HIT_DISTANCE_ID = "output_hit_distance";
    private static final String OUTPUT_HAS_HIT_ID = "output_has_hit";
    
    /**
     * 构造一个新的玩家准星指向节点
     */
    public PlayerLookAtNode() {
        // 使用新的分类命名 - inputs.minecraft.player_look_at
        super(UUID.randomUUID(), "inputs.minecraft.player_look_at");
        
        // 创建并添加输出端口
        IPort hitPositionOutput = new BasePort(OUTPUT_HIT_POSITION_ID, "Hit Position", 
                "The position of the raycast hit", NodeDataType.VECTOR, this);
        addOutputPort(hitPositionOutput);
        
        IPort hitBlockOutput = new BasePort(OUTPUT_HIT_BLOCK_ID, "Hit Block", 
                "The block that was hit", NodeDataType.BLOCK_INFO, this);
        addOutputPort(hitBlockOutput);
        
        IPort hitEntityOutput = new BasePort(OUTPUT_HIT_ENTITY_ID, "Hit Entity", 
                "The entity that was hit", NodeDataType.ENTITY_INFO, this);
        addOutputPort(hitEntityOutput);
        
        IPort hitDistanceOutput = new BasePort(OUTPUT_HIT_DISTANCE_ID, "Hit Distance", 
                "Distance to the hit", NodeDataType.FLOAT, this);
        addOutputPort(hitDistanceOutput);
        
        IPort hasHitOutput = new BasePort(OUTPUT_HAS_HIT_ID, "Has Hit", 
                "Whether something was hit", NodeDataType.BOOLEAN, this);
        addOutputPort(hasHitOutput);
        
        // 设置默认输出值
        resetOutputs();
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
            resetOutputs();
            return;
        }
        
        // 获取玩家准星指向
        PlayerAccessor playerAccessor = context.getPlayerAccessor();
        if (playerAccessor == null) {
            resetOutputs();
            return;
        }
        
        // 执行射线检测
        performRaycast(playerAccessor);
    }
    
    /**
     * 执行射线检测
     */
    private void performRaycast(PlayerAccessor playerAccessor) {
        // 获取玩家眼睛位置和视线方向
        Vector3 eyePosition = playerAccessor.getPlayerEyePosition();
        Vector3 lookVector = playerAccessor.getPlayerLookVector();
        
        // 在实际实现中，这里应该调用Minecraft的射线检测逻辑
        // 例如: RaycastResult result = world.raycast(eyePosition, lookVector, maxDistance, includeEntities, includeFluids);
        
        // 为演示目的，这里假设我们有一个射线检测结果
        // 在实际使用时，需要根据Minecraft API实现具体射线检测逻辑
        boolean hasHit = true; // 假设击中了某物
        Vector3 hitPosition = eyePosition.add(lookVector.scale(10.0f)); // 假设击中点
        Object hitBlock = "minecraft:stone"; // 假设击中的方块
        Object hitEntity = null; // 假设没有击中实体
        float hitDistance = 10.0f; // 假设击中距离
        
        // 更新输出值
        outputValues.put(OUTPUT_HAS_HIT_ID, hasHit);
        outputValues.put(OUTPUT_HIT_POSITION_ID, hitPosition);
        outputValues.put(OUTPUT_HIT_BLOCK_ID, hitBlock);
        outputValues.put(OUTPUT_HIT_ENTITY_ID, hitEntity);
        outputValues.put(OUTPUT_HIT_DISTANCE_ID, hitDistance);
    }
    
    /**
     * 重置输出端口的值为默认值
     */
    private void resetOutputs() {
        outputValues.put(OUTPUT_HAS_HIT_ID, false);
        outputValues.put(OUTPUT_HIT_POSITION_ID, new Vector3(0, 0, 0));
        outputValues.put(OUTPUT_HIT_BLOCK_ID, null);
        outputValues.put(OUTPUT_HIT_ENTITY_ID, null);
        outputValues.put(OUTPUT_HIT_DISTANCE_ID, 0.0f);
    }
    
    // --- Getters/Setters for Properties ---
    
    public float getMaxDistance() {
        return maxDistance;
    }
    
    public void setMaxDistance(float maxDistance) {
        if (maxDistance < 0) {
            maxDistance = 0;
        } else if (maxDistance > 1000) {
            maxDistance = 1000; // 设置合理上限
        }
        
        if (this.maxDistance != maxDistance) {
            this.maxDistance = maxDistance;
            markDirty();
        }
    }
    
    public boolean isIncludeEntities() {
        return includeEntities;
    }
    
    public void setIncludeEntities(boolean includeEntities) {
        if (this.includeEntities != includeEntities) {
            this.includeEntities = includeEntities;
            markDirty();
        }
    }
    
    public boolean isIncludeFluids() {
        return includeFluids;
    }
    
    public void setIncludeFluids(boolean includeFluids) {
        if (this.includeFluids != includeFluids) {
            this.includeFluids = includeFluids;
            markDirty();
        }
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("maxDistance", getMaxDistance());
        state.put("includeEntities", isIncludeEntities());
        state.put("includeFluids", isIncludeFluids());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("maxDistance")) {
                Object maxDist = stateMap.get("maxDistance");
                if (maxDist instanceof Number) {
                    setMaxDistance(((Number) maxDist).floatValue());
                }
            }
            
            if (stateMap.containsKey("includeEntities")) {
                Object includeEnt = stateMap.get("includeEntities");
                if (includeEnt instanceof Boolean) {
                    setIncludeEntities((Boolean) includeEnt);
                }
            }
            
            if (stateMap.containsKey("includeFluids")) {
                Object includeFlu = stateMap.get("includeFluids");
                if (includeFlu instanceof Boolean) {
                    setIncludeFluids((Boolean) includeFlu);
                }
            }
        }
    }
} 