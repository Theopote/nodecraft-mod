package com.nodecraft.gui.components;

import com.nodecraft.nodesystem.api.INode;
import com.nodecraft.nodesystem.datatypes.SquarePyramidGeometryData;
import imgui.ImGui;

final class SquarePyramidGeometryPropertyRenderer {

    static final PropertyRenderer RENDERER = SquarePyramidGeometryPropertyRenderer::render;

    private SquarePyramidGeometryPropertyRenderer() {
    }

    private static void render(PropertyPanelComponent panel, INode node, PropertyDescriptor prop, boolean isDisabled) {
        try {
            SquarePyramidGeometryData pyramid = (SquarePyramidGeometryData) prop.getter.invoke(node);
            if (pyramid == null) {
                ImGui.textDisabled("(null)");
                return;
            }

            ImGui.text("Base Center: " + PropertyValueFormatters.formatVector3d(pyramid.getBaseCenter()));
            ImGui.text("Apex: " + PropertyValueFormatters.formatVector3d(pyramid.getApex()));
            ImGui.text(String.format("Base Size: %.2f", pyramid.getBaseSize()));
            ImGui.text(String.format("Height: %.2f", pyramid.getHeight()));
            ImGui.text("Normal: " + PropertyValueFormatters.formatVector3d(pyramid.getNormal()));
        } catch (Throwable e) {
            panel.handlePropertyError(prop, e);
        }
    }
}
