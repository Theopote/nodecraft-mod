package com.nodecraft.nodesystem.nodes.world.read;

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
 * Read Sign Text 节点: 读取告示牌文本。
 */
@NodeInfo(
    id = "world.read.read_sign_text",
    displayName = "读取告示牌文本",
    description = "读取告示牌文本",
    category = "world.read"
)
public class ReadSignTextNode extends BaseNode {

    // --- 节点属性 ---
    private String description = "读取告示牌文本";

    // --- 输入端口 IDs ---
    private static final String INPUT_COORDINATE_ID = "input_coordinate";
    private static final String INPUT_INCLUDE_FORMATTING_ID = "input_include_formatting";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_TEXT_LINES_ID = "output_text_lines";
    private static final String OUTPUT_COMBINED_TEXT_ID = "output_combined_text";
    private static final String OUTPUT_IS_SIGN_ID = "output_is_sign";
    private static final String OUTPUT_SIGN_TYPE_ID = "output_sign_type";

    // --- 构造函数 ---
    public ReadSignTextNode() {
        super(UUID.randomUUID(), "world.read.read_sign_text");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_COORDINATE_ID, "Coordinate", 
                "告示牌坐标", NodeDataType.COORDINATE, this));
        addInputPort(new BasePort(INPUT_INCLUDE_FORMATTING_ID, "Include Formatting", 
                "是否包含格式代码", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功读取", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_TEXT_LINES_ID, "Text Lines", 
                "文本行列表", NodeDataType.LIST, this));
        addOutputPort(new BasePort(OUTPUT_COMBINED_TEXT_ID, "Combined Text", 
                "合并后的文本", NodeDataType.STRING, this));
        addOutputPort(new BasePort(OUTPUT_IS_SIGN_ID, "Is Sign", 
                "坐标处是否为告示牌", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_SIGN_TYPE_ID, "Sign Type", 
                "告示牌类型", NodeDataType.STRING, this));
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
        List<String> textLines = new ArrayList<>();
        String combinedText = "";
        boolean isSign = false;
        String signType = "";
        
        // 获取输入值
        Object coordinateObj = inputValues.get(INPUT_COORDINATE_ID);
        Object includeFormattingObj = inputValues.get(INPUT_INCLUDE_FORMATTING_ID);
        
        // 确定是否包含格式代码
        boolean includeFormatting = false;
        if (includeFormattingObj instanceof Boolean) {
            includeFormatting = (Boolean) includeFormattingObj;
        }
        
        // 检查必要的输入是否存在
        if (coordinateObj != null) {
            try {
                /*
                在实际实现中，需要根据Minecraft API实现以下操作:
                1. 获取指定坐标的方块
                2. 检查是否为告示牌
                3. 提取告示牌文本
                
                if (coordinateObj instanceof BlockPos) {
                    BlockPos pos = (BlockPos) coordinateObj;
                    World world = server.getWorld(World.OVERWORLD); // 或从上下文中获取
                    
                    // 获取方块实体
                    BlockEntity blockEntity = world.getBlockEntity(pos);
                    
                    // 检查是否为告示牌
                    if (blockEntity instanceof SignBlockEntity) {
                        SignBlockEntity sign = (SignBlockEntity) blockEntity;
                        isSign = true;
                        
                        // 获取告示牌类型
                        BlockState blockState = world.getBlockState(pos);
                        signType = Registry.BLOCK.getId(blockState.getBlock()).toString();
                        
                        // 读取文本行
                        for (int i = 0; i < 4; i++) {
                            Text text = sign.getText(i);
                            String lineText;
                            
                            if (includeFormatting) {
                                // 带有格式化代码的文本
                                lineText = text.getString();
                            } else {
                                // 纯文本
                                lineText = text.getString().replaceAll("§[0-9a-fk-or]", "");
                            }
                            
                            textLines.add(lineText);
                            if (!lineText.isEmpty()) {
                                if (combinedText.isEmpty()) {
                                    combinedText = lineText;
                                } else {
                                    combinedText += " " + lineText;
                                }
                            }
                        }
                        
                        success = true;
                    }
                }
                */
                
                // 模拟读取告示牌文本 (在实际实现中替换为上面的逻辑)
                isSign = true;
                signType = "minecraft:oak_sign";
                
                // 模拟文本行
                textLines.add("这是第一行");
                textLines.add("这是第二行");
                textLines.add("这是第三行");
                textLines.add("这是第四行");
                
                // 合并文本
                combinedText = String.join(" ", textLines);
                
                success = true;
            } catch (Exception e) {
                success = false;
                System.err.println("Error reading sign text: " + e.getMessage());
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_TEXT_LINES_ID, textLines);
        outputValues.put(OUTPUT_COMBINED_TEXT_ID, combinedText);
        outputValues.put(OUTPUT_IS_SIGN_ID, isSign);
        outputValues.put(OUTPUT_SIGN_TYPE_ID, signType);
    }
} 
