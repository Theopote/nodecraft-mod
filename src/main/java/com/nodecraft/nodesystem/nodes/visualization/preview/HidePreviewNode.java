package com.nodecraft.nodesystem.nodes.visualization.preview;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Hide Preview 节点: 隐藏当前连接到此节点的预览
 */
@NodeInfo(
    id = "visualization.preview.hide_preview",
    displayName = "隐藏预览",
    description = "隐藏当前连接到此节点的预览",
    category = "visualization.preview"
)
public class HidePreviewNode extends BaseNode {

    // --- 输入端口 IDs ---
    private static final String INPUT_PREVIEW_ID_ID = "input_preview_id";
    private static final String INPUT_HIDE_ID = "input_hide";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_SUCCESS_ID = "output_success";
    private static final String OUTPUT_IS_HIDDEN_ID = "output_is_hidden";

    // --- 节点属性 ---
    private boolean isHidden = false; // 当前是否隐藏
    private String description = "隐藏当前连接到此节点的预览";

    // --- 构造函数 ---
    public HidePreviewNode() {
        super(UUID.randomUUID(), "visualization.preview.hide_preview");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_PREVIEW_ID_ID, "Preview ID", 
                "要控制的预览ID", NodeDataType.STRING, this));
        addInputPort(new BasePort(INPUT_HIDE_ID, "Hide", 
                "是否隐藏预览（true为隐藏，false为显示）", NodeDataType.BOOLEAN, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_SUCCESS_ID, "Success", 
                "操作是否成功", NodeDataType.BOOLEAN, this));
        addOutputPort(new BasePort(OUTPUT_IS_HIDDEN_ID, "Is Hidden", 
                "预览当前是否处于隐藏状态", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 默认输出值
        boolean success = false;
        boolean isHidden = this.isHidden;
        
        // 获取输入值
        Object previewIdObj = inputValues.get(INPUT_PREVIEW_ID_ID);
        Object hideObj = inputValues.get(INPUT_HIDE_ID);
        
        // 确定是否隐藏
        boolean shouldHide = this.isHidden;
        if (hideObj instanceof Boolean) {
            shouldHide = (Boolean) hideObj;
        }
        
        // 处理预览ID
        String previewId = null;
        if (previewIdObj instanceof String) {
            previewId = (String) previewIdObj;
        }
        
        // 检查必要的输入是否存在
        if (previewId != null) {
            try {
                /*
                在实际实现中，需要根据Minecraft API实现以下操作:
                1. 获取指定ID的预览
                2. 设置预览的可见性
                
                // 获取预览是否已存在
                // boolean previewExists = PreviewManager.previewExists(previewId);
                boolean previewExists = true; // 假设预览存在
                
                if (previewExists) {
                    if (shouldHide) {
                        // 隐藏预览
                        // PreviewManager.hidePreview(previewId);
                    } else {
                        // 显示预览
                        // PreviewManager.showPreview(previewId);
                    }
                    
                    isHidden = shouldHide;
                    this.isHidden = isHidden;
                    success = true;
                }
                */
                
                // 模拟操作 (在实际实现中替换为上面的逻辑)
                isHidden = shouldHide;
                this.isHidden = isHidden;
                success = true;
                
                // 打印调试信息
                System.out.println("模拟" + (isHidden ? "隐藏" : "显示") + "预览，ID: " + previewId);
            } catch (Exception e) {
                success = false;
                System.err.println("Error hiding/showing preview: " + e.getMessage());
            }
        }
        
        // 设置输出值
        outputValues.put(OUTPUT_SUCCESS_ID, success);
        outputValues.put(OUTPUT_IS_HIDDEN_ID, isHidden);
    }
    
    // --- Getters/Setters for Properties ---
    
    public boolean isHidden() {
        return isHidden;
    }
    
    public void setHidden(boolean hidden) {
        isHidden = hidden;
        markDirty();
    }
    
    /**
     * 保存节点状态
     */
    @Override
    public @Nullable Object getNodeState() {
        return isHidden;
    }
    
    /**
     * 恢复节点状态
     */
    @Override
    public void setNodeState(@Nullable Object state) {
        if (state instanceof Boolean) {
            isHidden = (Boolean) state;
        }
    }
} 