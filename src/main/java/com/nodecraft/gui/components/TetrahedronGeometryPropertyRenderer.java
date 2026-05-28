package com.nodecraft.gui.components;

import com.nodecraft.nodesystem.api.INode;
import com.nodecraft.nodesystem.datatypes.TetrahedronGeometryData;
import imgui.ImGui;

final class TetrahedronGeometryPropertyRenderer {

    static final PropertyRenderer RENDERER = TetrahedronGeometryPropertyRenderer::render;

    private TetrahedronGeometryPropertyRenderer() {
    }

    private static void render(PropertyPanelComponent panel, INode node, PropertyDescriptor prop, boolean isDisabled) {
        try {
            TetrahedronGeometryData tetrahedron = (TetrahedronGeometryData) prop.getter.invoke(node);
            if (tetrahedron == null) {
                ImGui.textDisabled("(null)");
                return;
            }

            ImGui.text("Center: " + PropertyValueFormatters.formatVector3d(tetrahedron.getCenter()));
            ImGui.text(String.format("Edge Length: %.2f", tetrahedron.getEdgeLength()));
            ImGui.text(String.format("Circumradius: %.2f", tetrahedron.getCircumradius()));
            ImGui.text("Vertices: " + tetrahedron.getVertices().size());
        } catch (Throwable e) {
            panel.handlePropertyError(prop, e);
        }
    }
}
