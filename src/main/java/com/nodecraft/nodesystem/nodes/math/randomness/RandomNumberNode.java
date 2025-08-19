package com.nodecraft.nodesystem.nodes.math.randomness;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random; // 导入 Random
import java.util.UUID; // 添加导入

/**
 * Random Number Node: Generates random numbers within a specified range.
 */
@NodeInfo(
    id = "math.randomness.random_number",
    displayName = "随机数",
    description = "在指定范围内生成随机数",
    category = "math.randomness"
)
public class RandomNumberNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_COUNT_ID = "input_count";
    private static final String INPUT_MIN_ID = "input_min";
    private static final String INPUT_MAX_ID = "input_max";
    private static final String INPUT_SEED_ID = "input_seed";

    // --- 输出端口 IDs ---
    // 根据 Count 输出单个数字或列表
    private static final String OUTPUT_RANDOM_ID = "output_random"; 

    // --- 构造函数 ---
    public RandomNumberNode() {
        super(UUID.randomUUID(), "math.randomness.random_number"); // 修改构造函数调用
        
        // 创建并添加输入端口 (默认 Count=1, Min=0, Max=1, Seed=None)
        addInputPort(new BasePort(INPUT_COUNT_ID, "Count", "Number of random values to generate", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_MIN_ID, "Min", "Minimum random value (inclusive)", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_MAX_ID, "Max", "Maximum random value (exclusive)", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_SEED_ID, "Seed", "Optional seed for the random generator", NodeDataType.INTEGER, this));

        // 创建并添加输出端口 (类型为 ANY，因为可能是 Double 或 List<Double>)
        addOutputPort(new BasePort(OUTPUT_RANDOM_ID, "Random", "The generated random number(s)", NodeDataType.ANY, this));
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "Generates random numbers with optional Count, Min, Max, and Seed.";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "Random Number";
    }

    // --- 核心逻辑 ---
    
    @Override
    public void processNode(@Nullable ExecutionContext context) { // 修改为 public
        // 获取输入值
        int count = getValueAsInt(inputValues.get(INPUT_COUNT_ID), 1);
        double min = getValueAsDouble(inputValues.get(INPUT_MIN_ID), 0.0);
        double max = getValueAsDouble(inputValues.get(INPUT_MAX_ID), 1.0);
        Object seedVal = inputValues.get(INPUT_SEED_ID);

        // 确保 min <= max
        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }
        
        // 创建 Random 实例
        Random random;
        if (seedVal instanceof Number) {
            random = new Random(((Number) seedVal).longValue());
        } else {
            // 如果没有提供种子，每次执行使用新的 Random 实例以获得不同结果
            // 或者可以从 ExecutionContext 获取一个全局/稳定的种子？
            random = new Random(); 
        }

        // 生成随机数
        if (count <= 0) {
            outputValues.put(OUTPUT_RANDOM_ID, Collections.emptyList());
        } else if (count == 1) {
            double randomNumber = min + random.nextDouble() * (max - min);
            outputValues.put(OUTPUT_RANDOM_ID, randomNumber);
        } else {
            List<Double> randomNumbers = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                double randomNumber = min + random.nextDouble() * (max - min);
                randomNumbers.add(randomNumber);
            }
            outputValues.put(OUTPUT_RANDOM_ID, Collections.unmodifiableList(randomNumbers));
        }
    }
    
    /** Helper method to safely convert an input object to double. */
    private double getValueAsDouble(Object value, double defaultValue) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }
    
    /** Helper method to safely convert an input object to int. */
    private int getValueAsInt(Object value, int defaultValue) {
        if (value instanceof Number) {
            double doubleVal = ((Number) value).doubleValue();
            if (doubleVal >= Integer.MIN_VALUE && doubleVal <= Integer.MAX_VALUE) {
                 return (int) Math.round(doubleVal);
            }
        }
        return defaultValue;
    }

    // --- Getters/Setters (不需要) ---

    // --- (反)序列化 (不需要) ---
} 