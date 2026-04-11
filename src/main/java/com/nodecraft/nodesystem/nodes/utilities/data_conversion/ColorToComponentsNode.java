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
 * Color to Components                                 GBA      ?
 */
@NodeInfo(
    id = "utilities.data_conversion.color_to_components",
    displayName = "Color to Components",
    description = "Extracts the RGBA components from a color",
    category = "utilities.data_conversion"
)
public class ColorToComponentsNode extends BaseNode {
    
    // ---              ?---
    private boolean outputFloatValues = true; //                       ?(0.0-1.0)                ?(0-255)
    private String description; //                    ?
    
    // ---       ?              D ---
    private static final String INPUT_COLOR_ID = "input_color";
    private static final String OUTPUT_RED_ID = "output_red";
    private static final String OUTPUT_GREEN_ID = "output_green";
    private static final String OUTPUT_BLUE_ID = "output_blue";
    private static final String OUTPUT_ALPHA_ID = "output_alpha";
    
    /**
     *                                          ?
     */
    public ColorToComponentsNode() {
        //                                        UID.randomUUID()              D
        super(UUID.randomUUID(), "utilities.data_conversion.color_to_components");
        
        //                    ?
        this.description = "Extracts the RGBA components from a color";
        
        //                    ?
        IPort colorInput = new BasePort(INPUT_COLOR_ID, "Color", 
                "The input color", NodeDataType.COLOR, this);
        addInputPort(colorInput);
        
        //                    ?-         UBLE                                          ?
        IPort redOutput = new BasePort(OUTPUT_RED_ID, "Red", 
                "The red component", NodeDataType.DOUBLE, this);
        addOutputPort(redOutput);
        
        IPort greenOutput = new BasePort(OUTPUT_GREEN_ID, "Green", 
                "The green component", NodeDataType.DOUBLE, this);
        addOutputPort(greenOutput);
        
        IPort blueOutput = new BasePort(OUTPUT_BLUE_ID, "Blue", 
                "The blue component", NodeDataType.DOUBLE, this);
        addOutputPort(blueOutput);
        
        IPort alphaOutput = new BasePort(OUTPUT_ALPHA_ID, "Alpha", 
                "The alpha component", NodeDataType.DOUBLE, this);
        addOutputPort(alphaOutput);
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
        Object colorObj = inputValues.get(INPUT_COLOR_ID);
        
        //                                           ?
        float red = 0, green = 0, blue = 0, alpha = 1;
        
        //                                
        if (colorObj instanceof ColorData) {
            ColorData color = (ColorData) colorObj;
            red = color.r();
            green = color.g();
            blue = color.b();
            alpha = color.a();
        }
        
        //                             ?
        if (outputFloatValues) {
            //          ?(0.0-1.0)
            outputValues.put(OUTPUT_RED_ID, red);
            outputValues.put(OUTPUT_GREEN_ID, green);
            outputValues.put(OUTPUT_BLUE_ID, blue);
            outputValues.put(OUTPUT_ALPHA_ID, alpha);
        } else {
            //          ?(0-255)
            outputValues.put(OUTPUT_RED_ID, Math.round(red * 255));
            outputValues.put(OUTPUT_GREEN_ID, Math.round(green * 255));
            outputValues.put(OUTPUT_BLUE_ID, Math.round(blue * 255));
            outputValues.put(OUTPUT_ALPHA_ID, Math.round(alpha * 255));
        }
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isOutputFloatValues() {
        return outputFloatValues;
    }
    
    public void setOutputFloatValues(boolean useFloat) {
        this.outputFloatValues = useFloat;
        markDirty();
    }
    
    // ---                         ?---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("outputFloatValues", isOutputFloatValues());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("outputFloatValues")) {
                Object useFloatObj = stateMap.get("outputFloatValues");
                if (useFloatObj instanceof Boolean) {
                    setOutputFloatValues((Boolean) useFloatObj);
                }
            }
        }
    }
} 