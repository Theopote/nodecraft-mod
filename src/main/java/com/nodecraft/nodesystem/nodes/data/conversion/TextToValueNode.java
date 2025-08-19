package com.nodecraft.nodesystem.nodes.data.conversion;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Text to Value 节点，尝试将文本解析为数字或布尔值
 */
@NodeInfo(
    id = "data.conversion.text_to_value",
    displayName = "文本转数值",
    description = "尝试将文本解析为数字或布尔值",
    category = "data.conversion"
)
public class TextToValueNode extends BaseNode {
    
    // --- 节点属性 ---
    public enum OutputType {
        NUMBER, BOOLEAN, INTEGER
    }
    
    private OutputType outputType = OutputType.NUMBER; // 默认输出类型为数字
    private String description; // 存储节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_TEXT_ID = "input_text";
    private static final String OUTPUT_VALUE_ID = "output_value";
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    
    /**
     * 构造一个新的文本转值节点
     */
    public TextToValueNode() {
        // 调用父类构造函数，使用UUID.randomUUID()生成新的ID
        super(UUID.randomUUID(), "data.conversion.text_to_value");
        
        // 设置节点描述
        this.description = "Tries to parse text into a number or boolean value";
        
        // 创建输入端口
        IPort textInput = new BasePort(INPUT_TEXT_ID, "Text", 
                "The input text to parse", NodeDataType.STRING, this);
        addInputPort(textInput);
        
        // 创建输出端口
        IPort valueOutput = new BasePort(OUTPUT_VALUE_ID, "Value", 
                "The parsed value (number or boolean)", NodeDataType.ANY, this);
        addOutputPort(valueOutput);
        
        IPort successOutput = new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "Whether the parsing was successful", NodeDataType.BOOLEAN, this);
        addOutputPort(successOutput);
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
        
        // 默认输出值和成功状态
        Object result = null;
        boolean success = false;
        
        // 确保输入是字符串
        if (textObj instanceof String) {
            String text = (String) textObj;
            text = text.trim(); // 移除前后空白
            
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
                // 解析失败，保持默认值
                success = false;
            }
        }
        
        // 设置输出
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
     * 设置输出类型（字符串形式，用于从UI或配置中设置）
     * @param typeStr 输出类型字符串："NUMBER", "BOOLEAN", "INTEGER"
     */
    public void setOutputTypeString(String typeStr) {
        try {
            setOutputType(OutputType.valueOf(typeStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            // 如果字符串不匹配任何类型，使用默认的NUMBER
            setOutputType(OutputType.NUMBER);
        }
    }
    
    // --- 节点状态序列化 ---
    
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