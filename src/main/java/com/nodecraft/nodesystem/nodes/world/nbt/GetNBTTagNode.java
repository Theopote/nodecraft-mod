package com.nodecraft.nodesystem.nodes.world.nbt;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Get NBT Tag 节点: 获取方块实体或实体的NBT标签值。
 */
@NodeInfo(
    id = "world.nbt.get_nbt_tag",
    displayName = "获取NBT标签",
    description = "获取方块实体或实体的NBT标签值",
    category = "world.nbt"
)
public class GetNBTTagNode extends BaseNode {

    // --- 节点属性 ---
    private String description = "从MinecraftBlock或MinecraftEntity获取指定路径的NBT值";

    // --- 输入端口 IDs ---
    private static final String INPUT_TARGET_ID = "input_target";
    private static final String INPUT_PATH_ID = "input_path";
    private static final String INPUT_DEFAULT_VALUE_ID = "input_default_value";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_VALUE_ID = "output_value";
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_TYPE_ID = "output_type";

    // --- 构造函数 ---
    public GetNBTTagNode() {
        super(UUID.randomUUID(), "world.nbt.get_nbt_tag");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_TARGET_ID, "Target", 
                "目标方块或实体", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_PATH_ID, "Path", 
                "NBT标签路径 (如 'Items[0].id')", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_DEFAULT_VALUE_ID, "Default Value", 
                "如果找不到标签时返回的默认值", NodeDataType.ANY, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_VALUE_ID, "Value", 
                "NBT标签的值", NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功获取值", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_TYPE_ID, "Type", 
                "NBT值的类型", NodeDataType.STRING, this));
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
        String type = "none";
        
        // 获取输入值
        Object targetObj = inputValues.get(INPUT_TARGET_ID);
        Object pathObj = inputValues.get(INPUT_PATH_ID);
        Object defaultValueObj = inputValues.get(INPUT_DEFAULT_VALUE_ID);
        
        // 检查必要的输入是否存在
        if (targetObj != null && pathObj instanceof String) {
            String path = (String) pathObj;
            
            try {
                // 确定目标对象类型并获取NBT
                if (targetObj instanceof Object) { // 需要使用正确的Minecraft类型检查
                    /*
                    在实际实现中，需要根据Minecraft API实现以下操作:
                    1. 检查targetObj是否为方块或实体
                    2. 获取其NBT数据
                    3. 解析路径，获取指定位置的值
                    4. 确定值的类型
                    
                    CompoundTag nbt = null;
                    if (targetObj instanceof BlockEntity) {
                        nbt = ((BlockEntity) targetObj).saveWithFullMetadata();
                    } else if (targetObj instanceof Entity) {
                        nbt = ((Entity) targetObj).writeNbt(new CompoundTag());
                    }
                    
                    if (nbt != null) {
                        // 处理NBT路径以获取特定的值
                        // 简化版本 - 实际代码需要解析复杂路径如 "Items[0].id"
                        Object nbtValue = null;
                        
                        if (path.contains(".")) {
                            // 处理嵌套路径
                            String[] parts = path.split("\\.");
                            CompoundTag current = nbt;
                            
                            for (int i = 0; i < parts.length - 1; i++) {
                                String part = parts[i];
                                if (part.contains("[") && part.contains("]")) {
                                    // 处理数组索引，例如 Items[0]
                                    String name = part.substring(0, part.indexOf("["));
                                    int index = Integer.parseInt(part.substring(
                                        part.indexOf("[") + 1, part.indexOf("]")));
                                    
                                    if (current.contains(name)) {
                                        ListTag list = current.getList(name, Tag.LIST_TYPE);
                                        if (list.size() > index) {
                                            current = list.getCompound(index);
                                        } else {
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                } else if (current.contains(part)) {
                                    current = current.getCompound(part);
                                } else {
                                    break;
                                }
                            }
                            
                            String lastPart = parts[parts.length - 1];
                            if (current.contains(lastPart)) {
                                Tag tag = current.get(lastPart);
                                nbtValue = getNbtValue(tag);
                                type = getTagType(tag);
                                success = true;
                            }
                        } else {
                            // 简单路径
                            if (nbt.contains(path)) {
                                Tag tag = nbt.get(path);
                                nbtValue = getNbtValue(tag);
                                type = getTagType(tag);
                                success = true;
                            }
                        }
                        
                        if (success) {
                            value = nbtValue;
                        }
                    }
                    */
                    
                    // 模拟成功获取NBT值 (在实际实现中替换为上面的逻辑)
                    value = "示例NBT值";
                    type = "string";
                    success = true;
                }
            } catch (Exception e) {
                success = false;
                System.err.println("Error getting NBT tag: " + e.getMessage());
            }
        }
        
        // 如果获取失败并且提供了默认值，则使用默认值
        if (!success && defaultValueObj != null) {
            value = defaultValueObj;
            if (defaultValueObj instanceof String) {
                type = "string";
            } else if (defaultValueObj instanceof Number) {
                type = "number";
            } else if (defaultValueObj instanceof Boolean) {
                type = "boolean";
            } else {
                type = "compound";
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_VALUE_ID, value);
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_TYPE_ID, type);
    }
    
    /* 
    // 在实际实现中需要这些辅助方法
    private String getTagType(Tag tag) {
        if (tag instanceof CompoundTag) return "compound";
        if (tag instanceof ListTag) return "list";
        if (tag instanceof StringTag) return "string";
        if (tag instanceof IntTag) return "int";
        if (tag instanceof ByteTag) return "byte";
        if (tag instanceof LongTag) return "long";
        if (tag instanceof FloatTag) return "float";
        if (tag instanceof DoubleTag) return "double";
        if (tag instanceof ShortTag) return "short";
        if (tag instanceof ByteArrayTag) return "byte_array";
        if (tag instanceof IntArrayTag) return "int_array";
        if (tag instanceof LongArrayTag) return "long_array";
        return "unknown";
    }
    
    private Object getNbtValue(Tag tag) {
        if (tag instanceof CompoundTag) return tag;
        if (tag instanceof ListTag) return tag;
        if (tag instanceof StringTag) return ((StringTag) tag).asString();
        if (tag instanceof IntTag) return ((IntTag) tag).getInt();
        if (tag instanceof ByteTag) return ((ByteTag) tag).getByte();
        if (tag instanceof LongTag) return ((LongTag) tag).getLong();
        if (tag instanceof FloatTag) return ((FloatTag) tag).getFloat();
        if (tag instanceof DoubleTag) return ((DoubleTag) tag).getDouble();
        if (tag instanceof ShortTag) return ((ShortTag) tag).getShort();
        if (tag instanceof ByteArrayTag) return ((ByteArrayTag) tag).getByteArray();
        if (tag instanceof IntArrayTag) return ((IntArrayTag) tag).getIntArray();
        if (tag instanceof LongArrayTag) return ((LongArrayTag) tag).getLongArray();
        return null;
    }
    */
} 