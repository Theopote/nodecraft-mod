package com.nodecraft.gui.screens;

import com.nodecraft.core.NodeCraft;
import com.nodecraft.gui.editor.integration.ImGuiRenderer;
import com.nodecraft.gui.style.ImGuiStyleScope;
import com.nodecraft.gui.style.MinecraftTheme;
import com.nodecraft.gui.utils.ImGuiStyleVar;
import com.nodecraft.gui.window.ViewportCloseDetector;
import com.nodecraft.gui.window.WindowManager;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;

/**
 * NodeCraft编辑器窗口渲染器
 * 负责管理ImGui窗口的渲染和样式
 */
public class NodecraftWindowRenderer {
    
    private final NodecraftScreen parentScreen;
    private final ImGuiRenderer imGuiRenderer;
    private final WindowManager windowManager;
    private final ViewportCloseDetector closeDetector;
    private final MinecraftTheme theme;
    private ImGuiStyleScope styleScope;
    
    public NodecraftWindowRenderer(NodecraftScreen parentScreen) {
        this.parentScreen = parentScreen;
        this.imGuiRenderer = ImGuiRenderer.getInstance();
        this.windowManager = WindowManager.getInstance();
        this.closeDetector = ViewportCloseDetector.getInstance();
        this.theme = new MinecraftTheme();
        this.styleScope = new ImGuiStyleScope();
    }
    
