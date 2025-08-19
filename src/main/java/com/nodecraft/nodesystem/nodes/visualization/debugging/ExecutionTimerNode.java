package com.nodecraft.nodesystem.nodes.visualization.debugging;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Execution Timer 节点: 测量连接到此节点的计算分支所花费的时间
 */
@NodeInfo(
    id = "visualization.debugging.execution_timer",
    displayName = "执行计时器",
    description = "测量连接到此节点的计算分支所花费的时间",
    category = "visualization.debugging"
)
public class ExecutionTimerNode extends BaseNode {

    // --- 节点属性 ---
    private boolean autoReset = true; // 每次执行后是否自动重置
    private boolean showMilliseconds = true; // 是否显示毫秒
    private boolean printToConsole = false; // 是否打印到控制台
    private int precision = 2; // 浮点数精度
    
    // --- 内部状态 ---
    private long startTime = 0;
    private long endTime = 0;
    private long lastExecutionTime = 0;
    private long totalExecutionTime = 0;
    private int executionCount = 0;
    
    // --- 输入端口 IDs ---
    private static final String INPUT_START_ID = "input_start";
    private static final String INPUT_STOP_ID = "input_stop";
    private static final String INPUT_RESET_ID = "input_reset";
    private static final String INPUT_AUTO_RESET_ID = "input_auto_reset";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_EXECUTION_TIME_ID = "output_execution_time";
    private static final String OUTPUT_TOTAL_TIME_ID = "output_total_time";
    private static final String OUTPUT_AVERAGE_TIME_ID = "output_average_time";
    private static final String OUTPUT_COUNT_ID = "output_count";
    private static final String OUTPUT_FORMATTED_TIME_ID = "output_formatted_time";

