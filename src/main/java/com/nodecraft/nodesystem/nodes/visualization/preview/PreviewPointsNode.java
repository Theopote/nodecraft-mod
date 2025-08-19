package com.nodecraft.nodesystem.nodes.visualization.preview;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Preview Points 节点: 预览点 (List<Position> 或 List<Coordinate>) 为粒子或小标记
 */
@NodeInfo(
    id = "visualization.preview.preview_points",
    displayName = "预览点",
    description = "预览点 (List<Position> 或 List<Coordinate>) 为粒子或小标记",
    category = "visualization.preview"
)
public class PreviewPointsNode extends BaseNode {

    // --- 节点属性 ---
    private String previewColor = "#FF0000"; // 默认颜色（红色）
    private float pointSize = 0.2f; // 点大小（方块或粒子的大小）
    private int duration = 30; // 持续时间（秒）
    private String particleType = "flame"; // 粒子类型
    private boolean useParticles = true; // 是否使用粒子而不是小方块
    private UUID previewId = UUID.randomUUID(); // 预览实例ID
    private String description = "预览点 (List<Position> 或 List<Coordinate>) 为粒子或小标记";

    // --- 输入端口 IDs ---
    private static final String INPUT_POINTS_ID = "input_points";
    private static final String INPUT_COLOR_ID = "input_color";
    private static final String INPUT_POINT_SIZE_ID = "input_point_size";
    private static final String INPUT_DURATION_ID = "input_duration";
    private static final String INPUT_PARTICLE_TYPE_ID = "input_particle_type";
    private static final String INPUT_USE_PARTICLES_ID = "input_use_particles";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_PREVIEW_ID_ID = "output_preview_id";
    private static final String OUTPUT_POINT_COUNT_ID = "output_point_count";

