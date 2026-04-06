package com.nodecraft.nodesystem.nodes.spatial.analysis;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.NodeProperty;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.PointData;
import com.nodecraft.nodesystem.datatypes.SphereData;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@NodeInfo(
    id = "spatial.analysis.select_sphere_band_sector",
    displayName = "Select Sphere Band Sector",
    description = "Filters sphere surface points by latitude band and longitude sector while preserving point-normal pairing",
    category = "spatial.analysis"
)
public class SelectSphereBandSectorNode extends BaseNode {

    private static final double EPSILON = 1.0e-9d;

    @NodeProperty(displayName = "Min Latitude", category = "Latitude", order = 1)
    private double minLatitudeDeg = -90.0d;

    @NodeProperty(displayName = "Max Latitude", category = "Latitude", order = 2)
    private double maxLatitudeDeg = 90.0d;

    @NodeProperty(displayName = "Min Longitude", category = "Longitude", order = 3)
    private double minLongitudeDeg = -180.0d;

    @NodeProperty(displayName = "Max Longitude", category = "Longitude", order = 4)
    private double maxLongitudeDeg = 180.0d;

    @NodeProperty(displayName = "Wrap Longitude", category = "Longitude", order = 5)
    private boolean wrapLongitude = false;

    @NodeProperty(displayName = "Invert Selection", category = "Selection", order = 6)
    private boolean invertSelection = false;

    private static final String INPUT_SPHERE_ID = "input_sphere";
    private static final String INPUT_POINTS_ID = "input_points";
    private static final String INPUT_NORMALS_ID = "input_normals";
    private static final String INPUT_MIN_LATITUDE_ID = "input_min_latitude";
    private static final String INPUT_MAX_LATITUDE_ID = "input_max_latitude";
    private static final String INPUT_MIN_LONGITUDE_ID = "input_min_longitude";
    private static final String INPUT_MAX_LONGITUDE_ID = "input_max_longitude";

    private static final String OUTPUT_POINTS_ID = "output_points";
    private static final String OUTPUT_NORMALS_ID = "output_normals";
    private static final String OUTPUT_REJECTED_POINTS_ID = "output_rejected_points";
    private static final String OUTPUT_REJECTED_NORMALS_ID = "output_rejected_normals";
    private static final String OUTPUT_MASK_ID = "output_mask";
    private static final String OUTPUT_COUNT_ID = "output_count";
    private static final String OUTPUT_REJECTED_COUNT_ID = "output_rejected_count";
    private static final String OUTPUT_VALID_ID = "output_valid";

