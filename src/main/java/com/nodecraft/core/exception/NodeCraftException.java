package com.nodecraft.core.exception;

/**
 * Base unchecked exception for NodeCraft domain errors.
 */
public class NodeCraftException extends RuntimeException {

    public NodeCraftException(String message) {
        super(message);
    }

    public NodeCraftException(String message, Throwable cause) {
        super(message, cause);
    }
}
