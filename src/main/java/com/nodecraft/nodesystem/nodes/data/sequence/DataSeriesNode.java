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
 * 等差数列节点，生成具有固定增量的数值序列
 */
@NodeInfo(
    id = "data.sequence.series",
    displayName = "Data Series",
    description = "Generates a series of numbers with constant increment",
    category = "data.sequence"
)
public class DataSeriesNode extends BaseNode {
    
    // --- 节点属性 ---
    private boolean useIntegerType = true; // 是否使用整数类型（否则使用浮点数）
    private int defaultCount = 10; // 默认生成的元素数量
    private double defaultStart = 0; // 默认起始值
    private double defaultStep = 1; // 默认步长
    private String description = "Generates a series of numbers with constant increment"; // 节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_START_ID = "input_start";
    private static final String INPUT_STEP_ID = "input_step";
    private static final String INPUT_COUNT_ID = "input_count";
    private static final String OUTPUT_SERIES_ID = "output_series";
    private static final String OUTPUT_SUM_ID = "output_sum";
    
    /**
     * 构造一个新的等差数列节点
     */
    public DataSeriesNode() {
        // 使用分类命名 - data.sequence.series
        super(UUID.randomUUID(), "data.sequence.series");
        
        // 创建输入端口
        IPort startInput = new BasePort(INPUT_START_ID, "Start", 
                "Starting value of the series", NodeDataType.DOUBLE, this);
        addInputPort(startInput);
        
        IPort stepInput = new BasePort(INPUT_STEP_ID, "Step", 
                "Increment between consecutive elements", NodeDataType.DOUBLE, this);
        addInputPort(stepInput);
        
        IPort countInput = new BasePort(INPUT_COUNT_ID, "Count", 
                "Number of elements to generate", NodeDataType.INTEGER, this);
        addInputPort(countInput);
        
        // 创建输出端口
        IPort seriesOutput = new BasePort(OUTPUT_SERIES_ID, "Series", 
                "The generated sequence", NodeDataType.LIST, this);
        addOutputPort(seriesOutput);
        
        IPort sumOutput = new BasePort(OUTPUT_SUM_ID, "Sum", 
                "Sum of all values in the series", NodeDataType.DOUBLE, this);
        addOutputPort(sumOutput);
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
        Object stepObj = inputValues.get(INPUT_STEP_ID);
        Object countObj = inputValues.get(INPUT_COUNT_ID);
        
        // 设置默认值并处理输入
        double start = defaultStart;
        if (startObj instanceof Number) {
            start = ((Number) startObj).doubleValue();
        }
        
        double step = defaultStep;
        if (stepObj instanceof Number) {
            step = ((Number) stepObj).doubleValue();
        }
        
        int count = defaultCount;
        if (countObj instanceof Number) {
            count = ((Number) countObj).intValue();
            // 确保数量为正数
            count = Math.max(0, count);
        }
        
        // 生成序列
        List<Object> series = new ArrayList<>();
        double sum = 0;
        
        for (int i = 0; i < count; i++) {
            double value = start + i * step;
            Object element;
            
            // 根据类型设置选择使用整数或浮点数
            if (useIntegerType) {
                element = (int) Math.round(value);
                sum += (int) Math.round(value);
            } else {
                element = value;
                sum += value;
            }
            
            series.add(element);
        }
        
        // 设置输出
        outputValues.put(OUTPUT_SERIES_ID, series);
        outputValues.put(OUTPUT_SUM_ID, useIntegerType ? (int) Math.round(sum) : sum);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isUseIntegerType() {
        return useIntegerType;
    }
    
    public void setUseIntegerType(boolean useInt) {
        this.useIntegerType = useInt;
        markDirty();
    }
    
    public int getDefaultCount() {
        return defaultCount;
    }
    
    public void setDefaultCount(int count) {
        this.defaultCount = Math.max(0, count);
        markDirty();
    }
    
    public double getDefaultStart() {
        return defaultStart;
    }
    
    public void setDefaultStart(double start) {
        this.defaultStart = start;
        markDirty();
    }
    
    public double getDefaultStep() {
        return defaultStep;
    }
    
    public void setDefaultStep(double step) {
        this.defaultStep = step;
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("useIntegerType", isUseIntegerType());
        state.put("defaultCount", getDefaultCount());
        state.put("defaultStart", getDefaultStart());
        state.put("defaultStep", getDefaultStep());
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
            
            if (stateMap.containsKey("defaultCount")) {
                Object count = stateMap.get("defaultCount");
                if (count instanceof Number) {
                    setDefaultCount(((Number) count).intValue());
                }
            }
            
            if (stateMap.containsKey("defaultStart")) {
                Object start = stateMap.get("defaultStart");
                if (start instanceof Number) {
                    setDefaultStart(((Number) start).doubleValue());
                }
            }
            
            if (stateMap.containsKey("defaultStep")) {
                Object step = stateMap.get("defaultStep");
                if (step instanceof Number) {
                    setDefaultStep(((Number) step).doubleValue());
                }
            }
        }
    }
} 