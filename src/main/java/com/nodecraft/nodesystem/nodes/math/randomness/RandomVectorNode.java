package com.nodecraft.nodesystem.nodes.math.randomness;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random; // 导入 Random
import java.util.UUID; // 添加导入

/**
 * Random Vector Node: Generates random vectors within a specified bounding box.
 */
@NodeInfo(
    id = "math.randomness.random_vector",
    displayName = "随机向量",
    description = "在指定边界框内生成随机向量",
    category = "math.randomness"
)
public class RandomVectorNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_COUNT_ID = "input_count";
    private static final String INPUT_MIN_CORNER_ID = "input_min_corner";
    private static final String INPUT_MAX_CORNER_ID = "input_max_corner";
    private static final String INPUT_SEED_ID = "input_seed";

    // --- 输出端口 IDs ---
    // 根据 Count 输出单个向量或列表
    private static final String OUTPUT_RANDOM_ID = "output_random_vector"; 

    // --- 构造函数 ---
    public RandomVectorNode() {
        super(UUID.randomUUID(), "math.randomness.random_vector"); // 修改构造函数调用
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_COUNT_ID, "Count", "Number of random vectors", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_MIN_CORNER_ID, "Min Corner", "Minimum corner of the bounding box", NodeDataType.VECTOR, this));
        addInputPort(new BasePort(INPUT_MAX_CORNER_ID, "Max Corner", "Maximum corner of the bounding box", NodeDataType.VECTOR, this));
        addInputPort(new BasePort(INPUT_SEED_ID, "Seed", "Optional random seed", NodeDataType.INTEGER, this));

        // 创建并添加输出端口 (类型为 ANY，因为可能是 Vec3d 或 List<Vec3d>)
        addOutputPort(new BasePort(OUTPUT_RANDOM_ID, "Random", "The generated random vector(s)", NodeDataType.ANY, this));
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "Generates random vectors within a bounding box.";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "Random Vector";
    }

    // --- 核心逻辑 ---
    
    @Override
    public void processNode(@Nullable ExecutionContext context) { // 修改为 public
        // 获取输入值
        int count = getValueAsInt(inputValues.get(INPUT_COUNT_ID), 1);
        Vec3d minCorner = getValueAsVec3d(inputValues.get(INPUT_MIN_CORNER_ID), Vec3d.ZERO);
        Vec3d maxCorner = getValueAsVec3d(inputValues.get(INPUT_MAX_CORNER_ID), new Vec3d(1, 1, 1));
        Object seedVal = inputValues.get(INPUT_SEED_ID);

        // 确保 min <= max for each component
        double minX = Math.min(minCorner.x, maxCorner.x);
        double minY = Math.min(minCorner.y, maxCorner.y);
        double minZ = Math.min(minCorner.z, maxCorner.z);
        double maxX = Math.max(minCorner.x, maxCorner.x);
        double maxY = Math.max(minCorner.y, maxCorner.y);
        double maxZ = Math.max(minCorner.z, maxCorner.z);
        
        // 创建 Random 实例
        Random random;
        if (seedVal instanceof Number) {
            random = new Random(((Number) seedVal).longValue());
        } else {
            random = new Random(); 
        }

        // 生成随机向量
        if (count <= 0) {
            outputValues.put(OUTPUT_RANDOM_ID, Collections.emptyList());
        } else if (count == 1) {
            double randomX = minX + random.nextDouble() * (maxX - minX);
            double randomY = minY + random.nextDouble() * (maxY - minY);
            double randomZ = minZ + random.nextDouble() * (maxZ - minZ);
            outputValues.put(OUTPUT_RANDOM_ID, new Vec3d(randomX, randomY, randomZ));
        } else {
            List<Vec3d> randomVectors = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                double randomX = minX + random.nextDouble() * (maxX - minX);
                double randomY = minY + random.nextDouble() * (maxY - minY);
                double randomZ = minZ + random.nextDouble() * (maxZ - minZ);
                randomVectors.add(new Vec3d(randomX, randomY, randomZ));
            }
            outputValues.put(OUTPUT_RANDOM_ID, Collections.unmodifiableList(randomVectors));
        }
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
    
    /** Helper method to safely convert an input object to Vec3d. */
    private Vec3d getValueAsVec3d(Object value, Vec3d defaultValue) {
        if (value instanceof Vec3d) {
            return (Vec3d) value;
        }
        return defaultValue;
    }

    // --- Getters/Setters (不需要) ---

    // --- (反)序列化 (不需要) ---
} 