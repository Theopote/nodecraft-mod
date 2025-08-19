package com.nodecraft.gui.layout;

import com.nodecraft.gui.screens.EditorConstants;

/**
 * 布局配置记录类，存储布局尺寸和比例的配置
 */
public record LayoutConfig(
    float nodePanelRatio,
    float canvasRatio,
    float propertyPanelRatio,
    float minNodePanelWidth,
    float minCanvasWidth,
    float minPropertyPanelWidth
) {
    /**
     * 验证构造函数
     */
    public LayoutConfig {
        // 添加验证逻辑
        float sum = nodePanelRatio + canvasRatio + propertyPanelRatio;
        if (Math.abs(sum - 1.0f) > 0.001f) {
            throw new IllegalArgumentException("布局比例之和必须等于 1.0，当前为：" + sum);
        }
    }
    
    /**
     * 创建一个临时的LayoutConfig，不检查比例总和
     * 主要用于拖拽面板时的中间状态
     * 
     * @param nodePanelRatio 节点面板比例
     * @param canvasRatio 画布比例
     * @param propertyPanelRatio 属性面板比例
     * @param minNodePanelWidth 最小节点面板宽度
     * @param minCanvasWidth 最小画布宽度
     * @param minPropertyPanelWidth 最小属性面板宽度
     * @param skipValidation 是否跳过验证
     * @return 创建的配置实例
     */
    public static LayoutConfig createWithoutValidation(
        float nodePanelRatio,
        float canvasRatio,
        float propertyPanelRatio,
        float minNodePanelWidth,
        float minCanvasWidth,
        float minPropertyPanelWidth
    ) {
        // 自动调整比例确保总和为1.0
        float sum = nodePanelRatio + canvasRatio + propertyPanelRatio;
        float adjustedNodePanelRatio = nodePanelRatio;
        float adjustedCanvasRatio = canvasRatio;
        float adjustedPropertyPanelRatio = propertyPanelRatio;
        
        if (Math.abs(sum - 1.0f) > 0.001f) {
            // 计算调整因子
            float adjustFactor = 1.0f / sum;
            
            // 按比例调整各面板比例
            adjustedNodePanelRatio = nodePanelRatio * adjustFactor;
            adjustedCanvasRatio = canvasRatio * adjustFactor;
            adjustedPropertyPanelRatio = propertyPanelRatio * adjustFactor;
        }
        
        return new LayoutConfig(
            adjustedNodePanelRatio,
            adjustedCanvasRatio,
            adjustedPropertyPanelRatio,
            minNodePanelWidth,
            minCanvasWidth,
            minPropertyPanelWidth
        );
    }
    
    /**
     * 创建默认布局配置
     * @return 默认配置实例
     */
    public static LayoutConfig createDefault() {
        return new LayoutConfig(
            EditorConstants.NODE_PANEL_WIDTH_RATIO,
            EditorConstants.CANVAS_WIDTH_RATIO,
            EditorConstants.PROPERTY_PANEL_WIDTH_RATIO,
            EditorConstants.MIN_PANEL_WIDTH,
            EditorConstants.MIN_CANVAS_WIDTH,
            EditorConstants.MIN_PANEL_WIDTH
        );
    }
} 