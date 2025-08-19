package com.nodecraft.gui.editor.base;

import net.minecraft.client.gui.DrawContext;

/**
 * 节点编辑器的通用接口，定义了编辑器的生命周期、渲染和输入处理方法。
 */
public interface INodeEditor {

    /**
     * 获取此编辑器实现的唯一标识符。
     * @return 标识符字符串 (例如 "imgui", "native")
     */
    String getIdentifier();

    /**
     * 获取编辑器的优先级。
     * 数值越小，优先级越高。
     * 用于在多个可用编辑器中选择默认或最佳编辑器。
     * @return 优先级数值
     */
    int getPriority();

    /**
     * 检查此编辑器是否在当前平台上受支持。
     * 例如，ImGui 编辑器需要检查 ImGui 库是否存在。
     * @return 如果支持则返回 true，否则返回 false。
     */
    boolean isPlatformSupported();

    /**
     * 初始化编辑器。
     */
    void init();

    /**
     * 打开编辑器，使其可见或激活。
     */
    void open();

    /**
     * 关闭编辑器。
     */
    void close();

    /**
     * 检查编辑器当前是否处于打开状态。
     * @return 如果编辑器已打开，则返回 true；否则返回 false。
     */
    boolean isOpen();

    /**
     * 渲染编辑器内容 - 传统方式。
     * 对于ImGui实现，此方法可能为空，因为渲染由回调驱动。
     * 对于原生实现，此方法将绘制编辑器界面。
     *
     * @param context 绘制上下文
     * @param mouseX  当前鼠标X坐标
     * @param mouseY  当前鼠标Y坐标
     * @param delta   帧时间差
     */
    void render(DrawContext context, int mouseX, int mouseY, float delta);
    
    /**
     * 使用ImGui渲染编辑器内容。
     * 此方法仅适用于ImGui实现，应在ImGui的渲染循环中调用。
     * 非ImGui实现可以提供空实现或抛出UnsupportedOperationException。
     * 
     * 注意：调用此方法时，已经设置了ImGui上下文，并且已经有一个ImGui窗口被创建。
     * 此方法应该只处理具体的编辑器内容渲染，而不是创建新窗口。
     */
    default void renderImGui() {
        // 默认实现为空或抛出异常，由具体实现类重写
        throw new UnsupportedOperationException("此编辑器不支持ImGui渲染");
    }

    // 移除输入处理方法声明，将通过事件总线和 @Subscribe 处理
    /*
    boolean mouseClicked(double mouseX, double mouseY, int button);
    boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY);
    boolean mouseReleased(double mouseX, double mouseY, int button);
    boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount);
    boolean keyPressed(int keyCode, int scanCode, int modifiers);
    boolean keyReleased(int keyCode, int scanCode, int modifiers);
    boolean charTyped(char chr, int modifiers);
    */

    // 可以根据需要添加更多方法，例如 onResize, update 等
} 