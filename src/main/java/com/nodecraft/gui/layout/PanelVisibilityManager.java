package com.nodecraft.gui.layout;

import com.nodecraft.core.NodeCraft;

/**
 * 管理编辑器面板可见性的类
 */
public class PanelVisibilityManager {
    
    // 面板可见性状态
    private boolean nodePanelVisible = true;
    private boolean propertyPanelVisible = true;
    
    // 上次的布局配置，用于恢复
    private float lastNodePanelRatio = 0.0f;
    private float lastPropertyPanelRatio = 0.0f;
    
    /**
     * 切换节点面板的可见性
     * @param layoutConfig 当前布局配置
     * @return 更新后的布局配置
     */
    public LayoutConfig toggleNodePanel(LayoutConfig layoutConfig) {
        if (nodePanelVisible) {
            // 存储当前比例用于恢复
            lastNodePanelRatio = layoutConfig.nodePanelRatio();
            
            // 创建新的布局配置，节点面板比例为0，画布吸收所有比例
            float newCanvasRatio = layoutConfig.canvasRatio() + lastNodePanelRatio;
            
            LayoutConfig newConfig = new LayoutConfig(
                0, // 节点面板比例为0
                newCanvasRatio,
                layoutConfig.propertyPanelRatio(),
                layoutConfig.minNodePanelWidth(),
                layoutConfig.minCanvasWidth(),
                layoutConfig.minPropertyPanelWidth()
            );
            
            nodePanelVisible = false;
            NodeCraft.LOGGER.info("隐藏节点库面板");
            return newConfig;
        } else {
            // 恢复节点面板
            float restoredNodePanelRatio = lastNodePanelRatio > 0 ? lastNodePanelRatio : 0.20f;
            
            // 从画布中减去节点面板的比例
            float newCanvasRatio = layoutConfig.canvasRatio() - restoredNodePanelRatio;
            
            // 确保画布比例不会低于最小值
            if (newCanvasRatio < 0.1f) {
                newCanvasRatio = 0.1f;
                restoredNodePanelRatio = layoutConfig.canvasRatio() - 0.1f;
            }
            
            LayoutConfig newConfig = new LayoutConfig(
                restoredNodePanelRatio,
                newCanvasRatio,
                layoutConfig.propertyPanelRatio(),
                layoutConfig.minNodePanelWidth(),
                layoutConfig.minCanvasWidth(),
                layoutConfig.minPropertyPanelWidth()
            );
            
            nodePanelVisible = true;
            NodeCraft.LOGGER.info("显示节点库面板");
            return newConfig;
        }
    }
    
    /**
     * 切换属性面板的可见性
     * @param layoutConfig 当前布局配置
     * @return 更新后的布局配置
     */
    public LayoutConfig togglePropertyPanel(LayoutConfig layoutConfig) {
        if (propertyPanelVisible) {
            // 存储当前比例用于恢复
            lastPropertyPanelRatio = layoutConfig.propertyPanelRatio();
            
            // 创建新的布局配置，属性面板比例为0，画布吸收所有比例
            float newCanvasRatio = layoutConfig.canvasRatio() + lastPropertyPanelRatio;
            
            LayoutConfig newConfig = new LayoutConfig(
                layoutConfig.nodePanelRatio(),
                newCanvasRatio,
                0, // 属性面板比例为0
                layoutConfig.minNodePanelWidth(),
                layoutConfig.minCanvasWidth(),
                layoutConfig.minPropertyPanelWidth()
            );
            
            propertyPanelVisible = false;
            NodeCraft.LOGGER.info("隐藏属性面板");
            return newConfig;
        } else {
            // 恢复属性面板
            float restoredPropertyPanelRatio = lastPropertyPanelRatio > 0 ? lastPropertyPanelRatio : 0.25f;
            
            // 从画布中减去属性面板的比例
            float newCanvasRatio = layoutConfig.canvasRatio() - restoredPropertyPanelRatio;
            
            // 确保画布比例不会低于最小值
            if (newCanvasRatio < 0.1f) {
                newCanvasRatio = 0.1f;
                restoredPropertyPanelRatio = layoutConfig.canvasRatio() - 0.1f;
            }
            
            LayoutConfig newConfig = new LayoutConfig(
                layoutConfig.nodePanelRatio(),
                newCanvasRatio,
                restoredPropertyPanelRatio,
                layoutConfig.minNodePanelWidth(),
                layoutConfig.minCanvasWidth(),
                layoutConfig.minPropertyPanelWidth()
            );
            
            propertyPanelVisible = true;
            NodeCraft.LOGGER.info("显示属性面板");
            return newConfig;
        }
    }
    
    /**
     * 检查节点面板是否可见
     * @return 节点面板是否可见
     */
    public boolean isNodePanelVisible() {
        return nodePanelVisible;
    }
    
    /**
     * 检查属性面板是否可见
     * @return 属性面板是否可见
     */
    public boolean isPropertyPanelVisible() {
        return propertyPanelVisible;
    }
    
    /**
     * 在NodecraftScreen中使用前检查配置是否有效
     * @param config 要检查的配置
     * @return 是否有效(比例总和是否为1.0)
     */
    public boolean isValidConfig(LayoutConfig config) {
        float sum = config.nodePanelRatio() + config.canvasRatio() + config.propertyPanelRatio();
        return Math.abs(sum - 1.0f) <= 0.001f;
    }
} 