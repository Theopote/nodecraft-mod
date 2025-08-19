package com.nodecraft.nodesystem.nodes.data.lists;

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
 * 展平列表节点，将嵌套列表结构展平为单层列表
 */
@NodeInfo(
    id = "data.lists.flatten_list",
    displayName = "Flatten List",
    description = "Flattens a nested list structure into a single-level list",
    category = "data.lists"
)
public class FlattenListNode extends BaseNode {
    
    // --- 节点属性 ---
    private int maxDepth = -1; // 最大展平深度，-1表示无限深度
    private boolean preserveTypes = false; // 是否保留非列表类型不展平
    private String description = "Flattens a nested list structure into a single-level list"; // 节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_DEPTH_ID = "input_depth";
    private static final String OUTPUT_LIST_ID = "output_list";
    
    /**
     * 构造一个新的展平列表节点
     */
    public FlattenListNode() {
        // 使用分类命名 - data.lists.flatten_list
        super(UUID.randomUUID(), "data.lists.flatten_list");
        
        // 创建输入端口
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The nested list to flatten", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        IPort depthInput = new BasePort(INPUT_DEPTH_ID, "Depth", 
                "Maximum flattening depth (optional)", NodeDataType.INTEGER, this);
        addInputPort(depthInput);
        
        // 创建输出端口
        IPort listOutput = new BasePort(OUTPUT_LIST_ID, "Flattened List", 
                "The resulting flattened list", NodeDataType.LIST, this);
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
        Object listObj = inputValues.get(INPUT_LIST_ID);
        Object depthObj = inputValues.get(INPUT_DEPTH_ID);
        
        // 确定展平深度
        int depth = maxDepth;
        if (depthObj instanceof Number) {
            depth = ((Number) depthObj).intValue();
        }
        
        List<Object> resultList = new ArrayList<>();
        
        // 执行展平
        if (listObj instanceof List) {
            flatten((List<?>) listObj, resultList, 0, depth);
        } else if (listObj != null) {
            // 非列表输入，直接添加到结果
            resultList.add(listObj);
        }
        
        // 设置输出
        outputValues.put(OUTPUT_LIST_ID, resultList);
    }
    
    /**
     * 递归展平列表
     * @param input 输入列表
     * @param output 输出列表
     * @param currentDepth 当前深度
     * @param maxDepth 最大深度
     */
    private void flatten(List<?> input, List<Object> output, int currentDepth, int maxDepth) {
        // 检查是否达到最大深度
        if (maxDepth >= 0 && currentDepth >= maxDepth) {
            output.add(input);
            return;
        }
        
        // 处理列表中的每个元素
        for (Object item : input) {
            if (item instanceof List) {
                // 递归处理嵌套列表
                flatten((List<?>) item, output, currentDepth + 1, maxDepth);
            } else if (preserveTypes || item == null) {
                // 非列表元素直接添加
                output.add(item);
            } else {
                // 尝试将非列表元素也视为可能的列表
                try {
                    Object[] array = (Object[]) item;
                    for (Object arrayItem : array) {
                        output.add(arrayItem);
                    }
                } catch (ClassCastException e) {
                    // 不是数组，直接添加
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
    
    // --- 节点状态序列化 ---
    
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