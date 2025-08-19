package com.nodecraft.nodesystem.nodes.data.text;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Concatenate Text 节点，连接多个文本或数据为字符串
 */
@NodeInfo(
    id = "data.text.concatenate",
    displayName = "文本连接",
    description = "将多个文本输入连接成单个字符串",
    category = "data.text"
)
public class ConcatenateTextNode extends BaseNode {
    
    // --- 节点属性 ---
    private boolean addSpacesBetween = true; // 是否在连接的文本之间添加空格
    private int inputCount = 3; // 默认输入数量
    private String description = "Concatenates multiple text inputs into a single string"; // 节点描述
    
    // --- 输入/输出端口ID前缀 ---
    private static final String INPUT_PREFIX = "input_text_";
    private static final String OUTPUT_RESULT_ID = "output_result";
    
    /**
     * 构造一个新的文本连接节点
     */
    public ConcatenateTextNode() {
        // 使用与FindReplaceTextNode相同的构造函数格式
        super(UUID.randomUUID(), "data.text.concatenate");
        
        // 创建动态输入端口
        recreateInputPorts();
        
        // 创建输出端口
        IPort resultOutput = new BasePort(OUTPUT_RESULT_ID, "Result", 
                "The concatenated string", NodeDataType.STRING, this);
        addOutputPort(resultOutput);
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
     * 重新创建所有输入端口
     */
    private void recreateInputPorts() {
        // 清除现有输入端口
        inputPorts.clear();
        
        // 创建新的输入端口
        for (int i = 0; i < inputCount; i++) {
            String portId = INPUT_PREFIX + i;
            String displayName = "Text " + (i + 1);
            IPort textInput = new BasePort(portId, displayName, 
                    "Text input " + (i + 1), NodeDataType.ANY, this);
            addInputPort(textInput);
        }
    }
    
    /**
     * 节点的计算逻辑
     * @param context 执行上下文
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        StringBuilder result = new StringBuilder();
        
        // 收集非空输入并连接
        boolean isFirst = true;
        for (int i = 0; i < inputCount; i++) {
            String portId = INPUT_PREFIX + i;
            Object input = inputValues.get(portId);
            
            if (input != null) {
                String textValue = input.toString();
                
                // 如果不是第一个且需要添加空格
                if (!isFirst && addSpacesBetween) {
                    result.append(" ");
                }
                
                result.append(textValue);
                isFirst = false;
            }
        }
        
        // 设置输出
        outputValues.put(OUTPUT_RESULT_ID, result.toString());
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isAddSpacesBetween() {
        return addSpacesBetween;
    }
    
    public void setAddSpacesBetween(boolean addSpaces) {
        this.addSpacesBetween = addSpaces;
        markDirty();
    }
    
    public int getInputCount() {
        return inputCount;
    }
    
    public void setInputCount(int count) {
        // 确保输入数量有效
        count = Math.max(1, Math.min(10, count)); // 限制在1-10之间
        
        if (this.inputCount != count) {
            this.inputCount = count;
            recreateInputPorts();
            markDirty();
        }
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("addSpacesBetween", isAddSpacesBetween());
        state.put("inputCount", getInputCount());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("addSpacesBetween")) {
                Object addSpacesObj = stateMap.get("addSpacesBetween");
                if (addSpacesObj instanceof Boolean) {
                    setAddSpacesBetween((Boolean) addSpacesObj);
                }
            }
            
            if (stateMap.containsKey("inputCount")) {
                Object countObj = stateMap.get("inputCount");
                if (countObj instanceof Number) {
                    setInputCount(((Number) countObj).intValue());
                }
            }
        }
    }
} 