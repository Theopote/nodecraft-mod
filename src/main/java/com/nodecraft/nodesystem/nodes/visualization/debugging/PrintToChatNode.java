package com.nodecraft.nodesystem.nodes.visualization.debugging;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Print to Chat 节点: 将输入数据显示到游戏聊天框
 */
@NodeInfo(
    id = "visualization.debugging.print_to_chat",
    displayName = "打印到聊天",
    description = "将输入数据显示到游戏聊天框",
    category = "visualization.debugging"
)
public class PrintToChatNode extends BaseNode {

    // --- 节点属性 ---
    private String prefix = "[Debug] "; // 消息前缀
    private boolean includeNodeName = true; // 是否包含节点名称
    private boolean includeDataType = true; // 是否包含数据类型信息
    private boolean autoFormat = true; // 是否自动格式化复杂数据
    private String textColor = "gold"; // 文本颜色
    
    // --- 输入端口 IDs ---
    private static final String INPUT_DATA_ID = "input_data";
    private static final String INPUT_PREFIX_ID = "input_prefix";
    private static final String INPUT_COLOR_ID = "input_color";
    private static final String INPUT_TRIGGER_ID = "input_trigger";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_MESSAGE_ID = "output_message";

    // --- 构造函数 ---
    public PrintToChatNode() {
        super(UUID.randomUUID(), "visualization.debugging.print_to_chat");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_DATA_ID, "Data", 
                "要打印的数据", NodeDataType.ANY, this));
        addInputPort(new BasePort(INPUT_PREFIX_ID, "Prefix", 
                "消息前缀", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_COLOR_ID, "Color", 
                "文本颜色", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_TRIGGER_ID, "Trigger", 
                "触发打印信号", NodeDataType.ANY, this));
        
        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功打印", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_MESSAGE_ID, "Message", 
                "打印的消息内容", NodeDataType.STRING, this));
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "将输入数据显示到游戏聊天框";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "Print to Chat";
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 默认输出值
        boolean success = false;
        String message = "";
        
        // 获取输入值
        Object dataObj = inputValues.get(INPUT_DATA_ID);
        Object prefixObj = inputValues.get(INPUT_PREFIX_ID);
        Object colorObj = inputValues.get(INPUT_COLOR_ID);
        Object triggerObj = inputValues.get(INPUT_TRIGGER_ID); // 仅作为触发器
        
        // 检查是否有触发信号和玩家环境
        if (context != null && context.getPlayerAccessor() != null) {
            try {
                // 确定前缀
                String prefix = this.prefix;
                if (prefixObj instanceof String) {
                    prefix = (String) prefixObj;
                }
                
                // 确定颜色
                String color = this.textColor;
                if (colorObj instanceof String) {
                    color = (String) colorObj;
                }
                
                // 构建消息
                StringBuilder messageSb = new StringBuilder();
                messageSb.append(prefix);
                
                // 添加节点名称（如果启用）
                if (includeNodeName) {
                    messageSb.append(getDisplayName()).append(": ");
                }
                
                // 添加数据类型（如果启用）和数据
                String dataStr = formatData(dataObj);
                
                if (includeDataType && dataObj != null) {
                    messageSb.append("(").append(getDataTypeName(dataObj)).append(") ");
                }
                
                messageSb.append(dataStr);
                message = messageSb.toString();
                
                // 在Minecraft中发送消息
                /*
                在实际实现中，需要根据Minecraft API实现:
                context.getPlayerAccessor().sendMessage(message, color);
                */
                
                // 模拟发送消息
                System.out.println("模拟发送聊天消息: 颜色=" + color + ", 内容=\"" + message + "\"");
                success = true;
            } catch (Exception e) {
                System.err.println("Error printing to chat: " + e.getMessage());
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_MESSAGE_ID, message);
    }
    
    /**
     * 获取数据类型名称
     */
    private String getDataTypeName(Object obj) {
        if (obj == null) return "null";
        
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
            if (obj instanceof Float) return "Float";
            if (obj instanceof Double) return "Double";
            return "Number";
        }
        if (obj instanceof Boolean) {
            return "Bool";
        }
        
        // 返回简化的类名
        String className = obj.getClass().getSimpleName();
        return className.isEmpty() ? obj.getClass().getName() : className;
    }
    
    /**
     * 格式化数据为适合聊天显示的字符串
     */
    private String formatData(Object data) {
        if (data == null) {
            return "null";
        }
        
        // 处理不同类型的数据
        if (data instanceof Iterable) {
            if (autoFormat) {
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                
                boolean first = true;
                int count = 0;
                for (Object item : (Iterable<?>) data) {
                    if (count >= 5) {
                        sb.append(", ...");
                        break;
                    }
                    
                    if (!first) {
                        sb.append(", ");
                    }
                    first = false;
                    
                    sb.append(formatData(item));
                    count++;
                }
                
                sb.append("]");
                return sb.toString();
            } else {
                return data.toString();
            }
        } else if (data instanceof Object[]) {
            if (autoFormat) {
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                
                Object[] array = (Object[]) data;
                for (int i = 0; i < Math.min(5, array.length); i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(formatData(array[i]));
                }
                
                if (array.length > 5) {
                    sb.append(", ...");
                }
                
                sb.append("]");
                return sb.toString();
            } else {
                return data.toString();
            }
        } else if (data instanceof String) {
            if (autoFormat) {
                String str = (String) data;
                if (str.length() > 100) {
                    return "\"" + str.substring(0, 97) + "...\"";
                } else {
                    return "\"" + str + "\"";
                }
            } else {
                return (String) data;
            }
        } else {
            // 其他类型直接转换为字符串
            return data.toString();
        }
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getPrefix() {
        return prefix;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
        markDirty();
    }
    
    public boolean isIncludeNodeName() {
        return includeNodeName;
    }
    
    public void setIncludeNodeName(boolean includeNodeName) {
        this.includeNodeName = includeNodeName;
        markDirty();
    }
    
    public boolean isIncludeDataType() {
        return includeDataType;
    }
    
    public void setIncludeDataType(boolean includeDataType) {
        this.includeDataType = includeDataType;
        markDirty();
    }
    
    public boolean isAutoFormat() {
        return autoFormat;
    }
    
    public void setAutoFormat(boolean autoFormat) {
        this.autoFormat = autoFormat;
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
        Object[] state = new Object[5];
        state[0] = prefix;
        state[1] = includeNodeName;
        state[2] = includeDataType;
        state[3] = autoFormat;
        state[4] = textColor;
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
                    setPrefix((String) objState[0]);
                }
                if (objState[1] instanceof Boolean) {
                    setIncludeNodeName((Boolean) objState[1]);
                }
                if (objState[2] instanceof Boolean) {
                    setIncludeDataType((Boolean) objState[2]);
                }
                if (objState[3] instanceof Boolean) {
                    setAutoFormat((Boolean) objState[3]);
                }
                if (objState[4] instanceof String) {
                    setTextColor((String) objState[4]);
                }
            }
        }
    }
} 