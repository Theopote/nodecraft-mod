package com.nodecraft.nodesystem.nodes.visualization.debugging;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Value Monitor 节点: 在画布上悬停显示连接端口的数据值
 * 此节点可以作为UI特性或独立节点使用
 */
@NodeInfo(
    id = "visualization.debugging.value_monitor",
    displayName = "值监视器",
    description = "在画布上悬停显示连接端口的数据值",
    category = "visualization.debugging"
)
public class ValueMonitorNode extends BaseNode {

    // --- 节点属性 ---
    private String displayText = ""; // 显示的文本
    private boolean showLabel = true; // 是否显示标签
    private boolean showType = true; // 是否显示类型
    private boolean compactView = false; // 是否使用紧凑视图
    private int refreshInterval = 500; // 刷新间隔（毫秒）
    private String backgroundColor = "#333333"; // 背景颜色
    private String textColor = "#FFFFFF"; // 文本颜色
    
    // --- 输入端口 IDs ---
    private static final String INPUT_VALUE_ID = "input_value";
    private static final String INPUT_LABEL_ID = "input_label";
    private static final String INPUT_SHOW_TYPE_ID = "input_show_type";
    private static final String INPUT_REFRESH_INTERVAL_ID = "input_refresh_interval";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_VALUE_ID = "output_value";
    private static final String OUTPUT_DISPLAY_TEXT_ID = "output_display_text";
    private static final String OUTPUT_TYPE_ID = "output_type";