    /**
     * 渲染完整的窗口框架
     */
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        try {
            // 开始ImGui帧
            imGuiRenderer.beginFrame();
            
            try {
                // 渲染主窗口内容
                renderWithStyles(context, mouseX, mouseY, delta);
            } finally {
                // 确保结束ImGui帧
                imGuiRenderer.endFrame();
            }
        } catch (Exception e) {
            NodeCraft.LOGGER.error("渲染错误: {}", e.getMessage(), e);
            parentScreen.closeRequested = true;
        }
    }
    
    /**
     * 应用样式并渲染内容
     */
    private void renderWithStyles(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean stylesApplied = false;
        try {
            applyStyles();
            stylesApplied = true;
            
            if (parentScreen.isEditorDetached()) {
                renderDetachedWindow();
            } else {
                renderMainWindow(context, mouseX, mouseY, delta);

                if (!parentScreen.closeRequested) {
                    renderDialogs();
                }
            }
        } catch (Exception e) {
            NodeCraft.LOGGER.error("渲染ImGui界面错误: {}", e.getMessage(), e);
        } finally {
            if (stylesApplied) {
                popStyles();
            }
        }
    }
    
    /**
     * 渲染主编辑器窗口
     */
    private void renderMainWindow(DrawContext context, int mouseX, int mouseY, float delta) {
        // 检查是否启用了 ViewportsEnable
        boolean viewportsEnabled = ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable);

        // 防止窗口被历史配置移动到屏幕外
        MinecraftClient client = MinecraftClient.getInstance();
        float screenWidth = client.getWindow().getWidth();
        float screenHeight = client.getWindow().getHeight();

        float minVisibleWidth = 320.0f;
        float minVisibleHeight = 180.0f;

        float maxX = Math.max(0.0f, screenWidth - minVisibleWidth);
        float maxY = Math.max(0.0f, screenHeight - minVisibleHeight);

        parentScreen.windowX = Math.max(0.0f, Math.min(parentScreen.windowX, maxX));
        parentScreen.windowY = Math.max(0.0f, Math.min(parentScreen.windowY, maxY));

        float minWidth = EditorConstants.MIN_WINDOW_WIDTH;
        float minHeight = EditorConstants.MIN_WINDOW_HEIGHT;
        parentScreen.windowWidth = Math.max(minWidth, Math.min(parentScreen.windowWidth, screenWidth));
        parentScreen.windowHeight = Math.max(minHeight, Math.min(parentScreen.windowHeight, screenHeight));
        
        // 仅在窗口出现时设置初始位置和大小，避免每帧强制覆盖导致无法自由拖动
        ImGui.setNextWindowPos(parentScreen.windowX, parentScreen.windowY, ImGuiCond.Appearing);
        ImGui.setNextWindowSize(parentScreen.windowWidth, parentScreen.windowHeight, ImGuiCond.Appearing);
        ImGui.setNextWindowCollapsed(false, ImGuiCond.Appearing);
        
        // 创建窗口标志。分割线交互期间锁定窗口移动，避免拖拽分割线时整体面板漂移。
        LayoutRenderer layoutRenderer = parentScreen.getLayoutRenderer();
        boolean lockWindowMoveForSplitter = layoutRenderer != null &&
            (layoutRenderer.isDraggingSplitter() || layoutRenderer.isHoveringSplitter());
        int windowFlags = createWindowFlags(viewportsEnabled, lockWindowMoveForSplitter);
        
        // 自定义窗口标题
        String windowTitle = viewportsEnabled ?
                "NodeCraft 编辑器 - 独立窗口模式" : "NodeCraft 编辑器";
        
        boolean windowOpened;
        try {
            if (viewportsEnabled) {
                windowOpened = ImGui.begin(windowTitle, closeDetector.getWindowOpenFlag(), windowFlags);
            } else {
                windowOpened = ImGui.begin(windowTitle, windowFlags | ImGuiWindowFlags.NoSavedSettings);
            }
            
            // 检查窗口关闭请求
            if (viewportsEnabled && !closeDetector.getWindowOpenFlag().get()) {
                parentScreen.closeRequested = true;
                return;
            }
            
            if (windowOpened) {
                handleWindowAssociation();
                updateWindowDimensions();
                renderWindowContent(context, mouseX, mouseY, delta);
            }
        } finally {
            ImGui.end();
        }
    }
    
    private int createWindowFlags(boolean viewportsEnabled, boolean lockWindowMove) {
        int windowFlags = ImGuiWindowFlags.MenuBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoTitleBar;

        if (lockWindowMove) {
            windowFlags |= ImGuiWindowFlags.NoMove;
        }
        
        if (viewportsEnabled) {
            windowFlags |= ImGuiWindowFlags.NoDocking;
        }
        
        return windowFlags;
    }
    
    private void handleWindowAssociation() {
        if (!windowManager.isWindowAssociated()) {
            windowManager.associateNodeCraftWindow();
        }
    }
    
    private void updateWindowDimensions() {
        parentScreen.windowWidth = ImGui.getWindowWidth();
        parentScreen.windowHeight = ImGui.getWindowHeight();
        parentScreen.windowX = ImGui.getWindowPosX();
        parentScreen.windowY = ImGui.getWindowPosY();
    }
    
    private void renderWindowContent(DrawContext context, int mouseX, int mouseY, float delta) {
        // 渲染菜单栏
        MenuBarRenderer menuBarRenderer = parentScreen.getMenuBarRenderer();
        LayoutRenderer layoutRenderer = parentScreen.getLayoutRenderer();
        
        // 始终渲染菜单栏，避免空白菜单栏闪烁（窗口标志包含 MenuBar，必须调用 beginMenuBar/endMenuBar）
        if (parentScreen.showMenuBar) {
            if (menuBarRenderer != null) {
                try {
                    menuBarRenderer.render();
                } catch (Exception e) {
                    NodeCraft.LOGGER.error("渲染菜单栏时出错: {}", e.getMessage(), e);
                }
            }
        }
        
        // 渲染主布局
        if (layoutRenderer != null) {
            try {
                layoutRenderer.render(context, mouseX, mouseY, delta);
            } catch (Exception e) {
                NodeCraft.LOGGER.error("渲染布局时出错: {}", e.getMessage(), e);
            }
        }
    }

    public void renderDetachedWindow() {
        boolean stylesApplied = false;
        try {
            applyStyles();
            stylesApplied = true;

            ImGui.setNextWindowPos(0.0f, 0.0f, ImGuiCond.Always);
            ImGui.setNextWindowSize(ImGui.getIO().getDisplaySizeX(), ImGui.getIO().getDisplaySizeY(), ImGuiCond.Always);

            final int windowFlags = ImGuiWindowFlags.MenuBar
                | ImGuiWindowFlags.NoTitleBar
                | ImGuiWindowFlags.NoResize
                | ImGuiWindowFlags.NoMove
                | ImGuiWindowFlags.NoCollapse
                | ImGuiWindowFlags.NoSavedSettings;

            if (ImGui.begin("NodeCraft Detached Editor", windowFlags)) {
                renderWindowContent(null, 0, 0, 0.0f);
            }
            ImGui.end();

            if (!parentScreen.closeRequested) {
                renderDialogs();
            }
        } catch (Exception e) {
            NodeCraft.LOGGER.error("娓叉煋鐙珛缂栬緫鍣ㄧ獥鍙ｆ椂鍑洪敊: {}", e.getMessage(), e);
            parentScreen.closeRequested = true;
        } finally {
            if (stylesApplied) {
                popStyles();
            }
        }
    }
    
    private void renderDialogs() {
        // 渲染各种对话框
        try {
            com.nodecraft.gui.dialogs.FileDialogManager.renderFileDialogs();
            com.nodecraft.gui.dialogs.ConfirmationDialog.renderDialog();
            com.nodecraft.gui.dialogs.MessageDialog.renderDialog();
        } catch (Exception e) {
            NodeCraft.LOGGER.error("渲染对话框时出错: {}", e.getMessage(), e);
        }
    }
    
    private void applyStyles() {
        theme.apply(styleScope);
        styleScope.pushStyleVar(ImGuiStyleVar.WindowPadding, 
            EditorConstants.DEFAULT_WINDOW_PADDING, EditorConstants.DEFAULT_WINDOW_PADDING);
        styleScope.pushStyleVar(ImGuiStyleVar.ItemSpacing, 
            EditorConstants.DEFAULT_ITEM_SPACING, EditorConstants.DEFAULT_ITEM_SPACING);
    }
    
    private void popStyles() {
        if (styleScope != null) {
            try {
                styleScope.popAll();
            } catch (Exception e) {
                NodeCraft.LOGGER.error("弹出样式时出错: {}", e.getMessage());
            }
        }
    }
    
    public void cleanup() {
        if (styleScope != null) {
            try {
                styleScope.close();
            } catch (Exception e) {
                NodeCraft.LOGGER.error("清理样式作用域时出错: {}", e.getMessage());
            } finally {
                styleScope = null;
            }
        }
    }
} 
