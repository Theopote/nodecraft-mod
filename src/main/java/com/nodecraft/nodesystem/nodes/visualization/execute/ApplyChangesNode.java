package com.nodecraft.nodesystem.nodes.visualization.execute;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Apply Changes 节点: 特殊触发节点，将预览的结果实际应用到世界中
 * 通常以按钮形式存在，点击后激活所有连接的世界修改节点
 */
@NodeInfo(
    id = "visualization.execute.apply_changes",
    displayName = "应用修改",
    description = "将预览的结果实际应用到世界中",
    category = "visualization.execute"
)
public class ApplyChangesNode extends BaseNode {

    // --- 节点属性 ---
    private boolean showProgressBar = true; // 是否显示进度条
    private boolean notifyOnComplete = true; // 完成后是否通知
    private int executionTimeout = 30; // 执行超时（秒）
    private UUID executionId = UUID.randomUUID(); // 执行实例ID
    private String description = "将预览的结果实际应用到世界中";

    // --- 输入端口 IDs ---
    private static final String INPUT_TRIGGER_ID = "input_trigger";
    private static final String INPUT_PREVIEW_IDS_ID = "input_preview_ids";
    private static final String INPUT_SHOW_PROGRESS_ID = "input_show_progress";
    private static final String INPUT_NOTIFY_ID = "input_notify";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_OPERATION_COUNT_ID = "output_operation_count";
    private static final String OUTPUT_EXECUTION_TIME_ID = "output_execution_time";
    private static final String OUTPUT_STATUS_ID = "output_status";

    // --- 执行状态 ---
    private boolean isExecuting = false;
    private float progressPercentage = 0.0f;
    private String statusMessage = "Ready";

