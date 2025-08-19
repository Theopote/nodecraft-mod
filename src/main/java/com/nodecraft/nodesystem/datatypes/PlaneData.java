package com.nodecraft.nodesystem.datatypes;

import org.joml.Vector3d;
import org.joml.Vector4d;
import org.joml.Math;
import java.util.Objects;
import net.minecraft.util.math.Vec3d;

/**
 * 一个表示平面的简单数据结构。
 * 平面用方程 ax + by + cz + d = 0 表示，其中 (a,b,c) 是法线向量，d 是平面常数。
 */
public class PlaneData {
    private final Vector4d plane; // x,y,z = 法线, w = 平面常数
    
    // 预定义常用平面常量
    public static final PlaneData XY_PLANE = new PlaneData(new Vector3d(0, 0, 0), new Vector3d(0, 0, 1));
    public static final PlaneData YZ_PLANE = new PlaneData(new Vector3d(0, 0, 0), new Vector3d(1, 0, 0));
    public static final PlaneData XZ_PLANE = new PlaneData(new Vector3d(0, 0, 0), new Vector3d(0, 1, 0));

    // 从原点和法线向量构造 (JOML Vector3d)
    public PlaneData(Vector3d origin, Vector3d normal) {
        Vector3d normalizedNormal = new Vector3d(normal).normalize();
        this.plane = new Vector4d(normalizedNormal.x, normalizedNormal.y, normalizedNormal.z, 
                                 -normalizedNormal.dot(origin));
    }
    
    // 从原点和法线向量构造 (Minecraft Vec3d)
    public PlaneData(Vec3d origin, Vec3d normal) {
        // 转换Minecraft的Vec3d到JOML的Vector3d
        Vector3d originJoml = new Vector3d(origin.x, origin.y, origin.z);
        Vector3d normalJoml = new Vector3d(normal.x, normal.y, normal.z).normalize();
        
        this.plane = new Vector4d(normalJoml.x, normalJoml.y, normalJoml.z, 
                                 -normalJoml.dot(originJoml));
    }

    // 从平面上的三个点构造 (JOML Vector3d)
    public PlaneData(Vector3d p1, Vector3d p2, Vector3d p3) {
        Vector3d v1 = new Vector3d();
        Vector3d v2 = new Vector3d();
        Vector3d normal = new Vector3d();
        
        // 计算两个向量
        p2.sub(p1, v1);
        p3.sub(p1, v2);
        
        // 计算法线
        v1.cross(v2, normal);
        normal.normalize();
        
        // 计算平面常数
        double d = -normal.dot(p1);
        
        this.plane = new Vector4d(normal, d);
    }
    
    // 直接从Vector4d构造
    public PlaneData(Vector4d plane) {
        this.plane = new Vector4d(plane); // 防御性复制
    }

    // 获取内部平面表示
    public Vector4d getPlane() {
        return new Vector4d(plane); // 防御性复制
    }
    
    // 获取平面的法线 (JOML Vector3d)
    public Vector3d getNormal() {
        return new Vector3d(plane.x, plane.y, plane.z);
    }
    
    // 获取平面的法线 (Minecraft Vec3d) - 为了兼容现有代码
    public Vec3d normal() {
        return new Vec3d(plane.x, plane.y, plane.z);
    }
    
    // 获取平面的点法式中的点（任何在平面上的点）
    public Vector3d getPoint() {
        // 如果法线x分量不为0，取点(d/nx, 0, 0)
        if (Math.abs(plane.x) > 1e-6) {
            return new Vector3d(-plane.w / plane.x, 0, 0);
        }
        // 如果法线y分量不为0，取点(0, d/ny, 0)
        else if (Math.abs(plane.y) > 1e-6) {
            return new Vector3d(0, -plane.w / plane.y, 0);
        }
        // 否则取点(0, 0, d/nz)
        else {
            return new Vector3d(0, 0, -plane.w / plane.z);
        }
    }
    
    // 计算点到平面的距离
    public double distanceTo(Vector3d point) {
        return Math.abs(signedDistanceTo(point));
    }
    
    // 计算点到平面的有符号距离
    public double signedDistanceTo(Vector3d point) {
        return plane.x * point.x + plane.y * point.y + plane.z * point.z + plane.w;
    }
    
    // 将点投影到平面上
    public Vector3d projectPoint(Vector3d point) {
        double t = -(plane.x * point.x + plane.y * point.y + plane.z * point.z + plane.w) / 
                  (plane.x * plane.x + plane.y * plane.y + plane.z * plane.z);
        return new Vector3d(
            point.x + plane.x * t,
            point.y + plane.y * t,
            point.z + plane.z * t
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaneData planeData = (PlaneData) o;
        return Objects.equals(plane, planeData.plane);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plane);
    }
    
    @Override
    public String toString() {
        return "Plane[normal=(" + plane.x + ", " + plane.y + ", " + plane.z + "), d=" + plane.w + "]";
    }
} 