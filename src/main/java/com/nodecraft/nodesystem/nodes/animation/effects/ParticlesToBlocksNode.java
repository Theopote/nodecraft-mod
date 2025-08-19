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
 * Particles To Blocks Node: 粒子到方块效果节点
 * 实现粒子逐渐变成方块的效果
 */
@NodeInfo(
    id = "animation.effects.particles_to_blocks",
    displayName = "Particles To Blocks",
    description = "实现粒子逐渐变成方块的视觉效果",
    category = "animation.effects"
)
public class ParticlesToBlocksNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_TARGET_BLOCKS_ID = "input_target_blocks";
    private static final String INPUT_PARTICLE_TYPE_ID = "input_particle_type";
    private static final String INPUT_TIME_FACTOR_ID = "input_time_factor";
    private static final String INPUT_PARTICLE_DENSITY_ID = "input_particle_density";
    private static final String INPUT_PARTICLE_SPEED_ID = "input_particle_speed";
    private static final String INPUT_TRANSITION_MODE_ID = "input_transition_mode";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_VISIBLE_BLOCKS_ID = "output_visible_blocks";
    private static final String OUTPUT_PARTICLE_EFFECTS_ID = "output_particle_effects";
    
    // --- 粒子类型枚举 ---
    public enum ParticleType {
        DUST(0, "Dust", "灰尘粒子"),
        FLAME(1, "Flame", "火焰粒子"),
        SMOKE(2, "Smoke", "烟雾粒子"),
        PORTAL(3, "Portal", "传送门粒子"),
        ENCHANT(4, "Enchant", "附魔粒子"),
        REDSTONE(5, "Redstone", "红石粒子");
        
        private final int id;
        private final String name;
        private final String description;
        
        ParticleType(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
        
        public static ParticleType fromId(int id) {
            for (ParticleType type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return DUST; // 默认粒子类型
        }
    }
    
    // --- 过渡模式枚举 ---
    public enum TransitionMode {
        FADE(0, "Fade", "方块淡入，粒子淡出"),
        ASSEMBLE(1, "Assemble", "粒子聚集成方块"),
        MORPH(2, "Morph", "粒子变形为方块");
        
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
            return FADE; // 默认过渡模式
        }
    }
    
    // 随机数生成器
    private final Random random = new Random();
    
    // --- 构造函数 ---
    public ParticlesToBlocksNode() {
        super(UUID.randomUUID(), "animation.effects.particles_to_blocks");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_TARGET_BLOCKS_ID, "Target Blocks", "目标方块", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_PARTICLE_TYPE_ID, "Particle Type", "粒子类型", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_TIME_FACTOR_ID, "Time Factor", "时间因子（0-1）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_PARTICLE_DENSITY_ID, "Particle Density", "粒子密度", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_PARTICLE_SPEED_ID, "Particle Speed", "粒子速度", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_TRANSITION_MODE_ID, "Transition Mode", "过渡模式", NodeDataType.INTEGER, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_VISIBLE_BLOCKS_ID, "Visible Blocks", "可见方块", NodeDataType.LIST, this));
        addOutputPort(new BasePort(OUTPUT_PARTICLE_EFFECTS_ID, "Particle Effects", "粒子效果", NodeDataType.LIST, this));
    }
    
    @Override
    public String getDescription() {
        return "实现粒子逐渐变成方块的视觉效果";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object targetBlocksObj = inputValues.get(INPUT_TARGET_BLOCKS_ID);
        Integer particleTypeId = (Integer) inputValues.getOrDefault(INPUT_PARTICLE_TYPE_ID, ParticleType.DUST.id);
        Float timeFactor = (Float) inputValues.getOrDefault(INPUT_TIME_FACTOR_ID, 0.0f);
        Float particleDensity = (Float) inputValues.getOrDefault(INPUT_PARTICLE_DENSITY_ID, 5.0f);
        Float particleSpeed = (Float) inputValues.getOrDefault(INPUT_PARTICLE_SPEED_ID, 0.1f);
        Integer transitionModeId = (Integer) inputValues.getOrDefault(INPUT_TRANSITION_MODE_ID, TransitionMode.FADE.id);
        
        // 确保时间因子在0-1范围内
        timeFactor = Math.max(0.0f, Math.min(1.0f, timeFactor));
        
        // 确保粒子密度为正数
        particleDensity = Math.max(0.1f, particleDensity);
        
        // 确保粒子速度为正数
        particleSpeed = Math.max(0.01f, particleSpeed);
        
        // 获取粒子类型和过渡模式
        ParticleType particleType = ParticleType.fromId(particleTypeId);
        TransitionMode transitionMode = TransitionMode.fromId(transitionModeId);
        
        // 处理转换
        processTransition(targetBlocksObj, particleType, timeFactor, particleDensity, particleSpeed, transitionMode);
    }
    
    /**
     * 处理粒子到方块的转换
     */
    private void processTransition(Object targetBlocksObj, ParticleType particleType, float timeFactor, 
                                  float particleDensity, float particleSpeed, TransitionMode transitionMode) {
        List<Object> visibleBlocks = new ArrayList<>();
        List<Object> particleEffects = new ArrayList<>();
        
        if (targetBlocksObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> targetBlocks = (List<Object>) targetBlocksObj;
            
            // 根据时间因子计算当前应该显示的方块和粒子
            for (Object block : targetBlocks) {
                // 获取方块位置
                float[] position = getCoordinateAsFloatArray(block);
                if (position == null) continue;
                
                // 根据过渡模式处理每个方块
                processBlockTransition(block, position, particleType, timeFactor, particleDensity, 
                                     particleSpeed, transitionMode, visibleBlocks, particleEffects);
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_VISIBLE_BLOCKS_ID, visibleBlocks);
        outputValues.put(OUTPUT_PARTICLE_EFFECTS_ID, particleEffects);
    }
    
    /**
     * 处理单个方块的转换
     */
    private void processBlockTransition(Object block, float[] position, ParticleType particleType, float timeFactor,
                                      float particleDensity, float particleSpeed, TransitionMode transitionMode,
                                      List<Object> visibleBlocks, List<Object> particleEffects) {
        // 计算块的随机种子（确保相同的块在不同帧中有相同的行为）
        int blockSeed = getBlockSeed(block, position);
        Random blockRandom = new Random(blockSeed);
        
        // 为方块指定一个唯一的转换阈值（在0.2-0.8范围内）
        float threshold = 0.2f + blockRandom.nextFloat() * 0.6f;
        
        // 基于过渡模式计算方块是否可见
        boolean blockVisible = false;
        int particleCount = 0;
        
        switch (transitionMode) {
            case FADE:
                // 简单淡入淡出：时间因子超过阈值时显示方块，否则显示粒子
                blockVisible = timeFactor >= threshold;
                particleCount = blockVisible ? 
                    Math.round(particleDensity * (1.0f - timeFactor) * 2) : // 方块出现后粒子逐渐减少
                    Math.round(particleDensity * 2); // 方块出现前保持较多粒子
                break;
                
            case ASSEMBLE:
                // 聚集效果：时间因子接近阈值时粒子数量增加，超过阈值显示方块
                blockVisible = timeFactor >= threshold;
                float distanceToThreshold = Math.abs(timeFactor - threshold);
                particleCount = Math.round(particleDensity * (1.0f - distanceToThreshold * 3));
                
                // 限制粒子数量在合理范围内
                particleCount = Math.max(0, Math.min(particleCount, (int)(particleDensity * 2)));
                break;
                
            case MORPH:
                // 变形效果：时间因子增加时粒子逐渐聚拢，同时方块逐渐显现
                blockVisible = timeFactor >= threshold * 0.8f; // 提前显示方块
                
                // 粒子数量随时间减少，但保持一定数量直到完全变形
                float morphProgress = Math.min(1.0f, timeFactor / threshold);
                particleCount = Math.round(particleDensity * (1.0f - morphProgress * 0.7f));
                break;
        }
        
        // 如果方块可见，添加到可见方块列表
        if (blockVisible) {
            visibleBlocks.add(block);
        }
        
        // 创建粒子效果
        for (int i = 0; i < particleCount; i++) {
            // 创建粒子参数
            Map<String, Object> particleParams = createParticleParameters(
                position, particleType, blockRandom, timeFactor, particleSpeed, transitionMode);
            
            // 添加粒子效果
            particleEffects.add(particleParams);
        }
    }
    
    /**
     * 获取方块的种子值
     */
    private int getBlockSeed(Object block, float[] position) {
        if (position != null) {
            int x = Math.round(position[0]);
            int y = Math.round(position[1]);
            int z = Math.round(position[2]);
            return x * 73856093 ^ y * 19349663 ^ z * 83492791;
        }
        return block.hashCode();
    }
    
    /**
     * 创建粒子参数
     */
    private Map<String, Object> createParticleParameters(float[] blockPosition, ParticleType particleType, 
                                                      Random blockRandom, float timeFactor, 
                                                      float particleSpeed, TransitionMode transitionMode) {
        Map<String, Object> params = new HashMap<>();
        
        // 粒子类型
        params.put("type", particleType.name().toLowerCase());
        
        // 根据过渡模式计算粒子位置
        float[] particlePos = new float[3];
        float dispersal = 0.5f; // 基础扩散范围（方块半径）
        
        switch (transitionMode) {
            case FADE:
                // 淡入淡出：粒子在方块周围随机分布
                dispersal = 0.5f + 0.5f * (1.0f - timeFactor); // 随时间减小扩散
                break;
                
            case ASSEMBLE:
                // 聚集：粒子从远处向方块聚集
                dispersal = 0.5f + 2.0f * (1.0f - timeFactor); // 较大的初始扩散
                break;
                
            case MORPH:
                // 变形：粒子围绕方块旋转并逐渐贴近
                dispersal = 0.5f + 1.0f * (1.0f - timeFactor); // 中等扩散
                break;
        }
        
        // 计算粒子位置（方块中心加上随机偏移）
        for (int i = 0; i < 3; i++) {
            float offset = (blockRandom.nextFloat() * 2 - 1) * dispersal;
            particlePos[i] = blockPosition[i] + offset;
        }
        
        // 设置粒子位置
        params.put("x", particlePos[0]);
        params.put("y", particlePos[1]);
        params.put("z", particlePos[2]);
        
        // 计算粒子速度矢量
        float[] velocity = new float[3];
        
        switch (transitionMode) {
            case FADE:
                // 淡入淡出：粒子缓慢随机移动
                for (int i = 0; i < 3; i++) {
                    velocity[i] = (blockRandom.nextFloat() * 2 - 1) * particleSpeed * 0.2f;
                }
                break;
                
            case ASSEMBLE:
                // 聚集：粒子向方块中心移动
                for (int i = 0; i < 3; i++) {
                    // 从粒子指向方块中心的向量
                    float direction = blockPosition[i] - particlePos[i];
                    float distance = Math.abs(direction);
                    
                    // 速度与距离成正比
                    velocity[i] = direction * particleSpeed * (0.5f + distance * 0.5f);
                }
                break;
                
            case MORPH:
                // 变形：粒子围绕方块旋转
                // 创建围绕Y轴旋转的效果
                float dx = particlePos[0] - blockPosition[0];
                float dz = particlePos[2] - blockPosition[2];
                float radius = (float) Math.sqrt(dx * dx + dz * dz);
                
                if (radius > 0.01f) {
                    // 计算切线方向（垂直于径向方向）
                    velocity[0] = -dz / radius * particleSpeed;
                    velocity[1] = (blockRandom.nextFloat() * 2 - 1) * particleSpeed * 0.2f;
                    velocity[2] = dx / radius * particleSpeed;
                } else {
                    // 如果太靠近中心，给一个小的随机速度
                    for (int i = 0; i < 3; i++) {
                        velocity[i] = (blockRandom.nextFloat() * 2 - 1) * particleSpeed * 0.1f;
                    }
                }
                
                // 添加向中心的分量
                for (int i = 0; i < 3; i++) {
                    float direction = blockPosition[i] - particlePos[i];
                    velocity[i] += direction * particleSpeed * 0.2f;
                }
                break;
        }
        
        // 设置粒子速度
        params.put("vx", velocity[0]);
        params.put("vy", velocity[1]);
        params.put("vz", velocity[2]);
        
        // 设置粒子颜色（如果适用）
        if (particleType == ParticleType.DUST || particleType == ParticleType.REDSTONE) {
            // 从方块获取颜色或使用默认颜色
            int[] color = getBlockColor(blockRandom);
            params.put("r", color[0]);
            params.put("g", color[1]);
            params.put("b", color[2]);
        }
        
        // 设置粒子寿命
        float lifetime = 10.0f + blockRandom.nextFloat() * 10.0f;
        params.put("lifetime", lifetime);
        
        return params;
    }
    
    /**
     * 获取方块的颜色（RGB值）
     */
    private int[] getBlockColor(Random random) {
        // 这里可以根据方块类型返回适当的颜色
        // 此示例简单返回随机颜色
        return new int[]{
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        };
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