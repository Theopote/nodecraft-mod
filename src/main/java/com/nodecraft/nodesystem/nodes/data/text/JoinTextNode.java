package com.nodecraft.nodesystem.nodes.data.text;

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
 * Join Text 节点，将文本列表用分隔符连接成字符串
 */
@NodeInfo(
    id = "data.text.join",
    displayName = "文本合并",
    description = "将文本列表用分隔符连接成字符串",
    category = "data.text"
)
public class JoinTextNode extends BaseNode {
    
    // --- 节点属性 ---
    private String defaultDelimiter = ", "; // 默认分隔符
    private String description = "Joins a list of strings into a single string with a delimiter"; // 节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_DELIMITER_ID = "input_delimiter";
    private static final String OUTPUT_TEXT_ID = "output_text";
    
    /**
     * 构造一个新的文本连接节点
     */
    public JoinTextNode() {
        super(UUID.randomUUID(), "data.text.join");
        
        // 创建输入端口
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list of strings to join", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        IPort delimiterInput = new BasePort(INPUT_DELIMITER_ID, "Delimiter", 
                "The delimiter to join with (default: comma+space)", NodeDataType.STRING, this);
        addInputPort(delimiterInput);
        
        // 创建输出端口
        IPort textOutput = new BasePort(OUTPUT_TEXT_ID, "Text", 
                "The joined string", NodeDataType.STRING, this);
        addOutputPort(textOutput);
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
        Object listObj = inputValues.get(INPUT_LIST_ID);
        Object delimiterObj = inputValues.get(INPUT_DELIMITER_ID);
        
        // 使用默认分隔符或输入分隔符
        String delimiter = defaultDelimiter;
        if (delimiterObj instanceof String) {
            delimiter = (String) delimiterObj;
        }
        
        // 生成结果字符串
        String result = "";
        
        if (listObj instanceof List) {
            List<?> list = (List<?>) listObj;
            StringBuilder sb = new StringBuilder();
            
            boolean first = true;
            for (Object item : list) {
                if (!first) {
                    sb.append(delimiter);
                }
                
                // 将每个项目转换为字符串
                if (item != null) {
                    sb.append(item.toString());
                }
                
                first = false;
            }
            
            result = sb.toString();
        }
        
        // 设置输出
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
    
    // --- 节点状态序列化 ---
    
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