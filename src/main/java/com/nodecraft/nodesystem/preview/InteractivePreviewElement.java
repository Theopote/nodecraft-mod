package com.nodecraft.nodesystem.preview;

import net.minecraft.util.math.Vec3d;

/**
 * 交互式预览元素接口
 * 用于支持鼠标交互的预览元素（如 Gizmo）
 */
public interface InteractivePreviewElement {
    
    /**
     * 检查射线是否与该元素相交
     * @param rayStart 射线起点
     * @param rayDirection 射线方向（单位向量）
     * @param maxDistance 最大检测距离
     * @return 是否相交
     */
    boolean intersectsRay(Vec3d rayStart, Vec3d rayDirection, double maxDistance);
    
    /**
     * 处理鼠标点击事件
     * @param rayStart 射线起点
     * @param rayDirection 射线方向
     * @param button 鼠标按键 (0=左键, 1=右键, 2=中键)
     * @return 是否处理了此事件
     */
    boolean onMouseClick(Vec3d rayStart, Vec3d rayDirection, int button);
    
    /**
     * 处理鼠标拖拽事件
     * @param rayStart 射线起点
     * @param rayDirection 射线方向
     * @param deltaMovement 移动增量
     * @return 是否处理了此事件
     */
    boolean onMouseDrag(Vec3d rayStart, Vec3d rayDirection, Vec3d deltaMovement);
    
    /**
     * 处理鼠标释放事件
     * @param rayStart 射线起点
     * @param rayDirection 射线方向
     * @param button 鼠标按键
     * @return 是否处理了此事件
     */
    boolean onMouseRelease(Vec3d rayStart, Vec3d rayDirection, int button);
    
    /**
     * 处理鼠标悬停事件
     * @param rayStart 射线起点
     * @param rayDirection 射线方向
     * @return 是否处理了此事件
     */
    boolean onMouseHover(Vec3d rayStart, Vec3d rayDirection);
    
    /**
     * 检查是否正在被拖拽
     */
    boolean isBeingDragged();
    
    /**
     * 设置拖拽状态
     */
    void setBeingDragged(boolean dragged);
    
    /**
     * 获取交互半径
     */
    float getInteractionRadius();
    
    /**
     * 获取交互中心点
     */
    Vec3d getInteractionCenter();
    
    /**
     * 检查是否可以交互
     */
    boolean isInteractable();
    
    /**
     * 设置交互状态
     */
    void setInteractable(boolean interactable);
} 