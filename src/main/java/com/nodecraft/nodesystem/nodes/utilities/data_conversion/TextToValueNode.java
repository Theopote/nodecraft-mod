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
 * Text-to-value parser node for number/boolean/integer conversion.
 */
@NodeInfo(
    id = "utilities.data_conversion.text_to_value",
    displayName = "Text to Value",
    description = "Parses text into number, integer, or boolean values.",
    category = "utilities.data_conversion"
)
public class TextToValueNode extends BaseNode {
    
    // ---              ?---
    public enum OutputType {
        NUMBER, BOOLEAN, INTEGER
    }
    
    private OutputType outputType = OutputType.NUMBER; //                             ?
    private String description; //                    ?
    
    // ---       ?              D ---
    private static final String INPUT_TEXT_ID = "input_text";
    private static final String OUTPUT_VALUE_ID = "output_value";
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    
    /**
     *                                         ?
     */
    public TextToValueNode() {
        //                                        UID.randomUUID()              D
        super(UUID.randomUUID(), "utilities.data_conversion.text_to_value");
        
        //                    ?
        this.description = "Tries to parse text into a number or boolean value";
        
        //                    ?
        IPort textInput = new BasePort(INPUT_TEXT_ID, "Text", 
                "The input text to parse", NodeDataType.STRING, this);
        addInputPort(textInput);
        
        //                    ?
        IPort valueOutput = new BasePort(OUTPUT_VALUE_ID, "Value", 
                "The parsed value (number or boolean)", NodeDataType.ANY, this);
        addOutputPort(valueOutput);
        
        IPort successOutput = new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "Whether the parsing was successful", NodeDataType.BOOLEAN, this);
        addOutputPort(successOutput);
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
        Object textObj = inputValues.get(INPUT_TEXT_ID);
        
        //                                  ?
        Object result = null;
        boolean success = false;
        
        //                           ?
        if (textObj instanceof String) {
            String text = (String) textObj;
            text = text.trim(); //                    ?
            
            try {
                switch (outputType) {
                    case NUMBER:
                        if (!text.isEmpty()) {
                            result = Double.parseDouble(text);
                            success = true;
                        }
                        break;
                    case INTEGER:
                        if (!text.isEmpty()) {
                            result = Integer.parseInt(text);
                            success = true;
                        }
                        break;
                    case BOOLEAN:
                        if (text.equalsIgnoreCase("true") || text.equals("1")) {
                            result = true;
                            success = true;
                        } else if (text.equalsIgnoreCase("false") || text.equals("0")) {
                            result = false;
                            success = true;
                        }
                        break;
                }
            } catch (NumberFormatException e) {
                //                                   ?
                success = false;
            }
        }
        
        //              ?
        outputValues.put(OUTPUT_VALUE_ID, result);
        outputValues.put(OUTPUT_SUCCESS_ID, success);
    }
    
    // --- Getters/Setters for Properties ---
    
    public OutputType getOutputType() {
        return outputType;
    }
    
    public void setOutputType(OutputType type) {
        this.outputType = type;
        markDirty();
    }
    
    /**
     *                                                        I                      ?
     * @param typeStr                             "NUMBER", "BOOLEAN", "INTEGER"
     */
    public void setOutputTypeString(String typeStr) {
        try {
            setOutputType(OutputType.valueOf(typeStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            //                                                                UMBER
            setOutputType(OutputType.NUMBER);
        }
    }
    
    // ---                         ?---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("outputType", getOutputType().name());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("outputType")) {
                Object typeObj = stateMap.get("outputType");
                if (typeObj instanceof String) {
                    setOutputTypeString((String) typeObj);
                }
            }
        }
    }
} 