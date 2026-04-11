package com.nodecraft.nodesystem.nodes.utilities.flow_control;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Branch 鑺傜偣: 鏍规嵁甯冨皵鏉′欢閫夋嫨杈撳嚭璺緞銆?
 * 绫讳技浜?if-else 璇彞锛屾牴鎹潯浠剁殑鐪熷亣閫夋嫨杈撳嚭 True 鍒嗘敮鎴?False 鍒嗘敮鐨勫€笺€?
 */
@NodeInfo(
    id = "control.flow.branch",
    displayName = "鏉′欢鍒嗘敮",
    description = "鏍规嵁甯冨皵鏉′欢閫夋嫨杈撳嚭璺緞锛坕f/else锛?,
    category = "control.flow"
)
public class BranchNode extends BaseNode {

    // --- 杈撳叆绔彛 IDs ---
    private static final String INPUT_CONDITION_ID = "input_condition";
    private static final String INPUT_TRUE_VALUE_ID = "input_true_value";
    private static final String INPUT_FALSE_VALUE_ID = "input_false_value";

    // --- 杈撳嚭绔彛 IDs ---
    private static final String OUTPUT_RESULT_ID = "output_result";
    private static final String OUTPUT_IS_TRUE_ID = "output_is_true";

    // --- 鏋勯€犲嚱鏁?---
    public BranchNode() {
        super(UUID.randomUUID(), "control.flow.branch");
        
        addInputPort(new BasePort(INPUT_CONDITION_ID, "Condition",
                "甯冨皵鏉′欢", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_TRUE_VALUE_ID, "True Value",
                "鏉′欢涓虹湡鏃惰緭鍑虹殑鍊?, NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_FALSE_VALUE_ID, "False Value",
                "鏉′欢涓哄亣鏃惰緭鍑虹殑鍊?, NodeDataType.ANY, this));
        
        addOutputPort(new BasePort(OUTPUT_RESULT_ID, "Result",
                "鏍规嵁鏉′欢閫夋嫨鐨勮緭鍑哄€?, NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_IS_TRUE_ID, "Is True",
                "鏉′欢鏄惁涓虹湡", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDescription() {
        return "鏍规嵁甯冨皵鏉′欢閫夋嫨杈撳嚭璺緞锛坕f/else锛?;
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object conditionObj = inputValues.get(INPUT_CONDITION_ID);
        Object trueValue = inputValues.get(INPUT_TRUE_VALUE_ID);
        Object falseValue = inputValues.get(INPUT_FALSE_VALUE_ID);
        
        boolean condition = false;
        
        // 鏀寔澶氱鏉′欢绫诲瀷
        if (conditionObj instanceof Boolean) {
            condition = (Boolean) conditionObj;
        } else if (conditionObj instanceof Number) {
            condition = ((Number) conditionObj).doubleValue() != 0;
        } else if (conditionObj instanceof String) {
            condition = !((String) conditionObj).isEmpty();
        } else if (conditionObj != null) {
            condition = true; // 闈?null 瀵硅薄瑙嗕负 true
        }
        
        outputValues.put(OUTPUT_IS_TRUE_ID, condition);
        
        if (condition) {
            outputValues.put(OUTPUT_RESULT_ID, trueValue);
        } else {
            outputValues.put(OUTPUT_RESULT_ID, falseValue);
        }
    }
}
