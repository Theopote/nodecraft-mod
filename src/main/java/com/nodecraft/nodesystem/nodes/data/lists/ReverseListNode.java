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
import java.util.UUID;

/**
 * 反转列表节点，将列表元素顺序反转
 */
@NodeInfo(
    id = "data.lists.reverse_list",
    displayName = "反转列表",
    description = "将列表元素顺序反转",
    category = "data.lists"
)
public class ReverseListNode extends BaseNode {
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String OUTPUT_LIST_ID = "output_list";
    private String description; // 存储节点描述
    
    /**
     * 构造一个新的反转列表节点
     */
    public ReverseListNode() {
        // 调用父类构造函数，使用UUID.randomUUID()生成新的ID
        super(UUID.randomUUID(), "data.lists.reverse_list");
        
        // 设置节点描述
        this.description = "Reverses the order of elements in a list";
        
        // 创建输入端口
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list to reverse", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        // 创建输出端口
        IPort listOutput = new BasePort(OUTPUT_LIST_ID, "Reversed List", 
                "The list with elements in reverse order", NodeDataType.LIST, this);
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
        
        List<Object> resultList = new ArrayList<>();
        
        // 处理列表
        if (inputObj instanceof List) {
            List<?> inputList = (List<?>) inputObj;
            
            // 创建一个可修改的新列表并添加所有项
            resultList.addAll(inputList);
            
            // 反转列表
            Collections.reverse(resultList);
        }
        
        // 设置输出
        outputValues.put(OUTPUT_LIST_ID, resultList);
    }
    
    // 此节点没有需要序列化的自定义状态
    @Override
    public Object getNodeState() {
        return null;
    }
    
    @Override
    public void setNodeState(Object state) {
        // 无需额外状态处理
    }
} 