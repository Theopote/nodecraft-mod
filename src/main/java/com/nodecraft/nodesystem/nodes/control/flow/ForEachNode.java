package com.nodecraft.nodesystem.nodes.control.flow;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * ForEach 节点: 遍历一个列表，对每个元素输出当前元素和索引。
 * 适用于需要逐个处理列表元素的场景，例如遍历坐标列表进行批量操作。
 */
@NodeInfo(
    id = "control.flow.for_each",
    displayName = "ForEach 循环",
    description = "遍历列表中的每个元素，输出当前元素和索引",
    category = "control.flow"
)
public class ForEachNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_LIST_ID = "input_list";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_ELEMENT_ID = "output_element";
    private static final String OUTPUT_INDEX_ID = "output_index";
    private static final String OUTPUT_COUNT_ID = "output_count";
    private static final String OUTPUT_RESULTS_ID = "output_results";

    // --- 构造函数 ---
    public ForEachNode() {
        super(UUID.randomUUID(), "control.flow.for_each");
        
        addInputPort(new BasePort(INPUT_LIST_ID, "List",
                "要遍历的列表", NodeDataType.ANY, this));
        
        addOutputPort(new BasePort(OUTPUT_ELEMENT_ID, "Current Element",
                "当前遍历的元素", NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_INDEX_ID, "Index",
                "当前元素的索引（从0开始）", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Total Count",
                "列表中的元素总数", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_RESULTS_ID, "Results",
                "所有处理结果的列表", NodeDataType.ANY, this));
    }

    @Override
    public String getDescription() {
        return "遍历列表中的每个元素，输出当前元素和索引";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object listObj = inputValues.get(INPUT_LIST_ID);
        
        List<Object> results = new ArrayList<>();
        
        if (listObj instanceof List<?> list) {
            int count = list.size();
            outputValues.put(OUTPUT_COUNT_ID, count);
            
            // 遍历列表中的每个元素
            for (int i = 0; i < count; i++) {
                Object element = list.get(i);
                outputValues.put(OUTPUT_ELEMENT_ID, element);
                outputValues.put(OUTPUT_INDEX_ID, i);
                results.add(element);
            }
            
            // 最终输出最后一个元素（在当前的非嵌套执行模型中）
            // 未来可扩展为真正的循环执行
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
            // 如果输入不是列表，将其视为单元素列表
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
