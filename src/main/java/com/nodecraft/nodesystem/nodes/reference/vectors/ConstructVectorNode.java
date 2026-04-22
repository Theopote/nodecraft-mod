package com.nodecraft.nodesystem.nodes.reference.vectors;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.UUID;

@NodeInfo(
    id = "reference.vectors.construct_vector",
    displayName = "Construct Vector",
    description = "Builds a vector from X, Y, Z numeric inputs.",
    category = "reference.vectors",
    order = 1
)
public class ConstructVectorNode extends BaseNode {

    private static final String INPUT_X_ID = "input_x";
    private static final String INPUT_Y_ID = "input_y";
    private static final String INPUT_Z_ID = "input_z";

    private static final String OUTPUT_VECTOR_ID = "output_vector";
    private static final String OUTPUT_X_ID = "output_x";
    private static final String OUTPUT_Y_ID = "output_y";
    private static final String OUTPUT_Z_ID = "output_z";

    public ConstructVectorNode() {
        super(UUID.randomUUID(), "reference.vectors.construct_vector");

        addInputPort(new BasePort(INPUT_X_ID, "X", "X component input", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_Y_ID, "Y", "Y component input", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_Z_ID, "Z", "Z component input", NodeDataType.DOUBLE, this));

        addOutputPort(new BasePort(OUTPUT_VECTOR_ID, "Vector", "Constructed vector", NodeDataType.VECTOR, this));
        addOutputPort(new BasePort(OUTPUT_X_ID, "X", "X component output", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_Y_ID, "Y", "Y component output", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_Z_ID, "Z", "Z component output", NodeDataType.DOUBLE, this));
    }

    @Override
    public String getDescription() {
        return "Builds a vector from X, Y, Z inputs and outputs Vector / X / Y / Z.";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        double x = toDouble(inputValues.get(INPUT_X_ID));
        double y = toDouble(inputValues.get(INPUT_Y_ID));
        double z = toDouble(inputValues.get(INPUT_Z_ID));

        outputValues.put(OUTPUT_VECTOR_ID, new Vector3d(x, y, z));
        outputValues.put(OUTPUT_X_ID, x);
        outputValues.put(OUTPUT_Y_ID, y);
        outputValues.put(OUTPUT_Z_ID, z);
    }

    private double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return 0.0d;
    }
}
