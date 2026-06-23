package com.nodecraft.gui.editor;

import com.nodecraft.gui.editor.base.GraphApplyTarget;
import com.nodecraft.gui.editor.base.INodeEditor;
import com.nodecraft.gui.editor.impl.ImGuiNodeEditor;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves the active editor as a {@link GraphApplyTarget} when supported.
 */
public final class GraphApplyTargetResolver {

    private GraphApplyTargetResolver() {
    }

    @Nullable
    public static GraphApplyTarget resolve() {
        INodeEditor editor = NodeEditorFactory.createEditor();
        if (editor instanceof GraphApplyTarget target) {
            return target;
        }

        ImGuiNodeEditor imGuiEditor = ImGuiNodeEditor.getInstance();
        if (imGuiEditor != null && imGuiEditor.isPlatformSupported()) {
            return imGuiEditor;
        }
        return null;
    }
}
