package com.nodecraft.gui.layout;

import java.util.Map;
import com.nodecraft.gui.components.EditorComponent;
import com.nodecraft.gui.layout.LayoutConfig;

/**
 * 布局管理器接口
 * 负责计算和管理编辑器组件的布局
 */
public interface LayoutManager {
    
    /**
     * 计算所有注册组件的布局。
     * @param availableWidth 可用内容区宽度
     * @param availableHeight 可用内容区高度
     * @param startX 内容区起始 X 坐标
     * @param startY 内容区起始 Y 坐标
     * @param config 布局配置 (比例、最小尺寸等)
     * @param showMenuBar 菜单栏是否显示 (影响起始 Y)
     */
    void calculateLayout(float availableWidth, float availableHeight, float startX, float startY, LayoutConfig config, boolean showMenuBar);

    /**
     * 获取计算后的组件布局信息。
     * @param componentId 需要获取布局的组件 ID
     * @return 计算出的布局维度，如果未计算或组件未注册则返回 null。
     */
    LayoutDimensions getComputedLayout(String componentId);

    /**
     * 获取计算后的组件布局信息。
     * @param component 需要获取布局的组件
     * @return 计算出的布局维度，如果未计算或组件未注册则返回 null。
     */
    LayoutDimensions getComputedLayout(EditorComponent component);
    
    /**
     * 注册组件到布局管理器
     * 
     * @param component 要注册的组件
     * @param layoutConstraints 布局约束
     */
    void registerComponent(EditorComponent component, LayoutConstraints layoutConstraints);
    
    /**
     * 移除已注册的组件
     * 
     * @param componentId 要移除的组件ID
     * @return 是否成功移除组件
     */
    boolean removeComponent(String componentId);
    
    /**
     * 获取组件的布局约束
     * 
     * @param componentId 组件ID
     * @return 组件的布局约束，如果组件不存在则返回null
     */
    LayoutConstraints getComponentConstraints(String componentId);
    
    /**
     * 设置组件的布局约束
     * 
     * @param componentId 组件ID
     * @param constraints 新的布局约束
     * @return 是否成功设置
     */
    boolean setComponentConstraints(String componentId, LayoutConstraints constraints);

    /**
     * 获取所有已注册的组件及其ID。
     * @return 组件ID到组件实例的映射。
     */
    Map<String, EditorComponent> getRegisteredComponents();
} 