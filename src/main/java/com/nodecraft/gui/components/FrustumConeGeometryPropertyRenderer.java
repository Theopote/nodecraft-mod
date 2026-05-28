package com.nodecraft.gui.components;

import com.nodecraft.nodesystem.api.INode;
import com.nodecraft.nodesystem.datatypes.FrustumConeGeometryData;
import imgui.ImGui;
import org.joml.Vector3d;

final class FrustumConeGeometryPropertyRenderer {

    static final PropertyRenderer RENDERER = FrustumConeGeometryPropertyRenderer::render;

    private FrustumConeGeometryPropertyRenderer() {
    }

    private static void render(PropertyPanelComponent panel, INode node, PropertyDescriptor prop, boolean isDisabled) {
        try {
            FrustumConeGeometryData frustum = (FrustumConeGeometryData) prop.getter.invoke(node);
            if (frustum == null) {
                ImGui.textDisabled("(null)");
                return;
            }

            ImGui.text("Base Center: " + formatVector3d(frustum.getBaseCenter()));
            ImGui.text("Top Center: " + formatVector3d(frustum.getTopCenter()));
            ImGui.text("Axis: " + formatVector3d(frustum.getAxisVector()));
            ImGui.text(String.format("Height: %.2f", frustum.getHeight()));
            ImGui.text(String.format("Base Radius: %.2f", frustum.getBaseRadius()));
            ImGui.text(String.format("Top Radius: %.2f", frustum.getTopRadius()));
        } catch (Throwable e) {
            panel.handlePropertyError(prop, e);
        }
    }

    private static String formatVector3d(Vector3d vec) {
        if (vec == null) {
            return "(null)";
        }
        return String.format("(%.2f, %.2f, %.2f)", vec.x, vec.y, vec.z);
    }
}
