package com.nodecraft.gui.components.property.core;

public final class PropertyDescriptor {
    public final String name;
    public final String displayName;
    public final Class<?> type;
    public final MethodAccessor getter;
    public final MethodAccessor setter;
    public final PropertyRenderer renderer;
    public final String description;
    public final String category;
    public final int order;

    public PropertyDescriptor(
            String name,
            String displayName,
            Class<?> type,
            MethodAccessor getter,
            MethodAccessor setter,
            PropertyRenderer renderer,
            String description,
            String category,
            int order
    ) {
        this.name = name;
        this.displayName = displayName;
        this.type = type;
        this.getter = getter;
        this.setter = setter;
        this.renderer = renderer;
        this.description = description;
        this.category = category;
        this.order = order;
    }
}
