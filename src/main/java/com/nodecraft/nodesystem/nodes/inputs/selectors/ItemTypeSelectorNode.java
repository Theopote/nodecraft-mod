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
 * 物品类型选择器节点，用于在UI中搜索和选择Minecraft物品
 */
@NodeInfo(
    id = "inputs.selectors.item_type_selector",
    displayName = "物品类型选择器",
    description = "允许选择Minecraft物品类型",
    category = "inputs.selectors"
)
public class ItemTypeSelectorNode extends BaseNode {
    
    // --- 节点属性 ---
    private String selectedItem = "minecraft:diamond"; // 默认选择钻石
    private boolean allowModded = true; // 是否允许选择模组物品
    private boolean showCreativeTabsOnly = false; // 是否只显示创造模式物品栏中的物品
    private String categoryFilter = "all"; // 物品分类过滤
    
    // --- 输出端口 ---
    private static final String OUTPUT_ITEM_ID = "output_item_id";
    private static final String OUTPUT_ITEM_INFO = "output_item_info";
    private static final String OUTPUT_IS_MODDED = "output_is_modded";
    private static final String OUTPUT_NAMESPACE = "output_namespace";
    private static final String OUTPUT_ITEM_PATH = "output_item_path";
    private static final String OUTPUT_CATEGORY = "output_category";
    
    /**
     * 构造一个新的物品类型选择器节点
     */
    public ItemTypeSelectorNode() {
        // 使用新的分类命名 - inputs.selectors.item_type_selector
        super(UUID.randomUUID(), "inputs.selectors.item_type_selector");
        
        // 创建并添加输出端口
        IPort itemIdOutput = new BasePort(OUTPUT_ITEM_ID, "Item ID", 
                "The selected item's full identifier", NodeDataType.STRING, this);
        addOutputPort(itemIdOutput);
        
        IPort itemInfoOutput = new BasePort(OUTPUT_ITEM_INFO, "Item Info", 
                "Detailed information about the selected item", NodeDataType.ANY, this);
        addOutputPort(itemInfoOutput);
        
        IPort isModdedOutput = new BasePort(OUTPUT_IS_MODDED, "Is Modded", 
                "Whether the selected item is from a mod", NodeDataType.BOOLEAN, this);
        addOutputPort(isModdedOutput);
        
        IPort namespaceOutput = new BasePort(OUTPUT_NAMESPACE, "Namespace", 
                "The namespace part of the item ID (e.g., 'minecraft')", NodeDataType.STRING, this);
        addOutputPort(namespaceOutput);
        
        IPort itemPathOutput = new BasePort(OUTPUT_ITEM_PATH, "Item Path", 
                "The path part of the item ID (e.g., 'diamond')", NodeDataType.STRING, this);
        addOutputPort(itemPathOutput);
        
        IPort categoryOutput = new BasePort(OUTPUT_CATEGORY, "Category", 
                "The item's category (tool, weapon, food, etc.)", NodeDataType.STRING, this);
        addOutputPort(categoryOutput);
        
        // 更新输出值
        updateOutputs();
    }
    
    @Override
    public String getDescription() {
        return "Allows search and selection of a Minecraft item type";
    }
    
    @Override
    public String getDisplayName() {
        return "Item Type Selector";
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
     * 设置选中的物品ID
     * @param itemId 物品ID，例如 "minecraft:diamond"
     */
    public void setSelectedItem(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            itemId = "minecraft:diamond"; // 防止无效输入
        }
        
        if (!this.selectedItem.equals(itemId)) {
            this.selectedItem = itemId;
            updateOutputs();
            markDirty();
        }
    }
    
