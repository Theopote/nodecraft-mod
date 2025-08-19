package com.nodecraft.nodesystem.nodes.inputs.selectors;

import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * 效果类型选择器节点，用于在UI中选择Minecraft状态效果
 */
@NodeInfo(
    id = "inputs.selectors.effect_type_selector",
    displayName = "效果类型选择器",
    description = "允许选择Minecraft状态效果类型",
    category = "inputs.selectors"
)
public class EffectTypeSelectorNode extends BaseNode {
    
    // --- 节点属性 ---
    private String selectedEffect = "minecraft:speed"; // 默认选择速度效果
    private boolean allowModded = true; // 是否允许选择模组效果
    private boolean showBeneficialOnly = false; // 是否只显示有益效果
    private boolean showHarmfulOnly = false; // 是否只显示有害效果
    
    // --- 输出端口 ---
    private static final String OUTPUT_EFFECT_ID = "output_effect_id";
    private static final String OUTPUT_IS_MODDED = "output_is_modded";
    private static final String OUTPUT_NAMESPACE = "output_namespace";
    private static final String OUTPUT_EFFECT_PATH = "output_effect_path";
    private static final String OUTPUT_IS_BENEFICIAL = "output_is_beneficial";
    private static final String OUTPUT_EFFECT_TYPE = "output_effect_type";
    
    /**
     * 构造一个新的效果类型选择器节点
     */
    public EffectTypeSelectorNode() {
        // 使用新的分类命名 - inputs.selectors.effect_type_selector
        super(UUID.randomUUID(), "inputs.selectors.effect_type_selector");
        
        // 创建并添加输出端口
        IPort effectIdOutput = new BasePort(OUTPUT_EFFECT_ID, "Effect ID", 
                "The selected effect's full identifier", NodeDataType.STRING, this);
        addOutputPort(effectIdOutput);
        
        IPort isModdedOutput = new BasePort(OUTPUT_IS_MODDED, "Is Modded", 
                "Whether the selected effect is from a mod", NodeDataType.BOOLEAN, this);
        addOutputPort(isModdedOutput);
        
        IPort namespaceOutput = new BasePort(OUTPUT_NAMESPACE, "Namespace", 
                "The namespace part of the effect ID (e.g., 'minecraft')", NodeDataType.STRING, this);
        addOutputPort(namespaceOutput);
        
        IPort effectPathOutput = new BasePort(OUTPUT_EFFECT_PATH, "Effect Path", 
                "The path part of the effect ID (e.g., 'speed')", NodeDataType.STRING, this);
        addOutputPort(effectPathOutput);
        
        IPort isBeneficialOutput = new BasePort(OUTPUT_IS_BENEFICIAL, "Is Beneficial", 
                "Whether the effect is beneficial or harmful", NodeDataType.BOOLEAN, this);
        addOutputPort(isBeneficialOutput);
        
        IPort effectTypeOutput = new BasePort(OUTPUT_EFFECT_TYPE, "Effect Type", 
                "The type of effect (e.g., movement, combat, etc.)", NodeDataType.STRING, this);
        addOutputPort(effectTypeOutput);
        
        // 更新输出值
        updateOutputs();
    }
    
    /**
     * 节点的计算逻辑
     * @param context 执行上下文
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 由于这是一个UI选择器节点，主要由用户交互驱动
        // 仅需确保输出值与当前选择一致
        updateOutputs();
    }
    
    /**
     * 设置选中的效果ID
     * @param effectId 效果ID，例如 "minecraft:speed"
     */
    public void setSelectedEffect(String effectId) {
        if (effectId == null || effectId.isEmpty()) {
            effectId = "minecraft:speed"; // 防止无效输入
        }
        
        if (!this.selectedEffect.equals(effectId)) {
            this.selectedEffect = effectId;
            updateOutputs();
            markDirty();
        }
    }
    
