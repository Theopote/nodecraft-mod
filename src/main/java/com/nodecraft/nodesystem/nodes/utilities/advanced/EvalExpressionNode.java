package com.nodecraft.nodesystem.nodes.utilities.advanced;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.Bindings;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.UUID;

/**
 * Eval Expression 节点: 计算数学表达式
 * 支持变量替换和复杂的数学运算
 */
@NodeInfo(
    id = "utilities.advanced.eval_expression",
    displayName = "表达式计算",
    description = "计算数学表达式，支持变量替换",
    category = "utilities.advanced"
)
public class EvalExpressionNode extends BaseNode {

    // --- 节点属性 ---
    private String expression = "a + b * 2"; // 默认表达式
    private boolean autoEvaluate = false; // 输入变化时自动计算
    private String description = "计算表达式字符串，根据提供的变量计算结果";
    
    // --- 输入端口 IDs ---
    private static final String INPUT_EXPRESSION_ID = "input_expression";
    private static final String INPUT_VARIABLES_ID = "input_variables";
    private static final String INPUT_EVALUATE_ID = "input_evaluate";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_RESULT_ID = "output_result";
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_ERROR_ID = "output_error";
    
    // --- 脚本引擎 ---
    private transient ScriptEngineManager engineManager;
    private transient ScriptEngine jsEngine;
    
    // --- 上次输入 ---
    private transient Map<String, Object> lastInputs = new HashMap<>();
    
