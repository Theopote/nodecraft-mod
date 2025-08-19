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
 * 范围生成节点，生成指定范围和步长的数字序列
 */
@NodeInfo(
    id = "data.sequence.range",
    displayName = "Sequence Range",
    description = "Generates a sequence of numbers within a specified range",
    category = "data.sequence"
)
public class SequenceRangeNode extends BaseNode {
    
    // --- 节点属性 ---
    private boolean useIntegerType = true; // 是否使用整数类型（否则使用浮点数）
    private boolean includeEnd = false; // 是否包含结束值
    private String description; // 存储节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_START_ID = "input_start";
    private static final String INPUT_END_ID = "input_end";
    private static final String INPUT_STEP_ID = "input_step";
    private static final String OUTPUT_RANGE_ID = "output_range";
    private static final String OUTPUT_COUNT_ID = "output_count";
    
    /**
     * 构造一个新的范围生成节点
     */
    public SequenceRangeNode() {
        // 调用父类构造函数，使用UUID.randomUUID()生成新的ID
        super(UUID.randomUUID(), "data.sequence.range");
        
        // 设置节点描述
        this.description = "Generates a sequence of numbers within a specified range";
        
        // 创建输入端口
        IPort startInput = new BasePort(INPUT_START_ID, "Start", 
                "Starting value (inclusive)", NodeDataType.DOUBLE, this);
        addInputPort(startInput);
        
        IPort endInput = new BasePort(INPUT_END_ID, "End", 
                "Ending value (exclusive by default)", NodeDataType.DOUBLE, this);
        addInputPort(endInput);
        
        IPort stepInput = new BasePort(INPUT_STEP_ID, "Step", 
                "Increment between values", NodeDataType.DOUBLE, this);
        addInputPort(stepInput);
        
        // 创建输出端口
        IPort rangeOutput = new BasePort(OUTPUT_RANGE_ID, "Range", 
                "The generated number sequence", NodeDataType.LIST, this);
        addOutputPort(rangeOutput);
        
        IPort countOutput = new BasePort(OUTPUT_COUNT_ID, "Count", 
                "The number of elements in the range", NodeDataType.INTEGER, this);
        addOutputPort(countOutput);
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
        Object startObj = inputValues.get(INPUT_START_ID);
        Object endObj = inputValues.get(INPUT_END_ID);
        Object stepObj = inputValues.get(INPUT_STEP_ID);
        
        // 设置默认值并处理输入
        double start = 0;
        if (startObj instanceof Number) {
            start = ((Number) startObj).doubleValue();
        }
        
        double end = 10;
        if (endObj instanceof Number) {
            end = ((Number) endObj).doubleValue();
        }
        
        double step = 1;
        if (stepObj instanceof Number) {
            step = ((Number) stepObj).doubleValue();
            if (step == 0) {
                step = 1; // 防止步长为0导致无限循环
            }
        }
        
        // 生成序列
        List<Object> range = new ArrayList<>();
        
        // 处理正向和反向范围
        boolean ascending = step > 0;
        
        // 确定终止条件
        double limitValue = end;
        if (includeEnd) {
            // 如果包含结束值，则在恰当的方向调整限制值
            if (ascending) {
                limitValue = end + step * 0.5; // 稍微超过end，确保包含end
            } else {
                limitValue = end - step * 0.5; // 稍微低于end，确保包含end
            }
        }
        
        // 生成序列
        if (ascending) {
            for (double value = start; value < limitValue; value += step) {
                addValueToRange(range, value);
            }
        } else {
            for (double value = start; value > limitValue; value += step) {
                addValueToRange(range, value);
            }
        }
        
        // 设置输出
        outputValues.put(OUTPUT_RANGE_ID, range);
        outputValues.put(OUTPUT_COUNT_ID, range.size());
    }
    
    /**
     * 将值添加到范围列表中，根据所选类型转换
     */
    private void addValueToRange(List<Object> range, double value) {
        if (useIntegerType) {
            range.add((int) Math.round(value));
        } else {
            range.add(value);
        }
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isUseIntegerType() {
        return useIntegerType;
    }
    
    public void setUseIntegerType(boolean useInt) {
        this.useIntegerType = useInt;
        markDirty();
    }
    
    public boolean isIncludeEnd() {
        return includeEnd;
    }
    
    public void setIncludeEnd(boolean include) {
        this.includeEnd = include;
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("useIntegerType", isUseIntegerType());
        state.put("includeEnd", isIncludeEnd());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("useIntegerType")) {
                Object useInt = stateMap.get("useIntegerType");
                if (useInt instanceof Boolean) {
                    setUseIntegerType((Boolean) useInt);
                }
            }
            
            if (stateMap.containsKey("includeEnd")) {
                Object include = stateMap.get("includeEnd");
                if (include instanceof Boolean) {
                    setIncludeEnd((Boolean) include);
                }
            }
        }
    }
} 