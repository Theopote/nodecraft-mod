package com.nodecraft.nodesystem.nodes.math.list_sequence;

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

/**
 * 灞曞钩鍒楄〃鑺傜偣锛屽皢宓屽鍒楄〃缁撴瀯灞曞钩涓哄崟灞傚垪琛?
 */
@NodeInfo(
    id = "data.lists.flatten_list",
    displayName = "Flatten List",
    description = "Flattens a nested list structure into a single-level list",
    category = "data.lists"
)
public class FlattenListNode extends BaseNode {
    
    // --- 鑺傜偣灞炴€?---
    private int maxDepth = -1; // 鏈€澶у睍骞虫繁搴︼紝-1琛ㄧず鏃犻檺娣卞害
    private boolean preserveTypes = false; // 鏄惁淇濈暀闈炲垪琛ㄧ被鍨嬩笉灞曞钩
    private String description = "Flattens a nested list structure into a single-level list"; // 鑺傜偣鎻忚堪
    
    // --- 杈撳叆/杈撳嚭绔彛ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_DEPTH_ID = "input_depth";
    private static final String OUTPUT_LIST_ID = "output_list";
    
    /**
     * 鏋勯€犱竴涓柊鐨勫睍骞冲垪琛ㄨ妭鐐?
     */
    public FlattenListNode() {
        // 浣跨敤鍒嗙被鍛藉悕 - data.lists.flatten_list
        super(UUID.randomUUID(), "data.lists.flatten_list");
        
        // 鍒涘缓杈撳叆绔彛
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The nested list to flatten", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        IPort depthInput = new BasePort(INPUT_DEPTH_ID, "Depth", 
                "Maximum flattening depth (optional)", NodeDataType.INTEGER, this);
        addInputPort(depthInput);
        
        // 鍒涘缓杈撳嚭绔彛
        IPort listOutput = new BasePort(OUTPUT_LIST_ID, "Flattened List", 
                "The resulting flattened list", NodeDataType.LIST, this);
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
        Object listObj = inputValues.get(INPUT_LIST_ID);
        Object depthObj = inputValues.get(INPUT_DEPTH_ID);
        
        // 纭畾灞曞钩娣卞害
        int depth = maxDepth;
        if (depthObj instanceof Number) {
            depth = ((Number) depthObj).intValue();
        }
        
        List<Object> resultList = new ArrayList<>();
        
        // 鎵ц灞曞钩
        if (listObj instanceof List) {
            flatten((List<?>) listObj, resultList, 0, depth);
        } else if (listObj != null) {
            // 闈炲垪琛ㄨ緭鍏ワ紝鐩存帴娣诲姞鍒扮粨鏋?
            resultList.add(listObj);
        }
        
        // 璁剧疆杈撳嚭
        outputValues.put(OUTPUT_LIST_ID, resultList);
    }
    
    /**
     * 閫掑綊灞曞钩鍒楄〃
     * @param input 杈撳叆鍒楄〃
     * @param output 杈撳嚭鍒楄〃
     * @param currentDepth 褰撳墠娣卞害
     * @param maxDepth 鏈€澶ф繁搴?
     */
    private void flatten(List<?> input, List<Object> output, int currentDepth, int maxDepth) {
        // 妫€鏌ユ槸鍚﹁揪鍒版渶澶ф繁搴?
        if (maxDepth >= 0 && currentDepth >= maxDepth) {
            output.add(input);
            return;
        }
        
        // 澶勭悊鍒楄〃涓殑姣忎釜鍏冪礌
        for (Object item : input) {
            if (item instanceof List) {
                // 閫掑綊澶勭悊宓屽鍒楄〃
                flatten((List<?>) item, output, currentDepth + 1, maxDepth);
            } else if (preserveTypes || item == null) {
                // 闈炲垪琛ㄥ厓绱犵洿鎺ユ坊鍔?
                output.add(item);
            } else {
                // 灏濊瘯灏嗛潪鍒楄〃鍏冪礌涔熻涓哄彲鑳界殑鍒楄〃
                try {
                    Object[] array = (Object[]) item;
                    for (Object arrayItem : array) {
                        output.add(arrayItem);
                    }
                } catch (ClassCastException e) {
                    // 涓嶆槸鏁扮粍锛岀洿鎺ユ坊鍔?
                    output.add(item);
                }
            }
        }
    }
    
    // --- Getters/Setters for Properties ---
    
    public int getMaxDepth() {
        return maxDepth;
    }
    
    public void setMaxDepth(int depth) {
        this.maxDepth = depth;
        markDirty();
    }
    
    public boolean isPreserveTypes() {
        return preserveTypes;
    }
    
    public void setPreserveTypes(boolean preserve) {
        this.preserveTypes = preserve;
        markDirty();
    }
    
    // --- 鑺傜偣鐘舵€佸簭鍒楀寲 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("maxDepth", getMaxDepth());
        state.put("preserveTypes", isPreserveTypes());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("maxDepth")) {
                Object depth = stateMap.get("maxDepth");
                if (depth instanceof Number) {
                    setMaxDepth(((Number) depth).intValue());
                }
            }
            
            if (stateMap.containsKey("preserveTypes")) {
                Object preserve = stateMap.get("preserveTypes");
                if (preserve instanceof Boolean) {
                    setPreserveTypes((Boolean) preserve);
                }
            }
        }
    }
} 