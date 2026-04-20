package com.nodecraft.nodesystem.nodes.utilities.morphology;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.NodeProperty;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Morphological dilate or erode on a block set using a 6-connected voxel structuring element.
 */
@NodeInfo(
    id = "utilities.morphology.block_list_morphology",
    displayName = "Block List Morphology",
    description = "Dilates or erodes a block list using Manhattan-1 (6-neighbor) morphology iterations",
    category = "utilities.morphology",
    order = 0
)
public class BlockListMorphologyNode extends BaseNode {

    public enum MorphOp {
        DILATE,
        ERODE
    }

    private static final int[][] D6 = {
        {1, 0, 0}, {-1, 0, 0},
        {0, 1, 0}, {0, -1, 0},
        {0, 0, 1}, {0, 0, -1}
    };

    @NodeProperty(displayName = "Operation", category = "Morphology", order = 1)
    private MorphOp operation = MorphOp.DILATE;

    @NodeProperty(displayName = "Iterations", category = "Morphology", order = 2,
        description = "Number of 6-neighbor dilate/erode iterations to apply")
    private int iterations = 1;

    private static final String INPUT_BLOCKS_ID = "input_blocks";
    private static final String INPUT_ITERATIONS_ID = "input_iterations";

    private static final String OUTPUT_BLOCKS_ID = "output_blocks";
    private static final String OUTPUT_VALID_ID = "output_valid";

    public BlockListMorphologyNode() {
        super(UUID.randomUUID(), "utilities.morphology.block_list_morphology");

        addInputPort(new BasePort(INPUT_BLOCKS_ID, "Blocks",
            "Input block positions",
            NodeDataType.BLOCK_LIST, this));
        addInputPort(new BasePort(INPUT_ITERATIONS_ID, "Iterations",
            "Optional iteration override (>= 1). When disconnected, the node property is used.",
            NodeDataType.INTEGER, this));

        addOutputPort(new BasePort(OUTPUT_BLOCKS_ID, "Blocks",
            "Morphology result",
            NodeDataType.BLOCK_LIST, this));
        addOutputPort(new BasePort(OUTPUT_VALID_ID, "Valid",
            "True when processing succeeded",
            NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDisplayName() {
        return "Block List Morphology";
    }

    @Override
    public String getDescription() {
        return "Dilates or erodes a block list using Manhattan-1 (6-neighbor) morphology iterations";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object blocksObj = inputValues.get(INPUT_BLOCKS_ID);
        if (!(blocksObj instanceof BlockPosList input) || input.isEmpty()) {
            outputValues.put(OUTPUT_BLOCKS_ID, new BlockPosList());
            outputValues.put(OUTPUT_VALID_ID, false);
            return;
        }

        int iters = iterations;
        Object iterObj = inputValues.get(INPUT_ITERATIONS_ID);
        if (iterObj instanceof Number n) {
            iters = n.intValue();
        }
        if (iters < 1) {
            iters = 1;
        }
        if (iters > 64) {
            iters = 64;
        }

        Set<BlockPos> current = new LinkedHashSet<>();
        for (BlockPos p : input) {
            current.add(p.toImmutable());
        }

        for (int i = 0; i < iters; i++) {
            if (operation == MorphOp.DILATE) {
                current = dilateOnce(current);
            } else {
                current = erodeOnce(current);
            }
        }

        BlockPosList out = new BlockPosList();
        out.addAll(current);
        outputValues.put(OUTPUT_BLOCKS_ID, out);
        outputValues.put(OUTPUT_VALID_ID, true);
    }

    private static Set<BlockPos> dilateOnce(Set<BlockPos> input) {
        Set<BlockPos> out = new LinkedHashSet<>(input);
        for (BlockPos p : input) {
            for (int[] d : D6) {
                out.add(new BlockPos(p.getX() + d[0], p.getY() + d[1], p.getZ() + d[2]).toImmutable());
            }
        }
        return out;
    }

    private static Set<BlockPos> erodeOnce(Set<BlockPos> input) {
        Set<BlockPos> set = new HashSet<>(input);
        Set<BlockPos> out = new LinkedHashSet<>();
        for (BlockPos p : input) {
            boolean keep = true;
            for (int[] d : D6) {
                BlockPos q = new BlockPos(p.getX() + d[0], p.getY() + d[1], p.getZ() + d[2]).toImmutable();
                if (!set.contains(q)) {
                    keep = false;
                    break;
                }
            }
            if (keep) {
                out.add(p.toImmutable());
            }
        }
        return out;
    }

    public MorphOp getOperation() {
        return operation;
    }

    public void setOperation(MorphOp operation) {
        this.operation = operation == null ? MorphOp.DILATE : operation;
        markDirty();
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
        markDirty();
    }

    @Override
    public Object getNodeState() {
        return java.util.Map.of(
            "operation", operation.name(),
            "iterations", iterations
        );
    }

    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map<?, ?> map) {
            Object op = map.get("operation");
            if (op instanceof String s) {
                try {
                    setOperation(MorphOp.valueOf(s));
                } catch (IllegalArgumentException ignored) {
                    // keep default
                }
            }
            Object it = map.get("iterations");
            if (it instanceof Number n) {
                setIterations(n.intValue());
            }
        }
    }
}
