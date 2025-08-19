package com.nodecraft.nodesystem.util;

/**
 * 表示Minecraft世界中的整数坐标
 * 专门用于方块位置，区别于浮点数的Vector3
 */
public class Coordinate {
    private final int x;
    private final int y;
    private final int z;
    
    public Coordinate(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getZ() {
        return z;
    }
    
    /**
     * 转换为Vector3（浮点数形式）
     */
    public Vector3 toVector3() {
        return new Vector3(x, y, z);
    }
    
    /**
     * 从Vector3创建Coordinate（四舍五入）
     */
    public static Coordinate fromVector3(Vector3 vector) {
        return new Coordinate(
            Math.round(vector.getX()),
            Math.round(vector.getY()),
            Math.round(vector.getZ())
        );
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Coordinate that = (Coordinate) obj;
        return x == that.x && y == that.y && z == that.z;
    }
    
    @Override
    public int hashCode() {
        return 31 * (31 * x + y) + z;
    }
    
    @Override
    public String toString() {
        return String.format("(%d, %d, %d)", x, y, z);
    }
} 