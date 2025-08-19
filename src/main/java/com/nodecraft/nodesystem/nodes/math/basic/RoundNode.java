package com.nodecraft.nodesystem.nodes.math.basic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Round Node: 将数值四舍五入到最接近的整数
 */
@NodeInfo(
    id = "math.basic.round",
    displayName = "四舍五入 (Round)",
    description = "将数值四舍五入到最接近的整数",
    category = "math.basic"
)
public class RoundNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_VALUE_ID = "input_value";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_ROUNDED_ID = "output_rounded";

    // --- 构造函数 ---
    public RoundNode() {
        super(UUID.randomUUID(), "math.basic.round");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_VALUE_ID, "Value", "Value to round", NodeDataType.DOUBLE, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_ROUNDED_ID, "Rounded", "The rounded value", NodeDataType.DOUBLE, this));
    }

    @Override
    public String getDescription() {
        return "Rounds a value to the nearest integer";
    }
    
    @Override
    public String getDisplayName() {
        return "Round";
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入
        Object valueObj = inputValues.get(INPUT_VALUE_ID);
        
        // 默认值
        double value = 0.0;
        
        // 解析输入
        if (valueObj instanceof Number) {
            value = ((Number) valueObj).doubleValue();
        }
        
        // 四舍五入到最接近的整数
        double result = Math.round(value);
        
        // 设置输出
        outputValues.put(OUTPUT_ROUNDED_ID, result);
    }

    // --- Getters/Setters (不需要) ---

    // --- (反)序列化 (不需要) ---
} 