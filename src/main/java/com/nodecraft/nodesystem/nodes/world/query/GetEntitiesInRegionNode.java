package com.nodecraft.nodesystem.nodes.world.query;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.RegionData;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Get Entities in Region 节点: 获取区域内所有实体的列表。
 */
@NodeInfo(
    id = "world.query.get_entities_in_region",
    displayName = "获取区域内实体",
    description = "获取区域内所有实体的列表",
    category = "world.query"
)
public class GetEntitiesInRegionNode extends BaseNode {

    // --- 节点属性 ---
    private String description = "获取区域内所有实体的列表";
    private boolean excludePlayers = false; // 是否排除玩家
    private boolean includeItems = true; // 是否包括掉落物品

    // --- 输入端口 IDs ---
    private static final String INPUT_REGION_ID = "input_region";
    private static final String INPUT_ENTITY_TYPE_ID = "input_entity_type";
    private static final String INPUT_EXCLUDE_PLAYERS_ID = "input_exclude_players";
    private static final String INPUT_INCLUDE_ITEMS_ID = "input_include_items";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_ENTITIES_LIST_ID = "output_entities_list";
    private static final String OUTPUT_COUNT_ID = "output_count";
    private static final String OUTPUT_PLAYER_COUNT_ID = "output_player_count";
    private static final String OUTPUT_NEAREST_ENTITY_ID = "output_nearest_entity";

    // --- 构造函数 ---
    public GetEntitiesInRegionNode() {
        super(UUID.randomUUID(), "world.query.get_entities_in_region");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_REGION_ID, "Region", 
                "要查询的区域", NodeDataType.REGION, this));
        addInputPort(new BasePort(INPUT_ENTITY_TYPE_ID, "Entity Type", 
                "过滤的实体类型（可选）", NodeDataType.ENTITY_TYPE, this));
        addInputPort(new BasePort(INPUT_EXCLUDE_PLAYERS_ID, "Exclude Players", 
                "是否排除玩家", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_INCLUDE_ITEMS_ID, "Include Items", 
                "是否包括掉落物品", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_ENTITIES_LIST_ID, "Entities List", 
                "区域内的实体列表", NodeDataType.LIST, this));
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Count", 
                "实体总数", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_PLAYER_COUNT_ID, "Player Count", 
                "玩家数量", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_NEAREST_ENTITY_ID, "Nearest Entity", 
                "最近的实体", NodeDataType.MINECRAFT_ENTITY, this));
    }

    // --- 核心逻辑 ---
    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 默认输出值
        List<Object> entitiesList = new ArrayList<>();
        int count = 0;
        int playerCount = 0;
        Object nearestEntity = null;
        
        // 获取输入值
        Object regionObj = inputValues.get(INPUT_REGION_ID);
        Object entityTypeObj = inputValues.get(INPUT_ENTITY_TYPE_ID);
        
        // 获取布尔值参数
        boolean excludePlayersValue = this.excludePlayers;
        Object excludePlayersObj = inputValues.get(INPUT_EXCLUDE_PLAYERS_ID);
        if (excludePlayersObj instanceof Boolean) {
            excludePlayersValue = (Boolean) excludePlayersObj;
        }
        
        boolean includeItemsValue = this.includeItems;
        Object includeItemsObj = inputValues.get(INPUT_INCLUDE_ITEMS_ID);
        if (includeItemsObj instanceof Boolean) {
            includeItemsValue = (Boolean) includeItemsObj;
        }
        
        // 实体类型过滤器
        String entityTypeFilter = null;
        if (entityTypeObj instanceof String) {
            entityTypeFilter = (String) entityTypeObj;
            if (entityTypeFilter.isEmpty()) {
                entityTypeFilter = null;
            }
        }
        
        // 检查执行上下文和区域输入是否有效
        if (context != null && context.getWorld() != null && regionObj instanceof RegionData) {
            RegionData region = (RegionData) regionObj;
            
            // 确保区域完整且有效
            if (region.isComplete()) {
                BlockPos minCorner = region.getMinCorner();
                BlockPos maxCorner = region.getMaxCorner();
                
                // 在实际实现中，获取区域内的所有实体
                // 下面是模拟实现，需要替换为实际的Minecraft API调用
                
                // 假设从context获取包含所有实体的列表
                // List<Entity> allEntities = context.getWorld().getAllEntities();
                List<Object> allEntities = new ArrayList<>(); // 模拟列表
                
                // 最近实体的距离计算（如果有玩家参考点）
                double nearestDistance = Double.MAX_VALUE;
                
                // 过滤区域内的实体
                for (Object entity : allEntities) {
                    // 检查实体是否在区域内
                    // double x = entity.getX(), y = entity.getY(), z = entity.getZ();
                    // boolean inRegion = x >= minCorner.getX() && x <= maxCorner.getX() &&
                    //                   y >= minCorner.getY() && y <= maxCorner.getY() &&
                    //                   z >= minCorner.getZ() && z <= maxCorner.getZ();
                    boolean inRegion = false; // 模拟检查
                    
                    if (inRegion) {
                        // 判断实体类型
                        // boolean isPlayer = entity instanceof PlayerEntity;
                        boolean isPlayer = false; // 模拟检查
                        // boolean isItem = entity instanceof ItemEntity;
                        boolean isItem = false; // 模拟检查
                        
                        // 应用过滤器
                        boolean shouldInclude = true;
                        
                        if (excludePlayersValue && isPlayer) {
                            shouldInclude = false;
                        }
                        
                        if (!includeItemsValue && isItem) {
                            shouldInclude = false;
                        }
                        
                        if (entityTypeFilter != null) {
                            // 检查实体类型是否匹配过滤器
                            // String entityType = Registry.ENTITY_TYPE.getId(entity.getType()).toString();
                            String entityType = "minecraft:unknown"; // 模拟类型
                            if (!entityTypeFilter.equals(entityType)) {
                                shouldInclude = false;
                            }
                        }
                        
                        if (shouldInclude) {
                            entitiesList.add(entity);
                            count++;
                            
                            if (isPlayer) {
                                playerCount++;
                            }
                            
                            // 计算到玩家的距离，找出最近的实体
                            if (context.getPlayer() != null) {
                                // double distance = context.getPlayer().getPos().distanceTo(entity.getPos());
                                double distance = 0; // 模拟距离
                                
                                if (distance < nearestDistance) {
                                    nearestDistance = distance;
                                    nearestEntity = entity;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_ENTITIES_LIST_ID, entitiesList);
        outputValues.put(OUTPUT_COUNT_ID, count);
        outputValues.put(OUTPUT_PLAYER_COUNT_ID, playerCount);
        outputValues.put(OUTPUT_NEAREST_ENTITY_ID, nearestEntity);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isExcludePlayers() {
        return excludePlayers;
    }
    
    public void setExcludePlayers(boolean excludePlayers) {
        this.excludePlayers = excludePlayers;
        markDirty();
    }
    
    public boolean isIncludeItems() {
        return includeItems;
    }
    
    public void setIncludeItems(boolean includeItems) {
        this.includeItems = includeItems;
        markDirty();
    }
} 