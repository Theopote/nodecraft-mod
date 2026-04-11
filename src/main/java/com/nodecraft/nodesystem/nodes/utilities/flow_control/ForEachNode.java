package com.nodecraft.nodesystem.nodes.utilities.flow_control;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * ForEach 鑺傜偣: 閬嶅巻涓€涓垪琛紝瀵规瘡涓厓绱犺緭鍑哄綋鍓嶅厓绱犲拰绱㈠紩銆?
 * 閫傜敤浜庨渶瑕侀€愪釜澶勭悊鍒楄〃鍏冪礌鐨勫満鏅紝渚嬪閬嶅巻鍧愭爣鍒楄〃杩涜鎵归噺鎿嶄綔銆?
 */
@NodeInfo(
    id = "control.flow.for_each",
    displayName = "ForEach 寰幆",
    description = "閬嶅巻鍒楄〃涓殑姣忎釜鍏冪礌锛岃緭鍑哄綋鍓嶅厓绱犲拰绱㈠紩",
    category = "control.flow"
)
public class ForEachNode extends BaseNode {

    // --- 杈撳叆绔彛 IDs ---
    private static final String INPUT_LIST_ID = "input_list";

    // --- 杈撳嚭绔彛 IDs ---
    private static final String OUTPUT_ELEMENT_ID = "output_element";
    private static final String OUTPUT_INDEX_ID = "output_index";
    private static final String OUTPUT_COUNT_ID = "output_count";
    private static final String OUTPUT_RESULTS_ID = "output_results";

    // --- 鏋勯€犲嚱鏁?---
    public ForEachNode() {
        super(UUID.randomUUID(), "control.flow.for_each");
        
        addInputPort(new BasePort(INPUT_LIST_ID, "List",
                "瑕侀亶鍘嗙殑鍒楄〃", NodeDataType.ANY, this));
        
        addOutputPort(new BasePort(OUTPUT_ELEMENT_ID, "Current Element",
                "褰撳墠閬嶅巻鐨勫厓绱?, NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_INDEX_ID, "Index",
                "褰撳墠鍏冪礌鐨勭储寮曪紙浠?寮€濮嬶級", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Total Count",
                "鍒楄〃涓殑鍏冪礌鎬绘暟", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_RESULTS_ID, "Results",
                "鎵€鏈夊鐞嗙粨鏋滅殑鍒楄〃", NodeDataType.ANY, this));
    }

    @Override
    public String getDescription() {
        return "閬嶅巻鍒楄〃涓殑姣忎釜鍏冪礌锛岃緭鍑哄綋鍓嶅厓绱犲拰绱㈠紩";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object listObj = inputValues.get(INPUT_LIST_ID);
        
        List<Object> results = new ArrayList<>();
        
        if (listObj instanceof List<?> list) {
            int count = list.size();
            outputValues.put(OUTPUT_COUNT_ID, count);
            
            // 閬嶅巻鍒楄〃涓殑姣忎釜鍏冪礌
            for (int i = 0; i < count; i++) {
                Object element = list.get(i);
                outputValues.put(OUTPUT_ELEMENT_ID, element);
                outputValues.put(OUTPUT_INDEX_ID, i);
                results.add(element);
            }
            
            // 鏈€缁堣緭鍑烘渶鍚庝竴涓厓绱狅紙鍦ㄥ綋鍓嶇殑闈炲祵濂楁墽琛屾ā鍨嬩腑锛?
            // 鏈潵鍙墿灞曚负鐪熸鐨勫惊鐜墽琛?
        } else if (listObj instanceof Collection<?> collection) {
            int count = collection.size();
            outputValues.put(OUTPUT_COUNT_ID, count);
            
            int i = 0;
            Object lastElement = null;
            for (Object element : collection) {
                outputValues.put(OUTPUT_ELEMENT_ID, element);
                outputValues.put(OUTPUT_INDEX_ID, i);
                results.add(element);
                lastElement = element;
                i++;
            }
        } else if (listObj != null) {
            // 濡傛灉杈撳叆涓嶆槸鍒楄〃锛屽皢鍏惰涓哄崟鍏冪礌鍒楄〃
            outputValues.put(OUTPUT_ELEMENT_ID, listObj);
            outputValues.put(OUTPUT_INDEX_ID, 0);
            outputValues.put(OUTPUT_COUNT_ID, 1);
            results.add(listObj);
        } else {
            outputValues.put(OUTPUT_ELEMENT_ID, null);
            outputValues.put(OUTPUT_INDEX_ID, 0);
            outputValues.put(OUTPUT_COUNT_ID, 0);
        }
        
        outputValues.put(OUTPUT_RESULTS_ID, results);
    }
}
