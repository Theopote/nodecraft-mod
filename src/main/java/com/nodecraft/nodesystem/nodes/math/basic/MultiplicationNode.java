package com.nodecraft.nodesystem.nodes.math.basic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Multiplication Node: Multiplies two numbers (A * B).
 */
@NodeInfo(
    id = "math.basic.multiplication",
    displayName = "乘法 (*)",
    description = "执行乘法运算，计算A * B的结果",
    category = "math.basic"
)
public class MultiplicationNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_A_ID = "input_a";
    private static final String INPUT_B_ID = "input_b";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_PRODUCT_ID = "output_product";

    // --- 构造函数 ---
    public MultiplicationNode() {
        super(UUID.randomUUID(), "math.basic.multiplication");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_A_ID, "A", "Factor A", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_B_ID, "B", "Factor B", NodeDataType.ANY, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_PRODUCT_ID, "Product", "Result of A * B", NodeDataType.DOUBLE, this));
    }
    
    @Override
    public String getDescription() {
        return "Outputs the product of A and B.";
    }
    
    @Override
    public String getDisplayName() {
        return "Multiplication (*)";
    }

    // --- 核心逻辑 ---
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object valA = inputValues.get(INPUT_A_ID);
        Object valB = inputValues.get(INPUT_B_ID);

        // 检查输入是否为数字
        if (valA instanceof Number && valB instanceof Number) {
            // 将输入转换为 double 进行计算
            double a = ((Number) valA).doubleValue();
            double b = ((Number) valB).doubleValue();
            double product = a * b;
            
            // 设置输出值
            outputValues.put(OUTPUT_PRODUCT_ID, product);
        } else {
            // 如果输入无效
            outputValues.put(OUTPUT_PRODUCT_ID, 0.0); // 或者 null
        }
    }

    // --- Getters/Setters (不需要) ---

    // --- (反)序列化 (不需要) ---
} 