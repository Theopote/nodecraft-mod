package com.nodecraft.nodesystem.nodes.control.flow;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Compare 节点: 比较两个值，输出比较结果。
 * 支持数值比较（>、<、>=、<=、==、!=）。
 */
@NodeInfo(
    id = "control.flow.compare",
    displayName = "比较",
    description = "比较两个值的大小关系",
    category = "control.flow"
)
public class CompareNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_A_ID = "input_a";
    private static final String INPUT_B_ID = "input_b";
    private static final String INPUT_MODE_ID = "input_mode";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_RESULT_ID = "output_result";
    private static final String OUTPUT_EQUAL_ID = "output_equal";
    private static final String OUTPUT_GREATER_ID = "output_greater";
    private static final String OUTPUT_LESS_ID = "output_less";

    // 比较模式: 0=等于, 1=不等于, 2=大于, 3=小于, 4=大于等于, 5=小于等于
    private int compareMode = 0;

    // --- 构造函数 ---
    public CompareNode() {
        super(UUID.randomUUID(), "control.flow.compare");
        
        addInputPort(new BasePort(INPUT_A_ID, "A",
                "第一个值", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_B_ID, "B",
                "第二个值", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_MODE_ID, "Mode",
                "比较模式 (0=等于, 1=不等于, 2=大于, 3=小于, 4=>=, 5=<=)", NodeDataType.INTEGER, this));
        
        addOutputPort(new BasePort(OUTPUT_RESULT_ID, "Result",
                "比较结果（布尔值）", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_EQUAL_ID, "A == B",
                "A 是否等于 B", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_GREATER_ID, "A > B",
                "A 是否大于 B", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_LESS_ID, "A < B",
                "A 是否小于 B", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDescription() {
        return "比较两个值的大小关系";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object valA = inputValues.get(INPUT_A_ID);
        Object valB = inputValues.get(INPUT_B_ID);
        
        // 获取比较模式
        Object modeObj = inputValues.get(INPUT_MODE_ID);
        int mode = this.compareMode;
        if (modeObj instanceof Number) {
            mode = ((Number) modeObj).intValue();
        }
        
        boolean isEqual = false;
        boolean isGreater = false;
        boolean isLess = false;
        
        if (valA instanceof Number && valB instanceof Number) {
            double a = ((Number) valA).doubleValue();
            double b = ((Number) valB).doubleValue();
            isEqual = Double.compare(a, b) == 0;
            isGreater = a > b;
            isLess = a < b;
        } else if (valA instanceof String && valB instanceof String) {
            int cmp = ((String) valA).compareTo((String) valB);
            isEqual = cmp == 0;
            isGreater = cmp > 0;
            isLess = cmp < 0;
        } else if (valA != null && valB != null) {
            isEqual = valA.equals(valB);
        } else {
            isEqual = (valA == null && valB == null);
        }
        
        // 根据模式确定结果
        boolean result = switch (mode) {
            case 0 -> isEqual;           // ==
            case 1 -> !isEqual;          // !=
            case 2 -> isGreater;         // >
            case 3 -> isLess;            // <
            case 4 -> isGreater || isEqual; // >=
            case 5 -> isLess || isEqual;   // <=
            default -> isEqual;
        };
        
        outputValues.put(OUTPUT_RESULT_ID, result);
        outputValues.put(OUTPUT_EQUAL_ID, isEqual);
        outputValues.put(OUTPUT_GREATER_ID, isGreater);
        outputValues.put(OUTPUT_LESS_ID, isLess);
    }
}
