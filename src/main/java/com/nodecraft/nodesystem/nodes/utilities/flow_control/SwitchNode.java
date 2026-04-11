package com.nodecraft.nodesystem.nodes.utilities.flow_control;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Switch 鑺傜偣: 澶氳矾鍒嗘敮閫夋嫨鍣ㄣ€?
 * 鏍规嵁杈撳叆鐨勭储寮曞€硷紝浠庡涓緭鍏ヤ腑閫夋嫨涓€涓綔涓鸿緭鍑恒€傜被浼间簬 switch-case 璇彞銆?
 */
@NodeInfo(
    id = "control.flow.switch_select",
    displayName = "閫夋嫨鍣?,
    description = "鏍规嵁绱㈠紩閫夋嫨澶氳矾杈撳叆涔嬩竴锛坰witch/case锛?,
    category = "control.flow"
)
public class SwitchNode extends BaseNode {

    // --- 杈撳叆绔彛 IDs ---
    private static final String INPUT_INDEX_ID = "input_index";
    private static final String INPUT_VALUE_0_ID = "input_value_0";
    private static final String INPUT_VALUE_1_ID = "input_value_1";
    private static final String INPUT_VALUE_2_ID = "input_value_2";
    private static final String INPUT_VALUE_3_ID = "input_value_3";
    private static final String INPUT_DEFAULT_ID = "input_default";

    // --- 杈撳嚭绔彛 IDs ---
    private static final String OUTPUT_RESULT_ID = "output_result";
    private static final String OUTPUT_MATCHED_INDEX_ID = "output_matched_index";

    // --- 鏋勯€犲嚱鏁?---
    public SwitchNode() {
        super(UUID.randomUUID(), "control.flow.switch_select");
        
        addInputPort(new BasePort(INPUT_INDEX_ID, "Index",
                "閫夋嫨绱㈠紩锛?-3锛?, NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_VALUE_0_ID, "Value 0",
                "绱㈠紩涓?鏃剁殑鍊?, NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_VALUE_1_ID, "Value 1",
                "绱㈠紩涓?鏃剁殑鍊?, NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_VALUE_2_ID, "Value 2",
                "绱㈠紩涓?鏃剁殑鍊?, NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_VALUE_3_ID, "Value 3",
                "绱㈠紩涓?鏃剁殑鍊?, NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_DEFAULT_ID, "Default",
                "绱㈠紩瓒呭嚭鑼冨洿鏃剁殑榛樿鍊?, NodeDataType.ANY, this));
        
        addOutputPort(new BasePort(OUTPUT_RESULT_ID, "Result",
                "閫変腑鐨勫€?, NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_MATCHED_INDEX_ID, "Matched Index",
                "瀹為檯鍖归厤鐨勭储寮?, NodeDataType.INTEGER, this));
    }

    @Override
    public String getDescription() {
        return "鏍规嵁绱㈠紩閫夋嫨澶氳矾杈撳叆涔嬩竴锛坰witch/case锛?;
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object indexObj = inputValues.get(INPUT_INDEX_ID);
        
        int index = 0;
        if (indexObj instanceof Number) {
            index = ((Number) indexObj).intValue();
        }
        
        String[] valueKeys = {INPUT_VALUE_0_ID, INPUT_VALUE_1_ID, INPUT_VALUE_2_ID, INPUT_VALUE_3_ID};
        
        Object result;
        int matchedIndex;
        
        if (index >= 0 && index < valueKeys.length) {
            result = inputValues.get(valueKeys[index]);
            matchedIndex = index;
            // 濡傛灉閫変腑鐨勫€间负null锛屼娇鐢ㄩ粯璁ゅ€?
            if (result == null) {
                result = inputValues.get(INPUT_DEFAULT_ID);
                matchedIndex = -1;
            }
        } else {
            // 绱㈠紩瓒呭嚭鑼冨洿锛屼娇鐢ㄩ粯璁ゅ€?
            result = inputValues.get(INPUT_DEFAULT_ID);
            matchedIndex = -1;
        }
        
        outputValues.put(OUTPUT_RESULT_ID, result);
        outputValues.put(OUTPUT_MATCHED_INDEX_ID, matchedIndex);
    }
}
