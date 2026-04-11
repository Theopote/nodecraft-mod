package com.nodecraft.nodesystem.nodes.math.list_sequence;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * 鎵撲贡鍒楄〃鑺傜偣锛岄殢鏈洪噸鎺掑垪琛ㄥ厓绱?
 */
@NodeInfo(
    id = "data.lists.shuffle_list",
    displayName = "Shuffle List",
    description = "Randomly reorders elements in a list",
    category = "data.lists"
)
public class ShuffleListNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    private long seed = 0; // 闅忔満鏁扮瀛愶紝0琛ㄧず浣跨敤绯荤粺鏃堕棿
    private boolean preserveInput = false; // 鏄惁淇濈暀杈撳叆鍒楄〃
    private String description; // 瀛樺偍鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_SEED_ID = "input_seed";
    private static final String OUTPUT_LIST_ID = "output_list";
    
    /**
     * 鏋勯€犱竴涓柊鐨勬墦涔卞垪琛ㄨ妭鐐?
     */
    public ShuffleListNode() {
        // 璋冪敤鐖剁被鏋勯€犲嚱鏁帮紝浣跨敤UUID.randomUUID()鐢熸垚鏂扮殑ID
        super(UUID.randomUUID(), "data.lists.shuffle_list");
        
        // 璁剧疆鑺傜偣鎻忚堪
        this.description = "Randomly reorders elements in a list";
        
        // 鍒涘缓杈撳叆绔彛
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list to shuffle", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        IPort seedInput = new BasePort(INPUT_SEED_ID, "Seed", 
                "Optional random seed (integer)", NodeDataType.INTEGER, this);
        addInputPort(seedInput);
        
        // 鍒涘缓杈撳嚭绔彛
        IPort listOutput = new BasePort(OUTPUT_LIST_ID, "Shuffled List", 
                "The list with elements in random order", NodeDataType.LIST, this);
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
     * 鑺傜偣鐨勮绠楅€昏緫
     * @param context 鎵ц涓婁笅鏂?
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 鑾峰彇杈撳叆
        Object inputObj = inputValues.get(INPUT_LIST_ID);
        Object seedObj = inputValues.get(INPUT_SEED_ID);
        
        List<Object> resultList = new ArrayList<>();
        
        // 澶勭悊鍒楄〃
        if (inputObj instanceof List) {
            List<?> inputList = (List<?>) inputObj;
            
            // 鍒涘缓涓€涓彲淇敼鐨勬柊鍒楄〃骞舵坊鍔犳墍鏈夐」
            resultList.addAll(inputList);
            
            // 鑾峰彇闅忔満鏁扮瀛?
            long actualSeed = seed;
            if (seedObj instanceof Number) {
                actualSeed = ((Number) seedObj).longValue();
            }
            
            // 鎵撲贡鍒楄〃
            if (!resultList.isEmpty()) {
                if (actualSeed != 0) {
                    // 浣跨敤鐗瑰畾绉嶅瓙
                    Collections.shuffle(resultList, new Random(actualSeed));
                } else {
                    // 浣跨敤绯荤粺鏃堕棿浣滀负绉嶅瓙
                    Collections.shuffle(resultList);
                }
            }
        }
        
        // 璁剧疆杈撳嚭
        outputValues.put(OUTPUT_LIST_ID, resultList);
    }
    
    // --- Getters/Setters for Properties ---
    
    public long getSeed() {
        return seed;
    }
    
    public void setSeed(long seed) {
        this.seed = seed;
        markDirty();
    }
    
    public boolean isPreserveInput() {
        return preserveInput;
    }
    
    public void setPreserveInput(boolean preserve) {
        this.preserveInput = preserve;
        markDirty();
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("seed", getSeed());
        state.put("preserveInput", isPreserveInput());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("seed")) {
                Object seedObj = stateMap.get("seed");
                if (seedObj instanceof Number) {
                    setSeed(((Number) seedObj).longValue());
                }
            }
            
            if (stateMap.containsKey("preserveInput")) {
                Object preserve = stateMap.get("preserveInput");
                if (preserve instanceof Boolean) {
                    setPreserveInput((Boolean) preserve);
                }
            }
        }
    }
} 