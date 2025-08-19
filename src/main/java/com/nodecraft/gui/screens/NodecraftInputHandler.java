package com.nodecraft.gui.screens;

import com.nodecraft.core.NodeCraft;
import com.nodecraft.gui.components.CanvasComponent;
import com.nodecraft.gui.editor.impl.ImGuiNodeEditor;
import com.nodecraft.minecraft.client.GhostCameraManager;
import imgui.ImGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

/**
 * NodeCraft编辑器的输入事件处理器
 * 负责处理所有的键盘和鼠标输入事件
 */
public class NodecraftInputHandler {
    
    private final NodecraftScreen parentScreen;
    private final GhostCameraManager ghostCameraManager;
    private boolean isMiddleMousePressed = false;
    
    public NodecraftInputHandler(NodecraftScreen parentScreen, GhostCameraManager ghostCameraManager) {
        this.parentScreen = parentScreen;
        this.ghostCameraManager = ghostCameraManager;
    }
    
    /**
     * 处理鼠标点击事件
     */
    public boolean handleMouseClicked(double mouseX, double mouseY, int button) {
        // 处理中键按下事件
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            isMiddleMousePressed = true;
            if (NodeCraft.LOGGER.isDebugEnabled()) {
                NodeCraft.LOGGER.debug("中键按下 (NodecraftScreen) - 鼠标位置: ({}, {}), 窗口位置: ({}, {}), 窗口大小: ({}x{})", 
                    mouseX, mouseY, parentScreen.windowX, parentScreen.windowY, parentScreen.windowWidth, parentScreen.windowHeight);
            }
            
            boolean isMouseOverGui = parentScreen.isMouseOverNodecraftGui(mouseX, mouseY);
            if (NodeCraft.LOGGER.isDebugEnabled()) {
                NodeCraft.LOGGER.debug("鼠标在GUI上: {}", isMouseOverGui);
            }
            
            // 如果鼠标在窗口外，允许视角控制，不拦截事件
            if (!isMouseOverGui) {
                if (NodeCraft.LOGGER.isDebugEnabled()) {
                    NodeCraft.LOGGER.debug("中键按下且鼠标在窗口外，允许视角控制，返回 false");
                }
                return false; // 不拦截，让 Minecraft 处理
            } else {
                if (NodeCraft.LOGGER.isDebugEnabled()) {
                    NodeCraft.LOGGER.debug("中键按下但鼠标在窗口内，拦截事件");
                }
            }
        }

        // 检查 ImGui 是否捕获了鼠标
        if (parentScreen.isImGuiWantCaptureMouse()) {
            return true;
        }

        // 如果 ImGui 没有捕获，但鼠标在 ImGui 窗口范围内，我们仍然返回 true，防止事件穿透到 Minecraft 游戏世界
        if (parentScreen.isMouseOverNodecraftGui(mouseX, mouseY)) {
            return true;
        }

