package com.nodecraft.gui.components;

import com.nodecraft.nodesystem.api.INode;
import imgui.ImGui;

import java.util.List;

final class ListPropertyRenderer {

    static final PropertyRenderer RENDERER = ListPropertyRenderer::render;

    private ListPropertyRenderer() {
    }

    private static void render(PropertyPanelComponent panel, INode node, PropertyDescriptor prop, boolean isDisabled) {
        try {
            Object value = prop.getter.invoke(node);
            if (!(value instanceof List<?> list)) {
                if (value == null) {
                    ImGui.textDisabled("(null)");
                } else {
                    ImGui.textWrapped(value.toString());
                }
                return;
            }

            panel.renderList(list, prop.displayName);
        } catch (Throwable e) {
            panel.handlePropertyError(prop, e);
        }
    }
}
