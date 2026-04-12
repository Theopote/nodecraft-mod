package com.nodecraft.nodesystem.nodes.output.execute;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.NodeProperty;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPlacementData;
import com.nodecraft.nodesystem.util.BlockPosList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Concatenates multiple block placement lists into one execution-ready list.
 */
@NodeInfo(
    id = "output.execute.merge_block_placements",
    displayName = "Merge Block Placements",
    description = "Concatenates multiple block placement lists into one execution-ready placement list",
    category = "output.execute",
    order = 13
)
public class MergeBlockPlacementsNode extends BaseNode {

    private static final int MIN_INPUT_COUNT = 2;
    private static final int MAX_INPUT_COUNT = 16;

    private static final String OUTPUT_PLACEMENTS_ID = "output_placements";
    private static final String OUTPUT_POSITIONS_ID = "output_positions";
    private static final String OUTPUT_BLOCK_IDS_ID = "output_block_ids";
    private static final String OUTPUT_COUNT_ID = "output_count";

    @NodeProperty(
        displayName = "Input Count",
        category = "Settings",
        order = 1,
        description = "Number of placement list inputs to expose."
    )
    private int inputCount = 4;

    @NodeProperty(
        displayName = "Last Wins",
        category = "Merge",
        order = 2,
        description = "When enabled, later entries replace earlier placements at the same position."
    )
    private boolean lastWins = true;

    public MergeBlockPlacementsNode() {
        super(UUID.randomUUID(), "output.execute.merge_block_placements");
        rebuildInputPorts();
        addOutputPort(new BasePort(OUTPUT_PLACEMENTS_ID, "Block Placements", "Merged placement list", NodeDataType.BLOCK_PLACEMENT_LIST, this));
        addOutputPort(new BasePort(OUTPUT_POSITIONS_ID, "Positions", "Merged placement positions", NodeDataType.BLOCK_LIST, this));
        addOutputPort(new BasePort(OUTPUT_BLOCK_IDS_ID, "Block IDs", "Block ids aligned with the merged placement list", NodeDataType.BLOCK_INFO_LIST, this));
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Count", "Number of merged placements", NodeDataType.INTEGER, this));
    }

    @Override
    public String getDescription() {
        return "Concatenates multiple block placement lists into one execution-ready placement list";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        List<BlockPlacementData> merged = lastWins ? mergeWithOverwrite() : mergeByConcatenation();
        BlockPosList positions = new BlockPosList();
        List<String> blockIds = new ArrayList<>(merged.size());

        for (BlockPlacementData placement : merged) {
            if (placement.pos() != null) {
                positions.add(placement.pos());
            }
            blockIds.add(placement.blockId());
        }

        outputValues.put(OUTPUT_PLACEMENTS_ID, merged);
        outputValues.put(OUTPUT_POSITIONS_ID, positions);
        outputValues.put(OUTPUT_BLOCK_IDS_ID, blockIds);
        outputValues.put(OUTPUT_COUNT_ID, merged.size());
    }

    private List<BlockPlacementData> mergeByConcatenation() {
        List<BlockPlacementData> merged = new ArrayList<>();
        for (int i = 0; i < inputCount; i++) {
            Object value = inputValues.get(inputPortId(i));
            if (!(value instanceof List<?> list)) {
                continue;
            }
            for (Object entry : list) {
                if (entry instanceof BlockPlacementData placement && placement.pos() != null && placement.blockId() != null && !placement.blockId().isBlank()) {
                    merged.add(placement);
                }
            }
        }
        return merged;
    }

    private List<BlockPlacementData> mergeWithOverwrite() {
        Map<BlockPos, BlockPlacementData> merged = new LinkedHashMap<>();
        for (int i = 0; i < inputCount; i++) {
            Object value = inputValues.get(inputPortId(i));
            if (!(value instanceof List<?> list)) {
                continue;
            }
            for (Object entry : list) {
                if (entry instanceof BlockPlacementData placement && placement.pos() != null && placement.blockId() != null && !placement.blockId().isBlank()) {
                    merged.put(placement.pos(), placement);
                }
            }
        }
        return new ArrayList<>(merged.values());
    }

    private void rebuildInputPorts() {
        inputPorts.clear();
        for (int i = 0; i < inputCount; i++) {
            addInputPort(new BasePort(
                inputPortId(i),
                "Placements " + (i + 1),
                "Block placement list " + (i + 1),
                NodeDataType.BLOCK_PLACEMENT_LIST,
                this
            ));
        }
    }

    private String inputPortId(int index) {
        return "input_placements_" + index;
    }

    public int getInputCount() {
        return inputCount;
    }

    public void setInputCount(int inputCount) {
        int resolved = Math.max(MIN_INPUT_COUNT, Math.min(MAX_INPUT_COUNT, inputCount));
        if (this.inputCount != resolved) {
            this.inputCount = resolved;
            rebuildInputPorts();
            markDirty();
        }
    }

    public boolean isLastWins() {
        return lastWins;
    }

    public void setLastWins(boolean lastWins) {
        if (this.lastWins != lastWins) {
            this.lastWins = lastWins;
            markDirty();
        }
    }

    @Override
    public Object getNodeState() {
        Map<String, Object> state = new HashMap<>();
        state.put("inputCount", inputCount);
        state.put("lastWins", lastWins);
        return state;
    }

    @Override
    public void setNodeState(Object state) {
        if (!(state instanceof Map<?, ?> map)) {
            return;
        }
        if (map.get("inputCount") instanceof Number value) {
            setInputCount(value.intValue());
        }
        if (map.get("lastWins") instanceof Boolean value) {
            setLastWins(value);
        }
    }
}
