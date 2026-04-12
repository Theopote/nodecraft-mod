package com.nodecraft.nodesystem.nodes.deferred.out_of_scope;

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
import java.util.UUID;

/**
 * Script Node: 允许编写脚本来处理输入并产生输出
 * 支持JavaScript和Lua脚本
 */
@NodeInfo(
    id = "deferred.out_of_scope.script",
    displayName = "脚本节点",
    description = "执行JavaScript或Lua脚本代码",
    category = "deferred.out_of_scope"
)
public class ScriptNode extends BaseNode {

    // --- 节点属性 ---
    private String scriptType = "js"; // js 或 lua
    private String scriptCode = ""; // 脚本代码
    private boolean autoExecute = false; // 输入改变时自动执行
    private String description = "执行JavaScript或Lua脚本代码";
    
    // --- 输入端口 IDs ---
    private static final String INPUT_EXECUTE_ID = "input_execute";
    private static final String INPUT_VARIABLES_ID = "input_variables";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_RESULT_ID = "output_result";
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_ERROR_ID = "output_error";
    
    // --- 脚本引擎 ---
    private transient ScriptEngineManager engineManager;
    private transient ScriptEngine jsEngine;
    private transient ScriptEngine luaEngine;
    
    // --- 上次输入 ---
    private transient Map<String, Object> lastInputs = new HashMap<>();
    
    /**
     * 构造脚本节点
     */
    public ScriptNode() {
        super(UUID.randomUUID(), "deferred.out_of_scope.script");
        
        // 创建脚本引擎管理器
        engineManager = new ScriptEngineManager();
        
        // 创建JavaScript引擎
        jsEngine = engineManager.getEngineByName("nashorn");
        if (jsEngine == null) {
            jsEngine = engineManager.getEngineByName("JavaScript");
        }
        
        // 创建Lua引擎 (需要LuaJ库)
        luaEngine = engineManager.getEngineByName("luaj");
        if (luaEngine == null) {
            luaEngine = engineManager.getEngineByName("lua");
        }
        
        // 创建输入端口
        addInputPort(new BasePort(INPUT_EXECUTE_ID, "Execute", 
                "触发脚本执行", NodeDataType.ANY, this));
        
        addInputPort(new BasePort(INPUT_VARIABLES_ID, "Variables", 
                "传递给脚本的变量Map", NodeDataType.ANY, this));
        
        // 创建输出端口
        addOutputPort(new BasePort(OUTPUT_RESULT_ID, "Result", 
                "脚本执行结果", NodeDataType.ANY, this));
        
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "脚本是否成功执行", NodeDataType.BOOLEAN, this));
        
        addOutputPort(new BasePort(OUTPUT_ERROR_ID, "Error", 
                "脚本执行错误信息", NodeDataType.STRING, this));
        
        // 设置默认脚本
        if ("js".equals(scriptType)) {
            scriptCode = "// JavaScript示例\n" + 
                         "// 输入变量在input对象中\n" + 
                         "// 返回结果将作为output\n" + 
                         "var result = {};\n" + 
                         "for (var key in input) {\n" + 
                         "  result[key] = input[key];\n" + 
                         "}\n" + 
                         "result.processed = true;\n" + 
                         "result; // 返回结果";
        } else {
            scriptCode = "-- Lua示例\n" + 
                         "-- 输入变量在input表中\n" + 
                         "-- 返回结果将作为output\n" + 
                         "local result = {}\n" + 
                         "for k, v in pairs(input) do\n" + 
                         "  result[k] = v\n" + 
                         "end\n" + 
                         "result.processed = true\n" + 
                         "return result -- 返回结果";
        }
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
        Object executeObj = inputValues.get(INPUT_EXECUTE_ID);
        Object variablesObj = inputValues.get(INPUT_VARIABLES_ID);
        
        // 检查是否需要执行脚本
        boolean shouldExecute = executeObj != null;
        
        // 如果设置了自动执行，检查输入是否变化
        if (autoExecute && !shouldExecute) {
            Map<String, Object> currentInputs = new HashMap<>(inputValues);
            if (!currentInputs.equals(lastInputs)) {
                shouldExecute = true;
                // 保存当前输入以便比较
                lastInputs = new HashMap<>(currentInputs);
            }
        }
        
        if (shouldExecute) {
            // 获取变量对象
            Map<String, Object> variables = new HashMap<>();
            
            // 如果提供了变量，且是Map类型，则使用它
            if (variablesObj instanceof Map) {
                // 安全地将Map内容复制到变量Map
                @SuppressWarnings("unchecked")
                Map<Object, Object> varMap = (Map<Object, Object>) variablesObj;
                for (Map.Entry<Object, Object> entry : varMap.entrySet()) {
                    if (entry.getKey() != null) {
                        variables.put(entry.getKey().toString(), entry.getValue());
                    }
                }
            }
            
            // 将所有输入值添加到变量映射
            for (Map.Entry<String, Object> entry : inputValues.entrySet()) {
                variables.put(entry.getKey(), entry.getValue());
            }
            
            // 如果有执行上下文，添加它
            if (context != null) {
                variables.put("context", context);
                
                // 添加一些便捷变量
                if (context.getWorld() != null) {
                    variables.put("world", context.getWorld());
                }
                
                if (context.getPlayer() != null) {
                    variables.put("player", context.getPlayer());
                }
            }
            
            try {
                // 根据脚本类型选择引擎
                ScriptEngine engine = null;
                if ("js".equals(scriptType) && jsEngine != null) {
                    engine = jsEngine;
                } else if ("lua".equals(scriptType) && luaEngine != null) {
                    engine = luaEngine;
                }
                
                // 如果找到了引擎，执行脚本
                if (engine != null) {
                    // 创建绑定
                    Bindings bindings = engine.createBindings();
                    
                    // 将变量放入input对象中，以便脚本访问
                    bindings.put("input", variables);
                    
                    // 执行脚本
                    result = engine.eval(scriptCode, bindings);
                    success = true;
                } else {
                    errorMessage = "脚本引擎不可用: " + scriptType;
                }
            } catch (ScriptException e) {
                errorMessage = "脚本执行错误: " + e.getMessage();
            } catch (Exception e) {
                errorMessage = "执行脚本时发生错误: " + e.getMessage();
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_RESULT_ID, result);
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_ERROR_ID, errorMessage);
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getScriptType() {
        return scriptType;
    }
    
    public void setScriptType(String scriptType) {
        if ("js".equals(scriptType) || "lua".equals(scriptType)) {
            this.scriptType = scriptType;
            markDirty();
        }
    }
    
    public String getScriptCode() {
        return scriptCode;
    }
    
    public void setScriptCode(String scriptCode) {
        this.scriptCode = scriptCode;
        markDirty();
    }
    
    public boolean isAutoExecute() {
        return autoExecute;
    }
    
    public void setAutoExecute(boolean autoExecute) {
        this.autoExecute = autoExecute;
        markDirty();
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        Object[] state = new Object[3];
        state[0] = scriptType;
        state[1] = scriptCode;
        state[2] = autoExecute;
        return state;
    }
    
    /**
     * 恢复节点状态
     */
    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof Object[]) {
            Object[] objState = (Object[]) state;
            if (objState.length >= 3) {
                if (objState[0] instanceof String) {
                    scriptType = (String) objState[0];
                }
                if (objState[1] instanceof String) {
                    scriptCode = (String) objState[1];
                }
                if (objState[2] instanceof Boolean) {
                    autoExecute = (Boolean) objState[2];
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return this.description;
    }
} 
