package com.nodecraft.nodesystem.nodes.math.basic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Max Node: 计算两个数值中的最大值
 */
@NodeInfo(
    id = "math.basic.max",
    displayName = "最大值 (Max)",
    description = "计算两个数值中的最大值",
    category = "math.basic"
)
public class MaxNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_A_ID = "input_a";
    private static final String INPUT_B_ID = "input_b";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_MAX_ID = "output_max";

    // --- 构造函数 ---
    public MaxNode() {
        super(UUID.randomUUID(), "math.basic.max");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_A_ID, "A", "First value", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_B_ID, "B", "Second value", NodeDataType.DOUBLE, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_MAX_ID, "Max", "The maximum value", NodeDataType.DOUBLE, this));
    }
    
    @Override
    public String getDescription() {
        return "Outputs the maximum of two values";
    }
    
    @Override
    public String getDisplayName() {
        return "Max";
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入
        Object aObj = inputValues.get(INPUT_A_ID);
        Object bObj = inputValues.get(INPUT_B_ID);
        
        // 默认值
        double a = 0.0;
        double b = 0.0;
        
        // 解析输入
        if (aObj instanceof Number) {
            a = ((Number) aObj).doubleValue();
        }
        
        if (bObj instanceof Number) {
            b = ((Number) bObj).doubleValue();
        }
        
        // 计算最大值
        double max = Math.max(a, b);
        
        // 设置输出
        outputValues.put(OUTPUT_MAX_ID, max);
    }
} 