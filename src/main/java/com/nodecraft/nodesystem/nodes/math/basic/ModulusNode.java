package com.nodecraft.nodesystem.nodes.math.basic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Modulus Node: Computes the remainder of A divided by B (A % B).
 */
@NodeInfo(
    id = "math.basic.modulus",
    displayName = "取模运算 (%)",
    description = "计算A除以B的余数 (A % B)",
    category = "math.basic"
)
public class ModulusNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_A_ID = "input_a"; // Dividend
    private static final String INPUT_B_ID = "input_b"; // Divisor

    // --- 输出端口 IDs ---
    private static final String OUTPUT_REMAINDER_ID = "output_remainder";

    // --- 构造函数 ---
    public ModulusNode() {
        super(UUID.randomUUID(), "math.basic.modulus");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_A_ID, "A", "Dividend", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_B_ID, "B", "Divisor", NodeDataType.ANY, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_REMAINDER_ID, "Remainder", "Result of A % B", NodeDataType.DOUBLE, this));
    }
    
    @Override
    public String getDescription() {
        return "Outputs the remainder of A / B.";
    }
    
    @Override
    public String getDisplayName() {
        return "Modulus (%)";
    }

    // --- 核心逻辑 ---
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object valA = inputValues.get(INPUT_A_ID);
        Object valB = inputValues.get(INPUT_B_ID);

        // 检查输入是否为数字
        if (valA instanceof Number && valB instanceof Number) {
            double a = ((Number) valA).doubleValue();
            double b = ((Number) valB).doubleValue();

            // 检查除零
            if (Math.abs(b) < 1e-10) {
                // 除零错误处理
                outputValues.put(OUTPUT_REMAINDER_ID, Double.NaN);
            } else {
                double remainder = a % b;
                outputValues.put(OUTPUT_REMAINDER_ID, remainder);
            }
        } else {
            // 如果输入无效
            outputValues.put(OUTPUT_REMAINDER_ID, Double.NaN);
        }
    }

    // --- Getters/Setters (不需要) ---

    // --- (反)序列化 (不需要) ---
} 