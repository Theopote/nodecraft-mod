package com.nodecraft.nodesystem.nodes.math.basic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Floor Node: 对数值进行向下取整（取不大于x的最大整数）
 */
@NodeInfo(
    id = "math.basic.floor",
    displayName = "向下取整 (Floor)",
    description = "对数值进行向下取整（取不大于x的最大整数）",
    category = "math.basic"
)
public class FloorNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_VALUE_ID = "input_value";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_FLOORED_ID = "output_floored";

    // --- 构造函数 ---
    public FloorNode() {
        super(UUID.randomUUID(), "math.basic.floor");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_VALUE_ID, "Value", "Value to floor", NodeDataType.DOUBLE, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_FLOORED_ID, "Floored", "The floored value", NodeDataType.DOUBLE, this));
    }
    
    @Override
    public String getDescription() {
        return "Rounds a value down to the nearest integer";
    }
    
    @Override
    public String getDisplayName() {
        return "Floor";
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
        
        // 向下取整
        double result = Math.floor(value);
        
        // 设置输出
        outputValues.put(OUTPUT_FLOORED_ID, result);
    }

    // --- Getters/Setters (不需要) ---

    // --- (反)序列化 (不需要) ---
} 