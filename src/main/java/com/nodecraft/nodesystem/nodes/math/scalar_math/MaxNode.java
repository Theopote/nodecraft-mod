package com.nodecraft.nodesystem.nodes.math.scalar_math;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@NodeInfo(
    id = "math.scalar_math.max",
    displayName = "Max",
    description = "Returns the maximum of two values.",
    category = "math.scalar_math",
    order = 9
)
public class MaxNode extends BaseNode {

    private static final String INPUT_A_ID = "input_a";
    private static final String INPUT_B_ID = "input_b";
    private static final String OUTPUT_MAX_ID = "output_max";

    public MaxNode() {
        super(UUID.randomUUID(), "math.scalar_math.max");
        addInputPort(new BasePort(INPUT_A_ID, "A", "First value", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_B_ID, "B", "Second value", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_MAX_ID, "Max", "The maximum value", NodeDataType.DOUBLE, this));
    }

    @Override
    public String getDescription() {
        return "Returns the maximum of two values.";
    }

    @Override
    public String getDisplayName() {
        return "Max";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object aObj = inputValues.get(INPUT_A_ID);
        Object bObj = inputValues.get(INPUT_B_ID);
        double a = aObj instanceof Number ? ((Number) aObj).doubleValue() : 0.0;
        double b = bObj instanceof Number ? ((Number) bObj).doubleValue() : 0.0;
        outputValues.put(OUTPUT_MAX_ID, Math.max(a, b));
    }
}
