package com.nodecraft.nodesystem.nodes.control.flow;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.CompositeGeometryData;
import com.nodecraft.nodesystem.datatypes.GeometryData;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Merges multiple geometry inputs into one unified geometry value.
 */
@NodeInfo(
    id = "control.flow.geometry_merge",
    displayName = "Geometry Merge",
    description = "Combines multiple geometry inputs into a single geometry value",
    category = "control.flow"
)
public class GeometryMergeNode extends BaseNode {

    private static final String INPUT_GEOMETRY_0_ID = "input_geometry_0";
    private static final String INPUT_GEOMETRY_1_ID = "input_geometry_1";
    private static final String INPUT_GEOMETRY_2_ID = "input_geometry_2";
    private static final String INPUT_GEOMETRY_3_ID = "input_geometry_3";

    private static final String OUTPUT_GEOMETRY_ID = "output_geometry";
    private static final String OUTPUT_COUNT_ID = "output_count";
    private static final String OUTPUT_HAS_GEOMETRY_ID = "output_has_geometry";

    public GeometryMergeNode() {
        super(UUID.randomUUID(), "control.flow.geometry_merge");

        addInputPort(new BasePort(INPUT_GEOMETRY_0_ID, "Geometry 0", "First geometry input", NodeDataType.GEOMETRY, this));
        addInputPort(new BasePort(INPUT_GEOMETRY_1_ID, "Geometry 1", "Second geometry input", NodeDataType.GEOMETRY, this));
        addInputPort(new BasePort(INPUT_GEOMETRY_2_ID, "Geometry 2", "Third geometry input", NodeDataType.GEOMETRY, this));
        addInputPort(new BasePort(INPUT_GEOMETRY_3_ID, "Geometry 3", "Fourth geometry input", NodeDataType.GEOMETRY, this));

        addOutputPort(new BasePort(OUTPUT_GEOMETRY_ID, "Geometry", "Merged geometry output", NodeDataType.GEOMETRY, this));
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Count", "Number of merged geometry items", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_HAS_GEOMETRY_ID, "Has Geometry", "Whether any geometry was merged", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDescription() {
        return "Combines multiple geometry inputs into a single geometry value";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        List<GeometryData> geometries = new ArrayList<>();
        collectGeometry(geometries, inputValues.get(INPUT_GEOMETRY_0_ID));
        collectGeometry(geometries, inputValues.get(INPUT_GEOMETRY_1_ID));
        collectGeometry(geometries, inputValues.get(INPUT_GEOMETRY_2_ID));
        collectGeometry(geometries, inputValues.get(INPUT_GEOMETRY_3_ID));

        GeometryData result = null;
        if (geometries.size() == 1) {
            result = geometries.get(0);
        } else if (!geometries.isEmpty()) {
            result = new CompositeGeometryData(geometries);
        }

        outputValues.put(OUTPUT_GEOMETRY_ID, result);
        outputValues.put(OUTPUT_COUNT_ID, geometries.size());
        outputValues.put(OUTPUT_HAS_GEOMETRY_ID, !geometries.isEmpty());
    }

    private void collectGeometry(List<GeometryData> geometries, Object value) {
        if (value instanceof GeometryData geometry) {
            geometries.add(geometry);
        }
    }
}
