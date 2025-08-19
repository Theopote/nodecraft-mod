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
 * 实体类型选择器节点，用于在UI中选择Minecraft实体
 */
@NodeInfo(
    id = "inputs.selectors.entity_type_selector",
    displayName = "实体类型选择器",
    description = "允许选择Minecraft实体类型",
    category = "inputs.selectors"
)
public class EntityTypeSelectorNode extends BaseNode {
    
    // --- 节点属性 ---
    private String selectedEntity = "minecraft:pig"; // 默认选择猪实体
    private boolean allowModded = true; // 是否允许选择模组实体
    private boolean categoryFilter = false; // 是否启用分类过滤
    private String category = "all"; // 实体分类：all, passive, hostile, neutral, boss, etc.
    
    // --- 输出端口 ---
    private static final String OUTPUT_ENTITY_ID = "output_entity_id";
    private static final String OUTPUT_IS_MODDED = "output_is_modded";
    private static final String OUTPUT_NAMESPACE = "output_namespace";
    private static final String OUTPUT_ENTITY_PATH = "output_entity_path";
    private static final String OUTPUT_CATEGORY = "output_category";
    
    /**
     * 构造一个新的实体类型选择器节点
     */
    public EntityTypeSelectorNode() {
        // 使用新的分类命名 - inputs.selectors.entity_type_selector
        super(UUID.randomUUID(), "inputs.selectors.entity_type_selector");
        
        // 创建并添加输出端口
        IPort entityIdOutput = new BasePort(OUTPUT_ENTITY_ID, "Entity ID", 
                "The selected entity's full identifier", NodeDataType.STRING, this);
        addOutputPort(entityIdOutput);
        
        IPort isModdedOutput = new BasePort(OUTPUT_IS_MODDED, "Is Modded", 
                "Whether the selected entity is from a mod", NodeDataType.BOOLEAN, this);
        addOutputPort(isModdedOutput);
        
        IPort namespaceOutput = new BasePort(OUTPUT_NAMESPACE, "Namespace", 
                "The namespace part of the entity ID (e.g., 'minecraft')", NodeDataType.STRING, this);
        addOutputPort(namespaceOutput);
        
        IPort entityPathOutput = new BasePort(OUTPUT_ENTITY_PATH, "Entity Path", 
                "The path part of the entity ID (e.g., 'pig')", NodeDataType.STRING, this);
        addOutputPort(entityPathOutput);
        
        IPort categoryOutput = new BasePort(OUTPUT_CATEGORY, "Category", 
                "The entity's category (hostile, passive, etc.)", NodeDataType.STRING, this);
        addOutputPort(categoryOutput);
        
        // 更新输出值
        updateOutputs();
    }
    
    @Override
    public String getDescription() {
        return "Allows selection of a Minecraft entity type";
    }
    
    @Override
    public String getDisplayName() {
        return "Entity Type Selector";
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
     * 设置选中的实体ID
     * @param entityId 实体ID，例如 "minecraft:pig"
     */
    public void setSelectedEntity(String entityId) {
        if (entityId == null || entityId.isEmpty()) {
            entityId = "minecraft:pig"; // 防止无效输入
        }
        
        if (!this.selectedEntity.equals(entityId)) {
            this.selectedEntity = entityId;
            updateOutputs();
            markDirty();
        }
    }
    
    /**
     * 根据当前选择计算实体的分类
     * @return 实体分类
     */
    private String calculateEntityCategory() {
        // 在实际应用中，这应该通过Minecraft API查询实体分类
        // 这里为了演示，我们根据实体ID进行简单判断
        
        String path = selectedEntity.contains(":") ? 
                selectedEntity.split(":", 2)[1] : selectedEntity;
        
        // 一些简单的分类规则
        return switch (path) {
            case "zombie", "skeleton", "creeper", "spider", "enderman" -> "hostile";
            case "pig", "cow", "sheep", "chicken", "rabbit" -> "passive";
            case "wolf", "bee", "dolphin", "panda", "llama" -> "neutral";
            case "ender_dragon", "wither" -> "boss";
            case "villager", "wandering_trader" -> "npc";
            case "item", "arrow", "experience_orb" -> "other";
            default -> "unknown";
        };

    }
    
    /**
     * 更新输出端口的值
     */
    private void updateOutputs() {
        // 解析实体ID的命名空间和路径部分
        String namespace = "minecraft";
        String path = "pig";
        
        if (selectedEntity.contains(":")) {
            String[] parts = selectedEntity.split(":", 2);
            namespace = parts[0];
            path = parts[1];
        } else {
            // 如果没有命名空间，假定为minecraft
            path = selectedEntity;
        }
        
        // 确定是否为模组实体
        boolean isModded = !namespace.equals("minecraft");
        
        // 计算实体分类
        String category = calculateEntityCategory();
        
        // 更新输出值
        outputValues.put(OUTPUT_ENTITY_ID, selectedEntity);
        outputValues.put(OUTPUT_IS_MODDED, isModded);
        outputValues.put(OUTPUT_NAMESPACE, namespace);
        outputValues.put(OUTPUT_ENTITY_PATH, path);
        outputValues.put(OUTPUT_CATEGORY, category);
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getSelectedEntity() {
        return selectedEntity;
    }
    
    public boolean isAllowModded() {
        return allowModded;
    }
    
    public void setAllowModded(boolean allowModded) {
        this.allowModded = allowModded;
        // 如果设置为不允许模组实体，且当前选中的是模组实体，则重置为默认实体
        if (!allowModded && !selectedEntity.startsWith("minecraft:")) {
            setSelectedEntity("minecraft:pig");
        }
    }
    
    public boolean isCategoryFilter() {
        return categoryFilter;
    }
    
    public void setCategoryFilter(boolean categoryFilter) {
        this.categoryFilter = categoryFilter;
        // 这个属性不影响输出，只影响UI显示，所以不需要markDirty()
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        if (category == null || category.isEmpty()) {
            category = "all";
        }
        
        if (!this.category.equals(category)) {
            this.category = category;
            // 这个属性不影响输出，只影响UI显示，所以不需要markDirty()
        }
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("selectedEntity", getSelectedEntity());
        state.put("allowModded", isAllowModded());
        state.put("categoryFilter", isCategoryFilter());
        state.put("category", getCategory());
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
                if (catFilter instanceof Boolean) {
                    setCategoryFilter((Boolean) catFilter);
                }
            }
            
            if (stateMap.containsKey("category")) {
                Object cat = stateMap.get("category");
                if (cat instanceof String) {
                    setCategory((String) cat);
                }
            }
            
            // 最后设置选中的实体ID
            if (stateMap.containsKey("selectedEntity")) {
                Object selectedEnt = stateMap.get("selectedEntity");
                if (selectedEnt instanceof String) {
                    setSelectedEntity((String) selectedEnt);
                }
            }
        }
    }
} 