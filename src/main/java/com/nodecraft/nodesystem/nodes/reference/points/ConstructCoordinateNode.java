package com.nodecraft.nodesystem.nodes.reference.points;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@NodeInfo(
    id = "reference.points.construct_coordinate",
    displayName = "Construct Coordinate",
    description = "Builds a coordinate from X, Y, Z numeric inputs.",
    category = "reference.points",
    order = 1
)
public class ConstructCoordinateNode extends BaseNode {

    private static final String INPUT_X_ID = "input_x";
    private static final String INPUT_Y_ID = "input_y";
    private static final String INPUT_Z_ID = "input_z";

    private static final String OUTPUT_COORDINATE_ID = "output_coordinate";
    private static final String OUTPUT_BLOCK_POS_ID = "output_block_pos";
    private static final String OUTPUT_X_ID = "output_x";
    private static final String OUTPUT_Y_ID = "output_y";
    private static final String OUTPUT_Z_ID = "output_z";

    public ConstructCoordinateNode() {
        super(UUID.randomUUID(), "reference.points.construct_coordinate");

        addInputPort(new BasePort(INPUT_X_ID, "X", "X component input", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_Y_ID, "Y", "Y component input", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_Z_ID, "Z", "Z component input", NodeDataType.INTEGER, this));

        addOutputPort(new BasePort(OUTPUT_COORDINATE_ID, "Coordinate", "Constructed coordinate", NodeDataType.COORDINATE, this));
        addOutputPort(new BasePort(OUTPUT_BLOCK_POS_ID, "Block Pos", "Constructed block position", NodeDataType.BLOCK_POS, this));
        addOutputPort(new BasePort(OUTPUT_X_ID, "X", "X component output", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_Y_ID, "Y", "Y component output", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_Z_ID, "Z", "Z component output", NodeDataType.INTEGER, this));
    }

    @Override
    public String getDescription() {
        return "Builds a coordinate from X, Y, Z inputs and outputs Coordinate / Block Pos / X / Y / Z.";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        int x = toInt(inputValues.get(INPUT_X_ID));
        int y = toInt(inputValues.get(INPUT_Y_ID));
        int z = toInt(inputValues.get(INPUT_Z_ID));

        BlockPos blockPos = new BlockPos(x, y, z);
        outputValues.put(OUTPUT_COORDINATE_ID, blockPos);
        outputValues.put(OUTPUT_BLOCK_POS_ID, blockPos);
        outputValues.put(OUTPUT_X_ID, x);
        outputValues.put(OUTPUT_Y_ID, y);
        outputValues.put(OUTPUT_Z_ID, z);
    }

    private int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }
}
