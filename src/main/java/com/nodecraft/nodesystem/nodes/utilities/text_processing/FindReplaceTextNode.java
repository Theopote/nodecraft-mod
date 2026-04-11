package com.nodecraft.nodesystem.nodes.utilities.text_processing;

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
 * Find/Replace Text 鑺傜偣锛屽湪瀛楃涓蹭腑鏌ユ壘鎴栨浛鎹㈡枃鏈?
 */
@NodeInfo(
    id = "data.text.find_replace",
    displayName = "Find/Replace Text",
    description = "Finds or replaces text within a string",
    category = "data.text"
)
public class FindReplaceTextNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    public enum Mode {
        FIND, REPLACE, FIND_ALL
    }
    
    private Mode operationMode = Mode.REPLACE; // 鎿嶄綔妯″紡
    private boolean useRegex = false;          // 鏄惁浣跨敤姝ｅ垯琛ㄨ揪寮?
    private boolean ignoreCase = true;         // 鏄惁蹇界暐澶у皬鍐?
    private String description;                // 瀛樺偍鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_TEXT_ID = "input_text";
    private static final String INPUT_FIND_ID = "input_find";
    private static final String INPUT_REPLACE_ID = "input_replace";
    private static final String OUTPUT_RESULT_ID = "output_result";
    private static final String OUTPUT_FOUND_ID = "output_found";
    private static final String OUTPUT_COUNT_ID = "output_count";
    private static final String OUTPUT_POSITIONS_ID = "output_positions";
    
    /**
     * 鏋勯€犱竴涓柊鐨勬煡鎵?鏇挎崲鏂囨湰鑺傜偣
     */
    public FindReplaceTextNode() {
        // 璋冪敤鐖剁被鏋勯€犲嚱鏁帮紝浣跨敤UUID.randomUUID()鐢熸垚鏂扮殑ID
        super(UUID.randomUUID(), "data.text.find_replace");
        
        // 璁剧疆鑺傜偣鎻忚堪
        this.description = "Finds or replaces text within a string";
        
        // 鍒涘缓杈撳叆绔彛
        IPort textInput = new BasePort(INPUT_TEXT_ID, "Text", 
                "The text to search in", NodeDataType.STRING, this);
        addInputPort(textInput);
        
        IPort findInput = new BasePort(INPUT_FIND_ID, "Find", 
                "The text to find", NodeDataType.STRING, this);
        addInputPort(findInput);
        
        IPort replaceInput = new BasePort(INPUT_REPLACE_ID, "Replace", 
                "The text to replace with", NodeDataType.STRING, this);
        addInputPort(replaceInput);
        
        // 鍒涘缓杈撳嚭绔彛
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
     * 瀹炵幇INode鎺ュ彛鐨刧etDescription鏂规硶
     * @return 鑺傜偣鎻忚堪
     */
    @Override
    public String getDescription() {
        return this.description;
    }
    
    /**
     * 鑺傜偣鐨勮绠楅€昏緫
     * @param context 鎵ц涓婁笅鏂?
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 鑾峰彇杈撳叆
        Object textObj = inputValues.get(INPUT_TEXT_ID);
        Object findObj = inputValues.get(INPUT_FIND_ID);
        Object replaceObj = inputValues.get(INPUT_REPLACE_ID);
        
        // 榛樿鍊?
        String text = textObj != null ? textObj.toString() : "";
        String find = findObj != null ? findObj.toString() : "";
        String replace = replaceObj != null ? replaceObj.toString() : "";
        
        // 澶勭悊绌烘煡鎵惧瓧绗︿覆鐨勬儏鍐?
        if (find.isEmpty()) {
            outputValues.put(OUTPUT_RESULT_ID, text);
            outputValues.put(OUTPUT_FOUND_ID, false);
            outputValues.put(OUTPUT_COUNT_ID, 0);
            outputValues.put(OUTPUT_POSITIONS_ID, new ArrayList<>());
            return;
        }
        
        // 鏍规嵁鎿嶄綔妯″紡鎵ц涓嶅悓鐨勯€昏緫
        try {
            if (useRegex) {
                // 浣跨敤姝ｅ垯琛ㄨ揪寮?
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
                // 浣跨敤鏅€氬瓧绗︿覆鏌ユ壘
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
            // 姝ｅ垯琛ㄨ揪寮忛敊璇紝浠呰繑鍥炲師濮嬫枃鏈?
            outputValues.put(OUTPUT_RESULT_ID, text);
            outputValues.put(OUTPUT_FOUND_ID, false);
            outputValues.put(OUTPUT_COUNT_ID, 0);
            outputValues.put(OUTPUT_POSITIONS_ID, new ArrayList<>());
        }
    }
    
    /**
     * 璁＄畻姝ｅ垯琛ㄨ揪寮忔ā寮忕殑鍖归厤娆℃暟
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
     * 璁剧疆鎿嶄綔妯″紡锛堝瓧绗︿覆褰㈠紡锛岀敤浜庝粠UI鎴栭厤缃腑璁剧疆锛?
     * @param modeStr 妯″紡瀛楃涓诧細"FIND", "REPLACE", "FIND_ALL"
     */
    public void setOperationModeString(String modeStr) {
        try {
            setOperationMode(Mode.valueOf(modeStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            // 榛樿涓篟EPLACE
            setOperationMode(Mode.REPLACE);
        }
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
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