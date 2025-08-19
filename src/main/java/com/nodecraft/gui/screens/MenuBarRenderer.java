package com.nodecraft.gui.screens;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import com.nodecraft.core.NodeCraft;
import com.nodecraft.gui.components.CanvasComponent;
import com.nodecraft.gui.dialogs.ConfirmationDialog;
import com.nodecraft.gui.dialogs.FileDialogManager;
import com.nodecraft.gui.dialogs.MessageDialog;
import com.nodecraft.gui.editor.impl.ImGuiNodeEditor;
import com.nodecraft.gui.editor.impl.ImGuiNodeHistory;
import com.nodecraft.gui.editor.impl.ImGuiNodeIO;
import com.nodecraft.nodesystem.graph.NodeGraph;

import imgui.ImGui;
import imgui.ImVec2;

/**
 * 负责渲染 Nodecraft 编辑器的菜单栏。
 */
public class MenuBarRenderer {

    private final ComponentManager componentManager;
    private final Runnable closeAction;
    private final BooleanSupplier showMenuBarSupplier;

    // 文件过滤器
    private static final String NODE_GRAPH_FILTER = "节点图文件 (*.nodecraft;*.json)";
    private static final String DEFAULT_EXTENSION = ".nodecraft";
    
    // 最近文件路径
    private Path lastSavedPath = null;

    public MenuBarRenderer(
            ComponentManager componentManager,
            Runnable closeAction,
            BooleanSupplier showMenuBarSupplier,
            Consumer<Boolean> showMenuBarConsumer) {
        this.componentManager = componentManager;
        this.closeAction = closeAction;
        this.showMenuBarSupplier = showMenuBarSupplier;
    }

