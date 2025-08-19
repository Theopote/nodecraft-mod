package com.nodecraft.nodesystem.nodes.animation.interpolation;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Vector Blend Node: 向量混合节点
 * 在两个向量之间进行线性插值
 */
@NodeInfo(
    id = "animation.interpolation.vector_blend",
    displayName = "Vector Blend",
    description = "在两个向量之间进行插值混合",
    category = "animation.interpolation"
)
public class VectorBlendNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_VECTOR_A_ID = "input_vector_a";
    private static final String INPUT_VECTOR_B_ID = "input_vector_b";
    private static final String INPUT_FACTOR_ID = "input_factor";
    private static final String INPUT_NORMALIZE_ID = "input_normalize";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_BLENDED_VECTOR_ID = "output_blended_vector";
    
    // --- 构造函数 ---
    public VectorBlendNode() {
        super(UUID.randomUUID(), "animation.interpolation.vector_blend");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_VECTOR_A_ID, "Vector A", "起始向量", NodeDataType.VECTOR, this));
        addInputPort(new BasePort(INPUT_VECTOR_B_ID, "Vector B", "结束向量", NodeDataType.VECTOR, this));
        addInputPort(new BasePort(INPUT_FACTOR_ID, "Factor", "混合因子（0-1）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_NORMALIZE_ID, "Normalize", "是否归一化结果（保持向量长度为1）", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_BLENDED_VECTOR_ID, "Blended Vector", "混合后的向量", NodeDataType.VECTOR, this));
    }
    
    @Override
    public String getDescription() {
        return "在两个向量之间进行插值混合";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        float[] vectorA = (float[]) inputValues.getOrDefault(INPUT_VECTOR_A_ID, new float[]{1.0f, 0.0f, 0.0f});
        float[] vectorB = (float[]) inputValues.getOrDefault(INPUT_VECTOR_B_ID, new float[]{0.0f, 1.0f, 0.0f});
        Float factor = (Float) inputValues.getOrDefault(INPUT_FACTOR_ID, 0.5f);
        Boolean normalize = (Boolean) inputValues.getOrDefault(INPUT_NORMALIZE_ID, false);
        
        // 确保插值因子在0-1范围内
        factor = Math.max(0.0f, Math.min(1.0f, factor));
        
        // 确保向量有效
        if (vectorA == null || vectorA.length < 3) {
            vectorA = new float[]{1.0f, 0.0f, 0.0f}; // 默认x轴单位向量
        }
        if (vectorB == null || vectorB.length < 3) {
            vectorB = new float[]{0.0f, 1.0f, 0.0f}; // 默认y轴单位向量
        }
        
        // 混合向量
        float[] blendedVector = blendVectors(vectorA, vectorB, factor, normalize);
        
        // 设置输出值
        outputValues.put(OUTPUT_BLENDED_VECTOR_ID, blendedVector);
    }
    
    /**
     * 混合两个向量
     */
    private float[] blendVectors(float[] vectorA, float[] vectorB, float factor, boolean normalize) {
        // 假设向量是3D向量（x, y, z）
        float[] result = new float[3];
        
        // 线性插值
        for (int i = 0; i < 3; i++) {
            result[i] = vectorA[i] + (vectorB[i] - vectorA[i]) * factor;
        }
        
        // 如果需要归一化
        if (normalize) {
            normalizeVector(result);
        }
        
        return result;
    }
    
    /**
     * 归一化向量（使其长度为1）
     */
    private void normalizeVector(float[] vector) {
        // 计算向量长度
        float length = (float) Math.sqrt(
            vector[0] * vector[0] +
            vector[1] * vector[1] +
            vector[2] * vector[2]
        );
        
        // 防止除以零
        if (length > 0.0001f) {
            // 归一化
            vector[0] /= length;
            vector[1] /= length;
            vector[2] /= length;
        }
    }
} 