    public SelectSphereBandSectorNode() {
        super(UUID.randomUUID(), "spatial.analysis.select_sphere_band_sector");

        addInputPort(new BasePort(INPUT_SPHERE_ID, "Sphere", "Sphere geometry used for spherical coordinates", NodeDataType.SPHERE, this));
        addInputPort(new BasePort(INPUT_POINTS_ID, "Points", "Sphere surface points to filter", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_NORMALS_ID, "Normals", "Optional normals aligned by point index", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_MIN_LATITUDE_ID, "Min Latitude", "Optional minimum latitude in degrees", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_MAX_LATITUDE_ID, "Max Latitude", "Optional maximum latitude in degrees", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_MIN_LONGITUDE_ID, "Min Longitude", "Optional minimum longitude in degrees", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_MAX_LONGITUDE_ID, "Max Longitude", "Optional maximum longitude in degrees", NodeDataType.DOUBLE, this));

        addOutputPort(new BasePort(OUTPUT_POINTS_ID, "Points", "Selected points inside the spherical selection", NodeDataType.VECTOR_LIST, this));
        addOutputPort(new BasePort(OUTPUT_NORMALS_ID, "Normals", "Selected normals aligned with the filtered points", NodeDataType.VECTOR_LIST, this));
        addOutputPort(new BasePort(OUTPUT_REJECTED_POINTS_ID, "Rejected Points", "Points outside the spherical selection", NodeDataType.VECTOR_LIST, this));
        addOutputPort(new BasePort(OUTPUT_REJECTED_NORMALS_ID, "Rejected Normals", "Normals aligned with rejected points", NodeDataType.VECTOR_LIST, this));
        addOutputPort(new BasePort(OUTPUT_MASK_ID, "Mask", "Boolean mask aligned with the input points", NodeDataType.LIST, this));
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Count", "Number of selected points", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_REJECTED_COUNT_ID, "Rejected Count", "Number of rejected points", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_VALID_ID, "Valid", "True when the sphere and point list were resolved", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDescription() {
        return "Filters sphere surface points by latitude band and longitude sector while preserving point-normal pairing";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object sphereObj = inputValues.get(INPUT_SPHERE_ID);
        Object pointsObj = inputValues.get(INPUT_POINTS_ID);
        Object normalsObj = inputValues.get(INPUT_NORMALS_ID);

        if (!(sphereObj instanceof SphereData sphere) || !(pointsObj instanceof List<?> pointsInput)) {
            writeEmptyOutputs();
            return;
        }

        List<?> normalsInput = normalsObj instanceof List<?> list ? list : List.of();

        double resolvedMinLatitude = resolveDouble(inputValues.get(INPUT_MIN_LATITUDE_ID), minLatitudeDeg);
        double resolvedMaxLatitude = resolveDouble(inputValues.get(INPUT_MAX_LATITUDE_ID), maxLatitudeDeg);
        double resolvedMinLongitude = resolveDouble(inputValues.get(INPUT_MIN_LONGITUDE_ID), minLongitudeDeg);
        double resolvedMaxLongitude = resolveDouble(inputValues.get(INPUT_MAX_LONGITUDE_ID), maxLongitudeDeg);

        double clampedMinLatitude = clamp(Math.min(resolvedMinLatitude, resolvedMaxLatitude), -90.0d, 90.0d);
        double clampedMaxLatitude = clamp(Math.max(resolvedMinLatitude, resolvedMaxLatitude), -90.0d, 90.0d);
        double normalizedMinLongitude = normalizeLongitude(resolvedMinLongitude);
        double normalizedMaxLongitude = normalizeLongitude(resolvedMaxLongitude);

        Vector3d center = sphere.getCenter();
        List<Vector3d> selectedPoints = new ArrayList<>();
        List<Vector3d> selectedNormals = new ArrayList<>();
        List<Vector3d> rejectedPoints = new ArrayList<>();
        List<Vector3d> rejectedNormals = new ArrayList<>();
        List<Boolean> mask = new ArrayList<>(pointsInput.size());

        for (int i = 0; i < pointsInput.size(); i++) {
            Vector3d point = resolvePoint(pointsInput.get(i));
            Vector3d normal = i < normalsInput.size() ? resolvePoint(normalsInput.get(i)) : null;

            if (point == null) {
                mask.add(false);
                continue;
            }

            Vector3d radial = new Vector3d(point).sub(center);
            Vector3d direction = radial.lengthSquared() > EPSILON
                ? radial.normalize()
                : new Vector3d(0.0d, 1.0d, 0.0d);

            double latitude = Math.toDegrees(Math.asin(clamp(direction.y, -1.0d, 1.0d)));
            double longitude = normalizeLongitude(Math.toDegrees(Math.atan2(direction.z, direction.x)));

            boolean inLatitude = latitude >= clampedMinLatitude && latitude <= clampedMaxLatitude;
            boolean inLongitude = isLongitudeInRange(longitude, normalizedMinLongitude, normalizedMaxLongitude, wrapLongitude);
            boolean selected = inLatitude && inLongitude;

            if (invertSelection) {
                selected = !selected;
            }

            mask.add(selected);
            if (selected) {
                selectedPoints.add(point);
                if (normal != null) {
                    selectedNormals.add(normal);
                }
            } else {
                rejectedPoints.add(point);
                if (normal != null) {
                    rejectedNormals.add(normal);
                }
            }
        }

        outputValues.put(OUTPUT_POINTS_ID, List.copyOf(selectedPoints));
        outputValues.put(OUTPUT_NORMALS_ID, List.copyOf(selectedNormals));
        outputValues.put(OUTPUT_REJECTED_POINTS_ID, List.copyOf(rejectedPoints));
        outputValues.put(OUTPUT_REJECTED_NORMALS_ID, List.copyOf(rejectedNormals));
        outputValues.put(OUTPUT_MASK_ID, List.copyOf(mask));
        outputValues.put(OUTPUT_COUNT_ID, selectedPoints.size());
        outputValues.put(OUTPUT_REJECTED_COUNT_ID, rejectedPoints.size());
        outputValues.put(OUTPUT_VALID_ID, true);
    }

