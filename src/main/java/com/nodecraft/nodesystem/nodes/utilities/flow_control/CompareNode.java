package com.nodecraft.nodesystem.nodes.utilities.flow_control;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Compare node: compares two values and outputs comparison results.
 * Supports operators ==, !=, >, <, >=, <=.
 */
@NodeInfo(
    id = "utilities.flow_control.compare",
    displayName = "Compare",
    description = "Compares two values and outputs relation results.",
    category = "utilities.flow_control"
)
public class CompareNode extends BaseNode {

    // --- Input port IDs ---
    private static final String INPUT_A_ID = "input_a";
    private static final String INPUT_B_ID = "input_b";
    private static final String INPUT_MODE_ID = "input_mode";

    // --- Output port IDs ---
    private static final String OUTPUT_RESULT_ID = "output_result";
    private static final String OUTPUT_EQUAL_ID = "output_equal";
    private static final String OUTPUT_GREATER_ID = "output_greater";
    private static final String OUTPUT_LESS_ID = "output_less";

        // Compare mode: 0==, 1!=, 2>, 3<, 4>=, 5<=
    private int compareMode = 0;

        // --- Constructor ---
    public CompareNode() {
        super(UUID.randomUUID(), "utilities.flow_control.compare");
        
        addInputPort(new BasePort(INPUT_A_ID, "A",
            "First value", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_B_ID, "B",
            "Second value", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_MODE_ID, "Mode",
            "Comparison mode (0==, 1!=, 2>, 3<, 4>=, 5<=)", NodeDataType.INTEGER, this));
        
        addOutputPort(new BasePort(OUTPUT_RESULT_ID, "Result",
            "Result for selected comparison mode", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_EQUAL_ID, "A == B",
            "Whether A equals B", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_GREATER_ID, "A > B",
            "Whether A is greater than B", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_LESS_ID, "A < B",
            "Whether A is less than B", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDescription() {
        return "Compares two values and outputs relation results.";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object valA = inputValues.get(INPUT_A_ID);
        Object valB = inputValues.get(INPUT_B_ID);
        
        // Read compare mode
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
        
        // Evaluate selected compare mode
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