    // --- 构造函数 ---
    public PreviewPointsNode() {
        super(UUID.randomUUID(), "visualization.preview.preview_points");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_POINTS_ID, "Points", 
                "要预览的点列表", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_COLOR_ID, "Color", 
                "预览颜色（十六进制，如'#FF0000'）", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_POINT_SIZE_ID, "Point Size", 
                "点大小", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_DURATION_ID, "Duration", 
                "预览持续时间（秒）", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_PARTICLE_TYPE_ID, "Particle Type", 
                "粒子类型（如'flame', 'smoke', 'heart'等）", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_USE_PARTICLES_ID, "Use Particles", 
                "是否使用粒子而不是小方块", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功显示预览", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_PREVIEW_ID_ID, "Preview ID", 
                "预览实例ID（用于后续控制）", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_POINT_COUNT_ID, "Point Count", 
                "预览的点数量", NodeDataType.INTEGER, this));
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 默认输出值
        boolean success = false;
        String previewIdStr = previewId.toString();
        int pointCount = 0;
        
        // 获取输入值
        Object pointsObj = inputValues.get(INPUT_POINTS_ID);
        Object colorObj = inputValues.get(INPUT_COLOR_ID);
        Object pointSizeObj = inputValues.get(INPUT_POINT_SIZE_ID);
        Object durationObj = inputValues.get(INPUT_DURATION_ID);
        Object particleTypeObj = inputValues.get(INPUT_PARTICLE_TYPE_ID);
        Object useParticlesObj = inputValues.get(INPUT_USE_PARTICLES_ID);
        
        // 确定预览颜色
        String previewColor = this.previewColor;
        if (colorObj instanceof String) {
            previewColor = (String) colorObj;
        }
        
        // 确定点大小
        float pointSize = this.pointSize;
        if (pointSizeObj instanceof Number) {
            pointSize = Math.max(0.05f, Math.min(2.0f, ((Number) pointSizeObj).floatValue()));
        }
        
        // 确定持续时间
        int duration = this.duration;
        if (durationObj instanceof Number) {
            duration = Math.max(1, ((Number) durationObj).intValue());
        }
        
        // 确定粒子类型
        String particleType = this.particleType;
        if (particleTypeObj instanceof String) {
            particleType = (String) particleTypeObj;
        }
        
        // 确定是否使用粒子
        boolean useParticles = this.useParticles;
        if (useParticlesObj instanceof Boolean) {
            useParticles = (Boolean) useParticlesObj;
        }
        
        // 处理输入：点列表
        List<?> pointsList = null;
        
        if (pointsObj instanceof List && !((List<?>) pointsObj).isEmpty()) {
            pointsList = (List<?>) pointsObj;
        }
        
        // 检查必要的输入是否存在
        if (pointsList != null) {
            try {
                /*
                在实际实现中，需要根据Minecraft API实现以下操作:
                1. 清除当前预览ID的所有预览
                2. 处理输入数据，构建预览点列表
                3. 应用预览效果
                
                // 首先清除现有预览（如果有）
                // PreviewManager.clearPreview(previewId);
                
                List<PreviewPoint> previewPoints = new ArrayList<>();
                
                // 处理点列表输入
                for (Object obj : pointsList) {
                    Vec3d position = null;
                    
                    if (obj instanceof Vec3d) {
                        position = (Vec3d) obj;
                    } else if (obj instanceof BlockPos) {
                        BlockPos pos = (BlockPos) obj;
                        position = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    } else if (obj instanceof Coordinate) {
                        Coordinate coord = (Coordinate) obj;
                        position = new Vec3d(coord.getX(), coord.getY(), coord.getZ());
                    }
                    
                    if (position != null) {
                        previewPoints.add(new PreviewPoint(position, previewColor, pointSize));
                        pointCount++;
                    }
                }
                
                // 应用预览
                if (!previewPoints.isEmpty()) {
                    if (useParticles) {
                        // 使用粒子效果
                        ParticleEffect particleEffect = getParticleEffect(particleType);
                        // PreviewManager.showParticlePoints(previewId, previewPoints, particleEffect, duration);
                    } else {
                        // 使用小方块标记
                        // PreviewManager.showBlockPoints(previewId, previewPoints, duration);
                    }
                    success = true;
                }
                */
                
                // 模拟预览点 (在实际实现中替换为上面的逻辑)
                pointCount = pointsList.size();
                
                // 模拟成功预览
                success = pointCount > 0;
                
                // 打印调试信息
                System.out.println("模拟预览 " + pointCount + " 个点，颜色: " + previewColor + 
                        "，大小: " + pointSize + "，持续: " + duration + " 秒" +
                        "，使用粒子: " + useParticles + (useParticles ? ("，粒子类型: " + particleType) : ""));
            } catch (Exception e) {
                success = false;
                System.err.println("Error creating point preview: " + e.getMessage());
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_PREVIEW_ID_ID, previewIdStr);
        outputValues.put(OUTPUT_POINT_COUNT_ID, pointCount);
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getPreviewColor() {
        return previewColor;
    }
    
    public void setPreviewColor(String previewColor) {
        // 简单验证十六进制颜色
        if (previewColor != null && previewColor.matches("^#[0-9A-Fa-f]{6}$")) {
            this.previewColor = previewColor;
            markDirty();
        }
    }
    
    public float getPointSize() {
        return pointSize;
    }
    
    public void setPointSize(float pointSize) {
        this.pointSize = Math.max(0.05f, Math.min(2.0f, pointSize));
        markDirty();
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = Math.max(1, duration);
        markDirty();
    }
    
    public String getParticleType() {
        return particleType;
    }
    
    public void setParticleType(String particleType) {
        if (isValidParticleType(particleType)) {
            this.particleType = particleType;
            markDirty();
        }
    }
    
    /**
     * 检查粒子类型是否有效
     */
    private boolean isValidParticleType(String type) {
        if (type == null) return false;
        
        // 这里只是简单列举一些常见的粒子类型
        // 实际实现中应该根据Minecraft的粒子系统进行验证
        String[] validTypes = {
            "flame", "smoke", "cloud", "heart", "firework", "bubble", 
            "splash", "dust", "enchant", "portal", "end_rod", "totem"
        };
        
        for (String validType : validTypes) {
            if (validType.equals(type.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean isUseParticles() {
        return useParticles;
    }
    
    public void setUseParticles(boolean useParticles) {
        this.useParticles = useParticles;
        markDirty();
    }
    
    public UUID getPreviewId() {
        return previewId;
    }
    
    /**
     * 重置预览ID（用于创建新的预览实例）
     */
    public void resetPreviewId() {
        previewId = UUID.randomUUID();
        markDirty();
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        Object[] state = new Object[6];
        state[0] = previewColor;
        state[1] = pointSize;
        state[2] = duration;
        state[3] = particleType;
        state[4] = useParticles;
        state[5] = previewId.toString();
        return state;
    }
    
    /**
     * 恢复节点状态
     */
    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof Object[]) {
            Object[] objState = (Object[]) state;
            if (objState.length >= 6) {
                if (objState[0] instanceof String) {
                    setPreviewColor((String) objState[0]);
                }
                if (objState[1] instanceof Number) {
                    setPointSize(((Number) objState[1]).floatValue());
                }
                if (objState[2] instanceof Number) {
                    setDuration(((Number) objState[2]).intValue());
                }
                if (objState[3] instanceof String) {
                    setParticleType((String) objState[3]);
                }
                if (objState[4] instanceof Boolean) {
                    setUseParticles((Boolean) objState[4]);
                }
                if (objState[5] instanceof String) {
                    try {
                        previewId = UUID.fromString((String) objState[5]);
                    } catch (IllegalArgumentException e) {
                        resetPreviewId();
                    }
                }
            }
        }
    }
} 