package com.nodecraft.gui.components;

import com.nodecraft.nodesystem.api.INode;
import com.nodecraft.nodesystem.datatypes.PolylineData;
import imgui.ImGui;

final class PolylinePropertyRenderer {

    static final PropertyRenderer RENDERER = PolylinePropertyRenderer::render;

    private PolylinePropertyRenderer() {
    }

    private static void render(PropertyPanelComponent panel, INode node, PropertyDescriptor prop, boolean isDisabled) {
        try {
            PolylineData polyline = (PolylineData) prop.getter.invoke(node);
            if (polyline == null) {
                ImGui.textDisabled("(null)");
                return;
            }

            ImGui.text("Points: " + polyline.getPointCount());
            ImGui.text("Segments: " + polyline.getSegmentCount());
            ImGui.text(String.format("Length: %.2f", polyline.getLength()));
            ImGui.text("Closed: " + (polyline.isClosed() ? "Yes" : "No"));
        } catch (Throwable e) {
            panel.handlePropertyError(prop, e);
        }
    }
}
