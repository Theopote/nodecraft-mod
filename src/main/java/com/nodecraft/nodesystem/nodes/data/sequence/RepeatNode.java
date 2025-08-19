package com.nodecraft.nodesystem.nodes.data.sequence;

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
 * 重复数据节点，将单个数据或列表重复指定次数
 */
@NodeInfo(
    id = "data.sequence.repeat",
    displayName = "Repeat",
    description = "Repeats a single data item or list multiple times",
    category = "data.sequence"
)
public class RepeatNode extends BaseNode {
    
    // --- 节点属性 ---
    private int defaultCount = 3; // 默认重复次数
    private String description; // 存储节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_DATA_ID = "input_data";
    private static final String INPUT_COUNT_ID = "input_count";
    private static final String OUTPUT_RESULT_ID = "output_result";
    private static final String OUTPUT_LENGTH_ID = "output_length";
    
    /**
     * 构造一个新的重复数据节点
     */
    public RepeatNode() {
        // 调用父类构造函数，使用UUID.randomUUID()生成新的ID
        super(UUID.randomUUID(), "data.sequence.repeat");
        
        // 设置节点描述
        this.description = "Repeats a single data item or list multiple times";
        
        // 创建输入端口
        IPort dataInput = new BasePort(INPUT_DATA_ID, "Data", 
                "The data to repeat (single value or list)", NodeDataType.ANY, this);
        addInputPort(dataInput);
        
        IPort countInput = new BasePort(INPUT_COUNT_ID, "Count", 
                "Number of times to repeat", NodeDataType.INTEGER, this);
        addInputPort(countInput);
        
        // 创建输出端口
        IPort resultOutput = new BasePort(OUTPUT_RESULT_ID, "Result", 
                "The repeated data as a list", NodeDataType.LIST, this);
        addOutputPort(resultOutput);
        
        IPort lengthOutput = new BasePort(OUTPUT_LENGTH_ID, "Length", 
                "Length of the resulting list", NodeDataType.INTEGER, this);
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
        // 获取输入
        Object dataObj = inputValues.get(INPUT_DATA_ID);
        Object countObj = inputValues.get(INPUT_COUNT_ID);
        
        // 设置默认值并处理输入
        int count = defaultCount;
        if (countObj instanceof Number) {
            count = ((Number) countObj).intValue();
            // 确保数量为正数
            count = Math.max(0, count);
        }
        
        // 处理输入数据是列表的情况
        if (dataObj instanceof List) {
            List<?> inputList = (List<?>) dataObj;
            List<Object> result = new ArrayList<>();
            
            // 重复列表指定次数
            for (int i = 0; i < count; i++) {
                result.addAll(inputList);
            }
            
            // 设置输出
            outputValues.put(OUTPUT_RESULT_ID, result);
            outputValues.put(OUTPUT_LENGTH_ID, result.size());
        } 
        // 处理输入数据是单个值的情况
        else {
            List<Object> result = new ArrayList<>();
            
            // 重复单个值指定次数
            for (int i = 0; i < count; i++) {
                result.add(dataObj);
            }
            
            // 设置输出
            outputValues.put(OUTPUT_RESULT_ID, result);
            outputValues.put(OUTPUT_LENGTH_ID, result.size());
        }
    }
    
    // --- Getters/Setters for Properties ---
    
    public int getDefaultCount() {
        return defaultCount;
    }
    
    public void setDefaultCount(int count) {
        this.defaultCount = Math.max(0, count);
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("defaultCount", getDefaultCount());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("defaultCount")) {
                Object count = stateMap.get("defaultCount");
                if (count instanceof Number) {
                    setDefaultCount(((Number) count).intValue());
                }
            }
        }
    }
} 