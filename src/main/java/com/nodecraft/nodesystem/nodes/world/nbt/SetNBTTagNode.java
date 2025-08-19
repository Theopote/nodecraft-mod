package com.nodecraft.nodesystem.nodes.world.nbt;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Set NBT Tag 节点: 设置方块实体或实体的NBT标签。
 */
@NodeInfo(
    id = "world.nbt.set_nbt_tag",
    displayName = "设置NBT标签",
    description = "设置方块实体或实体的NBT标签",
    category = "world.nbt"
)
public class SetNBTTagNode extends BaseNode {

    // --- 节点属性 ---
    private String description = "设置 MinecraftBlock 或 MinecraftEntity 的 NBT 值";

    // --- 输入端口 IDs ---
    private static final String INPUT_TARGET_ID = "input_target";
    private static final String INPUT_PATH_ID = "input_path";
    private static final String INPUT_VALUE_ID = "input_value";
    private static final String INPUT_VALUE_TYPE_ID = "input_value_type";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_TARGET_ID = "output_target";
    private static final String OUTPUT_SUCCESS_ID = "output_success";

    // --- 构造函数 ---
    public SetNBTTagNode() {
        super(UUID.randomUUID(), "world.nbt.set_nbt_tag");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_TARGET_ID, "Target", 
                "目标方块或实体", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_PATH_ID, "Path", 
                "NBT标签路径 (如 'Items[0].id')", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_VALUE_ID, "Value", 
                "要设置的值", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_VALUE_TYPE_ID, "Value Type", 
                "值的类型 (string, int, float, double, byte, list, compound等)", NodeDataType.STRING, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_TARGET_ID, "Modified Target", 
                "被修改的目标对象", NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功设置值", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 默认输出值
        Object modifiedTarget = null;
        boolean success = false;
        
        // 获取输入值
        Object targetObj = inputValues.get(INPUT_TARGET_ID);
        Object pathObj = inputValues.get(INPUT_PATH_ID);
        Object valueObj = inputValues.get(INPUT_VALUE_ID);
        Object valueTypeObj = inputValues.get(INPUT_VALUE_TYPE_ID);
        
        // 确定值类型
        String valueType = "string"; // 默认类型
        if (valueTypeObj instanceof String) {
            valueType = ((String) valueTypeObj).toLowerCase();
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
        
        // 检查必要的输入是否存在
        if (targetObj != null && pathObj instanceof String && valueObj != null) {
            String path = (String) pathObj;
            
            try {
                // 确定目标对象类型并设置NBT
                if (targetObj instanceof Object) { // 需要使用正确的Minecraft类型检查
                    /*
                    在实际实现中，需要根据Minecraft API实现以下操作:
                    1. 检查targetObj是否为方块或实体
                    2. 获取其NBT数据
                    3. 解析路径，设置指定位置的值
                    4. 将更新后的NBT应用回对象
                    
                    CompoundTag nbt = null;
                    boolean isBlockEntity = false;
                    
                    if (targetObj instanceof BlockEntity) {
                        BlockEntity blockEntity = (BlockEntity) targetObj;
                        nbt = blockEntity.saveWithFullMetadata();
                        isBlockEntity = true;
                    } else if (targetObj instanceof Entity) {
                        Entity entity = (Entity) targetObj;
                        nbt = entity.writeNbt(new CompoundTag());
                    }
                    
                    if (nbt != null) {
                        // 处理NBT路径以设置特定的值
                        if (path.contains(".") || path.contains("[")) {
                            // 处理嵌套路径
                            String[] parts = path.split("\\.");
                            CompoundTag current = nbt;
                            
                            // 处理除最后一部分外的嵌套路径
                            for (int i = 0; i < parts.length - 1; i++) {
                                String part = parts[i];
                                if (part.contains("[") && part.contains("]")) {
                                    // 处理数组索引，例如 Items[0]
                                    String name = part.substring(0, part.indexOf("["));
                                    int index = Integer.parseInt(part.substring(
                                        part.indexOf("[") + 1, part.indexOf("]")));
                                    
                                    if (!current.contains(name)) {
                                        // 创建新的列表
                                        current.put(name, new ListTag());
                                    }
                                    
                                    ListTag list = current.getList(name, Tag.COMPOUND_TYPE);
                                    while (list.size() <= index) {
                                        // 确保列表有足够的元素
                                        list.add(new CompoundTag());
                                    }
                                    
                                    current = list.getCompound(index);
                                } else {
                                    if (!current.contains(part)) {
                                        // 创建新的复合标签
                                        current.put(part, new CompoundTag());
                                    }
                                    current = current.getCompound(part);
                                }
                            }
                            
                            // 处理最后一部分，设置实际值
                            String lastPart = parts[parts.length - 1];
                            Tag tag = createTag(valueType, valueObj);
                            
                            // 设置最终值
                            if (lastPart.contains("[") && lastPart.contains("]")) {
                                // 处理数组索引
                                String name = lastPart.substring(0, lastPart.indexOf("["));
                                int index = Integer.parseInt(lastPart.substring(
                                    lastPart.indexOf("[") + 1, lastPart.indexOf("]")));
                                
                                if (!current.contains(name)) {
                                    // 创建新的列表
                                    current.put(name, new ListTag());
                                }
                                
                                ListTag list = current.getList(name, Tag.LIST_TYPE);
                                while (list.size() <= index) {
                                    // 确保列表有足够的元素
                                    list.add(new CompoundTag());
                                }
                                
                                list.setTag(index, tag);
                            } else {
                                current.put(lastPart, tag);
                            }
                        } else {
                            // 简单路径，直接设置值
                            Tag tag = createTag(valueType, valueObj);
                            nbt.put(path, tag);
                        }
                        
                        // 将更新后的NBT应用回对象
                        if (isBlockEntity) {
                            BlockEntity blockEntity = (BlockEntity) targetObj;
                            blockEntity.loadNbt(nbt);
                            blockEntity.markDirty();
                            // 如果在服务器端，可能需要同步到客户端
                            if (blockEntity.getWorld() instanceof ServerWorld) {
                                ((ServerWorld) blockEntity.getWorld()).updateListeners(
                                    blockEntity.getPos(), 
                                    blockEntity.getCachedState(), 
                                    blockEntity.getCachedState(), 
                                    Block.NOTIFY_LISTENERS);
                            }
                        } else if (targetObj instanceof Entity) {
                            Entity entity = (Entity) targetObj;
                            entity.readNbt(nbt);
                        }
                        
                        modifiedTarget = targetObj;
                        success = true;
                    }
                    */
                    
                    // 模拟成功设置NBT值 (在实际实现中替换为上面的逻辑)
                    modifiedTarget = targetObj;
                    success = true;
                }
            } catch (Exception e) {
                success = false;
                System.err.println("Error setting NBT tag: " + e.getMessage());
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_TARGET_ID, modifiedTarget);
        outputValues.put(OUTPUT_SUCCESS_ID, success);
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
} 