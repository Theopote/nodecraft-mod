package com.nodecraft.nodesystem.nodes.geometry.architectural_primitives;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.NodeProperty;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.BoxFaceData;
import com.nodecraft.nodesystem.datatypes.BoxGeometryData;
import com.nodecraft.nodesystem.datatypes.CompositeGeometryData;
import com.nodecraft.nodesystem.datatypes.GeometryData;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3d;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Generates a rectangular array of inset opening boxes on a box face.
 */
@NodeInfo(
    id = "geometry.architectural_primitives.window_array",
    displayName = "Window Array",
    description = "Generates a rectangular array of inset window opening boxes on a box face",
    category = "geometry.profiles",
    order = 0
)
public class WindowArrayNode extends BaseNode {

    private static final String INPUT_FACE_ID = "input_face";
    private static final String INPUT_COLUMNS_ID = "input_columns";
    private static final String INPUT_ROWS_ID = "input_rows";
    private static final String INPUT_WINDOW_WIDTH_ID = "input_window_width";
    private static final String INPUT_WINDOW_HEIGHT_ID = "input_window_height";
    private static final String INPUT_MARGIN_ID = "input_margin";
    private static final String INPUT_DEPTH_ID = "input_depth";

    private static final String OUTPUT_GEOMETRY_ID = "output_geometry";
    private static final String OUTPUT_COUNT_ID = "output_count";
    private static final String OUTPUT_VALID_ID = "output_valid";

    @NodeProperty(
        displayName = "Default Depth",
        category = "Openings",
        order = 1,
        description = "Fallback opening depth when no depth input is connected."
    )
    private double defaultDepth = 1.0d;

    public WindowArrayNode() {
        super(UUID.randomUUID(), "geometry.architectural_primitives.window_array");

        addInputPort(new BasePort(INPUT_FACE_ID, "Face", "Box face used as the facade surface", NodeDataType.BOX_FACE, this));
        addInputPort(new BasePort(INPUT_COLUMNS_ID, "Columns", "Number of windows across the face width", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_ROWS_ID, "Rows", "Number of windows across the face height", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_WINDOW_WIDTH_ID, "Window Width", "Window opening width in blocks/meters", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_WINDOW_HEIGHT_ID, "Window Height", "Window opening height in blocks/meters", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_MARGIN_ID, "Margin", "Outer margin from the face edge to the first opening", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_DEPTH_ID, "Depth", "Inset depth of each opening into the solid", NodeDataType.DOUBLE, this));

        addOutputPort(new BasePort(OUTPUT_GEOMETRY_ID, "Geometry", "Composite geometry containing all opening boxes", NodeDataType.GEOMETRY, this));
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Count", "Number of opening boxes created", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_VALID_ID, "Valid", "True when a valid opening array could be generated", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDescription() {
        return "Generates a rectangular array of inset window opening boxes on a box face";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object faceObj = inputValues.get(INPUT_FACE_ID);
        Object columnsObj = inputValues.get(INPUT_COLUMNS_ID);
        Object rowsObj = inputValues.get(INPUT_ROWS_ID);
        Object widthObj = inputValues.get(INPUT_WINDOW_WIDTH_ID);
        Object heightObj = inputValues.get(INPUT_WINDOW_HEIGHT_ID);
        Object marginObj = inputValues.get(INPUT_MARGIN_ID);
        Object depthObj = inputValues.get(INPUT_DEPTH_ID);

        GeometryData geometry = null;
        int count = 0;
        boolean valid = false;

        if (faceObj instanceof BoxFaceData face) {
            int columns = resolvePositiveInt(columnsObj, 1);
            int rows = resolvePositiveInt(rowsObj, 1);
            double windowWidth = resolvePositiveDouble(widthObj, 1.0d);
            double windowHeight = resolvePositiveDouble(heightObj, 1.0d);
            double margin = resolveNonNegativeDouble(marginObj, 0.0d);
            double depth = resolvePositiveDouble(depthObj, defaultDepth);

            List<BoxGeometryData> openings = buildOpeningBoxes(face, columns, rows, windowWidth, windowHeight, margin, depth);
            if (!openings.isEmpty()) {
                geometry = new CompositeGeometryData(new ArrayList<>(openings));
                count = openings.size();
                valid = true;
            }
        }

        outputValues.put(OUTPUT_GEOMETRY_ID, geometry);
        outputValues.put(OUTPUT_COUNT_ID, count);
        outputValues.put(OUTPUT_VALID_ID, valid);
    }

