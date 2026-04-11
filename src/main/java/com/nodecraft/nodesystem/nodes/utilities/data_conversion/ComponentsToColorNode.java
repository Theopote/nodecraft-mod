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
 * Components to Color 鑺傜偣锛屼粠RGBA鍒嗛噺鏋勫缓棰滆壊
 */
@NodeInfo(
    id = "data.conversion.components_to_color",
    displayName = "Components to Color",
    description = "Creates a color from RGBA components",
    category = "data.conversion"
)
public class ComponentsToColorNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    private boolean inputFloatValues = true; // 鏄惁鎺ュ彈娴偣鍊?(0.0-1.0) 杩樻槸鏁存暟鍊?(0-255)
    private String description; // 瀛樺偍鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_RED_ID = "input_red";
    private static final String INPUT_GREEN_ID = "input_green";
    private static final String INPUT_BLUE_ID = "input_blue";
    private static final String INPUT_ALPHA_ID = "input_alpha";
    private static final String OUTPUT_COLOR_ID = "output_color";
    
    /**
     * 鏋勯€犱竴涓柊鐨勯鑹插悎鎴愯妭鐐?
     */
    public ComponentsToColorNode() {
        // 璋冪敤鐖剁被鏋勯€犲嚱鏁帮紝浣跨敤UUID.randomUUID()鐢熸垚鏂扮殑ID
        super(UUID.randomUUID(), "data.conversion.components_to_color");
        
        // 璁剧疆鑺傜偣鎻忚堪
        this.description = "Creates a color from RGBA components";
        
        // 鍒涘缓杈撳叆绔彛 - 浣跨敤DOUBLE绫诲瀷浠ユ敮鎸佹暣鏁板拰娴偣鍊艰緭鍏?
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
        
        // 鍒涘缓杈撳嚭绔彛
        IPort colorOutput = new BasePort(OUTPUT_COLOR_ID, "Color", 
                "The resulting color", NodeDataType.COLOR, this);
        addOutputPort(colorOutput);
    }
    
    /**
     * 瀹炵幇INode鎺ュ彛鐨刧etDescription鏂规硶
     * @return 鑺傜偣鎻忚堪
     */
    @Override
    public String getDescription() {
        return this.description;
    }
    
    /**
     * 鑺傜偣鐨勮绠楅€昏緫
     * @param context 鎵ц涓婁笅鏂?
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 鑾峰彇杈撳叆
        Object redObj = inputValues.get(INPUT_RED_ID);
        Object greenObj = inputValues.get(INPUT_GREEN_ID);
        Object blueObj = inputValues.get(INPUT_BLUE_ID);
        Object alphaObj = inputValues.get(INPUT_ALPHA_ID);
        
        // 榛樿鍊?
        float red = 0f, green = 0f, blue = 0f, alpha = 1f;
        
        // 澶勭悊杈撳叆鍊?
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
        
        // 濡傛灉杈撳叆鏄暣鏁板€?(0-255)锛岃浆鎹负娴偣鍊?(0.0-1.0)
        if (!inputFloatValues) {
            red /= 255f;
            green /= 255f;
            blue /= 255f;
            alpha /= 255f;
        }
        
        // 鍒涘缓棰滆壊瀵硅薄
        ColorData color = new ColorData(red, green, blue, alpha);
        
        // 璁剧疆杈撳嚭
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
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
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