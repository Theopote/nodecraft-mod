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
    id = "deferred.out_of_scope.number_to_integer",
    displayName = "Number To Integer",
    description = "Converts a floating-point number to an integer using floor, round, or ceil.",
    category = "deferred.out_of_scope"
)
public class NumberToIntegerNode extends BaseNode {

    public enum RoundingMode {
        FLOOR,
        ROUND,
        CEIL
    }

    private static final String INPUT_NUMBER_ID = "input_number";
    private static final String OUTPUT_INTEGER_ID = "output_integer";

    private RoundingMode roundingMode = RoundingMode.ROUND;

    public NumberToIntegerNode() {
        super(UUID.randomUUID(), "deferred.out_of_scope.number_to_integer");

        IPort numberInput = new BasePort(INPUT_NUMBER_ID, "Number",
            "Input number to convert", NodeDataType.DOUBLE, this);
        addInputPort(numberInput);

        IPort integerOutput = new BasePort(OUTPUT_INTEGER_ID, "Integer",
            "Converted integer value", NodeDataType.INTEGER, this);
        addOutputPort(integerOutput);
    }

    @Override
    public String getDescription() {
        return "Converts a floating-point number to an integer using floor, round, or ceil.";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object numberObj = inputValues.get(INPUT_NUMBER_ID);
        int result = 0;

        if (numberObj instanceof Number number) {
            double value = number.doubleValue();
            result = switch (roundingMode) {
                case FLOOR -> (int) Math.floor(value);
                case CEIL -> (int) Math.ceil(value);
                case ROUND -> (int) Math.round(value);
            };
        }

        outputValues.put(OUTPUT_INTEGER_ID, result);
    }

    public RoundingMode getRoundingMode() {
        return roundingMode;
    }

    public void setRoundingMode(RoundingMode roundingMode) {
        this.roundingMode = roundingMode == null ? RoundingMode.ROUND : roundingMode;
        markDirty();
    }

    public void setRoundingModeString(String mode) {
        if (mode == null || mode.isBlank()) {
            setRoundingMode(RoundingMode.ROUND);
            return;
        }
        try {
            setRoundingMode(RoundingMode.valueOf(mode.trim().toUpperCase()));
        } catch (IllegalArgumentException ignored) {
            setRoundingMode(RoundingMode.ROUND);
        }
    }

    @Override
    public Object getNodeState() {
        Map<String, Object> state = new HashMap<>();
        state.put("roundingMode", roundingMode.name());
        return state;
    }

    @Override
    public void setNodeState(Object state) {
        if (state instanceof Map<?, ?> stateMap) {
            Object mode = stateMap.get("roundingMode");
            if (mode instanceof String modeString) {
                setRoundingModeString(modeString);
            }
        }
    }
}
