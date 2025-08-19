package com.nodecraft.nodesystem.nodes.flora.generators;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.LSystemRule;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * L-System Rule 节点: 创建单个L-系统规则
 */
@NodeInfo(
    id = "flora.generators.lsystem_rule",
    displayName = "L-System Rule",
    description = "Creates a single L-system rule for plant generation",
    category = "flora.generators"
)
public class LSystemRuleNode extends BaseNode {
    
    // --- 节点属性 ---
    private String symbol = "F";           // 规则应用的符号
    private String production = "F[+F]F[-F]F"; // 符号展开的字符串
    private float probability = 1.0f;      // 规则应用的概率
    private String context = "";           // 上下文敏感规则
    private String description = "定义一个L-系统中的生产规则";
    
    // --- 输入端口 IDs ---
    private static final String INPUT_SYMBOL_ID = "input_symbol";
    private static final String INPUT_PRODUCTION_ID = "input_production";
    private static final String INPUT_PROBABILITY_ID = "input_probability";
    private static final String INPUT_CONTEXT_ID = "input_context";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_RULE_ID = "output_rule";
    
    /**
     * 构造一个新的L-系统规则节点
     */
    public LSystemRuleNode() {
        super(UUID.randomUUID(), "flora.generators.l_system_rule");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_SYMBOL_ID, "Symbol", 
                "规则应用的符号（如 'F'）", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_PRODUCTION_ID, "Production", 
                "符号如何展开的字符串（如 'F[+F]F[-F]F'）", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_PROBABILITY_ID, "Probability", 
                "规则应用的概率（0-1，用于随机L-系统）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_CONTEXT_ID, "Context", 
                "L-系统上下文敏感规则（可选）", NodeDataType.STRING, this));
        
        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_RULE_ID, "L-System Rule", 
                "生成的L-系统规则", NodeDataType.L_SYSTEM_RULE, this));
    }
    
    @Override
    public String getDescription() {
        return this.description;
    }
    
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值，如果没有连接则使用默认值
        String symbolValue = getInputValue(INPUT_SYMBOL_ID, this.symbol);
        String productionValue = getInputValue(INPUT_PRODUCTION_ID, this.production);
        Float probabilityValue = getInputValue(INPUT_PROBABILITY_ID, this.probability);
        String contextValue = getInputValue(INPUT_CONTEXT_ID, this.context);
        
        // 验证输入
        if (symbolValue == null || symbolValue.trim().isEmpty()) {
            symbolValue = "F";
        }
        if (productionValue == null || productionValue.trim().isEmpty()) {
            productionValue = symbolValue; // 如果没有生产字符串，使用符号本身
        }
        if (probabilityValue == null) {
            probabilityValue = 1.0f;
        }
        // 确保概率在有效范围内
        probabilityValue = Math.max(0.0f, Math.min(1.0f, probabilityValue));
        
        // 创建L-系统规则
        LSystemRule rule = new LSystemRule(
            symbolValue.trim(),
            productionValue.trim(),
            probabilityValue,
            (contextValue != null && !contextValue.trim().isEmpty()) ? contextValue.trim() : null
        );
        
        // 设置输出
        outputValues.put(OUTPUT_RULE_ID, rule);
    }
    
    /**
     * 获取输入值的辅助方法
     */
    @SuppressWarnings("unchecked")
    private <T> T getInputValue(String portId, T defaultValue) {
        Object value = inputValues.get(portId);
        if (value != null) {
            try {
                return (T) value;
            } catch (ClassCastException e) {
                // 类型转换失败，返回默认值
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    // --- Getters and Setters ---
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol != null ? symbol : "F";
        markDirty();
    }
    
    public String getProduction() {
        return production;
    }
    
    public void setProduction(String production) {
        this.production = production != null ? production : "F";
        markDirty();
    }
    
    public float getProbability() {
        return probability;
    }
    
    public void setProbability(float probability) {
        this.probability = Math.max(0.0f, Math.min(1.0f, probability));
        markDirty();
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context != null ? context : "";
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("symbol", getSymbol());
        state.put("production", getProduction());
        state.put("probability", getProbability());
        state.put("context", getContext());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("symbol")) {
                Object symbolObj = stateMap.get("symbol");
                if (symbolObj instanceof String) {
                    setSymbol((String) symbolObj);
                }
            }
            
            if (stateMap.containsKey("production")) {
                Object productionObj = stateMap.get("production");
                if (productionObj instanceof String) {
                    setProduction((String) productionObj);
                }
            }
            
            if (stateMap.containsKey("probability")) {
                Object probabilityObj = stateMap.get("probability");
                if (probabilityObj instanceof Number) {
                    setProbability(((Number) probabilityObj).floatValue());
                }
            }
            
            if (stateMap.containsKey("context")) {
                Object contextObj = stateMap.get("context");
                if (contextObj instanceof String) {
                    setContext((String) contextObj);
                }
            }
        }
    }
} 