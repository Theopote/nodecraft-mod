package com.nodecraft.nodesystem.nodes.math.basic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Absolute Node: Computes the absolute value of a number (|A|).
 */
@NodeInfo(
    id = "math.basic.absolute",
    displayName = "绝对值 (Abs)",
    description = "计算输入数字的绝对值 |Value|",
    category = "math.basic"
)
public class AbsoluteNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_VALUE_ID = "input_value";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_ABSOLUTE_ID = "output_absolute";

    // --- 构造函数 ---
    public AbsoluteNode() {
        super(UUID.randomUUID(), "math.basic.absolute");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_VALUE_ID, "Value", "Input number", NodeDataType.ANY, this));

        // 创建并添加输出端口 (输出类型与输入匹配或提升为 DOUBLE)
        addOutputPort(new BasePort(OUTPUT_ABSOLUTE_ID, "Absolute", "Result |Value|", NodeDataType.DOUBLE, this));
    }
    
    @Override
    public String getDescription() {
        return "Outputs the absolute value of the input number.";
    }
    
    @Override
    public String getDisplayName() {
        return "Absolute (Abs)";
    }

    // --- 核心逻辑 ---
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object val = inputValues.get(INPUT_VALUE_ID);

        // 检查输入是否为数字
        if (val instanceof Number) {
            double number = ((Number) val).doubleValue();
            double result = Math.abs(number);
            outputValues.put(OUTPUT_ABSOLUTE_ID, result);
        } else {
            // 如果输入无效
            outputValues.put(OUTPUT_ABSOLUTE_ID, Double.NaN);
        }
    }

    // --- Getters/Setters (不需要) ---

    // --- (反)序列化 (不需要) ---
} 