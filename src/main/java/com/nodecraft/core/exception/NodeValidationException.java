package com.nodecraft.core.exception;

/**
 * Invalid inputs, unknown node types, malformed user/file data at API boundaries.
 */
public class NodeValidationException extends NodeCraftException {

    public NodeValidationException(String message) {
        super(message);
    }

    public NodeValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
