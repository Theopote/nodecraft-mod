package com.nodecraft.nodesystem.nodes.math.list_sequence;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import com.nodecraft.nodesystem.api.NodeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 鍚堝苟鍒楄〃鑺傜偣锛屽皢澶氫釜鍒楄〃鎸夌储寮曠粍鍚堟垚涓€涓祵濂楀垪琛?
 */
@NodeInfo(
    id = "data.lists.combine_lists",
    displayName = "鍚堝苟鍒楄〃",
    description = "灏嗕袱涓垪琛ㄥ悎骞朵负涓€涓垪琛?,
    category = "data.lists"
)
public class CombineListsNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    private int inputCount = 2; // 杈撳叆鍒楄〃鏁伴噺锛岄粯璁や负2
    private boolean skipIncomplete = false; // 褰撴煇绱㈠紩澶勬湁鍒楄〃缂哄け鍏冪礌鏃舵槸鍚﹁烦杩?
    private boolean outputAsTuples = true; // 鏄惁灏嗘瘡缁勫悎骞跺悗鐨勫厓绱犱綔涓哄瓙鍒楄〃杈撳嚭
    private String description; // 瀛樺偍鑺傜偣鎻忚堪
    
    // --- 杈撳嚭绔彛ID ---
    private static final String OUTPUT_LIST_ID = "output_list";
    
    /**
     * 鏋勯€犱竴涓柊鐨勫悎骞跺垪琛ㄨ妭鐐?
     */
    public CombineListsNode() {
        // 璋冪敤鐖剁被鏋勯€犲嚱鏁帮紝浣跨敤UUID.randomUUID()鐢熸垚鏂扮殑ID
        super(UUID.randomUUID(), "data.lists.combine_lists");
        
        // 璁剧疆鑺傜偣鎻忚堪
        this.description = "Combines multiple lists into a single list by index";
        
        // 鍒涘缓鍔ㄦ€佽緭鍏ョ鍙?
        rebuildInputPorts();
        
        // 鍒涘缓杈撳嚭绔彛
        IPort listOutput = new BasePort(OUTPUT_LIST_ID, "Combined List", 
                "The resulting combined list", NodeDataType.LIST, this);
        addOutputPort(listOutput);
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
     * 鏍规嵁褰撳墠璁剧疆鐨勮緭鍏ユ暟閲忓垱寤鸿緭鍏ョ鍙?
     */
    private void rebuildInputPorts() {
        // 娓呴櫎鎵€鏈夌幇鏈夌殑杈撳叆绔彛
        inputPorts.clear();
        
        // 鍒涘缓鏂扮殑杈撳叆绔彛
        for (int i = 0; i < inputCount; i++) {
            String portId = "input_list_" + i;
            IPort inputPort = new BasePort(portId, "List " + (i + 1), 
                    "Input list " + (i + 1), NodeDataType.LIST, this);
            addInputPort(inputPort);
        }
    }
    
    /**
     * 鑺傜偣鐨勮绠楅€昏緫
     * @param context 鎵ц涓婁笅鏂?
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        List<Object> resultList = new ArrayList<>();
        List<List<?>> inputLists = new ArrayList<>();
        int maxLength = 0;
        
        // 鏀堕泦鎵€鏈夎緭鍏ュ垪琛?
        for (int i = 0; i < inputCount; i++) {
            String portId = "input_list_" + i;
            Object listObj = inputValues.get(portId);
            
            if (listObj instanceof List) {
                List<?> list = (List<?>) listObj;
                inputLists.add(list);
                maxLength = Math.max(maxLength, list.size());
            } else {
                // 濡傛灉杈撳叆涓嶆槸鍒楄〃锛屾坊鍔犱竴涓┖鍒楄〃
                inputLists.add(new ArrayList<>());
            }
        }
        
        // 鎸夌储寮曠粍鍚堝垪琛?
        for (int i = 0; i < maxLength; i++) {
            List<Object> combinedRow = new ArrayList<>();
            boolean rowComplete = true;
            
            // 浠庢瘡涓緭鍏ュ垪琛ㄨ幏鍙栧綋鍓嶇储寮曠殑鍏冪礌
            for (List<?> list : inputLists) {
                if (i < list.size()) {
                    combinedRow.add(list.get(i));
                } else {
                    combinedRow.add(null);
                    rowComplete = false;
                }
            }
            
            // 鏍规嵁skipIncomplete璁剧疆鍐冲畾鏄惁娣诲姞涓嶅畬鏁寸殑琛?
            if (rowComplete || !skipIncomplete) {
                if (outputAsTuples) {
                    // 娣诲姞浣滀负鍏冪粍锛堝垪琛級
                    resultList.add(combinedRow);
                } else {
                    // 娣诲姞涓烘墎骞冲厓绱?
                    resultList.addAll(combinedRow);
                }
            }
        }
        
        // 璁剧疆杈撳嚭
        outputValues.put(OUTPUT_LIST_ID, resultList);
    }
    
    /**
     * 澧炲姞杈撳叆绔彛鏁伴噺
     */
    public void increaseInputCount() {
        if (inputCount < 10) { // 璁剧疆涓€涓悎鐞嗙殑涓婇檺
            inputCount++;
            rebuildInputPorts();
            markDirty();
        }
    }
    
    /**
     * 鍑忓皯杈撳叆绔彛鏁伴噺
     */
    public void decreaseInputCount() {
        if (inputCount > 2) { // 鑷冲皯淇濈暀2涓緭鍏?
            inputCount--;
            rebuildInputPorts();
            markDirty();
        }
    }
    
    // --- Getters/Setters for Properties ---
    
    public int getInputCount() {
        return inputCount;
    }
    
    public void setInputCount(int count) {
        if (count >= 2 && count <= 10 && count != inputCount) {
            inputCount = count;
            rebuildInputPorts();
            markDirty();
        }
    }
    
    public boolean isSkipIncomplete() {
        return skipIncomplete;
    }
    
    public void setSkipIncomplete(boolean skip) {
        this.skipIncomplete = skip;
        markDirty();
    }
    
    public boolean isOutputAsTuples() {
        return outputAsTuples;
    }
    
    public void setOutputAsTuples(boolean asTuples) {
        this.outputAsTuples = asTuples;
        markDirty();
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("inputCount", getInputCount());
        state.put("skipIncomplete", isSkipIncomplete());
        state.put("outputAsTuples", isOutputAsTuples());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("skipIncomplete")) {
                Object skip = stateMap.get("skipIncomplete");
                if (skip instanceof Boolean) {
                    setSkipIncomplete((Boolean) skip);
                }
            }
            
            if (stateMap.containsKey("outputAsTuples")) {
                Object tuples = stateMap.get("outputAsTuples");
                if (tuples instanceof Boolean) {
                    setOutputAsTuples((Boolean) tuples);
                }
            }
            
            // 鏈€鍚庤缃緭鍏ョ鍙ｆ暟閲忥紝鍥犱负杩欎細瑙﹀彂绔彛閲嶅缓
            if (stateMap.containsKey("inputCount")) {
                Object count = stateMap.get("inputCount");
                if (count instanceof Number) {
                    setInputCount(((Number) count).intValue());
                }
            }
        }
    }
} 