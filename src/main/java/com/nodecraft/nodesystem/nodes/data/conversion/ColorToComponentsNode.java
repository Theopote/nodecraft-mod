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
 * Color to Components 节点，将颜色拆分为RGBA分量
 */
@NodeInfo(
    id = "data.conversion.color_to_components",
    displayName = "Color to Components",
    description = "Extracts the RGBA components from a color",
    category = "data.conversion"
)
public class ColorToComponentsNode extends BaseNode {
    
    // --- 节点属性 ---
    private boolean outputFloatValues = true; // 是否输出浮点值 (0.0-1.0) 还是整数值 (0-255)
    private String description; // 存储节点描述
    
    // --- 输入/输出端口ID ---
    private static final String INPUT_COLOR_ID = "input_color";
    private static final String OUTPUT_RED_ID = "output_red";
    private static final String OUTPUT_GREEN_ID = "output_green";
    private static final String OUTPUT_BLUE_ID = "output_blue";
    private static final String OUTPUT_ALPHA_ID = "output_alpha";
    
    /**
     * 构造一个新的颜色分解节点
     */
    public ColorToComponentsNode() {
        // 调用父类构造函数，使用UUID.randomUUID()生成新的ID
        super(UUID.randomUUID(), "data.conversion.color_to_components");
        
        // 设置节点描述
        this.description = "Extracts the RGBA components from a color";
        
        // 创建输入端口
        IPort colorInput = new BasePort(INPUT_COLOR_ID, "Color", 
                "The input color", NodeDataType.COLOR, this);
        addInputPort(colorInput);
        
        // 创建输出端口 - 使用DOUBLE类型以支持整数和浮点值输出
        IPort redOutput = new BasePort(OUTPUT_RED_ID, "Red", 
                "The red component", NodeDataType.DOUBLE, this);
        addOutputPort(redOutput);
        
        IPort greenOutput = new BasePort(OUTPUT_GREEN_ID, "Green", 
                "The green component", NodeDataType.DOUBLE, this);
        addOutputPort(greenOutput);
        
        IPort blueOutput = new BasePort(OUTPUT_BLUE_ID, "Blue", 
                "The blue component", NodeDataType.DOUBLE, this);
        addOutputPort(blueOutput);
        
        IPort alphaOutput = new BasePort(OUTPUT_ALPHA_ID, "Alpha", 
                "The alpha component", NodeDataType.DOUBLE, this);
        addOutputPort(alphaOutput);
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
        Object colorObj = inputValues.get(INPUT_COLOR_ID);
        
        // 默认值（黑色，完全不透明）
        float red = 0, green = 0, blue = 0, alpha = 1;
        
        // 检查输入是否为颜色
        if (colorObj instanceof ColorData) {
            ColorData color = (ColorData) colorObj;
            red = color.r();
            green = color.g();
            blue = color.b();
            alpha = color.a();
        }
        
        // 根据输出类型设置值
        if (outputFloatValues) {
            // 浮点值 (0.0-1.0)
            outputValues.put(OUTPUT_RED_ID, red);
            outputValues.put(OUTPUT_GREEN_ID, green);
            outputValues.put(OUTPUT_BLUE_ID, blue);
            outputValues.put(OUTPUT_ALPHA_ID, alpha);
        } else {
            // 整数值 (0-255)
            outputValues.put(OUTPUT_RED_ID, Math.round(red * 255));
            outputValues.put(OUTPUT_GREEN_ID, Math.round(green * 255));
            outputValues.put(OUTPUT_BLUE_ID, Math.round(blue * 255));
            outputValues.put(OUTPUT_ALPHA_ID, Math.round(alpha * 255));
        }
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isOutputFloatValues() {
        return outputFloatValues;
    }
    
    public void setOutputFloatValues(boolean useFloat) {
        this.outputFloatValues = useFloat;
        markDirty();
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("outputFloatValues", isOutputFloatValues());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            if (stateMap.containsKey("outputFloatValues")) {
                Object useFloatObj = stateMap.get("outputFloatValues");
                if (useFloatObj instanceof Boolean) {
                    setOutputFloatValues((Boolean) useFloatObj);
                }
            }
        }
    }
} 