    /**
     * 根据当前选择计算物品的分类
     * @return 物品分类
     */
    private String calculateItemCategory() {
        // 在实际应用中，这应该通过Minecraft API查询物品分类
        // 这里为了演示，我们根据物品ID进行简单判断
        
        String path = selectedItem.contains(":") ? 
                selectedItem.split(":", 2)[1] : selectedItem;
        
        // 一些简单的分类规则
        if (path.contains("sword") || path.contains("bow") || path.contains("arrow") || 
            path.equals("trident") || path.contains("_axe")) {
            return "weapon";
        } else if (path.contains("pickaxe") || path.contains("shovel") || path.contains("hoe") ||
                  path.equals("shears") || path.equals("flint_and_steel")) {
            return "tool";
        } else if (path.contains("helmet") || path.contains("chestplate") || 
                  path.contains("leggings") || path.contains("boots")) {
            return "armor";
        } else if (path.contains("apple") || path.contains("bread") || path.contains("beef") ||
                  path.contains("porkchop") || path.contains("fish") || path.contains("carrot") ||
                  path.contains("potato") || path.contains("stew") || path.contains("soup")) {
            return "food";
        } else if (path.contains("ore") || path.equals("diamond") || path.equals("gold_ingot") ||
                  path.equals("iron_ingot") || path.equals("emerald") || path.equals("coal")) {
            return "resource";
        } else if (path.contains("potion") || path.equals("enchanted_book") || 
                  path.equals("experience_bottle")) {
            return "magical";
        }
        
        return "misc";
    }
    
    /**
     * 更新输出端口的值
     */
    private void updateOutputs() {
        // 解析物品ID的命名空间和路径部分
        String namespace = "minecraft";
        String path = "diamond";
        
        if (selectedItem.contains(":")) {
            String[] parts = selectedItem.split(":", 2);
            namespace = parts[0];
            path = parts[1];
        } else {
            // 如果没有命名空间，假定为minecraft
            path = selectedItem;
        }
        
        // 确定是否为模组物品
        boolean isModded = !namespace.equals("minecraft");
        
        // 计算物品分类
        String category = calculateItemCategory();
        
        // 在实际应用中，这里会创建一个真实的ItemInfo对象
        // 为了演示，我们这里使用String代替
        Object itemInfo = selectedItem;
        
        // 更新输出值
        outputValues.put(OUTPUT_ITEM_ID, selectedItem);
        outputValues.put(OUTPUT_ITEM_INFO, itemInfo);
        outputValues.put(OUTPUT_IS_MODDED, isModded);
        outputValues.put(OUTPUT_NAMESPACE, namespace);
        outputValues.put(OUTPUT_ITEM_PATH, path);
        outputValues.put(OUTPUT_CATEGORY, category);
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getSelectedItem() {
        return selectedItem;
    }
    
    public boolean isAllowModded() {
        return allowModded;
    }
    
    public void setAllowModded(boolean allowModded) {
        this.allowModded = allowModded;
        // 如果设置为不允许模组物品，且当前选中的是模组物品，则重置为默认物品
        if (!allowModded && !selectedItem.startsWith("minecraft:")) {
            setSelectedItem("minecraft:diamond");
        }
    }
    
    public boolean isShowCreativeTabsOnly() {
        return showCreativeTabsOnly;
    }
    
    public void setShowCreativeTabsOnly(boolean showCreativeTabsOnly) {
        this.showCreativeTabsOnly = showCreativeTabsOnly;
        // 这个属性不影响输出，只影响UI显示，所以不需要markDirty()
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
        state.put("selectedItem", getSelectedItem());
        state.put("allowModded", isAllowModded());
        state.put("showCreativeTabsOnly", isShowCreativeTabsOnly());
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
            
            if (stateMap.containsKey("showCreativeTabsOnly")) {
                Object showCreative = stateMap.get("showCreativeTabsOnly");
                if (showCreative instanceof Boolean) {
                    setShowCreativeTabsOnly((Boolean) showCreative);
                }
            }
            
            if (stateMap.containsKey("categoryFilter")) {
                Object catFilter = stateMap.get("categoryFilter");
                if (catFilter instanceof String) {
                    setCategoryFilter((String) catFilter);
                }
            }
            
            // 最后设置选中的物品ID
            if (stateMap.containsKey("selectedItem")) {
                Object selectedItm = stateMap.get("selectedItem");
                if (selectedItm instanceof String) {
                    setSelectedItem((String) selectedItm);
                }
            }
        }
    }
} 