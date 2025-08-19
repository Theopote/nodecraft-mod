package com.nodecraft.nodesystem.nodes.math.basic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.api.NodeInfo;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Subtraction Node: Subtracts B from A (A - B).
 */
@NodeInfo(
    id = "math.basic.subtraction",
    displayName = "减法 (-)",
    description = "执行减法运算，计算A - B的结果",
    category = "math.basic"
)
public class SubtractionNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_A_ID = "input_a"; // Minuend
    private static final String INPUT_B_ID = "input_b"; // Subtrahend

    // --- 输出端口 IDs ---
    private static final String OUTPUT_DIFFERENCE_ID = "output_difference";

    // --- 构造函数 ---
    public SubtractionNode() {
        super(UUID.randomUUID(), "math.basic.subtraction");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_A_ID, "A", "Minuend", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_B_ID, "B", "Subtrahend", NodeDataType.ANY, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_DIFFERENCE_ID, "Difference", "Result of A - B", NodeDataType.DOUBLE, this));
    }
    
    @Override
    public String getDescription() {
        return "Outputs the result of A - B.";
    }
    
    @Override
    public String getDisplayName() {
        return "Subtraction (-)";
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
            double difference = a - b;
            
            // 设置输出值
            outputValues.put(OUTPUT_DIFFERENCE_ID, difference);
        } else {
            // 如果输入无效
            outputValues.put(OUTPUT_DIFFERENCE_ID, 0.0); // 或者 null
        }
    }

    // --- Getters/Setters (不需要) ---

    // --- (反)序列化 (不需要) ---
} 