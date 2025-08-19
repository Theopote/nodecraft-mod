package com.nodecraft.gui.editor;

import com.nodecraft.core.NodeCraft;
import com.nodecraft.gui.editor.base.INodeEditor;
import com.nodecraft.gui.editor.impl.ImGuiNodeEditor;
import com.nodecraft.gui.editor.impl.NativeNodeEditor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap; // For thread-safe caching

/**
 * 节点编辑器工厂类。
 * 负责发现、管理和创建节点编辑器实例，根据优先级和平台支持选择最佳编辑器。
 * 支持内置实现和通过 ServiceLoader 发现的外部实现。
 */
public class NodeEditorFactory {

    // 静态初始化块，用于在类加载时进行一次性检查
    private static final boolean IMGUI_LIBRARY_LOADED;
    static {
        boolean loaded = false;
        try {
            // 尝试加载 ImGui 类，如果成功则认为库已加载
            Class.forName("imgui.ImGui");
            loaded = true;
            NodeCraft.LOGGER.info("ImGui库类检测成功。");
        } catch (ClassNotFoundException e) {
            NodeCraft.LOGGER.error("ImGui库类未找到。如果需要ImGui编辑器，请确保ImGui库已正确包含在依赖中。");
        }
        IMGUI_LIBRARY_LOADED = loaded;
    }

    // 存储已加载的编辑器实现类 (不包括单例实例，它们通过 getOrCreateInstance 统一管理)
    private static final List<Class<? extends INodeEditor>> editorImplementations = new ArrayList<>();
    private static volatile boolean editorsLoaded = false; // 使用 volatile 确保多线程可见性

    // 缓存已创建的单例编辑器实例，使用 ConcurrentHashMap 确保线程安全
    private static final Map<Class<? extends INodeEditor>, INodeEditor> editorInstances = new ConcurrentHashMap<>();

    /**
     * 判断 ImGui 库是否在当前运行时环境中可用。
     * 这是一个一次性检查，结果会被缓存。
     * @return 如果 ImGui 库类可以被加载，则返回 true。
     */
    public static boolean isImGuiSupported() {
        return IMGUI_LIBRARY_LOADED;
    }

    /**
     * 加载所有内置和通过 ServiceLoader 发现的编辑器实现。
     * 此方法是线程安全的，并且只会执行一次。
     * 首次调用时，会填充 `editorImplementations` 列表。
     */
    public static synchronized void loadEditors() {
        if (editorsLoaded) {
            return;
        }
        NodeCraft.LOGGER.info("开始加载节点编辑器实现...");
        editorImplementations.clear(); // 确保列表为空，避免重复添加
        editorInstances.clear();     // 清除所有缓存的实例，以防重载

        // 1. 添加内置实现
        // ImGuiNodeEditor 是单例，我们不直接添加到这个列表，而是在 getOrCreateInstance 统一处理
        // NativeNodeEditor 也假设是单例，或可安全通过无参构造函数创建
        editorImplementations.add(NativeNodeEditor.class); // 先添加非 ImGui 的，或者 ImGui 的单例在 getOrCreateInstance 明确处理
        NodeCraft.LOGGER.info("已添加内置编辑器: NativeNodeEditor");

        // 2. 使用 ServiceLoader 加载外部实现
        // ServiceLoader 会尝试通过无参构造函数创建实例，如果 ImGuiNodeEditor 只有 getInstance()，这里会失败。
        // 因此，如果 ImGuiNodeEditor 确实是单例，不应该在 META-INF/services 中列出它。
        ServiceLoader<INodeEditor> loader = ServiceLoader.load(INodeEditor.class);
        try {
            for (INodeEditor editor : loader) {
                Class<? extends INodeEditor> implClass = editor.getClass();
                // 避免重复添加，特别是对于 ImGuiNodeEditor（如果它被列在 services 中）
                if (!editorImplementations.contains(implClass) && implClass != ImGuiNodeEditor.class) { // 明确排除 ImGuiNodeEditor
                    editorImplementations.add(implClass);
                    NodeCraft.LOGGER.info("发现外部编辑器: {} (ID: {}, Priority: {})", implClass.getName(), editor.getIdentifier(), editor.getPriority());
                } else if (implClass == ImGuiNodeEditor.class) {
                    NodeCraft.LOGGER.info("ServiceLoader 尝试加载 ImGuiNodeEditor，但它作为内置单例已特殊处理。");
                } else {
                    NodeCraft.LOGGER.warn("ServiceLoader 尝试重复加载编辑器: {}", implClass.getName());
                }
            }
        } catch (ServiceConfigurationError e) {
            NodeCraft.LOGGER.error("加载外部节点编辑器时出错: {}", e.getMessage(), e);
        }

        editorsLoaded = true;
        NodeCraft.LOGGER.info("节点编辑器加载完成，共发现 {} 种实现 (不包括特殊处理的单例 ImGuiNodeEditor)。", editorImplementations.size());
    }

