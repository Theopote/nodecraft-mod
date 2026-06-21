package com.nodecraft.core.exception;

/**
 * Invalid graph structure or subgraph extraction/serialization operations.
 */
public class GraphException extends NodeCraftException {

    public GraphException(String message) {
        super(message);
    }

    public GraphException(String message, Throwable cause) {
        super(message, cause);
    }
}
