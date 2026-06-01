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
    id = "world.terrain.hydraulic_erosion_step",
    displayName = "Hydraulic Erosion Step",
    description = "Applies one hydraulic erosion step using flow accumulation as transport energy.",
    category = "world.terrain",
    order = 10
)
public class HydraulicErosionStepNode extends BaseNode {

    private static final String INPUT_HEIGHT_FIELD_ID = "input_height_field";
    private static final String INPUT_ACCUMULATION_FIELD_ID = "input_accumulation_field";
    private static final String INPUT_EROSION_RATE_ID = "input_erosion_rate";
    private static final String INPUT_CAPACITY_ID = "input_capacity";

    private static final String OUTPUT_ERODED_FIELD_ID = "output_eroded_field";
    private static final String OUTPUT_SEDIMENT_FIELD_ID = "output_sediment_field";

    @NodeProperty(displayName = "Erosion Rate", category = "Hydraulic", order = 1)
    private double erosionRate = 0.08d;

    @NodeProperty(displayName = "Capacity", category = "Hydraulic", order = 2)
    private double capacity = 1.0d;

    public HydraulicErosionStepNode() {
        super(UUID.randomUUID(), "world.terrain.hydraulic_erosion_step");

        addInputPort(new BasePort(INPUT_HEIGHT_FIELD_ID, "Height Field", "Input elevation field", NodeDataType.SCALAR_FIELD, this));
        addInputPort(new BasePort(INPUT_ACCUMULATION_FIELD_ID, "Accumulation Field", "Flow accumulation or runoff energy", NodeDataType.SCALAR_FIELD, this));
        addInputPort(new BasePort(INPUT_EROSION_RATE_ID, "Erosion Rate", "Single-step hydraulic incision amount", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_CAPACITY_ID, "Capacity", "Sediment carrying capacity scaling", NodeDataType.DOUBLE, this));

        addOutputPort(new BasePort(OUTPUT_ERODED_FIELD_ID, "Eroded Field", "Hydraulically eroded height field", NodeDataType.SCALAR_FIELD, this));
        addOutputPort(new BasePort(OUTPUT_SEDIMENT_FIELD_ID, "Sediment Field", "Estimated transported sediment field", NodeDataType.SCALAR_FIELD, this));
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object heightObj = inputValues.get(INPUT_HEIGHT_FIELD_ID);
        Object accumulationObj = inputValues.get(INPUT_ACCUMULATION_FIELD_ID);
        if (!(heightObj instanceof ScalarFieldData heightField) || !(accumulationObj instanceof ScalarFieldData accumulationField)) {
            outputValues.put(OUTPUT_ERODED_FIELD_ID, null);
            outputValues.put(OUTPUT_SEDIMENT_FIELD_ID, null);
            return;
        }

        double resolvedErosionRate = clamp01(getInputDouble(INPUT_EROSION_RATE_ID, erosionRate));
        double resolvedCapacity = Math.max(0.0d, getInputDouble(INPUT_CAPACITY_ID, capacity));

        ScalarFieldData sedimentField = point -> {
            double flowEnergy = Math.max(0.0d, accumulationField.sampleScalar(point));
            return flowEnergy * resolvedCapacity;
        };

        ScalarFieldData erodedField = point -> {
            double baseHeight = heightField.sampleScalar(point);
            double sediment = sedimentField.sampleScalar(point);
            double incision = sediment * resolvedErosionRate;
            return baseHeight - incision;
        };

        outputValues.put(OUTPUT_ERODED_FIELD_ID, erodedField);
        outputValues.put(OUTPUT_SEDIMENT_FIELD_ID, sedimentField);
    }

    private double getInputDouble(String portId, double fallback) {
        Object value = inputValues.get(portId);
        return value instanceof Number number ? number.doubleValue() : fallback;
    }

    private double clamp01(double value) {
        return Math.max(0.0d, Math.min(1.0d, value));
    }
}
