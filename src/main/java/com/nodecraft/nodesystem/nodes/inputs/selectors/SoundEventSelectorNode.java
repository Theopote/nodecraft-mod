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
 * 声音事件选择器节点，用于在UI中选择Minecraft声音事件
 */
@NodeInfo(
    id = "inputs.selectors.sound_event_selector",
    displayName = "声音事件选择器",
    description = "允许选择Minecraft声音事件",
    category = "inputs.selectors"
)
public class SoundEventSelectorNode extends BaseNode {
    
    // --- 节点属性 ---
    private String selectedSound = "minecraft:entity.player.levelup"; // 默认选择玩家升级声音
    private boolean allowModded = true; // 是否允许选择模组声音
    private String categoryFilter = "all"; // 声音分类过滤
    
    // --- 输出端口 ---
    private static final String OUTPUT_SOUND_ID = "output_sound_id";
    private static final String OUTPUT_IS_MODDED = "output_is_modded";
    private static final String OUTPUT_NAMESPACE = "output_namespace";
    private static final String OUTPUT_SOUND_PATH = "output_sound_path";
    private static final String OUTPUT_CATEGORY = "output_category";
    private static final String OUTPUT_SUBTITLE = "output_subtitle";
    
    /**
     * 构造一个新的声音事件选择器节点
     */
    public SoundEventSelectorNode() {
        // 使用新的分类命名 - inputs.selectors.sound_event_selector
        super(UUID.randomUUID(), "inputs.selectors.sound_event_selector");
        
        // 创建并添加输出端口
        IPort soundIdOutput = new BasePort(OUTPUT_SOUND_ID, "Sound ID", 
                "The selected sound's full identifier", NodeDataType.STRING, this);
        addOutputPort(soundIdOutput);
        
        IPort isModdedOutput = new BasePort(OUTPUT_IS_MODDED, "Is Modded", 
                "Whether the selected sound is from a mod", NodeDataType.BOOLEAN, this);
        addOutputPort(isModdedOutput);
        
        IPort namespaceOutput = new BasePort(OUTPUT_NAMESPACE, "Namespace", 
                "The namespace part of the sound ID (e.g., 'minecraft')", NodeDataType.STRING, this);
        addOutputPort(namespaceOutput);
        
        IPort soundPathOutput = new BasePort(OUTPUT_SOUND_PATH, "Sound Path", 
                "The path part of the sound ID (e.g., 'entity.player.levelup')", NodeDataType.STRING, this);
        addOutputPort(soundPathOutput);
        
        IPort categoryOutput = new BasePort(OUTPUT_CATEGORY, "Category", 
                "The sound's category (player, block, entity, etc.)", NodeDataType.STRING, this);
        addOutputPort(categoryOutput);
        
        IPort subtitleOutput = new BasePort(OUTPUT_SUBTITLE, "Subtitle", 
                "The subtitle text displayed when the sound is played", NodeDataType.STRING, this);
        addOutputPort(subtitleOutput);
        
        // 更新输出值
        updateOutputs();
    }
    
    @Override
    public String getDescription() {
        return "Allows selection of a Minecraft sound event";
    }
    
