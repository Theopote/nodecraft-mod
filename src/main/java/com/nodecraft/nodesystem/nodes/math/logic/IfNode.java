package com.nodecraft.nodesystem.nodes.math.logic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * If Node: 根据条件选择输出true分支或false分支的值
 */
@NodeInfo(
    id = "math.logic.if",
    displayName = "条件选择",
    description = "根据条件选择输出true分支或false分支的值",
    category = "math.logic"
)
public class IfNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_CONDITION_ID = "input_condition";
    private static final String INPUT_TRUE_VALUE_ID = "input_true_value";
    private static final String INPUT_FALSE_VALUE_ID = "input_false_value";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_RESULT_ID = "output_result";

    // --- 构造函数 ---
    public IfNode() {
        super(UUID.randomUUID(), "logic.if");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_CONDITION_ID, "Condition", "条件 (布尔值)", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_TRUE_VALUE_ID, "True Value", "条件为true时的输出值", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_FALSE_VALUE_ID, "False Value", "条件为false时的输出值", NodeDataType.ANY, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_RESULT_ID, "Result", "根据条件选择的输出值", NodeDataType.ANY, this));
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "根据条件选择输出true分支或false分支的值";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "If / Gate";
    }

    // --- 核心逻辑 ---
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取条件输入值
        Object conditionValue = inputValues.get(INPUT_CONDITION_ID);
        
        // 默认条件为false
        boolean condition = false;
        
        // 判断条件是否为true
        if (conditionValue instanceof Boolean) {
            condition = (Boolean) conditionValue;
        } else if (conditionValue != null) {
            // 尝试转换非布尔类型
            try {
                condition = Boolean.parseBoolean(conditionValue.toString());
            } catch (Exception e) {
                // 转换失败，保持默认的false
            }
        }
        
        // 根据条件选择输出值
        Object result;
        if (condition) {
            result = inputValues.get(INPUT_TRUE_VALUE_ID);
        } else {
            result = inputValues.get(INPUT_FALSE_VALUE_ID);
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_RESULT_ID, result);
    }
}