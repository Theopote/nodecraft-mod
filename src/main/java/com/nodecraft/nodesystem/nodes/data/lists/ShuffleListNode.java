package com.nodecraft.nodesystem.nodes.data.lists;

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
 * 打乱列表节点，随机重排列表元素
 */
@NodeInfo(
    id = "data.lists.shuffle_list",
    displayName = "Shuffle List",
    description = "Randomly reorders elements in a list",
    category = "data.lists"
)
public class ShuffleListNode extends BaseNode {
    
    // --- 节点属性 ---
    private long seed = 0; // 随机数种子，0表示使用系统时间
    private boolean preserveInput = false; // 是否保留输入列表
    private String description; // 存储节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_SEED_ID = "input_seed";
    private static final String OUTPUT_LIST_ID = "output_list";
    
    /**
     * 构造一个新的打乱列表节点
     */
    public ShuffleListNode() {
        // 调用父类构造函数，使用UUID.randomUUID()生成新的ID
        super(UUID.randomUUID(), "data.lists.shuffle_list");
        
        // 设置节点描述
        this.description = "Randomly reorders elements in a list";
        
        // 创建输入端口
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list to shuffle", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        IPort seedInput = new BasePort(INPUT_SEED_ID, "Seed", 
                "Optional random seed (integer)", NodeDataType.INTEGER, this);
        addInputPort(seedInput);
        
        // 创建输出端口
        IPort listOutput = new BasePort(OUTPUT_LIST_ID, "Shuffled List", 
                "The list with elements in random order", NodeDataType.LIST, this);
        addOutputPort(listOutput);
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
        Object inputObj = inputValues.get(INPUT_LIST_ID);
        Object seedObj = inputValues.get(INPUT_SEED_ID);
        
        List<Object> resultList = new ArrayList<>();
        
        // 处理列表
        if (inputObj instanceof List) {
            List<?> inputList = (List<?>) inputObj;
            
            // 创建一个可修改的新列表并添加所有项
            resultList.addAll(inputList);
            
            // 获取随机数种子
            long actualSeed = seed;
            if (seedObj instanceof Number) {
                actualSeed = ((Number) seedObj).longValue();
            }
            
            // 打乱列表
            if (!resultList.isEmpty()) {
                if (actualSeed != 0) {
                    // 使用特定种子
                    Collections.shuffle(resultList, new Random(actualSeed));
                } else {
                    // 使用系统时间作为种子
                    Collections.shuffle(resultList);
                }
            }
        }
        
        // 设置输出
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
    
    // --- 节点状态序列化 ---
    
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