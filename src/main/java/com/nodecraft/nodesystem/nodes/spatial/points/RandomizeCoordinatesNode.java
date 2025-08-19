package com.nodecraft.nodesystem.nodes.spatial.points;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.Random;
import java.util.UUID;

/**
 * Randomize Coordinates 节点: 随机化坐标列表
 */
@NodeInfo(
    id = "spatial.points.randomize_coordinates",
    displayName = "坐标随机化",
    description = "对坐标列表中的每个坐标添加随机偏移",
    category = "spatial.points"
)
public class RandomizeCoordinatesNode extends BaseNode {

    // --- 节点属性 ---
    private boolean useUniformRange = true; // 默认使用统一范围
    private boolean useSeed = false; // 默认不使用种子
    private long seed = 0; // 默认种子值

    // --- 输入端口 IDs ---
    private static final String INPUT_COORDINATES_ID = "input_coordinates";
    private static final String INPUT_MIN_RANGE_ID = "input_min_range";
    private static final String INPUT_MAX_RANGE_ID = "input_max_range";
    private static final String INPUT_RANGE_VECTOR_ID = "input_range_vector";
    private static final String INPUT_SEED_ID = "input_seed";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_COORDINATES_ID = "output_coordinates";

    // --- 构造函数 ---
    public RandomizeCoordinatesNode() {
        super(UUID.randomUUID(), "spatial.points.randomize_coordinates");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_COORDINATES_ID, "Coordinates", 
                "The coordinates to randomize", NodeDataType.BLOCK_LIST, this));
        addInputPort(new BasePort(INPUT_MIN_RANGE_ID, "Min Range", 
                "Minimum random offset (uniform)", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_MAX_RANGE_ID, "Max Range", 
                "Maximum random offset (uniform)", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_RANGE_VECTOR_ID, "Range Vector", 
                "Maximum random offset vector (XYZ)", NodeDataType.VECTOR, this));
        addInputPort(new BasePort(INPUT_SEED_ID, "Seed", 
                "Random seed (optional)", NodeDataType.INTEGER, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_COORDINATES_ID, "Coordinates", 
                "Randomized coordinates", NodeDataType.BLOCK_LIST, this));
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "Applies random offset to a list of coordinates within a given range";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "Randomize Coordinates";
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object coordinatesObj = inputValues.get(INPUT_COORDINATES_ID);
        Object minRangeObj = inputValues.get(INPUT_MIN_RANGE_ID);
        Object maxRangeObj = inputValues.get(INPUT_MAX_RANGE_ID);
        Object rangeVectorObj = inputValues.get(INPUT_RANGE_VECTOR_ID);
        Object seedObj = inputValues.get(INPUT_SEED_ID);
        
        // 默认空的坐标列表
        BlockPosList result = new BlockPosList();
        
        // 检查基本输入是否合法
        if (!(coordinatesObj instanceof BlockPosList)) {
            outputValues.put(OUTPUT_COORDINATES_ID, result);
            return;
        }
        
        BlockPosList coordinates = (BlockPosList) coordinatesObj;
        
        // 确定随机范围
        double minRange = 0.0;
        double maxRange = 1.0;
        Vector3d rangeVector = new Vector3d(1, 1, 1);
        
        // 确定随机种子
        long randomSeed = this.seed;
        if (useSeed && seedObj instanceof Number) {
            randomSeed = ((Number) seedObj).longValue();
        } else if (!useSeed && seedObj instanceof Number) {
            // 如果提供了种子但没有启用种子设置，则自动启用
            randomSeed = ((Number) seedObj).longValue();
            useSeed = true;
        }
        
        // 创建随机数生成器
        Random random;
        if (useSeed) {
            random = new Random(randomSeed);
        } else {
            random = new Random();
        }
        
        // 确定使用的范围类型
        if (useUniformRange) {
            // 使用统一范围
            if (minRangeObj instanceof Number) {
                minRange = ((Number) minRangeObj).doubleValue();
            }
            if (maxRangeObj instanceof Number) {
                maxRange = ((Number) maxRangeObj).doubleValue();
            }
            // 确保最小值<=最大值
            if (minRange > maxRange) {
                double temp = minRange;
                minRange = maxRange;
                maxRange = temp;
            }
        } else if (rangeVectorObj instanceof Vector3d) {
            // 使用向量范围
            rangeVector = (Vector3d) rangeVectorObj;
            // 确保范围向量各分量为正值
            rangeVector.x = Math.abs(rangeVector.x);
            rangeVector.y = Math.abs(rangeVector.y);
            rangeVector.z = Math.abs(rangeVector.z);
        }
        
        // 应用随机偏移到每个坐标
        for (BlockPos pos : coordinates) {
            // 计算随机偏移
            int offsetX, offsetY, offsetZ;
            
            if (useUniformRange) {
                // 在最小范围和最大范围之间生成随机值
                double range = maxRange - minRange;
                offsetX = (int) Math.round(minRange + random.nextDouble() * range);
                if (random.nextBoolean()) offsetX = -offsetX; // 50%概率为负值
                
                offsetY = (int) Math.round(minRange + random.nextDouble() * range);
                if (random.nextBoolean()) offsetY = -offsetY; // 50%概率为负值
                
                offsetZ = (int) Math.round(minRange + random.nextDouble() * range);
                if (random.nextBoolean()) offsetZ = -offsetZ; // 50%概率为负值
            } else {
                // 使用向量范围生成随机值
                offsetX = (int) Math.round((random.nextDouble() * 2 - 1) * rangeVector.x);
                offsetY = (int) Math.round((random.nextDouble() * 2 - 1) * rangeVector.y);
                offsetZ = (int) Math.round((random.nextDouble() * 2 - 1) * rangeVector.z);
            }
            
            // 应用偏移
            BlockPos randomizedPos = new BlockPos(
                pos.getX() + offsetX,
                pos.getY() + offsetY,
                pos.getZ() + offsetZ
            );
            
            // 添加到结果列表
            result.add(randomizedPos);
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_COORDINATES_ID, result);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isUseUniformRange() {
        return useUniformRange;
    }
    
    public void setUseUniformRange(boolean useUniformRange) {
        this.useUniformRange = useUniformRange;
        markDirty();
    }
    
    public boolean isUseSeed() {
        return useSeed;
    }
    
    public void setUseSeed(boolean useSeed) {
        this.useSeed = useSeed;
        markDirty();
    }
    
    public long getSeed() {
        return seed;
    }
    
    public void setSeed(long seed) {
        this.seed = seed;
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("useUniformRange", useUniformRange);
        state.put("useSeed", useSeed);
        state.put("seed", seed);
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("useUniformRange")) {
                Object useUniformObj = stateMap.get("useUniformRange");
                if (useUniformObj instanceof Boolean) {
                    setUseUniformRange((Boolean) useUniformObj);
                }
            }
            
            if (stateMap.containsKey("useSeed")) {
                Object useSeedObj = stateMap.get("useSeed");
                if (useSeedObj instanceof Boolean) {
                    setUseSeed((Boolean) useSeedObj);
                }
            }
            
            if (stateMap.containsKey("seed")) {
                Object seedObj = stateMap.get("seed");
                if (seedObj instanceof Number) {
                    setSeed(((Number) seedObj).longValue());
                }
            }
        }
    }
} 