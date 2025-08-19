package com.nodecraft.nodesystem.nodes.inputs.sources;

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
 * 列表创建节点，将多个输入项打包成一个List
 */
@NodeInfo(
    id = "inputs.sources.create_list",
    displayName = "创建列表",
    description = "将多个输入项打包成一个列表",
    category = "inputs.sources"
)
public class CreateListNode extends BaseNode {
    
    // --- 节点属性 ---
    private int inputCount = 3; // 默认输入端口数量
    private boolean allowDifferentTypes = true; // 是否允许不同类型的输入
    
    // --- 输入/输出端口ID ---
    private static final String OUTPUT_LIST_ID = "output_list";
    
    /**
     * 构造一个新的列表创建节点
     */
    public CreateListNode() {
        // 使用新的分类命名 - inputs.sources.create_list
        super(UUID.randomUUID(), "inputs.sources.create_list");
        
        // 创建动态输入端口
        rebuildInputPorts();
        
        // 创建输出端口
        IPort listOutput = new BasePort(OUTPUT_LIST_ID, "List", 
                "The resulting list containing all input items", NodeDataType.LIST, this);
        addOutputPort(listOutput);
    }
    
    @Override
    public String getDescription() {
        return "Creates a list from multiple input items";
    }
    
    @Override
    public String getDisplayName() {
        return "Create List";
    }
    
    /**
     * 根据当前设置的输入数量创建输入端口
     */
    private void rebuildInputPorts() {
        // 清除所有现有的输入端口
        inputPorts.clear();
        
        // 创建新的输入端口
        for (int i = 0; i < inputCount; i++) {
            String portId = "input_" + i;
            IPort inputPort = new BasePort(portId, "Item " + (i + 1), 
                    "Item to add to the list", NodeDataType.ANY, this);
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
        
        // 收集所有非空输入
        for (int i = 0; i < inputCount; i++) {
            String portId = "input_" + i;
            Object value = inputValues.get(portId);
            
            if (value != null) {
                resultList.add(value);
            }
        }
        
        // 设置输出
        outputValues.put(OUTPUT_LIST_ID, resultList);
    }
    
    /**
     * 添加一个输入端口
     */
    public void addInputPort() {
        inputCount++;
        rebuildInputPorts();
        markDirty();
    }
    
    /**
     * 移除一个输入端口
     * @return 是否成功移除
     */
    public boolean removeInputPort() {
        if (inputCount > 1) {
            inputCount--;
            rebuildInputPorts();
            markDirty();
            return true;
        }
        return false;
    }
    
    // --- Getters/Setters for Properties ---
    
    public int getInputCount() {
        return inputCount;
    }
    
    public void setInputCount(int count) {
        if (count > 0 && count != inputCount) {
            inputCount = count;
            rebuildInputPorts();
            markDirty();
        }
    }
    
    public boolean isAllowDifferentTypes() {
        return allowDifferentTypes;
    }
    
    public void setAllowDifferentTypes(boolean allow) {
        this.allowDifferentTypes = allow;
        // 这个属性不影响输出，只影响输入验证，所以不需要markDirty()
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("inputCount", getInputCount());
        state.put("allowDifferentTypes", isAllowDifferentTypes());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            // 读取属性
            if (stateMap.containsKey("allowDifferentTypes")) {
                Object allowDiff = stateMap.get("allowDifferentTypes");
                if (allowDiff instanceof Boolean) {
                    setAllowDifferentTypes((Boolean) allowDiff);
                }
            }
            
            // 最后设置输入端口数量，因为这会触发端口重建
            if (stateMap.containsKey("inputCount")) {
                Object count = stateMap.get("inputCount");
                if (count instanceof Integer) {
                    setInputCount((Integer) count);
                }
            }
        }
    }
    
    /**
     * 标记节点需要重新计算
     */
    public void markDirty() {
        // 未连接到图执行逻辑，预留实现
    }
} 