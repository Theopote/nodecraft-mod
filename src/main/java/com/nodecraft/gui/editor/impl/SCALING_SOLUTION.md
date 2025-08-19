# NodeCraft 统一缩放架构解决方案

## 问题背景

在 NodeCraft 节点编辑器中，存在两套不同的缩放系统：

1. **通用节点元素**（文字、连接点、节点边框）：使用直接数学缩放 `element * canvasZoom`
2. **ImGui 自定义控件**（滑动条、输入框、按钮）：只能通过 `FontGlobalScale` 进行有限缩放

这导致了以下问题：
- 自定义控件的边框、内边距、交互区域无法正确缩放
- 控件在缩放时只是被裁剪，而不是真正的缩放
- 用户体验不一致

## 解决方案：画布级别统一缩放

### 核心思路

将缩放处理从**逐个元素缩放**改为**画布级别统一缩放**：

- **之前**：每个元素单独乘以 `canvasZoom`
- **现在**：在 `CustomUIRenderer` 中统一应用所有 ImGui 样式的缩放变换

### 技术实现

#### 1. CustomUIRenderer 统一缩放

在 `CustomUIRenderer.renderSingleCustomUIWithChildWindow()` 中：

```java
// 保存原始样式状态
float originalFontScale = ImGui.getIO().getFontGlobalScale();
float originalFramePadding = ImGui.getStyle().getFramePaddingX();
// ... 保存所有相关样式

// 应用统一缩放变换
float zoom = info.zoom;
ImGui.getIO().setFontGlobalScale(zoom);
ImGui.getStyle().setFramePadding(originalFramePadding * zoom, ...);
ImGui.getStyle().setItemSpacing(originalItemSpacing * zoom, ...);
ImGui.getStyle().setFrameBorderSize(originalFrameBorderSize * zoom);
ImGui.getStyle().setScrollbarSize(originalScrollbarSize * zoom);
// ... 缩放所有相关样式

try {
    // 渲染自定义UI - 现在所有控件都会正确缩放
    info.customUINode.renderCustomUI(safeWidth, safeHeight, zoom);
} finally {
    // 恢复原始样式状态
    ImGui.getIO().setFontGlobalScale(originalFontScale);
    // ... 恢复所有样式
}
```

#### 2. 节点内部简化

在自定义节点（如 `SelectedBlockNode`）中：

```java
// 之前：需要手动处理缩放
float availableWidth = getAvailableWidth(width, zoom);

// 现在：直接使用已缩放的值
float availableWidth = width - (2 * getMediumPadding());
```

#### 3. 主题系统适配

`MinecraftUITheme` 现在专注于颜色主题，缩放由 `CustomUIRenderer` 统一处理。

### 涉及的样式属性

以下 ImGui 样式属性现在会统一缩放：

- `FontGlobalScale` - 字体缩放
- `FramePadding` - 控件内边距
- `ItemSpacing` - 控件间距
- `IndentSpacing` - 缩进间距
- `FrameBorderSize` - 边框粗细
- `FrameRounding` - 边框圆角
- `GrabRounding` - 滑块圆角
- `ScrollbarSize` - 滚动条大小
- `ScrollbarRounding` - 滚动条圆角
- `GrabMinSize` - 滑块最小尺寸

## 优势

1. **完美的缩放一致性**：所有 UI 元素（通用和自定义）现在使用相同的缩放系统
2. **真正的控件缩放**：ImGui 控件的所有部分都会正确缩放，而不是被裁剪
3. **简化的节点实现**：节点内部不再需要手动处理复杂的缩放逻辑
4. **线程安全**：每个自定义UI渲染都有独立的样式作用域
5. **性能优化**：减少了重复的缩放计算

## 兼容性

- 现有的节点代码只需要少量修改
- 主题系统保持向后兼容
- 通用节点元素的渲染逻辑不变

## 使用指南

### 对于新节点开发者

```java
@Override
protected boolean renderCustomUIScaled(float width, float height, float zoom) {
    // 不需要手动处理缩放，直接使用 width 和 height
    // 所有 ImGui 控件会自动正确缩放
    
    if (ImGui.button("示例按钮", width * 0.8f, 0)) {
        // 按钮会完美缩放
    }
    
    return ImGui.getIO().getWantCaptureMouse();
}
```

### 对于现有节点迁移

1. 移除手动的 `zoom` 参数传递
2. 直接使用传入的 `width` 和 `height`
3. 移除自定义的样式缩放代码
4. 保持主题系统调用（如 `MinecraftUITheme.apply(1.0f)`）

## 关键修复：避免双重缩放

### 问题诊断

初始实现中存在**双重缩放**问题：

1. `ImGuiNodeRenderer` 中计算 `customUIActualRenderWidth` 时已经应用了缩放
2. `CustomUIRenderer` 中又再次应用了缩放变换
3. 导致自定义UI的缩放速度比通用元素慢很多

### 解决方案

**修改前**（双重缩放）：
```java
// ImGuiNodeRenderer 中
float customUIActualRenderWidth = (finalNodeWidthScaled / canvasZoom - 2 * NodeRenderConstants.NODE_HORIZONTAL_PADDING) * canvasZoom;

// CustomUIRenderer 中
ImGui.getStyle().setFramePadding(originalFramePadding * zoom, ...); // 再次缩放
```

**修改后**（单次缩放）：
```java
// ImGuiNodeRenderer 中 - 传递逻辑尺寸
float customUILogicalWidth = finalNodeWidthScaled / canvasZoom - 2 * NodeRenderConstants.NODE_HORIZONTAL_PADDING;

// CustomUIRenderer 中 - 只在这里应用缩放
float scaledWidth = info.width * info.zoom;
ImGui.getStyle().setFramePadding(originalFramePadding * zoom, ...);
```

### 数据流图

```
画布缩放 (canvasZoom) 
    ↓
通用元素: element * canvasZoom ✓
    ↓
自定义UI: 逻辑尺寸 → CustomUIRenderer → 统一缩放变换 ✓
```

## 测试建议

1. **缩放一致性测试**：验证自定义控件与通用元素的缩放速度完全一致
2. 测试不同缩放级别下的控件交互
3. 验证控件边框和内边距的正确缩放
4. 检查滑动条、输入框等复杂控件的行为
5. 确认主题颜色的正确应用

## 性能优化

- 避免了重复的缩放计算
- 统一的样式变换减少了状态切换开销
- 缓存友好的逻辑尺寸传递 