package com.nodecraft.nodesystem.nodes.math.logic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * GreaterThanOrEqual Node: 比较第一个值是否大于等于第二个值 (A >= B)
 */
@NodeInfo(
    id = "math.logic.greater_than_or_equal",
    displayName = "大于等于",
    description = "比较第一个值是否大于等于第二个值 (A >= B)",
    category = "math.logic"
)
public class GreaterThanOrEqualNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_A_ID = "input_a";
    private static final String INPUT_B_ID = "input_b";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_RESULT_ID = "output_result";

    // --- 构造函数 ---
    public GreaterThanOrEqualNode() {
        super(UUID.randomUUID(), "logic.greater_than_or_equal");
        
        // 创建并添加输入端口 (允许任何可比较的类型，但主要用于数字)
        addInputPort(new BasePort(INPUT_A_ID, "A", "输入值A", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_B_ID, "B", "输入值B", NodeDataType.ANY, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_RESULT_ID, "Result", "比较结果 (A >= B)", NodeDataType.BOOLEAN, this));
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "比较第一个值是否大于等于第二个值";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "Greater Than or Equal (>=)";
    }

    // --- 核心逻辑 ---
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object valA = inputValues.get(INPUT_A_ID);
        Object valB = inputValues.get(INPUT_B_ID);

        // 默认结果为false
        boolean result = false;

        // 处理数字比较
        if (valA instanceof Number && valB instanceof Number) {
            double numA = ((Number) valA).doubleValue();
            double numB = ((Number) valB).doubleValue();
            result = numA >= numB;
        } 
        // 处理字符串比较 (字典序)
        else if (valA instanceof String && valB instanceof String) {
            int comparison = ((String) valA).compareTo((String) valB);
            result = comparison >= 0;
        }
        // 处理字符与数字的比较 (尝试转换)
        else if (valA instanceof String && valB instanceof Number) {
            try {
                double numA = Double.parseDouble((String) valA);
                double numB = ((Number) valB).doubleValue();
                result = numA >= numB;
            } catch (NumberFormatException e) {
                // 如果字符串不能转换为数字，结果为false
            }
        }
        else if (valA instanceof Number && valB instanceof String) {
            try {
                double numA = ((Number) valA).doubleValue();
                double numB = Double.parseDouble((String) valB);
                result = numA >= numB;
            } catch (NumberFormatException e) {
                // 如果字符串不能转换为数字，结果为false
            }
        }
        // 对于不可比较的类型，保持默认的false结果
        
        // 设置输出值
        outputValues.put(OUTPUT_RESULT_ID, result);
    }
}