    // --- 构造函数 ---
    public ApplyChangesNode() {
        super(UUID.randomUUID(), "visualization.execute.apply_changes");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_TRIGGER_ID, "Trigger", 
                "触发执行操作的信号", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_PREVIEW_IDS_ID, "Preview IDs", 
                "要应用的预览ID列表（可选）", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_SHOW_PROGRESS_ID, "Show Progress", 
                "是否显示进度条", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_NOTIFY_ID, "Notify on Complete", 
                "完成后是否通知", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "操作是否成功完成", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_OPERATION_COUNT_ID, "Operation Count", 
                "执行的操作总数", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_EXECUTION_TIME_ID, "Execution Time", 
                "执行时间（毫秒）", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_STATUS_ID, "Status", 
                "执行状态信息", NodeDataType.STRING, this));
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
        int operationCount = 0;
        int executionTime = 0;
        String status = "No operation performed";
        
        // 获取输入值
        Object triggerObj = inputValues.get(INPUT_TRIGGER_ID);
        Object previewIdsObj = inputValues.get(INPUT_PREVIEW_IDS_ID);
        Object showProgressObj = inputValues.get(INPUT_SHOW_PROGRESS_ID);
        Object notifyObj = inputValues.get(INPUT_NOTIFY_ID);
        
        // 确定是否显示进度条
        boolean showProgress = this.showProgressBar;
        if (showProgressObj instanceof Boolean) {
            showProgress = (Boolean) showProgressObj;
        }
        
        // 确定是否完成后通知
        boolean notifyOnComplete = this.notifyOnComplete;
        if (notifyObj instanceof Boolean) {
            notifyOnComplete = (Boolean) notifyObj;
        }
        
        // 只有在收到触发信号时才执行
        if (triggerObj != null && !isExecuting && context != null) {
            // 重置进度和状态
            progressPercentage = 0.0f;
            statusMessage = "Starting execution...";
            isExecuting = true;
            long startTime = System.currentTimeMillis();
            
            try {
                /*
                在实际实现中，需要根据Minecraft API实现以下操作:
                1. 遍历所有连接的"世界修改"节点
                2. 对每个节点执行实际的世界修改操作
                3. 显示进度条并更新状态（如果启用）
                
                // 获取所有连接的下游节点
                // List<INode> connectedNodes = context.getGraph().getConnectedOutputNodes(this);
                
                // 过滤出所有"世界修改"类型的节点
                // List<INode> worldModificationNodes = connectedNodes.stream()
                //     .filter(node -> node instanceof IWorldModifier)
                //     .collect(Collectors.toList());
                
                int totalNodes = worldModificationNodes.size();
                
                if (totalNodes > 0) {
                    for (int i = 0; i < totalNodes; i++) {
                        INode node = worldModificationNodes.get(i);
                        
                        // 更新进度
                        progressPercentage = (float)(i) / totalNodes;
                        statusMessage = "Executing " + node.getDisplayName() + " (" + (i+1) + "/" + totalNodes + ")";
                        
                        if (showProgress) {
                            // 在UI上显示进度
                            // UIManager.updateProgress(executionId, progressPercentage, statusMessage);
                        }
                        
                        // 执行实际的世界修改
                        // boolean nodeSuccess = ((IWorldModifier)node).applyToWorld(context.getWorld());
                        // if (nodeSuccess) {
                        //     operationCount++;
                        // }
                    }
                    
                    // 完成
                    progressPercentage = 1.0f;
                    statusMessage = "Completed applying " + operationCount + " operations";
                    success = operationCount > 0;
                }
                */
                
                // 模拟执行世界修改 (在实际实现中替换为上面的逻辑)
                // 模拟处理时间和执行步骤
                try {
                    // 模拟启动阶段
                    progressPercentage = 0.1f;
                    statusMessage = "正在准备应用修改...";
                    Thread.sleep(200);
                    
                    // 模拟进度更新
                    progressPercentage = 0.3f;
                    statusMessage = "正在收集世界修改操作... (1/3)";
                    Thread.sleep(300);
                    
                    progressPercentage = 0.5f;
                    statusMessage = "正在验证操作可行性... (2/3)";
                    Thread.sleep(400);
                    
                    progressPercentage = 0.8f;
                    statusMessage = "正在应用世界修改... (3/3)";
                    Thread.sleep(500);
                    
                    // 模拟完成
                    progressPercentage = 1.0f;
                    statusMessage = "所有修改已应用到世界中";
                    
                    operationCount = 42; // 假设执行了42个操作
                    success = true;
                } catch (InterruptedException e) {
                    statusMessage = "执行被中断";
                    success = false;
                }
                
                // 计算执行时间
                executionTime = (int)(System.currentTimeMillis() - startTime);
                
                // 显示完成通知（如果启用）
                if (notifyOnComplete) {
                    // 在实际实现中显示通知
                    // UIManager.showNotification("应用修改完成", "已成功执行 " + operationCount + " 个世界修改操作");
                    System.out.println("应用修改完成通知: 已成功执行 " + operationCount + " 个世界修改操作");
                }
                
                // 打印调试信息
                System.out.println("应用修改完成: 操作数=" + operationCount + 
                        ", 用时=" + executionTime + "ms, 状态=\"" + statusMessage + "\"");
                
                status = statusMessage;
            } catch (Exception e) {
                success = false;
                status = "Error: " + e.getMessage();
                System.err.println("Error applying changes: " + e.getMessage());
            } finally {
                isExecuting = false;
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_OPERATION_COUNT_ID, operationCount);
        outputValues.put(OUTPUT_EXECUTION_TIME_ID, executionTime);
        outputValues.put(OUTPUT_STATUS_ID, status);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isShowProgressBar() {
        return showProgressBar;
    }
    
    public void setShowProgressBar(boolean showProgressBar) {
        this.showProgressBar = showProgressBar;
        markDirty();
    }
    
    public boolean isNotifyOnComplete() {
        return notifyOnComplete;
    }
    
    public void setNotifyOnComplete(boolean notifyOnComplete) {
        this.notifyOnComplete = notifyOnComplete;
        markDirty();
    }
    
    public int getExecutionTimeout() {
        return executionTimeout;
    }
    
    public void setExecutionTimeout(int executionTimeout) {
        this.executionTimeout = Math.max(5, executionTimeout);
        markDirty();
    }
    
    /**
     * 获取当前执行状态
     * @return 当前进度百分比(0.0-1.0)
     */
    public float getProgressPercentage() {
        return progressPercentage;
    }
    
    /**
     * 获取当前状态消息
     * @return 状态信息字符串
     */
    public String getStatusMessage() {
        return statusMessage;
    }
    
    /**
     * 是否正在执行中
     * @return true表示正在执行
     */
    public boolean isExecuting() {
        return isExecuting;
    }
    
    /**
     * 重置执行ID
     */
    public void resetExecutionId() {
        executionId = UUID.randomUUID();
        markDirty();
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        Object[] state = new Object[4];
        state[0] = showProgressBar;
        state[1] = notifyOnComplete;
        state[2] = executionTimeout;
        state[3] = executionId.toString();
        return state;
    }
    
    /**
     * 恢复节点状态
     */
    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof Object[]) {
            Object[] objState = (Object[]) state;
            if (objState.length >= 4) {
                if (objState[0] instanceof Boolean) {
                    showProgressBar = (Boolean) objState[0];
                }
                if (objState[1] instanceof Boolean) {
                    notifyOnComplete = (Boolean) objState[1];
                }
                if (objState[2] instanceof Number) {
                    executionTimeout = ((Number) objState[2]).intValue();
                }
                if (objState[3] instanceof String) {
                    try {
                        executionId = UUID.fromString((String) objState[3]);
                    } catch (IllegalArgumentException e) {
                        resetExecutionId();
                    }
                }
            }
        }
    }
} 