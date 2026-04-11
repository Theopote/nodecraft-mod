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
 * Number to Integer                                                       or     und     il        
 */
@NodeInfo(
    id = "utilities.data_conversion.number_to_integer",
    displayName = "Number to Integer",
    description = "Converts a floating point number to an integer with selectable rounding mode",
    category = "utilities.data_conversion"
)
public class NumberToIntegerNode extends BaseNode {
    
    // ---              ?---
    public enum RoundingMode {
        FLOOR, ROUND, CEIL
    }
    
    private RoundingMode roundingMode = RoundingMode.ROUND; //                       ?
    private String description; //                    ?
    
    // ---       ?              D ---
    private static final String INPUT_NUMBER_ID = "input_number";
    private static final String OUTPUT_INTEGER_ID = "output_integer";
    
    /**
     *                                               ?
     */
    public NumberToIntegerNode() {
        //                                        UID.randomUUID()              D
        super(UUID.randomUUID(), "utilities.data_conversion.number_to_integer");
        
        //                    ?
        this.description = "Converts a floating point number to an integer with selectable rounding mode";
        
        //                    ?
        IPort numberInput = new BasePort(INPUT_NUMBER_ID, "Number", 
                "The input number (float/double)", NodeDataType.DOUBLE, this);
        addInputPort(numberInput);
        
        //                    ?
        IPort integerOutput = new BasePort(OUTPUT_INTEGER_ID, "Integer", 
                "The converted integer value", NodeDataType.INTEGER, this);
        addOutputPort(integerOutput);
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
        
        //                ?
        int result = 0;
        
        //                                
        if (numberObj instanceof Number) {
            double number = ((Number) numberObj).doubleValue();
            
            //                                    ?
            switch (roundingMode) {
                case FLOOR:
                    result = (int) Math.floor(number);
                    break;
                case CEIL:
                    result = (int) Math.ceil(number);
                    break;
                case ROUND:
                default:
                    result = (int) Math.round(number);
                    break;
            }
        }
        
        //              ?
        outputValues.put(OUTPUT_INTEGER_ID, result);
    }
    
    // --- Getters/Setters for Properties ---
    
    public RoundingMode getRoundingMode() {
        return roundingMode;
    }
    
    public void setRoundingMode(RoundingMode mode) {
        this.roundingMode = mode;
        markDirty();
    }
    
    /**
     *                                                         I                      ?
     * @param modeStr                           "FLOOR", "ROUND", "CEIL"
     */
    public void setRoundingModeString(String modeStr) {
        try {
            setRoundingMode(RoundingMode.valueOf(modeStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            //                                                                  UND
            setRoundingMode(RoundingMode.ROUND);
        }
    }
    
    // ---                         ?---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("roundingMode", getRoundingMode().name());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("roundingMode")) {
                Object modeObj = stateMap.get("roundingMode");
                if (modeObj instanceof String) {
                    setRoundingModeString((String) modeObj);
                }
            }
        }
    }
} 