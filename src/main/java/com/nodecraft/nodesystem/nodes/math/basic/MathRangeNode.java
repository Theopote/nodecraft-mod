package com.nodecraft.nodesystem.nodes.math.basic;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Range Node: Generates a sequence of numbers within a domain [Start, End] using a Step value.
 */
@NodeInfo(
    id = "math.basic.range",
    displayName = "数值范围",
    description = "在指定范围内生成数字序列",
    category = "math.basic"
)
public class MathRangeNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_START_ID = "input_start";
    private static final String INPUT_END_ID = "input_end";
    private static final String INPUT_STEP_ID = "input_step";
    private String description; // 存储节点描述

    // --- 输出端口 IDs ---
    private static final String OUTPUT_NUMBERS_ID = "output_numbers";

    // --- 构造函数 ---
    public MathRangeNode() {
        // 调用父类构造函数，使用UUID.randomUUID()生成新的ID
        super(UUID.randomUUID(), "math.basic.range");
        
        // 设置节点描述
        this.description = "Generates a sequence of numbers from Start to End with Step.";
        
        // 创建并添加输入端口 (默认 Start=0, End=10, Step=1)
        addInputPort(new BasePort(INPUT_START_ID, "Start", "The starting number of the range", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_END_ID, "End", "The ending number of the range", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_STEP_ID, "Step", "The step size between numbers", NodeDataType.ANY, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_NUMBERS_ID, "Numbers", "The generated list of numbers", NodeDataType.ANY, this)); // Output: List<Double>
    }
    
    /**
     * 实现INode接口的getDescription方法
     * @return 节点描述
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    // --- 核心逻辑 ---
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值，提供默认值
        double start = getValueAsDouble(inputValues.get(INPUT_START_ID), 0.0);
        double end = getValueAsDouble(inputValues.get(INPUT_END_ID), 10.0);
        double step = getValueAsDouble(inputValues.get(INPUT_STEP_ID), 1.0);

        List<Double> numbers = new ArrayList<>();
        
        // 检查无效的 step 值以防止无限循环
        if (Math.abs(step) < 1e-10) {
             // Step is too close to zero. Return empty list or just the start value?
             // Let's return just the start value if start <= end (or start >= end if step was negative initially).
             if (start <= end) { // Assuming default positive direction
                 numbers.add(start);
             }
             outputValues.put(OUTPUT_NUMBERS_ID, numbers);
             return;
        }

        // 根据 step 的符号确定循环方向
        if (step > 0) {
            if (start <= end) {
                 // 使用浮点数比较时增加一点容差，避免精度问题导致最后一个数丢失
                for (double current = start; current <= end + step * 0.001; current += step) {
                    numbers.add(current);
                }
            } // else: start > end with positive step, result is empty list
        } else { // step < 0
            if (start >= end) {
                 // 使用浮点数比较时增加一点容差
                for (double current = start; current >= end + step * 0.001; current += step) {
                    numbers.add(current);
                }
            } // else: start < end with negative step, result is empty list
        }
        
        // 设置输出值 (输出不可变列表或副本)
        outputValues.put(OUTPUT_NUMBERS_ID, Collections.unmodifiableList(numbers));
    }
    
    /**
     * Helper method to safely convert an input object to double.
     */
    private double getValueAsDouble(Object value, double defaultValue) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    // --- Getters/Setters (不需要) ---

    // --- (反)序列化 (不需要) ---
} 