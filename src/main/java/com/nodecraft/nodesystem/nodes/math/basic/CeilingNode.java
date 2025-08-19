package com.nodecraft.nodesystem.nodes.math.basic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Ceiling Node: 对数值进行向上取整（取不小于x的最小整数）
 */
@NodeInfo(
    id = "math.basic.ceiling",
    displayName = "向上取整 (Ceiling)",
    description = "对数值进行向上取整（取不小于x的最小整数）",
    category = "math.basic"
)
public class CeilingNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_VALUE_ID = "input_value";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_CEILING_ID = "output_ceiling";

    // --- 构造函数 ---
    public CeilingNode() {
        super(UUID.randomUUID(), "math.basic.ceiling");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_VALUE_ID, "Value", "Value to ceiling", NodeDataType.DOUBLE, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_CEILING_ID, "Ceiling", "The ceiling value", NodeDataType.DOUBLE, this));
    }
    
    @Override
    public String getDescription() {
        return "Rounds a value up to the nearest integer";
    }
    
    @Override
    public String getDisplayName() {
        return "Ceiling";
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
        
        // 向上取整
        double result = Math.ceil(value);
        
        // 设置输出
        outputValues.put(OUTPUT_CEILING_ID, result);
    }

    // --- Getters/Setters (不需要) ---

    // --- (反)序列化 (不需要) ---
} 