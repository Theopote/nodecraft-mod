package com.nodecraft.nodesystem.nodes.math.logic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;
import java.util.UUID;

/**
 * Equals Node: 比较两个值是否相等 (A == B)
 */
@NodeInfo(
    id = "math.logic.equals",
    displayName = "相等比较 (==)",
    description = "比较两个输入值是否相等",
    category = "math.logic"
)
public class EqualsNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_A_ID = "input_a";
    private static final String INPUT_B_ID = "input_b";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_RESULT_ID = "output_result";

    // --- 构造函数 ---
    public EqualsNode() {
        super(UUID.randomUUID(), "math.logic.equals");
        
        // 创建并添加输入端口 (允许任意类型)
        addInputPort(new BasePort(INPUT_A_ID, "A", "输入值A", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_B_ID, "B", "输入值B", NodeDataType.ANY, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_RESULT_ID, "Result", "比较结果 (A == B)", NodeDataType.BOOLEAN, this));
    }
    
    @Override
    public String getDescription() {
        return "比较两个输入值是否相等";
    }
    
    @Override
    public String getDisplayName() {
        return "Equals (==)";
    }

    // --- 核心逻辑 ---
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object valA = inputValues.get(INPUT_A_ID);
        Object valB = inputValues.get(INPUT_B_ID);

        // 计算结果
        boolean result = false;
        
        // 处理null情况
        if (valA == null && valB == null) {
            result = true;
        } else if (valA == null || valB == null) {
            result = false;
        }
        // 处理数值比较（处理不同数字类型的比较）
        else if (valA instanceof Number && valB instanceof Number) {
            double numA = ((Number) valA).doubleValue();
            double numB = ((Number) valB).doubleValue();
            // 使用一个很小的误差范围进行浮点数比较
            result = Math.abs(numA - numB) < 1e-10;
        }
        // 处理字符串比较
        else if (valA instanceof String || valB instanceof String) {
            result = Objects.toString(valA, "").equals(Objects.toString(valB, ""));
        }
        // 其他类型使用equals方法
        else {
            result = Objects.equals(valA, valB);
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_RESULT_ID, result);
    }
} 