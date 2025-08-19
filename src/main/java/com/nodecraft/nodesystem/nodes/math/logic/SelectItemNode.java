package com.nodecraft.nodesystem.nodes.math.logic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * SelectItem Node: 根据索引或条件从多个输入中选择一个
 */
@NodeInfo(
    id = "math.logic.select_item",
    displayName = "选择项目",
    description = "根据索引或条件从多个输入中选择一个",
    category = "math.logic"
)
public class SelectItemNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_INDEX_ID = "input_index";
    private static final String INPUT_ITEM_0_ID = "input_item_0";
    private static final String INPUT_ITEM_1_ID = "input_item_1";
    private static final String INPUT_ITEM_2_ID = "input_item_2";
    private static final String INPUT_ITEM_3_ID = "input_item_3";
    private static final String INPUT_DEFAULT_ID = "input_default";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_RESULT_ID = "output_result";

    // --- 构造函数 ---
    public SelectItemNode() {
        super(UUID.randomUUID(), "logic.select_item");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_INDEX_ID, "Index", "选择索引 (0-3) 或布尔条件", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_ITEM_0_ID, "Item 0", "索引为0的项目", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_ITEM_1_ID, "Item 1", "索引为1的项目", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_ITEM_2_ID, "Item 2", "索引为2的项目", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_ITEM_3_ID, "Item 3", "索引为3的项目", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_DEFAULT_ID, "Default", "索引无效时的默认值", NodeDataType.ANY, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_RESULT_ID, "Result", "选择的项目", NodeDataType.ANY, this));
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "根据索引或条件从多个输入中选择一个";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "Select Item";
    }

    // --- 核心逻辑 ---
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取索引/条件输入值
        Object indexValue = inputValues.get(INPUT_INDEX_ID);
        
        // 准备结果，默认为默认值
        Object result = inputValues.get(INPUT_DEFAULT_ID);
        
        // 处理索引选择
        if (indexValue instanceof Number) {
            int index = ((Number) indexValue).intValue();
            
            switch (index) {
                case 0:
                    result = inputValues.get(INPUT_ITEM_0_ID);
                    break;
                case 1:
                    result = inputValues.get(INPUT_ITEM_1_ID);
                    break;
                case 2:
                    result = inputValues.get(INPUT_ITEM_2_ID);
                    break;
                case 3:
                    result = inputValues.get(INPUT_ITEM_3_ID);
                    break;
                default:
                    // 索引超出范围，使用默认值
                    break;
            }
        } 
        // 处理布尔条件选择 (true 选择 Item 0，false 选择 Item 1)
        else if (indexValue instanceof Boolean) {
            boolean condition = (Boolean) indexValue;
            result = condition ? inputValues.get(INPUT_ITEM_0_ID) : inputValues.get(INPUT_ITEM_1_ID);
        }
        // 处理字符串索引 (尝试解析为数字)
        else if (indexValue instanceof String) {
            try {
                int index = Integer.parseInt((String) indexValue);
                
                switch (index) {
                    case 0:
                        result = inputValues.get(INPUT_ITEM_0_ID);
                        break;
                    case 1:
                        result = inputValues.get(INPUT_ITEM_1_ID);
                        break;
                    case 2:
                        result = inputValues.get(INPUT_ITEM_2_ID);
                        break;
                    case 3:
                        result = inputValues.get(INPUT_ITEM_3_ID);
                        break;
                    default:
                        // 索引超出范围，使用默认值
                        break;
                }
            } catch (NumberFormatException e) {
                // 无法解析为数字，尝试解析为布尔值
                if ("true".equalsIgnoreCase((String) indexValue)) {
                    result = inputValues.get(INPUT_ITEM_0_ID);
                } else if ("false".equalsIgnoreCase((String) indexValue)) {
                    result = inputValues.get(INPUT_ITEM_1_ID);
                }
                // 否则使用默认值
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_RESULT_ID, result);
    }
}