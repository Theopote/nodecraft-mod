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
 * Preview Vectors 节点: 预览 List<Vector> 为带箭头的线
 */
@NodeInfo(
    id = "visualization.preview.preview_vectors",
    displayName = "预览向量",
    description = "预览 List<Vector> 为带箭头的线",
    category = "visualization.preview"
)
public class PreviewVectorsNode extends BaseNode {

    // --- 节点属性 ---
    private String lineColor = "#00FF00"; // 默认颜色（绿色）
    private float lineWidth = 0.1f; // 线宽度
    private float arrowSize = 0.3f; // 箭头大小
    private int duration = 30; // 持续时间（秒）
    private boolean showArrows = true; // 是否显示箭头
    private UUID previewId = UUID.randomUUID(); // 预览实例ID
    private String description = "预览 List<Vector> 为带箭头的线";

    // --- 输入端口 IDs ---
    private static final String INPUT_VECTORS_ID = "input_vectors";
    private static final String INPUT_START_POINTS_ID = "input_start_points";
    private static final String INPUT_COLOR_ID = "input_color";
    private static final String INPUT_LINE_WIDTH_ID = "input_line_width";
    private static final String INPUT_ARROW_SIZE_ID = "input_arrow_size";
    private static final String INPUT_DURATION_ID = "input_duration";
    private static final String INPUT_SHOW_ARROWS_ID = "input_show_arrows";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_PREVIEW_ID_ID = "output_preview_id";
    private static final String OUTPUT_VECTOR_COUNT_ID = "output_vector_count";

