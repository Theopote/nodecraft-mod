package com.nodecraft.nodesystem.preview.protocol;

public final class PreviewVector {
    private final double originX;
    private final double originY;
    private final double originZ;
    private final double dirX;
    private final double dirY;
    private final double dirZ;

    public PreviewVector(double originX, double originY, double originZ, double dirX, double dirY, double dirZ) {
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
        this.dirX = dirX;
        this.dirY = dirY;
        this.dirZ = dirZ;
    }

    public double originX() {
        return originX;
    }

    public double originY() {
        return originY;
    }

    public double originZ() {
        return originZ;
    }

    public double dirX() {
        return dirX;
    }

    public double dirY() {
        return dirY;
    }

    public double dirZ() {
        return dirZ;
    }
}
