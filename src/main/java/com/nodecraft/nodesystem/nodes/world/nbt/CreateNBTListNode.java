package com.nodecraft.nodesystem.nodes.world.nbt;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Create NBT List 节点: 创建 NBT 列表标签
 */
@NodeInfo(
    id = "world.nbt.create_nbt_list",
    displayName = "创建NBT列表",
    description = "创建NBT列表标签",
    category = "world.nbt"
)
public class CreateNBTListNode extends BaseNode {

    // --- 节点属性 ---
    private String description = "创建 NBT 列表标签";
    private int itemCount = 5; // 列表项数量
    private String itemType = "string"; // 列表项类型
    
    // --- 输入/输出端口ID前缀 ---
    private static final String INPUT_ITEM_PREFIX = "input_item_";
    private static final String INPUT_LIST_TYPE_ID = "input_list_type";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_LIST_ID = "output_list";

    // --- 构造函数 ---
    public CreateNBTListNode() {
        super(UUID.randomUUID(), "world.nbt.create_nbt_list");
        
        // 创建类型输入端口
        addInputPort(new BasePort(INPUT_LIST_TYPE_ID, "List Type", 
                "列表项的类型 (string, int, float, double, byte, compound)", 
                NodeDataType.STRING, this));
        
        // 创建动态项目端口
        rebuildItemPorts();
        
        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_LIST_ID, "NBT List", 
                "创建的NBT列表标签", NodeDataType.NBT_LIST, this));
    }

    /**
     * 重建项目输入端口
     */
    private void rebuildItemPorts() {
        // 移除旧的项目端口
        List<BasePort> portsToKeep = new ArrayList<>();
        for (int i = 0; i < inputPorts.size(); i++) {
            BasePort port = (BasePort) inputPorts.get(i);
            if (!port.getId().startsWith(INPUT_ITEM_PREFIX)) {
                portsToKeep.add(port);
            }
        }
        
        // 清除所有现有输入端口
        inputPorts.clear();
        
        // 重新添加要保留的端口
        for (BasePort port : portsToKeep) {
            addInputPort(port);
        }
        
        // 创建新的项目输入端口
        for (int i = 0; i < itemCount; i++) {
            addInputPort(new BasePort(
                INPUT_ITEM_PREFIX + i, 
                "Item " + (i + 1), 
                "列表项的值", 
                NodeDataType.ANY, 
                this
            ));
        }
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 创建输出
        Object nbtList = null;
        
        // 获取列表类型
        Object typeObj = inputValues.get(INPUT_LIST_TYPE_ID);
        String listType = this.itemType;
        if (typeObj instanceof String) {
            listType = ((String) typeObj).toLowerCase();
            if (!isValidType(listType)) {
                listType = "string"; // 如果类型无效则使用默认值
            }
        }
        
        try {
            /*
            在实际实现中，需要根据Minecraft API实现以下操作:
            
            // 确定标签类型
            int tagType;
            switch (listType) {
                case "byte": tagType = Tag.BYTE_TYPE; break;
                case "short": tagType = Tag.SHORT_TYPE; break;
                case "int": tagType = Tag.INT_TYPE; break;
                case "long": tagType = Tag.LONG_TYPE; break;
                case "float": tagType = Tag.FLOAT_TYPE; break;
                case "double": tagType = Tag.DOUBLE_TYPE; break;
                case "string": tagType = Tag.STRING_TYPE; break;
                case "compound": tagType = Tag.COMPOUND_TYPE; break;
                default: tagType = Tag.STRING_TYPE; break;
            }
            
            // 创建新的列表标签
            ListTag listTag = new ListTag();
            
            // 处理每个项目并添加到列表
            for (int i = 0; i < itemCount; i++) {
                Object itemObj = inputValues.get(INPUT_ITEM_PREFIX + i);
                
                if (itemObj != null) {
                    // 创建对应类型的标签并添加到列表
                    Tag tag = createTag(listType, itemObj);
                    if (tag != null) {
                        listTag.add(tag);
                    }
                }
            }
            
            nbtList = listTag;
            */
            
            // 模拟创建NBT列表标签 (在实际实现中替换为上面的逻辑)
            nbtList = new Object(); // 这里只是一个占位符
        } catch (Exception e) {
            System.err.println("Error creating NBT list: " + e.getMessage());
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_LIST_ID, nbtList);
    }
    
    /**
     * 检查类型是否有效
     */
    private boolean isValidType(String type) {
        switch (type.toLowerCase()) {
            case "string":
            case "int":
            case "float":
            case "double":
            case "byte":
            case "long":
            case "short":
            case "compound":
                return true;
            default:
                return false;
        }
    }
    
    /* 
    // 在实际实现中需要这个辅助方法来创建正确类型的NBT标签
    private Tag createTag(String type, Object value) {
        switch (type.toLowerCase()) {
            case "string":
                return StringTag.of(String.valueOf(value));
                
            case "int":
                if (value instanceof Number) {
                    return IntTag.of(((Number) value).intValue());
                } else {
                    try {
                        return IntTag.of(Integer.parseInt(String.valueOf(value)));
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                
            case "float":
                if (value instanceof Number) {
                    return FloatTag.of(((Number) value).floatValue());
                } else {
                    try {
                        return FloatTag.of(Float.parseFloat(String.valueOf(value)));
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                
            case "double":
                if (value instanceof Number) {
                    return DoubleTag.of(((Number) value).doubleValue());
                } else {
                    try {
                        return DoubleTag.of(Double.parseDouble(String.valueOf(value)));
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                
            case "byte":
                // 处理布尔值
                if (value instanceof Boolean) {
                    return ByteTag.of((Boolean) value ? (byte) 1 : (byte) 0);
                } else if (value instanceof Number) {
                    return ByteTag.of(((Number) value).byteValue());
                } else {
                    try {
                        return ByteTag.of(Byte.parseByte(String.valueOf(value)));
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                
            case "long":
                if (value instanceof Number) {
                    return LongTag.of(((Number) value).longValue());
                } else {
                    try {
                        return LongTag.of(Long.parseLong(String.valueOf(value)));
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                
            case "short":
                if (value instanceof Number) {
                    return ShortTag.of(((Number) value).shortValue());
                } else {
                    try {
                        return ShortTag.of(Short.parseShort(String.valueOf(value)));
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                
            case "compound":
                if (value instanceof CompoundTag) {
                    return (CompoundTag) value;
                }
                return new CompoundTag();
                
            default:
                return null;
        }
    }
    */
    
    // --- Getters/Setters for Properties ---
    
    public int getItemCount() {
        return itemCount;
    }
    
    public void setItemCount(int itemCount) {
        if (itemCount < 1) {
            itemCount = 1;
        } else if (itemCount > 50) {
            itemCount = 50; // 设置最大限制，防止创建过多端口
        }
        
        if (this.itemCount != itemCount) {
            this.itemCount = itemCount;
            rebuildItemPorts();
            markDirty();
        }
    }
    
    public String getItemType() {
        return itemType;
    }
    
    public void setItemType(String itemType) {
        if (isValidType(itemType)) {
            this.itemType = itemType.toLowerCase();
            markDirty();
        }
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        // 将状态保存为数组：[数量, 类型]
        return new Object[] { itemCount, itemType };
    }
    
    /**
     * 恢复节点状态
     */
    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof Object[]) {
            Object[] stateArray = (Object[]) state;
            
            if (stateArray.length >= 1 && stateArray[0] instanceof Number) {
                setItemCount(((Number) stateArray[0]).intValue());
            }
            
            if (stateArray.length >= 2 && stateArray[1] instanceof String) {
                setItemType((String) stateArray[1]);
            }
        }
    }

    @Override
    public String getDescription() {
        return this.description;
    }
} 