package com.nodecraft.core.exception;

/**
 * Geometry construction or solid-operation errors outside plain value-object checks.
 */
public class GeometryException extends NodeValidationException {

    public GeometryException(String message) {
        super(message);
    }

    public GeometryException(String message, Throwable cause) {
        super(message, cause);
    }
}
