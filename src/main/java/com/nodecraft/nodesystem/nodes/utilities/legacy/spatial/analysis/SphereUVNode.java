package com.nodecraft.nodesystem.nodes.utilities.legacy.spatial.analysis;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.PointData;
import com.nodecraft.nodesystem.datatypes.SphereData;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.UUID;

/**
 * Legacy compatibility analysis node for sphere parameterization queries.
 */
@NodeInfo(
    id = "spatial.analysis.sphere_uv",
    displayName = "Sphere UV",
    description = "Projects a point onto a sphere and reports UV, longitude, and latitude parameters",
    category = "utilities.legacy.spatial.analysis"
)
public class SphereUVNode extends BaseNode {

    private static final double EPSILON = 1.0e-9d;

    private static final String INPUT_SPHERE_ID = "input_sphere";
    private static final String INPUT_POINT_ID = "input_point";

    private static final String OUTPUT_SURFACE_POINT_ID = "output_surface_point";
    private static final String OUTPUT_NORMAL_ID = "output_normal";
    private static final String OUTPUT_U_ID = "output_u";
    private static final String OUTPUT_V_ID = "output_v";
    private static final String OUTPUT_LONGITUDE_RAD_ID = "output_longitude_rad";
    private static final String OUTPUT_LATITUDE_RAD_ID = "output_latitude_rad";
    private static final String OUTPUT_LONGITUDE_DEG_ID = "output_longitude_deg";
    private static final String OUTPUT_LATITUDE_DEG_ID = "output_latitude_deg";
    private static final String OUTPUT_VALID_ID = "output_valid";

    public SphereUVNode() {
        super(UUID.randomUUID(), "spatial.analysis.sphere_uv");

        addInputPort(new BasePort(INPUT_SPHERE_ID, "Sphere", "Sphere geometry to parameterize", NodeDataType.SPHERE, this));
        addInputPort(new BasePort(INPUT_POINT_ID, "Point", "Reference point near or on the sphere surface", NodeDataType.ANY, this));

        addOutputPort(new BasePort(OUTPUT_SURFACE_POINT_ID, "Surface Point", "Projected point on the sphere surface", NodeDataType.VECTOR, this));
        addOutputPort(new BasePort(OUTPUT_NORMAL_ID, "Normal", "Outward surface normal at the projected point", NodeDataType.VECTOR, this));
        addOutputPort(new BasePort(OUTPUT_U_ID, "U", "Normalized horizontal sphere coordinate in [0, 1]", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_V_ID, "V", "Normalized vertical sphere coordinate in [0, 1]", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_LONGITUDE_RAD_ID, "Longitude (rad)", "Longitude angle around the sphere in radians", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_LATITUDE_RAD_ID, "Latitude (rad)", "Latitude angle from equator in radians", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_LONGITUDE_DEG_ID, "Longitude (deg)", "Longitude angle around the sphere in degrees", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_LATITUDE_DEG_ID, "Latitude (deg)", "Latitude angle from equator in degrees", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_VALID_ID, "Valid", "True when both inputs were resolved", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDescription() {
        return "Projects a point onto a sphere and reports UV, longitude, and latitude parameters";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object sphereObj = inputValues.get(INPUT_SPHERE_ID);
        Vector3d point = resolvePoint(inputValues.get(INPUT_POINT_ID));

        if (!(sphereObj instanceof SphereData sphere) || point == null) {
            writeEmptyOutputs();
            return;
        }

        Vector3d center = sphere.getCenter();
        double radius = sphere.getRadius();
        Vector3d radial = new Vector3d(point).sub(center);
        Vector3d normal = radial.lengthSquared() > EPSILON
            ? radial.normalize()
            : new Vector3d(0.0d, 1.0d, 0.0d);
        Vector3d surfacePoint = new Vector3d(normal).mul(radius).add(center);

        double longitude = Math.atan2(normal.z, normal.x);
        double latitude = Math.asin(clamp(normal.y, -1.0d, 1.0d));
        double u = (longitude + Math.PI) / (Math.PI * 2.0d);
        double v = (Math.PI / 2.0d - latitude) / Math.PI;

        outputValues.put(OUTPUT_SURFACE_POINT_ID, surfacePoint);
        outputValues.put(OUTPUT_NORMAL_ID, normal);
        outputValues.put(OUTPUT_U_ID, clamp(u, 0.0d, 1.0d));
        outputValues.put(OUTPUT_V_ID, clamp(v, 0.0d, 1.0d));
        outputValues.put(OUTPUT_LONGITUDE_RAD_ID, longitude);
        outputValues.put(OUTPUT_LATITUDE_RAD_ID, latitude);
        outputValues.put(OUTPUT_LONGITUDE_DEG_ID, Math.toDegrees(longitude));
        outputValues.put(OUTPUT_LATITUDE_DEG_ID, Math.toDegrees(latitude));
        outputValues.put(OUTPUT_VALID_ID, true);
    }

    private void writeEmptyOutputs() {
        outputValues.put(OUTPUT_SURFACE_POINT_ID, null);
        outputValues.put(OUTPUT_NORMAL_ID, null);
        outputValues.put(OUTPUT_U_ID, 0.0d);
        outputValues.put(OUTPUT_V_ID, 0.0d);
        outputValues.put(OUTPUT_LONGITUDE_RAD_ID, 0.0d);
        outputValues.put(OUTPUT_LATITUDE_RAD_ID, 0.0d);
        outputValues.put(OUTPUT_LONGITUDE_DEG_ID, 0.0d);
        outputValues.put(OUTPUT_LATITUDE_DEG_ID, 0.0d);
        outputValues.put(OUTPUT_VALID_ID, false);
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

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
