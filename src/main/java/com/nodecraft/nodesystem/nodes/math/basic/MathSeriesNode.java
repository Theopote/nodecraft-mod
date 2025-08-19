package com.nodecraft.nodesystem.nodes.math.basic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Series Node: Generates an arithmetic series (Start, Start + Step, Start + 2*Step, ...).
 */
@NodeInfo(
    id = "math.basic.series",
    displayName = "数学序列",
    description = "生成等差数列（起始值，起始值+步长，起始值+2*步长，...）",
    category = "math.basic"
)
public class MathSeriesNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_START_ID = "input_start";
    private static final String INPUT_STEP_ID = "input_step";
    private static final String INPUT_COUNT_ID = "input_count";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_SERIES_ID = "output_series";

    // --- 构造函数 ---
    public MathSeriesNode() {
        super(UUID.randomUUID(), "math.basic.series");
        
        // 创建并添加输入端口 (默认 Start=0, Step=1, Count=10)
        addInputPort(new BasePort(INPUT_START_ID, "Start", "The first number in the series", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_STEP_ID, "Step", "The increment between numbers", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_COUNT_ID, "Count", "The total number of items in the series", NodeDataType.INTEGER, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_SERIES_ID, "Series", "The generated list of numbers", NodeDataType.ANY, this)); // Output: List<Double>
    }
    
    @Override
    public String getDescription() {
        return "Generates an arithmetic series with Start, Step, and Count.";
    }
    
    @Override
    public String getDisplayName() {
        return "Series";
    }

    // --- 核心逻辑 ---
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        double start = getValueAsDouble(inputValues.get(INPUT_START_ID), 0.0);
        double step = getValueAsDouble(inputValues.get(INPUT_STEP_ID), 1.0);
        int count = getValueAsInt(inputValues.get(INPUT_COUNT_ID), 10);

        List<Double> series = new ArrayList<>();

        // 检查 count 是否有效
        if (count <= 0) {
             outputValues.put(OUTPUT_SERIES_ID, Collections.emptyList());
             return;
        }
        
        double current = start;
        for (int i = 0; i < count; i++) {
            series.add(current);
            current += step;
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_SERIES_ID, Collections.unmodifiableList(series));
    }
    
    /** Helper method to safely convert an input object to double. */
    private double getValueAsDouble(Object value, double defaultValue) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }
    
    /** Helper method to safely convert an input object to int. */
    private int getValueAsInt(Object value, int defaultValue) {
        if (value instanceof Number) {
            // Round double/float inputs, clamp to int range if necessary
            double doubleVal = ((Number) value).doubleValue();
            if (doubleVal >= Integer.MIN_VALUE && doubleVal <= Integer.MAX_VALUE) {
                 return (int) Math.round(doubleVal);
            }
        }
        // Handle potential NumberFormatException if value is String?
        // For now, only handle Number types.
        return defaultValue;
    }

    // --- Getters/Setters (不需要) ---

    // --- (反)序列化 (不需要) ---
} 