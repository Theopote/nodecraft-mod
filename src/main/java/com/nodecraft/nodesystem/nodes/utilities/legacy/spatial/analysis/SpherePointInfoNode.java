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
 * Legacy compatibility analysis node for classifying a point relative to a sphere surface.
 */
@NodeInfo(
    id = "spatial.analysis.sphere_point_info",
    displayName = "Sphere Point Info",
    description = "Reports surface relationship, nearest surface point, and normal for a point relative to a sphere",
    category = "spatial.analysis"
)
public class SpherePointInfoNode extends BaseNode {

    private static final double EPSILON = 1.0e-6d;

    private static final String INPUT_SPHERE_ID = "input_sphere";
    private static final String INPUT_POINT_ID = "input_point";

    private static final String OUTPUT_POINT_ID = "output_point";
    private static final String OUTPUT_CENTER_ID = "output_center";
    private static final String OUTPUT_CLOSEST_SURFACE_POINT_ID = "output_closest_surface_point";
    private static final String OUTPUT_NORMAL_ID = "output_normal";
    private static final String OUTPUT_DISTANCE_TO_CENTER_ID = "output_distance_to_center";
    private static final String OUTPUT_DISTANCE_TO_SURFACE_ID = "output_distance_to_surface";
    private static final String OUTPUT_SIGNED_DISTANCE_ID = "output_signed_distance";
    private static final String OUTPUT_IS_ON_SURFACE_ID = "output_is_on_surface";
    private static final String OUTPUT_IS_INSIDE_ID = "output_is_inside";
    private static final String OUTPUT_IS_OUTSIDE_ID = "output_is_outside";
    private static final String OUTPUT_VALID_ID = "output_valid";

    public SpherePointInfoNode() {
        super(UUID.randomUUID(), "spatial.analysis.sphere_point_info");

        addInputPort(new BasePort(INPUT_SPHERE_ID, "Sphere", "Sphere geometry to evaluate against", NodeDataType.SPHERE, this));
        addInputPort(new BasePort(INPUT_POINT_ID, "Point", "Point, vector, or block coordinate to classify", NodeDataType.ANY, this));

        addOutputPort(new BasePort(OUTPUT_POINT_ID, "Point", "Resolved input point", NodeDataType.VECTOR, this));
        addOutputPort(new BasePort(OUTPUT_CENTER_ID, "Center", "Sphere center", NodeDataType.VECTOR, this));
        addOutputPort(new BasePort(OUTPUT_CLOSEST_SURFACE_POINT_ID, "Closest Surface Point", "Nearest point on the sphere surface", NodeDataType.VECTOR, this));
        addOutputPort(new BasePort(OUTPUT_NORMAL_ID, "Normal", "Outward surface normal at the closest surface point", NodeDataType.VECTOR, this));
        addOutputPort(new BasePort(OUTPUT_DISTANCE_TO_CENTER_ID, "Distance To Center", "Distance from the point to the sphere center", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_DISTANCE_TO_SURFACE_ID, "Distance To Surface", "Unsigned distance from the point to the sphere surface", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_SIGNED_DISTANCE_ID, "Signed Distance", "Negative inside, zero on surface, positive outside", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_IS_ON_SURFACE_ID, "Is On Surface", "True when the point lies on the sphere surface", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_IS_INSIDE_ID, "Is Inside", "True when the point lies strictly inside the sphere", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_IS_OUTSIDE_ID, "Is Outside", "True when the point lies outside the sphere", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_VALID_ID, "Valid", "True when both inputs were resolved", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDescription() {
        return "Reports surface relationship, nearest surface point, and normal for a point relative to a sphere";
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
        Vector3d direction = new Vector3d(point).sub(center);
        double distanceToCenter = direction.length();
        Vector3d normal = distanceToCenter > EPSILON
            ? direction.normalize()
            : new Vector3d(0.0d, 1.0d, 0.0d);
        Vector3d closestSurfacePoint = new Vector3d(normal).mul(radius).add(center);
        double signedDistance = distanceToCenter - radius;
        double distanceToSurface = Math.abs(signedDistance);
        boolean isOnSurface = Math.abs(signedDistance) <= EPSILON;
        boolean isInside = signedDistance < -EPSILON;
        boolean isOutside = signedDistance > EPSILON;

        outputValues.put(OUTPUT_POINT_ID, point);
        outputValues.put(OUTPUT_CENTER_ID, center);
        outputValues.put(OUTPUT_CLOSEST_SURFACE_POINT_ID, closestSurfacePoint);
        outputValues.put(OUTPUT_NORMAL_ID, normal);
        outputValues.put(OUTPUT_DISTANCE_TO_CENTER_ID, distanceToCenter);
        outputValues.put(OUTPUT_DISTANCE_TO_SURFACE_ID, distanceToSurface);
        outputValues.put(OUTPUT_SIGNED_DISTANCE_ID, signedDistance);
        outputValues.put(OUTPUT_IS_ON_SURFACE_ID, isOnSurface);
        outputValues.put(OUTPUT_IS_INSIDE_ID, isInside);
        outputValues.put(OUTPUT_IS_OUTSIDE_ID, isOutside);
        outputValues.put(OUTPUT_VALID_ID, true);
    }

    private void writeEmptyOutputs() {
        outputValues.put(OUTPUT_POINT_ID, null);
        outputValues.put(OUTPUT_CENTER_ID, null);
        outputValues.put(OUTPUT_CLOSEST_SURFACE_POINT_ID, null);
        outputValues.put(OUTPUT_NORMAL_ID, null);
        outputValues.put(OUTPUT_DISTANCE_TO_CENTER_ID, 0.0d);
        outputValues.put(OUTPUT_DISTANCE_TO_SURFACE_ID, 0.0d);
        outputValues.put(OUTPUT_SIGNED_DISTANCE_ID, 0.0d);
        outputValues.put(OUTPUT_IS_ON_SURFACE_ID, false);
        outputValues.put(OUTPUT_IS_INSIDE_ID, false);
        outputValues.put(OUTPUT_IS_OUTSIDE_ID, false);
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
}
