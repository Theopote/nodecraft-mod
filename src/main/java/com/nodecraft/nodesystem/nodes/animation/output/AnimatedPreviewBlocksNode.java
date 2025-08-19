package com.nodecraft.nodesystem.nodes.animation.output;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Animated Preview Blocks Node: 动画预览方块节点
 * 接收几何体列表，并在游戏世界中实时渲染为动画帧
 */
@NodeInfo(
    id = "animation.output.animated_preview_blocks",
    displayName = "Animated Preview Blocks",
    description = "在游戏世界中实时渲染动画帧",
    category = "animation.output"
)
public class AnimatedPreviewBlocksNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_ANIMATED_GEOMETRY_ID = "input_animated_geometry";
    private static final String INPUT_ENABLED_ID = "input_enabled";
    private static final String INPUT_FRAME_RATE_ID = "input_frame_rate";
    private static final String INPUT_RENDER_DISTANCE_ID = "input_render_distance";
    private static final String INPUT_RENDER_MODE_ID = "input_render_mode";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_RENDER_STATUS_ID = "output_render_status";
    private static final String OUTPUT_CURRENT_FRAME_ID = "output_current_frame";
    
    // --- 渲染模式枚举 ---
    public enum RenderMode {
        GHOST_BLOCKS(0, "Ghost Blocks", "使用透明幽灵方块渲染"),
        PARTICLES(1, "Particles", "使用粒子效果渲染"),
        MIXED(2, "Mixed", "混合使用幽灵方块和粒子");
        
        private final int id;
        private final String name;

        RenderMode(int id, String name, String description) {
            this.id = id;
            this.name = name;
        }
        
        public static RenderMode fromId(int id) {
            for (RenderMode mode : values()) {
                if (mode.id == id) {
                    return mode;
                }
            }
            return GHOST_BLOCKS; // 默认渲染模式
        }
    }
    
    // 上一次渲染的方块列表（用于清除）
    private List<Object> lastRenderedBlocks = new ArrayList<>();
    
    // 最后更新时间戳
    private long lastUpdateTime = 0;
    
    // --- 构造函数 ---
    public AnimatedPreviewBlocksNode() {
        super(UUID.randomUUID(), "animation.output.animated_preview_blocks");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_ANIMATED_GEOMETRY_ID, "Animated Geometry", "动画几何体", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_ENABLED_ID, "Enabled", "启用渲染", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_FRAME_RATE_ID, "Frame Rate", "帧率 (FPS)", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_RENDER_DISTANCE_ID, "Render Distance", "渲染距离", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_RENDER_MODE_ID, "Render Mode", "渲染模式", NodeDataType.INTEGER, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_RENDER_STATUS_ID, "Render Status", "渲染状态", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_CURRENT_FRAME_ID, "Current Frame", "当前帧", NodeDataType.INTEGER, this));
    }
    
    @Override
    public String getDescription() {
        return "在游戏世界中实时渲染动画帧";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object geometryObj = inputValues.get(INPUT_ANIMATED_GEOMETRY_ID);
        Boolean enabled = (Boolean) inputValues.getOrDefault(INPUT_ENABLED_ID, true);
        Integer frameRate = (Integer) inputValues.getOrDefault(INPUT_FRAME_RATE_ID, 20);
        Integer renderDistance = (Integer) inputValues.getOrDefault(INPUT_RENDER_DISTANCE_ID, 64);
        Integer renderModeId = (Integer) inputValues.getOrDefault(INPUT_RENDER_MODE_ID, RenderMode.GHOST_BLOCKS.id);
        
        // 确保帧率在合理范围内
        frameRate = Math.max(1, Math.min(60, frameRate));
        
        // 确保渲染距离在合理范围内
        renderDistance = Math.max(16, Math.min(256, renderDistance));
        
        // 获取渲染模式
        RenderMode renderMode = RenderMode.fromId(renderModeId);
        
        // 处理渲染逻辑
        processRendering(context, geometryObj, enabled, frameRate, renderDistance, renderMode);
    }
    
    /**
     * 处理渲染逻辑
     */
    private void processRendering(ExecutionContext context, Object geometryObj, boolean enabled, 
                                int frameRate, int renderDistance, RenderMode renderMode) {
        // 默认状态
        String renderStatus;
        int currentFrame = 0;
        
        // 如果禁用渲染，清除已渲染的方块
        if (!enabled) {
            clearRenderedBlocks(context);
            renderStatus = "Disabled";
        } else if (geometryObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> geometryList = (List<Object>) geometryObj;
            
            // 检查是否需要更新帧（基于帧率）
            long currentTime = System.currentTimeMillis();
            long frameInterval = 1000 / frameRate;
            
            if (currentTime - lastUpdateTime >= frameInterval) {
                // 更新时间戳
                lastUpdateTime = currentTime;
                
                // 清除上一帧
                clearRenderedBlocks(context);
                
                // 渲染新一帧
                renderNewFrame(context, geometryList, renderDistance, renderMode);
                
                // 更新状态
                currentFrame = getFrameCounter();
                renderStatus = "Rendering at " + frameRate + " FPS";
            } else {
                // 当前帧不需要更新
                currentFrame = getFrameCounter();
                renderStatus = "Waiting for next frame";
            }
        } else {
            // 几何体格式错误
            clearRenderedBlocks(context);
            renderStatus = "Error: Invalid geometry format";
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_RENDER_STATUS_ID, renderStatus);
        outputValues.put(OUTPUT_CURRENT_FRAME_ID, currentFrame);
    }
    
    /**
     * 清除已渲染的方块
     */
    private void clearRenderedBlocks(ExecutionContext context) {
        // 这里将调用Minecraft API来移除已渲染的临时方块或粒子效果
        // 由于这是一个模拟实现，此处只清空上一帧方块列表
        
        if (context != null && !lastRenderedBlocks.isEmpty()) {
            // 在实际实现中，这里将向客户端发送清除方块的命令
            // 例如：context.clearTemporaryRenderedBlocks(lastRenderedBlocks);
            
            lastRenderedBlocks.clear();
        }
    }
    
    /**
     * 渲染新一帧
     */
    private void renderNewFrame(ExecutionContext context, List<Object> geometry, int renderDistance, RenderMode renderMode) {
        // 这里将调用Minecraft API来渲染新的临时方块或粒子效果
        // 由于这是一个模拟实现，此处只保存当前帧方块列表
        
        if (context != null && !geometry.isEmpty()) {
            // 筛选在渲染距离内的方块
            List<Object> blocksToRender = filterBlocksByDistance(context, geometry, renderDistance);
            
            // 在实际实现中，这里将向客户端发送渲染方块的命令
            // 根据渲染模式选择不同的渲染方法
            switch (renderMode) {
                case GHOST_BLOCKS:
                    // 渲染幽灵方块
                    // 例如：context.renderGhostBlocks(blocksToRender);
                    break;
                    
                case PARTICLES:
                    // 渲染粒子效果
                    // 例如：context.renderBlockParticles(blocksToRender);
                    break;
                    
                case MIXED:
                    // 混合渲染
                    // 例如：
                    // List<Object> solidBlocks = filterSolidBlocks(blocksToRender);
                    // List<Object> transparentBlocks = filterTransparentBlocks(blocksToRender);
                    // context.renderGhostBlocks(solidBlocks);
                    // context.renderBlockParticles(transparentBlocks);
                    break;
            }
            
            // 保存本次渲染的方块列表
            lastRenderedBlocks = new ArrayList<>(blocksToRender);
        }
    }
    
    /**
     * 筛选在渲染距离内的方块
     */
    private List<Object> filterBlocksByDistance(ExecutionContext context, List<Object> blocks, int maxDistance) {
        List<Object> result = new ArrayList<>();
        
        if (context != null) {
            // 获取玩家位置（在实际实现中）
            // float[] playerPos = context.getPlayerPosition();
            float[] playerPos = new float[]{0, 0, 0}; // 模拟实现
            
            // 筛选在渲染距离内的方块
            for (Object block : blocks) {
                float[] blockPos = getCoordinateAsFloatArray(block);
                if (blockPos != null) {
                    float distance = calculateDistance(playerPos, blockPos);
                    if (distance <= maxDistance) {
                        result.add(block);
                    }
                }
            }
        }
        
        return result;
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
     * 获取当前帧计数器
     */
    private int getFrameCounter() {
        // 在实际实现中，这可能是一个递增的帧计数器
        // 此处简单地返回基于当前时间的帧数
        return (int)((System.currentTimeMillis() / 1000) % 1000);
    }
    
    /**
     * 将坐标对象转换为浮点数组
     */
    private float[] getCoordinateAsFloatArray(Object coordObj) {
        // 处理int[]格式的坐标
        if (coordObj instanceof int[] coord) {
            if (coord.length >= 3) {
                return new float[]{(float) coord[0], (float) coord[1], (float) coord[2]};
            }
        }
        // 处理包含x,y,z字段的对象（如Coordinate类或MinecraftBlock）
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