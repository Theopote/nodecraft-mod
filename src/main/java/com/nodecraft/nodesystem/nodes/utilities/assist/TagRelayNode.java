package com.nodecraft.nodesystem.nodes.utilities.assist;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 标签中继节点：带文本标签的中继节点，便于在连线上做语义标注。
 */
@NodeInfo(
    id = "utilities.assist.tag_relay",
    displayName = "标签中继",
    description = "用于标注语义的中继节点，输入输出保持透传",
    category = "utilities.assist"
)
public class TagRelayNode extends BaseNode {

    private static final String INPUT_SIGNAL_ID = "input_signal";
    private static final String OUTPUT_SIGNAL_ID = "output_signal";

    private String tag = "标签";
    private String color = "#8BC34A";

    public TagRelayNode() {
        super(UUID.randomUUID(), "utilities.assist.tag_relay");

        addInputPort(new BasePort(
            INPUT_SIGNAL_ID,
            "输入",
            "需要透传的输入信号",
            NodeDataType.ANY,
            this
        ));

        addOutputPort(new BasePort(
            OUTPUT_SIGNAL_ID,
            "输出",
            "透传后的输出信号",
            NodeDataType.ANY,
            this
        ));
    }

    @Override
    public String getDescription() {
        return "用于标注语义的中继节点，输入输出保持透传";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        outputValues.put(OUTPUT_SIGNAL_ID, inputValues.get(INPUT_SIGNAL_ID));
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        String safe = tag == null ? "" : tag;
        if (!safe.equals(this.tag)) {
            this.tag = safe;
            markDirty();
        }
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        String safe = color == null ? "#8BC34A" : color;
        if (!safe.equals(this.color)) {
            this.color = safe;
            markDirty();
        }
    }

    @Override
    public @Nullable Object getNodeState() {
        Object[] state = new Object[2];
        state[0] = tag;
        state[1] = color;
        return state;
    }

    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof Object[] values && values.length >= 2) {
            if (values[0] instanceof String valueTag) {
                tag = valueTag;
            }
            if (values[1] instanceof String valueColor) {
                color = valueColor;
            }
        }
    }
}
