package com.nodecraft.nodesystem.util;

import org.joml.Vector3d;

import java.util.List;

/**
 * Closest point on a polyline (ordered vertices) in 3D, optionally with segment tangent at the closest location.
 */
public final class PolylineClosestPoint3d {

    private static final double EPS = 1.0e-18d;

    private PolylineClosestPoint3d() {
    }

    public static void closestPoint(List<Vector3d> polyline, Vector3d query, Vector3d destClosest) {
        closestPointAndTangent(polyline, query, destClosest, null);
    }

    /**
     * Finds the closest point on the polyline to {@code query}. If {@code destTangent} is non-null, writes the
     * unit direction of the segment that contains the closest point (fallback axis if degenerate).
     */
    public static void closestPointAndTangent(List<Vector3d> polyline, Vector3d query, Vector3d destClosest, Vector3d destTangent) {
        if (polyline == null || polyline.isEmpty()) {
            destClosest.set(query);
            if (destTangent != null) {
                destTangent.set(1.0d, 0.0d, 0.0d);
            }
            return;
        }
        if (polyline.size() == 1) {
            destClosest.set(polyline.getFirst());
            if (destTangent != null) {
                destTangent.set(1.0d, 0.0d, 0.0d);
            }
            return;
        }
        double bestD2 = Double.POSITIVE_INFINITY;
        Vector3d best = new Vector3d(polyline.getFirst());
        Vector3d bestTan = new Vector3d(1.0d, 0.0d, 0.0d);
        for (int i = 0; i < polyline.size() - 1; i++) {
            Vector3d a = polyline.get(i);
            Vector3d b = polyline.get(i + 1);
            Vector3d ab = new Vector3d(b).sub(a);
            double ab2 = ab.lengthSquared();
            double t = ab2 < EPS ? 0.0d : Math.max(0.0d, Math.min(1.0d, new Vector3d(query).sub(a).dot(ab) / ab2));
            Vector3d cand = new Vector3d(a).fma(t, ab);
            double d2 = cand.distanceSquared(query);
            if (d2 < bestD2) {
                bestD2 = d2;
                best.set(cand);
                if (ab2 >= EPS) {
                    bestTan.set(ab).normalize();
                } else {
                    bestTan.set(1.0d, 0.0d, 0.0d);
                }
            }
        }
        destClosest.set(best);
        if (destTangent != null) {
            destTangent.set(bestTan);
        }
    }
}
