package com.nodecraft.nodesystem.nodes.deferred.out_of_scope;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * ForEach node: iterates over list items and exposes element/index outputs.
 * Useful for per-item processing workflows.
 */
@NodeInfo(
    id = "deferred.out_of_scope.for_each",
    displayName = "ForEach",
    description = "Iterates list elements and outputs current item and index.",
    category = "deferred.out_of_scope"
)
public class ForEachNode extends BaseNode {

    // ---              ?IDs ---
    private static final String INPUT_LIST_ID = "input_list";

    // ---              ?IDs ---
    private static final String OUTPUT_ELEMENT_ID = "output_element";
    private static final String OUTPUT_INDEX_ID = "output_index";
    private static final String OUTPUT_COUNT_ID = "output_count";
    private static final String OUTPUT_RESULTS_ID = "output_results";

    // ---              ?---
    public ForEachNode() {
        super(UUID.randomUUID(), "deferred.out_of_scope.for_each");
        
        addInputPort(new BasePort(INPUT_LIST_ID, "List",
            "List or collection to iterate", NodeDataType.ANY, this));
        
        addOutputPort(new BasePort(OUTPUT_ELEMENT_ID, "Current Element",
            "Current iterated element", NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_INDEX_ID, "Index",
            "Current element index (0-based)", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Total Count",
            "Total number of items", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_RESULTS_ID, "Results",
            "Collected iteration results", NodeDataType.ANY, this));
    }

    @Override
    public String getDescription() {
        return "Iterates list elements and outputs current item and index.";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        Object listObj = inputValues.get(INPUT_LIST_ID);
        
        List<Object> results = new ArrayList<>();
        
        if (listObj instanceof List<?> list) {
            int count = list.size();
            outputValues.put(OUTPUT_COUNT_ID, count);
            
            //                                   ?
            for (int i = 0; i < count; i++) {
                Object element = list.get(i);
                outputValues.put(OUTPUT_ELEMENT_ID, element);
                outputValues.put(OUTPUT_INDEX_ID, i);
                results.add(element);
            }
            
            //                                                                                 ?
            //                                           ?
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
            //                                                               ?
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
