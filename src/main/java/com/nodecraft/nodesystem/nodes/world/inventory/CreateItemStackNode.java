package com.nodecraft.nodesystem.nodes.world.inventory;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Create Item Stack 节点: 创建物品堆叠。
 */
@NodeInfo(
    id = "world.inventory.create_item_stack",
    displayName = "创建物品堆叠",
    description = "创建物品堆叠",
    category = "world.inventory"
)
public class CreateItemStackNode extends BaseNode {

    // --- 节点属性 ---
    private String description = "创建新的 ItemStack";

    // --- 输入端口 IDs ---
    private static final String INPUT_ITEM_ID = "input_item_id";
    private static final String INPUT_COUNT_ID = "input_count";
    private static final String INPUT_NBT_ID = "input_nbt";
    private static final String INPUT_DAMAGE_ID = "input_damage";
    private static final String INPUT_ENCHANTMENTS_ID = "input_enchantments";
    private static final String INPUT_CUSTOM_NAME_ID = "input_custom_name";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_ITEM_STACK_ID = "output_item_stack";
    private static final String OUTPUT_SUCCESS_ID = "output_success";

    // --- 构造函数 ---
    public CreateItemStackNode() {
        super(UUID.randomUUID(), "world.inventory.create_item_stack");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_ITEM_ID, "Item ID", 
                "物品ID，如 'minecraft:diamond_sword'", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_COUNT_ID, "Count", 
                "物品数量", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_NBT_ID, "NBT Data", 
                "物品的NBT数据", NodeDataType.NBT, this));
        addInputPort(new BasePort(INPUT_DAMAGE_ID, "Damage", 
                "物品损耗值", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_ENCHANTMENTS_ID, "Enchantments", 
                "附魔列表，格式为 {'enchantment_id': level, ...}", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_CUSTOM_NAME_ID, "Custom Name", 
                "物品自定义名称", NodeDataType.STRING, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_ITEM_STACK_ID, "ItemStack", 
                "创建的物品堆", NodeDataType.ITEM_STACK, this));
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功创建物品", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 默认输出值
        Object itemStack = null;
        boolean success = false;
        
        // 获取输入值
        Object itemIdObj = inputValues.get(INPUT_ITEM_ID);
        Object countObj = inputValues.get(INPUT_COUNT_ID);
        Object nbtObj = inputValues.get(INPUT_NBT_ID);
        Object damageObj = inputValues.get(INPUT_DAMAGE_ID);
        Object enchantmentsObj = inputValues.get(INPUT_ENCHANTMENTS_ID);
        Object customNameObj = inputValues.get(INPUT_CUSTOM_NAME_ID);
        
        // 检查必要的输入是否存在
        if (itemIdObj instanceof String) {
            String itemId = (String) itemIdObj;
            
            try {
                /*
                在实际实现中，需要根据Minecraft API实现以下操作:
                1. 创建基础ItemStack
                2. 应用NBT数据
                3. 设置附魔和自定义名称
                
                // 根据ID获取物品
                Identifier id = new Identifier(itemId);
                Item item = Registry.ITEM.get(id);
                
                // 创建初始物品堆
                int count = 1; // 默认数量
                if (countObj instanceof Number) {
                    count = Math.max(1, Math.min(64, ((Number) countObj).intValue()));
                }
                
                ItemStack stack = new ItemStack(item, count);
                
                // 设置NBT数据
                if (nbtObj instanceof CompoundTag) {
                    stack.setTag((CompoundTag) nbtObj);
                }
                
                // 设置损耗值
                if (damageObj instanceof Number && stack.isDamageable()) {
                    int damage = ((Number) damageObj).intValue();
                    stack.setDamage(damage);
                }
                
                // 设置附魔
                if (enchantmentsObj instanceof Map) {
                    Map<?, ?> enchantments = (Map<?, ?>) enchantmentsObj;
                    
                    for (Map.Entry<?, ?> entry : enchantments.entrySet()) {
                        if (entry.getKey() instanceof String && entry.getValue() instanceof Number) {
                            String enchId = (String) entry.getKey();
                            int level = ((Number) entry.getValue()).intValue();
                            
                            Identifier enchantId = new Identifier(enchId);
                            Enchantment enchantment = Registry.ENCHANTMENT.get(enchantId);
                            
                            if (enchantment != null) {
                                stack.addEnchantment(enchantment, level);
                            }
                        }
                    }
                }
                
                // 设置自定义名称
                if (customNameObj instanceof String) {
                    String customName = (String) customNameObj;
                    if (!customName.isEmpty()) {
                        stack.setCustomName(Text.of(customName));
                    }
                }
                
                itemStack = stack;
                success = true;
                */
                
                // 模拟成功创建物品堆 (在实际实现中替换为上面的逻辑)
                itemStack = new Object(); // 模拟物品堆
                success = true;
            } catch (Exception e) {
                success = false;
                System.err.println("Error creating ItemStack: " + e.getMessage());
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_ITEM_STACK_ID, itemStack);
        outputValues.put(OUTPUT_SUCCESS_ID, success);
    }
} 