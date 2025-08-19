package com.nodecraft.nodesystem.nodes.data.text;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Text Length 节点，获取字符串的长度
 */
@NodeInfo(
    id = "data.text.length",
    displayName = "文本长度",
    description = "获取字符串的长度，可选择是否忽略空白字符",
    category = "data.text"
)
public class TextLengthNode extends BaseNode {
    
    // --- 节点属性 ---
    private boolean ignoreWhitespace = false; // 是否忽略空白字符
    private String description = "Gets the length of a string"; // 节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_TEXT_ID = "input_text";
    private static final String OUTPUT_LENGTH_ID = "output_length";
    
    /**
     * 构造一个新的文本长度节点
     */
    public TextLengthNode() {
        super(UUID.randomUUID(), "data.text.length");
        
        // 创建输入端口
        IPort textInput = new BasePort(INPUT_TEXT_ID, "Text", 
                "The input text", NodeDataType.STRING, this);
        addInputPort(textInput);
        
        // 创建输出端口
        IPort lengthOutput = new BasePort(OUTPUT_LENGTH_ID, "Length", 
                "The length of the text", NodeDataType.INTEGER, this);
        addOutputPort(lengthOutput);
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
        
        // 默认长度为0
        int length = 0;
        
        // 计算字符串长度
        if (textObj != null) {
            String text = textObj.toString();
            
            if (ignoreWhitespace) {
                // 移除所有空白字符后计算长度
                text = text.replaceAll("\\s", "");
            }
            
            length = text.length();
        }
        
        // 设置输出
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
    
    // --- 节点状态序列化 ---
    
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