    /**
     * 构造表达式求值节点
     */
    public EvalExpressionNode() {
        super(UUID.randomUUID(), "utilities.advanced.eval_expression");
        
        // 创建脚本引擎管理器
        engineManager = new ScriptEngineManager();
        
        // 创建JavaScript引擎（用于表达式求值）
        jsEngine = engineManager.getEngineByName("nashorn");
        if (jsEngine == null) {
            jsEngine = engineManager.getEngineByName("JavaScript");
        }
        
        // 创建输入端口
        addInputPort(new BasePort(INPUT_EXPRESSION_ID, "Expression", 
                "要计算的表达式", NodeDataType.STRING, this));
        
        addInputPort(new BasePort(INPUT_VARIABLES_ID, "Variables", 
                "表达式中使用的变量Map", NodeDataType.ANY, this));
        
        addInputPort(new BasePort(INPUT_EVALUATE_ID, "Evaluate", 
                "触发表达式计算", NodeDataType.ANY, this));
        
        // 创建输出端口
        addOutputPort(new BasePort(OUTPUT_RESULT_ID, "Result", 
                "表达式计算结果", NodeDataType.ANY, this));
        
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "表达式是否成功计算", NodeDataType.BOOLEAN, this));
        
        addOutputPort(new BasePort(OUTPUT_ERROR_ID, "Error", 
                "计算错误信息", NodeDataType.STRING, this));
    }
    
    /**
     * 节点的计算逻辑
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 初始化输出值
        Object result = null;
        boolean success = false;
        String errorMessage = "";
        
        // 获取输入
        Object expressionObj = inputValues.get(INPUT_EXPRESSION_ID);
        Object variablesObj = inputValues.get(INPUT_VARIABLES_ID);
        Object evaluateObj = inputValues.get(INPUT_EVALUATE_ID);
        
        // 获取表达式字符串
        String expressionToEval = this.expression;
        if (expressionObj instanceof String && !((String) expressionObj).isEmpty()) {
            expressionToEval = (String) expressionObj;
        }
        
        // 检查是否需要计算表达式
        boolean shouldEvaluate = evaluateObj != null;
        
        // 如果设置了自动计算，检查输入是否变化
        if (autoEvaluate && !shouldEvaluate) {
            Map<String, Object> currentInputs = new HashMap<>(inputValues);
            if (!currentInputs.equals(lastInputs)) {
                shouldEvaluate = true;
                // 保存当前输入以便比较
                lastInputs = new HashMap<>(currentInputs);
            }
        }
        
        if (shouldEvaluate && !expressionToEval.isEmpty()) {
            // 获取变量Map
            Map<String, Object> variables = new HashMap<>();
            
            // 如果提供了变量，且是Map类型，则使用它
            if (variablesObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<Object, Object> varMap = (Map<Object, Object>) variablesObj;
                for (Map.Entry<Object, Object> entry : varMap.entrySet()) {
                    if (entry.getKey() != null) {
                        variables.put(entry.getKey().toString(), entry.getValue());
                    }
                }
            }
            
            // 从输入中获取其他变量
            for (Map.Entry<String, Object> entry : inputValues.entrySet()) {
                String key = entry.getKey();
                if (!key.equals(INPUT_EXPRESSION_ID) && 
                    !key.equals(INPUT_VARIABLES_ID) && 
                    !key.equals(INPUT_EVALUATE_ID)) {
                    variables.put(key, entry.getValue());
                }
            }
            
            // 尝试使用JavaScript引擎计算表达式
            if (jsEngine != null) {
                try {
                    // 创建绑定
                    Bindings bindings = jsEngine.createBindings();
                    
                    // 将变量添加到绑定
                    for (Map.Entry<String, Object> entry : variables.entrySet()) {
                        bindings.put(entry.getKey(), entry.getValue());
                    }
                    
                    // 计算表达式
                    result = jsEngine.eval(expressionToEval, bindings);
                    success = true;
                } catch (ScriptException e) {
                    errorMessage = "表达式计算错误: " + e.getMessage();
                } catch (Exception e) {
                    errorMessage = "计算表达式时发生错误: " + e.getMessage();
                }
            } else {
                // 脚本引擎不可用时，使用简单算术解析器
                try {
                    // 提取表达式中的变量并替换为实际值
                    String preparedExpression = expressionToEval;
                    
                    // 找出表达式中的所有变量名称
                    Pattern variablePattern = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
                    Matcher matcher = variablePattern.matcher(expressionToEval);
                    
                    while (matcher.find()) {
                        String varName = matcher.group();
                        if (variables.containsKey(varName)) {
                            Object value = variables.get(varName);
                            // 替换变量名为其值
                            if (value instanceof Number) {
                                preparedExpression = preparedExpression.replace(
                                    varName, value.toString());
                            } else if (value instanceof String) {
                                preparedExpression = preparedExpression.replace(
                                    varName, "\"" + value.toString() + "\"");
                            }
                            // 其他类型暂不支持
                        }
                    }
                    
                    // 计算简单算术表达式（非常基础的实现，只支持简单算术）
                    result = evaluateSimpleExpression(preparedExpression);
                    success = true;
                } catch (Exception e) {
                    errorMessage = "简单表达式计算错误: " + e.getMessage();
                }
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_RESULT_ID, result);
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_ERROR_ID, errorMessage);
    }
    
    /**
     * 简单表达式求值（非常基础，仅支持简单算术）
     * 注意：这是一个非常简化的实现，只能处理基本的算术表达式
     */
    private Object evaluateSimpleExpression(String expr) {
        // 只是一个非常简单的实现示例，实际上应该使用一个完整的表达式解析器
        try {
            return Double.parseDouble(expr);
        } catch (NumberFormatException e) {
            // 不是简单数字，尝试加减乘除
            if (expr.contains("+")) {
                String[] parts = expr.split("\\+", 2);
                return (Double) evaluateSimpleExpression(parts[0]) + 
                       (Double) evaluateSimpleExpression(parts[1]);
            } else if (expr.contains("-")) {
                String[] parts = expr.split("-", 2);
                return (Double) evaluateSimpleExpression(parts[0]) - 
                       (Double) evaluateSimpleExpression(parts[1]);
            } else if (expr.contains("*")) {
                String[] parts = expr.split("\\*", 2);
                return (Double) evaluateSimpleExpression(parts[0]) * 
                       (Double) evaluateSimpleExpression(parts[1]);
            } else if (expr.contains("/")) {
                String[] parts = expr.split("/", 2);
                return (Double) evaluateSimpleExpression(parts[0]) / 
                       (Double) evaluateSimpleExpression(parts[1]);
            }
        }
        
        // 如果无法计算，返回原始表达式
        return expr;
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getExpression() {
        return expression;
    }
    
    public void setExpression(String expression) {
        this.expression = expression;
        markDirty();
    }
    
    public boolean isAutoEvaluate() {
        return autoEvaluate;
    }
    
    public void setAutoEvaluate(boolean autoEvaluate) {
        this.autoEvaluate = autoEvaluate;
        markDirty();
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        Object[] state = new Object[2];
        state[0] = expression;
        state[1] = autoEvaluate;
        return state;
    }
    
    /**
     * 恢复节点状态
     */
    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof Object[]) {
            Object[] objState = (Object[]) state;
            if (objState.length >= 2) {
                if (objState[0] instanceof String) {
                    expression = (String) objState[0];
                }
                if (objState[1] instanceof Boolean) {
                    autoEvaluate = (Boolean) objState[1];
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return this.description;
    }
} 