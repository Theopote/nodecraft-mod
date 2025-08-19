package com.nodecraft.nodesystem.nodes.animation.interpolation;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Color Blend Node: 颜色混合节点
 * 在两种颜色之间进行平滑插值
 */
@NodeInfo(
    id = "animation.interpolation.color_blend",
    displayName = "Color Blend",
    description = "在两种颜色之间进行插值混合",
    category = "animation.interpolation"
)
public class ColorBlendNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_COLOR_A_ID = "input_color_a";
    private static final String INPUT_COLOR_B_ID = "input_color_b";
    private static final String INPUT_FACTOR_ID = "input_factor";
    private static final String INPUT_COLOR_SPACE_ID = "input_color_space";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_BLENDED_COLOR_ID = "output_blended_color";
    
    // --- 颜色空间枚举 ---
    public enum ColorSpace {
        RGB(0, "RGB", "RGB颜色空间混合"),
        HSV(1, "HSV", "HSV颜色空间混合");
        
        private final int id;
        private final String name;
        private final String description;
        
        ColorSpace(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
        
        public static ColorSpace fromId(int id) {
            for (ColorSpace space : values()) {
                if (space.id == id) {
                    return space;
                }
            }
            return RGB; // 默认RGB颜色空间
        }
    }
    
    // --- 构造函数 ---
    public ColorBlendNode() {
        super(UUID.randomUUID(), "animation.interpolation.color_blend");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_COLOR_A_ID, "Color A", "起始颜色", NodeDataType.COLOR, this));
        addInputPort(new BasePort(INPUT_COLOR_B_ID, "Color B", "结束颜色", NodeDataType.COLOR, this));
        addInputPort(new BasePort(INPUT_FACTOR_ID, "Factor", "混合因子（0-1）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_COLOR_SPACE_ID, "Color Space", "混合所使用的颜色空间（0=RGB, 1=HSV）", NodeDataType.INTEGER, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_BLENDED_COLOR_ID, "Blended Color", "混合后的颜色", NodeDataType.COLOR, this));
    }
    
    @Override
    public String getDescription() {
        return "在两种颜色之间进行插值混合";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        float[] colorA = (float[]) inputValues.getOrDefault(INPUT_COLOR_A_ID, new float[]{1.0f, 1.0f, 1.0f, 1.0f});
        float[] colorB = (float[]) inputValues.getOrDefault(INPUT_COLOR_B_ID, new float[]{0.0f, 0.0f, 0.0f, 1.0f});
        Float factor = (Float) inputValues.getOrDefault(INPUT_FACTOR_ID, 0.5f);
        Integer colorSpaceId = (Integer) inputValues.getOrDefault(INPUT_COLOR_SPACE_ID, 0);
        
        // 确保插值因子在0-1范围内
        factor = Math.max(0.0f, Math.min(1.0f, factor));
        
        // 确保颜色数组有效
        if (colorA.length < 4) {
            colorA = new float[]{1.0f, 1.0f, 1.0f, 1.0f}; // 默认白色
        }
        if (colorB.length < 4) {
            colorB = new float[]{0.0f, 0.0f, 0.0f, 1.0f}; // 默认黑色
        }
        
        // 获取颜色空间
        ColorSpace colorSpace = ColorSpace.fromId(colorSpaceId);
        
        // 混合颜色
        float[] blendedColor = blendColors(colorA, colorB, factor, colorSpace);
        
        // 设置输出值
        outputValues.put(OUTPUT_BLENDED_COLOR_ID, blendedColor);
    }
    
    /**
     * 混合两种颜色
     */
    private float[] blendColors(float[] colorA, float[] colorB, float factor, ColorSpace colorSpace) {
        float[] result = new float[4];
        
        switch (colorSpace) {
            case HSV:
                // 转换为HSV空间
                float[] hsvA = rgbToHsv(colorA[0], colorA[1], colorA[2]);
                float[] hsvB = rgbToHsv(colorB[0], colorB[1], colorB[2]);
                
                // 插值HSV值
                float[] blendedHsv = new float[3];
                // 特殊处理色相插值（考虑色轮）
                blendedHsv[0] = lerpHue(hsvA[0], hsvB[0], factor);
                // 正常插值饱和度和明度
                blendedHsv[1] = hsvA[1] + (hsvB[1] - hsvA[1]) * factor;
                blendedHsv[2] = hsvA[2] + (hsvB[2] - hsvA[2]) * factor;
                
                // 转回RGB空间
                float[] rgb = hsvToRgb(blendedHsv[0], blendedHsv[1], blendedHsv[2]);
                result[0] = rgb[0];
                result[1] = rgb[1];
                result[2] = rgb[2];
                break;
                
            case RGB:
            default:
                // RGB空间直接线性插值
                result[0] = colorA[0] + (colorB[0] - colorA[0]) * factor;
                result[1] = colorA[1] + (colorB[1] - colorA[1]) * factor;
                result[2] = colorA[2] + (colorB[2] - colorA[2]) * factor;
                break;
        }
        
        // 透明度总是在RGB空间插值
        result[3] = colorA[3] + (colorB[3] - colorA[3]) * factor;
        
        return result;
    }
    
    /**
     * 色相插值（考虑色轮特性）
     */
    private float lerpHue(float h1, float h2, float factor) {
        // 确保色相在0-1范围
        h1 = h1 % 1.0f;
        if (h1 < 0) h1 += 1.0f;
        
        h2 = h2 % 1.0f;
        if (h2 < 0) h2 += 1.0f;
        
        // 计算两个角度之间的最短路径
        float delta = h2 - h1;
        
        // 如果差值大于0.5，则走另一条路径（色轮是环形的）
        if (delta > 0.5f) delta -= 1.0f;
        if (delta < -0.5f) delta += 1.0f;
        
        // 线性插值
        float result = h1 + delta * factor;
        
        // 确保结果在0-1范围
        result = result % 1.0f;
        if (result < 0) result += 1.0f;
        
        return result;
    }
    
    /**
     * 将RGB转换为HSV
     */
    private float[] rgbToHsv(float r, float g, float b) {
        float[] hsv = new float[3];
        
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;
        
        // 明度
        hsv[2] = max;
        
        // 饱和度
        hsv[1] = max != 0 ? delta / max : 0;
        
        // 色相
        if (delta == 0) {
            hsv[0] = 0; // 灰色
        } else {
            if (r == max) {
                hsv[0] = (g - b) / delta; // 在黄色和品红之间
            } else if (g == max) {
                hsv[0] = 2 + (b - r) / delta; // 在青色和黄色之间
            } else {
                hsv[0] = 4 + (r - g) / delta; // 在品红和青色之间
            }
            
            hsv[0] /= 6; // 转换到0-1范围
            if (hsv[0] < 0) hsv[0] += 1;
        }
        
        return hsv;
    }
    
    /**
     * 将HSV转换为RGB
     */
    private float[] hsvToRgb(float h, float s, float v) {
        float[] rgb = new float[3];
        
        if (s == 0) {
            // 灰色
            rgb[0] = rgb[1] = rgb[2] = v;
            return rgb;
        }
        
        h = h * 6; // 转换到0-6范围
        int i = (int) Math.floor(h);
        float f = h - i; // h的小数部分
        float p = v * (1 - s);
        float q = v * (1 - s * f);
        float t = v * (1 - s * (1 - f));
        
        switch (i % 6) {
            case 0: rgb[0] = v; rgb[1] = t; rgb[2] = p; break;
            case 1: rgb[0] = q; rgb[1] = v; rgb[2] = p; break;
            case 2: rgb[0] = p; rgb[1] = v; rgb[2] = t; break;
            case 3: rgb[0] = p; rgb[1] = q; rgb[2] = v; break;
            case 4: rgb[0] = t; rgb[1] = p; rgb[2] = v; break;
            case 5: rgb[0] = v; rgb[1] = p; rgb[2] = q; break;
        }
        
        return rgb;
    }
} 