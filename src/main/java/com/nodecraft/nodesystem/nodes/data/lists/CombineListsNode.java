package com.nodecraft.nodesystem.nodes.data.lists;

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
 * 合并列表节点，将多个列表按索引组合成一个嵌套列表
 */
@NodeInfo(
    id = "data.lists.combine_lists",
    displayName = "合并列表",
    description = "将两个列表合并为一个列表",
    category = "data.lists"
)
public class CombineListsNode extends BaseNode {
    
    // --- 节点属性 ---
    private int inputCount = 2; // 输入列表数量，默认为2
    private boolean skipIncomplete = false; // 当某索引处有列表缺失元素时是否跳过
    private boolean outputAsTuples = true; // 是否将每组合并后的元素作为子列表输出
    private String description; // 存储节点描述
    
    // --- 输出端口ID ---
    private static final String OUTPUT_LIST_ID = "output_list";
    
    /**
     * 构造一个新的合并列表节点
     */
    public CombineListsNode() {
        // 调用父类构造函数，使用UUID.randomUUID()生成新的ID
        super(UUID.randomUUID(), "data.lists.combine_lists");
        
        // 设置节点描述
        this.description = "Combines multiple lists into a single list by index";
        
        // 创建动态输入端口
        rebuildInputPorts();
        
        // 创建输出端口
        IPort listOutput = new BasePort(OUTPUT_LIST_ID, "Combined List", 
                "The resulting combined list", NodeDataType.LIST, this);
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
     * 根据当前设置的输入数量创建输入端口
     */
    private void rebuildInputPorts() {
        // 清除所有现有的输入端口
        inputPorts.clear();
        
        // 创建新的输入端口
        for (int i = 0; i < inputCount; i++) {
            String portId = "input_list_" + i;
            IPort inputPort = new BasePort(portId, "List " + (i + 1), 
                    "Input list " + (i + 1), NodeDataType.LIST, this);
            addInputPort(inputPort);
        }
    }
    
    /**
     * 节点的计算逻辑
     * @param context 执行上下文
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        List<Object> resultList = new ArrayList<>();
        List<List<?>> inputLists = new ArrayList<>();
        int maxLength = 0;
        
        // 收集所有输入列表
        for (int i = 0; i < inputCount; i++) {
            String portId = "input_list_" + i;
            Object listObj = inputValues.get(portId);
            
            if (listObj instanceof List) {
                List<?> list = (List<?>) listObj;
                inputLists.add(list);
                maxLength = Math.max(maxLength, list.size());
            } else {
                // 如果输入不是列表，添加一个空列表
                inputLists.add(new ArrayList<>());
            }
        }
        
        // 按索引组合列表
        for (int i = 0; i < maxLength; i++) {
            List<Object> combinedRow = new ArrayList<>();
            boolean rowComplete = true;
            
            // 从每个输入列表获取当前索引的元素
            for (List<?> list : inputLists) {
                if (i < list.size()) {
                    combinedRow.add(list.get(i));
                } else {
                    combinedRow.add(null);
                    rowComplete = false;
                }
            }
            
            // 根据skipIncomplete设置决定是否添加不完整的行
            if (rowComplete || !skipIncomplete) {
                if (outputAsTuples) {
                    // 添加作为元组（列表）
                    resultList.add(combinedRow);
                } else {
                    // 添加为扁平元素
                    resultList.addAll(combinedRow);
                }
            }
        }
        
        // 设置输出
        outputValues.put(OUTPUT_LIST_ID, resultList);
    }
    
    /**
     * 增加输入端口数量
     */
    public void increaseInputCount() {
        if (inputCount < 10) { // 设置一个合理的上限
            inputCount++;
            rebuildInputPorts();
            markDirty();
        }
    }
    
    /**
     * 减少输入端口数量
     */
    public void decreaseInputCount() {
        if (inputCount > 2) { // 至少保留2个输入
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
    
    // --- 节点状态序列化 ---
    
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
            
            // 最后设置输入端口数量，因为这会触发端口重建
            if (stateMap.containsKey("inputCount")) {
                Object count = stateMap.get("inputCount");
                if (count instanceof Number) {
                    setInputCount(((Number) count).intValue());
                }
            }
        }
    }
} 