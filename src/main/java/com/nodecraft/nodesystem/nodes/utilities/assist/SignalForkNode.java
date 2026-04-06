package com.nodecraft.nodesystem.nodes.utilities.assist;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 分线节点：将一路输入透传到两路输出，用于整理连线布局。
 */
@NodeInfo(
    id = "utilities.assist.signal_fork",
    displayName = "分线节点",
    description = "将一路输入透传到两路输出，便于连线分流",
    category = "utilities.assist"
)
public class SignalForkNode extends BaseNode {

    private static final String INPUT_SIGNAL_ID = "input_signal";
    private static final String OUTPUT_A_ID = "output_a";
    private static final String OUTPUT_B_ID = "output_b";

    public SignalForkNode() {
        super(UUID.randomUUID(), "utilities.assist.signal_fork");

        addInputPort(new BasePort(
            INPUT_SIGNAL_ID,
            "输入",
            "需要分流的输入信号",
            NodeDataType.ANY,
            this
        ));

        addOutputPort(new BasePort(
            OUTPUT_A_ID,
            "输出 A",
            "分流输出 A",
            NodeDataType.ANY,
            this
        ));

        addOutputPort(new BasePort(
            OUTPUT_B_ID,
            "输出 B",
            "分流输出 B",
            NodeDataType.ANY,
            this
        ));
    }

    @Override
    public String getDescription() {
        return "将一路输入透传到两路输出，便于连线分流";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object value = inputValues.get(INPUT_SIGNAL_ID);
        outputValues.put(OUTPUT_A_ID, value);
        outputValues.put(OUTPUT_B_ID, value);
    }
}
