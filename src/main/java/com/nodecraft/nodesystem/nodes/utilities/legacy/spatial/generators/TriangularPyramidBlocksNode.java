package com.nodecraft.nodesystem.nodes.utilities.legacy.spatial.generators;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Triangular Pyramid (Blocks): generates a triangular pyramid volume as block coordinates.
 * Base size and height can be configured independently.
 */
@NodeInfo(
    id = "spatial.generators.triangular_pyramid_blocks",
    displayName = "Triangular Pyramid Generator",
    description = "Generates a triangular pyramid volume as block coordinates.",
    category = "utilities.legacy.spatial.generators"
)
public class TriangularPyramidBlocksNode extends BaseNode {

    // ---           IDs ---
    private static final String INPUT_CENTER_ID = "input_center";
    private static final String INPUT_BASE_SIZE_ID = "input_base_size";
    private static final String INPUT_HEIGHT_ID = "input_height";

    // ---           IDs ---
    private static final String OUTPUT_BLOCKS_ID = "output_blocks";
    private static final String OUTPUT_COUNT_ID = "output_count";

    public TriangularPyramidBlocksNode() {
        super(UUID.randomUUID(), "spatial.generators.triangular_pyramid_blocks");

        addInputPort(new BasePort(INPUT_CENTER_ID, "Base Center", "Center point of the base", NodeDataType.BLOCK_POS, this));
        addInputPort(new BasePort(INPUT_BASE_SIZE_ID, "Base Size", "Edge length of the base triangle", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_HEIGHT_ID, "Height", "Pyramid height", NodeDataType.INTEGER, this));

        addOutputPort(new BasePort(OUTPUT_BLOCKS_ID, "Blocks", "Generated pyramid block coordinates", NodeDataType.BLOCK_LIST, this));
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Count", "Number of generated blocks", NodeDataType.INTEGER, this));
    }

    @Override
    public String getDescription() {
        return "Generates a triangular pyramid volume as block coordinates.";
    }

    @Override
    public String getDisplayName() {
        return "Triangular Pyramid (Blocks)";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object centerObj = inputValues.get(INPUT_CENTER_ID);
        Object baseSizeObj = inputValues.get(INPUT_BASE_SIZE_ID);
        Object heightObj = inputValues.get(INPUT_HEIGHT_ID);

        BlockPosList result = new BlockPosList();

        if (centerObj instanceof BlockPos &&
            baseSizeObj instanceof Number &&
            heightObj instanceof Number) {

            BlockPos center = (BlockPos) centerObj;
            int baseSize = Math.max(1, ((Number) baseSizeObj).intValue());
            int height = Math.max(1, ((Number) heightObj).intValue());

            int cx = center.getX();
            int cy = center.getY();
            int cz = center.getZ();

            //                            ?cx, cy, cz)
            //           ?            Z         
            double circumR = baseSize / Math.sqrt(3.0); //           ?

            double[][] baseVertices = {
                {0, circumR},                                          //     ?
                {-circumR * Math.sqrt(3.0) / 2.0, -circumR / 2.0},    //     ?
                {circumR * Math.sqrt(3.0) / 2.0, -circumR / 2.0}      //     ?
            };

            //       ?(cx, cy + height, cz)
            //                                     
            for (int dy = 0; dy < height; dy++) {
                double ratio = 1.0 - (double) dy / height;

                //                     ?
                double[][] layerVerts = new double[3][2];
                for (int i = 0; i < 3; i++) {
                    layerVerts[i][0] = baseVertices[i][0] * ratio;
                    layerVerts[i][1] = baseVertices[i][1] * ratio;
                }

                //                    ?
                int bound = (int) Math.ceil(circumR * ratio) + 1;

                for (int dx = -bound; dx <= bound; dx++) {
                    for (int dz = -bound; dz <= bound; dz++) {
                        if (isInsideTriangle(dx, dz, layerVerts)) {
                            result.add(new BlockPos(cx + dx, cy + dy, cz + dz));
                        }
                    }
                }
            }
        }

        outputValues.put(OUTPUT_BLOCKS_ID, result);
        outputValues.put(OUTPUT_COUNT_ID, result.size());
    }

    /**
     *                                         ?
     */
    private boolean isInsideTriangle(double px, double pz, double[][] verts) {
        double x1 = verts[0][0], z1 = verts[0][1];
        double x2 = verts[1][0], z2 = verts[1][1];
        double x3 = verts[2][0], z3 = verts[2][1];

        double denom = (z2 - z3) * (x1 - x3) + (x3 - x2) * (z1 - z3);
        if (Math.abs(denom) < 1e-10) return false;

        double a = ((z2 - z3) * (px - x3) + (x3 - x2) * (pz - z3)) / denom;
        double b = ((z3 - z1) * (px - x3) + (x1 - x3) * (pz - z3)) / denom;
        double c = 1.0 - a - b;

        //     ?+0.5                         ?
        double tolerance = 0.5 / Math.max(1, Math.max(Math.abs(x1 - x2), Math.abs(x1 - x3)));
        return a >= -tolerance && b >= -tolerance && c >= -tolerance;
    }

    @Override
    public Object getNodeState() {
        return new java.util.HashMap<>();
    }

    @Override
    public void setNodeState(Object state) {
        //           ?
    }
}
