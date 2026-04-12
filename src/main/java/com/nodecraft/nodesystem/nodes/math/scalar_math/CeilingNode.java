package com.nodecraft.nodesystem.nodes.math.scalar_math;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@NodeInfo(
    id = "math.scalar_math.ceiling",
    displayName = "Ceiling",
    description = "Rounds a value up to the nearest integer.",
    category = "math.scalar_math",
    order = 13
)
public class CeilingNode extends BaseNode {

    private static final String INPUT_VALUE_ID = "input_value";
    private static final String OUTPUT_CEILING_ID = "output_ceiling";

    public CeilingNode() {
        super(UUID.randomUUID(), "math.scalar_math.ceiling");
        addInputPort(new BasePort(INPUT_VALUE_ID, "Value", "Value to ceiling", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_CEILING_ID, "Ceiling", "The ceiling value", NodeDataType.DOUBLE, this));
    }

    @Override
    public String getDescription() {
        return "Rounds a value up to the nearest integer.";
    }

    @Override
    public String getDisplayName() {
        return "Ceiling";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object valueObj = inputValues.get(INPUT_VALUE_ID);
        double value = valueObj instanceof Number ? ((Number) valueObj).doubleValue() : 0.0;
        outputValues.put(OUTPUT_CEILING_ID, Math.ceil(value));
    }
}
