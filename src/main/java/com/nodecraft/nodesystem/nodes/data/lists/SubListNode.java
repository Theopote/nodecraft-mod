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
 * 子列表节点，获取列表的子集
 */
@NodeInfo(
    id = "data.lists.sub_list",
    displayName = "子列表",
    description = "获取列表的子集（指定起始和结束索引）",
    category = "data.lists"
)
public class SubListNode extends BaseNode {
    
    // --- 节点属性 ---
    private boolean allowNegativeIndex = true; // 是否允许负索引（从列表末尾开始计算）
    private boolean clampToList = true; // 是否将索引限制在列表范围内
    private String description; // 存储节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String INPUT_START_ID = "input_start";
    private static final String INPUT_END_ID = "input_end";
    private static final String OUTPUT_SUBLIST_ID = "output_sublist";
    
    /**
     * 构造一个新的子列表节点
     */
    public SubListNode() {
        // 调用父类构造函数，使用UUID.randomUUID()生成新的ID
        super(UUID.randomUUID(), "data.lists.sub_list");
        
        // 设置节点描述
        this.description = "Gets a portion of a list between start and end indexes";
        
        // 创建输入端口
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The source list", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        IPort startInput = new BasePort(INPUT_START_ID, "Start Index", 
                "The starting index (inclusive, 0-based)", NodeDataType.INTEGER, this);
        addInputPort(startInput);
        
        IPort endInput = new BasePort(INPUT_END_ID, "End Index", 
                "The ending index (exclusive)", NodeDataType.INTEGER, this);
        addInputPort(endInput);
        
        // 创建输出端口
        IPort sublistOutput = new BasePort(OUTPUT_SUBLIST_ID, "Sub List", 
                "The resulting sub list", NodeDataType.LIST, this);
        addOutputPort(sublistOutput);
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
        Object startObj = inputValues.get(INPUT_START_ID);
        Object endObj = inputValues.get(INPUT_END_ID);
        
        List<Object> resultList = new ArrayList<>();
        
        // 处理列表
        if (inputObj instanceof List) {
            List<?> inputList = (List<?>) inputObj;
            int listSize = inputList.size();
            
            // 默认起始和结束索引
            int startIndex = 0;
            int endIndex = listSize;
            
            // 处理起始索引
            if (startObj instanceof Number) {
                startIndex = ((Number) startObj).intValue();
                
                // 处理负索引
                if (startIndex < 0 && allowNegativeIndex) {
                    startIndex = listSize + startIndex;
                }
                
                // 限制在列表范围内
                if (clampToList) {
                    startIndex = Math.max(0, Math.min(startIndex, listSize));
                } else if (startIndex < 0 || startIndex > listSize) {
                    // 如果不限制且超出范围，返回空列表
                    outputValues.put(OUTPUT_SUBLIST_ID, resultList);
                    return;
                }
            }
            
            // 处理结束索引
            if (endObj instanceof Number) {
                endIndex = ((Number) endObj).intValue();
                
                // 处理负索引
                if (endIndex < 0 && allowNegativeIndex) {
                    endIndex = listSize + endIndex;
                }
                
                // 限制在列表范围内
                if (clampToList) {
                    endIndex = Math.max(startIndex, Math.min(endIndex, listSize));
                } else if (endIndex < startIndex || endIndex > listSize) {
                    // 如果不限制且结束索引无效，则调整处理
                    if (endIndex < startIndex) {
                        outputValues.put(OUTPUT_SUBLIST_ID, resultList);
                        return;
                    }
                    endIndex = Math.min(endIndex, listSize);
                }
            }
            
            // 创建子列表
            for (int i = startIndex; i < endIndex; i++) {
                resultList.add(inputList.get(i));
            }
        }
        
        // 设置输出
        outputValues.put(OUTPUT_SUBLIST_ID, resultList);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isAllowNegativeIndex() {
        return allowNegativeIndex;
    }
    
    public void setAllowNegativeIndex(boolean allow) {
        this.allowNegativeIndex = allow;
        markDirty();
    }
    
    public boolean isClampToList() {
        return clampToList;
    }
    
    public void setClampToList(boolean clamp) {
        this.clampToList = clamp;
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("allowNegativeIndex", isAllowNegativeIndex());
        state.put("clampToList", isClampToList());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("allowNegativeIndex")) {
                Object allow = stateMap.get("allowNegativeIndex");
                if (allow instanceof Boolean) {
                    setAllowNegativeIndex((Boolean) allow);
                }
            }
            
            if (stateMap.containsKey("clampToList")) {
                Object clamp = stateMap.get("clampToList");
                if (clamp instanceof Boolean) {
                    setClampToList((Boolean) clamp);
                }
            }
        }
    }
} 