    // --- 构造函数 ---
    public ExecutionTimerNode() {
        super(UUID.randomUUID(), "visualization.debugging.execution_timer");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_START_ID, "Start", 
                "开始计时", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_STOP_ID, "Stop", 
                "停止计时", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_RESET_ID, "Reset", 
                "重置计时器", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_AUTO_RESET_ID, "Auto Reset", 
                "是否在每次开始时自动重置", NodeDataType.BOOLEAN, this));
        
        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_EXECUTION_TIME_ID, "Execution Time", 
                "最近一次执行时间（毫秒）", NodeDataType.FLOAT, this));
        addOutputPort(new BasePort(OUTPUT_TOTAL_TIME_ID, "Total Time", 
                "累计执行时间（毫秒）", NodeDataType.FLOAT, this));
        addOutputPort(new BasePort(OUTPUT_AVERAGE_TIME_ID, "Average Time", 
                "平均执行时间（毫秒）", NodeDataType.FLOAT, this));
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Count", 
                "执行次数", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_FORMATTED_TIME_ID, "Formatted Time", 
                "格式化的时间字符串", NodeDataType.STRING, this));
        
        // 初始化输出值
        resetOutputs();
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object startObj = inputValues.get(INPUT_START_ID);
        Object stopObj = inputValues.get(INPUT_STOP_ID);
        Object resetObj = inputValues.get(INPUT_RESET_ID);
        Object autoResetObj = inputValues.get(INPUT_AUTO_RESET_ID);
        
        // 确定是否自动重置
        boolean autoReset = this.autoReset;
        if (autoResetObj instanceof Boolean) {
            autoReset = (Boolean) autoResetObj;
        }
        
        // 处理重置信号
        if (resetObj != null) {
            resetTimer();
        }
        
        // 处理开始信号
        if (startObj != null) {
            // 如果设置了自动重置，则在开始时重置
            if (autoReset) {
                startTime = System.currentTimeMillis();
                endTime = 0;
            } 
            // 如果未设置自动重置且未开始计时，则开始计时
            else if (startTime == 0) {
                startTime = System.currentTimeMillis();
            }
        }
        
        // 处理停止信号
        if (stopObj != null && startTime > 0) {
            endTime = System.currentTimeMillis();
            
            // 计算执行时间
            lastExecutionTime = endTime - startTime;
            totalExecutionTime += lastExecutionTime;
            executionCount++;
            
            // 如果启用了控制台打印
            if (printToConsole) {
                System.out.println(
                    "执行时间 [" + getDisplayName() + "]: " +
                    formatDuration(lastExecutionTime) + " (" +
                    formatDuration(totalExecutionTime / executionCount) + " avg)"
                );
            }
            
            // 重置开始时间，准备下一次测量
            startTime = 0;
        }
        
        // 更新输出值
        updateOutputs();
    }
    
    /**
     * 重置计时器
     */
    private void resetTimer() {
        startTime = 0;
        endTime = 0;
        lastExecutionTime = 0;
        totalExecutionTime = 0;
        executionCount = 0;
        resetOutputs();
    }
    
    /**
     * 重置输出值
     */
    private void resetOutputs() {
        outputValues.put(OUTPUT_EXECUTION_TIME_ID, 0.0f);
        outputValues.put(OUTPUT_TOTAL_TIME_ID, 0.0f);
        outputValues.put(OUTPUT_AVERAGE_TIME_ID, 0.0f);
        outputValues.put(OUTPUT_COUNT_ID, 0);
        outputValues.put(OUTPUT_FORMATTED_TIME_ID, "0 ms");
    }
    
    /**
     * 更新输出值
     */
    private void updateOutputs() {
        float executionTime = (float) lastExecutionTime;
        float totalTime = (float) totalExecutionTime;
        float avgTime = executionCount > 0 ? totalTime / executionCount : 0;
        
        outputValues.put(OUTPUT_EXECUTION_TIME_ID, executionTime);
        outputValues.put(OUTPUT_TOTAL_TIME_ID, totalTime);
        outputValues.put(OUTPUT_AVERAGE_TIME_ID, avgTime);
        outputValues.put(OUTPUT_COUNT_ID, executionCount);
        outputValues.put(OUTPUT_FORMATTED_TIME_ID, formatDuration(lastExecutionTime));
    }
    
    /**
     * 格式化持续时间为易读的字符串
     */
    private String formatDuration(long duration) {
        if (duration < 1000 || showMilliseconds) {
            return String.format("%." + precision + "f ms", (float) duration);
        } else {
            float seconds = duration / 1000.0f;
            return String.format("%." + precision + "f s", seconds);
        }
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isAutoReset() {
        return autoReset;
    }
    
    public void setAutoReset(boolean autoReset) {
        this.autoReset = autoReset;
        markDirty();
    }
    
    public boolean isShowMilliseconds() {
        return showMilliseconds;
    }
    
    public void setShowMilliseconds(boolean showMilliseconds) {
        this.showMilliseconds = showMilliseconds;
        markDirty();
    }
    
    public boolean isPrintToConsole() {
        return printToConsole;
    }
    
    public void setPrintToConsole(boolean printToConsole) {
        this.printToConsole = printToConsole;
        markDirty();
    }
    
    public int getPrecision() {
        return precision;
    }
    
    public void setPrecision(int precision) {
        this.precision = Math.max(0, Math.min(6, precision));
        markDirty();
    }
    
    /**
     * 获取最近一次执行时间
     */
    public long getLastExecutionTime() {
        return lastExecutionTime;
    }
    
    /**
     * 获取平均执行时间
     */
    public float getAverageExecutionTime() {
        return executionCount > 0 ? (float) totalExecutionTime / executionCount : 0;
    }
    
    /**
     * 获取累计执行时间
     */
    public long getTotalExecutionTime() {
        return totalExecutionTime;
    }
    
    /**
     * 获取执行次数
     */
    public int getExecutionCount() {
        return executionCount;
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        Object[] state = new Object[8];
        state[0] = autoReset;
        state[1] = showMilliseconds;
        state[2] = printToConsole;
        state[3] = precision;
        state[4] = startTime;
        state[5] = lastExecutionTime;
        state[6] = totalExecutionTime;
        state[7] = executionCount;
        return state;
    }
    
    /**
     * 恢复节点状态
     */
    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof Object[]) {
            Object[] objState = (Object[]) state;
            if (objState.length >= 8) {
                if (objState[0] instanceof Boolean) {
                    setAutoReset((Boolean) objState[0]);
                }
                if (objState[1] instanceof Boolean) {
                    setShowMilliseconds((Boolean) objState[1]);
                }
                if (objState[2] instanceof Boolean) {
                    setPrintToConsole((Boolean) objState[2]);
                }
                if (objState[3] instanceof Number) {
                    setPrecision(((Number) objState[3]).intValue());
                }
                if (objState[4] instanceof Number) {
                    startTime = ((Number) objState[4]).longValue();
                }
                if (objState[5] instanceof Number) {
                    lastExecutionTime = ((Number) objState[5]).longValue();
                }
                if (objState[6] instanceof Number) {
                    totalExecutionTime = ((Number) objState[6]).longValue();
                }
                if (objState[7] instanceof Number) {
                    executionCount = ((Number) objState[7]).intValue();
                }
                
                // 更新输出值
                updateOutputs();
            }
        }
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "测量连接到此节点的计算分支所花费的时间";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "Execution Timer";
    }
} 