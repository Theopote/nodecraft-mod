package com.nodecraft.nodesystem.nodes.data.text;

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
 * Split Text 节点，按分隔符将文本拆分成列表
 */
@NodeInfo(
    id = "data.text.split",
    displayName = "文本拆分",
    description = "按分隔符将文本拆分成列表",
    category = "data.text"
)
public class SplitTextNode extends BaseNode {
    
    // --- 节点属性 ---
    private String defaultDelimiter = ","; // 默认分隔符
    private boolean trimResults = true;    // 是否去除结果的前后空白
    private boolean skipEmptyResults = false; // 是否跳过空结果
    private String description = "Splits text into a list using a delimiter"; // 节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_TEXT_ID = "input_text";
    private static final String INPUT_DELIMITER_ID = "input_delimiter";
    private static final String OUTPUT_LIST_ID = "output_list";
    private static final String OUTPUT_COUNT_ID = "output_count";
    
    /**
     * 构造一个新的文本拆分节点
     */
    public SplitTextNode() {
        super(UUID.randomUUID(), "data.text.split");
        
        // 创建输入端口
        IPort textInput = new BasePort(INPUT_TEXT_ID, "Text", 
                "The input text to split", NodeDataType.STRING, this);
        addInputPort(textInput);
        
        IPort delimiterInput = new BasePort(INPUT_DELIMITER_ID, "Delimiter", 
                "The delimiter to split by (default: comma)", NodeDataType.STRING, this);
        addInputPort(delimiterInput);
        
        // 创建输出端口
        IPort listOutput = new BasePort(OUTPUT_LIST_ID, "List", 
                "The resulting list of substrings", NodeDataType.LIST, this);
        addOutputPort(listOutput);
        
        IPort countOutput = new BasePort(OUTPUT_COUNT_ID, "Count", 
                "The number of items in the result", NodeDataType.INTEGER, this);
        addOutputPort(countOutput);
    }
    
    /**
     * 实现INode接口的getDescription方法
     * @return 节点描述
     */
    @Override
    public String getDescription() {
        return this.description;
    }
    
    /**
     * 节点的计算逻辑
     * @param context 执行上下文
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入
        Object textObj = inputValues.get(INPUT_TEXT_ID);
        Object delimiterObj = inputValues.get(INPUT_DELIMITER_ID);
        
        // 默认空字符串
        String text = "";
        if (textObj instanceof String) {
            text = (String) textObj;
        } else if (textObj != null) {
            text = textObj.toString();
        }
        
        // 使用默认分隔符或输入分隔符
        String delimiter = defaultDelimiter;
        if (delimiterObj instanceof String) {
            delimiter = (String) delimiterObj;
            if (delimiter.isEmpty()) {
                delimiter = defaultDelimiter;
            }
        }
        
        // 拆分文本
        String[] parts = text.split(java.util.regex.Pattern.quote(delimiter), -1);
        List<String> result = new ArrayList<>();
        
        // 处理结果
        for (String part : parts) {
            // 如果需要去除前后空白
            if (trimResults) {
                part = part.trim();
            }
            
            // 如果跳过空结果且该部分为空
            if (skipEmptyResults && part.isEmpty()) {
                continue;
            }
            
            result.add(part);
        }
        
        // 设置输出
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
    
    // --- 节点状态序列化 ---
    
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