    // --- 构造函数 ---
    public ValueMonitorNode() {
        super(UUID.randomUUID(), "visualization.debugging.value_monitor");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_VALUE_ID, "Value", 
                "要监视的值", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_LABEL_ID, "Label", 
                "显示值的标签", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_SHOW_TYPE_ID, "Show Type", 
                "是否显示类型信息", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_REFRESH_INTERVAL_ID, "Refresh Interval", 
                "刷新间隔（毫秒）", NodeDataType.INTEGER, this));
        
        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_VALUE_ID, "Value", 
                "监视的值（直接传递）", NodeDataType.ANY, this));
        addOutputPort(new BasePort(OUTPUT_DISPLAY_TEXT_ID, "Display Text", 
                "显示的文本", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_TYPE_ID, "Type", 
                "监视值的类型", NodeDataType.STRING, this));
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "在画布上悬停显示连接端口的数据值";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "Value Monitor";
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object valueObj = inputValues.get(INPUT_VALUE_ID);
        Object labelObj = inputValues.get(INPUT_LABEL_ID);
        Object showTypeObj = inputValues.get(INPUT_SHOW_TYPE_ID);
        Object refreshIntervalObj = inputValues.get(INPUT_REFRESH_INTERVAL_ID);
        
        // 确定是否显示类型
        boolean showType = this.showType;
        if (showTypeObj instanceof Boolean) {
            showType = (Boolean) showTypeObj;
        }
        
        // 确定刷新间隔
        int refreshInterval = this.refreshInterval;
        if (refreshIntervalObj instanceof Number) {
            refreshInterval = Math.max(100, ((Number) refreshIntervalObj).intValue());
        }
        
        // 确定标签
        String label = "";
        if (labelObj instanceof String) {
            label = (String) labelObj;
        }
        
        // 获取数据类型
        String typeName = getDataTypeName(valueObj);
        
        // 格式化显示文本
        String displayText = formatDisplayText(valueObj, label, typeName, showType);
        this.displayText = displayText;
        
        // 设置输出值
        outputValues.put(OUTPUT_VALUE_ID, valueObj); // 原样传递输入值
        outputValues.put(OUTPUT_DISPLAY_TEXT_ID, displayText);
        outputValues.put(OUTPUT_TYPE_ID, typeName);
        
        // 注意：在实际实现中，此节点可能需要特殊的UI渲染逻辑，以在画布上显示监视器框
        // 这些UI渲染代码通常不在节点的processNode方法中，而是在相关UI渲染类中
    }
    
    /**
     * 获取数据类型名称
     */
    private String getDataTypeName(Object obj) {
        if (obj == null) return "null";
        
        // 处理常见类型
        if (obj instanceof Iterable) {
            return "List";
        }
        if (obj instanceof Object[]) {
            return "Array";
        }
        if (obj instanceof String) {
            return "String";
        }
        if (obj instanceof Number) {
            if (obj instanceof Integer) return "Int";
            if (obj instanceof Long) return "Long";
            if (obj instanceof Float) return "Float";
            if (obj instanceof Double) return "Double";
            return "Number";
        }
        if (obj instanceof Boolean) {
            return "Boolean";
        }
        
        // 返回简化的类名
        return obj.getClass().getSimpleName();
    }
    
    /**
     * 格式化显示文本
     */
    private String formatDisplayText(Object value, String label, String typeName, boolean showType) {
        StringBuilder sb = new StringBuilder();
        
        // 添加标签（如果有）
        if (showLabel && !label.isEmpty()) {
            sb.append(label).append(": ");
        }
        
        // 添加类型（如果启用）
        if (showType) {
            sb.append("(").append(typeName).append(") ");
        }
        
        // 格式化值
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String) {
            String str = (String) value;
            if (compactView && str.length() > 30) {
                sb.append("\"").append(str.substring(0, 27)).append("...\"");
            } else {
                sb.append("\"").append(str).append("\"");
            }
        } else if (value instanceof Iterable) {
            if (compactView) {
                sb.append("[...]"); // 在紧凑模式下只显示占位符
            } else {
                // 简化的列表显示
                sb.append("[");
                Iterable<?> iterable = (Iterable<?>) value;
                boolean first = true;
                int count = 0;
                
                for (Object item : iterable) {
                    if (count >= 3) {
                        sb.append(", ...");
                        break;
                    }
                    
                    if (!first) {
                        sb.append(", ");
                    }
                    first = false;
                    
                    if (item instanceof String) {
                        sb.append("\"").append(item).append("\"");
                    } else {
                        sb.append(item);
                    }
                    count++;
                }
                
                sb.append("]");
            }
        } else if (value instanceof Object[]) {
            if (compactView) {
                sb.append("[...]"); // 在紧凑模式下只显示占位符
            } else {
                // 简化的数组显示
                sb.append("[");
                Object[] array = (Object[]) value;
                for (int i = 0; i < Math.min(3, array.length); i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    
                    Object item = array[i];
                    if (item instanceof String) {
                        sb.append("\"").append(item).append("\"");
                    } else {
                        sb.append(item);
                    }
                }
                
                if (array.length > 3) {
                    sb.append(", ...");
                }
                
                sb.append("]");
            }
        } else {
            // 其他类型直接转换为字符串
            sb.append(value);
        }
        
        return sb.toString();
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getDisplayText() {
        return displayText;
    }
    
    public boolean isShowLabel() {
        return showLabel;
    }
    
    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
        markDirty();
    }
    
    public boolean isShowType() {
        return showType;
    }
    
    public void setShowType(boolean showType) {
        this.showType = showType;
        markDirty();
    }
    
    public boolean isCompactView() {
        return compactView;
    }
    
    public void setCompactView(boolean compactView) {
        this.compactView = compactView;
        markDirty();
    }
    
    public int getRefreshInterval() {
        return refreshInterval;
    }
    
    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = Math.max(100, refreshInterval); // 最小100毫秒
        markDirty();
    }
    
    public String getBackgroundColor() {
        return backgroundColor;
    }
    
    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
        markDirty();
    }
    
    public String getTextColor() {
        return textColor;
    }
    
    public void setTextColor(String textColor) {
        this.textColor = textColor;
        markDirty();
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        Object[] state = new Object[6];
        state[0] = showLabel;
        state[1] = showType;
        state[2] = compactView;
        state[3] = refreshInterval;
        state[4] = backgroundColor;
        state[5] = textColor;
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
                if (objState[0] instanceof Boolean) {
                    setShowLabel((Boolean) objState[0]);
                }
                if (objState[1] instanceof Boolean) {
                    setShowType((Boolean) objState[1]);
                }
                if (objState[2] instanceof Boolean) {
                    setCompactView((Boolean) objState[2]);
                }
                if (objState[3] instanceof Number) {
                    setRefreshInterval(((Number) objState[3]).intValue());
                }
                if (objState[4] instanceof String) {
                    setBackgroundColor((String) objState[4]);
                }
                if (objState[5] instanceof String) {
                    setTextColor((String) objState[5]);
                }
            }
        }
    }
} 