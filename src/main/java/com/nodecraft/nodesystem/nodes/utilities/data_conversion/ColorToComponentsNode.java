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
 * Color to Components 鑺傜偣锛屽皢棰滆壊鎷嗗垎涓篟GBA鍒嗛噺
 */
@NodeInfo(
    id = "data.conversion.color_to_components",
    displayName = "Color to Components",
    description = "Extracts the RGBA components from a color",
    category = "data.conversion"
)
public class ColorToComponentsNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    private boolean outputFloatValues = true; // 鏄惁杈撳嚭娴偣鍊?(0.0-1.0) 杩樻槸鏁存暟鍊?(0-255)
    private String description; // 瀛樺偍鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_COLOR_ID = "input_color";
    private static final String OUTPUT_RED_ID = "output_red";
    private static final String OUTPUT_GREEN_ID = "output_green";
    private static final String OUTPUT_BLUE_ID = "output_blue";
    private static final String OUTPUT_ALPHA_ID = "output_alpha";
    
    /**
     * 鏋勯€犱竴涓柊鐨勯鑹插垎瑙ｈ妭鐐?
     */
    public ColorToComponentsNode() {
        // 璋冪敤鐖剁被鏋勯€犲嚱鏁帮紝浣跨敤UUID.randomUUID()鐢熸垚鏂扮殑ID
        super(UUID.randomUUID(), "data.conversion.color_to_components");
        
        // 璁剧疆鑺傜偣鎻忚堪
        this.description = "Extracts the RGBA components from a color";
        
        // 鍒涘缓杈撳叆绔彛
        IPort colorInput = new BasePort(INPUT_COLOR_ID, "Color", 
                "The input color", NodeDataType.COLOR, this);
        addInputPort(colorInput);
        
        // 鍒涘缓杈撳嚭绔彛 - 浣跨敤DOUBLE绫诲瀷浠ユ敮鎸佹暣鏁板拰娴偣鍊艰緭鍑?
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
        Object colorObj = inputValues.get(INPUT_COLOR_ID);
        
        // 榛樿鍊硷紙榛戣壊锛屽畬鍏ㄤ笉閫忔槑锛?
        float red = 0, green = 0, blue = 0, alpha = 1;
        
        // 妫€鏌ヨ緭鍏ユ槸鍚︿负棰滆壊
        if (colorObj instanceof ColorData) {
            ColorData color = (ColorData) colorObj;
            red = color.r();
            green = color.g();
            blue = color.b();
            alpha = color.a();
        }
        
        // 鏍规嵁杈撳嚭绫诲瀷璁剧疆鍊?
        if (outputFloatValues) {
            // 娴偣鍊?(0.0-1.0)
            outputValues.put(OUTPUT_RED_ID, red);
            outputValues.put(OUTPUT_GREEN_ID, green);
            outputValues.put(OUTPUT_BLUE_ID, blue);
            outputValues.put(OUTPUT_ALPHA_ID, alpha);
        } else {
            // 鏁存暟鍊?(0-255)
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
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
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