package com.nodecraft.nodesystem.nodes.animation.output;

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
 * Animation Recorder Node: 动画记录器节点
 * 将整个动画序列导出为一系列文件，以便在游戏中重放
 */
@NodeInfo(
    id = "animation.output.animation_recorder",
    displayName = "Animation Recorder",
    description = "将动画序列导出为文件，用于重放",
    category = "animation.output"
)
public class AnimationRecorderNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_ANIMATED_GEOMETRY_ID = "input_animated_geometry";
    private static final String INPUT_RECORD_TRIGGER_ID = "input_record_trigger";
    private static final String INPUT_STOP_TRIGGER_ID = "input_stop_trigger";
    private static final String INPUT_FRAME_RATE_ID = "input_frame_rate";
    private static final String INPUT_OUTPUT_FORMAT_ID = "input_output_format";
    private static final String INPUT_OUTPUT_PATH_ID = "input_output_path";
    private static final String INPUT_PREFIX_ID = "input_prefix";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_STATUS_ID = "output_status";
    private static final String OUTPUT_FRAMES_CAPTURED_ID = "output_frames_captured";
    private static final String OUTPUT_LAST_FILE_PATH_ID = "output_last_file_path";
    
    // --- 输出格式枚举 ---
    public enum OutputFormat {
        SCHEMATIC(0, "Schematic", ".schem文件"),
        FUNCTION(1, "Function", "Minecraft函数文件"),
        COMMAND_BLOCK(2, "Command Block", "适用于命令方块的命令"),
        NBT(3, "NBT", "NBT格式结构文件");
        
        private final int id;
        private final String name;
        private final String description;
        
        OutputFormat(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
        
        public static OutputFormat fromId(int id) {
            for (OutputFormat format : values()) {
                if (format.id == id) {
                    return format;
                }
            }
            return SCHEMATIC; // 默认格式
        }
    }
    
    // 是否正在记录中
    private boolean isRecording = false;
    
    // 上次记录触发状态
    private boolean lastRecordState = false;
    
    // 上次停止触发状态
    private boolean lastStopState = false;
    
    // 已捕获的帧数
    private int framesCaptured = 0;
    
    // 缓存的帧（用于记录整个动画）
    private final List<List<Object>> frameCache = new ArrayList<>();
    
    // 上次记录的时间戳
    private long lastFrameTime = 0;
    
    // --- 构造函数 ---
    public AnimationRecorderNode() {
        super(UUID.randomUUID(), "animation.output.animation_recorder");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_ANIMATED_GEOMETRY_ID, "Animated Geometry", "动画几何体", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_RECORD_TRIGGER_ID, "Record", "开始记录（脉冲信号）", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_STOP_TRIGGER_ID, "Stop", "停止记录（脉冲信号）", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_FRAME_RATE_ID, "Frame Rate", "捕获帧率 (FPS)", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_OUTPUT_FORMAT_ID, "Output Format", "输出格式", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_OUTPUT_PATH_ID, "Output Path", "输出目录路径", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_PREFIX_ID, "File Prefix", "文件名前缀", NodeDataType.STRING, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_STATUS_ID, "Status", "记录状态", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_FRAMES_CAPTURED_ID, "Frames Captured", "已捕获帧数", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_LAST_FILE_PATH_ID, "Last File Path", "最后导出的文件路径", NodeDataType.STRING, this));
    }
    
    @Override
    public String getDescription() {
        return "将动画序列导出为文件，用于重放";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object geometryObj = inputValues.get(INPUT_ANIMATED_GEOMETRY_ID);
        Boolean recordTrigger = (Boolean) inputValues.getOrDefault(INPUT_RECORD_TRIGGER_ID, false);
        Boolean stopTrigger = (Boolean) inputValues.getOrDefault(INPUT_STOP_TRIGGER_ID, false);
        Integer frameRate = (Integer) inputValues.getOrDefault(INPUT_FRAME_RATE_ID, 20);
        Integer outputFormatId = (Integer) inputValues.getOrDefault(INPUT_OUTPUT_FORMAT_ID, OutputFormat.SCHEMATIC.id);
        String outputPath = (String) inputValues.getOrDefault(INPUT_OUTPUT_PATH_ID, "animations");
        String prefix = (String) inputValues.getOrDefault(INPUT_PREFIX_ID, "animation");
        
        // 确保帧率在合理范围内
        frameRate = Math.max(1, Math.min(60, frameRate));
        
        // 获取输出格式
        OutputFormat outputFormat = OutputFormat.fromId(outputFormatId);
        
        // 处理记录逻辑
        processRecording(context, geometryObj, recordTrigger, stopTrigger, frameRate, outputFormat, outputPath, prefix);
        
        // 更新触发器状态
        lastRecordState = recordTrigger;
        lastStopState = stopTrigger;
    }
    
    /**
     * 处理记录逻辑
     */
    private void processRecording(ExecutionContext context, Object geometryObj, boolean recordTrigger, boolean stopTrigger,
                                int frameRate, OutputFormat outputFormat, String outputPath, String prefix) {
        // 检查开始记录触发（上升沿触发）
        if (recordTrigger && !lastRecordState && !isRecording) {
            // 开始新的记录
            startRecording();
        }
        
        // 检查停止记录触发（上升沿触发）
        if (stopTrigger && !lastStopState && isRecording) {
            // 停止记录并导出
            stopRecordingAndExport(context, outputFormat, outputPath, prefix);
        }
        
        // 如果正在记录，且几何体有效，则捕获当前帧
        if (isRecording && geometryObj instanceof List) {
            // 计算基于帧率的捕获间隔
            long currentTime = System.currentTimeMillis();
            long frameInterval = 1000 / frameRate;
            
            if (currentTime - lastFrameTime >= frameInterval) {
                // 捕获当前帧
                @SuppressWarnings("unchecked")
                List<Object> geometryList = (List<Object>)geometryObj;
                captureFrame(geometryList);
                lastFrameTime = currentTime;
            }
        }
        
        // 设置输出值
        String status = isRecording ? "Recording: " + framesCaptured + " frames" : 
                       (framesCaptured > 0 ? "Ready to export" : "Idle");
        
        outputValues.put(OUTPUT_STATUS_ID, status);
        outputValues.put(OUTPUT_FRAMES_CAPTURED_ID, framesCaptured);
    }
    
    /**
     * 开始记录
     */
    private void startRecording() {
        // 清空缓存
        frameCache.clear();
        framesCaptured = 0;
        isRecording = true;
        lastFrameTime = System.currentTimeMillis();
    }
    
    /**
     * 捕获当前帧
     */
    private void captureFrame(List<Object> geometry) {
        // 创建当前帧的深复制
        List<Object> frameCopy = new ArrayList<>();
        for (Object block : geometry) {
            frameCopy.add(cloneBlock(block));
        }
        
        // 将帧添加到缓存
        frameCache.add(frameCopy);
        framesCaptured++;
    }
    
    /**
     * 停止记录并导出文件
     */
    private void stopRecordingAndExport(ExecutionContext context, OutputFormat format, String outputPath, String prefix) {
        isRecording = false;
        
        if (framesCaptured > 0) {
            // 执行导出（这里需要根据选择的格式导出到相应的文件）
            String exportedPath = exportAnimationFrames(context, format, outputPath, prefix);
            
            // 设置最后导出的文件路径
            outputValues.put(OUTPUT_LAST_FILE_PATH_ID, exportedPath);
        }
    }
    
    /**
     * 导出动画帧到文件
     */
    private String exportAnimationFrames(ExecutionContext context, OutputFormat format, String outputPath, String prefix) {
        // 这里是实际的导出逻辑
        // 根据不同的格式，导出到不同类型的文件
        
        // 在实际实现中，这里将调用Minecraft API来导出相应格式
        // 由于这是一个模拟实现，我们只返回假想的文件路径
        
        String baseFileName = prefix + "_" + System.currentTimeMillis();
        String fileExtension;
        
        switch (format) {
            case SCHEMATIC:
                fileExtension = ".schem";
                // 例如：context.exportToSchematic(frameCache, outputPath, baseFileName);
                break;
                
            case FUNCTION:
                fileExtension = ".mcfunction";
                // 例如：context.exportToFunction(frameCache, outputPath, baseFileName);
                break;
                
            case COMMAND_BLOCK:
                fileExtension = ".txt";
                // 例如：context.exportToCommandBlocks(frameCache, outputPath, baseFileName);
                break;
                
            case NBT:
                fileExtension = ".nbt";
                // 例如：context.exportToNBT(frameCache, outputPath, baseFileName);
                break;
                
            default:
                fileExtension = ".schem";
        }
        
        // 返回输出目录
        return outputPath + "/" + baseFileName + fileExtension;
    }
    
    /**
     * 克隆方块对象
     */
    private Object cloneBlock(Object block) {
        // 处理int[]格式的坐标
        if (block instanceof int[]) {
            int[] original = (int[]) block;
            int[] clone = new int[original.length];
            System.arraycopy(original, 0, clone, 0, original.length);
            return clone;
        }
        // 处理Map格式的方块（如MinecraftBlock）
        else if (block instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> original = (Map<String, Object>) block;
            return new HashMap<>(original);
        }
        // 其他类型无法克隆，直接返回原对象
        return block;
    }
} 