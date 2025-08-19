package com.nodecraft.nodesystem.nodes.world.interaction;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Execute Command 节点: 执行 Minecraft 命令字符串
 */
@NodeInfo(
    id = "world.interaction.execute_command",
    displayName = "执行命令",
    description = "执行命令",
    category = "world.interaction"
)
public class ExecuteCommandNode extends BaseNode {

    // --- 节点属性 ---
    private boolean parseResult = false; // 是否解析命令返回结果
    private String description = "执行 Minecraft 命令字符串";

    // --- 输入端口 IDs ---
    private static final String INPUT_COMMAND_ID = "input_command";
    private static final String INPUT_EXECUTOR_ID = "input_executor";
    private static final String INPUT_PARSE_RESULT_ID = "input_parse_result";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_RESULT_CODE_ID = "output_result_code";
    private static final String OUTPUT_RESULT_TEXT_ID = "output_result_text";
    private static final String OUTPUT_PARSED_RESULT_ID = "output_parsed_result";

    // --- 构造函数 ---
    public ExecuteCommandNode() {
        super(UUID.randomUUID(), "world.interaction.execute_command");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_COMMAND_ID, "Command", 
                "要执行的命令字符串", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_EXECUTOR_ID, "Executor", 
                "命令执行者（为空则使用系统权限）", NodeDataType.PLAYER, this));
        addInputPort(new BasePort(INPUT_PARSE_RESULT_ID, "Parse Result", 
                "是否解析命令返回结果", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "命令是否成功执行", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_RESULT_CODE_ID, "Result Code", 
                "命令执行结果代码", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_RESULT_TEXT_ID, "Result Text", 
                "命令执行结果文本", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_PARSED_RESULT_ID, "Parsed Result", 
                "解析后的命令结果（如果启用）", NodeDataType.ANY, this));
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
        int resultCode = 0;
        String resultText = "";
        Object parsedResult = null;
        
        // 获取输入值
        Object commandObj = inputValues.get(INPUT_COMMAND_ID);
        Object executorObj = inputValues.get(INPUT_EXECUTOR_ID);
        Object parseResultObj = inputValues.get(INPUT_PARSE_RESULT_ID);
        
        // 确定是否解析结果
        boolean parseResult = this.parseResult;
        if (parseResultObj instanceof Boolean) {
            parseResult = (Boolean) parseResultObj;
        }
        
        // 检查必要的输入是否存在
        if (commandObj instanceof String) {
            String command = (String) commandObj;
            
            try {
                /*
                在实际实现中，需要根据Minecraft API实现以下操作:
                1. 根据执行者确定命令源
                2. 执行命令
                3. 处理命令结果
                4. 如果需要，解析命令结果
                
                // 确定命令源
                CommandSource source;
                if (executorObj instanceof PlayerEntity) {
                    PlayerEntity player = (PlayerEntity) executorObj;
                    source = player.getCommandSource();
                } else {
                    // 使用服务器命令源（系统权限）
                    source = server.getCommandSource();
                }
                
                // 执行命令
                try {
                    int cmdResult = server.getCommandManager().execute(source, command);
                    resultCode = cmdResult;
                    success = cmdResult > 0;
                    resultText = "Command executed with result: " + cmdResult;
                    
                    // 如果需要解析结果
                    if (parseResult && success) {
                        // 这里需要根据具体命令类型解析结果
                        // 例如，对于数据查询命令可能返回NBT数据
                        if (command.startsWith("data get") && cmdResult > 0) {
                            // 模拟解析data get命令的结果
                            // 实际实现中需要捕获命令输出并解析
                            parsedResult = new HashMap<String, Object>();
                        }
                    }
                } catch (CommandSyntaxException e) {
                    success = false;
                    resultCode = -1;
                    resultText = "Command syntax error: " + e.getMessage();
                }
                */
                
                // 模拟命令执行 (在实际实现中替换为上面的逻辑)
                success = true;
                resultCode = 1;
                resultText = "Command executed: " + command;
                
                if (parseResult) {
                    // 模拟一些解析后的数据
                    if (command.startsWith("data get")) {
                        // 假设这是一个获取数据的命令
                        parsedResult = "{'count': 1, 'id': 'minecraft:stone'}";
                    }
                }
            } catch (Exception e) {
                success = false;
                resultCode = -2;
                resultText = "Error executing command: " + e.getMessage();
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_RESULT_CODE_ID, resultCode);
        outputValues.put(OUTPUT_RESULT_TEXT_ID, resultText);
        outputValues.put(OUTPUT_PARSED_RESULT_ID, parsedResult);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isParseResult() {
        return parseResult;
    }
    
    public void setParseResult(boolean parseResult) {
        this.parseResult = parseResult;
        markDirty();
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        return parseResult;
    }
    
    /**
     * 恢复节点状态
     */
    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof Boolean) {
            parseResult = (Boolean) state;
        }
    }
} 