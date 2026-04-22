package com.nodecraft.nodesystem.nodes.world.query;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Get Entity 节点: 根据实体ID或选择器获取实体信息。
 */
@NodeInfo(
    id = "world.query.get_entity",
    displayName = "Get Entity",
    description = "根据实体ID或选择器获取实体信息",
    category = "world.query"
)
public class GetEntityNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_UUID_ID = "input_uuid";
    private static final String INPUT_ENTITY_TYPE_ID = "input_entity_type";
    private static final String INPUT_FIND_NEAREST_ID = "input_find_nearest";
    private static final String INPUT_MAX_DISTANCE_ID = "input_max_distance";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_ENTITY_ID = "output_entity";
    private static final String OUTPUT_ENTITY_TYPE_ID = "output_entity_type";
    private static final String OUTPUT_ENTITY_POS_ID = "output_entity_pos";
    private static final String OUTPUT_FOUND_ID = "output_found";
    private static final String OUTPUT_DISTANCE_ID = "output_distance";
    
    // 节点描述
    private final String description = "通过UUID或类型获取实体信息";

    // --- 构造函数 ---
    public GetEntityNode() {
        super(UUID.randomUUID(), "world.query.get_entity");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_UUID_ID, "UUID", 
                "实体的UUID", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_ENTITY_TYPE_ID, "Entity Type", 
                "实体类型（如果没有指定UUID）", NodeDataType.ENTITY_TYPE, this));
        addInputPort(new BasePort(INPUT_FIND_NEAREST_ID, "Find Nearest", 
                "查找最近的实体（按类型时）", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_MAX_DISTANCE_ID, "Max Distance", 
                "查找的最大距离", NodeDataType.DOUBLE, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_ENTITY_ID, "Entity", 
                "实体对象", NodeDataType.MINECRAFT_ENTITY, this));
        addOutputPort(new BasePort(OUTPUT_ENTITY_TYPE_ID, "Entity Type", 
                "实体类型", NodeDataType.ENTITY_TYPE, this));
        addOutputPort(new BasePort(OUTPUT_ENTITY_POS_ID, "Entity Position", 
                "实体位置", NodeDataType.POSITION, this));
        addOutputPort(new BasePort(OUTPUT_FOUND_ID, "Found", 
                "是否找到实体", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_DISTANCE_ID, "Distance", 
                "到实体的距离", NodeDataType.DOUBLE, this));
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
     * 实现INode接口的getDisplayName方法
     * @return 节点显示名称
     */
    @Override
    public String getDisplayName() {
        return "Get Entity";
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 默认输出值
        Object entityObj = null;
        String entityType = "";
        Object entityPos = null;
        boolean found = false;
        double distance = Double.MAX_VALUE;
        
        // 获取输入值
        Object uuidObj = inputValues.get(INPUT_UUID_ID);
        Object entityTypeObj = inputValues.get(INPUT_ENTITY_TYPE_ID);
        
        // 获取额外输入参数
        boolean findNearest = false;
        Object findNearestObj = inputValues.get(INPUT_FIND_NEAREST_ID);
        if (findNearestObj instanceof Boolean) {
            findNearest = (Boolean) findNearestObj;
        }
        
        // 设置默认最大距离为100个方块
        double maxDistance = 100.0;
        Object maxDistanceObj = inputValues.get(INPUT_MAX_DISTANCE_ID);
        if (maxDistanceObj instanceof Number) {
            maxDistance = ((Number) maxDistanceObj).doubleValue();
            if (maxDistance <= 0) {
                maxDistance = Double.MAX_VALUE; // 无限制
            }
        }
        
        // 检查执行上下文是否有效
        if (context != null && context.getWorld() != null) {
            try {
                if (uuidObj instanceof String) {
                    // 尝试解析UUID
                    try {
                        UUID uuid = UUID.fromString((String) uuidObj);
                        
                        // 通过UUID查找实体（在实际实现中使用Minecraft API）
                        // entityObj = context.getWorld().getEntity(uuid);
                        
                        // 模拟实现
                        if (entityObj != null) {
                            found = true;
                            // 获取实体类型
                            entityType = entityObj.getClass().getSimpleName();
                            
                            // 获取实体位置（实际实现中使用entity.getPos()或类似方法）
                            // entityPos = new Vector3d(entity.getX(), entity.getY(), entity.getZ());
                            
                            // 计算距离（如果有玩家位置）
                            if (context.getPlayer() != null) {
                                // 在实际实现中计算玩家到实体的距离
                                // distance = context.getPlayer().getPos().distanceTo(entity.getPos());
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        // UUID格式无效，记录错误
                        System.err.println("Invalid UUID format: " + uuidObj);
                    }
                } 
                // 如果没有找到实体通过UUID或没有提供UUID，尝试按类型查找
                else if (!found && entityTypeObj instanceof String) {
                    String type = (String) entityTypeObj;
                    
                    // 在实际实现中通过类型查找实体（可能找到多个）
                    // List<Entity> entities = context.getWorld().getEntitiesByType(...);
                    
                    // 如果找到多个实体
                    if (findNearest && context.getPlayer() != null) {
                        // 查找最近的实体
                        // 代码应根据实际Minecraft API实现
                    } else {
                        // 返回第一个找到的实体
                        // 代码应根据实际Minecraft API实现
                    }
                    
                    // 如果找到实体，设置输出值
                    if (entityObj != null) {
                        found = true;
                        entityType = type;
                        // 获取实体位置和计算距离
                        // 代码应根据实际Minecraft API实现
                    }
                }
            } catch (Exception e) {
                // 记录错误
                System.err.println("Error finding entity: " + e.getMessage());
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_ENTITY_ID, entityObj);
        outputValues.put(OUTPUT_ENTITY_TYPE_ID, entityType);
        outputValues.put(OUTPUT_ENTITY_POS_ID, entityPos);
        outputValues.put(OUTPUT_FOUND_ID, found);
        outputValues.put(OUTPUT_DISTANCE_ID, found ? distance : -1.0);
    }
} 