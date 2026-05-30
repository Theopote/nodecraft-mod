package com.nodecraft.nodesystem.nodes.geometry.architectural_primitives;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.BoxFaceData;
import com.nodecraft.nodesystem.datatypes.BoxGeometryData;
import com.nodecraft.nodesystem.datatypes.CompositeGeometryData;
import com.nodecraft.nodesystem.datatypes.GeometryData;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Generates a rectangular array of inset door opening boxes on a box face.
 */
@NodeInfo(
    id = "geometry.architectural_primitives.door_array",
    displayName = "Door Array",
    description = "Generates a rectangular array of inset door opening boxes on a box face",
    category = "geometry.architectural_primitives",
    order = 1
)
public class DoorArrayNode extends BaseNode {

    private static final String INPUT_FACE_ID = "input_face";
    private static final String INPUT_COLUMNS_ID = "input_columns";
    private static final String INPUT_ROWS_ID = "input_rows";
    private static final String INPUT_DOOR_WIDTH_ID = "input_door_width";
    private static final String INPUT_DOOR_HEIGHT_ID = "input_door_height";
    private static final String INPUT_MARGIN_ID = "input_margin";
    private static final String INPUT_DEPTH_ID = "input_depth";

    private static final String OUTPUT_GEOMETRY_ID = "output_geometry";
    private static final String OUTPUT_COUNT_ID = "output_count";
    private static final String OUTPUT_VALID_ID = "output_valid";

    public DoorArrayNode() {
        super(UUID.randomUUID(), "geometry.architectural_primitives.door_array");

        addInputPort(new BasePort(INPUT_FACE_ID, "Face", "Box face used as the facade surface", NodeDataType.BOX_FACE, this));
        addInputPort(new BasePort(INPUT_COLUMNS_ID, "Columns", "Number of doors across the face width", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_ROWS_ID, "Rows", "Number of doors stacked vertically", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_DOOR_WIDTH_ID, "Door Width", "Door opening width in blocks/meters", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_DOOR_HEIGHT_ID, "Door Height", "Door opening height in blocks/meters", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_MARGIN_ID, "Margin", "Outer margin from the face edge to the first opening", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_DEPTH_ID, "Depth", "Inset depth of each opening into the solid", NodeDataType.DOUBLE, this));

        addOutputPort(new BasePort(OUTPUT_GEOMETRY_ID, "Geometry", "Composite geometry containing all door opening boxes", NodeDataType.GEOMETRY, this));
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Count", "Number of door opening boxes created", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_VALID_ID, "Valid", "True when a valid door array could be generated", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDescription() {
        return "Generates a rectangular array of inset door opening boxes on a box face";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object faceObj = inputValues.get(INPUT_FACE_ID);

        GeometryData geometry = null;
        int count = 0;
        boolean valid = false;

        if (faceObj instanceof BoxFaceData face) {
            ArchitecturalPrimitiveSupport.FaceFrame frame = ArchitecturalPrimitiveSupport.resolveFaceFrame(face);
            if (frame != null) {
                int columns = ArchitecturalPrimitiveSupport.resolvePositiveInt(inputValues.get(INPUT_COLUMNS_ID), 1);
                int rows = ArchitecturalPrimitiveSupport.resolvePositiveInt(inputValues.get(INPUT_ROWS_ID), 1);
                double doorWidth = ArchitecturalPrimitiveSupport.resolvePositiveDouble(inputValues.get(INPUT_DOOR_WIDTH_ID), 1.0d);
                double doorHeight = ArchitecturalPrimitiveSupport.resolvePositiveDouble(inputValues.get(INPUT_DOOR_HEIGHT_ID), 2.0d);
                double margin = ArchitecturalPrimitiveSupport.resolveNonNegativeDouble(inputValues.get(INPUT_MARGIN_ID), 0.0d);
                double depth = ArchitecturalPrimitiveSupport.resolvePositiveDouble(inputValues.get(INPUT_DEPTH_ID), 1.0d);

                List<BoxGeometryData> openings = buildOpeningBoxes(frame, columns, rows, doorWidth, doorHeight, margin, depth);
                if (!openings.isEmpty()) {
                    geometry = new CompositeGeometryData(new ArrayList<>(openings));
                    count = openings.size();
                    valid = true;
                }
            }
        }

        outputValues.put(OUTPUT_GEOMETRY_ID, geometry);
        outputValues.put(OUTPUT_COUNT_ID, count);
        outputValues.put(OUTPUT_VALID_ID, valid);
    }

    private List<BoxGeometryData> buildOpeningBoxes(
        ArchitecturalPrimitiveSupport.FaceFrame frame,
        int columns,
        int rows,
        double doorWidth,
        double doorHeight,
        double margin,
        double depth
    ) {
        double availableWidth = frame.width() - 2.0d * margin;
        double availableHeight = frame.height() - 2.0d * margin;
        if (availableWidth < doorWidth || availableHeight < doorHeight) {
            return List.of();
        }

        double spacingX = columns > 1 ? (availableWidth - columns * doorWidth) / (columns - 1) : 0.0d;
        double spacingY = rows > 1 ? (availableHeight - rows * doorHeight) / (rows - 1) : 0.0d;
        if (spacingX < -1.0e-9d || spacingY < -1.0e-9d) {
            return List.of();
        }

        double startX = -frame.width() / 2.0d + margin + doorWidth / 2.0d;
        double startY = -frame.height() / 2.0d + margin + doorHeight / 2.0d;
        Vector3d inwardNormal = new Vector3d(frame.zAxis()).negate();
        List<BoxGeometryData> openings = new ArrayList<>(columns * rows);

        for (int row = 0; row < rows; row++) {
            double offsetY = startY + row * (doorHeight + spacingY);
            for (int column = 0; column < columns; column++) {
                double offsetX = startX + column * (doorWidth + spacingX);
                Vector3d center = new Vector3d(frame.center())
                    .fma(offsetX, frame.xAxis())
                    .fma(offsetY, frame.yAxis())
                    .fma(depth / 2.0d, inwardNormal);

                Vector3d halfExtents = new Vector3d(doorWidth / 2.0d, doorHeight / 2.0d, depth / 2.0d);
                openings.add(ArchitecturalPrimitiveSupport.createOrientedBox(center, halfExtents, frame.xAxis(), frame.yAxis(), frame.zAxis()));
            }
        }

        return List.copyOf(openings);
    }
}