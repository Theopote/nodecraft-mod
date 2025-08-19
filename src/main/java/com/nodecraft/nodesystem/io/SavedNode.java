package com.nodecraft.nodesystem.io;

/**
 * Represents a node's data for saving/loading.
 */
public class SavedNode {
    public String nodeId; // UUID as String
    public String typeId; // Registered type ID (e.g., "math.operators.add")
    public Object state; // Node-specific state (e.g., value for NumberInputNode)

    // Default constructor for Gson
    public SavedNode() {}
} 