package com.nodecraft.nodesystem.nodes.visualization.debugging;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Panel 节点: 显示连接到其输入端口的原始数据（文本形式）
 * 这是一个调试用核心节点，用于帮助用户理解数据流和节点状态
 */
@NodeInfo(
    id = "visualization.debugging.panel",
    displayName = "面板",
    description = "显示连接到其输入端口的原始数据（文本形式）",
    category = "visualization.debugging"
)
public class PanelNode extends BaseNode {

    // --- 节点属性 ---
    private String panelContent = ""; // 面板中显示的内容
    private boolean useFormatting = true; // 是否使用格式化显示
    private boolean autoRefresh = true; // 是否自动刷新
    private int maxDisplayLength = 2000; // 最大显示长度
    private boolean wrapText = true; // 是否自动换行
    
    // --- 输入端口 IDs ---
    private static final String INPUT_DATA_ID = "input_data";
    private static final String INPUT_FORMAT_ID = "input_format";
    private static final String INPUT_MAX_LENGTH_ID = "input_max_length";
    private static final String INPUT_REFRESH_ID = "input_refresh";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_TEXT_ID = "output_text";
    private static final String OUTPUT_TEXT_LENGTH_ID = "output_text_length";
    private static final String OUTPUT_DATA_TYPE_ID = "output_data_type";

