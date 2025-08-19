package com.nodecraft.nodesystem.nodes.math.logic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * NOT Node: 执行逻辑非操作 (!A)
 */
@NodeInfo(
    id = "math.logic.not",
    displayName = "逻辑非 (NOT)",
    description = "执行逻辑非操作，对输入值取反",
    category = "math.logic"
)
public class NotNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_VALUE_ID = "input_value";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_RESULT_ID = "output_result";

    // --- 构造函数 ---
    public NotNode() {
        super(UUID.randomUUID(), "math.logic.not");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_VALUE_ID, "Value", "输入值", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_RESULT_ID, "Result", "结果 (!Value)", NodeDataType.BOOLEAN, this));
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "执行逻辑非操作，对输入值取反";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "NOT";
    }

    // --- 核心逻辑 ---
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object val = inputValues.get(INPUT_VALUE_ID);

        // 默认结果为true (输入为null或false时)
        boolean result = true;

        // 检查输入并执行逻辑非操作
        if (val instanceof Boolean) {
            result = !(Boolean) val;
        } else {
            // 尝试转换非布尔类型输入
            try {
                if (val != null) {
                    boolean boolVal = Boolean.parseBoolean(val.toString());
                    result = !boolVal;
                }
            } catch (Exception e) {
                // 转换失败，保持默认的true结果
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_RESULT_ID, result);
    }
} 