    /**
     * 创建最佳的节点编辑器实例。
     * 它会加载所有编辑器（如果尚未加载），然后根据优先级和平台支持选择一个。
     * 选择逻辑：首先筛选出平台支持的编辑器，然后按优先级（数字越小优先级越高）排序，选择最佳的一个。
     *
     * @return 最佳的 INodeEditor 实例，如果找不到合适的则返回 null。
     */
    public static INodeEditor createEditor() {
        loadEditors(); // 确保编辑器实现已加载

        List<INodeEditor> candidates = new ArrayList<>();

        // 1. 特殊处理 ImGuiNodeEditor (因为它是单例且通过 getInstance() 获取)
        // 只有当 ImGui 库可用时，ImGuiNodeEditor 才可能是候选
        if (IMGUI_LIBRARY_LOADED) {
            try {
                INodeEditor imGuiEditorInstance = ImGuiNodeEditor.getInstance();
                // ImGuiNodeEditor 自身的 isPlatformSupported 应该会检查 ImGui 库是否真的可用
                if (imGuiEditorInstance != null && imGuiEditorInstance.isPlatformSupported()) {
                    candidates.add(imGuiEditorInstance);
                    editorInstances.put(ImGuiNodeEditor.class, imGuiEditorInstance); // 缓存实例
                    NodeCraft.LOGGER.info("找到支持的编辑器: ImGuiNodeEditor (ID: {}, 优先级: {})", imGuiEditorInstance.getIdentifier(), imGuiEditorInstance.getPriority());
                } else if (imGuiEditorInstance != null) {
                    NodeCraft.LOGGER.info("ImGuiNodeEditor (ID: {}) 不受当前平台支持或未通过其自身检查。", imGuiEditorInstance.getIdentifier());
                }
            } catch (Exception e) {
                NodeCraft.LOGGER.error("创建或检查 ImGuiNodeEditor 实例时出错", e);
            }
        } else {
            NodeCraft.LOGGER.info("ImGui库未加载，ImGuiNodeEditor 不会被考虑。");
        }


        // 2. 处理其他已发现的编辑器实现
        for (Class<? extends INodeEditor> implClass : editorImplementations) {
            if (implClass == ImGuiNodeEditor.class) continue; // 已经特殊处理过
            try {
                INodeEditor instance = getOrCreateInstance(implClass);
                if (instance != null && instance.isPlatformSupported()) {
                    candidates.add(instance);
                    NodeCraft.LOGGER.info("找到支持的编辑器: {} (ID: {}, 优先级: {})", instance.getIdentifier(), instance.getPriority(), instance.getPriority());
                } else if (instance != null) {
                    NodeCraft.LOGGER.info("编辑器 {} (ID: {}) 不受当前平台支持。", instance.getIdentifier(), instance.getIdentifier());
                }
            } catch (Exception e) {
                NodeCraft.LOGGER.error("创建或检查编辑器实例时出错: {}", implClass.getName(), e);
            }
        }

        // 3. 按优先级排序 (数字越小优先级越高)
        candidates.sort(Comparator.comparingInt(INodeEditor::getPriority));

        if (!candidates.isEmpty()) {
            INodeEditor bestChoice = candidates.getFirst(); // 优先级最低 (数字最小) 的编辑器
            NodeCraft.LOGGER.info("已选择编辑器: {} (Class: {})", bestChoice.getIdentifier(), bestChoice.getClass().getSimpleName());
            return bestChoice;
        } else {
            NodeCraft.LOGGER.error("找不到任何受支持的节点编辑器实现！尝试使用NativeNodeEditor作为回退选项。");
            // 作为最后的回退选项，尝试强制创建 NativeNodeEditor
            try {
                // 此时 NativeNodeEditor 实例可能已经在 candidates 中，如果它支持平台。
                // 这里的逻辑应更安全地从缓存中获取或再次创建。
                INodeEditor nativeEditor = NativeNodeEditor.getInstance(); // 假设 NativeNodeEditor 也是单例
                if (nativeEditor != null && nativeEditor.isPlatformSupported()) {
                    NodeCraft.LOGGER.info("成功创建并选择回退编辑器: NativeNodeEditor。");
                    return nativeEditor;
                }
                NodeCraft.LOGGER.error("回退到 NativeNodeEditor 失败。");
                return null;
            } catch (Exception e) {
                NodeCraft.LOGGER.error("无法创建回退编辑器 NativeNodeEditor", e);
                return null;
            }
        }
    }

    /**
     * 获取或创建编辑器类的单例实例。
     * 对于像 ImGuiNodeEditor 这样的单例，它会调用 getInstance()。
     * 对于其他类，它会尝试调用无参构造函数。
     * 实例会被缓存。
     * @param implClass 编辑器实现类。
     * @return 编辑器实例。
     */
    private static INodeEditor getOrCreateInstance(Class<? extends INodeEditor> implClass) {
        // 使用 ConcurrentHashMap 的 computeIfAbsent 避免双重检查锁定，并确保线程安全
        return editorInstances.computeIfAbsent(implClass, (key) -> {
            try {
                if (key == ImGuiNodeEditor.class) {
                    return ImGuiNodeEditor.getInstance(); // 特殊处理 ImGuiNodeEditor 单例
                } else if (key == NativeNodeEditor.class) {
                    return NativeNodeEditor.getInstance(); // 特殊处理 NativeNodeEditor 单例 (如果它也是单例)
                } else {
                    // 尝试使用无参构造函数创建新实例
                    return key.getDeclaredConstructor().newInstance();
                }
            } catch (NoSuchMethodException e) {
                NodeCraft.LOGGER.error("编辑器实现 {} 没有无参构造函数，或 getInstance() 方法。", key.getName(), e);
                throw new RuntimeException("Editor implementation " + key.getName() + " has no suitable constructor/getInstance method.", e);
            } catch (Exception e) {
                NodeCraft.LOGGER.error("无法实例化编辑器: {}", key.getName(), e);
                throw new RuntimeException("Failed to instantiate editor " + key.getName(), e);
            }
        });
    }
}