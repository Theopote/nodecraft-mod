package com.nodecraft.nodesystem.nodes.inputs.basic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 文本输入节点，允许用户输入单行或多行文本。
 */
@NodeInfo(
    id = "inputs.basic.text_input",
    displayName = "文本输入",
    description = "允许用户输入单行或多行文本",
    category = "inputs.basic"
)
public class TextInputNode extends BaseNode {
    
    // --- 节点属性 ---
    private String text = "";
    private boolean multiline = false;
    private int maxLength = 32767; // 默认最大长度
    private String placeholder = "Enter text...";
    private String description = "Allows input of text (single or multiline)."; // 节点描述
    
    // --- 输出端口 ---
    private static final String OUTPUT_TEXT_ID = "output_text";
    
    /**
     * 构造一个新的文本输入节点
     */
    public TextInputNode() {
        // 使用新的分类命名 - inputs.basic.text_input
        super(UUID.randomUUID(), "inputs.basic.text_input");
        
        // 创建并添加输出端口
        IPort textOutput = new BasePort(OUTPUT_TEXT_ID, "Text", "The entered text", NodeDataType.STRING, this);
        addOutputPort(textOutput);
        
        // 初始化输出值
        updateOutput();
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
        updateOutput();
    }
    
    /**
     * 设置文本内容
     * @param text 新的文本值
     */
    public void setText(String text) {
        String newText = text != null ? text : "";
        
        // 应用长度限制
        if (newText.length() > maxLength) {
            newText = newText.substring(0, maxLength);
        }
        
        // 如果不是多行模式，移除换行符
        if (!multiline) {
            newText = newText.replace("\n", " ").replace("\r", "");
        }
        
        // 如果值变化了，更新节点状态
        if (!this.text.equals(newText)) {
            this.text = newText;
            updateOutput();
            
            // 通知系统此节点的输出已更新
            markDirty();
        }
    }
    
    /**
     * 更新输出端口的值
     */
    private void updateOutput() {
        outputValues.put(OUTPUT_TEXT_ID, this.text);
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getText() {
        return text;
    }
    
    public boolean isMultiline() {
        return multiline;
    }
    
    public void setMultiline(boolean multiline) {
        if (this.multiline != multiline) {
            this.multiline = multiline;
            
            // 如果从多行切换到单行，需要移除换行符
            if (!multiline) {
                setText(this.text); // 会触发移除换行符的逻辑
            }
        }
    }
    
    public int getMaxLength() {
        return maxLength;
    }
    
    public void setMaxLength(int maxLength) {
        if (maxLength <= 0) {
            maxLength = 32767; // 防止设置无效值
        }
        
        this.maxLength = maxLength;
        
        // 如果当前文本超过新的最大长度，截断文本
        if (text.length() > maxLength) {
            setText(text); // 会触发长度限制的逻辑
        }
    }
    
    public String getPlaceholder() {
        return placeholder;
    }
    
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder != null ? placeholder : "Enter text...";
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        // 返回节点所有可序列化的状态
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("text", getText());
        state.put("multiline", isMultiline());
        state.put("maxLength", getMaxLength());
        state.put("placeholder", getPlaceholder());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            // 先设置属性
            if (stateMap.containsKey("multiline")) {
                Object multiline = stateMap.get("multiline");
                if (multiline instanceof Boolean) {
                    setMultiline((Boolean) multiline);
                }
            }
            
            if (stateMap.containsKey("maxLength")) {
                Object maxLength = stateMap.get("maxLength");
                if (maxLength instanceof Number) {
                    setMaxLength(((Number) maxLength).intValue());
                }
            }
            
            if (stateMap.containsKey("placeholder")) {
                Object placeholder = stateMap.get("placeholder");
                if (placeholder instanceof String) {
                    setPlaceholder((String) placeholder);
                }
            }
            
            // 最后设置文本，确保应用所有约束
            if (stateMap.containsKey("text")) {
                Object text = stateMap.get("text");
                if (text instanceof String) {
                    setText((String) text);
                } else {
                    setText(String.valueOf(text));
                }
            }
        } else if (state instanceof String) {
            // 向后兼容：如果状态直接是字符串，直接使用它作为文本
            setText((String) state);
        } else if (state != null) {
            // 如果是其他类型，尝试转换为字符串
            setText(String.valueOf(state));
        }
    }
} 