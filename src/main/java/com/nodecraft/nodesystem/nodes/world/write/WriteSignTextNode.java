package com.nodecraft.nodesystem.nodes.world.write;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Write Sign Text 节点: 设置告示牌文本
 */
@NodeInfo(
    id = "world.write.write_sign_text",
    displayName = "写入告示牌文本",
    description = "写入告示牌文本",
    category = "world.write"
)
public class WriteSignTextNode extends BaseNode {

    // --- 节点属性 ---
    private String[] defaultLines = new String[]{"", "", "", ""}; // 默认文本行
    private boolean allowFormatting = true; // 是否允许格式代码
    private String textColor = "black"; // 默认文本颜色
    private String description = "设置告示牌文本";

    // --- 输入端口 IDs ---
    private static final String INPUT_COORDINATE_ID = "input_coordinate";
    private static final String INPUT_LINE_1_ID = "input_line_1";
    private static final String INPUT_LINE_2_ID = "input_line_2";
    private static final String INPUT_LINE_3_ID = "input_line_3";
    private static final String INPUT_LINE_4_ID = "input_line_4";
    private static final String INPUT_LINES_LIST_ID = "input_lines_list";
    private static final String INPUT_TEXT_COLOR_ID = "input_text_color";
    private static final String INPUT_GLOWING_ID = "input_glowing";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_IS_SIGN_ID = "output_is_sign";
    private static final String OUTPUT_SIGN_TYPE_ID = "output_sign_type";

    // --- 构造函数 ---
    public WriteSignTextNode() {
        super(UUID.randomUUID(), "world.write.write_sign_text");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_COORDINATE_ID, "Coordinate", 
                "告示牌坐标", NodeDataType.COORDINATE, this));
        addInputPort(new BasePort(INPUT_LINE_1_ID, "Line 1", 
                "第一行文本", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_LINE_2_ID, "Line 2", 
                "第二行文本", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_LINE_3_ID, "Line 3", 
                "第三行文本", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_LINE_4_ID, "Line 4", 
                "第四行文本", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_LINES_LIST_ID, "Lines List", 
                "文本行列表（优先于单独的行输入）", NodeDataType.LIST, this));
        addInputPort(new BasePort(INPUT_TEXT_COLOR_ID, "Text Color", 
                "文本颜色", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_GLOWING_ID, "Glowing", 
                "文字是否发光", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功设置", NodeDataType.BOOLEAN, this));
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
        boolean isSign = false;
        String signType = "";
        
        // 获取输入值
        Object coordinateObj = inputValues.get(INPUT_COORDINATE_ID);
        Object line1Obj = inputValues.get(INPUT_LINE_1_ID);
        Object line2Obj = inputValues.get(INPUT_LINE_2_ID);
        Object line3Obj = inputValues.get(INPUT_LINE_3_ID);
        Object line4Obj = inputValues.get(INPUT_LINE_4_ID);
        Object linesListObj = inputValues.get(INPUT_LINES_LIST_ID);
        Object textColorObj = inputValues.get(INPUT_TEXT_COLOR_ID);
        Object glowingObj = inputValues.get(INPUT_GLOWING_ID);
        
        // 准备文本行
        String[] lines = new String[4];
        System.arraycopy(defaultLines, 0, lines, 0, 4);
        
        // 优先使用列表输入
        if (linesListObj instanceof List) {
            List<?> linesList = (List<?>) linesListObj;
            for (int i = 0; i < Math.min(linesList.size(), 4); i++) {
                Object lineObj = linesList.get(i);
                if (lineObj != null) {
                    lines[i] = lineObj.toString();
                }
            }
        } else {
            // 使用单独的行输入
            if (line1Obj instanceof String) lines[0] = (String) line1Obj;
            if (line2Obj instanceof String) lines[1] = (String) line2Obj;
            if (line3Obj instanceof String) lines[2] = (String) line3Obj;
            if (line4Obj instanceof String) lines[3] = (String) line4Obj;
        }
        
        // 确定文本颜色
        String textColor = this.textColor;
        if (textColorObj instanceof String) {
            textColor = (String) textColorObj;
        }
        
        // 确定是否发光
        boolean glowing = false;
        if (glowingObj instanceof Boolean) {
            glowing = (Boolean) glowingObj;
        }
        
        // 检查必要的输入是否存在
        if (coordinateObj != null) {
            try {
                /*
                在实际实现中，需要根据Minecraft API实现以下操作:
                1. 获取指定坐标的方块
                2. 检查是否为告示牌
                3. 设置告示牌文本
                
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
                        
                        // 设置文本行
                        for (int i = 0; i < 4; i++) {
                            String lineText = lines[i] != null ? lines[i] : "";
                            
                            // 处理格式代码
                            if (!allowFormatting) {
                                lineText = lineText.replaceAll("§[0-9a-fk-or]", "");
                            }
                            
                            // 创建具有颜色和样式的文本
                            Text text;
                            if (glowing) {
                                text = Text.literal(lineText).formatted(Formatting.valueOf(textColor.toUpperCase())).formatted(Formatting.BOLD);
                            } else {
                                text = Text.literal(lineText).formatted(Formatting.valueOf(textColor.toUpperCase()));
                            }
                            
                            // 设置文本
                            sign.setText(text, i);
                        }
                        
                        // 更新方块实体
                        blockEntity.markDirty();
                        world.updateListeners(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                        
                        success = true;
                    }
                }
                */
                
                // 模拟设置告示牌文本 (在实际实现中替换为上面的逻辑)
                isSign = true;
                signType = "minecraft:oak_sign";
                
                // 打印调试信息
                System.out.println("模拟在坐标 " + coordinateObj + " 的告示牌上设置文本:");
                for (int i = 0; i < 4; i++) {
                    System.out.println("行 " + (i + 1) + ": " + lines[i]);
                }
                System.out.println("颜色: " + textColor + ", 发光: " + glowing);
                
                success = true;
            } catch (Exception e) {
                success = false;
                System.err.println("Error writing sign text: " + e.getMessage());
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_IS_SIGN_ID, isSign);
        outputValues.put(OUTPUT_SIGN_TYPE_ID, signType);
    }
    
    // --- Getters/Setters for Properties ---
    
    public String[] getDefaultLines() {
        return defaultLines;
    }
    
    public void setDefaultLines(String[] defaultLines) {
        if (defaultLines != null && defaultLines.length == 4) {
            this.defaultLines = defaultLines;
            markDirty();
        }
    }
    
    public boolean isAllowFormatting() {
        return allowFormatting;
    }
    
    public void setAllowFormatting(boolean allowFormatting) {
        this.allowFormatting = allowFormatting;
        markDirty();
    }
    
    public String getTextColor() {
        return textColor;
    }
    
    public void setTextColor(String textColor) {
        if (isValidColor(textColor)) {
            this.textColor = textColor;
            markDirty();
        }
    }
    
    /**
     * 检查颜色是否有效
     */
    private boolean isValidColor(String color) {
        if (color == null) return false;
        
        String[] validColors = {
            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", 
            "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple", 
            "yellow", "white"
        };
        
        for (String validColor : validColors) {
            if (validColor.equals(color.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        Object[] state = new Object[3];
        state[0] = defaultLines;
        state[1] = allowFormatting;
        state[2] = textColor;
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
                if (objState[0] instanceof String[]) {
                    defaultLines = (String[]) objState[0];
                }
                if (objState[1] instanceof Boolean) {
                    allowFormatting = (Boolean) objState[1];
                }
                if (objState[2] instanceof String) {
                    textColor = (String) objState[2];
                }
            }
        }
    }
} 
