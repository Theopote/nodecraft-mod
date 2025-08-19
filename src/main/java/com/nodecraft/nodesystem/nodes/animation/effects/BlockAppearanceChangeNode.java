package com.nodecraft.nodesystem.nodes.animation.effects;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Block Appearance Change Node: 方块外观变化节点
 * 在两种不同外观之间实现方块的平滑过渡
 */
@NodeInfo(
    id = "animation.effects.block_appearance_change",
    displayName = "Block Appearance Change",
    description = "在两种不同外观之间实现方块的平滑过渡",
    category = "animation.effects"
)
public class BlockAppearanceChangeNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_GEOMETRY_ID = "input_geometry";
    private static final String INPUT_START_BLOCK_INFO_ID = "input_start_block_info";
    private static final String INPUT_END_BLOCK_INFO_ID = "input_end_block_info";
    private static final String INPUT_FACTOR_ID = "input_factor";
    private static final String INPUT_TRANSITION_STEPS_ID = "input_transition_steps";
    private static final String INPUT_TRANSITION_MODE_ID = "input_transition_mode";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_ANIMATED_BLOCKS_ID = "output_animated_blocks";
    
    // --- 过渡模式枚举 ---
    public enum TransitionMode {
        ALL_AT_ONCE(0, "All at Once", "所有方块同时变化"),
        SEQUENTIAL(1, "Sequential", "按顺序逐个变化"),
        RANDOM(2, "Random", "随机顺序变化"),
        DISTANCE_BASED(3, "Distance Based", "距离越近越先变化");
        
        private final int id;
        private final String name;
        private final String description;
        
        TransitionMode(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
        
        public static TransitionMode fromId(int id) {
            for (TransitionMode mode : values()) {
                if (mode.id == id) {
                    return mode;
                }
            }
            return ALL_AT_ONCE; // 默认模式
        }
    }
    
    // --- 构造函数 ---
    public BlockAppearanceChangeNode() {
        super(UUID.randomUUID(), "animation.effects.block_appearance_change");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_GEOMETRY_ID, "Geometry", "几何体", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_START_BLOCK_INFO_ID, "Start BlockInfo", "起始方块信息", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_END_BLOCK_INFO_ID, "End BlockInfo", "结束方块信息", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_FACTOR_ID, "Factor", "过渡因子（0-1）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_TRANSITION_STEPS_ID, "Transition Steps", "过渡步骤数", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_TRANSITION_MODE_ID, "Transition Mode", "过渡模式", NodeDataType.INTEGER, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_ANIMATED_BLOCKS_ID, "Animated Blocks", "动画后的方块", NodeDataType.LIST, this));
    }
    
    @Override
    public String getDescription() {
        return "在两种不同外观之间实现方块的平滑过渡";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object geometryObj = inputValues.get(INPUT_GEOMETRY_ID);
        Object startBlockInfo = inputValues.get(INPUT_START_BLOCK_INFO_ID);
        Object endBlockInfo = inputValues.get(INPUT_END_BLOCK_INFO_ID);
        Float factor = (Float) inputValues.getOrDefault(INPUT_FACTOR_ID, 0.0f);
        Integer transitionSteps = (Integer) inputValues.getOrDefault(INPUT_TRANSITION_STEPS_ID, 10);
        Integer transitionModeId = (Integer) inputValues.getOrDefault(INPUT_TRANSITION_MODE_ID, TransitionMode.ALL_AT_ONCE.id);
        
        // 确保过渡因子在0-1范围内
        factor = Math.max(0.0f, Math.min(1.0f, factor));
        
        // 确保过渡步骤至少为2
        transitionSteps = Math.max(2, transitionSteps);
        
        // 获取过渡模式
        TransitionMode transitionMode = TransitionMode.fromId(transitionModeId);
        
        // 处理几何体
        List<Object> animatedBlocks = processGeometry(geometryObj, startBlockInfo, endBlockInfo, factor, transitionSteps, transitionMode);
        
        // 设置输出值
        outputValues.put(OUTPUT_ANIMATED_BLOCKS_ID, animatedBlocks);
    }
    
    /**
     * 处理几何体，应用外观变化
     */
    private List<Object> processGeometry(Object geometryObj, Object startBlockInfo, Object endBlockInfo, 
                                       float factor, int transitionSteps, TransitionMode transitionMode) {
        List<Object> result = new ArrayList<>();
        
        if (geometryObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> geometryList = (List<Object>) geometryObj;
            
            // 如果几何体为空，直接返回空列表
            if (geometryList.isEmpty()) {
                return result;
            }
            
            // 如果没有起始或结束方块信息，直接返回原始几何体
            if (startBlockInfo == null || endBlockInfo == null) {
                return new ArrayList<>(geometryList);
            }
            
            // 预处理几何体（根据过渡模式计算每个方块的单独过渡因子）
            List<Float> blockFactors = calculateBlockFactors(geometryList, factor, transitionMode);
            
            // 处理每个方块
            for (int i = 0; i < geometryList.size(); i++) {
                Object block = geometryList.get(i);
                float blockFactor = blockFactors.get(i);
                
                // 确定当前方块应该显示的外观
                int step = determineTransitionStep(blockFactor, transitionSteps);
                Object blockInfo = interpolateBlockInfo(startBlockInfo, endBlockInfo, step, transitionSteps);
                
                // 创建新的方块对象（保留位置，更改外观）
                Object transformedBlock = createTransformedBlock(block, blockInfo);
                if (transformedBlock != null) {
                    result.add(transformedBlock);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 根据过渡模式计算每个方块的单独过渡因子
     */
    private List<Float> calculateBlockFactors(List<Object> geometry, float globalFactor, TransitionMode mode) {
        int size = geometry.size();
        List<Float> factors = new ArrayList<>(size);
        
        switch (mode) {
            case ALL_AT_ONCE:
                // 所有方块使用相同的全局因子
                for (int i = 0; i < size; i++) {
                    factors.add(globalFactor);
                }
                break;
                
            case SEQUENTIAL:
                // 线性分布的因子
                for (int i = 0; i < size; i++) {
                    float localFactor = globalFactor * size - i;
                    factors.add(Math.max(0.0f, Math.min(1.0f, localFactor)));
                }
                break;
                
            case RANDOM:
                // 使用方块哈希值计算伪随机因子
                for (int i = 0; i < size; i++) {
                    Object block = geometry.get(i);
                    
                                         // 使用方块坐标计算随机种子
                     float[] pos = getCoordinateAsFloatArray(block);
                     int seed;
                     if (pos != null) {
                         int x = (int)(pos[0] * 73856093);
                         int y = (int)(pos[1] * 19349663);
                         int z = (int)(pos[2] * 83492791);
                         seed = x ^ y ^ z;
                     } else {
                         seed = block.hashCode();
                     }
                    
                    // 基于种子计算0-1之间的值
                    float randomOffset = (seed % 1000) / 1000.0f;
                    
                    // 应用全局因子，考虑随机偏移
                    float localFactor = globalFactor - randomOffset * 0.8f; // 保留20%全局控制
                    factors.add(Math.max(0.0f, Math.min(1.0f, localFactor * 1.25f))); // 放大效果
                }
                break;
                
            case DISTANCE_BASED:
                // 计算几何体中心点
                float[] center = calculateCenter(geometry);
                
                // 找出最大距离用于归一化
                float maxDistance = 0.1f;
                List<Float> distances = new ArrayList<>(size);
                
                for (Object block : geometry) {
                    float[] pos = getCoordinateAsFloatArray(block);
                    if (pos == null) {
                        distances.add(0.0f);
                        continue;
                    }
                    
                    float distance = calculateDistance(pos, center);
                    distances.add(distance);
                    maxDistance = Math.max(maxDistance, distance);
                }
                
                // 根据距离计算因子
                for (int i = 0; i < size; i++) {
                    float normalizedDistance = distances.get(i) / maxDistance;
                    float localFactor = globalFactor - normalizedDistance * 0.7f; // 保留30%全局控制
                    factors.add(Math.max(0.0f, Math.min(1.0f, localFactor * 1.4f))); // 放大效果
                }
                break;
        }
        
        return factors;
    }
    
    /**
     * 计算几何体的中心点
     */
    private float[] calculateCenter(List<Object> geometry) {
        float[] center = new float[3];
        int count = 0;
        
        for (Object block : geometry) {
            float[] pos = getCoordinateAsFloatArray(block);
            if (pos != null) {
                center[0] += pos[0];
                center[1] += pos[1];
                center[2] += pos[2];
                count++;
            }
        }
        
        if (count > 0) {
            center[0] /= count;
            center[1] /= count;
            center[2] /= count;
        }
        
        return center;
    }
    
    /**
     * 计算两点之间的距离
     */
    private float calculateDistance(float[] p1, float[] p2) {
        float dx = p1[0] - p2[0];
        float dy = p1[1] - p2[1];
        float dz = p1[2] - p2[2];
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    /**
     * 确定当前过渡步骤
     */
    private int determineTransitionStep(float factor, int totalSteps) {
        return Math.min(totalSteps - 1, Math.round(factor * (totalSteps - 1)));
    }
    
    /**
     * 在起始和结束方块信息之间插值
     */
    private Object interpolateBlockInfo(Object startInfo, Object endInfo, int step, int totalSteps) {
        // 如果在起始步骤，直接返回起始信息
        if (step == 0) return startInfo;
        
        // 如果在结束步骤，直接返回结束信息
        if (step >= totalSteps - 1) return endInfo;
        
        // 根据步骤选择合适的中间方块
        // 注意：这里应该根据实际的方块信息结构进行具体实现
        // 下面是一个简单的示例
        
        if (startInfo instanceof Map && endInfo instanceof Map) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> startMap = (Map<String, Object>) startInfo;
                @SuppressWarnings("unchecked")
                Map<String, Object> endMap = (Map<String, Object>) endInfo;
                
                // 创建新对象，从起始信息复制基本属性
                Map<String, Object> resultMap = new HashMap<>(startMap);
                
                // 如果有方块ID/类型字段，进行插值
                if (startMap.containsKey("id") && endMap.containsKey("id") && 
                    startMap.get("id") instanceof String && endMap.get("id") instanceof String) {
                    
                    String startId = (String) startMap.get("id");
                    String endId = (String) endMap.get("id");
                    
                    // 如果ID不同，在过渡中间点时选择结束ID
                    if (!startId.equals(endId) && step > totalSteps / 2) {
                        resultMap.put("id", endId);
                    }
                }
                
                // 如果有属性字段，根据步骤进行渐进变化
                if (startMap.containsKey("properties") && endMap.containsKey("properties") &&
                    startMap.get("properties") instanceof Map && endMap.get("properties") instanceof Map) {
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> startProps = (Map<String, Object>) startMap.get("properties");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> endProps = (Map<String, Object>) endMap.get("properties");
                    
                    // 创建新属性映射
                    Map<String, Object> resultProps = new HashMap<>(startProps);
                    
                    // 对于数值属性，进行线性插值
                    for (String key : endProps.keySet()) {
                        if (startProps.containsKey(key)) {
                            Object startVal = startProps.get(key);
                            Object endVal = endProps.get(key);
                            
                            if (startVal instanceof Number && endVal instanceof Number) {
                                double startNum = ((Number) startVal).doubleValue();
                                double endNum = ((Number) endVal).doubleValue();
                                double interpolated = startNum + (endNum - startNum) * step / (totalSteps - 1);
                                
                                // 根据原始值类型，设置正确类型的值
                                if (startVal instanceof Integer) {
                                    resultProps.put(key, (int) Math.round(interpolated));
                                } else if (startVal instanceof Float) {
                                    resultProps.put(key, (float) interpolated);
                                } else {
                                    resultProps.put(key, interpolated);
                                }
                            }
                            // 非数值属性（如字符串）在超过半程后切换
                            else if (!startVal.equals(endVal) && step > totalSteps / 2) {
                                resultProps.put(key, endVal);
                            }
                        }
                        // 仅在结束属性中存在的键，在超过1/3后添加
                        else if (step > totalSteps / 3) {
                            resultProps.put(key, endProps.get(key));
                        }
                    }
                    
                    resultMap.put("properties", resultProps);
                }
                
                return resultMap;
            } catch (Exception e) {
                // 处理失败，返回基于步骤的默认方块
                return (step < totalSteps / 2) ? startInfo : endInfo;
            }
        }
        
        // 默认返回基于步骤的方块
        return (step < totalSteps / 2) ? startInfo : endInfo;
    }
    
    /**
     * 创建变换后的方块对象
     */
    private Object createTransformedBlock(Object block, Object blockInfo) {
        if (block instanceof int[] && blockInfo != null) {
            // 对于int[]格式的方块（仅坐标），创建一个带有位置和信息的新对象
            int[] coord = (int[]) block;
            if (coord.length >= 3) {
                Map<String, Object> result = new HashMap<>();
                result.put("x", coord[0]);
                result.put("y", coord[1]);
                result.put("z", coord[2]);
                
                // 合并方块信息
                if (blockInfo instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> infoMap = (Map<String, Object>) blockInfo;
                    for (Map.Entry<String, Object> entry : infoMap.entrySet()) {
                        if (!entry.getKey().equals("x") && !entry.getKey().equals("y") && !entry.getKey().equals("z")) {
                            result.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
                
                return result;
            }
        }
        else if (block instanceof Map && blockInfo instanceof Map) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> blockMap = (Map<String, Object>) block;
                @SuppressWarnings("unchecked")
                Map<String, Object> infoMap = (Map<String, Object>) blockInfo;
                
                // 创建新对象，复制所有属性
                Map<String, Object> result = new HashMap<>(blockMap);
                
                // 合并方块信息，但保留原始坐标
                for (Map.Entry<String, Object> entry : infoMap.entrySet()) {
                    if (!entry.getKey().equals("x") && !entry.getKey().equals("y") && !entry.getKey().equals("z")) {
                        result.put(entry.getKey(), entry.getValue());
                    }
                }
                
                return result;
            } catch (Exception e) {
                // 处理失败，返回原始方块
                return block;
            }
        }
        
        // 如果无法处理，返回原始方块
        return block;
    }
    
    /**
     * 将坐标对象转换为浮点数组
     */
    private float[] getCoordinateAsFloatArray(Object coordObj) {
        // 处理int[]格式的坐标
        if (coordObj instanceof int[]) {
            int[] coord = (int[]) coordObj;
            if (coord.length >= 3) {
                return new float[]{(float) coord[0], (float) coord[1], (float) coord[2]};
            }
        }
        // 处理包含x,y,z字段的对象（如Coordinate类）
        else if (coordObj instanceof Map) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> coordMap = (Map<String, Object>) coordObj;
                
                if (coordMap.containsKey("x") && coordMap.containsKey("y") && coordMap.containsKey("z")) {
                    float x = ((Number) coordMap.get("x")).floatValue();
                    float y = ((Number) coordMap.get("y")).floatValue();
                    float z = ((Number) coordMap.get("z")).floatValue();
                    
                    return new float[]{x, y, z};
                }
            } catch (Exception e) {
                // 处理失败，返回null
                return null;
            }
        }
        
        return null;
    }
} 