package com.nodecraft.nodesystem.nodes.math.basic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Division Node: Divides A by B (A / B).
 */
@NodeInfo(
    id = "math.basic.division",
    displayName = "除法 (/)",
    description = "执行除法运算，计算A / B的结果",
    category = "math.basic"
)
public class DivisionNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_A_ID = "input_a"; // Dividend
    private static final String INPUT_B_ID = "input_b"; // Divisor

    // --- 输出端口 IDs ---
    private static final String OUTPUT_QUOTIENT_ID = "output_quotient";

    // --- 构造函数 ---
    public DivisionNode() {
        super(UUID.randomUUID(), "math.basic.division");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_A_ID, "A", "Dividend", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_B_ID, "B", "Divisor", NodeDataType.ANY, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_QUOTIENT_ID, "Quotient", "Result of A / B", NodeDataType.DOUBLE, this));
    }
    
    @Override
    public String getDescription() {
        return "Outputs the result of A / B.";
    }
    
    @Override
    public String getDisplayName() {
        return "Division (/)";
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
            if (Math.abs(b) < 1e-10) { // 使用一个小的阈值比较浮点数是否接近零
                // 除零错误处理：可以输出 NaN, Infinity, 0, 或 null
                outputValues.put(OUTPUT_QUOTIENT_ID, Double.NaN); // 输出 NaN 表示无效结果
            } else {
                double quotient = a / b;
                outputValues.put(OUTPUT_QUOTIENT_ID, quotient);
            }
        } else {
            // 如果输入无效
            outputValues.put(OUTPUT_QUOTIENT_ID, Double.NaN); // 输出 NaN
        }
    }

    // --- Getters/Setters (不需要) ---

    // --- (反)序列化 (不需要) ---
} 