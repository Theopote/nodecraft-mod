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
 * Create NBT Compound 节点: 创建 NBT 复合标签结构
 */
@NodeInfo(
    id = "world.nbt.create_nbt_compound",
    displayName = "创建NBT复合标签",
    description = "创建NBT复合标签结构",
    category = "world.nbt"
)
public class CreateNBTCompoundNode extends BaseNode {

    // --- 节点属性 ---
    private String description = "创建 NBT 复合标签结构";
    private int pairCount = 5; // 键值对数量
    
    // --- 输入/输出端口ID前缀 ---
    private static final String INPUT_KEY_PREFIX = "input_key_";
    private static final String INPUT_VALUE_PREFIX = "input_value_";
    private static final String INPUT_VALUE_TYPE_PREFIX = "input_value_type_";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_NBT_ID = "output_nbt";

    // --- 构造函数 ---
    public CreateNBTCompoundNode() {
        super(UUID.randomUUID(), "world.nbt.create_nbt_compound");
        
        // 创建动态端口
        rebuildPorts();
        
        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_NBT_ID, "NBT Compound", 
                "创建的NBT复合标签", NodeDataType.NBT_COMPOUND, this));
    }

    /**
     * 重建输入端口
     */
    private void rebuildPorts() {
        // 清除所有现有输入端口
        inputPorts.clear();
        
        // 创建键值对输入端口
        for (int i = 0; i < pairCount; i++) {
            // 键输入端口
            addInputPort(new BasePort(
                INPUT_KEY_PREFIX + i, 
                "Key " + (i + 1), 
                "NBT标签的键名称", 
                NodeDataType.STRING, 
                this
            ));
            
            // 值输入端口
            addInputPort(new BasePort(
                INPUT_VALUE_PREFIX + i, 
                "Value " + (i + 1), 
                "对应的值", 
                NodeDataType.ANY, 
                this
            ));
            
            // 值类型输入端口
            addInputPort(new BasePort(
                INPUT_VALUE_TYPE_PREFIX + i, 
                "Type " + (i + 1), 
                "值的类型 (string, int, float, double, byte, list, compound)", 
                NodeDataType.STRING, 
                this
            ));
        }
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 创建输出
        Object nbtCompound = null;
        
        try {
            /*
            在实际实现中，需要根据Minecraft API实现以下操作:
            
            // 创建新的复合标签
            CompoundTag compound = new CompoundTag();
            
            // 处理每个键值对
            for (int i = 0; i < pairCount; i++) {
                // 获取键名、值和类型
                Object keyObj = inputValues.get(INPUT_KEY_PREFIX + i);
                Object valueObj = inputValues.get(INPUT_VALUE_PREFIX + i);
                Object typeObj = inputValues.get(INPUT_VALUE_TYPE_PREFIX + i);
                
                // 跳过空键
                if (keyObj == null || !(keyObj instanceof String) || ((String) keyObj).trim().isEmpty()) {
                    continue;
                }
                
                String key = (String) keyObj;
                
                // 确定值类型
                String valueType = "string"; // 默认类型
                if (typeObj instanceof String) {
                    valueType = ((String) typeObj).toLowerCase();
                } else if (valueObj instanceof String) {
                    valueType = "string";
                } else if (valueObj instanceof Integer) {
                    valueType = "int";
                } else if (valueObj instanceof Float) {
                    valueType = "float";
                } else if (valueObj instanceof Double) {
                    valueType = "double";
                } else if (valueObj instanceof Boolean) {
                    valueType = "byte"; // Minecraft中布尔值通常存储为字节
                } else if (valueObj instanceof java.util.List) {
                    valueType = "list";
                } else if (valueObj != null && valueObj.getClass().getSimpleName().contains("CompoundTag")) {
                    valueType = "compound";
                }
                
                // 创建并添加对应类型的标签
                if (valueObj != null) {
                    Tag tag = createTag(valueType, valueObj);
                    compound.put(key, tag);
                }
            }
            
            nbtCompound = compound;
            */
            
            // 模拟创建NBT复合标签 (在实际实现中替换为上面的逻辑)
            nbtCompound = new Object(); // 这里只是一个占位符
        } catch (Exception e) {
            System.err.println("Error creating NBT compound: " + e.getMessage());
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_NBT_ID, nbtCompound);
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
                        return IntTag.of(0);
                    }
                }
                
            case "float":
                if (value instanceof Number) {
                    return FloatTag.of(((Number) value).floatValue());
                } else {
                    try {
                        return FloatTag.of(Float.parseFloat(String.valueOf(value)));
                    } catch (NumberFormatException e) {
                        return FloatTag.of(0);
                    }
                }
                
            case "double":
                if (value instanceof Number) {
                    return DoubleTag.of(((Number) value).doubleValue());
                } else {
                    try {
                        return DoubleTag.of(Double.parseDouble(String.valueOf(value)));
                    } catch (NumberFormatException e) {
                        return DoubleTag.of(0);
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
                        return ByteTag.of((byte) 0);
                    }
                }
                
            case "long":
                if (value instanceof Number) {
                    return LongTag.of(((Number) value).longValue());
                } else {
                    try {
                        return LongTag.of(Long.parseLong(String.valueOf(value)));
                    } catch (NumberFormatException e) {
                        return LongTag.of(0L);
                    }
                }
                
            case "short":
                if (value instanceof Number) {
                    return ShortTag.of(((Number) value).shortValue());
                } else {
                    try {
                        return ShortTag.of(Short.parseShort(String.valueOf(value)));
                    } catch (NumberFormatException e) {
                        return ShortTag.of((short) 0);
                    }
                }
                
            case "list":
                if (value instanceof java.util.List) {
                    ListTag listTag = new ListTag();
                    for (Object item : (java.util.List<?>) value) {
                        // 递归处理列表项，假设所有项都是同一类型
                        // 实际实现可能需要更复杂的类型检测
                        if (item instanceof String) {
                            listTag.add(StringTag.of((String) item));
                        } else if (item instanceof Integer) {
                            listTag.add(IntTag.of((Integer) item));
                        } else if (item instanceof Double) {
                            listTag.add(DoubleTag.of((Double) item));
                        } else {
                            // 默认处理为复合标签
                            listTag.add(new CompoundTag());
                        }
                    }
                    return listTag;
                }
                return new ListTag();
                
            case "compound":
                if (value instanceof CompoundTag) {
                    return (CompoundTag) value;
                }
                return new CompoundTag();
                
            default:
                return StringTag.of(String.valueOf(value));
        }
    }
    */
    
    // --- Getters/Setters for Properties ---
    
    public int getPairCount() {
        return pairCount;
    }
    
    public void setPairCount(int pairCount) {
        if (pairCount < 1) {
            pairCount = 1;
        } else if (pairCount > 20) {
            pairCount = 20; // 设置最大限制，防止创建过多端口
        }
        
        if (this.pairCount != pairCount) {
            this.pairCount = pairCount;
            rebuildPorts();
            markDirty();
        }
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        return pairCount;
    }
    
    /**
     * 恢复节点状态
     */
    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof Integer) {
            setPairCount((Integer) state);
        } else if (state instanceof Number) {
            setPairCount(((Number) state).intValue());
        }
    }

    @Override
    public String getDescription() {
        return this.description;
    }
} 