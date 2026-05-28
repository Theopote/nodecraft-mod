package com.nodecraft.gui.components.property.renderers;

import com.nodecraft.gui.components.PropertyPanelComponent;
import com.nodecraft.gui.components.property.core.PropertyDescriptor;
import com.nodecraft.gui.components.property.core.PropertyRenderer;
import com.nodecraft.gui.components.property.core.PropertyValueFormatters;
import com.nodecraft.nodesystem.api.INode;
import com.nodecraft.nodesystem.datatypes.HemisphereGeometryData;
import imgui.ImGui;

public final class HemisphereGeometryPropertyRenderer {
    public static final PropertyRenderer RENDERER = HemisphereGeometryPropertyRenderer::render;

    private HemisphereGeometryPropertyRenderer() {
    }

    private static void render(PropertyPanelComponent panel, INode node, PropertyDescriptor prop, boolean isDisabled) {
        try {
            HemisphereGeometryData hemisphere = (HemisphereGeometryData) prop.getter.invoke(node);
            if (hemisphere == null) {
                ImGui.textDisabled("(null)");
                return;
            }

            ImGui.text("Center: " + PropertyValueFormatters.formatVector3d(hemisphere.getCenter()));
            ImGui.text("Axis: " + PropertyValueFormatters.formatVector3d(hemisphere.getAxis()));
            ImGui.text(String.format("Radius: %.2f", hemisphere.getRadius()));
        } catch (Throwable e) {
            panel.handlePropertyError(prop, e);
        }
    }
}
