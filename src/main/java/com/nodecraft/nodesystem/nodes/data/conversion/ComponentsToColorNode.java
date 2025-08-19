package com.nodecraft.nodesystem.nodes.data.conversion;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.datatypes.ColorData;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Components to Color 节点，从RGBA分量构建颜色
 */
@NodeInfo(
    id = "data.conversion.components_to_color",
    displayName = "Components to Color",
    description = "Creates a color from RGBA components",
    category = "data.conversion"
)
public class ComponentsToColorNode extends BaseNode {
    
    // --- 节点属性 ---
    private boolean inputFloatValues = true; // 是否接受浮点值 (0.0-1.0) 还是整数值 (0-255)
    private String description; // 存储节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_RED_ID = "input_red";
    private static final String INPUT_GREEN_ID = "input_green";
    private static final String INPUT_BLUE_ID = "input_blue";
    private static final String INPUT_ALPHA_ID = "input_alpha";
    private static final String OUTPUT_COLOR_ID = "output_color";
    
    /**
     * 构造一个新的颜色合成节点
     */
    public ComponentsToColorNode() {
        // 调用父类构造函数，使用UUID.randomUUID()生成新的ID
        super(UUID.randomUUID(), "data.conversion.components_to_color");
        
        // 设置节点描述
        this.description = "Creates a color from RGBA components";
        
        // 创建输入端口 - 使用DOUBLE类型以支持整数和浮点值输入
        IPort redInput = new BasePort(INPUT_RED_ID, "Red", 
                "The red component", NodeDataType.DOUBLE, this);
        addInputPort(redInput);
        
        IPort greenInput = new BasePort(INPUT_GREEN_ID, "Green", 
                "The green component", NodeDataType.DOUBLE, this);
        addInputPort(greenInput);
        
        IPort blueInput = new BasePort(INPUT_BLUE_ID, "Blue", 
                "The blue component", NodeDataType.DOUBLE, this);
        addInputPort(blueInput);
        
        IPort alphaInput = new BasePort(INPUT_ALPHA_ID, "Alpha", 
                "The alpha component (optional, defaults to 1.0)", NodeDataType.DOUBLE, this);
        addInputPort(alphaInput);
        
        // 创建输出端口
        IPort colorOutput = new BasePort(OUTPUT_COLOR_ID, "Color", 
                "The resulting color", NodeDataType.COLOR, this);
        addOutputPort(colorOutput);
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
        Object redObj = inputValues.get(INPUT_RED_ID);
        Object greenObj = inputValues.get(INPUT_GREEN_ID);
        Object blueObj = inputValues.get(INPUT_BLUE_ID);
        Object alphaObj = inputValues.get(INPUT_ALPHA_ID);
        
        // 默认值
        float red = 0f, green = 0f, blue = 0f, alpha = 1f;
        
        // 处理输入值
        if (redObj instanceof Number) {
            red = ((Number) redObj).floatValue();
        }
        
        if (greenObj instanceof Number) {
            green = ((Number) greenObj).floatValue();
        }
        
        if (blueObj instanceof Number) {
            blue = ((Number) blueObj).floatValue();
        }
        
        if (alphaObj instanceof Number) {
            alpha = ((Number) alphaObj).floatValue();
        }
        
        // 如果输入是整数值 (0-255)，转换为浮点值 (0.0-1.0)
        if (!inputFloatValues) {
            red /= 255f;
            green /= 255f;
            blue /= 255f;
            alpha /= 255f;
        }
        
        // 创建颜色对象
        ColorData color = new ColorData(red, green, blue, alpha);
        
        // 设置输出
        outputValues.put(OUTPUT_COLOR_ID, color);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isInputFloatValues() {
        return inputFloatValues;
    }
    
    public void setInputFloatValues(boolean useFloat) {
        this.inputFloatValues = useFloat;
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("inputFloatValues", isInputFloatValues());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("inputFloatValues")) {
                Object useFloatObj = stateMap.get("inputFloatValues");
                if (useFloatObj instanceof Boolean) {
                    setInputFloatValues((Boolean) useFloatObj);
                }
            }
        }
    }
} 