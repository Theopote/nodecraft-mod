package com.nodecraft.nodesystem.datatypes;

/**
 * A simple data structure to represent an RGBA color.
 * Values are typically in the range [0.0, 1.0].
 */
public record ColorData(float r, float g, float b, float a) {
    
    public static final ColorData BLACK = new ColorData(0, 0, 0, 1);
    public static final ColorData WHITE = new ColorData(1, 1, 1, 1);
    public static final ColorData RED = new ColorData(1, 0, 0, 1);
    public static final ColorData GREEN = new ColorData(0, 1, 0, 1);
    public static final ColorData BLUE = new ColorData(0, 0, 1, 1);

    public ColorData(float r, float g, float b, float a) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
        this.a = clamp(a);
    }

    // Convenience constructor for RGB, alpha defaults to 1.0
    public ColorData(float r, float g, float b) {
        this(r, g, b, 1.0f);
    }

    private static float clamp(float val) {
        return Math.max(0.0f, Math.min(1.0f, val));
    }
    
    /**
     * Creates a ColorData from an integer representation (e.g., 0xAARRGGBB).
     */
    public static ColorData fromIntARGB(int argb) {
        float a = ((argb >> 24) & 0xFF) / 255.0f;
        float r = ((argb >> 16) & 0xFF) / 255.0f;
        float g = ((argb >> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;
        return new ColorData(r, g, b, a);
    }
    
    /**
     * Creates a ColorData from an integer representation (e.g., 0xRRGGBB), alpha is 1.0.
     */
     public static ColorData fromIntRGB(int rgb) {
         return fromIntARGB(0xFF000000 | rgb);
     }

    /**
     * Converts the color to an integer representation (0xAARRGGBB).
     */
    public int toIntARGB() {
        int aInt = (int)(clamp(a) * 255.0f + 0.5f) << 24;
        int rInt = (int)(clamp(r) * 255.0f + 0.5f) << 16;
        int gInt = (int)(clamp(g) * 255.0f + 0.5f) << 8;
        int bInt = (int)(clamp(b) * 255.0f + 0.5f);
        return aInt | rInt | gInt | bInt;
    }
} 