        // 幽灵相机模式下的方块选择处理
        return handleGhostCameraBlockSelection(button);
    }
    
    /**
     * 处理鼠标释放事件
     */
    public boolean handleMouseReleased(double mouseX, double mouseY, int button) {
        // 处理中键释放事件
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            isMiddleMousePressed = false;
            if (NodeCraft.LOGGER.isDebugEnabled()) {
                NodeCraft.LOGGER.debug("中键释放 (NodecraftScreen) - 鼠标位置: ({}, {})", mouseX, mouseY);
            }
            
            boolean isMouseOverGui = parentScreen.isMouseOverNodecraftGui(mouseX, mouseY);
            if (NodeCraft.LOGGER.isDebugEnabled()) {
                NodeCraft.LOGGER.debug("鼠标在GUI上: {}", isMouseOverGui);
            }
            
            // 如果鼠标在窗口外，不拦截事件，让 Minecraft 处理
            if (!isMouseOverGui) {
                if (NodeCraft.LOGGER.isDebugEnabled()) {
                    NodeCraft.LOGGER.debug("中键释放且鼠标在窗口外，不拦截事件，返回 false");
                }
                return false; // 不拦截，让 Minecraft 处理
            } else {
                if (NodeCraft.LOGGER.isDebugEnabled()) {
                    NodeCraft.LOGGER.debug("中键释放但鼠标在窗口内，拦截事件");
                }
            }
        }

        // 只要 ImGui 想要捕获鼠标，就阻止事件向下传递
        if (parentScreen.isImGuiWantCaptureMouse()) {
            return true;
        }

        // 如果鼠标在窗口范围内，但 ImGui 没有捕获，也视为已处理（防止事件穿透）
        return parentScreen.isMouseOverNodecraftGui(mouseX, mouseY);
    }
    
    /**
     * 处理鼠标滚轮事件
     */
    public boolean handleMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // 只要 ImGui 想要捕获鼠标，就阻止事件向下传递
        boolean wantCapture = ImGui.getIO() != null && ImGui.getIO().getWantCaptureMouse();
        if (wantCapture) {
            return true;
        }

        // 如果当前编辑器是 NativeNodeEditor，则转发滚轮事件
        if (parentScreen.getCurrentEditor() != null && parentScreen.getCurrentEditor() instanceof com.nodecraft.gui.editor.impl.NativeNodeEditor nativeEditor) {
            return nativeEditor.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        return false;
    }
    
    /**
     * 处理鼠标拖拽事件
     */
    public boolean handleMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        // 获取缓存的鼠标状态
        boolean isMouseOverGui = parentScreen.isMouseOverNodecraftGui(mouseX, mouseY);
        
        // 如果应该允许视角控制，不拦截事件
        if (shouldAllowCameraControl(isMouseOverGui)) {
            if (NodeCraft.LOGGER.isDebugEnabled()) {
                NodeCraft.LOGGER.debug("允许视角控制，不拦截 mouseDragged 事件");
            }
            return false; // 不拦截，让 Minecraft 处理视角移动
        }

        // 只要 ImGui 想要捕获鼠标，就阻止事件向下传递
        if (parentScreen.isImGuiWantCaptureMouse()) {
            return true;
        }

        // 如果 ImGui 没有捕获，但鼠标在 ImGui 窗口范围内，仍然返回 true，防止事件穿透到 Minecraft 游戏世界。
        return isMouseOverGui;
    }
    
    /**
     * 处理鼠标移动事件
     */
    public boolean handleMouseMoved(double mouseX, double mouseY) {
        // 获取缓存的鼠标状态
        boolean isMouseOverGui = parentScreen.isMouseOverNodecraftGui(mouseX, mouseY);
        
        // 如果应该允许视角控制，不拦截鼠标移动事件
        if (shouldAllowCameraControl(isMouseOverGui)) {
            if (NodeCraft.LOGGER.isDebugEnabled()) {
                NodeCraft.LOGGER.debug("允许视角控制，不拦截 mouseMoved 事件");
            }
            // 不调用 super，让 Minecraft 的鼠标处理器直接处理
            return false;
        }

        return false;
    }
    
    /**
     * 处理键盘按键事件
     */
    public boolean handleKeyPressed(int keyCode, int scanCode, int modifiers) {
        // 检查是否是移动相关的键
        boolean isMovementKey = isMovementKey(keyCode);
        
        // 添加调试信息
        boolean isCtrlPressed = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        if (NodeCraft.LOGGER.isDebugEnabled()) {
            NodeCraft.LOGGER.debug("按键事件 - 键码: {}, 修饰符: {}, Ctrl: {}, 移动键: {}", keyCode, modifiers, isCtrlPressed, isMovementKey);
        }

        // 如果是移动键，直接传递给 Minecraft，不拦截
        if (isMovementKey) {
            if (NodeCraft.LOGGER.isDebugEnabled()) {
                NodeCraft.LOGGER.debug("移动键 {} 直接传递给 Minecraft", keyCode);
            }
            return false;
        }

        // 处理编辑器快捷键
        return handleEditorShortcuts(keyCode, modifiers);
    }
    
    /**
     * 处理键盘释放事件
     */
    public boolean handleKeyReleased(int keyCode, int scanCode, int modifiers) {
        // 检查是否是移动相关的键
        boolean isMovementKey = isMovementKey(keyCode);
        
        // 如果是移动键，直接传递给 Minecraft，不拦截
        if (isMovementKey) {
            if (NodeCraft.LOGGER.isDebugEnabled()) {
                NodeCraft.LOGGER.debug("移动键释放 {} 直接传递给 Minecraft", keyCode);
            }
            return false;
        }
        
        // 只要 ImGui 想要捕获键盘事件，就阻止事件向下传递
        return ImGui.getIO() != null && ImGui.getIO().getWantCaptureKeyboard();
    }
    
    /**
     * 处理字符输入事件
     */
    public boolean handleCharTyped(char codePoint, int modifiers) {
        // 只要 ImGui 想要捕获键盘事件，就阻止事件向下传递
        return ImGui.getIO() != null && ImGui.getIO().getWantCaptureKeyboard();
    }
    
    /**
     * 检查是否应该允许视角控制
     * @param isMouseOverGui 鼠标是否在GUI上方（使用缓存的状态）
     * @return 是否应该允许视角控制
     */
    public boolean shouldAllowCameraControl(boolean isMouseOverGui) {
        return isMiddleMousePressed && !isMouseOverGui;
    }
    
    /**
     * 获取中键按下状态
     * @return 中键是否被按下
     */
    public boolean isMiddleMousePressed() {
        return isMiddleMousePressed;
    }
    
    // 私有辅助方法
    
    /**
     * 检查是否是移动相关的键
     */
    private boolean isMovementKey(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_W ||     // 前进
               keyCode == GLFW.GLFW_KEY_S ||     // 后退
               keyCode == GLFW.GLFW_KEY_A ||     // 左移
               keyCode == GLFW.GLFW_KEY_D ||     // 右移
               keyCode == GLFW.GLFW_KEY_SPACE || // 跳跃
               keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || // 蹲下
               keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT || // 蹲下
               keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || // 疾跑
               keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL; // 疾跑
    }
    
    /**
     * 处理编辑器快捷键
     */
    private boolean handleEditorShortcuts(int keyCode, int modifiers) {
        boolean isCtrlPressed = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        ComponentManager componentManager = parentScreen.getComponentManager();
        
        if (componentManager == null) return false;
        
        CanvasComponent canvas = componentManager.getCanvasComponent();
        if (canvas == null || !(canvas.getNodeEditor() instanceof ImGuiNodeEditor editor)) {
            return false;
        }

        if (NodeCraft.LOGGER.isDebugEnabled()) {
            NodeCraft.LOGGER.debug("编辑器状态 - componentManager: {}, canvas: {}, editor: {}",
                    true, true, true);
        }
        
        // 优先处理全局快捷键（撤销/重做/删除），即使ImGui想要捕获键盘也要处理
        if (handleGlobalShortcuts(editor, keyCode, isCtrlPressed)) {
            return true;
        }
        
        // 检查ImGui是否想要捕获键盘事件
        boolean wantCapture = ImGui.getIO() != null && ImGui.getIO().getWantCaptureKeyboard();
        if (wantCapture) {
            // ImGui想要捕获键盘，但我们已经处理了全局快捷键，现在让ImGui处理其他键盘事件
            if (NodeCraft.LOGGER.isDebugEnabled()) {
                NodeCraft.LOGGER.debug("ImGui想要捕获键盘，跳过其他快捷键处理");
            }
            return true;
        }
        
        // 处理其他快捷键（只有在ImGui不捕获键盘时才处理）
        return handleOtherShortcuts(editor, canvas, keyCode, isCtrlPressed);
    }
    
    /**
     * 处理全局快捷键（撤销、重做、删除）
     */
    private boolean handleGlobalShortcuts(ImGuiNodeEditor editor, int keyCode, boolean isCtrlPressed) {
        // 撤销：Ctrl+Z
        if (isCtrlPressed && keyCode == GLFW.GLFW_KEY_Z) {
            NodeCraft.LOGGER.info("触发撤销快捷键: Ctrl+Z");
            boolean canUndo = editor.getHistory().canUndo();
            if (canUndo) {
                editor.undo();
                return true;
            } else {
                if (NodeCraft.LOGGER.isDebugEnabled()) {
                    NodeCraft.LOGGER.debug("无法撤销：历史记录为空");
                }
            }
            return true; // 即使无法撤销也消费这个事件
        }

        // 重做：Ctrl+Y
        if (isCtrlPressed && keyCode == GLFW.GLFW_KEY_Y) {
            NodeCraft.LOGGER.info("触发重做快捷键: Ctrl+Y");
            boolean canRedo = editor.getHistory().canRedo();
            if (canRedo) {
                editor.redo();
                return true;
            } else {
                if (NodeCraft.LOGGER.isDebugEnabled()) {
                    NodeCraft.LOGGER.debug("无法重做：重做栈为空");
                }
            }
            return true; // 即使无法重做也消费这个事件
        }
        
        // 删除：Delete - 优先处理，避免被ImGui捕获
        if (keyCode == GLFW.GLFW_KEY_DELETE) {
            boolean hasSelection = !editor.getSelectedNodeIds().isEmpty();
            if (hasSelection) {
                NodeCraft.LOGGER.info("触发删除快捷键: Delete");
                editor.deleteSelectedNodes();
                return true;
            } else {
                if (NodeCraft.LOGGER.isDebugEnabled()) {
                    NodeCraft.LOGGER.debug("无法删除：没有选中的节点");
                }
                return true; // 消费Delete键事件，避免被其他组件处理
            }
        }
        
        return false;
    }
    
    /**
     * 处理其他快捷键（剪切、复制、粘贴等）
     */
    private boolean handleOtherShortcuts(ImGuiNodeEditor editor, CanvasComponent canvas, int keyCode, boolean isCtrlPressed) {
        // 判断是否有选中的节点
        boolean hasSelection = !editor.getSelectedNodeIds().isEmpty();

        if (hasSelection) {
            // 剪切：Ctrl+X
            if (isCtrlPressed && keyCode == GLFW.GLFW_KEY_X) {
                NodeCraft.LOGGER.info("触发剪切快捷键: Ctrl+X");
                editor.cutSelectedNodes();
                return true;
            }

            // 复制：Ctrl+C
            if (isCtrlPressed && keyCode == GLFW.GLFW_KEY_C) {
                NodeCraft.LOGGER.info("触发复制快捷键: Ctrl+C");
                editor.copySelectedNodes();
                return true;
            }

            // 复制节点：Ctrl+D
            if (isCtrlPressed && keyCode == GLFW.GLFW_KEY_D) {
                NodeCraft.LOGGER.info("触发复制节点快捷键: Ctrl+D");
                // 只复制第一个选中的节点
                UUID nodeId = editor.getSelectedNodeIds().iterator().next();
                if (editor instanceof ImGuiNodeEditor) {
                    // 调用菜单系统的复制节点方法
                    editor.duplicateSelectedNode();
                }
                return true;
            }
        }

        // 粘贴：Ctrl+V
        if (isCtrlPressed && keyCode == GLFW.GLFW_KEY_V) {
            NodeCraft.LOGGER.info("触发粘贴快捷键: Ctrl+V");

            // 如果有画布组件，获取更精确的粘贴位置
            {
                // 获取鼠标在画布中的位置，或者使用画布中心
                double mouseX = MinecraftClient.getInstance().mouse.getX();
                double mouseY = MinecraftClient.getInstance().mouse.getY();

                // 将画布坐标转换为世界坐标
                float canvasX = (float)(mouseX - ImGui.getWindowPosX() - canvas.getCanvasOffsetX()) / canvas.getCanvasZoom();
                float canvasY = (float)(mouseY - ImGui.getWindowPosY() - canvas.getCanvasOffsetY()) / canvas.getCanvasZoom();

                editor.pasteNodesAt(canvasX, canvasY);
            }
            return true;
        }

        // 添加F1快捷键切换幽灵相机模式
        if (keyCode == GLFW.GLFW_KEY_F1) {
            ghostCameraManager.toggle();
            NodeCraft.LOGGER.info("幽灵相机模式已切换: {}", ghostCameraManager.isEnabled() ? "开启" : "关闭");
            return true;
        }
        
        return false;
    }
    
    /**
     * 处理幽灵相机模式下的方块选择
     */
    private boolean handleGhostCameraBlockSelection(int button) {
        if (!ghostCameraManager.isEnabled()) return false;
        
        // 获取当前指向的方块
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.crosshairTarget == null || 
            client.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        
        BlockHitResult blockHit = (BlockHitResult)client.crosshairTarget;
        
        // 例如：发送选中方块的位置到编辑器
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) { // 左键点击
            if (parentScreen.getCurrentEditor() != null) {
                // 向编辑器发送选中方块事件
                // 示例：currentEditor.onBlockSelected(blockHit.getBlockPos());
                NodeCraft.LOGGER.info("选中方块: {}", blockHit.getBlockPos());
                return true;
            }
        }
        
        return false;
    }
} 