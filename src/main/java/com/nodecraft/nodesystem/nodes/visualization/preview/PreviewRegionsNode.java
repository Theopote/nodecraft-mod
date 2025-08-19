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
 * Preview Regions 节点: 预览 List<Region> 为线框或半透明体
 */
@NodeInfo(
    id = "visualization.preview.preview_regions",
    displayName = "预览区域",
    description = "预览 List<Region> 为线框或半透明体",
    category = "visualization.preview"
)
public class PreviewRegionsNode extends BaseNode {

    // --- 节点属性 ---
    private String regionColor = "#0000FF"; // 默认颜色（蓝色）
    private float transparency = 0.3f; // 透明度
    private float lineWidth = 0.1f; // 线框宽度
    private int duration = 30; // 持续时间（秒）
    private String displayMode = "both"; // 展示模式："wireframe"（线框）, "solid"（半透明）, "both"（两者）
    private UUID previewId = UUID.randomUUID(); // 预览实例ID
    private String description = "预览 List<Region> 为线框或半透明体";

    // --- 输入端口 IDs ---
    private static final String INPUT_REGIONS_ID = "input_regions";
    private static final String INPUT_COLOR_ID = "input_color";
    private static final String INPUT_TRANSPARENCY_ID = "input_transparency";
    private static final String INPUT_LINE_WIDTH_ID = "input_line_width";
    private static final String INPUT_DURATION_ID = "input_duration";
    private static final String INPUT_DISPLAY_MODE_ID = "input_display_mode";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_PREVIEW_ID_ID = "output_preview_id";
    private static final String OUTPUT_REGION_COUNT_ID = "output_region_count";
    private static final String OUTPUT_TOTAL_VOLUME_ID = "output_total_volume";

