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
 * Text to Value 鑺傜偣锛屽皾璇曞皢鏂囨湰瑙ｆ瀽涓烘暟瀛楁垨甯冨皵鍊?
 */
@NodeInfo(
    id = "data.conversion.text_to_value",
    displayName = "鏂囨湰杞暟鍊?,
    description = "灏濊瘯灏嗘枃鏈В鏋愪负鏁板瓧鎴栧竷灏斿€?,
    category = "data.conversion"
)
public class TextToValueNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    public enum OutputType {
        NUMBER, BOOLEAN, INTEGER
    }
    
    private OutputType outputType = OutputType.NUMBER; // 榛樿杈撳嚭绫诲瀷涓烘暟瀛?
    private String description; // 瀛樺偍鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_TEXT_ID = "input_text";
    private static final String OUTPUT_VALUE_ID = "output_value";
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    
    /**
     * 鏋勯€犱竴涓柊鐨勬枃鏈浆鍊艰妭鐐?
     */
    public TextToValueNode() {
        // 璋冪敤鐖剁被鏋勯€犲嚱鏁帮紝浣跨敤UUID.randomUUID()鐢熸垚鏂扮殑ID
        super(UUID.randomUUID(), "data.conversion.text_to_value");
        
        // 璁剧疆鑺傜偣鎻忚堪
        this.description = "Tries to parse text into a number or boolean value";
        
        // 鍒涘缓杈撳叆绔彛
        IPort textInput = new BasePort(INPUT_TEXT_ID, "Text", 
                "The input text to parse", NodeDataType.STRING, this);
        addInputPort(textInput);
        
        // 鍒涘缓杈撳嚭绔彛
        IPort valueOutput = new BasePort(OUTPUT_VALUE_ID, "Value", 
                "The parsed value (number or boolean)", NodeDataType.ANY, this);
        addOutputPort(valueOutput);
        
        IPort successOutput = new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "Whether the parsing was successful", NodeDataType.BOOLEAN, this);
        addOutputPort(successOutput);
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
        Object textObj = inputValues.get(INPUT_TEXT_ID);
        
        // 榛樿杈撳嚭鍊煎拰鎴愬姛鐘舵€?
        Object result = null;
        boolean success = false;
        
        // 纭繚杈撳叆鏄瓧绗︿覆
        if (textObj instanceof String) {
            String text = (String) textObj;
            text = text.trim(); // 绉婚櫎鍓嶅悗绌虹櫧
            
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
                // 瑙ｆ瀽澶辫触锛屼繚鎸侀粯璁ゅ€?
                success = false;
            }
        }
        
        // 璁剧疆杈撳嚭
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
     * 璁剧疆杈撳嚭绫诲瀷锛堝瓧绗︿覆褰㈠紡锛岀敤浜庝粠UI鎴栭厤缃腑璁剧疆锛?
     * @param typeStr 杈撳嚭绫诲瀷瀛楃涓诧細"NUMBER", "BOOLEAN", "INTEGER"
     */
    public void setOutputTypeString(String typeStr) {
        try {
            setOutputType(OutputType.valueOf(typeStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            // 濡傛灉瀛楃涓蹭笉鍖归厤浠讳綍绫诲瀷锛屼娇鐢ㄩ粯璁ょ殑NUMBER
            setOutputType(OutputType.NUMBER);
        }
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
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