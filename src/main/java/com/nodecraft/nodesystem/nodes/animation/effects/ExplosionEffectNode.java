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
import java.util.Random;
import java.util.UUID;

/**
 * Explosion Effect Node: 爆炸效果节点
 * 让几何体的方块沿爆炸轨迹移动
 */
@NodeInfo(
    id = "animation.effects.explosion",
    displayName = "Explosion Effect",
    description = "让几何体产生爆炸或内爆效果",
    category = "animation.effects"
)
public class ExplosionEffectNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_GEOMETRY_ID = "input_geometry";
    private static final String INPUT_CENTER_ID = "input_center";
    private static final String INPUT_MAX_FORCE_ID = "input_max_force";
    private static final String INPUT_TIME_FACTOR_ID = "input_time_factor";
    private static final String INPUT_MODE_ID = "input_mode";
    private static final String INPUT_GRAVITY_ID = "input_gravity";
    private static final String INPUT_RANDOMNESS_ID = "input_randomness";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_ANIMATED_GEOMETRY_ID = "output_animated_geometry";
    
    // --- 爆炸模式枚举 ---
    public enum ExplosionMode {
        EXPLOSION(0, "Explosion", "向外爆炸"),
        IMPLOSION(1, "Implosion", "向内内爆");
        
        private final int id;
        private final String name;
        private final String description;
        
        ExplosionMode(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
        
        public static ExplosionMode fromId(int id) {
            return id == 1 ? IMPLOSION : EXPLOSION;
        }
    }
    
    // 随机数生成器
    private final Random random = new Random();
    
    // 缓存方块的初始位置（用于计算爆炸轨迹）
    private final Map<String, float[]> initialPositions = new HashMap<>();
    
    // --- 构造函数 ---
    public ExplosionEffectNode() {
        super(UUID.randomUUID(), "animation.effects.explosion");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_GEOMETRY_ID, "Original Geometry", "原始几何体", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_CENTER_ID, "Explosion Center", "爆炸中心点", NodeDataType.VECTOR, this));
        addInputPort(new BasePort(INPUT_MAX_FORCE_ID, "Max Force", "最大爆炸力", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_TIME_FACTOR_ID, "Time Factor", "时间因子（0-1）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_MODE_ID, "Mode", "模式（0=爆炸, 1=内爆）", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_GRAVITY_ID, "Gravity", "重力因子", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_RANDOMNESS_ID, "Randomness", "随机因子", NodeDataType.FLOAT, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_ANIMATED_GEOMETRY_ID, "Animated Geometry", "动画后的几何体", NodeDataType.LIST, this));
    }
    
    @Override
    public String getDescription() {
        return "让几何体产生爆炸或内爆效果";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object geometryObj = inputValues.get(INPUT_GEOMETRY_ID);
        float[] center = (float[]) inputValues.getOrDefault(INPUT_CENTER_ID, new float[]{0.0f, 0.0f, 0.0f});
        Float maxForce = (Float) inputValues.getOrDefault(INPUT_MAX_FORCE_ID, 10.0f);
        Float timeFactor = (Float) inputValues.getOrDefault(INPUT_TIME_FACTOR_ID, 0.0f);
        Integer modeId = (Integer) inputValues.getOrDefault(INPUT_MODE_ID, ExplosionMode.EXPLOSION.id);
        Float gravity = (Float) inputValues.getOrDefault(INPUT_GRAVITY_ID, 9.8f);
        Float randomness = (Float) inputValues.getOrDefault(INPUT_RANDOMNESS_ID, 0.3f);
        
        // 确保时间因子在0-1范围内
        timeFactor = Math.max(0.0f, Math.min(1.0f, timeFactor));
        
        // 确保向量有效
        if (center.length < 3) center = new float[]{0.0f, 0.0f, 0.0f};
        
        // 确保力度为正数
        maxForce = Math.abs(maxForce);
        
        // 确保随机因子在合理范围内
        randomness = Math.max(0.0f, Math.min(1.0f, randomness));
        
        // 获取爆炸模式
        ExplosionMode mode = ExplosionMode.fromId(modeId);
        
        // 处理几何体
        List<Object> animatedGeometry = processGeometry(geometryObj, center, maxForce, timeFactor, mode, gravity, randomness);
        
        // 设置输出值
        outputValues.put(OUTPUT_ANIMATED_GEOMETRY_ID, animatedGeometry);
    }
    
    /**
     * 处理几何体，应用爆炸/内爆效果
     */
    private List<Object> processGeometry(Object geometryObj, float[] center, float maxForce, float timeFactor, 
                                       ExplosionMode mode, float gravity, float randomness) {
        List<Object> result = new ArrayList<>();
        
        if (geometryObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> geometryList = (List<Object>) geometryObj;
            
            // 如果几何体为空，直接返回空列表
            if (geometryList.isEmpty()) {
                return result;
            }
            
            // 处理每个方块
            for (Object block : geometryList) {
                // 获取方块坐标
                float[] blockPos = getBlockPosition(block);
                if (blockPos == null) continue;
                
                // 计算方块的新位置
                float[] newPos = calculateExplosionPosition(block, blockPos, center, maxForce, timeFactor, mode, gravity, randomness);
                
                // 创建新的方块对象并添加到结果中
                Object transformedBlock = createTransformedBlock(block, newPos);
                if (transformedBlock != null) {
                    result.add(transformedBlock);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 计算方块在爆炸中的位置
     */
    private float[] calculateExplosionPosition(Object block, float[] blockPos, float[] center, float maxForce, 
                                             float timeFactor, ExplosionMode mode, float gravity, float randomness) {
        // 获取或创建方块的初始爆炸数据
        String blockKey = getBlockKey(block);
        float[] initialDirection = initialPositions.computeIfAbsent(blockKey, k -> {
            // 计算从中心点到方块的方向向量
            float[] direction = new float[3];
            for (int i = 0; i < 3; i++) {
                direction[i] = blockPos[i] - center[i];
            }
            
            // 计算距离
            float distance = (float) Math.sqrt(
                direction[0] * direction[0] + 
                direction[1] * direction[1] + 
                direction[2] * direction[2]
            );
            
            // 规范化方向向量
            if (distance > 0.0001f) {
                for (int i = 0; i < 3; i++) {
                    direction[i] /= distance;
                }
            } else {
                // 如果距离太小，给一个随机方向
                direction[0] = (random.nextFloat() * 2.0f - 1.0f);
                direction[1] = (random.nextFloat() * 2.0f - 1.0f);
                direction[2] = (random.nextFloat() * 2.0f - 1.0f);
                
                // 归一化随机方向
                float randLength = (float) Math.sqrt(
                    direction[0] * direction[0] + 
                    direction[1] * direction[1] + 
                    direction[2] * direction[2]
                );
                
                if (randLength > 0.0001f) {
                    for (int i = 0; i < 3; i++) {
                        direction[i] /= randLength;
                    }
                }
            }
            
            // 添加随机性
            if (randomness > 0) {
                for (int i = 0; i < 3; i++) {
                    direction[i] += (random.nextFloat() * 2.0f - 1.0f) * randomness;
                }
                
                // 重新归一化
                float newLength = (float) Math.sqrt(
                    direction[0] * direction[0] + 
                    direction[1] * direction[1] + 
                    direction[2] * direction[2]
                );
                
                if (newLength > 0.0001f) {
                    for (int i = 0; i < 3; i++) {
                        direction[i] /= newLength;
                    }
                }
            }
            
            // 计算初始力（基于距离）
            float force = calculateForce(distance, maxForce);
            
            // 返回初始方向和力的复合向量
            float[] result = new float[3];
            for (int i = 0; i < 3; i++) {
                result[i] = direction[i] * force;
            }
            
            return result;
        });
        
        // 计算当前位置
        float[] newPos = new float[3];
        
        // 是否反转时间（用于内爆）
        float effectiveTime = (mode == ExplosionMode.EXPLOSION) ? timeFactor : 1.0f - timeFactor;
        
        // 应用物理模拟
        for (int i = 0; i < 3; i++) {
            // 位置 = 初始位置 + 方向*时间 + 重力*时间^2/2 (i=1为Y轴)
            float gravityEffect = (i == 1) ? -0.5f * gravity * effectiveTime * effectiveTime : 0;
            
            if (mode == ExplosionMode.EXPLOSION) {
                newPos[i] = blockPos[i] + initialDirection[i] * effectiveTime + gravityEffect;
            } else {
                // 内爆：从最终位置向初始位置移动
                float finalPos = blockPos[i] + initialDirection[i] + ((i == 1) ? -0.5f * gravity : 0);
                newPos[i] = finalPos - (finalPos - blockPos[i]) * effectiveTime;
            }
        }
        
        return newPos;
    }
    
    /**
     * 根据距离计算力度
     */
    private float calculateForce(float distance, float maxForce) {
        // 距离越远，力越小（反比例）
        float minDistance = 0.1f; // 防止除以零
        distance = Math.max(minDistance, distance);
        
        // 使用平方反比衰减
        return maxForce / (1.0f + distance * 0.1f);
    }
    
    /**
     * 获取方块的唯一标识（用于缓存）
     */
    private String getBlockKey(Object block) {
        float[] pos = getBlockPosition(block);
        if (pos != null) {
            return pos[0] + "," + pos[1] + "," + pos[2];
        }
        return block.hashCode() + "";
    }
    
    /**
     * 获取方块的位置
     */
    private float[] getBlockPosition(Object block) {
        // 处理int[]格式的坐标
        if (block instanceof int[]) {
            int[] coord = (int[]) block;
            if (coord.length >= 3) {
                return new float[]{(float) coord[0], (float) coord[1], (float) coord[2]};
            }
        }
        // 处理包含x,y,z字段的对象（如Coordinate类）
        else if (block instanceof Map) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> coordMap = (Map<String, Object>) block;
                
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
    
    /**
     * 创建变换后的方块对象
     */
    private Object createTransformedBlock(Object originalBlock, float[] newPosition) {
        // 处理int[]格式的坐标
        if (originalBlock instanceof int[]) {
            int[] newCoord = new int[]{
                Math.round(newPosition[0]),
                Math.round(newPosition[1]),
                Math.round(newPosition[2])
            };
            return newCoord;
        }
        // 处理包含x,y,z字段的对象（如Coordinate类）
        else if (originalBlock instanceof Map) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> origMap = (Map<String, Object>) originalBlock;
                
                // 创建新对象，复制所有属性
                Map<String, Object> newMap = new HashMap<>(origMap);
                
                // 更新坐标
                newMap.put("x", Math.round(newPosition[0]));
                newMap.put("y", Math.round(newPosition[1]));
                newMap.put("z", Math.round(newPosition[2]));
                
                return newMap;
            } catch (Exception e) {
                // 处理失败，返回null
                return null;
            }
        }
        
        return null;
    }
} 