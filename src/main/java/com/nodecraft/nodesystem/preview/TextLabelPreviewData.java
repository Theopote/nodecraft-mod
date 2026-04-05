package com.nodecraft.nodesystem.preview;

import net.minecraft.util.math.Vec3d;

public class TextLabelPreviewData {

    private final Vec3d position;
    private final String text;

    public TextLabelPreviewData(Vec3d position, String text) {
        this.position = position;
        this.text = text;
    }

    public Vec3d getPosition() {
        return position;
    }

    public String getText() {
        return text;
    }
}
