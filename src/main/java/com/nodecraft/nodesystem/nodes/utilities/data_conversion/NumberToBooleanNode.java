package com.nodecraft.nodesystem.nodes.utilities.data_conversion;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Number to Boolean                                         ?
 * 0            lse      ?            ue
 */
@NodeInfo(
    id = "utilities.data_conversion.number_to_boolean",
    displayName = "Number to Boolean",
    description = "Converts a number to boolean (0 = false, non-zero = true)",
    category = "utilities.data_conversion"
)
public class NumberToBooleanNode extends BaseNode {
    
    // ---       ?---
    private boolean invertResult = false; //                     ?
    private String description; //                    ?
    
    // ---       ?              D ---
    private static final String INPUT_NUMBER_ID = "input_number";
    private static final String OUTPUT_BOOLEAN_ID = "output_boolean";
    
    /**
     *                                                 ?
     */
    public NumberToBooleanNode() {
        //                                        UID.randomUUID()              D
        super(UUID.randomUUID(), "utilities.data_conversion.number_to_boolean");
        
        //                    ?
        this.description = "Converts a number to boolean (0 = false, non-zero = true)";
        
        //                    ?
        IPort numberInput = new BasePort(INPUT_NUMBER_ID, "Number", 
                "The input number", NodeDataType.DOUBLE, this);
        addInputPort(numberInput);
        
        //                    ?
        IPort booleanOutput = new BasePort(OUTPUT_BOOLEAN_ID, "Boolean", 
                "The converted boolean value", NodeDataType.BOOLEAN, this);
        addOutputPort(booleanOutput);
    }
    
    /**
     *         ode            tDescription      ?
     * @return              ?
     */
    @Override
    public String getDescription() {
        return this.description;
    }
    
    /**
     *                         ?
     * @param context                ?
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        //              ?
        Object numberObj = inputValues.get(INPUT_NUMBER_ID);
        
        //                   alse
        boolean result = false;
        
        //                                
        if (numberObj instanceof Number) {
            double number = ((Number) numberObj).doubleValue();
            //                        ?            ue
            result = number != 0;
        }
        
        //                                           ?
        if (invertResult) {
            result = !result;
        }
        
        //              ?
        outputValues.put(OUTPUT_BOOLEAN_ID, result);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isInvertResult() {
        return invertResult;
    }
    
    public void setInvertResult(boolean invert) {
        this.invertResult = invert;
        markDirty();
    }
    
    // ---                         ?---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("invertResult", isInvertResult());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("invertResult")) {
                Object invertObj = stateMap.get("invertResult");
                if (invertObj instanceof Boolean) {
                    setInvertResult((Boolean) invertObj);
                }
            }
        }
    }
} 