    /**
     * 渲染菜单栏。
     */
    public void render() {
        if (ImGui.beginMenuBar()) {
            if (ImGui.beginMenu("文件")) {
                if (ImGui.menuItem("新建节点图", "Ctrl+N")) {
                    createNewNodeGraph();
                }
                if (ImGui.menuItem("打开节点图...", "Ctrl+O")) {
                    openNodeGraph();
                }
                if (ImGui.menuItem("保存节点图", "Ctrl+S")) {
                    saveNodeGraph(false);
                }
                if (ImGui.menuItem("另存为...", null)) {
                    saveNodeGraph(true);
                }
                ImGui.separator();
                if (ImGui.menuItem("关闭编辑器", "Esc")) {
                    closeAction.run(); // 调用关闭回调
                }
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("编辑")) {
                // 获取编辑器实例
                CanvasComponent canvas = componentManager.getCanvasComponent();
                ImGuiNodeEditor editor = null;
                
                if (canvas != null && canvas.getNodeEditor() instanceof ImGuiNodeEditor) {
                    editor = (ImGuiNodeEditor) canvas.getNodeEditor();
                }
                
                // 撤销
                boolean canUndo = editor != null && editor.getHistory().canUndo();
                if (ImGui.menuItem("撤销", "Ctrl+Z", false, canUndo)) {
                    if (editor != null) {
                        editor.undo();
                    }
                }
                
                // 重做
                boolean canRedo = editor != null && editor.getHistory().canRedo();
                if (ImGui.menuItem("重做", "Ctrl+Y", false, canRedo)) {
                    if (editor != null) {
                        editor.redo();
                    }
                }
                
                ImGui.separator();
                
                // 判断是否有选中的节点
                boolean hasSelection = editor != null && !editor.getSelectedNodeIds().isEmpty();
                
                // 剪切
                if (ImGui.menuItem("剪切", "Ctrl+X", false, hasSelection)) {
                    if (editor != null) {
                        editor.cutSelectedNodes();
                    }
                }
                
                // 复制
                if (ImGui.menuItem("复制", "Ctrl+C", false, hasSelection)) {
                    if (editor != null) {
                        editor.copySelectedNodes();
                    }
                }
                
                // 粘贴
                if (ImGui.menuItem("粘贴", "Ctrl+V")) {
                    if (editor != null) {
                        // 获取画布组件(复用之前的canvas变量)
                        // 使用画布组件的新方法获取准确的画布中心世界坐标
                        ImVec2 centerWorldPos = canvas.getCanvasCenterWorldPosition();

                        NodeCraft.LOGGER.info("尝试在画布中心世界坐标 ({}, {}) 粘贴节点", centerWorldPos.x, centerWorldPos.y);

                        // 使用准确的世界坐标粘贴
                        editor.pasteNodesAtPosition(centerWorldPos.x, centerWorldPos.y);
                    }
                }
                
                // 删除
                if (ImGui.menuItem("删除", "Del", false, hasSelection)) {
                    if (editor != null) {
                        editor.deleteSelectedNodes();
                    }
                }
                
                ImGui.separator();
                
                // 分组功能暂未实现
                if (ImGui.menuItem("组选择", null, false, false)) {
                    NodeCraft.LOGGER.info("组选择功能尚未实现");
                }
                
                if (ImGui.menuItem("解组选择", null, false, false)) {
                    NodeCraft.LOGGER.info("解组选择功能尚未实现");
                }
                
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("视图")) {
                // 缩放选项
                if (ImGui.menuItem("放大", "Ctrl++")) {
                    CanvasComponent canvas = componentManager != null ? componentManager.getCanvasComponent() : null;
                    if (canvas != null) {
                        canvas.zoomIn();
                    }
                }
                if (ImGui.menuItem("缩小", "Ctrl+-")) {
                    CanvasComponent canvas = componentManager != null ? componentManager.getCanvasComponent() : null;
                    if (canvas != null) {
                        canvas.zoomOut();
                    }
                }
                if (ImGui.menuItem("重置视图", "Ctrl+0")) {
                    CanvasComponent canvas = componentManager != null ? componentManager.getCanvasComponent() : null;
                    if (canvas != null) {
                        canvas.resetCanvasView();
                    }
                }
                if (ImGui.menuItem("适应视图", "Ctrl+Home")) {
                    CanvasComponent canvas = componentManager != null ? componentManager.getCanvasComponent() : null;
                    if (canvas != null) {
                        canvas.fitToContent();
                    }
                }
                
                ImGui.separator();
                
                // 面板显示选项
                if (componentManager != null) {
                    NodecraftScreen screen = getNodecraftScreen();
                    if (screen != null) {
                        // 节点库面板显示选项
                        boolean nodeLibVisible = screen.isNodePanelVisible();
                        if (ImGui.menuItem("显示节点库面板", null, nodeLibVisible)) {
                            screen.toggleNodePanel();
                        }
                        
                        // 属性面板显示选项
                        boolean propertyPanelVisible = screen.isPropertyPanelVisible();
                        if (ImGui.menuItem("显示属性面板", null, propertyPanelVisible)) {
                            screen.togglePropertyPanel();
                        }
                        
                        ImGui.separator();
                    }
                }
                
                // 网格显示选项
                CanvasComponent canvas = componentManager != null ? componentManager.getCanvasComponent() : null;
                boolean showGrid = canvas != null && canvas.isShowGrid();
                if (ImGui.menuItem("显示网格", null, showGrid)) {
                    if (canvas != null) {
                        canvas.toggleGrid();
                    } else {
                        NodeCraft.LOGGER.warn("CanvasComponent is null, cannot toggle grid");
                    }
                }
                
                // 节点显示模式子菜单
                if (ImGui.beginMenu("节点显示模式")) {
                    if (canvas != null) {
                        CanvasComponent.NodeDisplayMode currentMode = canvas.getNodeDisplayMode();
                        
                        if (ImGui.menuItem("完整模式", null, currentMode == CanvasComponent.NodeDisplayMode.FULL)) {
                            canvas.setNodeDisplayMode(CanvasComponent.NodeDisplayMode.FULL);
                        }
                        if (ImGui.menuItem("紧凑模式", null, currentMode == CanvasComponent.NodeDisplayMode.COMPACT)) {
                            canvas.setNodeDisplayMode(CanvasComponent.NodeDisplayMode.COMPACT);
                        }
                        if (ImGui.menuItem("仅图标", null, currentMode == CanvasComponent.NodeDisplayMode.ICON_ONLY)) {
                            canvas.setNodeDisplayMode(CanvasComponent.NodeDisplayMode.ICON_ONLY);
                        }
                        if (ImGui.menuItem("仅文本", null, currentMode == CanvasComponent.NodeDisplayMode.TEXT_ONLY)) {
                            canvas.setNodeDisplayMode(CanvasComponent.NodeDisplayMode.TEXT_ONLY);
                        }
                    } else {
                        ImGui.menuItem("无法获取显示模式", null, false, false);
                    }
                    ImGui.endMenu();
                }
                
                // 节点预览选项
                boolean showPreviews = canvas != null && canvas.isShowNodePreviews();
                if (ImGui.menuItem("显示节点预览", null, showPreviews)) {
                    if (canvas != null) {
                        canvas.toggleNodePreviews();
                    } else {
                        NodeCraft.LOGGER.warn("CanvasComponent is null, cannot toggle node previews");
                    }
                }
                
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("帮助")) {
                if (ImGui.menuItem("关于 NodeCraft", null)) {
                    showAboutDialog();
                }
                if (ImGui.menuItem("节点参考", null)) {
                    openNodeReference();
                }
                
                ImGui.separator();
                
                // 调试选项
                if (ImGui.menuItem("调试历史记录", null)) {
                    debugHistoryFunction();
                }
                
                ImGui.endMenu();
            }
            ImGui.endMenuBar();
        }
    }
    
