package com.nodecraft.nodesystem.nodes.utilities.flow_control;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Branch node: selects between two values based on a condition.
 * Similar to an if/else expression.
 */
@NodeInfo(
    id = "utilities.flow_control.branch",
    displayName = "Branch",
    description = "Chooses true/false branch output based on a condition (if/else).",
    category = "utilities.flow_control"
)
public class BranchNode extends BaseNode {

    // ---              ?IDs ---
    private static final String INPUT_CONDITION_ID = "input_condition";
    private static final String INPUT_TRUE_VALUE_ID = "input_true_value";
    private static final String INPUT_FALSE_VALUE_ID = "input_false_value";

    // ---              ?IDs ---
    private static final String OUTPUT_RESULT_ID = "output_result";
    private static final String OUTPUT_IS_TRUE_ID = "output_is_true";

    // ---              ?---
    public BranchNode() {
        super(UUID.randomUUID(), "utilities.flow_control.branch");
        
        addInputPort(new BasePort(INPUT_CONDITION_ID, "Condition",
            "Boolean condition input", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_TRUE_VALUE_ID, "True Value",
            "Value output when condition is true", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_FALSE_VALUE_ID, "False Value",
            "Value output when condition is false", NodeDataType.ANY, this));
        
        addOutputPort(new BasePort(OUTPUT_RESULT_ID, "Result",
            "Selected branch output value", NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_IS_TRUE_ID, "Is True",
            "Whether the condition evaluated to true", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDescription() {
        return "Chooses true/false branch output based on a condition (if/else).";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object conditionObj = inputValues.get(INPUT_CONDITION_ID);
        Object trueValue = inputValues.get(INPUT_TRUE_VALUE_ID);
        Object falseValue = inputValues.get(INPUT_FALSE_VALUE_ID);
        
        boolean condition = false;
        
        //                            ?
        if (conditionObj instanceof Boolean) {
            condition = (Boolean) conditionObj;
        } else if (conditionObj instanceof Number) {
            condition = ((Number) conditionObj).doubleValue() != 0;
        } else if (conditionObj instanceof String) {
            condition = !((String) conditionObj).isEmpty();
        } else if (conditionObj != null) {
            condition = true; //  ?null              ?true
        }
        
        outputValues.put(OUTPUT_IS_TRUE_ID, condition);
        
        if (condition) {
            outputValues.put(OUTPUT_RESULT_ID, trueValue);
        } else {
            outputValues.put(OUTPUT_RESULT_ID, falseValue);
        }
    }
}