    @Override
    public Object getNodeState() {
        Map<String, Object> state = new HashMap<>();
        state.put("minLatitudeDeg", minLatitudeDeg);
        state.put("maxLatitudeDeg", maxLatitudeDeg);
        state.put("minLongitudeDeg", minLongitudeDeg);
        state.put("maxLongitudeDeg", maxLongitudeDeg);
        state.put("wrapLongitude", wrapLongitude);
        state.put("invertSelection", invertSelection);
        return state;
    }

    @Override
    public void setNodeState(Object state) {
        if (!(state instanceof Map<?, ?> map)) {
            return;
        }
        if (map.get("minLatitudeDeg") instanceof Number value) {
            setMinLatitudeDeg(value.doubleValue());
        }
        if (map.get("maxLatitudeDeg") instanceof Number value) {
            setMaxLatitudeDeg(value.doubleValue());
        }
        if (map.get("minLongitudeDeg") instanceof Number value) {
            setMinLongitudeDeg(value.doubleValue());
        }
        if (map.get("maxLongitudeDeg") instanceof Number value) {
            setMaxLongitudeDeg(value.doubleValue());
        }
        if (map.get("wrapLongitude") instanceof Boolean value) {
            setWrapLongitude(value);
        }
        if (map.get("invertSelection") instanceof Boolean value) {
            setInvertSelection(value);
        }
    }

    public double getMinLatitudeDeg() {
        return minLatitudeDeg;
    }

    public void setMinLatitudeDeg(double minLatitudeDeg) {
        this.minLatitudeDeg = clamp(minLatitudeDeg, -90.0d, 90.0d);
        markDirty();
    }

    public double getMaxLatitudeDeg() {
        return maxLatitudeDeg;
    }

    public void setMaxLatitudeDeg(double maxLatitudeDeg) {
        this.maxLatitudeDeg = clamp(maxLatitudeDeg, -90.0d, 90.0d);
        markDirty();
    }

    public double getMinLongitudeDeg() {
        return minLongitudeDeg;
    }

    public void setMinLongitudeDeg(double minLongitudeDeg) {
        this.minLongitudeDeg = minLongitudeDeg;
        markDirty();
    }

    public double getMaxLongitudeDeg() {
        return maxLongitudeDeg;
    }

    public void setMaxLongitudeDeg(double maxLongitudeDeg) {
        this.maxLongitudeDeg = maxLongitudeDeg;
        markDirty();
    }

    public boolean isWrapLongitude() {
        return wrapLongitude;
    }

    public void setWrapLongitude(boolean wrapLongitude) {
        this.wrapLongitude = wrapLongitude;
        markDirty();
    }

    public boolean isInvertSelection() {
        return invertSelection;
    }

    public void setInvertSelection(boolean invertSelection) {
        this.invertSelection = invertSelection;
        markDirty();
    }

    private void writeEmptyOutputs() {
        outputValues.put(OUTPUT_POINTS_ID, List.of());
        outputValues.put(OUTPUT_NORMALS_ID, List.of());
        outputValues.put(OUTPUT_REJECTED_POINTS_ID, List.of());
        outputValues.put(OUTPUT_REJECTED_NORMALS_ID, List.of());
        outputValues.put(OUTPUT_MASK_ID, List.of());
        outputValues.put(OUTPUT_COUNT_ID, 0);
        outputValues.put(OUTPUT_REJECTED_COUNT_ID, 0);
        outputValues.put(OUTPUT_VALID_ID, false);
    }

    private double resolveDouble(Object value, double fallback) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return fallback;
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

    private double normalizeLongitude(double longitudeDeg) {
        double normalized = longitudeDeg % 360.0d;
        if (normalized <= -180.0d) {
            normalized += 360.0d;
        }
        if (normalized > 180.0d) {
            normalized -= 360.0d;
        }
        return normalized;
    }

    private boolean isLongitudeInRange(double longitude,
                                       double minLongitude,
                                       double maxLongitude,
                                       boolean wrap) {
        if (!wrap) {
            double min = Math.min(minLongitude, maxLongitude);
            double max = Math.max(minLongitude, maxLongitude);
            return longitude >= min && longitude <= max;
        }
        if (minLongitude <= maxLongitude) {
            return longitude >= minLongitude && longitude <= maxLongitude;
        }
        return longitude >= minLongitude || longitude <= maxLongitude;
    }
}
