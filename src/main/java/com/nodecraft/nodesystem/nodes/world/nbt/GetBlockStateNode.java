package com.nodecraft.nodesystem.nodes.world.nbt;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Get Block State 节点: 获取方块的状态属性。
 */
@NodeInfo(
    id = "world.nbt.get_block_state",
    displayName = "获取方块状态",
    description = "获取方块的状态属性",
    category = "world.nbt"
)
public class GetBlockStateNode extends BaseNode {

    // --- 节点属性 ---
    private String description = "从 MinecraftBlock 获取指定状态属性的值";

    // --- 输入端口 IDs ---
    private static final String INPUT_BLOCK_ID = "input_block";
    private static final String INPUT_PROPERTY_ID = "input_property";
    private static final String INPUT_DEFAULT_VALUE_ID = "input_default_value";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_VALUE_ID = "output_value";
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_PROPERTIES_ID = "output_properties";

    // --- 构造函数 ---
    public GetBlockStateNode() {
        super(UUID.randomUUID(), "world.nbt.get_block_state");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_BLOCK_ID, "Block", 
                "目标方块", NodeDataType.MINECRAFT_BLOCK, this));
        addInputPort(new BasePort(INPUT_PROPERTY_ID, "Property", 
                "要获取的属性名称", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_DEFAULT_VALUE_ID, "Default Value", 
                "如果找不到属性时返回的默认值", NodeDataType.ANY, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_VALUE_ID, "Value", 
                "属性的值", NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功获取值", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_PROPERTIES_ID, "All Properties", 
                "方块的所有属性", NodeDataType.ANY, this));
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 默认输出值
        Object value = null;
        boolean success = false;
        Object allProperties = new java.util.HashMap<String, Object>();
        
        // 获取输入值
        Object blockObj = inputValues.get(INPUT_BLOCK_ID);
        Object propertyObj = inputValues.get(INPUT_PROPERTY_ID);
        Object defaultValueObj = inputValues.get(INPUT_DEFAULT_VALUE_ID);
        
        // 检查必要的输入是否存在
        if (blockObj != null && propertyObj instanceof String) {
            String propertyName = (String) propertyObj;
            
            try {
                /*
                在实际实现中，需要根据Minecraft API实现以下操作:
                1. 获取方块状态
                2. 查找指定属性
                3. 获取属性值
                
                if (blockObj instanceof BlockState) {
                    BlockState blockState = (BlockState) blockObj;
                    
                    // 获取所有属性
                    Map<String, Object> properties = new HashMap<>();
                    for (Property<?> property : blockState.getProperties()) {
                        String name = property.getName();
                        Comparable<?> propValue = blockState.get(property);
                        properties.put(name, propValue);
                    }
                    
                    // 设置所有属性输出
                    allProperties = properties;
                    
                    // 查找指定属性
                    for (Property<?> property : blockState.getProperties()) {
                        if (property.getName().equals(propertyName)) {
                            value = blockState.get(property);
                            success = true;
                            break;
                        }
                    }
                }
                */
                
                // 模拟成功获取方块状态属性 (在实际实现中替换为上面的逻辑)
                java.util.Map<String, Object> mockProperties = new java.util.HashMap<>();
                mockProperties.put("facing", "north");
                mockProperties.put("powered", false);
                mockProperties.put("waterlogged", false);
                
                allProperties = mockProperties;
                
                if (mockProperties.containsKey(propertyName)) {
                    value = mockProperties.get(propertyName);
                    success = true;
                }
            } catch (Exception e) {
                success = false;
                System.err.println("Error getting block state: " + e.getMessage());
            }
        }
        
        // 如果获取失败并且提供了默认值，则使用默认值
        if (!success && defaultValueObj != null) {
            value = defaultValueObj;
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_VALUE_ID, value);
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_PROPERTIES_ID, allProperties);
    }
} 