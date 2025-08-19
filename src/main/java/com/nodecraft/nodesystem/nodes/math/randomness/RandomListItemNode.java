package com.nodecraft.nodesystem.nodes.math.randomness;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Random List Item Node: 从列表中随机选择一个或多个项目
 */
@NodeInfo(
    id = "math.randomness.random_list_item",
    displayName = "随机列表项",
    description = "从列表中随机选择一个或多个项目",
    category = "math.randomness"
)
public class RandomListItemNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_COUNT_ID = "input_count";
    private static final String INPUT_ALLOW_DUPLICATES_ID = "input_allow_duplicates";
    private static final String INPUT_SEED_ID = "input_seed";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_ITEM_ID = "output_item";
    private static final String OUTPUT_ITEMS_ID = "output_items";

    // --- 构造函数 ---
    public RandomListItemNode() {
        super(UUID.randomUUID(), "math.randomness.random_list_item");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_LIST_ID, "List", "输入列表（可以是任何对象列表）", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_COUNT_ID, "Count", "要选择的项目数量 (默认=1)", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_ALLOW_DUPLICATES_ID, "Allow Duplicates", "是否允许重复选择同一项目", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_SEED_ID, "Seed", "随机生成器的种子（可选）", NodeDataType.INTEGER, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_ITEM_ID, "Item", "随机选择的单个项目（当Count=1时）", NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_ITEMS_ID, "Items", "随机选择的多个项目（列表）", NodeDataType.ANY, this));
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "从列表中随机选择一个或多个项目";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "Random List Item";
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object listValue = inputValues.get(INPUT_LIST_ID);
        int count = getValueAsInt(inputValues.get(INPUT_COUNT_ID), 1);
        boolean allowDuplicates = getValueAsBoolean(inputValues.get(INPUT_ALLOW_DUPLICATES_ID), false);
        Object seedValue = inputValues.get(INPUT_SEED_ID);
        
        // 检查列表输入
        if (!(listValue instanceof List)) {
            // 处理非列表输入
            if (listValue == null) {
                // 如果为null，设置空输出
                outputValues.put(OUTPUT_ITEM_ID, null);
                outputValues.put(OUTPUT_ITEMS_ID, Collections.emptyList());
                return;
            } else {
                // 将单个项目转换为只有一个元素的列表
                List<Object> singleItemList = new ArrayList<>(1);
                singleItemList.add(listValue);
                listValue = singleItemList;
            }
        }
        
        // 转换为列表
        @SuppressWarnings("unchecked")
        List<Object> inputList = (List<Object>) listValue;
        
        // 如果列表为空，提早返回
        if (inputList.isEmpty()) {
            outputValues.put(OUTPUT_ITEM_ID, null);
            outputValues.put(OUTPUT_ITEMS_ID, Collections.emptyList());
            return;
        }
        
        // 确保count值有效（不能为负数且不能超过列表大小，除非允许重复）
        count = Math.max(0, count);
        if (!allowDuplicates && count > inputList.size()) {
            count = inputList.size();
        }
        
        // 创建随机数生成器
        Random random;
        if (seedValue instanceof Number) {
            random = new Random(((Number) seedValue).longValue());
        } else {
            random = new Random();
        }
        
        // 结果存储
        Object singleItem = null;
        List<Object> selectedItems = new ArrayList<>(count);
        
        // 选择项目
        if (allowDuplicates) {
            // 允许重复：简单随机选择count次
            for (int i = 0; i < count; i++) {
                int randomIndex = random.nextInt(inputList.size());
                Object selectedItem = inputList.get(randomIndex);
                selectedItems.add(selectedItem);
                
                // 如果是第一个选择的项目，保存为单项输出
                if (i == 0) {
                    singleItem = selectedItem;
                }
            }
        } else {
            // 不允许重复：创建列表的副本并打乱顺序
            List<Object> shuffledList = new ArrayList<>(inputList);
            Collections.shuffle(shuffledList, random);
            
            // 选择前count个项目
            for (int i = 0; i < count; i++) {
                Object selectedItem = shuffledList.get(i);
                selectedItems.add(selectedItem);
                
                // 如果是第一个选择的项目，保存为单项输出
                if (i == 0) {
                    singleItem = selectedItem;
                }
            }
        }
        
        // 设置输出
        outputValues.put(OUTPUT_ITEM_ID, singleItem);
        outputValues.put(OUTPUT_ITEMS_ID, Collections.unmodifiableList(selectedItems));
    }
    
    /** Helper method to safely convert an input object to int. */
    private int getValueAsInt(Object value, int defaultValue) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
    
    /** Helper method to safely convert an input object to boolean. */
    private boolean getValueAsBoolean(Object value, boolean defaultValue) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        } else if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        return defaultValue;
    }
} 