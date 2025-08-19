package com.nodecraft.nodesystem.nodes.data.text;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Find/Replace Text 节点，在字符串中查找或替换文本
 */
@NodeInfo(
    id = "data.text.find_replace",
    displayName = "Find/Replace Text",
    description = "Finds or replaces text within a string",
    category = "data.text"
)
public class FindReplaceTextNode extends BaseNode {
    
    // --- 节点属性 ---
    public enum Mode {
        FIND, REPLACE, FIND_ALL
    }
    
    private Mode operationMode = Mode.REPLACE; // 操作模式
    private boolean useRegex = false;          // 是否使用正则表达式
    private boolean ignoreCase = true;         // 是否忽略大小写
    private String description;                // 存储节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_TEXT_ID = "input_text";
    private static final String INPUT_FIND_ID = "input_find";
    private static final String INPUT_REPLACE_ID = "input_replace";
    private static final String OUTPUT_RESULT_ID = "output_result";
    private static final String OUTPUT_FOUND_ID = "output_found";
    private static final String OUTPUT_COUNT_ID = "output_count";
    private static final String OUTPUT_POSITIONS_ID = "output_positions";
    
    /**
     * 构造一个新的查找/替换文本节点
     */
    public FindReplaceTextNode() {
        // 调用父类构造函数，使用UUID.randomUUID()生成新的ID
        super(UUID.randomUUID(), "data.text.find_replace");
        
        // 设置节点描述
        this.description = "Finds or replaces text within a string";
        
        // 创建输入端口
        IPort textInput = new BasePort(INPUT_TEXT_ID, "Text", 
                "The text to search in", NodeDataType.STRING, this);
        addInputPort(textInput);
        
        IPort findInput = new BasePort(INPUT_FIND_ID, "Find", 
                "The text to find", NodeDataType.STRING, this);
        addInputPort(findInput);
        
        IPort replaceInput = new BasePort(INPUT_REPLACE_ID, "Replace", 
                "The text to replace with", NodeDataType.STRING, this);
        addInputPort(replaceInput);
        
        // 创建输出端口
        IPort resultOutput = new BasePort(OUTPUT_RESULT_ID, "Result", 
                "The resulting text (replaced or original)", NodeDataType.STRING, this);
        addOutputPort(resultOutput);
        
        IPort foundOutput = new BasePort(OUTPUT_FOUND_ID, "Found", 
                "Whether the text was found", NodeDataType.BOOLEAN, this);
        addOutputPort(foundOutput);
        
        IPort countOutput = new BasePort(OUTPUT_COUNT_ID, "Count", 
                "Number of occurrences found", NodeDataType.INTEGER, this);
        addOutputPort(countOutput);
        
        IPort positionsOutput = new BasePort(OUTPUT_POSITIONS_ID, "Positions", 
                "Starting positions of matches (Find All mode)", NodeDataType.LIST, this);
        addOutputPort(positionsOutput);
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
        Object findObj = inputValues.get(INPUT_FIND_ID);
        Object replaceObj = inputValues.get(INPUT_REPLACE_ID);
        
        // 默认值
        String text = textObj != null ? textObj.toString() : "";
        String find = findObj != null ? findObj.toString() : "";
        String replace = replaceObj != null ? replaceObj.toString() : "";
        
        // 处理空查找字符串的情况
        if (find.isEmpty()) {
            outputValues.put(OUTPUT_RESULT_ID, text);
            outputValues.put(OUTPUT_FOUND_ID, false);
            outputValues.put(OUTPUT_COUNT_ID, 0);
            outputValues.put(OUTPUT_POSITIONS_ID, new ArrayList<>());
            return;
        }
        
        // 根据操作模式执行不同的逻辑
        try {
            if (useRegex) {
                // 使用正则表达式
                int flags = ignoreCase ? Pattern.CASE_INSENSITIVE : 0;
                Pattern pattern = Pattern.compile(find, flags);
                Matcher matcher = pattern.matcher(text);
                
                switch (operationMode) {
                    case REPLACE:
                        String replaced = matcher.replaceAll(replace);
                        boolean found = !replaced.equals(text);
                        int count = countMatches(pattern, text);
                        
                        outputValues.put(OUTPUT_RESULT_ID, replaced);
                        outputValues.put(OUTPUT_FOUND_ID, found);
                        outputValues.put(OUTPUT_COUNT_ID, count);
                        outputValues.put(OUTPUT_POSITIONS_ID, new ArrayList<>());
                        break;
                        
                    case FIND:
                        boolean foundAny = matcher.find();
                        outputValues.put(OUTPUT_RESULT_ID, text);
                        outputValues.put(OUTPUT_FOUND_ID, foundAny);
                        outputValues.put(OUTPUT_COUNT_ID, foundAny ? 1 : 0);
                        outputValues.put(OUTPUT_POSITIONS_ID, foundAny ? 
                                List.of(matcher.start()) : new ArrayList<>());
                        break;
                        
                    case FIND_ALL:
                        List<Integer> positions = new ArrayList<>();
                        int matchCount = 0;
                        
                        matcher.reset();
                        while (matcher.find()) {
                            positions.add(matcher.start());
                            matchCount++;
                        }
                        
                        outputValues.put(OUTPUT_RESULT_ID, text);
                        outputValues.put(OUTPUT_FOUND_ID, matchCount > 0);
                        outputValues.put(OUTPUT_COUNT_ID, matchCount);
                        outputValues.put(OUTPUT_POSITIONS_ID, positions);
                        break;
                }
            } else {
                // 使用普通字符串查找
                switch (operationMode) {
                    case REPLACE:
                        String findStr = ignoreCase ? find.toLowerCase() : find;
                        String textToSearch = ignoreCase ? text.toLowerCase() : text;
                        
                        StringBuilder result = new StringBuilder();
                        int lastIndex = 0;
                        int count = 0;
                        int index;
                        
                        while ((index = textToSearch.indexOf(findStr, lastIndex)) != -1) {
                            result.append(text, lastIndex, index).append(replace);
                            lastIndex = index + find.length();
                            count++;
                        }
                        
                        if (lastIndex < text.length()) {
                            result.append(text.substring(lastIndex));
                        }
                        
                        outputValues.put(OUTPUT_RESULT_ID, count > 0 ? result.toString() : text);
                        outputValues.put(OUTPUT_FOUND_ID, count > 0);
                        outputValues.put(OUTPUT_COUNT_ID, count);
                        outputValues.put(OUTPUT_POSITIONS_ID, new ArrayList<>());
                        break;
                        
                    case FIND:
                        int pos = ignoreCase ? 
                                text.toLowerCase().indexOf(find.toLowerCase()) : 
                                text.indexOf(find);
                        
                        outputValues.put(OUTPUT_RESULT_ID, text);
                        outputValues.put(OUTPUT_FOUND_ID, pos >= 0);
                        outputValues.put(OUTPUT_COUNT_ID, pos >= 0 ? 1 : 0);
                        outputValues.put(OUTPUT_POSITIONS_ID, pos >= 0 ? 
                                List.of(pos) : new ArrayList<>());
                        break;
                        
                    case FIND_ALL:
                        List<Integer> allPositions = new ArrayList<>();
                        String textLower = ignoreCase ? text.toLowerCase() : text;
                        String findLower = ignoreCase ? find.toLowerCase() : find;
                        
                        int startIndex = 0;
                        int foundIndex;
                        int foundCount = 0;
                        
                        while ((foundIndex = textLower.indexOf(findLower, startIndex)) >= 0) {
                            allPositions.add(foundIndex);
                            startIndex = foundIndex + findLower.length();
                            foundCount++;
                        }
                        
                        outputValues.put(OUTPUT_RESULT_ID, text);
                        outputValues.put(OUTPUT_FOUND_ID, foundCount > 0);
                        outputValues.put(OUTPUT_COUNT_ID, foundCount);
                        outputValues.put(OUTPUT_POSITIONS_ID, allPositions);
                        break;
                }
            }
        } catch (PatternSyntaxException e) {
            // 正则表达式错误，仅返回原始文本
            outputValues.put(OUTPUT_RESULT_ID, text);
            outputValues.put(OUTPUT_FOUND_ID, false);
            outputValues.put(OUTPUT_COUNT_ID, 0);
            outputValues.put(OUTPUT_POSITIONS_ID, new ArrayList<>());
        }
    }
    
