package com.nodecraft.nodesystem.nodes.animation.output;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Capture Frame Node: 捕获帧节点
 * 将动画某一帧的方块列表输出，用于进一步处理或烘焙
 */
@NodeInfo(
    id = "animation.output.capture_frame",
    displayName = "Capture Frame",
    description = "捕获动画特定帧的方块列表",
    category = "animation.output"
)
public class CaptureFrameNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_ANIMATED_GEOMETRY_ID = "input_animated_geometry";
    private static final String INPUT_TRIGGER_ID = "input_trigger";
    private static final String INPUT_FRAME_NAME_ID = "input_frame_name";
    private static final String INPUT_AUTO_CAPTURE_ID = "input_auto_capture";
    private static final String INPUT_CAPTURE_INTERVAL_ID = "input_capture_interval";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_CAPTURED_BLOCKS_ID = "output_captured_blocks";
    private static final String OUTPUT_FRAME_INFO_ID = "output_frame_info";
    private static final String OUTPUT_CAPTURE_COUNT_ID = "output_capture_count";
    
    // 捕获计数器
    private int captureCount = 0;
    
    // 上次捕获时间
    private long lastCaptureTime = 0;
    
    // 上次触发器状态
    private boolean lastTriggerState = false;
    
    // --- 构造函数 ---
    public CaptureFrameNode() {
        super(UUID.randomUUID(), "animation.output.capture_frame");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_ANIMATED_GEOMETRY_ID, "Animated Geometry", "动画几何体", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_TRIGGER_ID, "Trigger", "触发捕获（脉冲信号）", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_FRAME_NAME_ID, "Frame Name", "帧名称", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_AUTO_CAPTURE_ID, "Auto Capture", "自动捕获模式", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_CAPTURE_INTERVAL_ID, "Capture Interval", "自动捕获间隔（毫秒）", NodeDataType.INTEGER, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_CAPTURED_BLOCKS_ID, "Captured Blocks", "捕获的方块", NodeDataType.LIST, this));
        addOutputPort(new BasePort(OUTPUT_FRAME_INFO_ID, "Frame Info", "帧信息", NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_CAPTURE_COUNT_ID, "Capture Count", "捕获计数", NodeDataType.INTEGER, this));
    }
    
    @Override
    public String getDescription() {
        return "捕获动画特定帧的方块列表";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object geometryObj = inputValues.get(INPUT_ANIMATED_GEOMETRY_ID);
        Boolean trigger = (Boolean) inputValues.getOrDefault(INPUT_TRIGGER_ID, false);
        String frameName = (String) inputValues.getOrDefault(INPUT_FRAME_NAME_ID, "frame");
        Boolean autoCapture = (Boolean) inputValues.getOrDefault(INPUT_AUTO_CAPTURE_ID, false);
        Integer captureInterval = (Integer) inputValues.getOrDefault(INPUT_CAPTURE_INTERVAL_ID, 1000);
        
        // 确保捕获间隔在合理范围内
        captureInterval = Math.max(100, captureInterval);
        
        // 处理捕获逻辑
        processCapture(context, geometryObj, trigger, frameName, autoCapture, captureInterval);
        
        // 更新上次触发器状态
        lastTriggerState = trigger;
    }
    
    /**
     * 处理捕获逻辑
     */
    private void processCapture(ExecutionContext context, Object geometryObj, boolean trigger, 
                              String frameName, boolean autoCapture, int captureInterval) {
        // 检查是否应该捕获
        boolean shouldCapture = trigger && !lastTriggerState;
        
        // 检查触发器（上升沿触发）

        // 检查自动捕获
        if (autoCapture) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastCaptureTime >= captureInterval) {
                shouldCapture = true;
                lastCaptureTime = currentTime;
            }
        }
        
        // 如果需要捕获，并且几何体有效
        if (shouldCapture && geometryObj instanceof List) {
            // 捕获当前帧
            @SuppressWarnings("unchecked")
            List<Object> geometryList = (List<Object>) geometryObj;
            captureFrame(geometryList, frameName);
        }
        
        // 始终输出捕获计数
        outputValues.put(OUTPUT_CAPTURE_COUNT_ID, captureCount);
    }
    
    /**
     * 捕获当前帧
     */
    private void captureFrame(List<Object> geometry, String frameName) {
        // 增加捕获计数器
        captureCount++;
        
        // 创建帧名称（如果需要，添加计数器后缀）
        String finalFrameName = frameName;
        if (!finalFrameName.contains("%d")) {
            finalFrameName += "_%d";
        }
        finalFrameName = String.format(finalFrameName, captureCount);
        
        // 创建捕获的方块列表（深复制）
        List<Object> capturedBlocks = new ArrayList<>();
        for (Object block : geometry) {
            capturedBlocks.add(cloneBlock(block));
        }
        
        // 创建帧信息
        java.util.Map<String, Object> frameInfo = new java.util.HashMap<>();
        frameInfo.put("name", finalFrameName);
        frameInfo.put("timestamp", System.currentTimeMillis());
        frameInfo.put("blockCount", capturedBlocks.size());
        frameInfo.put("captureId", captureCount);
        
        // 设置输出值
        outputValues.put(OUTPUT_CAPTURED_BLOCKS_ID, capturedBlocks);
        outputValues.put(OUTPUT_FRAME_INFO_ID, frameInfo);
    }
    
    /**
     * 克隆方块对象
     */
    private Object cloneBlock(Object block) {
        // 处理int[]格式的坐标
        if (block instanceof int[] original) {
            int[] clone = new int[original.length];
            System.arraycopy(original, 0, clone, 0, original.length);
            return clone;
        }
        // 处理Map格式的方块（如MinecraftBlock）
        else if (block instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> original = (java.util.Map<String, Object>) block;
            return new java.util.HashMap<>(original);
        }
        // 其他类型无法克隆，直接返回原对象
        return block;
    }
} 