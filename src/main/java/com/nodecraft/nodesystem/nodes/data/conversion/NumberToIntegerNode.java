package com.nodecraft.nodesystem.nodes.data.conversion;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Number to Integer 节点，将浮点数转换为整数，支持Floor、Round、Ceil模式
 */
@NodeInfo(
    id = "data.conversion.number_to_integer",
    displayName = "Number to Integer",
    description = "Converts a floating point number to an integer with selectable rounding mode",
    category = "data.conversion"
)
public class NumberToIntegerNode extends BaseNode {
    
    // --- 节点属性 ---
    public enum RoundingMode {
        FLOOR, ROUND, CEIL
    }
    
    private RoundingMode roundingMode = RoundingMode.ROUND; // 默认为四舍五入
    private String description; // 存储节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_NUMBER_ID = "input_number";
    private static final String OUTPUT_INTEGER_ID = "output_integer";
    
    /**
     * 构造一个新的数字转整数节点
     */
    public NumberToIntegerNode() {
        // 调用父类构造函数，使用UUID.randomUUID()生成新的ID
        super(UUID.randomUUID(), "data.conversion.number_to_integer");
        
        // 设置节点描述
        this.description = "Converts a floating point number to an integer with selectable rounding mode";
        
        // 创建输入端口
        IPort numberInput = new BasePort(INPUT_NUMBER_ID, "Number", 
                "The input number (float/double)", NodeDataType.DOUBLE, this);
        addInputPort(numberInput);
        
        // 创建输出端口
        IPort integerOutput = new BasePort(OUTPUT_INTEGER_ID, "Integer", 
                "The converted integer value", NodeDataType.INTEGER, this);
        addOutputPort(integerOutput);
    }
    
    /**
     * 实现INode接口的getDescription方法
     * @return 节点描述
     */
    @Override
    public String getDescription() {
        return this.description;
    }
    
    /**
     * 节点的计算逻辑
     * @param context 执行上下文
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入
        Object numberObj = inputValues.get(INPUT_NUMBER_ID);
        
        // 默认输出为0
        int result = 0;
        
        // 检查输入是否为数字
        if (numberObj instanceof Number) {
            double number = ((Number) numberObj).doubleValue();
            
            // 根据舍入模式进行转换
            switch (roundingMode) {
                case FLOOR:
                    result = (int) Math.floor(number);
                    break;
                case CEIL:
                    result = (int) Math.ceil(number);
                    break;
                case ROUND:
                default:
                    result = (int) Math.round(number);
                    break;
            }
        }
        
        // 设置输出
        outputValues.put(OUTPUT_INTEGER_ID, result);
    }
    
    // --- Getters/Setters for Properties ---
    
    public RoundingMode getRoundingMode() {
        return roundingMode;
    }
    
    public void setRoundingMode(RoundingMode mode) {
        this.roundingMode = mode;
        markDirty();
    }
    
    /**
     * 设置舍入模式（字符串形式，用于从UI或配置中设置）
     * @param modeStr 舍入模式字符串："FLOOR", "ROUND", "CEIL"
     */
    public void setRoundingModeString(String modeStr) {
        try {
            setRoundingMode(RoundingMode.valueOf(modeStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            // 如果字符串不匹配任何模式，使用默认的ROUND
            setRoundingMode(RoundingMode.ROUND);
        }
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("roundingMode", getRoundingMode().name());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("roundingMode")) {
                Object modeObj = stateMap.get("roundingMode");
                if (modeObj instanceof String) {
                    setRoundingModeString((String) modeObj);
                }
            }
        }
    }
} 