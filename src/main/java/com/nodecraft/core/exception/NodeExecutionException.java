package com.nodecraft.core.exception;

/**
 * Failures while running node graphs or instantiating executable node types.
 */
public class NodeExecutionException extends NodeCraftException {

    public NodeExecutionException(String message) {
        super(message);
    }

    public NodeExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
