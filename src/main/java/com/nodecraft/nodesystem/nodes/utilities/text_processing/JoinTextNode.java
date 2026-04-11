package com.nodecraft.nodesystem.nodes.utilities.text_processing;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Join Text 鑺傜偣锛屽皢鏂囨湰鍒楄〃鐢ㄥ垎闅旂杩炴帴鎴愬瓧绗︿覆
 */
@NodeInfo(
    id = "data.text.join",
    displayName = "鏂囨湰鍚堝苟",
    description = "灏嗘枃鏈垪琛ㄧ敤鍒嗛殧绗﹁繛鎺ユ垚瀛楃涓?,
    category = "data.text"
)
public class JoinTextNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    private String defaultDelimiter = ", "; // 榛樿鍒嗛殧绗?
    private String description = "Joins a list of strings into a single string with a delimiter"; // 鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_DELIMITER_ID = "input_delimiter";
    private static final String OUTPUT_TEXT_ID = "output_text";
    
    /**
     * 鏋勯€犱竴涓柊鐨勬枃鏈繛鎺ヨ妭鐐?
     */
    public JoinTextNode() {
        super(UUID.randomUUID(), "data.text.join");
        
        // 鍒涘缓杈撳叆绔彛
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list of strings to join", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        IPort delimiterInput = new BasePort(INPUT_DELIMITER_ID, "Delimiter", 
                "The delimiter to join with (default: comma+space)", NodeDataType.STRING, this);
        addInputPort(delimiterInput);
        
        // 鍒涘缓杈撳嚭绔彛
        IPort textOutput = new BasePort(OUTPUT_TEXT_ID, "Text", 
                "The joined string", NodeDataType.STRING, this);
        addOutputPort(textOutput);
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
        Object listObj = inputValues.get(INPUT_LIST_ID);
        Object delimiterObj = inputValues.get(INPUT_DELIMITER_ID);
        
        // 浣跨敤榛樿鍒嗛殧绗︽垨杈撳叆鍒嗛殧绗?
        String delimiter = defaultDelimiter;
        if (delimiterObj instanceof String) {
            delimiter = (String) delimiterObj;
        }
        
        // 鐢熸垚缁撴灉瀛楃涓?
        String result = "";
        
        if (listObj instanceof List) {
            List<?> list = (List<?>) listObj;
            StringBuilder sb = new StringBuilder();
            
            boolean first = true;
            for (Object item : list) {
                if (!first) {
                    sb.append(delimiter);
                }
                
                // 灏嗘瘡涓」鐩浆鎹负瀛楃涓?
                if (item != null) {
                    sb.append(item.toString());
                }
                
                first = false;
            }
            
            result = sb.toString();
        }
        
        // 璁剧疆杈撳嚭
        outputValues.put(OUTPUT_TEXT_ID, result);
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getDefaultDelimiter() {
        return defaultDelimiter;
    }
    
    public void setDefaultDelimiter(String delimiter) {
        this.defaultDelimiter = delimiter != null ? delimiter : ", ";
        markDirty();
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("defaultDelimiter", getDefaultDelimiter());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("defaultDelimiter")) {
                Object delimiterObj = stateMap.get("defaultDelimiter");
                if (delimiterObj instanceof String) {
                    setDefaultDelimiter((String) delimiterObj);
                }
            }
        }
    }
} 