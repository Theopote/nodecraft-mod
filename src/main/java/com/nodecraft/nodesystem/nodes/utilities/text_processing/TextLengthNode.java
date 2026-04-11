package com.nodecraft.nodesystem.nodes.utilities.text_processing;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Text Length 鑺傜偣锛岃幏鍙栧瓧绗︿覆鐨勯暱搴?
 */
@NodeInfo(
    id = "data.text.length",
    displayName = "鏂囨湰闀垮害",
    description = "鑾峰彇瀛楃涓茬殑闀垮害锛屽彲閫夋嫨鏄惁蹇界暐绌虹櫧瀛楃",
    category = "data.text"
)
public class TextLengthNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    private boolean ignoreWhitespace = false; // 鏄惁蹇界暐绌虹櫧瀛楃
    private String description = "Gets the length of a string"; // 鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_TEXT_ID = "input_text";
    private static final String OUTPUT_LENGTH_ID = "output_length";
    
    /**
     * 鏋勯€犱竴涓柊鐨勬枃鏈暱搴﹁妭鐐?
     */
    public TextLengthNode() {
        super(UUID.randomUUID(), "data.text.length");
        
        // 鍒涘缓杈撳叆绔彛
        IPort textInput = new BasePort(INPUT_TEXT_ID, "Text", 
                "The input text", NodeDataType.STRING, this);
        addInputPort(textInput);
        
        // 鍒涘缓杈撳嚭绔彛
        IPort lengthOutput = new BasePort(OUTPUT_LENGTH_ID, "Length", 
                "The length of the text", NodeDataType.INTEGER, this);
        addOutputPort(lengthOutput);
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
        
        // 榛樿闀垮害涓?
        int length = 0;
        
        // 璁＄畻瀛楃涓查暱搴?
        if (textObj != null) {
            String text = textObj.toString();
            
            if (ignoreWhitespace) {
                // 绉婚櫎鎵€鏈夌┖鐧藉瓧绗﹀悗璁＄畻闀垮害
                text = text.replaceAll("\\s", "");
            }
            
            length = text.length();
        }
        
        // 璁剧疆杈撳嚭
        outputValues.put(OUTPUT_LENGTH_ID, length);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isIgnoreWhitespace() {
        return ignoreWhitespace;
    }
    
    public void setIgnoreWhitespace(boolean ignore) {
        this.ignoreWhitespace = ignore;
        markDirty();
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("ignoreWhitespace", isIgnoreWhitespace());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("ignoreWhitespace")) {
                Object ignoreObj = stateMap.get("ignoreWhitespace");
                if (ignoreObj instanceof Boolean) {
                    setIgnoreWhitespace((Boolean) ignoreObj);
                }
            }
        }
    }
} 