    // --- 构造函数 ---
    public PreviewRegionsNode() {
        super(UUID.randomUUID(), "visualization.preview.preview_regions");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_REGIONS_ID, "Regions", 
                "要预览的区域列表", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_COLOR_ID, "Color", 
                "区域颜色（十六进制，如'#0000FF'）", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_TRANSPARENCY_ID, "Transparency", 
                "透明度（0.0-1.0）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_LINE_WIDTH_ID, "Line Width", 
                "线框宽度", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_DURATION_ID, "Duration", 
                "预览持续时间（秒）", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_DISPLAY_MODE_ID, "Display Mode", 
                "展示模式（wireframe, solid, both）", NodeDataType.STRING, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功显示预览", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_PREVIEW_ID_ID, "Preview ID", 
                "预览实例ID（用于后续控制）", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_REGION_COUNT_ID, "Region Count", 
                "预览的区域数量", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_TOTAL_VOLUME_ID, "Total Volume", 
                "所有区域的总体积", NodeDataType.INTEGER, this));
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
        int regionCount = 0;
        int totalVolume = 0;
        
        // 获取输入值
        Object regionsObj = inputValues.get(INPUT_REGIONS_ID);
        Object colorObj = inputValues.get(INPUT_COLOR_ID);
        Object transparencyObj = inputValues.get(INPUT_TRANSPARENCY_ID);
        Object lineWidthObj = inputValues.get(INPUT_LINE_WIDTH_ID);
        Object durationObj = inputValues.get(INPUT_DURATION_ID);
        Object displayModeObj = inputValues.get(INPUT_DISPLAY_MODE_ID);
        
        // 确定区域颜色
        String regionColor = this.regionColor;
        if (colorObj instanceof String) {
            regionColor = (String) colorObj;
        }
        
        // 确定透明度
        float transparency = this.transparency;
        if (transparencyObj instanceof Number) {
            transparency = Math.max(0.0f, Math.min(1.0f, ((Number) transparencyObj).floatValue()));
        }
        
        // 确定线框宽度
        float lineWidth = this.lineWidth;
        if (lineWidthObj instanceof Number) {
            lineWidth = Math.max(0.01f, Math.min(0.5f, ((Number) lineWidthObj).floatValue()));
        }
        
        // 确定持续时间
        int duration = this.duration;
        if (durationObj instanceof Number) {
            duration = Math.max(1, ((Number) durationObj).intValue());
        }
        
        // 确定展示模式
        String displayMode = this.displayMode;
        if (displayModeObj instanceof String) {
            displayMode = (String) displayModeObj;
            if (!isValidDisplayMode(displayMode)) {
                displayMode = this.displayMode;
            }
        }
        
        // 处理输入：区域列表
        List<?> regionsList = null;
        
        if (regionsObj instanceof List && !((List<?>) regionsObj).isEmpty()) {
            regionsList = (List<?>) regionsObj;
        }
        
        // 检查必要的输入是否存在
        if (regionsList != null) {
            try {
                /*
                在实际实现中，需要根据Minecraft API实现以下操作:
                1. 清除当前预览ID的所有预览
                2. 处理输入数据，构建预览区域列表
                3. 应用预览效果
                
                // 首先清除现有预览（如果有）
                // PreviewManager.clearPreview(previewId);
                
                List<PreviewRegion> previewRegions = new ArrayList<>();
                
                // 处理区域列表
                for (Object obj : regionsList) {
                    if (obj instanceof Region) {
                        Region region = (Region) obj;
                        
                        // 计算区域的起点和终点坐标
                        BlockPos startPos = region.getMinPos();
                        BlockPos endPos = region.getMaxPos();
                        
                        // 计算体积
                        int volume = region.getVolume();
                        totalVolume += volume;
                        
                        previewRegions.add(new PreviewRegion(startPos, endPos, regionColor, transparency, lineWidth, displayMode));
                        regionCount++;
                    } else if (obj instanceof Box) {
                        // 如果是Box对象
                        Box box = (Box) obj;
                        BlockPos startPos = new BlockPos(box.minX, box.minY, box.minZ);
                        BlockPos endPos = new BlockPos(box.maxX, box.maxY, box.maxZ);
                        
                        // 计算体积
                        int volume = (int)((box.maxX - box.minX) * (box.maxY - box.minY) * (box.maxZ - box.minZ));
                        totalVolume += volume;
                        
                        previewRegions.add(new PreviewRegion(startPos, endPos, regionColor, transparency, lineWidth, displayMode));
                        regionCount++;
                    } else if (obj instanceof Object[]) {
                        // 假设是包含两个位置的数组 [startPos, endPos]
                        Object[] posArray = (Object[]) obj;
                        if (posArray.length >= 2) {
                            BlockPos startPos = null;
                            BlockPos endPos = null;
                            
                            if (posArray[0] instanceof BlockPos) {
                                startPos = (BlockPos) posArray[0];
                            }
                            
                            if (posArray[1] instanceof BlockPos) {
                                endPos = (BlockPos) posArray[1];
                            }
                            
                            if (startPos != null && endPos != null) {
                                // 确保startPos是最小坐标，endPos是最大坐标
                                BlockPos minPos = new BlockPos(
                                    Math.min(startPos.getX(), endPos.getX()),
                                    Math.min(startPos.getY(), endPos.getY()),
                                    Math.min(startPos.getZ(), endPos.getZ())
                                );
                                
                                BlockPos maxPos = new BlockPos(
                                    Math.max(startPos.getX(), endPos.getX()),
                                    Math.max(startPos.getY(), endPos.getY()),
                                    Math.max(startPos.getZ(), endPos.getZ())
                                );
                                
                                // 计算体积
                                int volume = (maxPos.getX() - minPos.getX() + 1) * 
                                           (maxPos.getY() - minPos.getY() + 1) * 
                                           (maxPos.getZ() - minPos.getZ() + 1);
                                totalVolume += volume;
                                
                                previewRegions.add(new PreviewRegion(minPos, maxPos, regionColor, transparency, lineWidth, displayMode));
                                regionCount++;
                            }
                        }
                    }
                }
                
                // 应用预览
                if (!previewRegions.isEmpty()) {
                    // PreviewManager.showRegions(previewId, previewRegions, duration);
                    success = true;
                }
                */
                
                // 模拟预览区域 (在实际实现中替换为上面的逻辑)
                regionCount = regionsList.size();
                totalVolume = regionCount * 100; // 假设每个区域平均体积为100个方块
                
                // 模拟成功预览
                success = regionCount > 0;
                
                // 打印调试信息
                System.out.println("模拟预览 " + regionCount + " 个区域，总体积: " + totalVolume + 
                        "，颜色: " + regionColor + "，透明度: " + transparency + 
                        "，线框宽度: " + lineWidth + "，持续: " + duration + " 秒" +
                        "，展示模式: " + displayMode);
            } catch (Exception e) {
                success = false;
                System.err.println("Error creating region preview: " + e.getMessage());
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_PREVIEW_ID_ID, previewIdStr);
        outputValues.put(OUTPUT_REGION_COUNT_ID, regionCount);
        outputValues.put(OUTPUT_TOTAL_VOLUME_ID, totalVolume);
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getRegionColor() {
        return regionColor;
    }
    
    public void setRegionColor(String regionColor) {
        // 简单验证十六进制颜色
        if (regionColor != null && regionColor.matches("^#[0-9A-Fa-f]{6}$")) {
            this.regionColor = regionColor;
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
    
    public float getLineWidth() {
        return lineWidth;
    }
    
    public void setLineWidth(float lineWidth) {
        this.lineWidth = Math.max(0.01f, Math.min(0.5f, lineWidth));
        markDirty();
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = Math.max(1, duration);
        markDirty();
    }
    
    public String getDisplayMode() {
        return displayMode;
    }
    
    public void setDisplayMode(String displayMode) {
        if (isValidDisplayMode(displayMode)) {
            this.displayMode = displayMode;
            markDirty();
        }
    }
    
    /**
     * 检查展示模式是否有效
     */
    private boolean isValidDisplayMode(String mode) {
        if (mode == null) return false;
        
        return "wireframe".equals(mode) || "solid".equals(mode) || "both".equals(mode);
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
        state[0] = regionColor;
        state[1] = transparency;
        state[2] = lineWidth;
        state[3] = duration;
        state[4] = displayMode;
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
                    setRegionColor((String) objState[0]);
                }
                if (objState[1] instanceof Number) {
                    setTransparency(((Number) objState[1]).floatValue());
                }
                if (objState[2] instanceof Number) {
                    setLineWidth(((Number) objState[2]).floatValue());
                }
                if (objState[3] instanceof Number) {
                    setDuration(((Number) objState[3]).intValue());
                }
                if (objState[4] instanceof String) {
                    setDisplayMode((String) objState[4]);
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