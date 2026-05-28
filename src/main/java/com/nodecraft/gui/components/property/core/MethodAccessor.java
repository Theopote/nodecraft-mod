package com.nodecraft.gui.components.property.core;

public interface MethodAccessor {
    Object invoke(Object obj, Object... args) throws Throwable;

    Class<?> getReturnType();

    Class<?>[] getParameterTypes();
}
