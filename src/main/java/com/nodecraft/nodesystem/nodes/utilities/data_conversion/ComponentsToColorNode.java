package com.nodecraft.nodesystem.nodes.utilities.data_conversion;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.datatypes.ColorData;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Components to Color               GBA                   ?
 */
@NodeInfo(
    id = "utilities.data_conversion.components_to_color",
    displayName = "Components to Color",
    description = "Creates a color from RGBA components",
    category = "utilities.data_conversion"
)
public class ComponentsToColorNode extends BaseNode {
    
    // ---              ?---
    private boolean inputFloatValues = true; //                        ?(0.0-1.0)                ?(0-255)
    private String description; //                    ?
    
    // ---       ?              D ---
    private static final String INPUT_RED_ID = "input_red";
    private static final String INPUT_GREEN_ID = "input_green";
    private static final String INPUT_BLUE_ID = "input_blue";
    private static final String INPUT_ALPHA_ID = "input_alpha";
    private static final String OUTPUT_COLOR_ID = "output_color";
    
    /**
     *                                          ?
     */
    public ComponentsToColorNode() {
        //                                        UID.randomUUID()              D
        super(UUID.randomUUID(), "utilities.data_conversion.components_to_color");
        
        //                    ?
        this.description = "Creates a color from RGBA components";
        
        //                    ?-         UBLE                                          ?
        IPort redInput = new BasePort(INPUT_RED_ID, "Red", 
                "The red component", NodeDataType.DOUBLE, this);
        addInputPort(redInput);
        
        IPort greenInput = new BasePort(INPUT_GREEN_ID, "Green", 
                "The green component", NodeDataType.DOUBLE, this);
        addInputPort(greenInput);
        
        IPort blueInput = new BasePort(INPUT_BLUE_ID, "Blue", 
                "The blue component", NodeDataType.DOUBLE, this);
        addInputPort(blueInput);
        
        IPort alphaInput = new BasePort(INPUT_ALPHA_ID, "Alpha", 
                "The alpha component (optional, defaults to 1.0)", NodeDataType.DOUBLE, this);
        addInputPort(alphaInput);
        
        //                    ?
        IPort colorOutput = new BasePort(OUTPUT_COLOR_ID, "Color", 
                "The resulting color", NodeDataType.COLOR, this);
        addOutputPort(colorOutput);
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
        Object redObj = inputValues.get(INPUT_RED_ID);
        Object greenObj = inputValues.get(INPUT_GREEN_ID);
        Object blueObj = inputValues.get(INPUT_BLUE_ID);
        Object alphaObj = inputValues.get(INPUT_ALPHA_ID);
        
        //          ?
        float red = 0f, green = 0f, blue = 0f, alpha = 1f;
        
        //                ?
        if (redObj instanceof Number) {
            red = ((Number) redObj).floatValue();
        }
        
        if (greenObj instanceof Number) {
            green = ((Number) greenObj).floatValue();
        }
        
        if (blueObj instanceof Number) {
            blue = ((Number) blueObj).floatValue();
        }
        
        if (alphaObj instanceof Number) {
            alpha = ((Number) alphaObj).floatValue();
        }
        
        //                             ?(0-255)                      ?(0.0-1.0)
        if (!inputFloatValues) {
            red /= 255f;
            green /= 255f;
            blue /= 255f;
            alpha /= 255f;
        }
        
        //                    ?
        ColorData color = new ColorData(red, green, blue, alpha);
        
        //              ?
        outputValues.put(OUTPUT_COLOR_ID, color);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isInputFloatValues() {
        return inputFloatValues;
    }
    
    public void setInputFloatValues(boolean useFloat) {
        this.inputFloatValues = useFloat;
        markDirty();
    }
    
    // ---                         ?---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("inputFloatValues", isInputFloatValues());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("inputFloatValues")) {
                Object useFloatObj = stateMap.get("inputFloatValues");
                if (useFloatObj instanceof Boolean) {
                    setInputFloatValues((Boolean) useFloatObj);
                }
            }
        }
    }
} 