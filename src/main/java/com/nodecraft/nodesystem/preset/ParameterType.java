package com.nodecraft.nodesystem.preset;

/**
 * Types of parameters that can be exposed in presets.
 */
public enum ParameterType {
    INTEGER,
    FLOAT,
    BOOLEAN,
    STRING,
    DROPDOWN,
    BLOCK_SELECTOR,
    COLOR,
    VECTOR3,
    ANGLE;

    public static ParameterType fromString(String value) {
        if (value == null) {
            return STRING;
        }
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return STRING;
        }
    }
}
