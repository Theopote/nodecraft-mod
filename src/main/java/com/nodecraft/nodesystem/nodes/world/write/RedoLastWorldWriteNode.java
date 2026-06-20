package com.nodecraft.nodesystem.nodes.world.write;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@NodeInfo(
    id = "world.write.redo_last_write",
    displayName = "Redo Last World Write",
    description = "Reapplies the most recently undone world.write block operation",
    category = "world.write",
    order = 100
)
public class RedoLastWorldWriteNode extends BaseNode {
    private static final String INPUT_TRIGGER_ID = "input_trigger";
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_REMAINING_REDO_ID = "output_remaining_redo";
    private static final String OUTPUT_REMAINING_HISTORY_ID = "output_remaining_history";
    private static final String OUTPUT_STATUS_ID = "output_status";

    public RedoLastWorldWriteNode() {
        super(UUID.randomUUID(), "world.write.redo_last_write");
        addInputPort(new BasePort(INPUT_TRIGGER_ID, "Trigger", "Any non-null value triggers redo", NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", "Whether a redo record was applied", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_REMAINING_REDO_ID, "Remaining Redo", "Remaining redo records", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_REMAINING_HISTORY_ID, "Remaining History", "Undo records after redo", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_STATUS_ID, "Status", "Redo status message", NodeDataType.STRING, this));
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        WorldWriteHistoryService history = WorldWriteHistoryService.getInstance();
        boolean success = false;
        String status = "No redo executed";

        if (inputValues.get(INPUT_TRIGGER_ID) != null) {
            if (context == null || context.getWorld() == null) {
                status = "Missing execution context";
            } else if (history.redoSize() == 0) {
                status = "No recorded world.write redo history";
            } else {
                success = history.redoLast(context.getWorld());
                status = success ? "Redid last world.write operation" : "Redo failed";
            }
        }

        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_REMAINING_REDO_ID, history.redoSize());
        outputValues.put(OUTPUT_REMAINING_HISTORY_ID, history.size());
        outputValues.put(OUTPUT_STATUS_ID, status);
    }
}
