package com.nodecraft.nodesystem.nodes.math.basic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Logarithm Node: Computes the logarithm of A with base B (logB(A)).
 */
@NodeInfo(
    id = "math.basic.logarithm",
    displayName = "对数函数 (Log)",
    description = "计算以指定底数的对数值 logB(A)",
    category = "math.basic"
)
public class LogarithmNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_NUMBER_ID = "input_number"; // A
    private static final String INPUT_BASE_ID = "input_base";     // B

    // --- 输出端口 IDs ---
    private static final String OUTPUT_LOGARITHM_ID = "output_logarithm";

    // --- 构造函数 ---
    public LogarithmNode() {
        super(UUID.randomUUID(), "math.basic.logarithm");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_NUMBER_ID, "Number", "The number (A)", NodeDataType.ANY, this));
        // 默认底数为 e (自然对数)
        addInputPort(new BasePort(INPUT_BASE_ID, "Base", "The base (B), defaults to e", NodeDataType.ANY, this)); 

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_LOGARITHM_ID, "Logarithm", "Result of logB(A)", NodeDataType.DOUBLE, this));
    }
    
    @Override
    public String getDescription() {
        return "Outputs the logarithm of Number with the specified Base.";
    }
    
    @Override
    public String getDisplayName() {
        return "Logarithm (log)";
    }

    // --- 核心逻辑 ---
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object valNumber = inputValues.get(INPUT_NUMBER_ID);
        Object valBase = inputValues.getOrDefault(INPUT_BASE_ID, Math.E); // Default base is e

        // 检查输入是否为数字
        if (valNumber instanceof Number && valBase instanceof Number) {
            double number = ((Number) valNumber).doubleValue();
            double base = ((Number) valBase).doubleValue();

            // 检查无效输入
            // number > 0, base > 0, base != 1
            if (number <= 0 || base <= 0 || Math.abs(base - 1.0) < 1e-10) {
                outputValues.put(OUTPUT_LOGARITHM_ID, Double.NaN);
            } else {
                // 使用换底公式: logB(A) = ln(A) / ln(B)
                double result = Math.log(number) / Math.log(base);
                outputValues.put(OUTPUT_LOGARITHM_ID, result);
            }
        } else {
            // 如果输入无效
            outputValues.put(OUTPUT_LOGARITHM_ID, Double.NaN);
        }
    }

    // --- Getters/Setters (不需要) ---

    // --- (反)序列化 (不需要) ---
} 