package com.nodecraft.nodesystem.nodes.deferred.out_of_scope;

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
 * Set Attribute 节点: 给坐标或方块列表附加自定义数据（属性）
 */
@NodeInfo(
    id = "deferred.out_of_scope.set_attribute",
    displayName = "设置属性",
    description = "给坐标或方块列表附加自定义数据（属性）",
    category = "deferred.out_of_scope"
)
public class SetAttributeNode extends BaseNode {
    
    // --- 节点属性 ---
    private String attributeName = "attribute"; // 属性名称
    private String defaultValue = "true"; // 默认值
    private String description = "给坐标或方块列表附加自定义数据（属性）";
    
    // --- 输入端口 IDs ---
    private static final String INPUT_COORDINATES_ID = "input_coordinates";
    private static final String INPUT_BLOCKS_ID = "input_blocks";
    private static final String INPUT_ATTRIBUTE_NAME_ID = "input_attribute_name";
    private static final String INPUT_ATTRIBUTE_VALUE_ID = "input_attribute_value";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_COORDINATES_ID = "output_coordinates";
    private static final String OUTPUT_BLOCKS_ID = "output_blocks";
    private static final String OUTPUT_ATTRIBUTE_MAP_ID = "output_attribute_map";
    private static final String OUTPUT_COUNT_ID = "output_count";
    
    /**
     * 构造属性设置节点
     */
    public SetAttributeNode() {
        super(UUID.randomUUID(), "deferred.out_of_scope.set_attribute");
        
        // 创建输入端口
        addInputPort(new BasePort(INPUT_COORDINATES_ID, "Coordinates", 
                "要添加属性的坐标列表", NodeDataType.BLOCK_LIST, this));
        
        addInputPort(new BasePort(INPUT_BLOCKS_ID, "Blocks", 
                "要添加属性的方块列表", NodeDataType.BLOCK_LIST, this));
        
        addInputPort(new BasePort(INPUT_ATTRIBUTE_NAME_ID, "Attribute Name", 
                "属性名称", NodeDataType.STRING, this));
        
        addInputPort(new BasePort(INPUT_ATTRIBUTE_VALUE_ID, "Attribute Value", 
                "属性值", NodeDataType.ANY, this));
        
        // 创建输出端口
        addOutputPort(new BasePort(OUTPUT_COORDINATES_ID, "Coordinates", 
                "带属性的坐标列表", NodeDataType.BLOCK_LIST, this));
        
        addOutputPort(new BasePort(OUTPUT_BLOCKS_ID, "Blocks", 
                "带属性的方块列表", NodeDataType.BLOCK_LIST, this));
        
        addOutputPort(new BasePort(OUTPUT_ATTRIBUTE_MAP_ID, "Attribute Map", 
                "坐标到属性的映射", NodeDataType.ANY, this));
        
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Count", 
                "设置了属性的项目数量", NodeDataType.INTEGER, this));
    }
    
    /**
     * 节点的计算逻辑
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 初始化输出值
        BlockPosList coordinatesOutput = new BlockPosList();
        BlockPosList blocksOutput = new BlockPosList();
        Map<BlockPos, Object> attributeMap = new HashMap<>();
        int count = 0;
        
        // 获取输入值
        Object coordinatesObj = inputValues.get(INPUT_COORDINATES_ID);
        Object blocksObj = inputValues.get(INPUT_BLOCKS_ID);
        Object attributeNameObj = inputValues.get(INPUT_ATTRIBUTE_NAME_ID);
        Object attributeValueObj = inputValues.get(INPUT_ATTRIBUTE_VALUE_ID);
        
        // 获取属性名称
        String attrName = this.attributeName;
        if (attributeNameObj instanceof String && !((String) attributeNameObj).isEmpty()) {
            attrName = (String) attributeNameObj;
        }
        
        // 获取属性值
        Object attrValue = attributeValueObj != null ? attributeValueObj : this.defaultValue;
        
        // 将属性附加到坐标列表
        if (coordinatesObj instanceof BlockPosList) {
            BlockPosList coordinates = (BlockPosList) coordinatesObj;
            
            for (BlockPos pos : coordinates) {
                // 添加到输出坐标列表
                coordinatesOutput.add(pos);
                
                // 保存属性映射
                attributeMap.put(pos, attrValue);
                count++;
            }
        } else if (coordinatesObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> coordList = (List<Object>) coordinatesObj;
            
            for (Object obj : coordList) {
                if (obj instanceof BlockPos) {
                    BlockPos pos = (BlockPos) obj;
                    
                    // 添加到输出坐标列表
                    coordinatesOutput.add(pos);
                    
                    // 保存属性映射
                    attributeMap.put(pos, attrValue);
                    count++;
                }
            }
        }
        
        // 将属性附加到方块列表
        if (blocksObj instanceof BlockPosList) {
            BlockPosList blocks = (BlockPosList) blocksObj;
            
            for (BlockPos pos : blocks) {
                // 添加到输出方块列表
                blocksOutput.add(pos);
                
                // 保存属性映射（如果坐标尚未处理）
                if (!attributeMap.containsKey(pos)) {
                    attributeMap.put(pos, attrValue);
                    count++;
                }
            }
        } else if (blocksObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> blockList = (List<Object>) blocksObj;
            
            for (Object obj : blockList) {
                if (obj instanceof BlockPos) {
                    BlockPos pos = (BlockPos) obj;
                    
                    // 添加到输出方块列表
                    blocksOutput.add(pos);
                    
                    // 保存属性映射（如果坐标尚未处理）
                    if (!attributeMap.containsKey(pos)) {
                        attributeMap.put(pos, attrValue);
                        count++;
                    }
                }
            }
        }
        
        // 创建带属性的坐标映射
        Map<String, Object> attributedItems = new HashMap<>();
        attributedItems.put(attrName, attributeMap);
        
        // 设置输出值
        outputValues.put(OUTPUT_COORDINATES_ID, coordinatesOutput);
        outputValues.put(OUTPUT_BLOCKS_ID, blocksOutput);
        outputValues.put(OUTPUT_ATTRIBUTE_MAP_ID, attributedItems);
        outputValues.put(OUTPUT_COUNT_ID, count);
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getAttributeName() {
        return attributeName;
    }
    
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
        markDirty();
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        markDirty();
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        Object[] state = new Object[2];
        state[0] = attributeName;
        state[1] = defaultValue;
        return state;
    }
    
    /**
     * 恢复节点状态
     */
    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof Object[]) {
            Object[] objState = (Object[]) state;
            if (objState.length >= 2) {
                if (objState[0] instanceof String) {
                    attributeName = (String) objState[0];
                }
                if (objState[1] instanceof String) {
                    defaultValue = (String) objState[1];
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return this.description;
    }
} 
