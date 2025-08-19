package com.nodecraft.nodesystem.nodes.data.text;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Format Text 节点，使用占位符格式化文本
 */
@NodeInfo(
    id = "data.text.format",
    displayName = "Format Text",
    description = "Formats text using placeholders (e.g., 'X: {0}, Y: {1}')",
    category = "data.text"
)
public class FormatTextNode extends BaseNode {
    
    // --- 节点属性 ---
    private String formatTemplate = "Value: {0}"; // 默认格式模板
    private int argCount = 3; // 默认参数数量
    private String description = "Formats text using placeholders (e.g., 'X: {0}, Y: {1}')"; // 节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_FORMAT_ID = "input_format";
    private static final String INPUT_ARG_PREFIX = "input_arg_";
    private static final String OUTPUT_RESULT_ID = "output_result";
    
    /**
     * 构造一个新的文本格式化节点
     */
    public FormatTextNode() {
        super(UUID.randomUUID(), "data.text.format");
        
        // 创建输入端口 - 格式
        IPort formatInput = new BasePort(INPUT_FORMAT_ID, "Format", 
                "Text format with placeholders {0}, {1}, etc.", NodeDataType.STRING, this);
        addInputPort(formatInput);
        
        // 创建参数输入端口
        recreateArgInputPorts();
        
        // 创建输出端口
        IPort resultOutput = new BasePort(OUTPUT_RESULT_ID, "Result", 
                "The formatted string", NodeDataType.STRING, this);
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
     * 重新创建所有参数输入端口
     */
    private void recreateArgInputPorts() {
        // 移除旧的参数端口
        List<IPort> portsToKeep = new ArrayList<>();
        for (IPort port : inputPorts) {
            if (!port.getId().startsWith(INPUT_ARG_PREFIX)) {
                portsToKeep.add(port);
            }
        }
        
        inputPorts.clear();
        for (IPort port : portsToKeep) {
            addInputPort(port);
        }
        
        // 创建新的参数端口
        for (int i = 0; i < argCount; i++) {
            String portId = INPUT_ARG_PREFIX + i;
            String displayName = "Arg " + i;
            IPort argInput = new BasePort(portId, displayName, 
                    "Argument for placeholder {" + i + "}", NodeDataType.ANY, this);
            addInputPort(argInput);
        }
    }
    
    /**
     * 节点的计算逻辑
     * @param context 执行上下文
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取格式字符串
        String format = formatTemplate;
        Object formatObj = inputValues.get(INPUT_FORMAT_ID);
        if (formatObj instanceof String) {
            format = (String) formatObj;
        }
        
        // 构建参数数组
        Object[] args = new Object[argCount];
        for (int i = 0; i < argCount; i++) {
            String argPortId = INPUT_ARG_PREFIX + i;
            args[i] = inputValues.get(argPortId);
            
            // 确保没有null参数（MessageFormat不处理null）
            if (args[i] == null) {
                args[i] = "";
            }
        }
        
        // 格式化文本
        String result;
        try {
            result = MessageFormat.format(format, args);
        } catch (Exception e) {
            // 如果格式化失败，返回原始格式字符串
            result = format;
        }
        
        // 设置输出
        outputValues.put(OUTPUT_RESULT_ID, result);
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getFormatTemplate() {
        return formatTemplate;
    }
    
    public void setFormatTemplate(String template) {
        this.formatTemplate = template != null ? template : "";
        markDirty();
    }
    
    public int getArgCount() {
        return argCount;
    }
    
    public void setArgCount(int count) {
        // 确保参数数量有效
        count = Math.max(0, Math.min(10, count)); // 限制在0-10之间
        
        if (this.argCount != count) {
            this.argCount = count;
            recreateArgInputPorts();
            markDirty();
        }
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("formatTemplate", getFormatTemplate());
        state.put("argCount", getArgCount());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("formatTemplate")) {
                Object templateObj = stateMap.get("formatTemplate");
                if (templateObj instanceof String) {
                    setFormatTemplate((String) templateObj);
                }
            }
            
            if (stateMap.containsKey("argCount")) {
                Object countObj = stateMap.get("argCount");
                if (countObj instanceof Number) {
                    setArgCount(((Number) countObj).intValue());
                }
            }
        }
    }
} 