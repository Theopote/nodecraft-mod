package com.nodecraft.nodesystem.nodes.animation.interpolation;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Ease Curve Node: 缓动曲线节点
 * 将线性插值因子转换为各种非线性曲线，用于创建自然的动画效果
 */
@NodeInfo(
    id = "animation.interpolation.ease_curve",
    displayName = "Ease Curve",
    description = "将线性插值因子转换为非线性曲线",
    category = "animation.interpolation"
)
public class EaseCurveNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_FACTOR_ID = "input_factor";
    private static final String INPUT_CURVE_TYPE_ID = "input_curve_type";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_EASED_FACTOR_ID = "output_eased_factor";
    
    // --- 缓动曲线类型枚举 ---
    public enum EaseType {
        LINEAR(0, "Linear", "线性曲线，没有缓动效果"),
        EASE_IN(1, "Ease In", "渐入效果，开始慢后来快"),
        EASE_OUT(2, "Ease Out", "渐出效果，开始快后来慢"),
        EASE_IN_OUT(3, "Ease In Out", "渐入渐出效果，开始和结束慢，中间快"),
        ELASTIC(4, "Elastic", "弹性效果，模拟弹簧振动"),
        BOUNCE(5, "Bounce", "弹跳效果，模拟物体弹跳"),
        BACK(6, "Back", "回弹效果，超过目标点后回弹"),
        SINE(7, "Sine", "基于正弦曲线的平滑过渡"),
        QUAD(8, "Quad", "二次方曲线"),
        CUBIC(9, "Cubic", "三次方曲线"),
        QUART(10, "Quart", "四次方曲线"),
        QUINT(11, "Quint", "五次方曲线"),
        EXPO(12, "Expo", "指数曲线"),
        CIRC(13, "Circ", "圆形曲线");
        
        private final int id;
        private final String name;
        private final String description;
        
        EaseType(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
        
        public static EaseType fromId(int id) {
            for (EaseType type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return LINEAR; // 默认线性曲线
        }
    }
    
    // --- 构造函数 ---
    public EaseCurveNode() {
        super(UUID.randomUUID(), "animation.interpolation.ease_curve");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_FACTOR_ID, "Factor", "线性插值因子（0-1）", NodeDataType.FLOAT, this));
        addInputPort(new BasePort(INPUT_CURVE_TYPE_ID, "Curve Type", "缓动曲线类型", NodeDataType.INTEGER, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_EASED_FACTOR_ID, "Eased Factor", "应用缓动曲线后的因子", NodeDataType.FLOAT, this));
    }
    
    @Override
    public String getDescription() {
        return "将线性插值因子转换为非线性曲线";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Float factor = (Float) inputValues.getOrDefault(INPUT_FACTOR_ID, 0.5f);
        Integer curveTypeId = (Integer) inputValues.getOrDefault(INPUT_CURVE_TYPE_ID, 0);
        
        // 确保插值因子在0-1范围内
        factor = Math.max(0.0f, Math.min(1.0f, factor));
        
        // 获取缓动曲线类型
        EaseType easeType = EaseType.fromId(curveTypeId);
        
        // 应用缓动函数
        float easedFactor = applyEasingFunction(factor, easeType);
        
        // 设置输出值
        outputValues.put(OUTPUT_EASED_FACTOR_ID, easedFactor);
    }
    
    /**
     * 应用缓动函数
     */
    private float applyEasingFunction(float t, EaseType easeType) {
        switch (easeType) {
            case LINEAR:
                return t;
                
            case EASE_IN:
                return t * t * t; // 三次方缓入
                
            case EASE_OUT:
                return 1 - (1 - t) * (1 - t) * (1 - t); // 三次方缓出
                
            case EASE_IN_OUT:
                return t < 0.5f ? 
                       4 * t * t * t : 
                       1 - (float) Math.pow(-2 * t + 2, 3) / 2; // 三次方缓入缓出
                
            case ELASTIC:
                if (t == 0 || t == 1) return t;
                return (float) (Math.pow(2, -10 * t) * Math.sin((t * 10 - 0.75) * (2 * Math.PI) / 3) + 1); // 弹性效果
                
            case BOUNCE:
                return bounceOut(t); // 弹跳效果
                
            case BACK:
                // 回弹效果
                final float c1 = 1.70158f;
                final float c3 = c1 + 1;
                return 1 + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
                
            case SINE:
                return (float) Math.sin((t * Math.PI) / 2); // 正弦曲线
                
            case QUAD:
                return t < 0.5f ? 
                       2 * t * t : 
                       1 - (float) Math.pow(-2 * t + 2, 2) / 2; // 二次方
                
            case CUBIC:
                return t < 0.5f ? 
                       4 * t * t * t : 
                       1 - (float) Math.pow(-2 * t + 2, 3) / 2; // 三次方
                
            case QUART:
                return t < 0.5f ? 
                       8 * t * t * t * t : 
                       1 - (float) Math.pow(-2 * t + 2, 4) / 2; // 四次方
                
            case QUINT:
                return t < 0.5f ? 
                       16 * t * t * t * t * t : 
                       1 - (float) Math.pow(-2 * t + 2, 5) / 2; // 五次方
                
            case EXPO:
                return t == 0 ? 0 : 
                       t == 1 ? 1 : 
                       t < 0.5 ? (float) Math.pow(2, 20 * t - 10) / 2 : 
                       (2 - (float) Math.pow(2, -20 * t + 10)) / 2; // 指数曲线
                
            case CIRC:
                return t < 0.5 ? 
                       (1 - (float) Math.sqrt(1 - Math.pow(2 * t, 2))) / 2 : 
                       (float) (Math.sqrt(1 - Math.pow(-2 * t + 2, 2)) + 1) / 2; // 圆形曲线
                
            default:
                return t;
        }
    }
    
    /**
     * 弹跳效果的缓动函数
     */
    private float bounceOut(float t) {
        final float n1 = 7.5625f;
        final float d1 = 2.75f;
        
        if (t < 1 / d1) {
            return n1 * t * t;
        } else if (t < 2 / d1) {
            return n1 * (t -= 1.5f / d1) * t + 0.75f;
        } else if (t < 2.5 / d1) {
            return n1 * (t -= 2.25f / d1) * t + 0.9375f;
        } else {
            return n1 * (t -= 2.625f / d1) * t + 0.984375f;
        }
    }
} 