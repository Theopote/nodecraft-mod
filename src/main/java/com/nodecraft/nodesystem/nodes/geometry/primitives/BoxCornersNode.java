package com.nodecraft.nodesystem.nodes.geometry.primitives;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BasePort;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix3d;
import org.joml.Vector3d;

@NodeInfo(
    id = "geometry.primitives.box_from_corners",
    displayName = "Box by Two Corners",
    description = "Generates an axis-aligned box from two opposite corner points",
    category = "geometry.primitives",
    order = 2
)
public class BoxCornersNode extends AbstractBoxGeneratorNode {

    private static final String INPUT_CORNER_A_ID = "input_corner_a";
    private static final String INPUT_CORNER_B_ID = "input_corner_b";

    public BoxCornersNode() {
        super("geometry.primitives.box_from_corners");

        addInputPort(new BasePort(INPUT_CORNER_A_ID, "Corner A", "First corner of the box", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_CORNER_B_ID, "Corner B", "Opposite corner of the box. The result stays axis-aligned.", NodeDataType.ANY, this));
    }

    @Override
    public String getDescription() {
        return "Generates an axis-aligned box from two opposite corner points";
    }

    @Override
    public String getDisplayName() {
        return "Box by Two Corners";
    }

    @Override
    protected BoxDefinition resolveBoxDefinition() {
        Object cornerAObj = inputValues.get(INPUT_CORNER_A_ID);
        Object cornerBObj = inputValues.get(INPUT_CORNER_B_ID);

        Vector3d cornerA = resolveVectorInput(cornerAObj);
        Vector3d cornerB = resolveVectorInput(cornerBObj);
        if (cornerA == null || cornerB == null) {
            return null;
        }

        double minX = Math.min(cornerA.x, cornerB.x);
        double minY = Math.min(cornerA.y, cornerB.y);
        double minZ = Math.min(cornerA.z, cornerB.z);
        double maxX = Math.max(cornerA.x, cornerB.x);
        double maxY = Math.max(cornerA.y, cornerB.y);
        double maxZ = Math.max(cornerA.z, cornerB.z);

        BlockPos minCorner = BlockPos.ofFloored(minX, minY, minZ);
        BlockPos maxCorner = BlockPos.ofFloored(maxX, maxY, maxZ);

        Vector3d center = new Vector3d(
            (minX + maxX) * 0.5d,
            (minY + maxY) * 0.5d,
            (minZ + maxZ) * 0.5d
        );
        Vector3d halfExtents = new Vector3d(
            Math.max(1.0e-6d, (maxX - minX) * 0.5d),
            Math.max(1.0e-6d, (maxY - minY) * 0.5d),
            Math.max(1.0e-6d, (maxZ - minZ) * 0.5d)
        );

        return new BoxDefinition(
            new com.nodecraft.nodesystem.datatypes.RegionData(minCorner, maxCorner),
            center,
            halfExtents,
            new Matrix3d().identity(),
            false
        );
    }
}
