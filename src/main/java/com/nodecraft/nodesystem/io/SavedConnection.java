package com.nodecraft.nodesystem.io;

/**
 * Represents a connection between two ports for saving/loading.
 */
public class SavedConnection {
    public String sourceNodeId; // UUID as String
    public String sourcePortId;
    public String targetNodeId; // UUID as String
    public String targetPortId;

    // Default constructor for Gson
    public SavedConnection() {}
} 