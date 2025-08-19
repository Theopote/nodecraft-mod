package com.nodecraft.nodesystem.nodes.math.logic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * OR Node: 执行逻辑或操作 (A || B)
 */
@NodeInfo(
    id = "math.logic.or",
    displayName = "逻辑或 (OR)",
    description = "执行逻辑或操作，当A或B至少有一个为true时输出true，否则输出false",
    category = "math.logic"
)
public class OrNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_A_ID = "input_a";
    private static final String INPUT_B_ID = "input_b";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_RESULT_ID = "output_result";

    // --- 构造函数 ---
    public OrNode() {
        super(UUID.randomUUID(), "math.logic.or");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_A_ID, "A", "输入值A", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_B_ID, "B", "输入值B", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_RESULT_ID, "Result", "结果 (A || B)", NodeDataType.BOOLEAN, this));
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "执行逻辑或操作，当A或B至少有一个为true时输出true，否则输出false";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "OR";
    }

    // --- 核心逻辑 ---
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object valA = inputValues.get(INPUT_A_ID);
        Object valB = inputValues.get(INPUT_B_ID);

        // 默认结果为false
        boolean result = false;

        // 检查输入并执行逻辑或操作
        if (valA instanceof Boolean && valB instanceof Boolean) {
            result = (Boolean) valA || (Boolean) valB;
        } else {
            // 尝试转换非布尔类型输入
            try {
                boolean boolA = valA != null && Boolean.parseBoolean(valA.toString());
                boolean boolB = valB != null && Boolean.parseBoolean(valB.toString());
                result = boolA || boolB;
            } catch (Exception e) {
                // 转换失败，保持默认的false结果
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_RESULT_ID, result);
    }
}