package com.nodecraft.nodesystem.nodes.utilities.text_processing;

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
 * Format Text 鑺傜偣锛屼娇鐢ㄥ崰浣嶇鏍煎紡鍖栨枃鏈?
 */
@NodeInfo(
    id = "data.text.format",
    displayName = "Format Text",
    description = "Formats text using placeholders (e.g., 'X: {0}, Y: {1}')",
    category = "data.text"
)
public class FormatTextNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    private String formatTemplate = "Value: {0}"; // 榛樿鏍煎紡妯℃澘
    private int argCount = 3; // 榛樿鍙傛暟鏁伴噺
    private String description = "Formats text using placeholders (e.g., 'X: {0}, Y: {1}')"; // 鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_FORMAT_ID = "input_format";
    private static final String INPUT_ARG_PREFIX = "input_arg_";
    private static final String OUTPUT_RESULT_ID = "output_result";
    
    /**
     * 鏋勯€犱竴涓柊鐨勬枃鏈牸寮忓寲鑺傜偣
     */
    public FormatTextNode() {
        super(UUID.randomUUID(), "data.text.format");
        
        // 鍒涘缓杈撳叆绔彛 - 鏍煎紡
        IPort formatInput = new BasePort(INPUT_FORMAT_ID, "Format", 
                "Text format with placeholders {0}, {1}, etc.", NodeDataType.STRING, this);
        addInputPort(formatInput);
        
        // 鍒涘缓鍙傛暟杈撳叆绔彛
        recreateArgInputPorts();
        
        // 鍒涘缓杈撳嚭绔彛
        IPort resultOutput = new BasePort(OUTPUT_RESULT_ID, "Result", 
                "The formatted string", NodeDataType.STRING, this);
        addOutputPort(resultOutput);
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
     * 閲嶆柊鍒涘缓鎵€鏈夊弬鏁拌緭鍏ョ鍙?
     */
    private void recreateArgInputPorts() {
        // 绉婚櫎鏃х殑鍙傛暟绔彛
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
        
        // 鍒涘缓鏂扮殑鍙傛暟绔彛
        for (int i = 0; i < argCount; i++) {
            String portId = INPUT_ARG_PREFIX + i;
            String displayName = "Arg " + i;
            IPort argInput = new BasePort(portId, displayName, 
                    "Argument for placeholder {" + i + "}", NodeDataType.ANY, this);
            addInputPort(argInput);
        }
    }
    
    /**
     * 鑺傜偣鐨勮绠楅€昏緫
     * @param context 鎵ц涓婁笅鏂?
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 鑾峰彇鏍煎紡瀛楃涓?
        String format = formatTemplate;
        Object formatObj = inputValues.get(INPUT_FORMAT_ID);
        if (formatObj instanceof String) {
            format = (String) formatObj;
        }
        
        // 鏋勫缓鍙傛暟鏁扮粍
        Object[] args = new Object[argCount];
        for (int i = 0; i < argCount; i++) {
            String argPortId = INPUT_ARG_PREFIX + i;
            args[i] = inputValues.get(argPortId);
            
            // 纭繚娌℃湁null鍙傛暟锛圡essageFormat涓嶅鐞唍ull锛?
            if (args[i] == null) {
                args[i] = "";
            }
        }
        
        // 鏍煎紡鍖栨枃鏈?
        String result;
        try {
            result = MessageFormat.format(format, args);
        } catch (Exception e) {
            // 濡傛灉鏍煎紡鍖栧け璐ワ紝杩斿洖鍘熷鏍煎紡瀛楃涓?
            result = format;
        }
        
        // 璁剧疆杈撳嚭
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
        // 纭繚鍙傛暟鏁伴噺鏈夋晥
        count = Math.max(0, Math.min(10, count)); // 闄愬埗鍦?-10涔嬮棿
        
        if (this.argCount != count) {
            this.argCount = count;
            recreateArgInputPorts();
            markDirty();
        }
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
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