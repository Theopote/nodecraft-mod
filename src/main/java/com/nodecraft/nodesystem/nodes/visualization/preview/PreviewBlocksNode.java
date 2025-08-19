package com.nodecraft.nodesystem.nodes.visualization.preview;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.preview.PreviewManager;
import com.nodecraft.nodesystem.preview.PreviewOptions;
import com.nodecraft.nodesystem.util.Coordinate;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Preview Blocks 节点: 在世界中将输入的 List<Coordinate> 或 List<MinecraftBlock> 预览为"幽灵方块"
 */
@NodeInfo(
    id = "visualization.preview.preview_blocks",
    displayName = "预览方块",
    description = "在世界中将输入的 List<Coordinate> 或 List<MinecraftBlock> 预览为幽灵方块",
    category = "visualization.preview"
)
public class PreviewBlocksNode extends BaseNode {

    // --- 节点属性 ---
    private String previewColor = "#3498db"; // 默认颜色（天蓝色）
    private float transparency = 0.5f; // 默认透明度
    private int duration = 30; // 持续时间（秒）
    private boolean showOutline = true; // 是否显示方块轮廓
    private UUID previewId = UUID.randomUUID(); // 预览实例ID，用于清除预览
    private String description = "在世界中将输入的 List<Coordinate> 或 List<MinecraftBlock> 预览为幽灵方块";

    // --- 输入端口 IDs ---
    private static final String INPUT_BLOCKS_ID = "input_blocks";
    private static final String INPUT_COORDS_ID = "input_coords";
    private static final String INPUT_BLOCK_TYPE_ID = "input_block_type";
    private static final String INPUT_COLOR_ID = "input_color";
    private static final String INPUT_TRANSPARENCY_ID = "input_transparency";
    private static final String INPUT_DURATION_ID = "input_duration";
    private static final String INPUT_SHOW_OUTLINE_ID = "input_show_outline";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_PREVIEW_ID_ID = "output_preview_id";
    private static final String OUTPUT_BLOCK_COUNT_ID = "output_block_count";