    /**
     * 计算正则表达式模式的匹配次数
     */
    private int countMatches(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
    
    // --- Getters/Setters for Properties ---
    
    public Mode getOperationMode() {
        return operationMode;
    }
    
    public void setOperationMode(Mode mode) {
        this.operationMode = mode;
        markDirty();
    }
    
    public boolean isUseRegex() {
        return useRegex;
    }
    
    public void setUseRegex(boolean useRegex) {
        this.useRegex = useRegex;
        markDirty();
    }
    
    public boolean isIgnoreCase() {
        return ignoreCase;
    }
    
    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        markDirty();
    }
    
    /**
     * 设置操作模式（字符串形式，用于从UI或配置中设置）
     * @param modeStr 模式字符串："FIND", "REPLACE", "FIND_ALL"
     */
    public void setOperationModeString(String modeStr) {
        try {
            setOperationMode(Mode.valueOf(modeStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            // 默认为REPLACE
            setOperationMode(Mode.REPLACE);
        }
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("operationMode", getOperationMode().name());
        state.put("useRegex", isUseRegex());
        state.put("ignoreCase", isIgnoreCase());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("operationMode")) {
                Object modeObj = stateMap.get("operationMode");
                if (modeObj instanceof String) {
                    setOperationModeString((String) modeObj);
                }
            }
            
            if (stateMap.containsKey("useRegex")) {
                Object regexObj = stateMap.get("useRegex");
                if (regexObj instanceof Boolean) {
                    setUseRegex((Boolean) regexObj);
                }
            }
            
            if (stateMap.containsKey("ignoreCase")) {
                Object caseObj = stateMap.get("ignoreCase");
                if (caseObj instanceof Boolean) {
                    setIgnoreCase((Boolean) caseObj);
                }
            }
        }
    }
} 