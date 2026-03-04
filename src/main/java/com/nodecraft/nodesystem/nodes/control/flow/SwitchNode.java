package com.nodecraft.nodesystem.nodes.control.flow;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Switch 节点: 多路分支选择器。
 * 根据输入的索引值，从多个输入中选择一个作为输出。类似于 switch-case 语句。
 */
@NodeInfo(
    id = "control.flow.switch_select",
    displayName = "选择器",
    description = "根据索引选择多路输入之一（switch/case）",
    category = "control.flow"
)
public class SwitchNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_INDEX_ID = "input_index";
    private static final String INPUT_VALUE_0_ID = "input_value_0";
    private static final String INPUT_VALUE_1_ID = "input_value_1";
    private static final String INPUT_VALUE_2_ID = "input_value_2";
    private static final String INPUT_VALUE_3_ID = "input_value_3";
    private static final String INPUT_DEFAULT_ID = "input_default";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_RESULT_ID = "output_result";
    private static final String OUTPUT_MATCHED_INDEX_ID = "output_matched_index";

    // --- 构造函数 ---
    public SwitchNode() {
        super(UUID.randomUUID(), "control.flow.switch_select");
        
        addInputPort(new BasePort(INPUT_INDEX_ID, "Index",
                "选择索引（0-3）", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_VALUE_0_ID, "Value 0",
                "索引为0时的值", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_VALUE_1_ID, "Value 1",
                "索引为1时的值", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_VALUE_2_ID, "Value 2",
                "索引为2时的值", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_VALUE_3_ID, "Value 3",
                "索引为3时的值", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_DEFAULT_ID, "Default",
                "索引超出范围时的默认值", NodeDataType.ANY, this));
        
        addOutputPort(new BasePort(OUTPUT_RESULT_ID, "Result",
                "选中的值", NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_MATCHED_INDEX_ID, "Matched Index",
                "实际匹配的索引", NodeDataType.INTEGER, this));
    }

    @Override
    public String getDescription() {
        return "根据索引选择多路输入之一（switch/case）";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object indexObj = inputValues.get(INPUT_INDEX_ID);
        
        int index = 0;
        if (indexObj instanceof Number) {
            index = ((Number) indexObj).intValue();
        }
        
        String[] valueKeys = {INPUT_VALUE_0_ID, INPUT_VALUE_1_ID, INPUT_VALUE_2_ID, INPUT_VALUE_3_ID};
        
        Object result;
        int matchedIndex;
        
        if (index >= 0 && index < valueKeys.length) {
            result = inputValues.get(valueKeys[index]);
            matchedIndex = index;
            // 如果选中的值为null，使用默认值
            if (result == null) {
                result = inputValues.get(INPUT_DEFAULT_ID);
                matchedIndex = -1;
            }
        } else {
            // 索引超出范围，使用默认值
            result = inputValues.get(INPUT_DEFAULT_ID);
            matchedIndex = -1;
        }
        
        outputValues.put(OUTPUT_RESULT_ID, result);
        outputValues.put(OUTPUT_MATCHED_INDEX_ID, matchedIndex);
    }
}
