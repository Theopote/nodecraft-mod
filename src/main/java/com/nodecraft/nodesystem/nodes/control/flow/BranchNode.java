package com.nodecraft.nodesystem.nodes.control.flow;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Branch 节点: 根据布尔条件选择输出路径。
 * 类似于 if-else 语句，根据条件的真假选择输出 True 分支或 False 分支的值。
 */
@NodeInfo(
    id = "control.flow.branch",
    displayName = "条件分支",
    description = "根据布尔条件选择输出路径（if/else）",
    category = "control.flow"
)
public class BranchNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_CONDITION_ID = "input_condition";
    private static final String INPUT_TRUE_VALUE_ID = "input_true_value";
    private static final String INPUT_FALSE_VALUE_ID = "input_false_value";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_RESULT_ID = "output_result";
    private static final String OUTPUT_IS_TRUE_ID = "output_is_true";

    // --- 构造函数 ---
    public BranchNode() {
        super(UUID.randomUUID(), "control.flow.branch");
        
        addInputPort(new BasePort(INPUT_CONDITION_ID, "Condition",
                "布尔条件", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_TRUE_VALUE_ID, "True Value",
                "条件为真时输出的值", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_FALSE_VALUE_ID, "False Value",
                "条件为假时输出的值", NodeDataType.ANY, this));
        
        addOutputPort(new BasePort(OUTPUT_RESULT_ID, "Result",
                "根据条件选择的输出值", NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_IS_TRUE_ID, "Is True",
                "条件是否为真", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDescription() {
        return "根据布尔条件选择输出路径（if/else）";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object conditionObj = inputValues.get(INPUT_CONDITION_ID);
        Object trueValue = inputValues.get(INPUT_TRUE_VALUE_ID);
        Object falseValue = inputValues.get(INPUT_FALSE_VALUE_ID);
        
        boolean condition = false;
        
        // 支持多种条件类型
        if (conditionObj instanceof Boolean) {
            condition = (Boolean) conditionObj;
        } else if (conditionObj instanceof Number) {
            condition = ((Number) conditionObj).doubleValue() != 0;
        } else if (conditionObj instanceof String) {
            condition = !((String) conditionObj).isEmpty();
        } else if (conditionObj != null) {
            condition = true; // 非 null 对象视为 true
        }
        
        outputValues.put(OUTPUT_IS_TRUE_ID, condition);
        
        if (condition) {
            outputValues.put(OUTPUT_RESULT_ID, trueValue);
        } else {
            outputValues.put(OUTPUT_RESULT_ID, falseValue);
        }
    }
}
