package com.nodecraft.gui.components.property.core;

import com.nodecraft.gui.components.PropertyPanelComponent;
import com.nodecraft.nodesystem.api.INode;

@FunctionalInterface
public interface PropertyRenderer {
    void render(PropertyPanelComponent panel, INode node, PropertyDescriptor prop, boolean isDisabled);
}
