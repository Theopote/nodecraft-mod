package com.nodecraft.nodesystem.nodes.utilities.organization;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Align Nodes 节点: 对齐选中的节点
 * 这是一个特殊的节点，主要作用于编辑器UI层面，对选中节点的位置进行调整
 */
@NodeInfo(
    id = "utilities.organization.align_nodes",
    displayName = "节点对齐",
    description = "对齐选中的节点",
    category = "utilities.organization"
)
public class AlignNodesNode extends BaseNode {

    // --- 节点属性 ---
    private AlignmentType alignmentType = AlignmentType.HORIZONTAL_CENTER; // 对齐类型
    private boolean distributeEvenly = false; // 是否均匀分布
    private double spacing = 10.0; // 节点间距（均匀分布时使用）
    private String description = "对齐选中的节点";
    
    // --- 输入端口 IDs ---
    private static final String INPUT_NODES_ID = "input_nodes";
    private static final String INPUT_ALIGNMENT_TYPE_ID = "input_alignment_type";
    private static final String INPUT_DISTRIBUTE_ID = "input_distribute";
    private static final String INPUT_SPACING_ID = "input_spacing";
    private static final String INPUT_TRIGGER_ID = "input_trigger";
    
    // --- 输出端口 IDs ---
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_NODES_ID = "output_nodes";
    
    /**
     * 对齐类型枚举
     */
    public enum AlignmentType {
        HORIZONTAL_LEFT("左对齐", "水平左对齐"),
        HORIZONTAL_CENTER("水平居中", "水平中心对齐"),
        HORIZONTAL_RIGHT("右对齐", "水平右对齐"),
        VERTICAL_TOP("顶部对齐", "垂直顶部对齐"),
        VERTICAL_CENTER("垂直居中", "垂直中心对齐"),
        VERTICAL_BOTTOM("底部对齐", "垂直底部对齐"),
        GRID("网格对齐", "按网格对齐"),
        COMPACT("紧凑排列", "紧凑排列节点");
        
        private final String displayName;
        private final String description;
        
        AlignmentType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 构造一个新的对齐节点节点
     */
    public AlignNodesNode() {
        super(UUID.randomUUID(), "utilities.organization.align_nodes");
        
        // 创建输入端口
        addInputPort(new BasePort(INPUT_NODES_ID, "Nodes", 
                "要对齐的节点列表", NodeDataType.LIST, this));
        
        addInputPort(new BasePort(INPUT_ALIGNMENT_TYPE_ID, "Alignment Type", 
                "对齐类型", NodeDataType.STRING, this));
        
        addInputPort(new BasePort(INPUT_DISTRIBUTE_ID, "Distribute Evenly", 
                "是否均匀分布", NodeDataType.BOOLEAN, this));
        
        addInputPort(new BasePort(INPUT_SPACING_ID, "Spacing", 
                "节点间距", NodeDataType.DOUBLE, this));
        
        addInputPort(new BasePort(INPUT_TRIGGER_ID, "Trigger", 
                "触发对齐操作", NodeDataType.ANY, this));
        
        // 创建输出端口
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "是否成功执行对齐操作", NodeDataType.BOOLEAN, this));
        
        addOutputPort(new BasePort(OUTPUT_NODES_ID, "Aligned Nodes", 
                "已对齐的节点列表", NodeDataType.LIST, this));
    }
    
    /**
     * 节点的计算逻辑
     * 对于AlignNodes节点，核心逻辑涉及对节点位置的调整
     */
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        boolean success = false;
        
        // 获取输入值
        Object nodesObj = inputValues.get(INPUT_NODES_ID);
        Object alignmentTypeObj = inputValues.get(INPUT_ALIGNMENT_TYPE_ID);
        Object distributeObj = inputValues.get(INPUT_DISTRIBUTE_ID);
        Object spacingObj = inputValues.get(INPUT_SPACING_ID);
        Object triggerObj = inputValues.get(INPUT_TRIGGER_ID); // 仅作为触发器
        
        // 只有在有触发信号且有效节点列表的情况下才执行对齐
        if (triggerObj != null && nodesObj instanceof List && !((List<?>) nodesObj).isEmpty()) {
            List<?> nodeList = (List<?>) nodesObj;
            
            // 确定对齐类型
            AlignmentType alignmentType = this.alignmentType;
            if (alignmentTypeObj instanceof String) {
                try {
                    alignmentType = AlignmentType.valueOf((String) alignmentTypeObj);
                } catch (IllegalArgumentException ignored) {
                    // 使用默认值
                }
            }
            
            // 确定是否均匀分布
            boolean distributeEvenly = this.distributeEvenly;
            if (distributeObj instanceof Boolean) {
                distributeEvenly = (Boolean) distributeObj;
            }
            
            // 确定节点间距
            double spacing = this.spacing;
            if (spacingObj instanceof Number) {
                spacing = ((Number) spacingObj).doubleValue();
            }
            
            // 执行节点对齐
            // 注意：实际的对齐操作应该由Editor实现，因为涉及节点位置的变更
            // 这里只是模拟对齐操作的逻辑流程
            success = performAlignment(nodeList, alignmentType, distributeEvenly, spacing, context);
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_NODES_ID, nodesObj); // 将输入节点列表直接传递到输出
    }
    
    /**
     * 执行对齐操作
     * 注意：此方法在实际实现中需要与编辑器紧密集成
     */
    private boolean performAlignment(List<?> nodeList, AlignmentType alignmentType, 
                                    boolean distributeEvenly, double spacing, 
                                    @Nullable ExecutionContext context) {
        // 此处的实现是一个示例，实际的对齐逻辑需要依赖编辑器环境
        // 例如，获取节点的实际位置和尺寸信息，以及更新它们的位置
        
        if (context == null) {
            return false;
        }
        
        // 提取有效的节点ID
        List<UUID> validNodeIds = nodeList.stream()
                .filter(obj -> obj instanceof UUID)
                .map(obj -> (UUID) obj)
                .toList();
        
        if (validNodeIds.isEmpty()) {
            return false;
        }
        
        // 在这里调用编辑器的对齐功能
        // context.getEditor().alignNodes(validNodeIds, alignmentType, distributeEvenly, spacing);
        
        // 由于此处无法访问实际的编辑器，所以只返回一个成功标志
        return true;
    }
    
    // --- Getters/Setters for Properties ---
    
    public AlignmentType getAlignmentType() {
        return alignmentType;
    }
    
    public void setAlignmentType(AlignmentType alignmentType) {
        this.alignmentType = alignmentType;
        markDirty();
    }
    
    public boolean isDistributeEvenly() {
        return distributeEvenly;
    }
    
    public void setDistributeEvenly(boolean distributeEvenly) {
        this.distributeEvenly = distributeEvenly;
        markDirty();
    }
    
    public double getSpacing() {
        return spacing;
    }
    
    public void setSpacing(double spacing) {
        this.spacing = Math.max(0, spacing);
        markDirty();
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        Object[] state = new Object[3];
        state[0] = alignmentType.name();
        state[1] = distributeEvenly;
        state[2] = spacing;
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
                if (objState[0] instanceof String) {
                    try {
                        alignmentType = AlignmentType.valueOf((String) objState[0]);
                    } catch (IllegalArgumentException e) {
                        alignmentType = AlignmentType.HORIZONTAL_CENTER; // 默认值
                    }
                }
                if (objState[1] instanceof Boolean) {
                    distributeEvenly = (Boolean) objState[1];
                }
                if (objState[2] instanceof Number) {
                    spacing = ((Number) objState[2]).doubleValue();
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return this.description;
    }
} 