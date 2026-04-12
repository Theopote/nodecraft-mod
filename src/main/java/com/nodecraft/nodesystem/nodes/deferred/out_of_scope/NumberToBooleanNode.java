package com.nodecraft.nodesystem.nodes.deferred.out_of_scope;

import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NodeInfo(
    id = "deferred.out_of_scope.number_to_boolean",
    displayName = "Number To Boolean",
    description = "Converts a number to boolean where zero is false and non-zero is true.",
    category = "deferred.out_of_scope"
)
public class NumberToBooleanNode extends BaseNode {

    private static final String INPUT_NUMBER_ID = "input_number";
    private static final String OUTPUT_BOOLEAN_ID = "output_boolean";

    private boolean invertResult = false;

    public NumberToBooleanNode() {
        super(UUID.randomUUID(), "deferred.out_of_scope.number_to_boolean");

        IPort numberInput = new BasePort(INPUT_NUMBER_ID, "Number",
            "Input number to evaluate", NodeDataType.DOUBLE, this);
        addInputPort(numberInput);

        IPort booleanOutput = new BasePort(OUTPUT_BOOLEAN_ID, "Boolean",
            "Converted boolean value", NodeDataType.BOOLEAN, this);
        addOutputPort(booleanOutput);
    }

    @Override
    public String getDescription() {
        return "Converts a number to boolean where zero is false and non-zero is true.";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object numberObj = inputValues.get(INPUT_NUMBER_ID);
        boolean result = false;

        if (numberObj instanceof Number number) {
            result = number.doubleValue() != 0.0;
        }

        if (invertResult) {
            result = !result;
        }

        outputValues.put(OUTPUT_BOOLEAN_ID, result);
    }

    public boolean isInvertResult() {
        return invertResult;
    }

    public void setInvertResult(boolean invertResult) {
        this.invertResult = invertResult;
        markDirty();
    }

    @Override
    public Object getNodeState() {
        Map<String, Object> state = new HashMap<>();
        state.put("invertResult", invertResult);
        return state;
    }

    @Override
    public void setNodeState(Object state) {
        if (state instanceof Map<?, ?> stateMap) {
            Object invertObj = stateMap.get("invertResult");
            if (invertObj instanceof Boolean invert) {
                setInvertResult(invert);
            }
        }
    }
}
