package com.nodecraft.nodesystem.nodes.world.terrain;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.NodeProperty;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.ScalarFieldData;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@NodeInfo(
    id = "world.terrain.deposition_step",
    displayName = "Deposition Step",
    description = "Deposits sediment in low-slope and low-energy zones.",
    category = "world.terrain",
    order = 11
)
public class DepositionStepNode extends BaseNode {

    private static final String INPUT_HEIGHT_FIELD_ID = "input_height_field";
    private static final String INPUT_SEDIMENT_FIELD_ID = "input_sediment_field";
    private static final String INPUT_SLOPE_FIELD_ID = "input_slope_field";
    private static final String INPUT_RATE_ID = "input_rate";

    private static final String OUTPUT_HEIGHT_FIELD_ID = "output_height_field";

    @NodeProperty(displayName = "Rate", category = "Deposition", order = 1)
    private double rate = 0.1d;

    public DepositionStepNode() {
        super(UUID.randomUUID(), "world.terrain.deposition_step");

        addInputPort(new BasePort(INPUT_HEIGHT_FIELD_ID, "Height Field", "Current terrain height field", NodeDataType.SCALAR_FIELD, this));
        addInputPort(new BasePort(INPUT_SEDIMENT_FIELD_ID, "Sediment Field", "Transported sediment load", NodeDataType.SCALAR_FIELD, this));
        addInputPort(new BasePort(INPUT_SLOPE_FIELD_ID, "Slope Field", "Slope magnitude field", NodeDataType.SCALAR_FIELD, this));
        addInputPort(new BasePort(INPUT_RATE_ID, "Rate", "Single-step deposition strength", NodeDataType.DOUBLE, this));

        addOutputPort(new BasePort(OUTPUT_HEIGHT_FIELD_ID, "Height Field", "Height field after deposition", NodeDataType.SCALAR_FIELD, this));
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object heightObj = inputValues.get(INPUT_HEIGHT_FIELD_ID);
        Object sedimentObj = inputValues.get(INPUT_SEDIMENT_FIELD_ID);
        Object slopeObj = inputValues.get(INPUT_SLOPE_FIELD_ID);

        if (!(heightObj instanceof ScalarFieldData heightField)
            || !(sedimentObj instanceof ScalarFieldData sedimentField)
            || !(slopeObj instanceof ScalarFieldData slopeField)) {
            outputValues.put(OUTPUT_HEIGHT_FIELD_ID, null);
            return;
        }

        double resolvedRate = clamp01(getInputDouble(INPUT_RATE_ID, rate));

        ScalarFieldData depositedField = point -> {
            double baseHeight = heightField.sampleScalar(point);
            double sediment = Math.max(0.0d, sedimentField.sampleScalar(point));
            double slope = Math.max(0.0d, slopeField.sampleScalar(point));

            double settleFactor = 1.0d / (1.0d + slope * 2.5d);
            double deposition = sediment * settleFactor * resolvedRate;
            return baseHeight + deposition;
        };

        outputValues.put(OUTPUT_HEIGHT_FIELD_ID, depositedField);
    }

    private double getInputDouble(String portId, double fallback) {
        Object value = inputValues.get(portId);
        return value instanceof Number number ? number.doubleValue() : fallback;
    }

    private double clamp01(double value) {
        return Math.max(0.0d, Math.min(1.0d, value));
    }
}