    /**
     * 判断效果是否是有益效果
     * @return 是否为有益效果
     */
    private boolean isEffectBeneficial() {
        // 在实际应用中，这应该通过Minecraft API查询效果类型
        // 这里为了演示，我们根据效果ID进行简单判断
        
        String path = selectedEffect.contains(":") ? 
                selectedEffect.split(":", 2)[1] : selectedEffect;
        
        // 默认有益效果列表
        String[] beneficialEffects = {
            "speed", "haste", "strength", "instant_health", "jump_boost",
            "regeneration", "resistance", "fire_resistance", "water_breathing",
            "invisibility", "night_vision", "health_boost", "absorption",
            "saturation", "luck", "slow_falling", "conduit_power", "dolphins_grace"
        };
        
        for (String effect : beneficialEffects) {
            if (path.equals(effect)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 计算效果的类型分类
     * @return 效果类型
     */
    private String calculateEffectType() {
        String path = selectedEffect.contains(":") ? 
                selectedEffect.split(":", 2)[1] : selectedEffect;
        
        // 移动类效果
        return switch (path) {
            case "speed", "slowness", "jump_boost", "slow_falling", "levitation", "dolphins_grace" -> "movement";

// 战斗类效果
            case "strength", "weakness", "resistance", "instant_damage", "instant_health", "regeneration" -> "combat";

// 生存类效果
            case "fire_resistance", "water_breathing", "night_vision", "invisibility", "conduit_power" -> "survival";

// 挖掘类效果
            case "haste", "mining_fatigue" -> "mining";

// 有害状态类效果
            case "poison", "wither", "hunger", "nausea" -> "negative_status";
            default -> "other";
        };

    }
    
    /**
     * 更新输出端口的值
     */
    private void updateOutputs() {
        // 解析效果ID的命名空间和路径部分
        String namespace = "minecraft";
        String path = "speed";
        
        if (selectedEffect.contains(":")) {
            String[] parts = selectedEffect.split(":", 2);
            namespace = parts[0];
            path = parts[1];
        } else {
            // 如果没有命名空间，假定为minecraft
            path = selectedEffect;
        }
        
        // 确定是否为模组效果
        boolean isModded = !namespace.equals("minecraft");
        
        // 判断是否为有益效果
        boolean isBeneficial = isEffectBeneficial();
        
        // 计算效果类型
        String effectType = calculateEffectType();
        
        // 更新输出值
        outputValues.put(OUTPUT_EFFECT_ID, selectedEffect);
        outputValues.put(OUTPUT_IS_MODDED, isModded);
        outputValues.put(OUTPUT_NAMESPACE, namespace);
        outputValues.put(OUTPUT_EFFECT_PATH, path);
        outputValues.put(OUTPUT_IS_BENEFICIAL, isBeneficial);
        outputValues.put(OUTPUT_EFFECT_TYPE, effectType);
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getSelectedEffect() {
        return selectedEffect;
    }
    
    public boolean isAllowModded() {
        return allowModded;
    }
    
    public void setAllowModded(boolean allowModded) {
        this.allowModded = allowModded;
        // 如果设置为不允许模组效果，且当前选中的是模组效果，则重置为默认效果
        if (!allowModded && !selectedEffect.startsWith("minecraft:")) {
            setSelectedEffect("minecraft:speed");
        }
    }
    
    public boolean isShowBeneficialOnly() {
        return showBeneficialOnly;
    }
    
    public void setShowBeneficialOnly(boolean showBeneficialOnly) {
        this.showBeneficialOnly = showBeneficialOnly;
        // 如果启用了只显示有益效果，应该关闭只显示有害效果选项
        if (showBeneficialOnly && showHarmfulOnly) {
            this.showHarmfulOnly = false;
        }
        // 这个属性不影响输出，只影响UI显示，所以不需要markDirty()
    }
    
    public boolean isShowHarmfulOnly() {
        return showHarmfulOnly;
    }
    
    public void setShowHarmfulOnly(boolean showHarmfulOnly) {
        this.showHarmfulOnly = showHarmfulOnly;
        // 如果启用了只显示有害效果，应该关闭只显示有益效果选项
        if (showHarmfulOnly && showBeneficialOnly) {
            this.showBeneficialOnly = false;
        }
        // 这个属性不影响输出，只影响UI显示，所以不需要markDirty()
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("selectedEffect", getSelectedEffect());
        state.put("allowModded", isAllowModded());
        state.put("showBeneficialOnly", isShowBeneficialOnly());
        state.put("showHarmfulOnly", isShowHarmfulOnly());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            // 先设置属性
            if (stateMap.containsKey("allowModded")) {
                Object allowMod = stateMap.get("allowModded");
                if (allowMod instanceof Boolean) {
                    setAllowModded((Boolean) allowMod);
                }
            }
            
            if (stateMap.containsKey("showBeneficialOnly")) {
                Object beneficial = stateMap.get("showBeneficialOnly");
                if (beneficial instanceof Boolean) {
                    setShowBeneficialOnly((Boolean) beneficial);
                }
            }
            
            if (stateMap.containsKey("showHarmfulOnly")) {
                Object harmful = stateMap.get("showHarmfulOnly");
                if (harmful instanceof Boolean) {
                    setShowHarmfulOnly((Boolean) harmful);
                }
            }
            
            // 最后设置选中的效果ID
            if (stateMap.containsKey("selectedEffect")) {
                Object selectedEff = stateMap.get("selectedEffect");
                if (selectedEff instanceof String) {
                    setSelectedEffect((String) selectedEff);
                }
            }
        }
    }

    // 添加 getDescription 方法
    @Override
    public String getDescription() {
        return "Allows selection of a Minecraft status effect type";
    }

    // 添加 getDisplayName 方法
    @Override
    public String getDisplayName() {
        return "Effect Type Selector";
    }
} 