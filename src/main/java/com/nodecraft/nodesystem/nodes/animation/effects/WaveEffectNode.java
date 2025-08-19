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
 * Wave Effect Node: 波浪效果节点
 * 使几何体产生波浪动画效果
 */
@NodeInfo(
    id = "animation.effects.wave",
    displayName = "Wave Effect",
    description = "使几何体产生波浪动画效果",
    category = "animation.effects"
)
public class WaveEffectNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_GEOMETRY_ID = "input_geometry";
    private static final String INPUT_DIRECTION_ID = "input_direction";
    private static final String INPUT_AMPLITUDE_ID = "input_amplitude";
    private static final String INPUT_WAVELENGTH_ID = "input_wavelength";
    private static final String INPUT_SPEED_ID = "input_speed";
    private static final String INPUT_TIME_ID = "input_time";
    private static final String INPUT_WAVE_TYPE_ID = "input_wave_type";
    private static final String INPUT_DAMPING_ID = "input_damping";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_WAVED_GEOMETRY_ID = "output_waved_geometry";
    
    // --- 波浪类型枚举 ---
    public enum WaveType {
        SINE(0, "Sine", "正弦波"),
        TRIANGLE(1, "Triangle", "三角波"),
        SQUARE(2, "Square", "方波"),
        RIPPLE(3, "Ripple", "涟漪波");
        
        private final int id;
        private final String name;
        private final String description;
        
        WaveType(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
        
        public static WaveType fromId(int id) {
            for (WaveType type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return SINE; // 默认波形
        }
    }
    
    // --- 构造函数 ---
    public WaveEffectNode() {
        super(UUID.randomUUID(), "animation.effects.wave");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_GEOMETRY_ID, "Geometry", "几何体", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_DIRECTION_ID, "Direction", "波浪方向", NodeDataType.VECTOR, this));
        addInputPort(new BasePort(INPUT_AMPLITUDE_ID, "Amplitude", "波幅", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_WAVELENGTH_ID, "Wavelength", "波长", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_SPEED_ID, "Speed", "波速", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_TIME_ID, "Time", "时间", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_WAVE_TYPE_ID, "Wave Type", "波形类型（0=正弦波, 1=三角波, 2=方波, 3=涟漪波）", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_DAMPING_ID, "Damping", "波幅衰减系数", NodeDataType.FLOAT, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_WAVED_GEOMETRY_ID, "Waved Geometry", "波浪化的几何体", NodeDataType.LIST, this));
    }
    
    @Override
    public String getDescription() {
        return "使几何体产生波浪动画效果";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object geometryObj = inputValues.get(INPUT_GEOMETRY_ID);
        float[] direction = (float[]) inputValues.getOrDefault(INPUT_DIRECTION_ID, new float[]{1.0f, 0.0f, 0.0f});
        Float amplitude = (Float) inputValues.getOrDefault(INPUT_AMPLITUDE_ID, 1.0f);
        Float wavelength = (Float) inputValues.getOrDefault(INPUT_WAVELENGTH_ID, 10.0f);
        Float speed = (Float) inputValues.getOrDefault(INPUT_SPEED_ID, 1.0f);
        Float time = (Float) inputValues.getOrDefault(INPUT_TIME_ID, 0.0f);
        Integer waveTypeId = (Integer) inputValues.getOrDefault(INPUT_WAVE_TYPE_ID, WaveType.SINE.id);
        Float damping = (Float) inputValues.getOrDefault(INPUT_DAMPING_ID, 0.0f);
        
        // 确保向量有效
        if (direction.length < 3) direction = new float[]{1.0f, 0.0f, 0.0f};
        
        // 归一化方向向量
        normalizeVector(direction);
        
        // 确保波长为正数
        wavelength = Math.max(0.1f, wavelength);
        
        // 获取波形类型
        WaveType waveType = WaveType.fromId(waveTypeId);
        
        // 处理几何体
        List<Object> wavedGeometry = processGeometry(geometryObj, direction, amplitude, wavelength, speed, time, waveType, damping);
        
        // 设置输出值
        outputValues.put(OUTPUT_WAVED_GEOMETRY_ID, wavedGeometry);
    }
    
    /**
     * 处理几何体，应用波浪效果
     */
    private List<Object> processGeometry(Object geometryObj, float[] direction, float amplitude, float wavelength, 
                                       float speed, float time, WaveType waveType, float damping) {
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
                float[] blockPos = getCoordinateAsFloatArray(block);
                if (blockPos == null) continue;
                
                // 计算方块的新位置
                float[] newPos = calculateWavePosition(blockPos, direction, amplitude, wavelength, speed, time, waveType, damping);
                
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
     * 计算波浪效果后的位置
     */
    private float[] calculateWavePosition(float[] position, float[] direction, float amplitude, float wavelength, 
                                        float speed, float time, WaveType waveType, float damping) {
        // 计算方块在波浪方向上的投影距离
        float distance = dotProduct(position, direction);
        
        // 计算波的相位（加上时间和速度因子）
        float phase = (distance / wavelength + time * speed) * (float) (2.0 * Math.PI);
        
        // 计算波的振幅（可能带有衰减）
        float effectiveAmplitude = amplitude;
        if (damping > 0) {
            // 衰减基于与原点的距离
            float distanceFromOrigin = (float) Math.sqrt(
                position[0] * position[0] + 
                position[1] * position[1] + 
                position[2] * position[2]
            );
            effectiveAmplitude *= Math.exp(-damping * distanceFromOrigin);
        }
        
        // 根据波形类型计算位移
        float displacement = 0;
        switch (waveType) {
            case SINE:
                // 正弦波
                displacement = (float) Math.sin(phase) * effectiveAmplitude;
                break;
                
            case TRIANGLE:
                // 三角波
                float modPhase = phase % (2 * (float) Math.PI);
                if (modPhase < 0) modPhase += 2 * (float) Math.PI;
                
                if (modPhase < (float) Math.PI / 2) {
                    displacement = (modPhase / ((float) Math.PI / 2)) * effectiveAmplitude;
                } else if (modPhase < (float) Math.PI * 3 / 2) {
                    displacement = (1 - (modPhase - (float) Math.PI / 2) / (float) Math.PI) * 2 * effectiveAmplitude - effectiveAmplitude;
                } else {
                    displacement = ((modPhase - (float) Math.PI * 3 / 2) / ((float) Math.PI / 2) - 1) * effectiveAmplitude;
                }
                break;
                
            case SQUARE:
                // 方波
                displacement = (Math.sin(phase) >= 0 ? 1 : -1) * effectiveAmplitude;
                break;
                
            case RIPPLE:
                // 涟漪波（使用径向距离）
                // 计算方块到原点的距离
                float distFromOrigin = (float) Math.sqrt(
                    position[0] * position[0] + 
                    position[1] * position[1] + 
                    position[2] * position[2]
                );
                
                // 计算径向波的相位
                float ripplePhase = (distFromOrigin / wavelength - time * speed) * (float) (2.0 * Math.PI);
                
                // 使用衰减的正弦波
                float rippleFactor = (float) (1.0 / Math.max(0.1, distFromOrigin));
                displacement = (float) Math.sin(ripplePhase) * effectiveAmplitude * rippleFactor;
                break;
        }
        
        // 计算新位置（沿Y轴移动）
        float[] newPosition = new float[3];
        newPosition[0] = position[0];
        newPosition[1] = position[1] + displacement; // 默认垂直方向为Y轴
        newPosition[2] = position[2];
        
        return newPosition;
    }
    
    /**
     * 计算点积
     */
    private float dotProduct(float[] v1, float[] v2) {
        return v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
    }
    
    /**
     * 归一化向量
     */
    private void normalizeVector(float[] v) {
        float length = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        
        // 防止除以零
        if (length > 0.0001f) {
            v[0] /= length;
            v[1] /= length;
            v[2] /= length;
        } else {
            // 如果向量长度为0，设置为默认X轴
            v[0] = 1.0f;
            v[1] = 0.0f;
            v[2] = 0.0f;
        }
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