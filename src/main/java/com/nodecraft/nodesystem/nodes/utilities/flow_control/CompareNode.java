package com.nodecraft.nodesystem.nodes.utilities.flow_control;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Compare 鑺傜偣: 姣旇緝涓や釜鍊硷紝杈撳嚭姣旇緝缁撴灉銆?
 * 鏀寔鏁板€兼瘮杈冿紙>銆?銆?=銆?=銆?=銆?=锛夈€?
 */
@NodeInfo(
    id = "control.flow.compare",
    displayName = "姣旇緝",
    description = "姣旇緝涓や釜鍊肩殑澶у皬鍏崇郴",
    category = "control.flow"
)
public class CompareNode extends BaseNode {

    // --- 杈撳叆绔彛 IDs ---
    private static final String INPUT_A_ID = "input_a";
    private static final String INPUT_B_ID = "input_b";
    private static final String INPUT_MODE_ID = "input_mode";

    // --- 杈撳嚭绔彛 IDs ---
    private static final String OUTPUT_RESULT_ID = "output_result";
    private static final String OUTPUT_EQUAL_ID = "output_equal";
    private static final String OUTPUT_GREATER_ID = "output_greater";
    private static final String OUTPUT_LESS_ID = "output_less";

    // 姣旇緝妯″紡: 0=绛変簬, 1=涓嶇瓑浜? 2=澶т簬, 3=灏忎簬, 4=澶т簬绛変簬, 5=灏忎簬绛変簬
    private int compareMode = 0;

    // --- 鏋勯€犲嚱鏁?---
    public CompareNode() {
        super(UUID.randomUUID(), "control.flow.compare");
        
        addInputPort(new BasePort(INPUT_A_ID, "A",
                "绗竴涓€?, NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_B_ID, "B",
                "绗簩涓€?, NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_MODE_ID, "Mode",
                "姣旇緝妯″紡 (0=绛変簬, 1=涓嶇瓑浜? 2=澶т簬, 3=灏忎簬, 4=>=, 5=<=)", NodeDataType.INTEGER, this));
        
        addOutputPort(new BasePort(OUTPUT_RESULT_ID, "Result",
                "姣旇緝缁撴灉锛堝竷灏斿€硷級", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_EQUAL_ID, "A == B",
                "A 鏄惁绛変簬 B", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_GREATER_ID, "A > B",
                "A 鏄惁澶т簬 B", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_LESS_ID, "A < B",
                "A 鏄惁灏忎簬 B", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDescription() {
        return "姣旇緝涓や釜鍊肩殑澶у皬鍏崇郴";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object valA = inputValues.get(INPUT_A_ID);
        Object valB = inputValues.get(INPUT_B_ID);
        
        // 鑾峰彇姣旇緝妯″紡
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
        
        // 鏍规嵁妯″紡纭畾缁撴灉
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
