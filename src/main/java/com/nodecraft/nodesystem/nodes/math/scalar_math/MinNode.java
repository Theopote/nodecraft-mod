package com.nodecraft.nodesystem.nodes.math.scalar_math;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@NodeInfo(
    id = "math.scalar_math.min",
    displayName = "Min",
    description = "Returns the minimum of two values.",
    category = "math.scalar_math"
)
public class MinNode extends BaseNode {

    private static final String INPUT_A_ID = "input_a";
    private static final String INPUT_B_ID = "input_b";
    private static final String OUTPUT_MIN_ID = "output_min";

    public MinNode() {
        super(UUID.randomUUID(), "math.scalar_math.min");
        addInputPort(new BasePort(INPUT_A_ID, "A", "First value", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_B_ID, "B", "Second value", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_MIN_ID, "Min", "The minimum value", NodeDataType.DOUBLE, this));
    }

    @Override
    public String getDescription() {
        return "Returns the minimum of two values.";
    }

    @Override
    public String getDisplayName() {
        return "Min";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object aObj = inputValues.get(INPUT_A_ID);
        Object bObj = inputValues.get(INPUT_B_ID);
        double a = aObj instanceof Number ? ((Number) aObj).doubleValue() : 0.0;
        double b = bObj instanceof Number ? ((Number) bObj).doubleValue() : 0.0;
        outputValues.put(OUTPUT_MIN_ID, Math.min(a, b));
    }
}
