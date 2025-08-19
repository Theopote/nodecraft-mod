package com.nodecraft.nodesystem.nodes.animation.interpolation;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Lerp Node: 线性插值节点
 * 在两个值（数字、向量、颜色等）之间进行线性插值
 */
@NodeInfo(
    id = "animation.interpolation.lerp",
    displayName = "Lerp",
    description = "在两个值之间进行线性插值",
    category = "animation.interpolation"
)
public class LerpNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_A_ID = "input_a";
    private static final String INPUT_B_ID = "input_b";
    private static final String INPUT_FACTOR_ID = "input_factor";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_RESULT_ID = "output_result";
    
    // --- 构造函数 ---
    public LerpNode() {
        super(UUID.randomUUID(), "animation.interpolation.lerp");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_A_ID, "A", "起始值（数字、向量或颜色）", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_B_ID, "B", "结束值（数字、向量或颜色）", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_FACTOR_ID, "Factor", "插值因子（0-1）", NodeDataType.FLOAT, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_RESULT_ID, "Result", "插值结果", NodeDataType.ANY, this));
    }
    
    @Override
    public String getDescription() {
        return "在两个值之间进行线性插值";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object valueA = inputValues.get(INPUT_A_ID);
        Object valueB = inputValues.get(INPUT_B_ID);
        Float factor = (Float) inputValues.getOrDefault(INPUT_FACTOR_ID, 0.5f);
        
        // 确保插值因子在0-1范围内
        factor = Math.max(0.0f, Math.min(1.0f, factor));
        
        // 根据输入类型进行不同的插值计算
        Object result = interpolate(valueA, valueB, factor);
        
        // 设置输出值
        if (result != null) {
            outputValues.put(OUTPUT_RESULT_ID, result);
        }
    }
    
    /**
     * 根据输入类型进行插值计算
     */
    private Object interpolate(Object a, Object b, float factor) {
        // 如果任一值为空，则返回另一个
        if (a == null) return b;
        if (b == null) return a;
        
        // 数值类型插值
        if (a instanceof Number && b instanceof Number) {
            float aValue = ((Number) a).floatValue();
            float bValue = ((Number) b).floatValue();
            return aValue + (bValue - aValue) * factor;
        }
        
        // 向量插值（假设Vector3类型）
        if (a instanceof float[] && b instanceof float[] && ((float[]) a).length == 3 && ((float[]) b).length == 3) {
            float[] aVec = (float[]) a;
            float[] bVec = (float[]) b;
            float[] result = new float[3];
            for (int i = 0; i < 3; i++) {
                result[i] = aVec[i] + (bVec[i] - aVec[i]) * factor;
            }
            return result;
        }
        
        // 颜色插值（假设Color类型是float[4]，RGBA）
        if (a instanceof float[] && b instanceof float[] && ((float[]) a).length == 4 && ((float[]) b).length == 4) {
            float[] aColor = (float[]) a;
            float[] bColor = (float[]) b;
            float[] result = new float[4];
            for (int i = 0; i < 4; i++) {
                result[i] = aColor[i] + (bColor[i] - aColor[i]) * factor;
            }
            return result;
        }
        
        // 布尔类型插值（超过0.5则为B值）
        if (a instanceof Boolean && b instanceof Boolean) {
            return factor >= 0.5f ? b : a;
        }
        
        // 不支持的类型组合
        return factor >= 0.5f ? b : a;
    }
} 