    private List<BoxGeometryData> buildOpeningBoxes(
        BoxFaceData face,
        int columns,
        int rows,
        double windowWidth,
        double windowHeight,
        double margin,
        double depth
    ) {
        List<Vector3d> corners = face.getCorners();
        if (corners.size() < 4) {
            return List.of();
        }

        Vector3d c0 = corners.get(0);
        Vector3d c1 = corners.get(1);
        Vector3d c3 = corners.get(3);

        Vector3d xAxis = new Vector3d(c1).sub(c0);
        Vector3d yHint = new Vector3d(c3).sub(c0);
        double faceWidth = xAxis.length();
        double faceHeight = yHint.length();
        if (faceWidth <= 1.0e-9d || faceHeight <= 1.0e-9d) {
            return List.of();
        }

        xAxis.normalize();
        Vector3d zAxis = new Vector3d(xAxis).cross(yHint);
        if (zAxis.lengthSquared() <= 1.0e-12d) {
            return List.of();
        }
        zAxis.normalize();
        Vector3d faceNormal = face.getNormal();
        if (zAxis.dot(faceNormal) < 0.0d) {
            zAxis.negate();
        }
        Vector3d yAxis = new Vector3d(zAxis).cross(xAxis).normalize();

        double availableWidth = faceWidth - 2.0d * margin;
        double availableHeight = faceHeight - 2.0d * margin;
        if (availableWidth < windowWidth || availableHeight < windowHeight) {
            return List.of();
        }

        double spacingX = columns > 1 ? (availableWidth - columns * windowWidth) / (columns - 1) : 0.0d;
        double spacingY = rows > 1 ? (availableHeight - rows * windowHeight) / (rows - 1) : 0.0d;
        if (spacingX < -1.0e-9d || spacingY < -1.0e-9d) {
            return List.of();
        }

        double startX = -faceWidth / 2.0d + margin + windowWidth / 2.0d;
        double startY = faceHeight / 2.0d - margin - windowHeight / 2.0d;

        Matrix3d orientation = new Matrix3d(
            xAxis.x, yAxis.x, zAxis.x,
            xAxis.y, yAxis.y, zAxis.y,
            xAxis.z, yAxis.z, zAxis.z
        );

        Vector3d faceCenter = face.getCenter();
        Vector3d inwardNormal = new Vector3d(zAxis).negate();
        List<BoxGeometryData> openings = new ArrayList<>(columns * rows);

        for (int row = 0; row < rows; row++) {
            double offsetY = startY - row * (windowHeight + spacingY);
            for (int column = 0; column < columns; column++) {
                double offsetX = startX + column * (windowWidth + spacingX);
                Vector3d center = new Vector3d(faceCenter)
                    .fma(offsetX, xAxis)
                    .fma(offsetY, yAxis)
                    .fma(depth / 2.0d, inwardNormal);

                Vector3d halfExtents = new Vector3d(windowWidth / 2.0d, windowHeight / 2.0d, depth / 2.0d);
                openings.add(new BoxGeometryData(center, halfExtents, orientation, true));
            }
        }

        return List.copyOf(openings);
    }

    private int resolvePositiveInt(Object value, int fallback) {
        if (value instanceof Number number) {
            return Math.max(1, number.intValue());
        }
        return fallback;
    }

    private double resolvePositiveDouble(Object value, double fallback) {
        if (value instanceof Number number) {
            return Math.max(1.0e-6d, number.doubleValue());
        }
        return fallback;
    }

    private double resolveNonNegativeDouble(Object value, double fallback) {
        if (value instanceof Number number) {
            return Math.max(0.0d, number.doubleValue());
        }
        return fallback;
    }

    public double getDefaultDepth() {
        return defaultDepth;
    }

    public void setDefaultDepth(double defaultDepth) {
        double resolved = Math.max(1.0e-6d, defaultDepth);
        if (Double.compare(this.defaultDepth, resolved) != 0) {
            this.defaultDepth = resolved;
            markDirty();
        }
    }

    @Override
    public Object getNodeState() {
        Map<String, Object> state = new HashMap<>();
        state.put("defaultDepth", defaultDepth);
        return state;
    }

    @Override
    public void setNodeState(Object state) {
        if (state instanceof Map<?, ?> map && map.get("defaultDepth") instanceof Number value) {
            setDefaultDepth(value.doubleValue());
        }
    }
}
