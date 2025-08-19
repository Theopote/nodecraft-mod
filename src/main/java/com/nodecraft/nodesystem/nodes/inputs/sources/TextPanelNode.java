package com.nodecraft.nodesystem.nodes.inputs.sources;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文本面板节点，用于手动输入文本列表或显示数据
 */
@NodeInfo(
    id = "inputs.sources.text_panel",
    displayName = "文本面板",
    description = "用于手动输入文本列表或显示数据",
    category = "inputs.sources"
)
public class TextPanelNode extends BaseNode {
    
    // --- 节点属性 ---
    private String text = ""; // 文本内容
    private boolean isMultiline = true; // 是否为多行文本
    private boolean splitLines = true; // 是否将文本分割为列表
    private boolean readOnly = false; // 是否为只读模式
    private String delimiter = "\n"; // 分隔符
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_TEXT_ID = "input_text";
    private static final String OUTPUT_TEXT_ID = "output_text";
    private static final String OUTPUT_LINES_ID = "output_lines";
    private static final String OUTPUT_LINE_COUNT_ID = "output_line_count";
    
    /**
     * 构造一个新的文本面板节点
     */
    public TextPanelNode() {
        // 使用新的分类命名 - inputs.sources.text_panel
        super(UUID.randomUUID(), "inputs.sources.text_panel");
        
        // 创建输入端口
        IPort textInput = new BasePort(INPUT_TEXT_ID, "Text Input", 
                "Optional text input to display", NodeDataType.STRING, this);
        addInputPort(textInput);
        
        // 创建输出端口
        IPort textOutput = new BasePort(OUTPUT_TEXT_ID, "Text", 
                "The text content as a single string", NodeDataType.STRING, this);
        addOutputPort(textOutput);
        
        IPort linesOutput = new BasePort(OUTPUT_LINES_ID, "Lines", 
                "The text content as a list of strings (lines)", NodeDataType.LIST, this);
        addOutputPort(linesOutput);
        
        IPort lineCountOutput = new BasePort(OUTPUT_LINE_COUNT_ID, "Line Count", 
                "The number of lines in the text", NodeDataType.INTEGER, this);
        addOutputPort(lineCountOutput);
    }
    
    @Override
    public String getDescription() {
        return "For manually entering text or displaying data";
    }
    
    @Override
    public String getDisplayName() {
        return "Text Panel";
    }
    
    /**
     * 节点的计算逻辑
     * @param context 执行上下文
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 如果有输入连接，则优先使用输入值
        String textValue = text;
        Object inputText = inputValues.get(INPUT_TEXT_ID);
        if (inputText != null) {
            textValue = inputText.toString();
        }
        
        // 计算行数和行列表
        List<String> lines;
        if (splitLines) {
            lines = new ArrayList<>(Arrays.asList(textValue.split(delimiter)));
        } else {
            lines = new ArrayList<>();
            if (!textValue.isEmpty()) {
                lines.add(textValue);
            }
        }
        
        int lineCount = lines.size();
        
        // 设置输出
        outputValues.put(OUTPUT_TEXT_ID, textValue);
        outputValues.put(OUTPUT_LINES_ID, lines);
        outputValues.put(OUTPUT_LINE_COUNT_ID, lineCount);
    }
    
    /**
     * 设置文本内容
     * @param text 文本内容
     */
    public void setText(String text) {
        if (text == null) {
            text = "";
        }
        
        if (!this.text.equals(text)) {
            this.text = text;
            markDirty();
        }
    }
    
    /**
     * 添加文本
     * @param additionalText 要添加的文本
     */
    public void appendText(String additionalText) {
        if (additionalText != null && !additionalText.isEmpty()) {
            setText(this.text + additionalText);
        }
    }
    
    /**
     * 清除文本
     */
    public void clearText() {
        setText("");
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getText() {
        return text;
    }
    
    public boolean isMultiline() {
        return isMultiline;
    }
    
    public void setMultiline(boolean multiline) {
        this.isMultiline = multiline;
    }
    
    public boolean isSplitLines() {
        return splitLines;
    }
    
    public void setSplitLines(boolean splitLines) {
        if (this.splitLines != splitLines) {
            this.splitLines = splitLines;
            markDirty();
        }
    }
    
    public boolean isReadOnly() {
        return readOnly;
    }
    
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    
    public String getDelimiter() {
        return delimiter;
    }
    
    public void setDelimiter(String delimiter) {
        if (delimiter == null || delimiter.isEmpty()) {
            delimiter = "\n";
        }
        
        if (!this.delimiter.equals(delimiter)) {
            this.delimiter = delimiter;
            markDirty();
        }
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("text", getText());
        state.put("isMultiline", isMultiline());
        state.put("splitLines", isSplitLines());
        state.put("readOnly", isReadOnly());
        state.put("delimiter", getDelimiter());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("isMultiline")) {
                Object multiline = stateMap.get("isMultiline");
                if (multiline instanceof Boolean) {
                    setMultiline((Boolean) multiline);
                }
            }
            
            if (stateMap.containsKey("splitLines")) {
                Object split = stateMap.get("splitLines");
                if (split instanceof Boolean) {
                    setSplitLines((Boolean) split);
                }
            }
            
            if (stateMap.containsKey("readOnly")) {
                Object ro = stateMap.get("readOnly");
                if (ro instanceof Boolean) {
                    setReadOnly((Boolean) ro);
                }
            }
            
            if (stateMap.containsKey("delimiter")) {
                Object delim = stateMap.get("delimiter");
                if (delim instanceof String) {
                    setDelimiter((String) delim);
                }
            }
            
            // 最后设置文本，因为这会触发markDirty
            if (stateMap.containsKey("text")) {
                Object txt = stateMap.get("text");
                if (txt instanceof String) {
                    setText((String) txt);
                }
            }
        }
    }
} 