    /**
     * 显示关于NodeCraft的对话框
     */
    private void showAboutDialog() {
        String version = "1.0.0"; // 版本号
        String buildDate = "2023-05-01"; // 构建日期

        String content = "NodeCraft 版本: " + version + "\n" +
                "构建日期: " + buildDate + "\n\n" +
                "NodeCraft 是一个基于节点的可视化编程工具，\n" +
                "专为Minecraft模组开发设计。\n\n" +
                "使用 Java 和 ImGui 开发。\n" +
                "Copyright © 2023 NodeCraft 团队。保留所有权利。";
        
        new MessageDialog(
            "关于 NodeCraft",
                content
        ).show();
        
        NodeCraft.LOGGER.info("已显示关于对话框");
    }
    
    /**
     * 打开节点参考文档
     */
    private void openNodeReference() {
        try {
            // 检查文档是否存在
            java.io.File docsDir = new java.io.File("docs");
            java.io.File nodeRefFile = new java.io.File(docsDir, "node_reference.html");
            
            if (nodeRefFile.exists()) {
                // 如果文档存在，使用默认浏览器打开
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    desktop.browse(nodeRefFile.toURI());
                    NodeCraft.LOGGER.info("已打开节点参考文档: {}", nodeRefFile.getAbsolutePath());
                } else {
                    showDocumentationError("您的系统不支持自动打开浏览器。请手动打开文档：" + nodeRefFile.getAbsolutePath());
                }
            } else {
                // 如果文档不存在，尝试打开在线文档
                String onlineDocUrl = "https://nodecraft.example.com/docs/reference";
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    desktop.browse(java.net.URI.create(onlineDocUrl));
                    NodeCraft.LOGGER.info("已打开在线节点参考文档: {}", onlineDocUrl);
                } else {
                    showDocumentationError("找不到本地文档，且您的系统不支持自动打开浏览器。请访问: " + onlineDocUrl);
                }
            }
        } catch (Exception e) {
            NodeCraft.LOGGER.error("打开节点参考文档失败", e);
            showDocumentationError("打开文档失败: " + e.getMessage());
        }
    }
    
    /**
     * 显示文档错误对话框
     */
    private void showDocumentationError(String message) {
        new MessageDialog(
            "无法打开文档",
            message
        ).show();
    }
    
    /**
     * 创建新的节点图
     */
    private void createNewNodeGraph() {
        try {
            CanvasComponent canvas = componentManager.getCanvasComponent();
            if (canvas != null && canvas.getNodeEditor() instanceof ImGuiNodeEditor editor) {
                // 检查当前节点图是否有未保存的更改
                if (editor.hasUnsavedChanges()) {
                    // 显示确认对话框，询问是否保存当前节点图
                    showSaveConfirmationDialog(() -> {
                        // 用户确认后的回调，创建新节点图
                        createNewNodeGraphImpl(editor);
                    });
                } else {
                    // 直接创建新节点图
                    createNewNodeGraphImpl(editor);
                }
            }
        } catch (Exception e) {
            NodeCraft.LOGGER.error("创建新节点图失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 实际创建新节点图的实现
     */
    private void createNewNodeGraphImpl(ImGuiNodeEditor editor) {
        // 创建新的空白节点图
        NodeGraph newGraph = new NodeGraph("新建节点图");
        editor.setCurrentGraph(newGraph);
        editor.clearNodePositions();
        
        // 重置视图
        editor.setCanvasView(1.0f, 0.0f, 0.0f);
        
        // 清除最近保存路径
        lastSavedPath = null;
        
        NodeCraft.LOGGER.info("已创建新的节点图");
    }
    
    /**
     * 显示保存确认对话框
     * @param onConfirm 用户确认后的回调
     */
    private void showSaveConfirmationDialog(Runnable onConfirm) {
        // "否"按钮 - 不保存，直接创建新节点图
        new ConfirmationDialog(
            "保存确认",
            "当前节点图有未保存的更改，是否保存？",
            () -> {
                // "是"按钮 - 保存当前文件，然后创建新节点图
                // 保存成功后创建新节点图
                saveNodeGraph(false, onConfirm);
            },
                onConfirm,
            null  // "取消"按钮 - 不做任何操作
        ).show();
    }
    
    /**
     * 打开节点图文件
     */
    private void openNodeGraph() {
        try {
            CanvasComponent canvas = componentManager.getCanvasComponent();
            if (canvas != null && canvas.getNodeEditor() instanceof ImGuiNodeEditor editor) {
                // 检查当前节点图是否有未保存的更改
                if (editor.hasUnsavedChanges()) {
                    // 显示确认对话框，询问是否保存当前节点图
                    showSaveConfirmationDialog(() -> {
                        // 用户确认后的回调，打开新节点图
                        openNodeGraphImpl(editor);
                    });
                } else {
                    // 直接打开新节点图
                    openNodeGraphImpl(editor);
                }
            }
        } catch (Exception e) {
            NodeCraft.LOGGER.error("加载节点图失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 实际打开节点图的实现
     */
    private void openNodeGraphImpl(ImGuiNodeEditor editor) {
        // 使用文件对话框管理器的回调方式
        FileDialogManager.showFileDialog(
            "打开节点图",
            getDefaultDirectory(),
            NODE_GRAPH_FILTER,
            false, // 不是保存对话框
            null,  // 打开文件不需要默认扩展名
            selectedPath -> {
                if (selectedPath != null) {
                    // 获取IO组件并加载文件
                    ImGuiNodeIO io = editor.getNodeIO();
                    if (io != null) {
                        io.loadGraph(selectedPath);
                        lastSavedPath = selectedPath;
                        NodeCraft.LOGGER.info("成功加载节点图: {}", selectedPath);
                    } else {
                        NodeCraft.LOGGER.error("无法获取NodeIO组件");
                    }
                }
            }
        );
    }
    
    /**
     * 保存节点图
     * @param saveAs 是否另存为
     */
    private void saveNodeGraph(boolean saveAs) {
        saveNodeGraph(saveAs, null);
    }
    
    /**
     * 保存节点图
     * @param saveAs 是否另存为
     * @param onSaveComplete 保存完成后的回调
     */
    private void saveNodeGraph(boolean saveAs, Runnable onSaveComplete) {
        try {
            CanvasComponent canvas = componentManager.getCanvasComponent();
            if (canvas != null && canvas.getNodeEditor() instanceof ImGuiNodeEditor editor) {
                ImGuiNodeIO io = editor.getNodeIO();
                if (io == null) {
                    NodeCraft.LOGGER.error("无法获取NodeIO组件");
                    return;
                }
                
                // 确定保存路径
                Path savePath = lastSavedPath;
                
                // 如果是另存为或者还没有保存过，显示保存对话框
                if (saveAs || savePath == null) {
                    // 使用文件对话框管理器的回调方式
                    FileDialogManager.showFileDialog(
                        "保存节点图",
                        getDefaultDirectory(),
                        NODE_GRAPH_FILTER,
                        true, // 是保存对话框
                        DEFAULT_EXTENSION,
                        selectedPath -> {
                            if (selectedPath != null) {
                                // 更新最近保存路径
                                lastSavedPath = selectedPath;
                                
                                // 保存文件
                                io.saveGraph(selectedPath);
                                NodeCraft.LOGGER.info("成功保存节点图: {}", selectedPath);
                                
                                // 调用回调
                                if (onSaveComplete != null) {
                                    onSaveComplete.run();
                                }
                            }
                        }
                    );
                } else {
                    // 直接保存到已有路径
                    io.saveGraph(savePath);
                    NodeCraft.LOGGER.info("成功保存节点图: {}", savePath);
                    
                    // 调用回调
                    if (onSaveComplete != null) {
                        onSaveComplete.run();
                    }
                }
            }
        } catch (Exception e) {
            NodeCraft.LOGGER.error("保存节点图失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 获取默认目录
     */
    private Path getDefaultDirectory() {
        // 如果有最近保存的路径，使用其父目录
        if (lastSavedPath != null) {
            Path parent = lastSavedPath.getParent();
            if (parent != null) {
                return parent;
            }
        }
        
        // 否则使用当前工作目录
        return Paths.get("").toAbsolutePath();
    }

    /**
     * 获取NodecraftScreen实例，如果可能的话
     * @return NodecraftScreen实例，如果无法获取则返回null
     */
    private NodecraftScreen getNodecraftScreen() {
        if (componentManager == null) {
            return null;
        }
        
        // 通过组件管理器获取所属的NodecraftScreen
        // 此方法假设ComponentManager包含了获取屏幕的方法，或者可以通过其他方式获取
        if (componentManager.getParentScreen() instanceof NodecraftScreen) {
            return (NodecraftScreen) componentManager.getParentScreen();
        }
        
        return null;
    }

    /**
     * 调试历史记录功能
     */
    private void debugHistoryFunction() {
        try {
            CanvasComponent canvas = componentManager.getCanvasComponent();
            if (canvas != null && canvas.getNodeEditor() instanceof ImGuiNodeEditor editor) {
                ImGuiNodeHistory history = editor.getHistory();
                
                if (history != null) {
                    String testResult = history.testHistoryFunction();
                    NodeCraft.LOGGER.info("历史记录调试信息:\n{}", testResult);
                    
                    // 也在控制台输出
                    System.out.println("=== 历史记录调试信息 ===");
                    System.out.println(testResult);
                    System.out.println("========================");
                } else {
                    NodeCraft.LOGGER.error("历史记录组件为空");
                }
            } else {
                NodeCraft.LOGGER.error("无法获取编辑器实例");
            }
        } catch (Exception e) {
            NodeCraft.LOGGER.error("调试历史记录功能时出错: {}", e.getMessage(), e);
        }
    }
} 