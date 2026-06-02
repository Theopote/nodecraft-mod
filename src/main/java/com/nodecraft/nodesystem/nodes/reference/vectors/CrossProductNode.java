package com.nodecraft.nodesystem.nodes.reference.vectors;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.UUID;

/**
 * Computes the cross product of two vectors (A x B).
 */
@NodeInfo(
    id = "reference.vectors.cross_product",
    displayName = "Cross Product",
    description = "Computes the cross product A x B and its magnitude.",
    category = "reference.vectors",
    order = 3
)
public class CrossProductNode extends BaseNode {

    private static final double EPS = 1.0e-12d;

    // Keep existing port IDs so saved graphs continue to resolve connections.
    private static final String INPUT_A_ID = "input_vector_a";
    private static final String INPUT_B_ID = "input_vector_b";

    private static final String OUTPUT_CROSS_PRODUCT_ID = "output_cross_product";
    private static final String OUTPUT_MAGNITUDE_ID = "output_magnitude";
    private static final String OUTPUT_VALID_ID = "output_valid";

    public CrossProductNode() {
        super(UUID.randomUUID(), "reference.vectors.cross_product");

        addInputPort(new BasePort(INPUT_A_ID, "A", "First vector", NodeDataType.VECTOR, this));
        addInputPort(new BasePort(INPUT_B_ID, "B", "Second vector", NodeDataType.VECTOR, this));

        addOutputPort(new BasePort(OUTPUT_CROSS_PRODUCT_ID, "Cross Product", "Result A x B", NodeDataType.VECTOR, this));
        addOutputPort(new BasePort(OUTPUT_MAGNITUDE_ID, "Magnitude", "Length of the cross product", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_VALID_ID, "Valid",
            "True when inputs are valid and the cross product is non-zero",
            NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDisplayName() {
        return "Cross Product";
    }

    @Override
    public String getDescription() {
        return "Computes the cross product A x B and its magnitude.";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Vector3d a = toVector(inputValues.get(INPUT_A_ID));
        Vector3d b = toVector(inputValues.get(INPUT_B_ID));
        if (a == null || b == null || !isFinite(a) || !isFinite(b)) {
            writeInvalid();
            return;
        }

        Vector3d cross = new Vector3d(a).cross(b);
        double magnitude = cross.length();

        outputValues.put(OUTPUT_CROSS_PRODUCT_ID, cross);
        outputValues.put(OUTPUT_MAGNITUDE_ID, magnitude);
        outputValues.put(OUTPUT_VALID_ID, magnitude >= EPS);
    }

    private void writeInvalid() {
        outputValues.put(OUTPUT_CROSS_PRODUCT_ID, new Vector3d());
        outputValues.put(OUTPUT_MAGNITUDE_ID, 0.0d);
        outputValues.put(OUTPUT_VALID_ID, false);
    }

    private boolean isFinite(Vector3d vector) {
        return Double.isFinite(vector.x) && Double.isFinite(vector.y) && Double.isFinite(vector.z);
    }

    private Vector3d toVector(Object value) {
        if (value instanceof Vector3d vector) {
            return new Vector3d(vector);
        }
        if (value instanceof Vec3d vector) {
            return new Vector3d(vector.x, vector.y, vector.z);
        }
        return null;
    }
}
