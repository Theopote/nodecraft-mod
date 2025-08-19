package com.nodecraft.nodesystem.nodes.math.basic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;
import com.nodecraft.nodesystem.api.INode;
import com.nodecraft.nodesystem.api.NodeInfo;

/**
 * Addition Node: Adds two numbers (A + B).
 */
@NodeInfo(
    id = "math.basic.addition",
    displayName = "Addition (+)",
    description = "两个数的加法运算",
    category = "math.basic"
)
public class AdditionNode extends BaseNode implements INode {

    // --- 输入端口 IDs ---
    private static final String INPUT_A_ID = "input_a";
    private static final String INPUT_B_ID = "input_b";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_SUM_ID = "output_sum";

    // --- 构造函数 ---
    public AdditionNode() {
        super(UUID.randomUUID(), "math.basic.addition");
        
        // 创建并添加输入端口 (允许 ANY 数字类型)
        addInputPort(new BasePort(INPUT_A_ID, "A", "First number", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_B_ID, "B", "Second number", NodeDataType.ANY, this));

        // 创建并添加输出端口 (结果通常为 DOUBLE)
        addOutputPort(new BasePort(OUTPUT_SUM_ID, "Sum", "Result of A + B", NodeDataType.DOUBLE, this));
    }
    
    @Override
    public String getDescription() {
        return "Outputs the sum of A and B.";
    }
    
    @Override
    public String getDisplayName() {
        return "Addition (+)";
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
            double sum = a + b;
            
            // 设置输出值
            outputValues.put(OUTPUT_SUM_ID, sum);
        } else {
            // 如果输入无效，可以设置默认值、错误状态或 null
            outputValues.put(OUTPUT_SUM_ID, 0.0); // 或者 null
        }
    }

    // --- Getters/Setters (不需要) ---

    // --- (反)序列化 (不需要) ---
} 