    // --- 构造函数 ---
    public PanelNode() {
        super(UUID.randomUUID(), "visualization.debugging.panel");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_DATA_ID, "Data", 
                "要显示的数据（任意类型）", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_FORMAT_ID, "Use Formatting", 
                "是否使用格式化显示", NodeDataType.BOOLEAN, this));
        addInputPort(new BasePort(INPUT_MAX_LENGTH_ID, "Max Length", 
                "最大显示字符数", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_REFRESH_ID, "Refresh", 
                "刷新触发信号", NodeDataType.ANY, this));
        
        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_TEXT_ID, "Text", 
                "显示的文本内容", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_TEXT_LENGTH_ID, "Text Length", 
                "显示文本的长度", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_DATA_TYPE_ID, "Data Type", 
                "输入数据的类型", NodeDataType.STRING, this));
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "显示连接到其输入端口的原始数据（文本形式）";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "Panel";
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object dataObj = inputValues.get(INPUT_DATA_ID);
        Object formatObj = inputValues.get(INPUT_FORMAT_ID);
        Object maxLengthObj = inputValues.get(INPUT_MAX_LENGTH_ID);
        Object refreshObj = inputValues.get(INPUT_REFRESH_ID); // 仅用于触发刷新
        
        // 确定是否使用格式化
        boolean useFormatting = this.useFormatting;
        if (formatObj instanceof Boolean) {
            useFormatting = (Boolean) formatObj;
        }
        
        // 确定最大显示长度
        int maxDisplayLength = this.maxDisplayLength;
        if (maxLengthObj instanceof Number) {
            maxDisplayLength = Math.max(10, ((Number) maxLengthObj).intValue());
        }
        
        // 处理待显示数据
        String displayText = "";
        String dataType = "null";
        
        if (dataObj != null) {
            dataType = getDataTypeName(dataObj);
            displayText = formatDataToString(dataObj, useFormatting, maxDisplayLength);
        }
        
        // 更新面板内容
        panelContent = displayText;
        
        // 设置输出值
        outputValues.put(OUTPUT_TEXT_ID, displayText);
        outputValues.put(OUTPUT_TEXT_LENGTH_ID, displayText.length());
        outputValues.put(OUTPUT_DATA_TYPE_ID, dataType);
    }
    
    /**
     * 获取数据类型名称
     */
    private String getDataTypeName(Object obj) {
        if (obj == null) return "null";
        
        // 处理常见的Java集合和数组类型
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            return "List<" + (list.isEmpty() ? "?" : getDataTypeName(list.get(0))) + "> (size=" + list.size() + ")";
        }
        if (obj instanceof Object[]) {
            Object[] array = (Object[]) obj;
            return "Array<" + (array.length == 0 ? "?" : getDataTypeName(array[0])) + "> (length=" + array.length + ")";
        }
        if (obj instanceof String) {
            return "String (length=" + ((String) obj).length() + ")";
        }
        if (obj instanceof Number) {
            if (obj instanceof Integer) return "Integer";
            if (obj instanceof Long) return "Long";
            if (obj instanceof Float) return "Float";
            if (obj instanceof Double) return "Double";
            return "Number";
        }
        if (obj instanceof Boolean) {
            return "Boolean";
        }
        
        // 返回实际的类名
        return obj.getClass().getSimpleName();
    }
    
    /**
     * 将数据格式化为字符串
     */
    private String formatDataToString(Object data, boolean useFormatting, int maxLength) {
        if (data == null) {
            return "null";
        }
        
        StringBuilder sb = new StringBuilder();
        
        // 处理不同类型的数据
        if (data instanceof List) {
            formatList((List<?>) data, sb, useFormatting, 0, maxLength);
        } else if (data instanceof Object[]) {
            formatArray((Object[]) data, sb, useFormatting, 0, maxLength);
        } else if (data instanceof String) {
            formatString((String) data, sb, useFormatting, maxLength);
        } else {
            // 其他类型的数据直接调用toString()
            sb.append(data.toString());
            
            // 截断过长的文本
            if (sb.length() > maxLength) {
                sb.setLength(maxLength);
                sb.append("...");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 格式化列表
     */
    private void formatList(List<?> list, StringBuilder sb, boolean useFormatting, int indent, int maxLength) {
        if (list.isEmpty()) {
            sb.append("[]");
            return;
        }
        
        String indentStr = useFormatting ? "  ".repeat(indent) : "";
        String newLine = useFormatting ? "\n" : ", ";
        
        sb.append("[");
        if (useFormatting) sb.append(newLine);
        
        int count = 0;
        boolean reachedMax = false;
        
        for (Object item : list) {
            // 检查是否已经超过最大长度
            if (sb.length() > maxLength) {
                reachedMax = true;
                break;
            }
            
            if (count > 0) sb.append(",").append(newLine);
            if (useFormatting) sb.append(indentStr).append("  ");
            
            // 递归格式化子项
            if (item instanceof List) {
                formatList((List<?>) item, sb, useFormatting, indent + 1, maxLength);
            } else if (item instanceof Object[]) {
                formatArray((Object[]) item, sb, useFormatting, indent + 1, maxLength);
            } else if (item instanceof String) {
                formatString((String) item, sb, useFormatting, maxLength);
            } else {
                sb.append(item == null ? "null" : item.toString());
            }
            
            count++;
        }
        
        if (reachedMax) {
            sb.append(newLine).append(indentStr).append("  ...").append(newLine);
            sb.append(indentStr).append("] (truncated, total items: ").append(list.size()).append(")");
        } else {
            if (useFormatting) sb.append(newLine).append(indentStr);
            sb.append("]");
        }
    }
    
    /**
     * 格式化数组
     */
    private void formatArray(Object[] array, StringBuilder sb, boolean useFormatting, int indent, int maxLength) {
        List<Object> list = new ArrayList<>();
        for (Object item : array) {
            list.add(item);
        }
        formatList(list, sb, useFormatting, indent, maxLength);
    }
    
    /**
     * 格式化字符串
     */
    private void formatString(String str, StringBuilder sb, boolean useFormatting, int maxLength) {
        if (useFormatting) {
            sb.append("\"");
            
            // 处理转义字符
            String escapedStr = str
                    .replace("\\", "\\\\")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t")
                    .replace("\"", "\\\"");
            
            // 截断过长的字符串
            if (escapedStr.length() > maxLength - 5) {
                sb.append(escapedStr.substring(0, maxLength - 5)).append("...");
            } else {
                sb.append(escapedStr);
            }
            
            sb.append("\"");
        } else {
            // 非格式化模式，直接显示字符串
            if (str.length() > maxLength) {
                sb.append(str.substring(0, maxLength)).append("...");
            } else {
                sb.append(str);
            }
        }
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getPanelContent() {
        return panelContent;
    }
    
    public boolean isUseFormatting() {
        return useFormatting;
    }
    
    public void setUseFormatting(boolean useFormatting) {
        this.useFormatting = useFormatting;
        markDirty();
    }
    
    public boolean isAutoRefresh() {
        return autoRefresh;
    }
    
    public void setAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
        markDirty();
    }
    
    public int getMaxDisplayLength() {
        return maxDisplayLength;
    }
    
    public void setMaxDisplayLength(int maxDisplayLength) {
        this.maxDisplayLength = Math.max(10, maxDisplayLength);
        markDirty();
    }
    
    public boolean isWrapText() {
        return wrapText;
    }
    
    public void setWrapText(boolean wrapText) {
        this.wrapText = wrapText;
        markDirty();
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        Object[] state = new Object[4];
        state[0] = useFormatting;
        state[1] = autoRefresh;
        state[2] = maxDisplayLength;
        state[3] = wrapText;
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
                    setUseFormatting((Boolean) objState[0]);
                }
                if (objState[1] instanceof Boolean) {
                    setAutoRefresh((Boolean) objState[1]);
                }
                if (objState[2] instanceof Number) {
                    setMaxDisplayLength(((Number) objState[2]).intValue());
                }
                if (objState[3] instanceof Boolean) {
                    setWrapText((Boolean) objState[3]);
                }
            }
        }
    }
} 