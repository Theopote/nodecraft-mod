package com.nodecraft.nodesystem.nodes.utilities.advanced;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Get Attribute 节点: 获取坐标或方块列表的属性值
 * 支持嵌套属性访问和类型转换
 */
@NodeInfo(
    id = "utilities.advanced.get_attribute",
    displayName = "获取属性",
    description = "从对象中获取指定属性的值",
    category = "utilities.advanced"
)
public class GetAttributeNode extends BaseNode {
    
    // --- 节点属性 ---
    private String attributeName = "attribute"; // 属性名称
    private Object defaultValue = null; // 默认值
    private String description = "获取坐标或方块列表的属性值";
    
    // --- 输入端口 IDs ---
    private static final String INPUT_COORDINATES_ID = "input_coordinates";
    private static final String INPUT_ATTRIBUTE_MAP_ID = "input_attribute_map";
    private static final String INPUT_ATTRIBUTE_NAME_ID = "input_attribute_name";
    private static final String INPUT_DEFAULT_VALUE_ID = "input_default_value";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_VALUES_ID = "output_values";
    private static final String OUTPUT_VALUE_MAP_ID = "output_value_map";
    private static final String OUTPUT_HAS_ATTRIBUTE_ID = "output_has_attribute";
    
    /**
     * 构造获取属性节点
     */
    public GetAttributeNode() {
        super(UUID.randomUUID(), "utilities.advanced.get_attribute");
        
        // 创建输入端口
        addInputPort(new BasePort(INPUT_COORDINATES_ID, "Coordinates", 
                "要获取属性的坐标或方块列表", NodeDataType.BLOCK_LIST, this));
        
        addInputPort(new BasePort(INPUT_ATTRIBUTE_MAP_ID, "Attribute Map", 
                "属性映射（通常来自Set Attribute节点）", NodeDataType.ANY, this));
        
        addInputPort(new BasePort(INPUT_ATTRIBUTE_NAME_ID, "Attribute Name", 
                "要获取的属性名称", NodeDataType.STRING, this));
        
        addInputPort(new BasePort(INPUT_DEFAULT_VALUE_ID, "Default Value", 
                "未找到属性时的默认值", NodeDataType.ANY, this));
        
        // 创建输出端口
        addOutputPort(new BasePort(OUTPUT_VALUES_ID, "Values", 
                "获取的属性值列表", NodeDataType.LIST, this));
        
        addOutputPort(new BasePort(OUTPUT_VALUE_MAP_ID, "Value Map", 
                "坐标到属性值的映射", NodeDataType.ANY, this));
        
        addOutputPort(new BasePort(OUTPUT_HAS_ATTRIBUTE_ID, "Has Attribute", 
                "每个坐标是否包含指定属性", NodeDataType.BOOLEAN, this));
    }
    
    /**
     * 节点的计算逻辑
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 初始化输出值
        List<Object> attributeValues = new ArrayList<>();
        Map<BlockPos, Object> valueMap = new HashMap<>();
        boolean hasAttribute = false;
        
        // 获取输入值
        Object coordinatesObj = inputValues.get(INPUT_COORDINATES_ID);
        Object attributeMapObj = inputValues.get(INPUT_ATTRIBUTE_MAP_ID);
        Object attributeNameObj = inputValues.get(INPUT_ATTRIBUTE_NAME_ID);
        Object defaultValueObj = inputValues.get(INPUT_DEFAULT_VALUE_ID);
        
        // 获取属性名称
        String attrName = this.attributeName;
        if (attributeNameObj instanceof String && !((String) attributeNameObj).isEmpty()) {
            attrName = (String) attributeNameObj;
        }
        
        // 获取默认值
        Object defValue = defaultValueObj != null ? defaultValueObj : this.defaultValue;
        
        // 解析属性映射
        Map<BlockPos, Object> attributeMap = new HashMap<>();
        if (attributeMapObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> attrMaps = (Map<String, Object>) attributeMapObj;
            
            // 查找指定名称的属性映射
            Object attrMapForName = attrMaps.get(attrName);
            if (attrMapForName instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<Object, Object> posToValueMap = (Map<Object, Object>) attrMapForName;
                
                // 从Object映射转换为BlockPos映射
                for (Map.Entry<Object, Object> entry : posToValueMap.entrySet()) {
                    if (entry.getKey() instanceof BlockPos) {
                        attributeMap.put((BlockPos) entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        
        // 处理坐标列表，获取每个坐标的属性值
        List<BlockPos> positions = new ArrayList<>();
        
        // 从各种输入类型收集BlockPos
        if (coordinatesObj instanceof BlockPosList) {
            BlockPosList coordinates = (BlockPosList) coordinatesObj;
            for (BlockPos pos : coordinates) {
                positions.add(pos);
            }
        } else if (coordinatesObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> coordList = (List<Object>) coordinatesObj;
            
            for (Object obj : coordList) {
                if (obj instanceof BlockPos) {
                    positions.add((BlockPos) obj);
                }
            }
        }
        
        // 处理收集到的位置
        for (BlockPos pos : positions) {
            // 检查该位置是否有属性值
            if (attributeMap.containsKey(pos)) {
                Object value = attributeMap.get(pos);
                attributeValues.add(value);
                valueMap.put(pos, value);
                hasAttribute = true;
            } else {
                // 使用默认值
                attributeValues.add(defValue);
                valueMap.put(pos, defValue);
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_VALUES_ID, attributeValues);
        outputValues.put(OUTPUT_VALUE_MAP_ID, valueMap);
        outputValues.put(OUTPUT_HAS_ATTRIBUTE_ID, hasAttribute);
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getAttributeName() {
        return attributeName;
    }
    
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
        markDirty();
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        Object[] state = new Object[1];
        state[0] = attributeName;
        return state;
    }
    
    /**
     * 恢复节点状态
     */
    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof Object[]) {
            Object[] objState = (Object[]) state;
            if (objState.length >= 1 && objState[0] instanceof String) {
                attributeName = (String) objState[0];
            }
        }
    }

    @Override
    public String getDescription() {
        return this.description;
    }
} 