    @Override
    public String getDisplayName() {
        return "Sound Event Selector";
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
     * 设置选中的声音ID
     * @param soundId 声音ID，例如 "minecraft:entity.player.levelup"
     */
    public void setSelectedSound(String soundId) {
        if (soundId == null || soundId.isEmpty()) {
            soundId = "minecraft:entity.player.levelup"; // 防止无效输入
        }
        
        if (!this.selectedSound.equals(soundId)) {
            this.selectedSound = soundId;
            updateOutputs();
            markDirty();
        }
    }
    
    /**
     * 获取声音类别
     * 在Minecraft中，声音通常按照路径的第一个部分进行分类
     * @return 声音类别
     */
    private String getSoundCategory() {
        String path = selectedSound.contains(":") ? 
                selectedSound.split(":", 2)[1] : selectedSound;
        
        if (path.isEmpty()) {
            return "unknown";
        }
        
        String[] pathParts = path.split("\\.", 2);
        if (pathParts.length > 0) {
            String firstPart = pathParts[0];
            
            // 标准化一些特殊类别
            if (firstPart.equals("block") || firstPart.equals("blocks")) {
                return "block";
            } else if (firstPart.equals("entity") || firstPart.equals("entities") || 
                      firstPart.equals("mob") || firstPart.equals("mobs")) {
                return "entity";
            } else if (firstPart.equals("item") || firstPart.equals("items")) {
                return "item";
            } else if (firstPart.equals("music")) {
                return "music";
            } else if (firstPart.equals("ambient")) {
                return "ambient";
            } else if (firstPart.equals("weather")) {
                return "weather";
            } else if (firstPart.equals("player")) {
                return "player";
            } else if (firstPart.equals("ui") || firstPart.equals("interface")) {
                return "ui";
            }
            
            return firstPart;
        }
        
        return "unknown";
    }
    
    /**
     * 获取声音的字幕文本
     * 在实际实现中，这将从游戏资源获取
     * @return 字幕文本
     */
    private String getSoundSubtitle() {
        // 在实际应用中，这将从Minecraft的语言文件或API获取
        // 这里为了演示，我们根据声音ID生成一个模拟的字幕
        
        String path = selectedSound.contains(":") ? 
                selectedSound.split(":", 2)[1] : selectedSound;
        
        // 将路径点分隔符替换为空格，并将每个单词首字母大写
        if (!path.isEmpty()) {
            String[] parts = path.split("\\.");
            StringBuilder subtitle = new StringBuilder();
            
            for (String part : parts) {
                if (subtitle.length() > 0) {
                    subtitle.append(" ");
                }
                if (!part.isEmpty()) {
                    subtitle.append(Character.toUpperCase(part.charAt(0)));
                    if (part.length() > 1) {
                        subtitle.append(part.substring(1));
                    }
                }
            }
            
            return subtitle.toString();
        }
        
        return "Unknown Sound";
    }
    
    /**
     * 更新输出端口的值
     */
    private void updateOutputs() {
        // 解析声音ID的命名空间和路径部分
        String namespace = "minecraft";
        String path = "entity.player.levelup";
        
        if (selectedSound.contains(":")) {
            String[] parts = selectedSound.split(":", 2);
            namespace = parts[0];
            path = parts[1];
        } else {
            // 如果没有命名空间，假定为minecraft
            path = selectedSound;
        }
        
        // 确定是否为模组声音
        boolean isModded = !namespace.equals("minecraft");
        
        // 获取声音类别
        String category = getSoundCategory();
        
        // 获取声音字幕
        String subtitle = getSoundSubtitle();
        
        // 更新输出值
        outputValues.put(OUTPUT_SOUND_ID, selectedSound);
        outputValues.put(OUTPUT_IS_MODDED, isModded);
        outputValues.put(OUTPUT_NAMESPACE, namespace);
        outputValues.put(OUTPUT_SOUND_PATH, path);
        outputValues.put(OUTPUT_CATEGORY, category);
        outputValues.put(OUTPUT_SUBTITLE, subtitle);
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getSelectedSound() {
        return selectedSound;
    }
    
    public boolean isAllowModded() {
        return allowModded;
    }
    
    public void setAllowModded(boolean allowModded) {
        this.allowModded = allowModded;
        // 如果设置为不允许模组声音，且当前选中的是模组声音，则重置为默认声音
        if (!allowModded && !selectedSound.startsWith("minecraft:")) {
            setSelectedSound("minecraft:entity.player.levelup");
        }
    }
    
    public String getCategoryFilter() {
        return categoryFilter;
    }
    
    public void setCategoryFilter(String categoryFilter) {
        if (categoryFilter == null || categoryFilter.isEmpty()) {
            categoryFilter = "all";
        }
        
        if (!this.categoryFilter.equals(categoryFilter)) {
            this.categoryFilter = categoryFilter;
            // 这个属性不影响输出，只影响UI显示，所以不需要markDirty()
        }
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("selectedSound", getSelectedSound());
        state.put("allowModded", isAllowModded());
        state.put("categoryFilter", getCategoryFilter());
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
            
            if (stateMap.containsKey("categoryFilter")) {
                Object catFilter = stateMap.get("categoryFilter");
                if (catFilter instanceof String) {
                    setCategoryFilter((String) catFilter);
                }
            }
            
            // 最后设置选中的声音ID
            if (stateMap.containsKey("selectedSound")) {
                Object selectedSnd = stateMap.get("selectedSound");
                if (selectedSnd instanceof String) {
                    setSelectedSound((String) selectedSnd);
                }
            }
        }
    }
} 