package com.nodecraft.nodesystem.nodes.math.basic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Power Node: Computes A raised to the power of B (A ^ B).
 */
@NodeInfo(
    id = "math.basic.power",
    displayName = "幂运算 (^)",
    description = "计算底数的指数次幂，输出A的B次方",
    category = "math.basic"
)
public class PowerNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_BASE_ID = "input_base"; // A
    private static final String INPUT_EXPONENT_ID = "input_exponent"; // B

    // --- 输出端口 IDs ---
    private static final String OUTPUT_POWER_ID = "output_power";

    // --- 构造函数 ---
    public PowerNode() {
        super(UUID.randomUUID(), "math.basic.power");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_BASE_ID, "Base", "The base value (A)", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_EXPONENT_ID, "Exponent", "The exponent value (B)", NodeDataType.ANY, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_POWER_ID, "Power", "Result of A ^ B", NodeDataType.DOUBLE, this));
    }
    
    @Override
    public String getDescription() {
        return "Outputs the result of Base raised to the Exponent.";
    }
    
    @Override
    public String getDisplayName() {
        return "Power (^)";
    }

    // --- 核心逻辑 ---
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object valBase = inputValues.get(INPUT_BASE_ID);
        Object valExponent = inputValues.get(INPUT_EXPONENT_ID);

        // 检查输入是否为数字
        if (valBase instanceof Number && valExponent instanceof Number) {
            double base = ((Number) valBase).doubleValue();
            double exponent = ((Number) valExponent).doubleValue();
            
            double result = Math.pow(base, exponent);
            
            // Math.pow 处理了一些边缘情况 (e.g., 0^0 returns 1), 但可能返回 NaN 或 Infinity
            outputValues.put(OUTPUT_POWER_ID, result);
        } else {
            // 如果输入无效
            outputValues.put(OUTPUT_POWER_ID, Double.NaN);
        }
    }

    // --- Getters/Setters (不需要) ---

    // --- (反)序列化 (不需要) ---
} 