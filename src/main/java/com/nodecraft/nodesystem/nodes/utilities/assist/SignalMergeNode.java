package com.nodecraft.nodesystem.nodes.utilities.assist;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 汇线节点：将两路输入按优先级汇聚为一路输出，用于简化连线。
 */
@NodeInfo(
    id = "utilities.assist.signal_merge",
    displayName = "汇线节点",
    description = "将两路输入按优先级汇聚为一路输出",
    category = "utilities.assist"
)
public class SignalMergeNode extends BaseNode {

    private static final String INPUT_PRIMARY_ID = "input_primary";
    private static final String INPUT_SECONDARY_ID = "input_secondary";
    private static final String INPUT_PREFER_PRIMARY_ID = "input_prefer_primary";

    private static final String OUTPUT_SIGNAL_ID = "output_signal";
    private static final String OUTPUT_SOURCE_ID = "output_source";

    private boolean preferPrimary = true;

    public SignalMergeNode() {
        super(UUID.randomUUID(), "utilities.assist.signal_merge");

        addInputPort(new BasePort(
            INPUT_PRIMARY_ID,
            "主输入",
            "主优先级输入",
            NodeDataType.ANY,
            this
        ));

        addInputPort(new BasePort(
            INPUT_SECONDARY_ID,
            "次输入",
            "次优先级输入",
            NodeDataType.ANY,
            this
        ));

        addInputPort(new BasePort(
            INPUT_PREFER_PRIMARY_ID,
            "主输入优先",
            "是否优先使用主输入（布尔）",
            NodeDataType.BOOLEAN,
            this
        ));

        addOutputPort(new BasePort(
            OUTPUT_SIGNAL_ID,
            "输出",
            "汇聚后的输出",
            NodeDataType.ANY,
            this
        ));

        addOutputPort(new BasePort(
            OUTPUT_SOURCE_ID,
            "来源",
            "输出来源：primary / secondary / none",
            NodeDataType.STRING,
            this
        ));
    }

    @Override
    public String getDescription() {
        return "将两路输入按优先级汇聚为一路输出";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object primary = inputValues.get(INPUT_PRIMARY_ID);
        Object secondary = inputValues.get(INPUT_SECONDARY_ID);

        boolean usePrimaryFirst = preferPrimary;
        Object preferPrimaryInput = inputValues.get(INPUT_PREFER_PRIMARY_ID);
        if (preferPrimaryInput instanceof Boolean value) {
            usePrimaryFirst = value;
        }

        Object result;
        String source;

        if (usePrimaryFirst) {
            if (primary != null) {
                result = primary;
                source = "primary";
            } else if (secondary != null) {
                result = secondary;
                source = "secondary";
            } else {
                result = null;
                source = "none";
            }
        } else {
            if (secondary != null) {
                result = secondary;
                source = "secondary";
            } else if (primary != null) {
                result = primary;
                source = "primary";
            } else {
                result = null;
                source = "none";
            }
        }

        outputValues.put(OUTPUT_SIGNAL_ID, result);
        outputValues.put(OUTPUT_SOURCE_ID, source);
    }

    public boolean isPreferPrimary() {
        return preferPrimary;
    }

    public void setPreferPrimary(boolean preferPrimary) {
        if (this.preferPrimary != preferPrimary) {
            this.preferPrimary = preferPrimary;
            markDirty();
        }
    }

    @Override
    public @Nullable Object getNodeState() {
        return preferPrimary;
    }

    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof Boolean value) {
            this.preferPrimary = value;
        }
    }
}
