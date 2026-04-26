package com.nodecraft.nodesystem.nodes.geometry.boolops;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.GeometryData;
import com.nodecraft.nodesystem.datatypes.PointData;
import com.nodecraft.nodesystem.datatypes.SdfGeometryData;
import com.nodecraft.nodesystem.datatypes.SignedDistanceFieldData;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.UUID;

@NodeInfo(
    id = "geometry.boolean.sdf_to_geometry",
    displayName = "SDF To Geometry",
    description = "Wraps an SDF into GeometryData with explicit sampling bounds so it can be baked to blocks",
    category = "geometry.boolean",
    order = 26
)
public class SdfToGeometryNode extends BaseNode {
    private static final String INPUT_SDF_ID = "input_sdf";
    private static final String INPUT_MIN_ID = "input_min";
    private static final String INPUT_MAX_ID = "input_max";
    private static final String INPUT_ISO_ID = "input_iso";

    private static final String OUTPUT_GEOMETRY_ID = "output_geometry";
    private static final String OUTPUT_VALID_ID = "output_valid";

    public SdfToGeometryNode() {
        super(UUID.randomUUID(), "geometry.boolean.sdf_to_geometry");
        addInputPort(new BasePort(INPUT_SDF_ID, "SDF", "Signed distance field to wrap", NodeDataType.SDF, this));
        addInputPort(new BasePort(INPUT_MIN_ID, "Bounds Min", "Sampling minimum corner", NodeDataType.POINT, this));
        addInputPort(new BasePort(INPUT_MAX_ID, "Bounds Max", "Sampling maximum corner", NodeDataType.POINT, this));
        addInputPort(new BasePort(INPUT_ISO_ID, "Iso Value", "Iso-surface threshold (0 is standard SDF surface)", NodeDataType.DOUBLE, this));

        addOutputPort(new BasePort(OUTPUT_GEOMETRY_ID, "Geometry", "Geometry wrapper consumable by geometry voxelizer", NodeDataType.GEOMETRY, this));
        addOutputPort(new BasePort(OUTPUT_VALID_ID, "Valid", "True when SDF and bounds are valid", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDescription() {
        return "Wraps an SDF into GeometryData with explicit sampling bounds so it can be baked to blocks";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object sdfObj = inputValues.get(INPUT_SDF_ID);
        Vector3d min = resolvePoint(inputValues.get(INPUT_MIN_ID));
        Vector3d max = resolvePoint(inputValues.get(INPUT_MAX_ID));
        double iso = inputValues.get(INPUT_ISO_ID) instanceof Number n ? n.doubleValue() : 0.0d;

        if (!(sdfObj instanceof SignedDistanceFieldData sdf) || min == null || max == null
            || min.x > max.x || min.y > max.y || min.z > max.z) {
            outputValues.put(OUTPUT_GEOMETRY_ID, null);
            outputValues.put(OUTPUT_VALID_ID, false);
            return;
        }

        GeometryData geometry = new SdfGeometryData(sdf, min, max, iso);
        outputValues.put(OUTPUT_GEOMETRY_ID, geometry);
        outputValues.put(OUTPUT_VALID_ID, true);
    }

    private Vector3d resolvePoint(Object value) {
        if (value instanceof PointData pointData) {
            return pointData.getPosition();
        }
        if (value instanceof Vector3d vector) {
            return new Vector3d(vector);
        }
        if (value instanceof BlockPos blockPos) {
            return new Vector3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }
        return null;
    }
}
