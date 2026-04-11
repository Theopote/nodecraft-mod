package com.nodecraft.nodesystem.nodes.utilities.text_processing;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Split Text 鑺傜偣锛屾寜鍒嗛殧绗﹀皢鏂囨湰鎷嗗垎鎴愬垪琛?
 */
@NodeInfo(
    id = "data.text.split",
    displayName = "鏂囨湰鎷嗗垎",
    description = "鎸夊垎闅旂灏嗘枃鏈媶鍒嗘垚鍒楄〃",
    category = "data.text"
)
public class SplitTextNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    private String defaultDelimiter = ","; // 榛樿鍒嗛殧绗?
    private boolean trimResults = true;    // 鏄惁鍘婚櫎缁撴灉鐨勫墠鍚庣┖鐧?
    private boolean skipEmptyResults = false; // 鏄惁璺宠繃绌虹粨鏋?
    private String description = "Splits text into a list using a delimiter"; // 鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_TEXT_ID = "input_text";
    private static final String INPUT_DELIMITER_ID = "input_delimiter";
    private static final String OUTPUT_LIST_ID = "output_list";
    private static final String OUTPUT_COUNT_ID = "output_count";
    
    /**
     * 鏋勯€犱竴涓柊鐨勬枃鏈媶鍒嗚妭鐐?
     */
    public SplitTextNode() {
        super(UUID.randomUUID(), "data.text.split");
        
        // 鍒涘缓杈撳叆绔彛
        IPort textInput = new BasePort(INPUT_TEXT_ID, "Text", 
                "The input text to split", NodeDataType.STRING, this);
        addInputPort(textInput);
        
        IPort delimiterInput = new BasePort(INPUT_DELIMITER_ID, "Delimiter", 
                "The delimiter to split by (default: comma)", NodeDataType.STRING, this);
        addInputPort(delimiterInput);
        
        // 鍒涘缓杈撳嚭绔彛
        IPort listOutput = new BasePort(OUTPUT_LIST_ID, "List", 
                "The resulting list of substrings", NodeDataType.LIST, this);
        addOutputPort(listOutput);
        
        IPort countOutput = new BasePort(OUTPUT_COUNT_ID, "Count", 
                "The number of items in the result", NodeDataType.INTEGER, this);
        addOutputPort(countOutput);
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
        Object delimiterObj = inputValues.get(INPUT_DELIMITER_ID);
        
        // 榛樿绌哄瓧绗︿覆
        String text = "";
        if (textObj instanceof String) {
            text = (String) textObj;
        } else if (textObj != null) {
            text = textObj.toString();
        }
        
        // 浣跨敤榛樿鍒嗛殧绗︽垨杈撳叆鍒嗛殧绗?
        String delimiter = defaultDelimiter;
        if (delimiterObj instanceof String) {
            delimiter = (String) delimiterObj;
            if (delimiter.isEmpty()) {
                delimiter = defaultDelimiter;
            }
        }
        
        // 鎷嗗垎鏂囨湰
        String[] parts = text.split(java.util.regex.Pattern.quote(delimiter), -1);
        List<String> result = new ArrayList<>();
        
        // 澶勭悊缁撴灉
        for (String part : parts) {
            // 濡傛灉闇€瑕佸幓闄ゅ墠鍚庣┖鐧?
            if (trimResults) {
                part = part.trim();
            }
            
            // 濡傛灉璺宠繃绌虹粨鏋滀笖璇ラ儴鍒嗕负绌?
            if (skipEmptyResults && part.isEmpty()) {
                continue;
            }
            
            result.add(part);
        }
        
        // 璁剧疆杈撳嚭
        outputValues.put(OUTPUT_LIST_ID, result);
        outputValues.put(OUTPUT_COUNT_ID, result.size());
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getDefaultDelimiter() {
        return defaultDelimiter;
    }
    
    public void setDefaultDelimiter(String delimiter) {
        this.defaultDelimiter = delimiter != null ? delimiter : ",";
        markDirty();
    }
    
    public boolean isTrimResults() {
        return trimResults;
    }
    
    public void setTrimResults(boolean trim) {
        this.trimResults = trim;
        markDirty();
    }
    
    public boolean isSkipEmptyResults() {
        return skipEmptyResults;
    }
    
    public void setSkipEmptyResults(boolean skip) {
        this.skipEmptyResults = skip;
        markDirty();
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("defaultDelimiter", getDefaultDelimiter());
        state.put("trimResults", isTrimResults());
        state.put("skipEmptyResults", isSkipEmptyResults());
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
            
            if (stateMap.containsKey("trimResults")) {
                Object trimObj = stateMap.get("trimResults");
                if (trimObj instanceof Boolean) {
                    setTrimResults((Boolean) trimObj);
                }
            }
            
            if (stateMap.containsKey("skipEmptyResults")) {
                Object skipObj = stateMap.get("skipEmptyResults");
                if (skipObj instanceof Boolean) {
                    setSkipEmptyResults((Boolean) skipObj);
                }
            }
        }
    }
} 