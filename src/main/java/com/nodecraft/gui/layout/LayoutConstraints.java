package com.nodecraft.gui.layout;

/**
 * 布局约束类
 * 用于定义组件在布局中的位置、大小和行为
 */
public class LayoutConstraints {

    /**
     * 区域类型枚举
     * 定义组件在布局中的区域
     */
    public enum RegionType {
        TOOLBAR,    // 工具栏区域
        NODE_PANEL, // 左侧节点面板区域
        CANVAS,     // 中央画布区域
        PROPERTY_PANEL, // 右侧属性面板区域
        STATUS_BAR, // 底部状态栏区域
        CUSTOM      // 自定义区域
    }
    
    /**
     * 尺寸策略枚举
     * 定义组件如何适应可用空间
     */
    public enum SizePolicy {
        FIXED,      // 固定大小
        PREFERRED,  // 首选大小
        MINIMUM,    // 最小大小
        MAXIMUM,    // 最大大小
        EXPANDING   // 扩展填充
    }
    
    // 区域类型
    private RegionType regionType;
    
    // 宽度和高度策略
    private SizePolicy widthPolicy;
    private SizePolicy heightPolicy;
    
    // 对于固定大小策略，存储具体尺寸
    private float width;
    private float height;
    
    // 最小和最大尺寸
    private float minWidth;
    private float minHeight;
    private float maxWidth;
    private float maxHeight;
    
    // 比例权重（用于EXPANDING策略）
    private float weightX;
    private float weightY;
    
    /**
     * 默认构造函数
     * 创建一个扩展填充的约束
     */
    public LayoutConstraints() {
        this(RegionType.CUSTOM, SizePolicy.EXPANDING, SizePolicy.EXPANDING);
    }
    
    /**
     * 创建特定区域类型的约束
     * 
     * @param regionType 区域类型
     */
    public LayoutConstraints(RegionType regionType) {
        this(regionType, SizePolicy.PREFERRED, SizePolicy.PREFERRED);
    }
    
    /**
     * 创建具有指定策略的约束
     * 
     * @param regionType 区域类型
     * @param widthPolicy 宽度策略
     * @param heightPolicy 高度策略
     */
    public LayoutConstraints(RegionType regionType, SizePolicy widthPolicy, SizePolicy heightPolicy) {
        this.regionType = regionType;
        this.widthPolicy = widthPolicy;
        this.heightPolicy = heightPolicy;
        
        // 默认值
        this.width = 0;
        this.height = 0;
        this.minWidth = 0;
        this.minHeight = 0;
        this.maxWidth = Float.MAX_VALUE;
        this.maxHeight = Float.MAX_VALUE;
        this.weightX = 1.0f;
        this.weightY = 1.0f;
    }
    
    /**
     * 创建固定大小的约束
     * 
     * @param regionType 区域类型
     * @param width 固定宽度
     * @param height 固定高度
     * @return 新的布局约束
     */
    public static LayoutConstraints createFixed(RegionType regionType, float width, float height) {
        LayoutConstraints constraints = new LayoutConstraints(regionType, SizePolicy.FIXED, SizePolicy.FIXED);
        constraints.setWidth(width);
        constraints.setHeight(height);
        return constraints;
    }
    
    /* // 移除 createToolbar 方法
    public static LayoutConstraints createToolbar(float height) {
        return createFixed(RegionType.TOOLBAR, 0, height);
    }
    */
    
    /**
     * 创建状态栏约束
     * 
     * @param height 状态栏高度
     * @return 新的布局约束
     */
    public static LayoutConstraints createStatusBar(float height) {
        return createFixed(RegionType.STATUS_BAR, 0, height);
    }
    
    /**
     * 创建节点面板约束
     * 
     * @param widthRatio 宽度比例
     * @param minWidth 最小宽度
     * @return 新的布局约束
     */
    public static LayoutConstraints createNodePanel(float widthRatio, float minWidth) {
        LayoutConstraints constraints = new LayoutConstraints(RegionType.NODE_PANEL, SizePolicy.EXPANDING, SizePolicy.EXPANDING);
        constraints.setWeightX(widthRatio);
        constraints.setMinWidth(minWidth);
        return constraints;
    }
    
    /**
     * 创建画布约束
     * 
     * @param widthRatio 宽度比例
     * @param minWidth 最小宽度
     * @return 新的布局约束
     */
    public static LayoutConstraints createCanvas(float widthRatio, float minWidth) {
        LayoutConstraints constraints = new LayoutConstraints(RegionType.CANVAS, SizePolicy.EXPANDING, SizePolicy.EXPANDING);
        constraints.setWeightX(widthRatio);
        constraints.setMinWidth(minWidth);
        return constraints;
    }
    
    /**
     * 创建属性面板约束
     * 
     * @param widthRatio 宽度比例
     * @param minWidth 最小宽度
     * @return 新的布局约束
     */
    public static LayoutConstraints createPropertyPanel(float widthRatio, float minWidth) {
        LayoutConstraints constraints = new LayoutConstraints(RegionType.PROPERTY_PANEL, SizePolicy.EXPANDING, SizePolicy.EXPANDING);
        constraints.setWeightX(widthRatio);
        constraints.setMinWidth(minWidth);
        return constraints;
    }
    
    // Getter 和 Setter 方法
    
    public RegionType getRegionType() {
        return regionType;
    }
    
    public void setRegionType(RegionType regionType) {
        this.regionType = regionType;
    }
    
    public SizePolicy getWidthPolicy() {
        return widthPolicy;
    }
    
    public void setWidthPolicy(SizePolicy widthPolicy) {
        this.widthPolicy = widthPolicy;
    }
    
    public SizePolicy getHeightPolicy() {
        return heightPolicy;
    }
    
    public void setHeightPolicy(SizePolicy heightPolicy) {
        this.heightPolicy = heightPolicy;
    }
    
    public float getWidth() {
        return width;
    }
    
    public void setWidth(float width) {
        this.width = width;
    }
    
    public float getHeight() {
        return height;
    }
    
    public void setHeight(float height) {
        this.height = height;
    }
    
    public float getMinWidth() {
        return minWidth;
    }
    
    public void setMinWidth(float minWidth) {
        this.minWidth = minWidth;
    }
    
    public float getMinHeight() {
        return minHeight;
    }
    
    public void setMinHeight(float minHeight) {
        this.minHeight = minHeight;
    }
    
    public float getMaxWidth() {
        return maxWidth;
    }
    
    public void setMaxWidth(float maxWidth) {
        this.maxWidth = maxWidth;
    }
    
    public float getMaxHeight() {
        return maxHeight;
    }
    
    public void setMaxHeight(float maxHeight) {
        this.maxHeight = maxHeight;
    }
    
    public float getWeightX() {
        return weightX;
    }
    
    public void setWeightX(float weightX) {
        this.weightX = weightX;
    }
    
    public float getWeightY() {
        return weightY;
    }
    
    public void setWeightY(float weightY) {
        this.weightY = weightY;
    }
} 