    // --- 构造函数 ---
    public PreviewVectorsNode() {
        super(UUID.randomUUID(), "visualization.preview.preview_vectors");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_VECTORS_ID, "Vectors", 
                "要预览的向量列表", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_START_POINTS_ID, "Start Points", 
                "向量起点（如果未提供，则默认为原点或从上一个向量末端开始）", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_COLOR_ID, "Color", 
                "线条颜色（十六进制，如'#00FF00'）", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_LINE_WIDTH_ID, "Line Width", 
                "线宽度", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_ARROW_SIZE_ID, "Arrow Size", 
                "箭头大小", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_DURATION_ID, "Duration", 
                "预览持续时间（秒）", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_SHOW_ARROWS_ID, "Show Arrows", 
                "是否显示箭头", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功显示预览", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_PREVIEW_ID_ID, "Preview ID", 
                "预览实例ID（用于后续控制）", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_VECTOR_COUNT_ID, "Vector Count", 
                "预览的向量数量", NodeDataType.INTEGER, this));
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
        int vectorCount = 0;
        
        // 获取输入值
        Object vectorsObj = inputValues.get(INPUT_VECTORS_ID);
        Object startPointsObj = inputValues.get(INPUT_START_POINTS_ID);
        Object colorObj = inputValues.get(INPUT_COLOR_ID);
        Object lineWidthObj = inputValues.get(INPUT_LINE_WIDTH_ID);
        Object arrowSizeObj = inputValues.get(INPUT_ARROW_SIZE_ID);
        Object durationObj = inputValues.get(INPUT_DURATION_ID);
        Object showArrowsObj = inputValues.get(INPUT_SHOW_ARROWS_ID);
        
        // 确定线条颜色
        String lineColor = this.lineColor;
        if (colorObj instanceof String) {
            lineColor = (String) colorObj;
        }
        
        // 确定线宽度
        float lineWidth = this.lineWidth;
        if (lineWidthObj instanceof Number) {
            lineWidth = Math.max(0.01f, Math.min(1.0f, ((Number) lineWidthObj).floatValue()));
        }
        
        // 确定箭头大小
        float arrowSize = this.arrowSize;
        if (arrowSizeObj instanceof Number) {
            arrowSize = Math.max(0.1f, Math.min(1.0f, ((Number) arrowSizeObj).floatValue()));
        }
        
        // 确定持续时间
        int duration = this.duration;
        if (durationObj instanceof Number) {
            duration = Math.max(1, ((Number) durationObj).intValue());
        }
        
        // 确定是否显示箭头
        boolean showArrows = this.showArrows;
        if (showArrowsObj instanceof Boolean) {
            showArrows = (Boolean) showArrowsObj;
        }
        
        // 处理输入：向量列表和起点列表
        List<?> vectorsList = null;
        List<?> startPointsList = null;
        
        if (vectorsObj instanceof List && !((List<?>) vectorsObj).isEmpty()) {
            vectorsList = (List<?>) vectorsObj;
        }
        
        if (startPointsObj instanceof List && !((List<?>) startPointsObj).isEmpty()) {
            startPointsList = (List<?>) startPointsObj;
        }
        
        // 检查必要的输入是否存在
        if (vectorsList != null) {
            try {
                /*
                在实际实现中，需要根据Minecraft API实现以下操作:
                1. 清除当前预览ID的所有预览
                2. 处理输入数据，构建预览向量列表
                3. 应用预览效果
                
                // 首先清除现有预览（如果有）
                // PreviewManager.clearPreview(previewId);
                
                List<PreviewVector> previewVectors = new ArrayList<>();
                
                // 处理向量列表和起点列表
                for (int i = 0; i < vectorsList.size(); i++) {
                    Object vectorObj = vectorsList.get(i);
                    Vec3d vector = null;
                    Vec3d startPoint = new Vec3d(0, 0, 0); // 默认起点为原点
                    
                    // 获取向量
                    if (vectorObj instanceof Vec3d) {
                        vector = (Vec3d) vectorObj;
                    }
                    
                    // 获取起点（如果有）
                    if (startPointsList != null && i < startPointsList.size()) {
                        Object startObj = startPointsList.get(i);
                        if (startObj instanceof Vec3d) {
                            startPoint = (Vec3d) startObj;
                        } else if (startObj instanceof BlockPos) {
                            BlockPos pos = (BlockPos) startObj;
                            startPoint = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                        } else if (startObj instanceof Coordinate) {
                            Coordinate coord = (Coordinate) startObj;
                            startPoint = new Vec3d(coord.getX(), coord.getY(), coord.getZ());
                        }
                    } else if (i > 0 && previewVectors.size() > 0) {
                        // 如果没有提供起点，且不是第一个向量，则使用上一个向量的终点作为起点
                        PreviewVector prevVector = previewVectors.get(i - 1);
                        startPoint = prevVector.getEndPoint();
                    }
                    
                    if (vector != null) {
                        // 创建预览向量
                        Vec3d endPoint = new Vec3d(
                            startPoint.x + vector.x,
                            startPoint.y + vector.y,
                            startPoint.z + vector.z
                        );
                        
                        previewVectors.add(new PreviewVector(startPoint, endPoint, lineColor, lineWidth, arrowSize, showArrows));
                        vectorCount++;
                    }
                }
                
                // 应用预览
                if (!previewVectors.isEmpty()) {
                    // PreviewManager.showVectors(previewId, previewVectors, duration);
                    success = true;
                }
                */
                
                // 模拟预览向量 (在实际实现中替换为上面的逻辑)
                vectorCount = vectorsList.size();
                
                // 模拟成功预览
                success = vectorCount > 0;
                
                // 打印调试信息
                System.out.println("模拟预览 " + vectorCount + " 个向量，颜色: " + lineColor + 
                        "，线宽: " + lineWidth + "，箭头大小: " + arrowSize +
                        "，持续: " + duration + " 秒，显示箭头: " + showArrows);
            } catch (Exception e) {
                success = false;
                System.err.println("Error creating vector preview: " + e.getMessage());
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_PREVIEW_ID_ID, previewIdStr);
        outputValues.put(OUTPUT_VECTOR_COUNT_ID, vectorCount);
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getLineColor() {
        return lineColor;
    }
    
    public void setLineColor(String lineColor) {
        // 简单验证十六进制颜色
        if (lineColor != null && lineColor.matches("^#[0-9A-Fa-f]{6}$")) {
            this.lineColor = lineColor;
            markDirty();
        }
    }
    
    public float getLineWidth() {
        return lineWidth;
    }
    
    public void setLineWidth(float lineWidth) {
        this.lineWidth = Math.max(0.01f, Math.min(1.0f, lineWidth));
        markDirty();
    }
    
    public float getArrowSize() {
        return arrowSize;
    }
    
    public void setArrowSize(float arrowSize) {
        this.arrowSize = Math.max(0.1f, Math.min(1.0f, arrowSize));
        markDirty();
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = Math.max(1, duration);
        markDirty();
    }
    
    public boolean isShowArrows() {
        return showArrows;
    }
    
    public void setShowArrows(boolean showArrows) {
        this.showArrows = showArrows;
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
        state[0] = lineColor;
        state[1] = lineWidth;
        state[2] = arrowSize;
        state[3] = duration;
        state[4] = showArrows;
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
                    setLineColor((String) objState[0]);
                }
                if (objState[1] instanceof Number) {
                    setLineWidth(((Number) objState[1]).floatValue());
                }
                if (objState[2] instanceof Number) {
                    setArrowSize(((Number) objState[2]).floatValue());
                }
                if (objState[3] instanceof Number) {
                    setDuration(((Number) objState[3]).intValue());
                }
                if (objState[4] instanceof Boolean) {
                    setShowArrows((Boolean) objState[4]);
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