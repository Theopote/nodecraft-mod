package com.nodecraft.nodesystem.nodes.data.lists;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * 列表长度节点，获取列表的长度（元素数量）
 */
@NodeInfo(
    id = "data.lists.list_length",
    displayName = "列表长度",
    description = "获取列表中元素的数量",
    category = "data.lists"
)
public class ListLengthNode extends BaseNode {
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_LIST_ID = "input_list";
    private static final String OUTPUT_LENGTH_ID = "output_length";
    private String description; // 存储节点描述
    
    /**
     * 构造一个新的列表长度节点
     */
    public ListLengthNode() {
        // 调用父类构造函数，使用UUID.randomUUID()生成新的ID
        super(UUID.randomUUID(), "data.lists.list_length");
        
        // 设置节点描述
        this.description = "Returns the number of items in a list";
        
        // 创建输入端口
        IPort listInput = new BasePort(INPUT_LIST_ID, "List", 
                "The list to get the length of", NodeDataType.LIST, this);
        addInputPort(listInput);
        
        // 创建输出端口
        IPort lengthOutput = new BasePort(OUTPUT_LENGTH_ID, "Length", 
                "The number of items in the list", NodeDataType.INTEGER, this);
        addOutputPort(lengthOutput);
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
        // 获取输入列表
        Object inputObj = inputValues.get(INPUT_LIST_ID);
        int length = 0;
        
        // 计算列表长度
        if (inputObj instanceof List) {
            List<?> list = (List<?>) inputObj;
            length = list.size();
        }
        
        // 设置输出
        outputValues.put(OUTPUT_LENGTH_ID, length);
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