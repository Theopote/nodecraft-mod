package com.nodecraft.nodesystem.preview.elements;

import com.nodecraft.nodesystem.preview.AbstractPreviewElement;
import com.nodecraft.nodesystem.preview.PreviewOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

/**
 * 点预览元素
 * 用于显示单个点或点集合
 */
public class PointsElement extends AbstractPreviewElement {
    
    public PointsElement(String id, String ownerNodeId, Object data, PreviewOptions options) {
        super(id, ownerNodeId, data, options);
    }
    
    @Override
    protected void processData(Object data) {
        // TODO: 实现点数据处理
    }
    
    @Override
    public void render(MatrixStack matrices, Camera camera, float partialTicks, float globalOpacity) {
        // TODO: 实现点渲染
    }
    
    @Override
    public boolean shouldRender(Camera camera) {
        return true; // TODO: 实现渲染判断
    }
    
    @Override
    public void cleanup() {
        // TODO: 实现清理
    }
} 