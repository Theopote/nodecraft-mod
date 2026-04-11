package com.nodecraft.nodesystem.nodes.utilities.flow_control;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Switch node: selects one value from multiple inputs by index.
 * Similar to a switch/case expression.
 */
@NodeInfo(
    id = "utilities.flow_control.switch_select",
    displayName = "Switch",
    description = "Selects one of multiple inputs by index (switch/case).",
    category = "utilities.flow_control"
)
public class SwitchNode extends BaseNode {

    // ---              ?IDs ---
    private static final String INPUT_INDEX_ID = "input_index";
    private static final String INPUT_VALUE_0_ID = "input_value_0";
    private static final String INPUT_VALUE_1_ID = "input_value_1";
    private static final String INPUT_VALUE_2_ID = "input_value_2";
    private static final String INPUT_VALUE_3_ID = "input_value_3";
    private static final String INPUT_DEFAULT_ID = "input_default";

    // ---              ?IDs ---
    private static final String OUTPUT_RESULT_ID = "output_result";
    private static final String OUTPUT_MATCHED_INDEX_ID = "output_matched_index";

    // ---              ?---
    public SwitchNode() {
        super(UUID.randomUUID(), "utilities.flow_control.switch_select");
        
        addInputPort(new BasePort(INPUT_INDEX_ID, "Index",
            "Selection index (0-3)", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_VALUE_0_ID, "Value 0",
            "Value when index is 0", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_VALUE_1_ID, "Value 1",
            "Value when index is 1", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_VALUE_2_ID, "Value 2",
            "Value when index is 2", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_VALUE_3_ID, "Value 3",
            "Value when index is 3", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_DEFAULT_ID, "Default",
            "Fallback value when index is out of range", NodeDataType.ANY, this));
        
        addOutputPort(new BasePort(OUTPUT_RESULT_ID, "Result",
            "Selected output value", NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_MATCHED_INDEX_ID, "Matched Index",
            "Matched input index, -1 when default is used", NodeDataType.INTEGER, this));
    }

    @Override
    public String getDescription() {
        return "Selects one of multiple inputs by index (switch/case).";
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
            //                            ll                    ?
            if (result == null) {
                result = inputValues.get(INPUT_DEFAULT_ID);
                matchedIndex = -1;
            }
        } else {
            //                                         ?
            result = inputValues.get(INPUT_DEFAULT_ID);
            matchedIndex = -1;
        }
        
        outputValues.put(OUTPUT_RESULT_ID, result);
        outputValues.put(OUTPUT_MATCHED_INDEX_ID, matchedIndex);
    }
}
