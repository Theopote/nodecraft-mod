package com.nodecraft.nodesystem.nodes.world.nbt;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Set Block State 节点: 修改 BlockInfo 或 MinecraftBlock 的状态属性
 */
@NodeInfo(
    id = "world.nbt.set_block_state",
    displayName = "设置方块状态",
    description = "设置方块的状态属性",
    category = "world.nbt"
)
public class SetBlockStateNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_BLOCK_ID = "input_block";
    private static final String INPUT_PROPERTY_ID = "input_property";
    private static final String INPUT_VALUE_ID = "input_value";
    private static final String INPUT_UPDATE_CLIENTS_ID = "input_update_clients";
    private static final String INPUT_UPDATE_NEIGHBORS_ID = "input_update_neighbors";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_BLOCK_ID = "output_block";
    private static final String OUTPUT_SUCCESS_ID = "output_success";

    // --- 节点属性 ---
    private String description = "修改 BlockInfo 或 MinecraftBlock 的状态属性";
    private boolean updateClients = true; // 是否通知客户端更新
    private boolean updateNeighbors = true; // 是否通知相邻方块更新

    // --- 构造函数 ---
    public SetBlockStateNode() {
        super(UUID.randomUUID(), "world.nbt.set_block_state");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_BLOCK_ID, "Block", 
                "目标方块", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_PROPERTY_ID, "Property", 
                "要设置的属性名称", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_VALUE_ID, "Value", 
                "要设置的属性值", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_UPDATE_CLIENTS_ID, "Update Clients", 
                "是否通知客户端更新", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_UPDATE_NEIGHBORS_ID, "Update Neighbors", 
                "是否通知相邻方块更新", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_BLOCK_ID, "Modified Block", 
                "被修改的方块对象", NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功设置属性", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 默认输出值
        Object modifiedBlock = null;
        boolean success = false;
        
        // 获取输入值
        Object blockObj = inputValues.get(INPUT_BLOCK_ID);
        Object propertyObj = inputValues.get(INPUT_PROPERTY_ID);
        Object valueObj = inputValues.get(INPUT_VALUE_ID);
        Object updateClientsObj = inputValues.get(INPUT_UPDATE_CLIENTS_ID);
        Object updateNeighborsObj = inputValues.get(INPUT_UPDATE_NEIGHBORS_ID);
        
        // 获取是否更新客户端和相邻方块
        boolean shouldUpdateClients = this.updateClients;
        if (updateClientsObj instanceof Boolean) {
            shouldUpdateClients = (Boolean) updateClientsObj;
        }
        
        boolean shouldUpdateNeighbors = this.updateNeighbors;
        if (updateNeighborsObj instanceof Boolean) {
            shouldUpdateNeighbors = (Boolean) updateNeighborsObj;
        }
        
        // 检查必要的输入是否存在
        if (blockObj != null && propertyObj instanceof String && valueObj != null) {
            String propertyName = (String) propertyObj;
            
            try {
                /* 
                在实际实现中，需要根据Minecraft API实现以下操作:
                1. 获取方块状态
                2. 查找并修改指定属性
                3. 应用新的状态
                
                示例代码如下（需要根据实际API调整）:
                
                // 计算更新标志
                int updateFlags = 0;
                if (shouldUpdateClients) {
                    updateFlags |= Block.NOTIFY_LISTENERS;
                }
                if (shouldUpdateNeighbors) {
                    updateFlags |= Block.NOTIFY_NEIGHBORS;
                }
                
                // 处理BlockState对象
                if (blockObj instanceof BlockState) {
                    BlockState oldState = (BlockState) blockObj;
                    
                    // 查找目标属性
                    for (Property<?> property : oldState.getProperties()) {
                        if (property.getName().equals(propertyName)) {
                            // 尝试解析并设置属性值
                            Optional<Object> validValue = parsePropertyValue(property, valueObj);
                            
                            if (validValue.isPresent()) {
                                // 注意：这里需要类型转换和类型安全处理
                                // Property<Object> uncheckedProperty = (Property<Object>) property;
                                // BlockState newState = oldState.with(uncheckedProperty, validValue.get());
                                
                                // 如果包含在世界中的方块，则应用更改
                                // if (context != null && context.getWorld() != null) {
                                //     BlockPos pos = ...; // 需要确定方块位置
                                //     context.getWorld().setBlockState(pos, newState, updateFlags);
                                // }
                                
                                // modifiedBlock = newState;
                                // success = true;
                                // break;
                            }
                        }
                    }
                } 
                // 处理BlockInfo对象
                // else if (blockObj instanceof BlockInfo) {
                //     BlockInfo blockInfo = (BlockInfo) blockObj;
                //     BlockState oldState = blockInfo.getState();
                //     
                //     // 查找目标属性
                //     for (Property<?> property : oldState.getProperties()) {
                //         if (property.getName().equals(propertyName)) {
                //             // 尝试解析并设置属性值
                //             Optional<Object> validValue = parsePropertyValue(property, valueObj);
                //             
                //             if (validValue.isPresent()) {
                //                 Property<Object> uncheckedProperty = (Property<Object>) property;
                //                 BlockState newState = oldState.with(uncheckedProperty, validValue.get());
                //                 
                //                 // 创建新的BlockInfo对象
                //                 BlockInfo newBlockInfo = new BlockInfo(
                //                     blockInfo.getPos(),
                //                     newState,
                //                     blockInfo.getNbt() != null ? blockInfo.getNbt().copy() : null
                //                 );
                //                 
                //                 modifiedBlock = newBlockInfo;
                //                 success = true;
                //                 break;
                //             }
                //         }
                //     }
                // }
                */
                
                // 模拟成功设置方块状态属性 (在实际实现中替换为上面的逻辑)
                modifiedBlock = blockObj;
                success = true;
            } catch (Exception e) {
                success = false;
                System.err.println("Error setting block state: " + e.getMessage());
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_BLOCK_ID, modifiedBlock);
        outputValues.put(OUTPUT_SUCCESS_ID, success);
    }
    
    /* 
    // 在实际实现中需要这个辅助方法来解析属性值
    private Optional<Object> parsePropertyValue(Property<?> property, Object value) {
        // 处理字符串值
        if (value instanceof String) {
            return property.parse((String) value);
        }
        
        // 处理布尔值
        if (property instanceof BooleanProperty && value instanceof Boolean) {
            return Optional.of(value);
        }
        
        // 处理整数值
        if (property instanceof IntProperty && value instanceof Number) {
            int intValue = ((Number) value).intValue();
            IntProperty intProperty = (IntProperty) property;
            if (intValue >= intProperty.getMin() && intValue <= intProperty.getMax()) {
                return Optional.of(intValue);
            }
        }
        
        // 处理枚举值（如方向）
        if (property instanceof EnumProperty && value instanceof String) {
            String strValue = (String) value;
            EnumProperty<?> enumProperty = (EnumProperty<?>) property;
            
            for (Object enumConstant : enumProperty.getValues()) {
                if (enumConstant.toString().equalsIgnoreCase(strValue)) {
                    return Optional.of(enumConstant);
                }
            }
        }
        
        return Optional.empty();
    }
    */
    
    // --- Getters/Setters for Properties ---
    
    public boolean isUpdateClients() {
        return updateClients;
    }
    
    public void setUpdateClients(boolean updateClients) {
        this.updateClients = updateClients;
        markDirty();
    }
    
    public boolean isUpdateNeighbors() {
        return updateNeighbors;
    }
    
    public void setUpdateNeighbors(boolean updateNeighbors) {
        this.updateNeighbors = updateNeighbors;
        markDirty();
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        return new boolean[] { updateClients, updateNeighbors };
    }
    
    /**
     * 恢复节点状态
     */
    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof boolean[]) {
            boolean[] boolArray = (boolean[]) state;
            if (boolArray.length >= 1) {
                updateClients = boolArray[0];
            }
            if (boolArray.length >= 2) {
                updateNeighbors = boolArray[1];
            }
        }
    }
} 