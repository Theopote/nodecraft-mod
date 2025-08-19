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
 * 方块类型选择器节点，用于在UI中选择Minecraft方块
 */
@NodeInfo(
    id = "inputs.selectors.block_type_selector",
    displayName = "方块类型选择器",
    description = "允许选择Minecraft方块类型",
    category = "inputs.selectors"
)
public class BlockTypeSelectorNode extends BaseNode {
    
    // --- 节点属性 ---
    private String selectedBlock = "minecraft:stone"; // 默认选择石头方块
    private boolean showVariants = true; // 是否显示方块变种（如不同朝向、状态等）
    private boolean allowModded = true; // 是否允许选择模组方块
    
    // --- 输出端口 ---
    private static final String OUTPUT_BLOCK_ID = "output_block_id";
    private static final String OUTPUT_BLOCK_INFO = "output_block_info";
    private static final String OUTPUT_IS_MODDED = "output_is_modded";
    private static final String OUTPUT_NAMESPACE = "output_namespace";
    private static final String OUTPUT_BLOCK_PATH = "output_block_path";
    
    /**
     * 构造一个新的方块类型选择器节点
     */
    public BlockTypeSelectorNode() {
        // 使用新的分类命名 - inputs.selectors.block_type_selector
        super(UUID.randomUUID(), "inputs.selectors.block_type_selector");
        
        // 创建并添加输出端口
        IPort blockIdOutput = new BasePort(OUTPUT_BLOCK_ID, "Block ID", 
                "The selected block's full identifier", NodeDataType.STRING, this);
        addOutputPort(blockIdOutput);
        
        IPort blockInfoOutput = new BasePort(OUTPUT_BLOCK_INFO, "Block Info", 
                "Detailed information about the selected block", NodeDataType.BLOCK_INFO, this);
        addOutputPort(blockInfoOutput);
        
        IPort isModdedOutput = new BasePort(OUTPUT_IS_MODDED, "Is Modded", 
                "Whether the selected block is from a mod", NodeDataType.BOOLEAN, this);
        addOutputPort(isModdedOutput);
        
        IPort namespaceOutput = new BasePort(OUTPUT_NAMESPACE, "Namespace", 
                "The namespace part of the block ID (e.g., 'minecraft')", NodeDataType.STRING, this);
        addOutputPort(namespaceOutput);
        
        IPort blockPathOutput = new BasePort(OUTPUT_BLOCK_PATH, "Block Path", 
                "The path part of the block ID (e.g., 'stone')", NodeDataType.STRING, this);
        addOutputPort(blockPathOutput);
        
        // 更新输出值
        updateOutputs();
    }
    
    @Override
    public String getDescription() {
        return "Allows selection of a Minecraft block type";
    }
    
    @Override
    public String getDisplayName() {
        return "Block Type Selector";
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
     * 设置选中的方块ID
     * @param blockId 方块ID，例如 "minecraft:stone"
     */
    public void setSelectedBlock(String blockId) {
        if (blockId == null || blockId.isEmpty()) {
            blockId = "minecraft:stone"; // 防止无效输入
        }
        
        if (!this.selectedBlock.equals(blockId)) {
            this.selectedBlock = blockId;
            updateOutputs();
            markDirty();
        }
    }
    
    /**
     * 更新输出端口的值
     */
    private void updateOutputs() {
        // 解析方块ID的命名空间和路径部分
        String namespace = "minecraft";
        String path = "stone";
        
        if (selectedBlock.contains(":")) {
            String[] parts = selectedBlock.split(":", 2);
            namespace = parts[0];
            path = parts[1];
        } else {
            // 如果没有命名空间，假定为minecraft
            path = selectedBlock;
        }
        
        // 确定是否为模组方块
        boolean isModded = !namespace.equals("minecraft");
        
        // 在实际应用中，这里会创建一个真实的BlockInfo对象
        // 为了演示，我们这里使用String代替
        Object blockInfo = selectedBlock;
        
        // 更新输出值
        outputValues.put(OUTPUT_BLOCK_ID, selectedBlock);
        outputValues.put(OUTPUT_BLOCK_INFO, blockInfo);
        outputValues.put(OUTPUT_IS_MODDED, isModded);
        outputValues.put(OUTPUT_NAMESPACE, namespace);
        outputValues.put(OUTPUT_BLOCK_PATH, path);
    }
    
    // --- Getters/Setters for Properties ---
    
    public String getSelectedBlock() {
        return selectedBlock;
    }
    
    public boolean isShowVariants() {
        return showVariants;
    }
    
    public void setShowVariants(boolean showVariants) {
        this.showVariants = showVariants;
        // 这个属性不影响输出，只影响UI显示，所以不需要markDirty()
    }
    
    public boolean isAllowModded() {
        return allowModded;
    }
    
    public void setAllowModded(boolean allowModded) {
        this.allowModded = allowModded;
        // 如果设置为不允许模组方块，且当前选中的是模组方块，则重置为默认方块
        if (!allowModded && !selectedBlock.startsWith("minecraft:")) {
            setSelectedBlock("minecraft:stone");
        }
    }
    
    // --- 节点状态序列化 ---
    
    @Override
    public Object getNodeState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("selectedBlock", getSelectedBlock());
        state.put("showVariants", isShowVariants());
        state.put("allowModded", isAllowModded());
        return state;
    }
    
    @Override
    public void setNodeState(Object state) {
        if (state instanceof java.util.Map) {
            java.util.Map<?, ?> stateMap = (java.util.Map<?, ?>) state;
            
            // 先设置属性
            if (stateMap.containsKey("showVariants")) {
                Object showVar = stateMap.get("showVariants");
                if (showVar instanceof Boolean) {
                    setShowVariants((Boolean) showVar);
                }
            }
            
            if (stateMap.containsKey("allowModded")) {
                Object allowMod = stateMap.get("allowModded");
                if (allowMod instanceof Boolean) {
                    setAllowModded((Boolean) allowMod);
                }
            }
            
            // 最后设置选中的方块ID
            if (stateMap.containsKey("selectedBlock")) {
                Object selectedBlk = stateMap.get("selectedBlock");
                if (selectedBlk instanceof String) {
                    setSelectedBlock((String) selectedBlk);
                }
            }
        }
    }
} 