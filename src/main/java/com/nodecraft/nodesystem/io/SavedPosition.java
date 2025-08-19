package com.nodecraft.nodesystem.io;

/**
 * Represents a node's position on the canvas for saving/loading.
 */
public class SavedPosition {
    public float x;
    public float y;

    // Default constructor for Gson
    public SavedPosition() {}

    // Constructor for easy creation
    public SavedPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
} 