    // --- 构造函数 ---
    public PreviewBlocksNode() {
        super(UUID.randomUUID(), "visualization.preview.preview_blocks");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_BLOCKS_ID, "Blocks", 
                "要预览的方块列表", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_COORDS_ID, "Coordinates", 
                "要预览方块的坐标列表", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_BLOCK_TYPE_ID, "Block Type", 
                "用于坐标列表的方块类型（若未指定，则使用'minecraft:stone'）", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_COLOR_ID, "Color", 
                "预览颜色（十六进制，如'#FF0000'）", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_TRANSPARENCY_ID, "Transparency", 
                "透明度（0.0-1.0）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_DURATION_ID, "Duration", 
                "预览持续时间（秒）", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_SHOW_OUTLINE_ID, "Show Outline", 
                "是否显示方块轮廓", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功显示预览", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_PREVIEW_ID_ID, "Preview ID", 
                "预览实例ID（用于后续控制）", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_BLOCK_COUNT_ID, "Block Count", 
                "预览的方块数量", NodeDataType.INTEGER, this));
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
        int blockCount = 0;
        
        // 获取输入值
        Object blocksObj = inputValues.get(INPUT_BLOCKS_ID);
        Object coordsObj = inputValues.get(INPUT_COORDS_ID);
        Object blockTypeObj = inputValues.get(INPUT_BLOCK_TYPE_ID);
        Object colorObj = inputValues.get(INPUT_COLOR_ID);
        Object transparencyObj = inputValues.get(INPUT_TRANSPARENCY_ID);
        Object durationObj = inputValues.get(INPUT_DURATION_ID);
        Object showOutlineObj = inputValues.get(INPUT_SHOW_OUTLINE_ID);
        
        // 确定预览颜色
        String previewColor = this.previewColor;
        if (colorObj instanceof String) {
            previewColor = (String) colorObj;
        }
        
        // 确定透明度
        float transparency = this.transparency;
        if (transparencyObj instanceof Number) {
            transparency = Math.max(0.0f, Math.min(1.0f, ((Number) transparencyObj).floatValue()));
        }
        
        // 确定持续时间
        int duration = this.duration;
        if (durationObj instanceof Number) {
            duration = Math.max(1, ((Number) durationObj).intValue());
        }
        
        // 确定是否显示轮廓
        boolean showOutline = this.showOutline;
        if (showOutlineObj instanceof Boolean) {
            showOutline = (Boolean) showOutlineObj;
        }
        
        // 确定默认方块类型
        String defaultBlockType = "minecraft:stone";
        if (blockTypeObj instanceof String) {
            defaultBlockType = (String) blockTypeObj;
        }
        
        // 处理输入：方块列表或坐标列表
        List<?> blocksList = null;
        List<?> coordsList = null;
        
        if (blocksObj instanceof List && !((List<?>) blocksObj).isEmpty()) {
            blocksList = (List<?>) blocksObj;
        }
        
        if (coordsObj instanceof List && !((List<?>) coordsObj).isEmpty()) {
            coordsList = (List<?>) coordsObj;
        }
        
        // 检查必要的输入是否存在
        if (blocksList != null || coordsList != null) {
            try {
                // 使用新的预览系统
                // 首先清除现有预览
                PreviewManager.hideNodePreviews(getId().toString());
                
                List<Coordinate> coordinates = new ArrayList<>();
                
                // 处理方块列表输入
                if (blocksList != null) {
                    for (Object obj : blocksList) {
                        // TODO: 处理 MinecraftBlock 类型
                            blockCount++;
                    }
                }
                
                // 处理坐标列表输入
                if (coordsList != null) {
                    for (Object obj : coordsList) {
                        if (obj instanceof Coordinate) {
                            coordinates.add((Coordinate) obj);
                            blockCount++;
                        }
                        // TODO: 处理其他坐标类型
                    }
                }
                
                // 应用预览
                if (!coordinates.isEmpty()) {
                    PreviewOptions options = new PreviewOptions()
                            .ghostBlockMode()
                            .setOpacity(transparency)
                            .setDuration(duration);
                    
                    if (previewColor != null && previewColor.startsWith("#")) {
                        // TODO: 解析颜色字符串并设置
                    }
                    
                    String newPreviewId = PreviewManager.showGhostBlocks(getId().toString(), coordinates, options);
                    if (newPreviewId != null) {
                    success = true;
                    }
                }
                
                // 模拟预览方块 (在实际实现中替换为上面的逻辑)
                if (blocksList != null) {
                    blockCount = blocksList.size();
                } else if (coordsList != null) {
                    blockCount = coordsList.size();
                }
                
                // 模拟成功预览
                success = blockCount > 0;
                
                // 打印调试信息
                System.out.println("模拟预览 " + blockCount + " 个方块，颜色: " + previewColor + 
                        "，透明度: " + transparency + "，持续: " + duration + " 秒" +
                        "，显示轮廓: " + showOutline);
            } catch (Exception e) {
                success = false;
                System.err.println("Error creating block preview: " + e.getMessage());
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_PREVIEW_ID_ID, previewIdStr);
        outputValues.put(OUTPUT_BLOCK_COUNT_ID, blockCount);
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
    
    public float getTransparency() {
        return transparency;
    }
    
    public void setTransparency(float transparency) {
        this.transparency = Math.max(0.0f, Math.min(1.0f, transparency));
        markDirty();
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = Math.max(1, duration);
        markDirty();
    }
    
    public boolean isShowOutline() {
        return showOutline;
    }
    
    public void setShowOutline(boolean showOutline) {
        this.showOutline = showOutline;
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
        Object[] state = new Object[5];
        state[0] = previewColor;
        state[1] = transparency;
        state[2] = duration;
        state[3] = showOutline;
        state[4] = previewId.toString();
        return state;
    }
    
    /**
     * 恢复节点状态
     */
    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof Object[]) {
            Object[] objState = (Object[]) state;
            if (objState.length >= 5) {
                if (objState[0] instanceof String) {
                    setPreviewColor((String) objState[0]);
                }
                if (objState[1] instanceof Number) {
                    setTransparency(((Number) objState[1]).floatValue());
                }
                if (objState[2] instanceof Number) {
                    setDuration(((Number) objState[2]).intValue());
                }
                if (objState[3] instanceof Boolean) {
                    setShowOutline((Boolean) objState[3]);
                }
                if (objState[4] instanceof String) {
                    try {
                        previewId = UUID.fromString((String) objState[4]);
                    } catch (IllegalArgumentException e) {
                        resetPreviewId();
                    }
                }
            }
        }
    }
} 